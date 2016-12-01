package com.fluxsoft.voiceassist.vo;

/**
 * Clase que contiene los metodos y atributos del Objeto comando
 * Created by Cristian on 25/04/2016.
 */
public class Command {

    private String comando;
    private String clase;
    private String metodo;

    /**
     * Encapsulamiento de la variable comando
     *
     * @return variable contiene la descripcion del comando
     */
    public String getComando() {
        return comando;
    }

    /**
     * Encapsulamiento de la variable strType
     *
     * @param comando variable contiene la descripcion del comando
     */
    public void setComando(String comando) {
        this.comando = comando;
    }

    /**
     * Encapsulamiento de la variable clase
     *
     * @return  variable contiene el nombre de la clase
     */
    public String getClase() {
        return clase;
    }

    /**
     * Encapsulamiento de la variable clase
     *
     * @param clase variable contiene el nombre de la clase
     */
    public void setClase(String clase) {
        this.clase = clase;
    }

    /**
     * Encapsulamiento de la variable metodo
     *
     * @return  variable contiene el nombre del metodo
     */
    public String getMetodo() {
        return metodo;
    }

    /**
     * Encapsulamiento de la variable metodo
     *
     * @param metodo variable contiene el nombre del metodo
     */
    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }
}
