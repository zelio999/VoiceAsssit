package com.fluxsoft.voiceassist.service.voicein;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import com.fluxsoft.voiceassist.event.EventVoice;
import com.fluxsoft.voiceassist.service.AbstractService;
import com.fluxsoft.voiceassist.service.util.Util;
import com.fluxsoft.voiceassist.singleton.RouterServiceVoice;
import com.fluxsoft.voiceassist.singleton.ServiceVoice;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Clase de tipo servicio que administra el servicio de reconomciento de voz
 * Created by Cristian on 17/06/2016.
 */
@SuppressWarnings("ALL")
public class ServiceInVoice extends AbstractService implements RecognitionListener {

    private final String LIB_LOGTAG = this.getClass().getSimpleName(); //this.getClass().getName();

    public AudioManager mAudioManager;
    protected SpeechRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;
    protected Messenger mServerMessenger;
    protected volatile boolean isContinuo;
    protected volatile boolean mIsCountDownOn;

    protected long lastStart;
    public ArrayList<String> nBestList;
    private Integer errorCode;
    protected boolean mIsListening = false;

    public static final int MSG_RECOGNIZER_START = 11;
    public static final int MSG_RECOGNIZER_CANCEL = 12;

    public static final int MSG_RECOGNIZER_RESULT = 13;
    public static final int MSG_RECOGNIZER_INITIATE = 14;
    public static final int MSG_RECOGNIZER_STOP = 15;

    protected PowerManager.WakeLock wakeLock;
    protected Context context;
    protected int storedVolume;
    protected CountDownTimer mTimerRestartRecognizer;

