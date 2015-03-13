package com.gaston.git.httpserver;

/**
 * Esta clase debe ser extendida por todas aquellas clases que se comporten como
 * contextos web
 *
 * @author gaston
 */
public abstract class WebContext extends ContextHandler {

    public WebContext(String name) {
        super(name);
    }

}
