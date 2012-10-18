package org.opentosca.bpmn2bpel.service.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;


@Path("/test")
public class BPMN2BPELrestService {
	
	@GET
	public String getHTML() {
		return "test";
	}
	
	public BPMN2BPELrestService() {
		System.out.println("initialized HEREANDTHERE");
	}
}
