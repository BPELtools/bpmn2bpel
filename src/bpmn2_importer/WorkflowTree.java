package bpmn2_importer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


import org.eclipse.bpel.model.BPELFactory;
import org.eclipse.bpel.model.Sequence;
import org.eclipse.bpel.model.impl.ActivityImpl;
import org.eclipse.bpel.model.impl.BPELFactoryImpl;
import org.eclipse.bpel.model.impl.InvokeImpl;
import org.eclipse.bpel.model.impl.PartnerLinkImpl;
import org.eclipse.bpel.model.impl.ReceiveImpl;
import org.eclipse.bpel.model.impl.SequenceImpl;
import org.eclipse.bpel.model.impl.ProcessImpl;
import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.CallActivity;
import org.eclipse.bpmn2.CatchEvent;
import org.eclipse.bpmn2.ComplexGateway;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.EventBasedGateway;
import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.ImplicitThrowEvent;
import org.eclipse.bpmn2.InclusiveGateway;
import org.eclipse.bpmn2.IntermediateCatchEvent;
import org.eclipse.bpmn2.IntermediateThrowEvent;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.SubProcess;
import org.eclipse.bpmn2.Task;
import org.eclipse.bpmn2.ThrowEvent;
import org.eclipse.bpmn2.util.Bpmn2Resource;
import org.eclipse.emf.ecore.EObject;
import org.jbpt.graph.DirectedGraph;
import org.jbpt.graph.algo.rpst.RPST;
import org.jbpt.graph.algo.rpst.RPSTNode;
import org.jbpt.graph.algo.tctree.TCType;
import org.jbpt.hypergraph.abs.Vertex;

@SuppressWarnings("rawtypes")
public class WorkflowTree extends DirectedGraph {

	private Bpmn2Resource BPMN2Resource;
	private RPST rpstg;
	
	public WorkflowTree(Bpmn2Resource res) {
		super();
		this.BPMN2Resource = res;
		// TODO Auto-generated constructor stub
	}
	
	// NOTE: Here it could be factorized the function by returning the vertex or null instead of iterating twice
	// the VertexSet
	@SuppressWarnings("unchecked")
	public WFNode containsVertex(String id){
		
		Collection<Vertex> vertices = getVertices();
		//Set<WFNode> vSet = (Set<WFNode>) vertexSet();
		WFNode node = null;
		
		WFNode nodei;
		
		for(Iterator<Vertex> p = vertices.iterator(); p.hasNext();) {
			
			nodei = (WFNode) p.next();

		    if((nodei.getId()).equals(id)){
		    	
		    	node = nodei;
		    	break;
		    }

		}
		
		return node;
	}
	
	public WFNode getRoot(){
		
		Collection<Vertex> vertices = getVertices();
		WFNode node = null;
		WFNode nodei;
		
		for(Iterator<Vertex> p = vertices.iterator(); p.hasNext();) {
			
			nodei = (WFNode) p.next();
			
			if(nodei.getElement().getIncoming().size()==0){
				
				node = nodei;
				break;
			}
		}
		
		return node;
	}
	
	public void setRPST(RPST rpstGraph){
		
		rpstg = rpstGraph;
	}
	
	public void FillTree(EObject eobj){
		
		for (EObject son: eobj.eContents()) {
			
			WFNode n1 = null;
			
			if(son!=null){
				
				// Check if element is a Flow WFNode of the bpmn2 File
				if(son instanceof FlowNode){
					
					
					//FlowNode nodo = (FlowNode) son;
					
					// It's defined what type of element it is Event, Activity or Gateway
					FlowNode nodo = (FlowNode) son;
					n1 = new WFNode(nodo);
					
					
					System.out.println("son: "+nodo.getName());
					
					if(this.containsVertex(n1.getId())==null){
						if(this.addVertex(n1)!=null)
							System.out.println("Vertex "+n1.getName()+" newly added succesfully");}
					else{
						n1 = this.containsVertex(n1.getId());
						System.out.println("Vertex not added, already exists");}
					System.out.println("vertices: "+this.countVertices());
					
					
					// Get outgoing SequenceFlows if any
					for(SequenceFlow s : ((FlowNode) n1.getElement()).getOutgoing()){
						
						System.out.println("verticesd: "+this.countVertices());
						String targetid = s.getTargetRef().getId();
						WFNode ntarget = this.containsVertex(targetid);
						
						if(ntarget!=null){
							// NOTE: The testing of correct creation of edge is missing!
							if(this.addEdge(n1,ntarget)!=null){
								
								System.out.println("Edge "+n1.getName()+","+ntarget.getName()+" created succesfully1");
							}
							else{
								System.out.println("Edge not created");
							}
						}
						else{
							ntarget = new WFNode(s.getTargetRef());
							if(this.addVertex(ntarget)!=null){
								System.out.println("Vertex "+ntarget.getName()+" added succesfully");
								System.out.println("vertices: "+this.countVertices());
								if(this.addEdge(n1,ntarget)!=null){
									System.out.println("verticess: "+this.countVertices());
									System.out.println("Edge "+n1.getName()+","+ntarget.getName()+" created succesfully2");
									
								}
								else{
									System.out.println("Edge not created");
								}
							}							
							
						}
						System.out.println("->  outgoing: "+s.getName());
						System.out.println("out target: "+ s.getTargetRef().getName());
						
						
					}
				}
				System.out.println("verticess: "+this.countVertices());
				//System.out.println("clase: "+son.getClass());
				FillTree(son);
			
			}
			
		}
		
		//System.out.println("vertices: "+this.countVertices());
		//System.out.println("edges: "+this.countEdges());
		
	}
	
