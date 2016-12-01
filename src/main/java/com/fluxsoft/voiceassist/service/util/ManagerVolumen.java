package com.fluxsoft.voiceassist.service.util;

import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

/**
 * Clase de tarea ansicrona que controla el volumen del dispositivo
 * Created by Cristian on 19/06/2016.
 */
public class ManagerVolumen extends ExecuteTaks {
    private AudioManager mAudioManager;

    /**
     * Constructor de la tarea ansicrona
     * @param audioManager controlador de Audio
     */
    public ManagerVolumen(AudioManager audioManager) {
        this.mAudioManager = audioManager;
    }

    /**
     * Se encarga de ejecutar las tareas de manejo de sonido del dispositivo
     */
    protected void executeTaks() {
        try {
            if (Util.storedVolume > 0) {
                Thread.sleep(1000);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mStreamVolume, 0); // nuevo ajuste del volumen del sistema de nuevo a la original,
                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
                } else {
                    mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
                }
            }
        } catch (InterruptedException ex) {
            Log.e("Util -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> Error del silenciar Volumen: " + ex.toString() + " <<<<");
        } catch (NullPointerException ex) {
            Log.e("Util -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> Error del silenciar Volumen: " + ex.toString() + " <<<<");
        } catch (IllegalArgumentException ex) {
            Log.e("Util -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> Error del silenciar Volumen: " + ex.toString() + " <<<<");
        }

    }
}

