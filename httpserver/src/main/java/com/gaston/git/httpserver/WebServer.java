package com.gaston.git.httpserver;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import javax.ws.rs.Path;

import org.apache.log4j.Logger;


import com.sun.net.httpserver.HttpServer;

/**
 * Esta clase es la encargada de levantar el servidor http en el puerto
 * indicado, y creando los contextos dinamicamente en base a las clases que
 * estan anotadas con el annotation @Path.Por defecto busca en el package y sus
 * packages donde se encuentran estas clases
 *
 * @author gaston
 */
public final class WebServer {

    private static final Logger logger = Logger.getLogger("webServerLogger");

    /**
     * Instancia del serverWeb
     */
    private HttpServer server;

    /**
     * Puerto donde inicia el servidor
     */
    private int port;

    /**
     * Tamaño del pool concurrente de peticiones
     */
    private int poolSize;
    
    /**
     * Indica la ruta absoluta del paquete a partir del cual se realizara el escaneo
     * de los componentes que representan contextos web
     */
    private String packageBase;

    /**
     * Lista de contextos web cargados
     */
    private List<WebContext> contexts;

    public WebServer() {
    }

    public WebServer(int port, int poolSize, String packageBase) {
        this.port = port;
        this.poolSize = poolSize;
        this.packageBase = packageBase;                
    }

    /**
     * Método encargado de iniciar el servidor web
     * Se realiza la inyección dinamica de los contextos web realizando el escaneo 
     * de los paquetes y subpaquetes segun el pquete base indicado, y cada clase
     * que se encuentre y tenga la annotacion Path ademas de extender de WebContext
     * se agrega como conterxto web
     */
    public void init() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.setExecutor(Executors.newFixedThreadPool(poolSize));
            contexts = Collections.synchronizedList(new ArrayList<WebContext>());
            packageBase = packageBase.replace("/", ".");
            URL resource = this.getClass().getClassLoader().getResource(packageBase.replace(".", "/"));        
            List<Class> webContextClazzes = scanningAnnotatedClasses(new File(resource.getFile().replace("%20", " ")),
                    packageBase);            
            for (Class contextClass : webContextClazzes) {
                this.addWebContext((WebContext) contextClass.getConstructor(String.class).newInstance(((Path) contextClass.getAnnotation(Path.class)).value()));
         
            }
            server.start();
            logger.debug("Server iniciado en puerto ["+ port +"]");
            logger.debug("Server Adress ["+ server.getAddress() +"]");
        } catch (Exception ex) {
            logger.error("Error iniciando server web", ex);
        }

    }
 
    private List<Class> scanningAnnotatedClasses(File directory, String packageName) {    	
        List<Class> classes = new ArrayList<>();
        if (!directory.exists()) {            
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) { 
                classes.addAll(scanningAnnotatedClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                try {
                    //-6 para restar .class
                    Class clazz = Class.forName(packageName + '.'
                            + file.getName().substring(0, file.getName().length() - 6));
                    if (clazz.isAnnotationPresent(Path.class) && WebContext.class.isAssignableFrom(clazz)) {
                        classes.add(clazz);
                    }
                } catch (ClassNotFoundException e) {
                    logger.error("La clase " + file.getName() + " no existe", e);
                }
            }           
        }
        return classes;
    }

    /**
     * Agrega un contexto al  servidor
     * @param context ContextoWeb 
     */
    private void addWebContext(WebContext context) {
        contexts.add(context);
        server.createContext(context.getName(), context);
        logger.debug("Contexts["+context.getName()+" "+ context +"]");
    }

    /**
     * Elimina el contexto web indicado
     * @param context  COntexto a eliminar
     */
    private void removeWebContext(WebContext context) {
        contexts.remove(context);
        server.removeContext(context.getName());
    }
    
    /**
     * Numero de puerto donde se encuentra el server
     * @return Numero de puerto del servidor
     */
    public int getPort() {
        return port;
    }

    /**
     * Setea el numero de puerto del servidor
     * @param port 
     */
    public void setPort(int port) {
        this.port = port;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public List<WebContext> getContexts() {
        return contexts;
    }

    public void setContexts(List<WebContext> contexts) {
        this.contexts = contexts;
    }

    public String getPackageBase() {
        return packageBase;
    }

    public void setPackageBase(String packageBase) {
        this.packageBase = packageBase;
    }

    public void stop() {
        server.stop(0);
    }
}
