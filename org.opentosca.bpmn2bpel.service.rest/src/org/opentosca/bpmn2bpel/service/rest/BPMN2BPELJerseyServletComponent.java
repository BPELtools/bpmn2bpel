package org.opentosca.bpmn2bpel.service.rest;

import org.osgi.service.http.HttpService;

import com.sun.jersey.core.spi.component.ComponentContext;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class BPMN2BPELJerseyServletComponent {
	
	// private static final Logger LOG =
	// LoggerFactory.getLogger(BPMN2BPELJerseyServletComponent.class);

	protected void bindHttpService(HttpService httpService) {
		// BPMN2BPELJerseyServletComponent.LOG.debug("Binding HTTP Service");
		try {
			httpService.registerServlet("/bpmn2bpel", new com.sun.jersey.spi.container.servlet.ServletContainer(new BPMN2BPELApplication()), null, null);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	protected void unbindHttpService(HttpService httpService) {
		// BPMN2BPELJerseyServletComponent.LOG.debug("Unbinding HTTP Service");
		httpService.unregister("/bpmn2bpel");
	}
	
	protected void activate(ComponentContext componentContext) {
		// // the Uri for the ContainerApi is also stored in the SettingsBundle
		// String port =
		// componentContext.getBundleContext().getProperty("org.osgi.service.http.port");
		// if (port == null || port.trim().length() == 0) {
		// port = "1337";
		// }
		// BPMN2BPELJerseyServletComponent.LOG.info("JerseyServletComponent aktiviert: http://localhost:{}{}",
		// port, ResourceConstants.ROOT);
	}
	
}
