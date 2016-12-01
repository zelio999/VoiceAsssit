package com.fluxsoft.voiceassist.service;


import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.fluxsoft.voiceassist.singleton.ServiceVoice;

import java.util.ArrayList;

/**
 * Clase abastracta de los servicios
 * Created by Cristian on 17/06/2016.
 */
public abstract class AbstractService extends Service {

    private final String LIB_LOGTAG = this.getClass().getSimpleName(); //this.getClass().getName();
    private ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    private final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.
    protected Intent intent;

    public static final int MSG_REGISTER_CLIENT = 9991;
    public static final int MSG_UNREGISTER_CLIENT = 9992;

    /**
     * Clase que controla los mensajes entrantes de cliente.
     * Created by Cristian on 17/06/2016.
     */
    private class IncomingHandler extends Handler { // Handler of incoming messages from clients.

        /**
         * Gestiona los mensajes
         * @param msg contiene el mensaje de la actividad a ejecutar
         */
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
                        Log.d(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "Cliente registrado: " + msg.replyTo);
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
                        Log.d(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "Cliente no registrado: " + msg.replyTo);
                    mClients.remove(msg.replyTo);
                    break;
                default:
                    //super.handleMessage(msg);
                    onReceiveMessage(msg);
            }
        }
    }

    /**
     * metodo interno del ciclo de vida del servicio que se ejecuta cuando se crea el servicio
     */
    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * metodo interno del ciclo de vida del servicio que se ejecuta cuando recibe los parametros de inicialiacion
     * @param intent envia la peticion de inilizacion
     * @param flags  banderas de inicializacion
     * @param startId id de ejecuccion
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.intent = intent;
        onStartService();
        if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
            Log.d(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(),  "Recibido Identificación del inicio_ " + startId + ": " + intent);
        return START_STICKY; // run until explicitly stopped.
    }

    /**
     * Devuelve los mensajes consultados al servicio
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    /**
     * metodo interno del ciclo de vida del servicio que se ejecuta cuando el servicio es destruido
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        onStopService();
        if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
            Log.d(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "Servicio Detenido");
    }

    /**
     * gestiona el envio de los mensajes al cliente
     * @param msg mensaje a enviar
     */
    protected void send(Message msg) {

        if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
            Log.d(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "Cantidad de Mensajes: "+mClients.size());
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
                    Log.d(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "Enviando el Mensaje a Cliente: "+msg);
                mClients.get(i).send(msg);
            }
            catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                Log.e(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "Cliente Muerto Removiendo de la lista: "+i);
                mClients.remove(i);
            }
        }
    }

    /**
     * Metodo abstracto que se ejecuta cuando inicia el servicio
     */
    public abstract void onStartService();

    /**
     * Metodo abstracto que se ejecuta cuando detiene el servicio
     */
    public abstract void onStopService();

    /**
     * Metodo abstracto que se ejecuta cuando recibe un mensaje por parte del cliente
     * @param msg mensaje enviado por el cliente
     */
    public abstract void onReceiveMessage(Message msg);

}