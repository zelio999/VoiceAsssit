package com.fluxsoft.voiceassist.service;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.fluxsoft.voiceassist.singleton.ServiceVoice;

/**
 * Clase administradora del servicios
 * Created by Cristian on 17/06/2016.
 */
public class ServiceManager {

    private final String LIB_LOGTAG = this.getClass().getSimpleName(); //this.getClass().getName();
    private Class<? extends AbstractService> mServiceClass;
    private Context mActivity;
    private boolean mIsBound;
    private Messenger mService = null;
    private Handler mIncomingHandler = null;
    private final Messenger mMessenger = new Messenger(new IncomingHandler());

    private ServiceConnection mConnection = new ServiceConnection() {
        /**
         * Realiza la conexion con el servicio con la aplicacion movil
         * @param className Contiene el nombre de la clase
         * @param service contiene los parametros del servicio a ejecutar
         */
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
                Log.d(LIB_LOGTAG + " ->>>> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "Conecto.  "+(mService == null));
            try {
                Message msg = Message.obtain(null, AbstractService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
                Log.e(LIB_LOGTAG + " ->>>> " + Thread.currentThread().getStackTrace()[2].getMethodName(), e.getMessage());
            }
        }

        /**
         * Realiza la desconexion del servicio con la aplicacion movil
         * @param className Contiene el nombre de la clase
         */
        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
            if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
                Log.d(LIB_LOGTAG + " ->>>> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "Desconectado.");
        }
    };

    /**
     * Constructor de la clase
     * @param context contexto de la aplicacion
     * @param serviceClass Objeto de la clase del servicio
     * @param incomingHandler Objeto del hijo ansicrono donde se realiza el envio de los mensajes
     */
    public ServiceManager(Context context, Class<? extends AbstractService> serviceClass, Handler incomingHandler) {
        this.mActivity = context;
        this.mServiceClass = serviceClass;
        this.mIncomingHandler = incomingHandler;

        if (isRunning()) {
            doBindService();
        }
    }

    /**
     * Clase del hilo de comunicaciones de los mensajes hacia el servicio
     */
    private class IncomingHandler extends Handler {
        /**
         * Realiza el envio del mensaje hacia el servicio
         * @param msg mensaje a enviar
         */
        @Override
        public void handleMessage(Message msg) {
            if (mIncomingHandler != null) {
                if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
                    Log.d(LIB_LOGTAG + " ->>>> " + Thread.currentThread().getStackTrace()[2].getMethodName(),  "Mensaje entrante. Pasando al controlador: "+msg);
                mIncomingHandler.handleMessage(msg);
            }
        }
    }

    /**
     * Inicia el servicio y lo une con la aplicacion
     */
    public void start() {
        doStartService();
        doBindService();
    }

    /**
     * Detiene el servicio y lo desvincula con la aplicacion
     */
    public void stop() {
        doUnbindService();
        doStopService();
    }

    /**
     * Desvincula el servicio vinculado con la aplicacion
     * Use with caution (only in Activity.onDestroy())!
     */
    public void unbind() {
        doUnbindService();
    }

    /**
     * Verifica si el servicio esta actualmente en ejecuccion
     * @return
     */
    public boolean isRunning() {
        ActivityManager manager = (ActivityManager) mActivity.getSystemService(Context.ACTIVITY_SERVICE);

        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (mServiceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Envia el mensaje hacia el servicio vinculado
     * @param msg Objeto mensaje a enviar al servicio
     * @throws RemoteException se ejecuta en caso que haya problemas con el envio del mensaje
     */
    public void send(Message msg) throws RemoteException {
        if (mIsBound) {
            if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
                Log.d(LIB_LOGTAG + " ->>>> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "---> "+(mService == null)+" ***** "+msg.toString());

            if (mService != null) {
                mService.send(msg);
            }
        }
    }

    /**
     * Inicializa el servicio y lo vincula con la aplicacion
     */
    private void doStartService() {
        mActivity.startService(new Intent(mActivity, mServiceClass));
        if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
            Log.d(LIB_LOGTAG + " ->>>> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "Creado servicio ---> "+mServiceClass.toString());

    }

    /**
     * Detiene el servicio y lo desvincula con la aplicacion
     */
    private void doStopService() {
        Log.d(LIB_LOGTAG + " ->>>> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "Detuvo Servicio.");
        mActivity.stopService(new Intent(mActivity, mServiceClass));
    }

    /**
     * Vincula el envio de mensajes con el servicio
     */
    private void doBindService() {
        mActivity.bindService(new Intent(mActivity, mServiceClass), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    /**
     * Desvincula el envio de mensajes con el servicio
     */
    private void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, AbstractService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }

            // Detach our existing connection.
            mActivity.unbindService(mConnection);
            mIsBound = false;
            ///if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
                Log.d(LIB_LOGTAG + " ->>>> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "Desmonto Servicio.");
        }
    }
}