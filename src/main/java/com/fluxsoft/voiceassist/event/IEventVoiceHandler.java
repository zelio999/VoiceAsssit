package com.fluxsoft.voiceassist.event;

/**
 * Interfaz que coniene la estructura de los metodos a decalarar en el objeto origen para capturar los resultados obtenidos del procesamiento de los servicios de la libreria
 * Created by Cristian on 21/02/2016.
 */
public interface IEventVoiceHandler {
    /**
     * Retorna el resultado del evento VOICE_RESULT_DIALOG
     * @param event objeto de tipo EventVoice
     */
    public void ResultVoiceDialog(EventVoice event);
    /**
     * Retorna el resultado del evento VOICE_RESULT_OUT
     * @param event objeto de tipo EventVoice
     */
    public void ResultVoiceOut(EventVoice event);
    /**
     * Retorna el resultado del evento VOICE_RESULT_IN
     * @param event objeto de tipo EventVoice
     */
    public void ResultVoiceIn(EventVoice event);
    /**
     * Retorna el resultado del evento VOICE_RESULT_COINCIDENCE
     * @param event objeto de tipo EventVoice
     */
    public void ResultVoiceCoincidence(EventVoice event);
}
