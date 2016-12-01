package com.fluxsoft.voiceassist.event;

import android.content.Context;

/**
 * Interfaz que contiene la estructura de metodos a adoptar del objeto manejador de eventos que se encarga de gestionar los eventos declarados en la aplicacion
 * Created by Cristian on 21/02/2016.
 */
public interface IEventVoiceDisparcher {

    /**
     * Adiciona el evento en la coleccion de eventos
     * @param type tipo de evento
     * @param handler objeto donde se retorna los eventos
     */
    public void addEventListener(String type, IEventVoiceHandler handler);
    /**
     * Remueve determinado evento segun el tipo y el objeto contenedor de la respuesta
     * @param type tipo de evento
     * @param handler objeto donde se retorna los eventos
     */
    public void removeEventListener(String type, IEventVoiceHandler handler);
    /**
     * Dispara la funcionalidad del evento ingresado por medio de los parametros
     * @param event Evento a ejecutar
     * @param handler objeto donde se retorna los eventos
     * @param ctx contexto de la aplicacion
     */
    public void dispatchEvent(EventVoice event, IEventVoiceHandler handler, Context ctx);
    /**
     * Valida que el evento existe en la coleccion de eventos
     * @param type tipo de evento
     * @param handler objeto donde se retorna los eventos
     * @return boolean
     */
    public Boolean hasEventListener(String type, IEventVoiceHandler handler);
    /**
     * Remueve todos los eventos de la lista de eventos
     */
    public void removeAllListeners();
}
