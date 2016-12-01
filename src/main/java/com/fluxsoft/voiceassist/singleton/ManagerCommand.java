package com.fluxsoft.voiceassist.singleton;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.fluxsoft.voiceassist.event.IEventVoiceHandler;
import com.fluxsoft.voiceassist.service.util.Util;
import com.fluxsoft.voiceassist.vo.Command;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *  Clase de tipo singleton que se encarga de administrara la lista de comandos
 * Created by Cristian on 21/02/2016.
 */
public class ManagerCommand {
    private static ManagerCommand ourInstance;
    private IEventVoiceHandler handler;
    private Context ctx;
    private ArrayList<Command> comandos;
    private final String LIB_LOGTAG = this.getClass().getSimpleName(); //this.getClass().getName();

    /**
     * Inicia la unica instancia del objeto ManagerCommand
     * @param handler Objeto del origen de llamado del servicio
     * @param ctx contexto de la aplicacion
     * @return
     */
    public static ManagerCommand getInstance(IEventVoiceHandler handler, Context ctx) {
        if (ourInstance == null) {
            ourInstance = new ManagerCommand(handler, ctx);
        } else {
            ourInstance.handler = handler;
            ourInstance.ctx = ctx;
        }

        return ourInstance;
    }

    /**
     * Devuelve la instancia del unico objeto ManagerCommand
     * @return objeto ManagerCommand
     */
    public static ManagerCommand getInstance() {
        return ourInstance;
    }

    /**
     * Contructor privado de la clase
     * @param handler
     * @param ctx
     */
    private ManagerCommand(IEventVoiceHandler handler, Context ctx) {
        this.handler = handler;
        this.ctx = ctx;
        listaComandos();
    }

    /**
     * se encarga de pasar a una lista los comandos contenidos en el archivo
     */
    private void listaComandos(){
        comandos = new ArrayList<>();
        String archivo_comandos = getContentFromAssets("config_comandos.txt");
        String[] lineas = archivo_comandos.split(System.getProperty("line.separator"));
        for (int i = 0; i < lineas.length; i++) {
            String[] datos = lineas[i].split(";");
            Command comando = new Command();
            comando.setComando(datos[0]);
            comando.setClase(datos[1]);
            comando.setMetodo(datos[2]);
            comandos.add(comando);
        }
    }

    /**
     * Unica el metodo declarado dentro de la clase de origen
     * @param nombre_function nombre del metodo a ejecutar
     * @return
     */
    private Method traerMetodo(String nombre_function){
        try {
            Class[] parameterTypes = new Class[1];
            parameterTypes[0] = ArrayList.class;
            Log.e(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), nombre_function+" ---> "+handler.getClass().getName());
            return handler.getClass().getDeclaredMethod(nombre_function, parameterTypes);
        }catch(NoSuchMethodException e){
            Log.e(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), e.getMessage());
            return null;
        }
    }

    /**
     * Lanza el metodo asociado al comando
     * @param arrayResult lista de los comandos declarados
     */
    public void lanzarMetodoComando(ArrayList<String> arrayResult) {
        try {
            for(Iterator<Command> iter = comandos.iterator(); iter.hasNext();){
                Command obj = iter.next();

                if(handler.getClass().getSimpleName().indexOf(obj.getClase()) > -1 && buscarCoincidencia(arrayResult, obj.getComando())){
                    Method method = traerMetodo(obj.getMetodo());
                    method.invoke(handler, new ArrayList<Object>());
                    return;
                }
            }
        }catch(Exception e){
            Log.e(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), e.getMessage());
        }
    }

    /**
     * Realiza la buscqueda de los comandos dentro de la lista
     * @param arrayResult lista de comandos
     * @param buscar comandos a buscar dentro de la lista
     * @return
     */
    private Boolean buscarCoincidencia(ArrayList<String> arrayResult, String buscar){
        Boolean retorno;

            try {
                retorno = Util.getSimilarComando(buscar, arrayResult, 1f);

                if (!retorno) {
                    retorno = Util.getSimilarComando(buscar, arrayResult, 0.98f);
                }

                if (!retorno) {
                    retorno = Util.getSimilarComando(buscar, arrayResult, 0.95f);
                }

            } catch (Exception e) {
                retorno = Boolean.FALSE;
            }

        return retorno;
    }

    /**
     * Lee el contenido del archivo en la carpeta de Assets.
     * @param filename nombre del archivo
     * @return devuelve el contenido leido
     */
    private String getContentFromAssets(String filename) {

        StringBuffer contents = new StringBuffer();
        BufferedReader reader = null;

        try {
            AssetManager assetManager = this.ctx.getAssets();
            InputStream inputStream = assetManager.open(filename);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String text=null;

            while ((text = reader.readLine()) != null) {
                contents.append(text).append(System.getProperty("line.separator"));
            }
        } catch (FileNotFoundException e) {
            Log.e(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), e.getMessage());
        } catch (IOException e) {
            Log.e(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                Log.e(LIB_LOGTAG + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), e.getMessage());
            }}

        return contents.toString();
    }

    /**
     * destruye la instancia
     */
    public void destroy(){
        ourInstance = null;
    }
}
