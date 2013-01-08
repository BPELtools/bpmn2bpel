package org.opentosca.bpmn2bpel.converter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.bpel.model.resource.BPELResource;
import org.eclipse.bpel.model.resource.BPELResourceImpl;
import org.eclipse.bpel.model.resource.BPELWriter;
import org.eclipse.bpmn2.util.Bpmn2Resource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.jbpt.graph.algo.rpst.RPST;
import org.jbpt.graph.algo.rpst.RPSTNode;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

public class Converter {
	
	private static final XLogger logger = XLoggerFactory.getXLogger(Converter.class);
	
	public static void convert(String bpmnFilename, String bpelFilename) throws IOException {
		// 1.: Create a BPMN resource
		ResourceSet resourceSet = new ResourceSetImpl();
		Bpmn2Resource res = (Bpmn2Resource) resourceSet.getResource(URI.createFileURI(bpmnFilename), true);
		
		// 2.: Convert the resource
		BPELResource resBPEL = Converter.convertBPMN2BPEL(res);
		
		// 3.: Write the BPEL file
		BPELWriter writer = new BPELWriter();
		Map<?, ?> p = new HashMap();
		FileOutputStream fout = new FileOutputStream(new File(bpelFilename));
		writer.write(resBPEL, fout, p);
		fout.close();
	}
	
	/**
	 * Converts a given BPMN process into a BPEL process
	 * 
	 * @param res the BPMN process to convert
	 * @return the BPEL process representing the BPMN process in BPEL
	 */
	public static BPELResource convertBPMN2BPEL(Bpmn2Resource res) {
		// creates the internal graph representation of the given process
		BPMNProcessTree wt = new BPMNProcessTree(res);
		
		// Transform the BPMN Process Tree into a single entry Single Exit
		// process tree
		wt.transfrom2SESE();
		
		RPST rpstgraph = new RPST(wt);
		wt.setRPST(rpstgraph);
		RPSTNode rpstRoot = rpstgraph.getRoot();
		// logger.debug(rpstgraph.getRoot().getFragment());
		Converter.logger.debug("RPST:\n{}", rpstgraph.toString());
		Converter.logger.debug("Vertex count: {}", Integer.toString(rpstgraph.countVertices()));
		
		// Transform any Rigid components (Generalised Flows this graph may
		// have)
		wt.TransformGenFlows(rpstgraph, rpstRoot);
		rpstgraph = new RPST(wt);
		wt.setRPST(rpstgraph);
		rpstRoot = rpstgraph.getRoot();
		Converter.logger.debug("RPST after rigid transformation:\n{}", rpstgraph.toString());
		
		// Restructure any quasicomponents this graph may have
		wt.restructureQuasi(rpstgraph, rpstRoot, null, null, false);
		rpstgraph = new RPST(wt);
		wt.setRPST(rpstgraph);
		rpstRoot = rpstgraph.getRoot();
		// logger.debug(wt.toString());
		Converter.logger.debug("RPST after quasi component restructuring:\n{}", rpstgraph.toString());
		
		// Traverse the workflowtree and create BpelModel
		org.eclipse.bpel.model.Process bpelmodel = wt.BpmnProctree2BpelModel(rpstRoot);
		
		// The BPELResource is declared with the bpel model as its content
		BPELResourceImpl resbpel = new BPELResourceImpl();
		resbpel.getContents().add(bpelmodel);
		
		return resbpel;
	}
	
}
