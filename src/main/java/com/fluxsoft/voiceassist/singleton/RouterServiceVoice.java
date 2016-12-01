package com.fluxsoft.voiceassist.singleton;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.fluxsoft.voiceassist.event.EventVoice;
import com.fluxsoft.voiceassist.event.IEventVoiceHandler;
import com.fluxsoft.voiceassist.service.ServiceManager;
import com.fluxsoft.voiceassist.service.util.Util;
import com.fluxsoft.voiceassist.service.voicein.ServiceInVoice;
import com.fluxsoft.voiceassist.service.voiceout.ServiceOutVoice;

import java.util.ArrayList;

/**
 * Clase que se encarga de gesionar las tareas a realizar segun el evento ejectuado y retonar los resultados al objeto origen de llamado
 * Created by Cristian on 23/02/2016.
 */
public class RouterServiceVoice {

    private static RouterServiceVoice ourInstance;
    private final String LIB_LOGTAG = this.getClass().getSimpleName(); //this.getClass().getName();
    private ManagerCommand managerCommand;
    private ServiceManager serviceInVoice;
    private ServiceManager serviceOutVoice;

    private Context ctx;
    private IEventVoiceHandler eventVoiceHandler;
    private EventVoice event;

    public ArrayList<String> arrayResult;

    public static RouterServiceVoice getInstance(EventVoice event, IEventVoiceHandler eventVoiceHandler, Context ctx) {
        final Handler handler = new Handler(ctx.getMainLooper());

        if (ourInstance == null) {
            ourInstance = new RouterServiceVoice(event,  eventVoiceHandler, ctx);
            final Runnable r = new Runnable() { //h --> Handler h =new Handler();
                public void run() {
                    ourInstance.initRecognition();
                }
            };
            handler.postDelayed(r, 500);
        } else {
            ourInstance.setEvent(event);
            ourInstance.eventVoiceHandler = eventVoiceHandler;
            ourInstance.ctx = ctx;
            ourInstance.initRecognition();
        }

        return ourInstance;
    }

    public static RouterServiceVoice getInstance(){
        return ourInstance == null ? null : ourInstance;
    }

    /**
     * Constructor encargado de declarar y iniciazliar los servicios de reconocimiento y sintesis de voz
     * @param event
     * @param eventVoiceHandler
     * @param ctx
     */
    private RouterServiceVoice(final EventVoice event, IEventVoiceHandler eventVoiceHandler, Context ctx) {

        this.ctx = ctx;
        this.eventVoiceHandler = eventVoiceHandler;
        this.setEvent(event);
        managerCommand = ManagerCommand.getInstance(eventVoiceHandler, ctx);

        serviceInVoice = new ServiceManager(ctx, ServiceInVoice.class, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ServiceInVoice.MSG_RECOGNIZER_RESULT:
                        // Recibe el array de resultados para realizar el procesamiento
                        arrayResult = msg.obj == null ? new ArrayList<String>() :(ArrayList) msg.obj;
                        returnEventVoice();
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        });

        serviceOutVoice = new ServiceManager(ctx, ServiceOutVoice.class, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ServiceOutVoice.MSG_SYNTHESIS_FINISH:
                        // Recibe el array de resultados para realizar el procesamiento
                        Log.d(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), " 2 Deberia ir >>>>> "+ RouterServiceVoice.this.getEvent().getStrType());

