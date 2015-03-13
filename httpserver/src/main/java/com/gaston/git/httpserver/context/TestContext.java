package com.gaston.git.httpserver.context;

import com.gaston.git.httpserver.WebContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author gaston
 */
@Path(value = "/context")
@Consumes(value = MediaType.APPLICATION_JSON)
public class TestContext extends WebContext {

    public TestContext(String name) {
        super(name);
    }

    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    public String getValue(
            @QueryParam(value = "key") String key,
            @QueryParam(value = "value") String value) throws Exception {
        return key + value;
    }
}
