package bpmn2_importer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.bpel.model.impl.ProcessImpl;
import org.eclipse.bpel.model.resource.BPELResourceImpl;
import org.eclipse.bpel.model.resource.BPELWriter;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.util.Bpmn2Resource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.jbpt.graph.algo.rpst.RPST;
import org.jbpt.graph.algo.rpst.RPSTNode;

public class ApplicationStarter implements IApplication {
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public Object start(IApplicationContext context) throws Exception {
		
		ResourceSet resourceSet = new ResourceSetImpl();
		Bpmn2Resource res = (Bpmn2Resource) resourceSet.getResource(URI.createFileURI("C:/git-repositories/BPMN2BPEL/examples/sugarCRMBuildPlan2.xml"), true);
		DocumentRoot root = (DocumentRoot) res.getContents().get(0);
		BPMNProcessTree wt = new BPMNProcessTree(res);
		BPMNProcessTree rpstWt;
		WFNode wftreeRoot;
		
		for (EObject content : root.eContents()) {
			if (content instanceof Definitions) {
				Definitions def = (Definitions) content;
				System.out.println("id: " + def.getId());
				System.out.println("class: " + def.getClass());
				
				wt.FillTree(content);
				wt.searchBoundaryEvents(content, wt, null);
				System.out.println(wt.toString());
				wt.AssignImports(def.getImports());
			} else {
				
				System.out.println(content.getClass());
				System.out.println("class: " + content.getClass());
				
			}
		}
		
		// Transform the BPMN Process Tree into a single entry Single Exit
		// process tree
		wt.transfrom2SESE();
		
		RPST rpstgraph = new RPST(wt);
		wt.setRPST(rpstgraph);
		RPSTNode rpstRoot = rpstgraph.getRoot();
		// System.out.println(rpstgraph.getRoot().getFragment());
		System.out.println(rpstgraph.toString());
		System.out.println(rpstgraph.countVertices());
		
		// Transform any Rigid components (Generalised Flows( this graph may
		// have
		wt.TransformGenFlows(rpstgraph, rpstRoot);
		rpstgraph = new RPST(wt);
		wt.setRPST(rpstgraph);
		rpstRoot = rpstgraph.getRoot();
		System.out.println(rpstgraph.toString());
		
		// Restructure any quasicomponents this graph may have
		wt.restructureQuasi(rpstgraph, rpstRoot, null, null, false);
		rpstgraph = new RPST(wt);
		wt.setRPST(rpstgraph);
		rpstRoot = rpstgraph.getRoot();
		// System.out.println(wt.toString());
		System.out.println(rpstgraph.toString());
		
		// Traverse the workflowtree and create BpelModel
		ProcessImpl bpelmodel = wt.BpmnProctree2BpelModel(rpstRoot);
		
		// The BPELResource is declared with the bpel model as its content
		BPELResourceImpl resbpel = new BPELResourceImpl();
		resbpel.getContents().add(bpelmodel);
		
		// The BPELWriter is created
		BPELWriter writer = new BPELWriter();
		Map<?, ?> p = new HashMap();
		
		// The path of the translated bpel is selected
		File fstream = new File("C:/git-repositories/BPMN2BPEL/results/Result-bpel.xml");
		FileOutputStream fout = new FileOutputStream(fstream);
		writer.write(resbpel, fout, p);
		fout.close();
		
		return null;
	}
	
	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}
	
}