                        if (RouterServiceVoice.this.getEvent().getStrType().equals(EventVoice.VOICE_RESULT_DIALOG))
                            transicionDialog();
                        else
                           returnEventVoice();

                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        });

        serviceInVoice.start();
        serviceOutVoice.start();
    }

    /**
     * Se encarga de enlazar los datos de los eventos con las funcionalidades de los servicios de reconocimiento y sintesis
     */
    public void initRecognition() {
        try {
            Log.d(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), " 1 Deberia ir >>>>> "+ this.getEvent().getStrType());

           if (getEvent().getStrType().equals(EventVoice.VOICE_RESULT_OUT) || getEvent().getStrType().equals(EventVoice.VOICE_RESULT_DIALOG)) {
                final ArrayList param = (ArrayList) getEvent().getParams();
                serviceInVoice.send(Message.obtain(null, ServiceInVoice.MSG_RECOGNIZER_STOP));
                serviceOutVoice.send(Message.obtain(null, ServiceOutVoice.MSG_TEXT_SYNTHESIS, param.get(2)));
            } else {
                serviceInVoice.send(Message.obtain(null, ServiceInVoice.MSG_RECOGNIZER_INITIATE));
            }
        } catch (RemoteException e) {
            Log.e(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "Error Enviar el Mensaje >>>> " + e.getMessage() + " <<<<");
        }
    }

    /**
     *  Funcion de transicion en el evento VOICE_RESULT_DIALOG, paso de servicio de sintesis al servicio de reconocimiento
     */
    public void transicionDialog() {

        //Log.e(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> Deberia Hacer transicion aca ahora <<<<");
        try {
            serviceInVoice.send(Message.obtain(null, ServiceInVoice.MSG_RECOGNIZER_INITIATE));
        } catch (RemoteException e) {
            Log.e(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "Error Enviar el Mensaje >>>> " + e.getMessage() + " <<<<");
        }
    }

    /**
     *  Gestiona el retorno de los resultados segun el evento que se lo requiera
     */
    public void returnEventVoice() {
        try {
            if (getEvent().getStrType().equals(EventVoice.VOICE_RESULT_DIALOG)) {
                serviceInVoice.send(Message.obtain(null, ServiceInVoice.MSG_RECOGNIZER_STOP));
                resultVoiceDialog();
                ServiceVoice.getInstance().sendCommand();
            }
            if (getEvent().getStrType().equals(EventVoice.VOICE_RESULT_COINCIDENCE)) {
                serviceInVoice.send(Message.obtain(null, ServiceInVoice.MSG_RECOGNIZER_STOP));
                resultVoiceCoincidence();
                ServiceVoice.getInstance().sendCommand();
            }
            if (getEvent().getStrType().equals(EventVoice.VOICE_RESULT_IN)) {
                serviceInVoice.send(Message.obtain(null, ServiceInVoice.MSG_RECOGNIZER_STOP));
                resultVoiceIn();
                ServiceVoice.getInstance().sendCommand();
            }
            if (getEvent().getStrType().equals(EventVoice.VOICE_RESULT_OUT)) {
                resultVoiceOut();
                ServiceVoice.getInstance().sendCommand();
            }
            if (getEvent().getStrType().equals(EventVoice.VOICE_RESULT_COMMAND)) {
                resultVoiceCommand();
            }


        } catch (RemoteException e) {
            Log.e(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "Error Enviar el Mensaje >>>> " + e.getMessage() + " <<<<");
        }
    }

    /**
     *  Retorna los resultados del evento VOICE_RESULT_COINCIDENCE
     */
    private void resultVoiceCoincidence() {
        ArrayList param = (ArrayList) getEvent().getParams();
        ArrayList paramReturn = new ArrayList();
        Boolean retorno;
        paramReturn.add(0, (String) param.get(1));

        try {
            retorno = Util.getSimilarComando((String) param.get(2), arrayResult, 1f);

            if (!retorno)
                retorno = Util.getSimilarComando((String) param.get(2), arrayResult, 0.95f);

            if (!retorno)
                retorno = Util.getSimilarComando((String) param.get(2), arrayResult, 0.9f);

        } catch (Exception e) {
            retorno = Boolean.FALSE;
        }

        paramReturn.add(1, retorno);
        getEvent().setReturn(paramReturn);
        eventVoiceHandler.ResultVoiceCoincidence(getEvent());
    }

    /**
     *  Retorna los resultados del evento VOICE_RESULT_IN
     */
    private void resultVoiceIn() {
        ArrayList param = (ArrayList) getEvent().getParams();
        ArrayList paramReturn = new ArrayList();
        paramReturn.add(0, (String) param.get(1));
        paramReturn.add(1, arrayResult);
        getEvent().setReturn(paramReturn);
        eventVoiceHandler.ResultVoiceIn(getEvent());
    }

    /**
     *  Retorna los resultados del evento VOICE_RESULT_OUT
     */
    private void resultVoiceOut() {
        ArrayList param = (ArrayList) getEvent().getParams();
        ArrayList paramReturn = new ArrayList();
        paramReturn.add(0, (String) param.get(1));
        paramReturn.add(1, Boolean.TRUE);
        getEvent().setReturn(paramReturn);
        eventVoiceHandler.ResultVoiceOut(getEvent());
    }

    /**
     *  Retorna los resultados del evento VOICE_RESULT_DIALOG
     */
    private void resultVoiceDialog() {
        ArrayList param = (ArrayList) getEvent().getParams();
        ArrayList paramReturn = new ArrayList();
        paramReturn.add(0, (String) param.get(1));
        paramReturn.add(1, arrayResult);
        getEvent().setReturn(paramReturn);
        eventVoiceHandler.ResultVoiceDialog(getEvent());
    }

    /**
     *  Retorna los resultados del evento VOICE_RESULT_COMMAND
     */
    private void resultVoiceCommand() {
        managerCommand.lanzarMetodoComando(arrayResult);
        arrayResult.clear();
    }

    /**
     * Destrye los servicios asociados, serviceOutVoice y serviceInVoice
     */
    public void destroy() {
        try {
            serviceOutVoice.stop();
            serviceInVoice.stop();
            ourInstance = null;
        } catch (Throwable t) {
            Log.e(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), "No se ha podido desvincular de los servicios", t);
        }
    }

    /**
     * Encapsulamiento de la variable event
     * @return objeto tipo EventVoice
     */
    public EventVoice getEvent() {
        return event;
    }

    /**
     * Encapsulamiento de la variable event
     * @param event
     */
    public void setEvent(EventVoice event) {
        this.event = event;
    }
}