	public Collection<RPSTNode> organizeRPST(RPSTNode parent, Collection<RPSTNode> children){
		
		
      	RPSTNode root = parent;
		String rootName = "";
		RPSTNode n = null;
		boolean orderFinished = false;
		Collection childrenf = new ArrayList<RPSTNode>();
		Collection children1 = children;
		
		
		// The Parent node is analyzed, when its of type Bond the order isn't important
		if(parent.getType().equals(TCType.B)){
		
			return children;
		}
		else{
			// Get the name of the entry node
			rootName = parent.getEntry().getName();
			
			// The RPSTRoot 1st Child node is added as first
			for(Object e : children1){
				
				n = (RPSTNode) e;
				if(n.getEntry().getName().equals(rootName)){
					childrenf.add(n);
					break;
				}
				
			}
			
			// The collection of children of root is ordered
			while(!orderFinished){
			 for(Object e1 : children1){
				
				RPSTNode n1 = (RPSTNode) e1;
				
				if(n1.getEntry().equals(n.getExit())){
					childrenf.add(n1);
					n = n1;
					orderFinished = false;
					break;	
				}
				else{
					orderFinished = true;
				}	
			 }
			}
			return childrenf;
		}
		
		
		
	}
	
	// Returns the Bpel Model asociated with this workflowgraph
	public ProcessImpl wftree2BpelModel(RPSTNode rpstnode) {
		// TODO Auto-generated method stub
		
		RPSTNode rpstgRoot;
		BPELFactoryImpl mainfact = new BPELFactoryImpl();
		ProcessImpl mainproc = (ProcessImpl) mainfact.createProcess();

		BPELFactoryImpl creator = new BPELFactoryImpl();
		SequenceImpl mainSeq = (SequenceImpl) creator.createSequence();
		
		// For every child of root RPSTNode visit and unfold in order to analyze and create BPEL Structures
		
		for(Object e: organizeRPST(rpstnode,rpstg.getChildren(rpstnode))){
			
			RPSTNode node = (RPSTNode) e;

			mainproc.setActivity(mainSeq);
			System.out.println(node.getName()+" - "+node.getEntry().getName()+" - "+node.getExit().getName());
			
			
			// If the node is of trivial type 
			if(node.getType().equals(TCType.T)){
				
				//Call wftree2BpelModelPart
				WFNode nexit = (WFNode) node.getExit();
				
				if(nexit.getElement() instanceof EndEvent){
				    
					// This adds the entry node translation to Bpel when the component is "T"
					org.eclipse.bpel.model.Activity a1 = wftree2BpelModelPart(node);
					mainSeq.getActivities().add(a1);
					
					//Add the end Event to the Bpel Model.
					
				}
				else{
					
					org.eclipse.bpel.model.Activity a1 = wftree2BpelModelPart(node);
					mainSeq.getActivities().add(a1);
					
				}
				
				
			}
			else if(node.getType().equals(TCType.R)){
			}
			else if(node.getType().equals(TCType.B)){
				this.wftree2BpelModel(node);
			}
			else if(node.getType().equals(TCType.P)){
				this.wftree2BpelModel(node);
			}
			
			
		}
		return mainproc;
		
	}
	
	private org.eclipse.bpel.model.Activity wftree2BpelModelPart(RPSTNode node) {
		// TODO Auto-generated method stub
		// If the node is of trivial type
		
		BPELFactoryImpl mainfact = new BPELFactoryImpl();
		
			if(node.getType().equals(TCType.T)){
				
				//Call wftree2BpelModelPart
				WFNode nentry = (WFNode) node.getEntry();
				
				if(nentry.getElement() instanceof Task){
					
					Task t1 = (Task) nentry.getElement();
					InvokeImpl i1 = (InvokeImpl) mainfact.createInvoke();
					
					// Set name of the Invoke
					i1.setName(nentry.getName());

					return i1;
					
					
				}
				else if(nentry.getElement() instanceof StartEvent){
					
					StartEvent se1 = (StartEvent) nentry.getElement();
					ReceiveImpl r1 = (ReceiveImpl) mainfact.createReceive();
					
					// Set name of the Receive
					r1.setName(se1.getName());
					
					// Set CreateInstance attribute
					r1.setCreateInstance(true);
					
					//Define Partnerlink (in the case of a start event the partner should be "itself")
					
					// Set the input variable (NOT NULL)
					//r1.setVariable(null);
					
					return r1;
					
				}
				
				
			}
			else if(node.getType().equals(TCType.R)){
				
				return null;
			}
			else if(node.getType().equals(TCType.B)){
				return null;
			}
			else if(node.getType().equals(TCType.P)){
				return null;
			}
			else{
				return null;
			}
			return null;
	}

}
