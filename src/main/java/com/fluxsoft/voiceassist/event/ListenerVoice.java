package com.fluxsoft.voiceassist.event;

/**
 * Clase encargada de asociar los eventos decalarados en la aplicacion con los objetos de origen de donde fueron declarados los eventos
 * Created by Cristian on 21/02/2016.
 */
public class ListenerVoice {
    private String type;
    private IEventVoiceHandler handler;

    /**
     * Constructor de la clase ListenerVoice, recibe dos parametros
     * @param type tipo de evento
     * @param handler objeto que contiene los resultados de los eventos
     */
    public ListenerVoice(String type, IEventVoiceHandler handler){
        this.type = type;
        this.handler = handler;
    }

    /**
     * Encapsulamiento de la variable type
     * @return type String
     */
    public String getType() {
        return type;
    }

    /**
     * Encapsulamiento de la variable handler
     * @return handler IEventVoiceHandler
     */
    public IEventVoiceHandler getHandler() {
        return handler;
    }
}
