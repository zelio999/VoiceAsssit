package com.fluxsoft.voiceassist.singleton;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.fluxsoft.voiceassist.event.EventVoiceDispatcher;
import com.fluxsoft.voiceassist.event.EventVoice;
import com.fluxsoft.voiceassist.event.IEventVoiceHandler;

import java.util.ArrayList;

/**
 * Clase con patron singleton que gestiona la comunicacion de envios de los eventos hacia la libreria
 * Created by Cristian on 21/02/2016.
 */
public class ServiceVoice extends EventVoiceDispatcher {
    private static ServiceVoice ourInstance;
    private IEventVoiceHandler handler;
    private Context ctx;
    private final String LIB_LOGTAG = this.getClass().getSimpleName(); //this.getClass().getName();
    private boolean debug;

    /**
     * Crea la unica instancia que realiara el envio de los eventos
     * @param handler objeto del origen de cual se realizo el llamado del evento
     * @param ctx objeto contezto de la aplicacion movil
     * @return
     */
    public static ServiceVoice getInstance(IEventVoiceHandler handler, Context ctx) {
        if (ourInstance == null) {
            ourInstance = new ServiceVoice(handler, ctx);
        } else {
            ourInstance.setHandler(handler);
            ourInstance.setCtx(ctx);
        }

        ourInstance.addEventListener(EventVoice.VOICE_RESULT_COMMAND, handler);
        ourInstance.sendCommand();
        ourInstance.debug = false;
        return ourInstance;
    }

    /**
     * Retorna la instancia del objeto que controla el envio de los eventos hacia la libreria
     * @return
     */
    public static ServiceVoice getInstance(){
        return ourInstance;
    }

    /**
     * Constructor del singleton
     * @param handler  objeto del origen de cual se realizo el llamado del evento
     * @param ctx objeto contezto de la aplicacion movil
     */
    private ServiceVoice(IEventVoiceHandler handler, Context ctx) {
        this.setHandler(handler);
        this.setCtx(ctx);
    }

    /**
     * Se encarga de ejecutar el evento EventVoice de tipo VOICE_RESULT_COINCIDENCE (buscar coincidencia)
     * @param id llave de la peticion
     * @param search parametro de la cadena de texto a buscar
     */
    public void sendCoincidence(String id, String search) {
        ArrayList param = new ArrayList();
        param.add(Boolean.FALSE);
        param.add(id);
        param.add(search);
        EventVoice event = new EventVoice(EventVoice.VOICE_RESULT_COINCIDENCE, param);
        if(ServiceVoice.getInstance().isDebug())
            Log.d("Event Callback", "Evento VOICE_RESULT_COINCIDENCE  disparandose");
        dispatchEvent(event, getHandler(), getCtx());
    }

    /**
     * Se encarga de ejecutar el evento EventVoice de tipo VOICE_RESULT_DIALOG (interaccion pregunta - respuesta)
     * @param id llave de la peticion
     * @param question parametro de la cadena de texto a realizar la traduccion (pregunta)
     */
    public void sendDialog(String id, String question) {
        ArrayList param = new ArrayList();
        param.add(Boolean.FALSE);
        param.add(id);
        param.add(question);
        EventVoice event = new EventVoice(EventVoice.VOICE_RESULT_DIALOG, param);
        if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
            Log.d("Event Callback", "Evento VOICE_RESULT_DIALOG  disparandose");
        dispatchEvent(event, getHandler(), getCtx());
    }

    /**
     * Se encarga de ejecutar el evento EventVoice de tipo VOICE_RESULT_IN (reconocimiento de voz)
     * @param id llave de la peticion
     */
    public void sendIn(String id) {
        ArrayList param = new ArrayList();
        param.add(Boolean.FALSE);
        param.add(id);
        EventVoice event = new EventVoice(EventVoice.VOICE_RESULT_IN, param);
        if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
            Log.d("ServicioInVoz", "Evento VOICE_RESULT_IN  disparandose");
        dispatchEvent(event, getHandler(), getCtx());
    }

    /**
     * Se encarga de ejecutar el evento EventVoice de tipo VOICE_RESULT_OUT (sintesis de voz)
     * @param id llave de la peticion
     * @param text parametro de la cadena de texto a realizar la traduccion
     */
    public void sendOut(String id, String text) {
        ArrayList param = new ArrayList();
        param.add(Boolean.FALSE);
        param.add(id);
        param.add(text);
        EventVoice event = new EventVoice(EventVoice.VOICE_RESULT_OUT, param);
        if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
            Log.d("Event Callback", "Evento VOICE_RESULT_OUT  disparandose");
        dispatchEvent(event, getHandler(), getCtx());
    }

    /**
     * Se encarga de ejecutar el evento EventVoice de tipo VOICE_RESULT_COMMAND (reconocimiento de comandos)
     */
    public void sendCommand() {
        ArrayList param = new ArrayList();
        param.add(Boolean.TRUE);
        EventVoice event = new EventVoice(EventVoice.VOICE_RESULT_COMMAND, param);
        //if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
            Log.e("Event Callback", "Evento VOICE_RESULT_COMMAND  disparandose");
        dispatchEvent(event, getHandler(), getCtx());
    }

    /**
     * Encapsulamento de la variable ctx (contexto)
     * @return variable del contexto de la aplicacion
     */
    public Context getCtx() {
        return ctx;
    }

    /**
     * Encapsulamento de la variable ctx (contexto)
     * @param ctx variable del contexto de la aplicacion
     */
    public void setCtx(Context ctx) {
        this.ctx = ctx;
    }

    /**
     * Encapsulamento de la variable handler (origen de ivocacion de los eventos)
     * @return
     */
    public IEventVoiceHandler getHandler() {
        return handler;
    }

    /**
     * Encapsulamento de la variable handler (origen de ivocacion de los eventos)
     * @param handler contiene el objeto origen de donde se estan ejecutando los eventos
     */
    public void setHandler(IEventVoiceHandler handler) {
        this.handler = handler;
    }

    /**
     * Verifica la conextion a internet
     * @param ctx variable del contexto de la aplicacion movil
     * @return
     */
    public static Boolean verificaConexion(Context ctx) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network[] networks = connectivityManager.getAllNetworks();
            NetworkInfo networkInfo;
            for (Network mNetwork : networks) {
                networkInfo = connectivityManager.getNetworkInfo(mNetwork);
                if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                    return true;
                }
            }
        }else {
            if (connectivityManager != null) {
                NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
                if (info != null) {
                    for (NetworkInfo anInfo : info) {
                        if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                    }
                }
            }
        }
        Toast.makeText(ctx, "Actualmente no hay conexion de Red, Intente Conectarse", Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * Cierra todos los servicios de la libreria
     */
    public void cerrarServicios(){
        if(RouterServiceVoice.getInstance() != null )
            RouterServiceVoice.getInstance().destroy();
        if(ManagerCommand.getInstance() != null )
            ManagerCommand.getInstance().destroy();
        ourInstance = null;
    }

    /**
     * Encapsulamiento de la variable de control para verificar los logs de la libreria
     * @return
     */
    public boolean isDebug() {
        return  debug;
    }

    /**
     * Encapsulamiento de la variable de control para verificar los logs de la libreria
     * @param debug variable de control de logs
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
