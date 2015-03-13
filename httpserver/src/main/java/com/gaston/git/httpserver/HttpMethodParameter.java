package com.gaston.git.httpserver;

import java.lang.reflect.Type;

/**
 * Clase que se utiliza para mantener el par nombre de parametro. tipo de clase
 * que identifica los parametros de cada servicio web
 *
 * @author gaston
 */
public class HttpMethodParameter {

    private String parameterName;
    private Type parameterType;

    public HttpMethodParameter(String parameterName, Type parameterType) {
        this.parameterName = parameterName;
        this.parameterType = parameterType;
    }

    public HttpMethodParameter(Type parameterType) {
        this.parameterType = parameterType;
    }

    /**
     * Obtiene el nombre del parametro en un metodo del contexto
     *
     * @return Nombre del parametro
     */
    public String getParameterName() {
        return parameterName;
    }

    /**
     * Establece el nombre del parametro de un metodo del contexto
     *
     * @param parameterName Nombre del parametro
     */
    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    /**
     * Obtiene el tipo de clase del parametro de un metodo
     *
     * @return Tipo de clase
     */
    public Type getParameterType() {
        return parameterType;
    }

    /**
     * Establece el tipo de clase de un parametro
     *
     * @param parameterType Tipo de clase
     */
    public void setParameterType(Type parameterType) {
        this.parameterType = parameterType;
    }

}
