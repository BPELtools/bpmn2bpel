package org.opentosca.bpmn2bpel.cmd;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.bpel.model.resource.BPELResource;
import org.eclipse.bpel.model.resource.BPELWriter;
import org.eclipse.bpmn2.util.Bpmn2Resource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.opentosca.bpmn2bpel.converter.Converter;

public class BPMN2BPELCommandLineApplication implements IApplication {
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object start(IApplicationContext context) throws Exception {
		// 1.: Create a BPMN resource
		ResourceSet resourceSet = new ResourceSetImpl();
		Bpmn2Resource res = (Bpmn2Resource) resourceSet.getResource(URI.createFileURI("C:/git-repositories/BPMN2BPEL/examples/sugarCRMBuildPlan2.xml"), true);
		
		// 2.: Convert the resource
		BPELResource resBPEL = Converter.convertBPMN2BPEL(res);
		
		// 3.: Write the BPEL file
		BPELWriter writer = new BPELWriter();
		Map<?, ?> p = new HashMap();
		// The path of the translated bpel is selected
		File fstream = new File("C:/git-repositories/BPMN2BPEL/results/Result-bpel.xml");
		FileOutputStream fout = new FileOutputStream(fstream);
		writer.write(resBPEL, fout, p);
		fout.close();
		
		return null;
	}
	
	@Override
	public void stop() {
		// nothing to be done here
	}
	
}
