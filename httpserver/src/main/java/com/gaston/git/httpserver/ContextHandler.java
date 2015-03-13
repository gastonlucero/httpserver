package com.gaston.git.httpserver;


import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.json.XML;

/**
 * Clase encargada de manejar un contexto web.Todas las peticiones que se
 * realicen a este contexto son manejadas por cn el metodo handle, en donde se
 * identifican todos los parametros enviados en la peticion y se invoca el
 * metodo correspondiente en la propia clase.
 *
 * Todos los metodos deben alguna de las anotaciones validas en jaxrs, es decir
 *
 * @GET,@PUT,@POST,@DELETE para que sean considerados posibles endpoints del
 * servicio, a su vez pueden recibir cualquier tipo de dato y retornar cualquier
 * tipo de dato.
 *
 * Los metodos PUT,POST y DELETE no necesitan ningun anotaacion adicional, salvo
 * si es necesario utilizar subpaths la anotacion PATH.
 *
 * Los metodos GET ademas la anotaacion PATH si es necesario, para cada uno de
 * sus parametros pueden utilizarse todas las anotaciones de jaxrs pero si o si
 * necesita el annotation @QueryParam, sino ese parametro en el metodo es
 * ignorado
 *
 *
 * Todos lo metodos pueden ser anotados con @Consume o @Produces para
 * especificar el formato en como recibiran o enviaran los datos, en el caso de
 * poner el wildcard "*" se toma el valor por defecto que es application/json,
 * si no se indica nada en el metodo se retorna segun el formato que se envia en
 * el mismo request
 *
 * A nivel de clase estos annotationes no tienen ningun efecto
 *
 * @author gaston
 */
public class ContextHandler implements HttpHandler {

    private static final Logger logger = Logger.getLogger("webServerLogger");

    /**
     * Este mapa contiene las representaciones de los metodos post de insercion
     * en forma de nombre de <pathMetodo,<Metodo,Parametros>>
     *
     */
    public Map<String, Map<Method, List<HttpMethodParameter>>> contextPostMethods;

    /**
     * Este mapa contiene las representaciones de los metodos put de
     * actualizacion en forma de nombre de <pathMetodo,<Metodo,Parametros>>
     *
     */
    public Map<String, Map<Method, List<HttpMethodParameter>>> contextPutMethods;

    /**
     * Este mapa contiene las representaciones de los metodos get de
     * actualizacion en forma de nombre de <pathMetodo,<Metodo,Parametros>>
     */
    public Map<String, Map<Method, List<HttpMethodParameter>>> contextGetMethods;

    /**
     * Este mapa contiene las representaciones de los metodos delete de
     * actualizacion en forma de nombre de <pathMetodo,<Metodo,Parametros>>
     */
    public Map<String, Map<Method, List<HttpMethodParameter>>> contextDeleteMethods;

    /**
     * Nomre del contexto que se corresponde con el path
     */
    private String name;

    /**
     * Mapper Json
     */
    private ObjectMapper mapper;

    private Throwable contextException;

    /**
     * Aqui por reflections se obtienen todos los datos de los distintos metodos
     * para completar los mapas de metodos de cada operacion
     *
     * @param name Nombre del contexto
     */
    public ContextHandler(String name) {
        try {
            this.name = name;
            this.mapper = new ObjectMapper(new JsonFactory());
            contextPostMethods = Collections.synchronizedMap(new HashMap());
            contextPutMethods = Collections.synchronizedMap(new HashMap());
            contextGetMethods = Collections.synchronizedMap(new HashMap());
            contextDeleteMethods = Collections.synchronizedMap(new HashMap());
            for (Method method : this.getClass().getDeclaredMethods()) {
                Map methodRepresetation = Collections.synchronizedMap(new HashMap());
                List<HttpMethodParameter> httpParams = new ArrayList<>();
                if (method.getAnnotation(PUT.class) != null) {
                    for (int i = 0; i < method.getParameterTypes().length; i++) {
                        httpParams.add(new HttpMethodParameter("",
                                method.getGenericParameterTypes()[i]));
                    }
                    methodRepresetation.put(method, httpParams);
                    if (method.getAnnotation(Path.class) != null) {
                        contextPutMethods.put(this.name + "" + method.getAnnotation(Path.class).value(), methodRepresetation);
                    } else {
                        contextPutMethods.put(this.name, methodRepresetation);
                    }
                } else if (method.getAnnotation(POST.class) != null) {
                    for (int i = 0; i < method.getParameterTypes().length; i++) {
                        httpParams.add(new HttpMethodParameter("",
                                method.getGenericParameterTypes()[i]));
                    }
                    methodRepresetation.put(method, httpParams);
                    if (method.getAnnotation(Path.class) != null) {
                        contextPostMethods.put(this.name + "" + method.getAnnotation(Path.class).value(), methodRepresetation);
                    } else {
                        contextPostMethods.put(this.name, methodRepresetation);
                    }
                } else if (method.getAnnotation(GET.class) != null) {
                    for (int i = 0; i < method.getParameterTypes().length; i++) {
                        httpParams.add(new HttpMethodParameter(((QueryParam) method.getParameterAnnotations()[i][0]).value(),
                                method.getGenericParameterTypes()[i]));
                    }
                    methodRepresetation.put(method, httpParams);
                    if (method.getAnnotation(Path.class) != null) {
                        contextGetMethods.put(this.name + "" + method.getAnnotation(Path.class).value(), methodRepresetation);
                    } else {
                        contextGetMethods.put(this.name, methodRepresetation);
                    }
                } else if (method.getAnnotation(DELETE.class) != null) {
                    for (int i = 0; i < method.getParameterTypes().length; i++) {
                        httpParams.add(new HttpMethodParameter("",
                                method.getGenericParameterTypes()[i]));
                    }
                    methodRepresetation.put(method, httpParams);
                    if (method.getAnnotation(Path.class) != null) {
                        contextDeleteMethods.put(this.name + "" + method.getAnnotation(Path.class).value(), methodRepresetation);
                    } else {
                        contextDeleteMethods.put(this.name, methodRepresetation);
                    }
                }
            }
        } catch (SecurityException e) {
            logger.error("Error iniciando contexto web", e);
            //TODO Aca hay q enviar un evento para que el weserver maneje este error
        }
    }

