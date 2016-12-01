package com.fluxsoft.voiceassist.service.voicein;

import android.os.CountDownTimer;
import android.os.Message;
import android.os.RemoteException;

/**
 * Clase que se encarga de controlar ciclicamente el inicio y fin de las peticiones servicio de reconocmiento
 * Created by Cristian on 17/06/2016.
 */
public class TimerRestartRecognizer extends CountDownTimer {

    private ServiceInVoice service;

    /**
     * Constructor del contador
     * @param millisInFuture    el numero de tiempo (milisegundos) en que va durar la ejecuccion de la peticion
     * @param countDownInterval El intervalo de tiempo (milisegundos) con que se llamara la peticion dentro del contador
     * @param service Objeto de tipo ServiceInVoice que contiene la instancia del servicio de reconocimiento de voz
     */
    public TimerRestartRecognizer(long millisInFuture, long countDownInterval, ServiceInVoice service) {
        super(millisInFuture, countDownInterval);
        this.service = service;
    }

    /**
     * Funcion que se ejecuta en el caso de comenzar el contador a ejecutar
     * @param millisUntilFinished
     */
    @Override
    public void onTick(long millisUntilFinished){
        // TODO Auto-generated method stub
    }

    /**
     * Funcion que se ejectua al finalizar el contador y ejectua las tareas asicronas de cancelacion y reinicio del servicio de reconocmiento
     */
    @Override
    public void onFinish(){
        try{
            service.mIsCountDownOn = false;
            service.mServerMessenger.send(Message.obtain(null, ServiceInVoice.MSG_RECOGNIZER_CANCEL));
            service.mServerMessenger.send(Message.obtain(null, ServiceInVoice.MSG_RECOGNIZER_START));
        }
        catch (RemoteException ignored){

        }
    }
}
