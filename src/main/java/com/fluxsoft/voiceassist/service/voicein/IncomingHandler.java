package com.fluxsoft.voiceassist.service.voicein;

import android.os.Handler;
import android.os.Message;
import android.speech.SpeechRecognizer;
import android.util.Log;

import com.fluxsoft.voiceassist.service.util.Util;
import com.fluxsoft.voiceassist.singleton.ServiceVoice;

import java.lang.ref.WeakReference;
import java.util.Date;

/**
 * Case Hilo Ansicrono que realiza la gestion de peticiones al servicio de reconocimiento de voz
 * Created by Cristian on 17/06/2016.
 */
public class IncomingHandler extends Handler {
    private WeakReference<ServiceInVoice> mtarget;
    private final String TAG = this.getClass().getSimpleName(); //this.getClass().getName();

    /**
     * Clase constructora, contiene parametro del objeto del servicio que se realizara la gestion
     * @param target Objeto de tipo ServiceInVoice que realizara la gestion de reconocmiento
     */
    public IncomingHandler(ServiceInVoice target){
        mtarget = new WeakReference<ServiceInVoice>(target);
    }

    /**
     * Captura el mensaje y realiza la tarea a ejecutar relacionado con el mensaje
     * @param msg mensaje de operacion a realizar
     */
    public void handleMessage(Message msg) {
        final ServiceInVoice target = mtarget.get();

        switch (msg.what){
            case ServiceInVoice.MSG_RECOGNIZER_START:

                if (!target.mIsListening) {
                    try {
                        Thread.sleep(200);
                        Util.mute(target.mAudioManager, true);
                        target.lastStart = new Date().getTime();
                        target.mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(target);
                        target.mSpeechRecognizer.setRecognitionListener(target);
                        target.mSpeechRecognizer.startListening(target.mSpeechRecognizerIntent);
                    } catch (SecurityException ex) {
                        Log.e(TAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> Error. No hay permiso para iniciar el servicio de reconocimiento en este dispositivo: " + ex.toString()+ " <<<<"); //$NON-NLS-1$
                        return;
                    } catch (InterruptedException e) {
                        Log.e(TAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), e.getMessage()); //$NON-NLS-1$
                        e.printStackTrace();
                    }
                    target.mIsListening = true;
                    if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
                        Log.d(TAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> Message Start listening <<<<");
                    target.startCountdown();
                }

                break;

            case ServiceInVoice.MSG_RECOGNIZER_CANCEL:

                if(target.mSpeechRecognizer != null) {
                    target.mSpeechRecognizer.stopListening();
                    target.mSpeechRecognizer.destroy();
                    target.mSpeechRecognizer = null;
                }

                target.mIsListening = false;
                if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
                    Log.d(TAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> Message Canceled Recognizer <<<<"); //$NON-NLS-1$
                break;
        }
    }
}