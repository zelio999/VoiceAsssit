
package com.fluxsoft.voiceassist.service.voiceout;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

import com.fluxsoft.voiceassist.service.AbstractService;
import com.fluxsoft.voiceassist.service.util.Util;
import com.fluxsoft.voiceassist.singleton.ServiceVoice;

import java.util.Locale;

/**
 * Clase de servicio de modulacion de voz
 * Created by Cristian on 17/06/2016.
 */
@SuppressWarnings("ALL")
public class ServiceOutVoice extends AbstractService implements OnInitListener {

    //Instancia del contezto que se esta ejecutando la actividad
    private final String LIB_LOGTAG = this.getClass().getSimpleName(); //this.getClass().getName();
    public static final int MSG_TEXT_SYNTHESIS = 21;
    public static final int MSG_SYNTHESIS_FINISH = 22;
    private TextToSpeech myTTS;
    private AudioManager mAudioManager;
    private Context contex;
    private String texto;
    private boolean iniciar;

    /**
     * inicializa las variables asociadas al servicio de modulacion
     */
    @Override
    public void onStartService() {
        contex = this;
        iniciar = false;
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //startTTS();
    }

    /**
     * Recibe los mensajes enviados al servicio de sintesis
     * @param msg
     */
    @Override
    public void onReceiveMessage(Message msg) {
        // Get argument from message, take square root and respond right away
        if (msg.what == MSG_TEXT_SYNTHESIS) {
            Util.mute(this.mAudioManager, false);
            texto = (String) msg.obj;
            iniciar = false;
            startTTS();

            Handler handler = new Handler(contex.getMainLooper());
            final Runnable r = new Runnable() {
                public void run() {
                    if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
                        Log.i(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "<<<< Inicio Modulacion de Voz >>>>");
                    speak(texto);
                }
            };

            handler.postDelayed(r, 1100);
        }
    }

    /**
     * Inicializa el servicio de sintesis de voz
     */
    private void startTTS() {
        if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
            Log.i(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "<<<< Servicio ServicOutVoice Creado >>>>");
        if(contex != null)
            myTTS = new TextToSpeech(contex, (OnInitListener) ServiceOutVoice.this);
    }

    /**
     * Establece la configuración regional para la síntesis de voz, teniendo en cuenta los idiomas y países códigos
     * Si el <code> countryCode </ code> es nulo, sólo se establece el idioma, si el
     * <code> languageCode </ code> es nulo, utiliza el idioma por defecto del dispositivo
     * Si cualquiera de los códigos no son válidos, se utiliza el idioma por defecto
     * @param languageCode una cadena que representa el código de idioma, por ejemplo, EN
     * @param countryCode  una cadena que representa el código de país para el lenguaje utilizado, por ejemplo, US.
     */
    public void setLocale(String languageCode, String countryCode) {
        if (languageCode == null) {
            setLocale();
            Log.e(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "<<<< No se proporcionó código de idioma, se va a utilizar el codigo regional predeterminado >>>>");
        } else if (countryCode == null)
            setLocale(languageCode);
        else {
            Locale lang = new Locale(languageCode, countryCode);
            if (myTTS.isLanguageAvailable(lang) == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE)
                myTTS.setLanguage(lang);
            else {
                setLocale();
                Log.e(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "<<<< código del idioma o país no admitido, usando local predeterminado >>>>");
            }
        }
    }

    /**
     * Establece la configuración regional para la síntesis de voz, teniendo en cuenta el código de idioma
     * Si el código es nulo o no válido, se utiliza el idioma por defecto del dispositivo
     * @param languageCode una cadena que representa el código de idioma, por ejemplo, ES
     */
    public void setLocale(String languageCode) {
        if (languageCode == null) {
            setLocale();
            Log.e(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "<<<< No se proporcionó código de idioma, usando local predeterminado >>>>");
        } else {
            Locale lang = new Locale(languageCode);
            if (myTTS.isLanguageAvailable(lang) != TextToSpeech.LANG_MISSING_DATA && myTTS.isLanguageAvailable(lang) != TextToSpeech.LANG_NOT_SUPPORTED)
                myTTS.setLanguage(lang);
            else {
                setLocale();
                Log.e(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "<<<< código de idioma no compatible, utilizando codigo regional predeterminado >>>>");
            }
        }
    }

