package org.opentosca.bpmn2bpel.service.rest;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {
	
	private static BundleContext context;
	private ServiceRegistration<?> registration;
	
	static BundleContext getContext() {
		return Activator.context;
	}
	
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		BPMN2BPELrestService service = new BPMN2BPELrestService();
		this.registration = Activator.context.registerService(BPMN2BPELrestService.class.getName(), service, null);
	}
	
	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		this.registration.unregister();
	}
	
}