    /**
     * Este metodo hay que implemementarlo y es el que cotniene toda la logica
     * para saber que metodo del contexto realmente se va a ejecutar .Para ello
     * realiza distintas verificaciones de los datos del request y evalua cual
     * de los metdos del hashmap de la operacion correspondiente, es el que se
     * debe ejecutar si alguno de los valores no se corresponden con la
     * definicion de metodo, o el path invocado no existe en ninugn metodo, se
     * lanza una excepcion que se traduce en una respuesta con un mensaje de
     * error al cliente que invoco el servicio
     *
     *
     * @param he Informacion de la peticion web
     */
    @Override
    public void handle(HttpExchange he) throws IOException {
        try {
            Headers headers = he.getRequestHeaders();
            Object returnValue = null;
            Method methodContext = null;
            switch (he.getRequestMethod()) {
                case "GET": {
                    String path = he.getRequestURI().getPath();
                    Map<Method, List<HttpMethodParameter>> methodMap = contextGetMethods.get(path);
                    if (methodMap != null) {
                        String query = he.getRequestURI().getQuery();
                        Map<String, String> parameters = getParameters(query);
                        for (Map.Entry<Method, List<HttpMethodParameter>> methodToInvoke : methodMap.entrySet()) {
                            methodContext = methodToInvoke.getKey();
                            returnValue = methodContext.invoke(this, checkHttpParameters(methodToInvoke, parameters).toArray());
                        }
                    } else {
                        contextException = new Exception("Metodo inexistente");
                    }
                    break;
                }
                case "POST": {
                    Map<Method, List<HttpMethodParameter>> methodMap = contextPostMethods.get(he.getRequestURI().getPath());
                    if (methodMap != null) {
                        for (Map.Entry<Method, List<HttpMethodParameter>> methodToInvoke : methodMap.entrySet()) {
                            Class clazz = (Class) ((ParameterizedType) methodToInvoke.getValue().get(0).getParameterType()).getRawType();
                            methodContext = methodToInvoke.getKey();
                            returnValue = methodContext.invoke(this, mapper.readValue(new InputStreamReader(he.getRequestBody()), clazz));
                        }
                    } else {
                        //Exception pq el path no existe
                        contextException = new Exception("Metodo inexistente");
                    }
                    break;
                }
                case "PUT": {
                    Map<Method, List<HttpMethodParameter>> methodMap = contextPutMethods.get(he.getRequestURI().getPath());
                    if (methodMap != null) {
                        for (Map.Entry<Method, List<HttpMethodParameter>> methodToInvoke : methodMap.entrySet()) {
                            Class clazz = (Class) ((ParameterizedType) methodToInvoke.getValue().get(0).getParameterType()).getRawType();
                            methodContext = methodToInvoke.getKey();
                            returnValue = methodContext.invoke(this, mapper.readValue(new InputStreamReader(he.getRequestBody()), clazz));
                        }
                    } else {
                        //Exception pq el path no existe
                        contextException = new Exception("Metodo inexistente");
                    }
                    break;
                }
                case "DELETE": {
                    String path = he.getRequestURI().getPath().contains("&")
                            ? he.getRequestURI().getPath().substring(0, he.getRequestURI().getPath().indexOf("&"))
                            : he.getRequestURI().getPath();
                    Map<Method, List<HttpMethodParameter>> methodMap = contextGetMethods.get(path);
                    if (methodMap != null) {
                        String query = he.getRequestURI().getPath().substring(he.getRequestURI().getPath().indexOf("&") + 1,
                                he.getRequestURI().getPath().length());
                        Map<String, String> parameters = getParameters(query);
                        for (Map.Entry<Method, List<HttpMethodParameter>> methodToInvoke : methodMap.entrySet()) {
                            methodContext = methodToInvoke.getKey();
                            returnValue = methodContext.invoke(this, checkHttpParameters(methodToInvoke, parameters).toArray());
                        }
                    } else {
                        contextException = new Exception("Metodo inexistente");
                    }
                    break;
                }
            }

            he.getResponseHeaders().set("Server", "CNS");
            String produces = headers.getFirst(HttpHeaders.ACCEPT);
            if (contextException == null) {
                if (methodContext.getAnnotation(Produces.class) != null) {
                    if (methodContext.getAnnotation(Produces.class).value()[0].equals(MediaType.WILDCARD)) {
                        he.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
                        produces = MediaType.APPLICATION_JSON.toString();
                    } else {

                        he.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, methodContext.getAnnotation(Produces.class).value()[0]);
                        produces = methodContext.getAnnotation(Produces.class).value()[0];
                    }
                } else {
                    he.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, headers.getFirst(HttpHeaders.ACCEPT));
                }
                String response = null;
                switch (produces) {
                    case MediaType.APPLICATION_JSON: {
                        response = mapper.writeValueAsString(returnValue);
                        he.sendResponseHeaders(200, response.length());
                        he.getResponseBody().write(response.getBytes());
                        break;
                    }
                    case MediaType.APPLICATION_XML: {
                        JSONObject obj = new JSONObject(returnValue);
                        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + XML.toString(obj);
                        he.sendResponseHeaders(200, xml.length());
                        he.getResponseBody().write(xml.getBytes());
                        break;
                    }
                    case MediaType.TEXT_PLAIN: {
                        response = mapper.writeValueAsString(returnValue);
                        he.sendResponseHeaders(200, response.length());
                        he.getResponseBody().write(response.getBytes());
                        break;
                    }
                }
            } else {
                he.sendResponseHeaders(404, contextException.getMessage().length());
                he.getResponseBody().write(contextException.getMessage().getBytes());
            }
        } catch (Exception e) {
            he.sendResponseHeaders(404, e.getMessage().length());
            he.getResponseBody().write(e.getMessage().getBytes());
        } finally {
            he.getResponseBody().close();
            he.close();
        }
    }

    private List<Object> checkHttpParameters(Map.Entry<Method, List<HttpMethodParameter>> methodToInvoke, Map<String, String> parameters) throws Exception {
        List<Object> valuesParam = new ArrayList<>();
        for (HttpMethodParameter httpParam : methodToInvoke.getValue()) {
            String valueParam = parameters.get(httpParam.getParameterName());
            if (valueParam == null) {
                throw new Exception("Parametros inválidos");
            } else {
                Type type = httpParam.getParameterType();
                Class clazz = null;
                Class listType = null;
                //Clase normal
                if (type instanceof Class) {
                    clazz = (Class) type;
                } //clase parametrizada
                else if (type instanceof ParameterizedType) {
                    clazz = (Class) ((ParameterizedType) type).getRawType();
                    listType = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
                }

                if (clazz.isAssignableFrom(List.class)) {
                    List list = new ArrayList();
                    if (valueParam.contains(",")) {
                        String splitList[] = valueParam.split(",");
                        for (String param : splitList) {
                            list.add(listType.getDeclaredMethod("valueOf", String.class).invoke(new Object(), param));
                        }
                    } else {
                        list.add(listType.getDeclaredMethod("valueOf", String.class).invoke(new Object(), valueParam));
                    }
                    valuesParam.add(list);
                } else if (clazz.isAssignableFrom(Date.class)) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    valuesParam.add(format.parse(valueParam));
                } else if (clazz.isAssignableFrom(String.class)) {
                    valuesParam.add(valueParam);
                } else if (clazz.isAssignableFrom(BaseDto.class)) {
                    //Implemetar esto
                } else {
                    valuesParam.add(clazz.getDeclaredMethod("valueOf", String.class).invoke(new Object(), valueParam));
                }
            }
        }
        return valuesParam;
    }

    /**
     * Metodo que lee los valores del queryStrign de una url en una peticion get
     *
     * @param queryParameters String con los parametros
     * @return Mapa con los parametrosf
     */
    private Map<String, String> getParameters(String queryParameters) {
        String[] params = queryParameters.split("&");
        HashMap<String, String> result = new HashMap<>();
        for (String param : params) {
            if (param.split("=").length == 2) {
                result.put(param.split("=")[0], (param.split("=")[1]));
            } else {
                result.put(param.split("=")[0], "");
            }
        }
        return result;
    }

    /**
     * Nombre del contexto
     *
     * @return Nombre del contexto
     */
    public String getName() {
        return name;
    }

    /**
     * Establece el nombre del contexto
     *
     * @param name Nombre del contexto
     */
    public void setName(String name) {
        this.name = name;
    }
}