    /**
     * Metodo que se ejecuta cuando se inicializa el servicio dentro de la aplicacion
     */
    @Override
    public void onStartService() {
        mIsListening = false;
        isContinuo = true;
        this.context = this;

        if (intent != null && wakeLock == null) {
            mServerMessenger = new Messenger(new IncomingHandler(this));

            if (mTimerRestartRecognizer == null)
                mTimerRestartRecognizer = new TimerRestartRecognizer((long) 2000, (long) 500, this);//5000

            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

            if (wakeLock == null || !wakeLock.isHeld()) {
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ServiceInVoiceWakelock");
                wakeLock.acquire();
                if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
                    Log.d(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> Wakelock Aquired <<<<");
            }

            if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
                Log.d(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> Creo el Servicio Continuo Voz <<<<"); //$NON-NLS-1$
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            mSpeechRecognizer.setRecognitionListener(this);

            mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.getDefault());// Especificar el idioma de lenguaje
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 4000);
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);// Especificar el número de resultados a recibir. Los resultados aparecen en orden de confianza

        } else if (intent != null) {
            mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.getDefault());// Especificar el idioma de lenguaje
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 4000);
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);// Especificar el número de resultados a recibir. Los resultados aparecen en orden de confianza

            sendCancelListeningMessage();
        }
    }

    /**
     * Metodo que se ejecuta cuando se detiene el servicio dentro de la aplicacion
     */
    @Override
    public void onStopService(){

        cancelCountdown();
        sendCancelListeningMessage();

        if (this.wakeLock != null) {
            if (this.wakeLock.isHeld()) {
                this.wakeLock.release();
                if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
                    Log.d(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> Wakelock Released <<<<"); //$NON-NLS-1$
            }
            this.wakeLock = null;
        }

        if (this.mServerMessenger != null)
            mServerMessenger = null;

        Util.mute(this.mAudioManager, false);
        if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
            Log.d(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> Detenido el Continuous Service <<<<");
    }

    /**
     * Captura el mensaje y realiza la tarea a ejecutar relacionado con el mensaje
     * @param msg mensaje de operacion a realizar
     */
    @Override
    public void onReceiveMessage(Message msg) {
        // Get argument from message, take square root and respond right away
        if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
            Log.d(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> Llego mensaje: " + msg.what + " <<<<");

        if (msg.what == MSG_RECOGNIZER_INITIATE) {
            isContinuo = true;
            Util.mute(this.mAudioManager, true);
            sendStartListeningMessage();
            if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
                Log.d(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> Inicio el Servicio Continuo Voz <<<<");
        } else if (msg.what == MSG_RECOGNIZER_STOP) {
            isContinuo = false;
            cancelCountdown();
            sendCancelListeningMessage();
            Util.mute(this.mAudioManager, false);

            if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
                Log.d(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> Detenido el Servicio Continuo Voz <<<<");
        }
    }

    /**
     * Cancela el contador encargado de reiniciar las peticiones del servicio de reconocimiento
     */
    public void cancelCountdown() {
        if (mIsCountDownOn) {
            if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
                Log.d(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> Cancelo Timer Speech <<<<");
            mIsCountDownOn = false;
            mTimerRestartRecognizer.cancel();
        }
    }

    /**
     * Reinicia el contador encargado de reiniciar las peticiones del servicio de reconocimiento
     */
    public void startCountdown() {
        if (mIsCountDownOn) {
            if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
                Log.d(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> Inicio Timer Speech <<<<");
            mIsCountDownOn = true;
            mTimerRestartRecognizer.cancel();
            mTimerRestartRecognizer.start();
            Util.mute(this.mAudioManager, true);
        }
    }

    /**
     * Envia el mensaje al hilo manejador de peticiones, iniciando el reconocimiento de voz
     */
    public void sendStartListeningMessage() {
        try {
            if(mServerMessenger != null)
                mServerMessenger.send(Message.obtain(null, ServiceInVoice.MSG_RECOGNIZER_START));
        } catch (RemoteException e) {
            Log.e(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "Error Iniciar Servicio >>>> " + e.getMessage() + " <<<<");
        }
    }

    /**
     * Envia el mensaje al hilo manejador de peticiones, deteniendo el reconocimiento de voz
     */
    public void sendCancelListeningMessage() {
        try {
            if(mServerMessenger != null)
                mServerMessenger.send(Message.obtain(null, ServiceInVoice.MSG_RECOGNIZER_CANCEL));
        } catch (RemoteException e) {
            Log.e(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "Error Cancelar Servicio >>>> " + e.getMessage() + " <<<<");
        }
    }

    /**
     * Metodo interno que se ejecuta cuando el usuario ha comenzado a hablar.
     */
    @Override
    public void onBeginningOfSpeech() {
        //Log.d(LIB_LOGTAG, "--------> onBeginningOfSpeech");
        if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
            Log.d(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> Reconociendo la entrada de voz <<<<");
        // speech input will be processed, so there is no need for count down anymore
        cancelCountdown();
    }

    /**
     * Metodo interno que se ejecuta cuando el servicio captura el buffer del sonido recibido.
     * @param bytes array de bytes del buffer del sonido
     */
    @Override
    public void onBufferReceived(byte[] bytes) {
        //Log.d(LIB_LOGTAG, "--------> onBufferReceived");
    }

    /**
     * Metodo interno que se ejecuta después de que el usuario dejo de hablar.
     */
    @Override
    public void onEndOfSpeech() {
        if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
            Log.d(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> A la espera de resultado ...  <<<<");
    }

    /**
     * Metodo interno que es reservado para agregar eventos futuros.
     * @param i identificacion del evento
     * @param bundle objeto que contiene el mensaje de ejecucion
     */
    @Override
    public void onEvent(int i, Bundle bundle) {
        //Log.d(LIB_LOGTAG, "--------> onEvent");
    }

    /**
     * Metodo interno que se ejecuta durante la ejecuccion del servicio durante la grabacion del habla
     * @param bundle contiene los detalles de la grabacion durante la ejecuccion
     */
    @Override
    public void onPartialResults(Bundle bundle) {
        //Log.d(LIB_LOGTAG, "--------> onPartialResults");
    }

    /**
     * Metodo interno que se ejecuta cuando está preparado para que el usuario comience a hablar.
     * @param bundle Cotiene informacion del servicio a Ejecutar
     */
    @Override
    public void onReadyForSpeech(Bundle bundle) {
        if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
            Log.d(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> Inicializo el servicio de reconocimiento la voz <<<<");
        startCountdown();
    }

    /**
     * Metodo interno que se ejecuta cuando el nivel de sonido en el flujo de audio ha cambiado.
     * @param v indicador del volumen del sonido
     */
    @Override
    public void onRmsChanged(float v) {
        // Log.d(LIB_LOGTAG, "--------> onRmsChanged");
    }

    /**
     * Metodo interno que se ejecuta cuando se ha producido un error de red o de reconocimiento.
     * @param errorCode
     */
    @Override
    public void onError(int errorCode) {
        this.errorCode = errorCode;

        if (errorCode != 7 || new Date().getTime() - lastStart >= 500){
            Log.e(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> Servicio ServiceInVoice Finalizado por error <<<<");
            Log.e(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> " + this.errorCode + " <<<<");
            processError();

            if(isContinuo)
                sendStartListeningMessage();
        }
    }

    /**
     * Proporciona información al usuario (por medio de una tostada y un mensaje sintetizado) cuando el reconocedor ha detectado un error
     */
    public void processError() {

        try {
            String errorMessage = "Ocurrio Error en el Servicio de Reconocimiento, Causa: ";
            switch (this.errorCode) {
                case SpeechRecognizer.ERROR_AUDIO:
                    errorMessage += "Error de grabación de audio";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    errorMessage += "Error del lado del cliente";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    errorMessage += "Error Permisos insuficientes";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    errorMessage += "Error relacionado con la red";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    errorMessage += "Error de tiempo de espera en el funcionamiento de la red ";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    errorMessage += "Error no se obtuvo ningún resultado ";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    errorMessage += "Error Servicio de reconocimiento ocupado";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    errorMessage += "Error por el Servidor de Reconocimento";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    errorMessage += "Error no obtuvo entrada de voz";
                    break;
                default:
                    errorMessage += "Error en el Motor de Reconocimiento";
                    break;
            }
            //Toast.makeText(MyService.this,errorMessage, Toast.LENGTH_SHORT).show();
            Log.e(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> " + errorMessage + " <<<<");
            cancelCountdown();
            sendCancelListeningMessage();

        } catch (Exception e) {
            Log.e(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> " + e.getMessage() + " <<<<");
        }

    }

    /**
     * Se llama cuando los resultados del reconocimiento están listos.
     * @param results contiene la informacion de los resultados
     */
    @Override
    public void onResults(Bundle results) {
        if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
            Log.i(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> Servicio ServiceInVoice Finalizado Exitosamente <<<<");
        this.errorCode = 1;

        if (results != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                //Checks the API level because the confidence scores are supported only from API level 14:
                //http://developer.android.com/reference/android/speech/SpeechRecognizer.html#CONFIDENCE_SCORES
                //Procesa los resultados de reconocimiento y sus confidencias
                nBestList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            } else {
                nBestList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            }

            StringBuilder result = new StringBuilder();
            for (String trimForLiteVersion : nBestList) {
                result.append(trimForLiteVersion);
                result.append("\n");
            }
            String commandString = Util.noneIfEmpty(result.toString());
            String str = "%sRecibio posibles comandos:\n%s";
            Object[] objArr = new Object[2];
            objArr[0] = "";
            objArr[1] = commandString;
            String formatted = String.format(str, objArr);

            Log.d(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), formatted);
            Toast.makeText(this, formatted, Toast.LENGTH_SHORT).show();

            // Devuelve el mensaje
            send(Message.obtain(null, MSG_RECOGNIZER_RESULT, nBestList));
        } else {
            Log.e(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> No encontro resultados del servicio continuo <<<<");
        }

        cancelCountdown();
        sendCancelListeningMessage();

        if (isContinuo)
            sendStartListeningMessage();

        if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
            Log.d(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> Reiniciar Servicio Reconocimiento de Voz Continua <<<<");
    }

}