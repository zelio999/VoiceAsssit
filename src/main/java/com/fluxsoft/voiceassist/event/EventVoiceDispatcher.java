package com.fluxsoft.voiceassist.event;

import android.content.Context;
import android.util.Log;

import com.fluxsoft.voiceassist.singleton.RouterServiceVoice;
import com.fluxsoft.voiceassist.singleton.ServiceVoice;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Clase administradora de los eventos (EventVoice) gestionados dentro de la aplicacion
 * Created by Cristian on 21/02/2016.
 */
public class EventVoiceDispatcher implements IEventVoiceDisparcher {

    protected ArrayList<ListenerVoice> listenerVoiceArrayList = new ArrayList();
    private final String LIB_LOGTAG = this.getClass().getSimpleName(); //this.getClass().getName();

    /**
     * Valida que el evento existe en la coleccion de eventos
     * @param type tipo de evento
     * @param handler objeto donde se retorna los eventos
     * @return boolean
     */
    @Override
    public Boolean hasEventListener(String type, IEventVoiceHandler handler) {
        for(Iterator<ListenerVoice> iter = listenerVoiceArrayList.iterator(); iter.hasNext();){
            ListenerVoice obj = iter.next();
            if(obj.getType().equals(type) && handler.equals(obj.getHandler()))
                return true;
        }

        return false;
    }

    /**
     * Adiciona el evento en la coleccion de eventos
     * @param type tipo de evento
     * @param handler objeto donde se retorna los eventos
     */
    @Override
    public void addEventListener(String type, IEventVoiceHandler handler) {
        ListenerVoice listenerVoice = new ListenerVoice(type, handler);
        removeEventListener(type, handler);
        listenerVoiceArrayList.add(listenerVoice);
    }

    /**
     * Dispara la funcionalidad del evento ingresado por medio de los parametros
     * @param event Evento a ejecutar
     * @param handler objeto donde se retorna los eventos
     * @param ctx contexto de la aplicacion
     */
    @Override
    public void dispatchEvent(EventVoice event, IEventVoiceHandler handler, Context ctx) {
        //Log.d(LIB_LOGTAG, "Se disparo el siguiente evento --> "+event.getStrType()+ " --> "+handler.getClass().getSimpleName());

        for(Iterator<ListenerVoice> iter = listenerVoiceArrayList.iterator(); iter.hasNext();){
            ListenerVoice obj = iter.next();
            if(event.getStrType().equals(obj.getType()) && handler.equals(obj.getHandler())){
                IEventVoiceHandler eventVoiceHandler = obj.getHandler();
                if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
                    Log.d(LIB_LOGTAG, "Se disparo el siguiente evento --> "+event.getStrType());
                RouterServiceVoice.getInstance(event, eventVoiceHandler, ctx);
            }

        }
    }

    /**
     * Remueve todos los eventos de la lista de eventos
     */
    @Override
    public void removeAllListeners() {
        for(Iterator<ListenerVoice> iter = listenerVoiceArrayList.iterator(); iter.hasNext();){
            ListenerVoice obj = iter.next();
            listenerVoiceArrayList.remove(obj);
        }
    }

    /**
     * Remueve determinado evento segun el tipo y el objeto contenedor de la respuesta
     * @param type tipo de evento
     * @param handler objeto donde se retorna los eventos
     */
    @Override
    public void removeEventListener(String type, IEventVoiceHandler handler ) {
        for(Iterator<ListenerVoice> iter = listenerVoiceArrayList.iterator(); iter.hasNext();){
            ListenerVoice obj = iter.next();
            if(obj.getType().equals(type) && handler.equals(obj.getHandler()))
                listenerVoiceArrayList.remove(obj);
        }
    }
}
