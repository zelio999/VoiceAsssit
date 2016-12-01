package com.fluxsoft.voiceassist.service.util;

import android.os.AsyncTask;

/**
 * Clase abstracta de la tarea ansicrona que se encarga de controlar del volumen del dispositivo
 * Created by Cristian on 19/06/2016.
 */
public abstract class ExecuteTaks extends AsyncTask<Void, Void, Void> {
    protected abstract void executeTaks();

    /**
     * Constructor de la tarea
     */
    protected ExecuteTaks() {
        execute(new Void[0]);
    }

    /**
     * Recibe la tarea y la ejecuta en hilo
     * @param params parametros qu necesita la tarea para ejecutarse
     * @return
     */
    protected Void doInBackground(Void... params) {
        executeTaks();
        return null;
    }
}
