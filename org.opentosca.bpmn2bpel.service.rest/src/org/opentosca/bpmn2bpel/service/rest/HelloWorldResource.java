package org.opentosca.bpmn2bpel.service.rest;

import javax.ws.rs.*;

@Path("/helloworld")
public class HelloWorldResource {
    @GET
    @Produces("text/plain")
    public String getMessage() {
        return "Hello world!";
    }
}