    /**
     * Establece el idioma por defecto del dispositivo como configuración regional para la síntesis de voz
     */
    public void setLocale() {
        myTTS.setLanguage(Locale.getDefault());
    }

    /**
     * Sintetiza un texto en el idioma indicado (o en el idioma por defecto del dispositivo
     * que no está disponible)
     * @param languageCode codigo del idioma para el TTS, e.g. EN
     * @param countryCode  codigo del pais para TTS, e.g. US
     * @param text         cadena que se sintetiza
     * @throws Exception cuando los códigos suministrados no se pueden utilizar y se selecciona la configuración regional predeterminada
     */
    @SuppressWarnings("deprecation")
    public void speak(final String text, final String languageCode, final String countryCode) throws Exception {
        final Handler handler = new Handler(contex.getMainLooper());
        handler.postDelayed(new Runnable() {
            public void run() {
                if (ServiceOutVoice.this.iniciar) {
                    setLocale(languageCode, countryCode);
                    myTTS.speak(text, TextToSpeech.QUEUE_ADD, null);
                    IsSpeak();
                } else
                    handler.postDelayed(this, 200);
            }
        }, 200);
    }

    /**
     * Sintetiza un texto en el idioma indicado (o en el idioma por defecto del dispositivo
     * que no está disponible)     *
     * @param languageCode codigo del idioma para el TTS, e.g. EN
     * @param text         cadena que se sintetiza
     * @throws Exception cuando los códigos suministrados no se pueden utilizar y se selecciona la configuración regional predeterminada
     */
    @SuppressWarnings("deprecation")
    public void speak(final String text, final String languageCode) {
        final Handler handler = new Handler(contex.getMainLooper());
        handler.postDelayed(new Runnable() {
            public void run() {
                if (ServiceOutVoice.this.iniciar) {
                    setLocale(languageCode);
                    myTTS.speak(text, TextToSpeech.QUEUE_ADD, null);
                    IsSpeak();
                } else
                    handler.postDelayed(this, 200);
            }
        }, 200);
    }

    /**
     * Sintetiza un texto utilizando el idioma por defecto del dispositivo
     * @param text cadena que se sintetiza
     */
    @SuppressWarnings("deprecation")
    public void speak(final String text) {
        final Handler handler = new Handler(contex.getMainLooper());
        handler.postDelayed(new Runnable() {
            public void run() {
                if (ServiceOutVoice.this.iniciar) {
                    setLocale();
                    Log.e(LIB_LOGTAG, "<<<< " + text + " >>>>");
                    myTTS.speak(text, TextToSpeech.QUEUE_ADD, null);
                    IsSpeak();
                } else
                    handler.postDelayed(this, 200);
            }
        }, 200);
    }

    /**
     * Se encarga de verificar de que la traduccion de voz haya finalizado y envie el mensaje de su cofirmacion
     */
    public void IsSpeak() {
        final Handler handler = new Handler(contex.getMainLooper());
        final Runnable r = new Runnable() { //h --> Handler h =new Handler();
            public void run() {
                if (!myTTS.isSpeaking()) {
                    if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
                        Log.i(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "<<<< Finalizo Modulacion de Voz >>>>");
                    onStopService();
                    send(Message.obtain(null, MSG_SYNTHESIS_FINISH));
                } else
                    handler.postDelayed(this, 200);
            }
        };

        handler.postDelayed(r, 200);
    }

    /**
     * Detiene el sintetizador si se está hablando
     */
    public void stop() {
        if (myTTS.isSpeaking())
            myTTS.stop();
    }

    /**
     * Detiene el motor de síntesis de voz. Es importante llamar a ella, para
     * liberar los recursos nativos utilizados.
     */

    @Override
    public void onStopService() {
        if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
            Log.i(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "<<<< Servicio ServiceOutVoice Finalizado >>>>");
        myTTS.stop();
        myTTS.shutdown();
    }


    /**
     * Un <code>TextToSpeech</code> ejemplo sólo se puede utilizar para sintetizar texto una vez
     * que ha completado la inicialización.
     * (non-Javadoc)
     * @see android.speech.tts.TextToSpeech.OnInitListener#onInit(int)
     * @param status contiene el estado de la inicializacion del servicio de modulacion
     */
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
                Log.i(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "<<<< Inicia Sintenizador >>>>");
            iniciar = true;
        } else if (status == TextToSpeech.ERROR) {
            Log.e(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "<<<< Error al crear el Sintentizado >>>>");
        }
    }

}