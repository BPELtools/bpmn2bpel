package org.opentosca.bpmn2bpel.service.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

/**
 * JAX-RS resource provision <br/>
 * Inspired by OpenTOSCA
 */
public class BPMN2BPELApplication extends Application {
	
	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> s = new HashSet<Class<?>>();
		// add all root resources
		s.add(BPMN2BPELrestService.class);
		return s;
	}
	
}
