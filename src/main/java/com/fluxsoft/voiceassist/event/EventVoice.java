package com.fluxsoft.voiceassist.event;

/**
 * Clase que contiene los metodos y atributos que compone el evento EventVoice
 * Created by Cristian on 21/02/2016.
 */
public class EventVoice {

    public static final String VOICE_RESULT_DIALOG = "voice_result_dialog";
    public static final String VOICE_RESULT_OUT = "voice_result_out";
    public static final String VOICE_RESULT_IN = "voice_result_in";
    public static final String VOICE_RESULT_COINCIDENCE = "voice_result_coincidence";
    public static final String VOICE_RESULT_COMMAND = "voice_result_command";

    protected String strType = "";
    protected Object params;
    protected Object retorno;

    /**
     * Constructor del evento EventVoice
     * @param type tipo de evento
     * @param params parametros asociados al evento
     */
    public EventVoice(String type, Object params){
        initProperties(type, params);
    }

    /**
     * Inicializa los atributos asociados al evento
     * @param type tipo de evento
     * @param params parametros asociados al evento
     */
    protected void initProperties(String type, Object params){
        this.strType = type;
        this.params = params;
    }

    /**
     * Encapsulamiento de la variable strType
     * @return strType variable de tipo de evento
     */
    public String getStrType(){
        return strType;
    }

    /**
     * Encapsulamiento de la variable params
     * @return params variable de los parametros adjuntos en el evento
     */
    public Object getParams() {
        return params;
    }

    /**
     * Encapsulamiento de la variable retorno
     * @return retorno variables que contiene los resultados
     */
    public Object getReturn() {
        return retorno;
    }

    /**
     * Encapsulamiento de la variable retorno
     * @param retorno variables que contiene los resultados
     */
    public void setReturn(Object retorno) {
        this.retorno = retorno;
    }
}
