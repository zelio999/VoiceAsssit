package com.fluxsoft.voiceassist.service.util;

import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

import com.fluxsoft.voiceassist.singleton.ServiceVoice;

import org.apache.commons.codec.language.Soundex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

/**
 * Clase de ayuda que presta diferente metodos auxiliares a los servicios de reconocimiento y modulacion de voz
 * Created by Cristian on 18/07/2016.
 */
public class Util {
    public static int storedVolume = 0;

    /**
     * Maneja el control del volumen, pone en estado de silencio en la salida de sonido, o recobra los valores originales del sonido
     * @param mAudioManager Controlador del volumen del dispositivo
     * @param mute variable de tipo booleana que maneja el estado del volumen
     */
    public static void mute(AudioManager mAudioManager, boolean mute) {
        if (mAudioManager != null) {

            if(storedVolume <= 0)
                storedVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC); // conseguir el volumen del sistema en var para más adelante colocarlo en silencio
            if(storedVolume <= 0)
                storedVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // conseguir el volumen del sistema en var para más adelante colocarlo en silencio

            try {
                if (mute) {
                    if (storedVolume > 0)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            //mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0); // ajuste de volumen del sistema a cero, en silencio
                            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
                        } else {
                            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                        }

                    if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
                        Log.d("Util -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> silenciado con éxito  <<<<");
                    return;
                } else {
                    ManagerVolumen managerVolumen = new ManagerVolumen(mAudioManager);
                    if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
                        Log.d("Util -> "  + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> restaurar volumen con éxito  <<<<");
                    return;
                }

            } catch (NullPointerException ex) {
                Log.e("Util -> "  + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> Error del silenciar Volumen: " + ex.toString() + " <<<<");
                return;
            } catch(IllegalArgumentException ex){
                Log.e("Util -> "  + Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> Error del silenciar Volumen: " + ex.toString() + " <<<<");
                return;
            }

        }
        Log.d("Util -> "+ Thread.currentThread().getStackTrace()[2].getMethodName(), ">>>> No se pudo silenciar. El Administrador de audio es nulo  <<<<");
    }

    /**
     * Realiza el parseo (conversion) de un valor de tipo String a tipo Integer
     * @param intString Valor en tipo string
     * @param defaultValue Valor numerico de defecto en caso de que no se pueda realizar la conversion
     * @return retorna el numero convertido a tipo String
     */
    public static Integer parseInt(String intString, Integer defaultValue) {
        Integer result = defaultValue;
        if (intString == null || intString.equals("")) {
            return result;
        }
        try {
            result = Integer.valueOf(Integer.parseInt(intString));
        } catch (Exception e) {
        }
        return result;
    }

    /**
     * Valida que la cadena de texto ingresada devuelva un valor "none" en caso de encontrarse vacia o en null
     * @param str cadena de texto
     * @return validacion de la cadena de texto
     */
    public static String noneIfEmpty(String str) {
        if (str == null || str.equals("")) {
            return "none";
        }
        return str;
    }

    /**
     * Busca las coincidencias tanto por ortografia como por fonema
     * Cuando el reconocimiento se realiza correctamente, se obtiene el mejor resultado de reconocimiento (supuestamente las palabras a buscar),
     * Y clasifica todas las coincidencias de acuerdo a la similitud de las palabras a buscar (Considerando sólo los similares encima de un umbral).
     * A continuación, se devuelve true  el resultado que arroje mayor similitud. Si las similitudes todas son abajo del umbral definido, devuelve falso en su resultado final
     *
     * @param coincedence nombre del string a comparar con los resultados del reconocedor
     * @return boolean true en caso de que la coincidencia sea coreccta.
     */
    public static Boolean getSimilarComando(String coincedence, ArrayList<String> nBestList, double similarityThreshold) {
        Double similarity = 0.0;

        if (nBestList != null && nBestList.size() > 0) {
            //Obtiene el mejor resultado de reconocimiento
            for (Iterator<String> iter = nBestList.iterator(); iter.hasNext(); ) {
                String bestResult = iter.next().trim();
                if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
                    Log.d("ServiceInVoice" + " -> " + Thread.currentThread().getStackTrace()[2].getMethodName(), " >>>> "+bestResult+" "+coincedence+" <<<<");
                similarity = compareOrthographic(normalize(coincedence), normalize(bestResult));

                if (similarity > similarityThreshold) {
                    return Boolean.TRUE;
                } else {
                    similarity = comparePhonetic(normalize(coincedence), normalize(bestResult));
                    if (similarity > similarityThreshold) {
                        return Boolean.TRUE;
                    }
                }
            }
        }

        return Boolean.FALSE;
    }

    /**
     * Compara los nombres usando la distancia Levenshtein, que es el número mínimo de caracteres que tiene que reemplazar,
     * Insertar o eliminar para transformar cadena a en cadena b.
     * Hemos utilizado un cálculo de esta distancia proporcionada por Wikipedia.
     *
     * @return
     * @param a Primera Cadena de texto a comparar
     * @param b Segunda Cadena de texto a comparar
     * @return similitud de 0 (mínimo) a 1 (máximo)
     */

    private static double compareOrthographic(String a, String b) {
        //Log.e(LIB_LOGTAG, "Resultado LevenshteinDistance : "+a+", "+b+ " --- " + LevenshteinDistance.computeLevenshteinDistance(a, b));
        return Util.computeLevenshteinDistance(a, b);
    }

    /**
     * Compara los nombres usando su similitud fonética, utilizando el algoritmo soundex.
     * Hemos utilizado una implementación de este algoritmo proporcionado por Apache.
     */
    private static double comparePhonetic(String a, String b) {
        Soundex soundex = new Soundex();

        //Devuelve el número de caracteres de las dos cadenas codificadas que son los mismos.
        // Este valor de retorno se extiende de 0 a la longitud de la cadena más corta codificada: 0 indica poca o ninguna similitud,
        // Y 4 de 4 (por ejemplo) indica que hay similitud o valores idénticos.
        double sim = 0;
        try {
            sim = soundex.difference(a, b);
            if(ServiceVoice.getInstance() != null && ServiceVoice.getInstance().isDebug())
                Log.d("ServiceInVoice", ">>>> Resultado soundex : "+a+", "+b+ " --- " + sim);
        } catch (Exception e) {
            Log.e("ServiceInVoice", ">>>> Error de codificación soundex. Similitud obligado a 0 <<<<");
            sim = 0;
        }
        return sim / 4;
    }

    /**
     * Calcula el valor minimo entre las variables ingeresadas
     * @param a primer numero a comparar
     * @param b segundo numero a comparar
     * @param c tercero numero a comparar
     * @return valor minimo calculado
     */
    private static int minimum(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

    /**
     * Algoritmo de comparacion usando la distancia Levenshtein
     * @param str1 primera cadena de texto a comparar
     * @param str2 segunda cadena de texto a comparar
     * @return retorna la comparacion
     */
    private static int levenshteinDistance(CharSequence str1, CharSequence str2) {
        int[][] distance = new int[str1.length() + 1][str2.length() + 1];

        for (int i = 0; i <= str1.length(); i++)
            distance[i][0] = i;
        for (int j = 1; j <= str2.length(); j++)
            distance[0][j] = j;

        for (int i = 1; i <= str1.length(); i++)
            for (int j = 1; j <= str2.length(); j++)
                distance[i][j] = minimum(
                        distance[i - 1][j] + 1,
                        distance[i][j - 1] + 1,
                        distance[i - 1][j - 1]
                                + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
                                : 1));

        return distance[str1.length()][str2.length()];
    }

    /**
     * Realiza el calculo de coincidencias por medio del algoritmo de distancia Levenshtein
     * @param a primera cadena de texto a comparar
     * @param b segunda cadena de texto a comparar
     * @return retorna la comparacion
     */
    public static double computeLevenshteinDistance(String a, String b) {
        double distance = levenshteinDistance(a, b);
        double normalizedDistance = distance / Math.max(a.length(), b.length());
        return (1 - normalizedDistance);
    }

    /**
     * Realiza la normalizacion del texto para su comparacion, quitandole los espacios en blanco y convirtiendolo en minuscula
     * @param text Cadena de texto a normalizar
     * @return Cadena de texto normalizado
     */
    private static String normalize(String text) {
        return text.trim().toLowerCase(Locale.getDefault());
    }
}
