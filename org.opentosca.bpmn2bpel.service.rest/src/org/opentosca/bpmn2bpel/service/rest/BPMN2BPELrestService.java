package org.opentosca.bpmn2bpel.service.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.eclipse.bpel.model.resource.BPELResource;
import org.eclipse.bpel.model.resource.BPELWriter;
import org.eclipse.bpmn2.util.Bpmn2Resource;
import org.eclipse.bpmn2.util.Bpmn2ResourceFactoryImpl;
import org.eclipse.emf.common.util.URI;
import org.opentosca.bpmn2bpel.converter.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class BPMN2BPELrestService {
	
	private static final Logger logger = LoggerFactory.getLogger(BPMN2BPELrestService.class);
	
	@GET
	public String getHTML() {
		return "This is a BPMN2BPEL transformation service. Please do a POST on this URL with application/xml.";
	}
	
	@POST
	@Consumes("application/xml")
	// BPMN 2.0
	@Produces("application/xml")
	// BPEL 2.0
	public Response convert(String xml) {
		
		// 1.: Create a BPMN resource
		ByteArrayInputStream bis = new ByteArrayInputStream(xml.getBytes());
		Bpmn2ResourceFactoryImpl factory = new Bpmn2ResourceFactoryImpl();
		URI uri = URI.createURI(".");
		Bpmn2Resource bpmnResource = (Bpmn2Resource) factory.createResource(uri);
		try {
			// empty loading options
			@SuppressWarnings("rawtypes")
			Map<?, ?> p = new HashMap();
			bpmnResource.load(bis, p);
		} catch (IOException e) {
			BPMN2BPELrestService.logger.error("Could not load BPMN2Resource", e);
			return Response.serverError().build();
		}
		
		// 2.: Convert the resource
		final BPELResource resBPEL = Converter.convertBPMN2BPEL(bpmnResource);
		
		// 3.: Write the BPEL file
		return Response.ok().entity(new StreamingOutput() {
			
			@Override
			public void write(OutputStream os) throws IOException, WebApplicationException {
				BPELWriter writer = new BPELWriter();
				Map<?, ?> p = new HashMap();
				writer.write(resBPEL, os, p);
			}
			
		}).build();
	}
	
}
