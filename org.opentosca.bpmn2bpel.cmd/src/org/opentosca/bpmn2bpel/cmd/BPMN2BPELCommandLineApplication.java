package org.opentosca.bpmn2bpel.cmd;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.opentosca.bpmn2bpel.converter.Converter;

public class BPMN2BPELCommandLineApplication implements IApplication {
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		Converter.convert("C:/git-repositories/BPMN2BPEL/examples/sugarCRMBuildPlan2.xml", "C:/git-repositories/BPMN2BPEL/results/Result-bpel.xml");
		return null;
	}
	
	@Override
	public void stop() {
		// nothing to be done here
	}
	
}
