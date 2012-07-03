package bpmn2_importer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.RepaintManager;


import org.eclipse.bpel.model.BPELFactory;
import org.eclipse.bpel.model.Condition;
import org.eclipse.bpel.model.ElseIf;
import org.eclipse.bpel.model.OnAlarm;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.RepeatUntil;
import org.eclipse.bpel.model.Sequence;
import org.eclipse.bpel.model.Variables;
import org.eclipse.bpel.model.While;
import org.eclipse.bpel.model.impl.ActivityImpl;
import org.eclipse.bpel.model.impl.BPELFactoryImpl;
import org.eclipse.bpel.model.impl.ConditionImpl;
import org.eclipse.bpel.model.impl.ElseIfImpl;
import org.eclipse.bpel.model.impl.ElseImpl;
import org.eclipse.bpel.model.impl.FlowImpl;
import org.eclipse.bpel.model.impl.ForEachImpl;
import org.eclipse.bpel.model.impl.IfImpl;
import org.eclipse.bpel.model.impl.InvokeImpl;
import org.eclipse.bpel.model.impl.LinkImpl;
import org.eclipse.bpel.model.impl.LinksImpl;
import org.eclipse.bpel.model.impl.OnAlarmImpl;
import org.eclipse.bpel.model.impl.OnMessageImpl;
import org.eclipse.bpel.model.impl.PartnerLinkImpl;
import org.eclipse.bpel.model.impl.PickImpl;
import org.eclipse.bpel.model.impl.ReceiveImpl;
import org.eclipse.bpel.model.impl.ScopeImpl;
import org.eclipse.bpel.model.impl.SequenceImpl;
import org.eclipse.bpel.model.impl.ProcessImpl;
import org.eclipse.bpel.model.impl.SourceImpl;
import org.eclipse.bpel.model.impl.SourcesImpl;
import org.eclipse.bpel.model.impl.TargetImpl;
import org.eclipse.bpel.model.impl.TargetsImpl;
import org.eclipse.bpel.model.impl.WhileImpl;
import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.CallActivity;
import org.eclipse.bpmn2.CatchEvent;
import org.eclipse.bpmn2.CompensateEventDefinition;
import org.eclipse.bpmn2.ComplexGateway;
import org.eclipse.bpmn2.DataInputAssociation;
import org.eclipse.bpmn2.DataObject;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.EventBasedGateway;
import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.Expression;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.ImplicitThrowEvent;
import org.eclipse.bpmn2.Import;
import org.eclipse.bpmn2.InclusiveGateway;
import org.eclipse.bpmn2.Interface;
import org.eclipse.bpmn2.IntermediateCatchEvent;
import org.eclipse.bpmn2.IntermediateThrowEvent;
import org.eclipse.bpmn2.LoopCharacteristics;
import org.eclipse.bpmn2.MessageEventDefinition;
import org.eclipse.bpmn2.MultiInstanceLoopCharacteristics;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.ReceiveTask;
import org.eclipse.bpmn2.SendTask;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.ServiceTask;
import org.eclipse.bpmn2.SignalEventDefinition;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.SubProcess;
import org.eclipse.bpmn2.Task;
import org.eclipse.bpmn2.ThrowEvent;
import org.eclipse.bpmn2.TimerEventDefinition;
import org.eclipse.bpmn2.impl.Bpmn2FactoryImpl;
import org.eclipse.bpmn2.impl.DataObjectImpl;
import org.eclipse.bpmn2.impl.ExclusiveGatewayImpl;
import org.eclipse.bpmn2.impl.ExpressionImpl;
import org.eclipse.bpmn2.impl.FormalExpressionImpl;
import org.eclipse.bpmn2.impl.GatewayImpl;
import org.eclipse.bpmn2.impl.IntermediateCatchEventImpl;
import org.eclipse.bpmn2.impl.LoopCharacteristicsImpl;
import org.eclipse.bpmn2.impl.MessageEventDefinitionImpl;
import org.eclipse.bpmn2.impl.MultiInstanceLoopCharacteristicsImpl;
import org.eclipse.bpmn2.impl.SignalEventDefinitionImpl;
import org.eclipse.bpmn2.impl.SubProcessImpl;
import org.eclipse.bpmn2.impl.TimerEventDefinitionImpl;
import org.eclipse.bpmn2.util.Bpmn2Resource;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.DirectedGraph;
import org.jbpt.graph.abs.AbstractDirectedEdge;
import org.jbpt.graph.abs.AbstractDirectedGraph;
import org.jbpt.graph.abs.IDirectedGraph;
import org.jbpt.graph.algo.rpst.RPST;
import org.jbpt.graph.algo.rpst.RPSTNode;
import org.jbpt.graph.algo.tctree.TCType;
import org.jbpt.hypergraph.abs.Vertex;

@SuppressWarnings("rawtypes")
public class BPMNProcessTree extends DirectedGraph {

	private Bpmn2Resource BPMN2Resource;
	private RPST rpstg;
	private List<Import> BpmnImports; 
	private ProcessImpl mainproc;
	
	public BPMNProcessTree(Bpmn2Resource res) {
		super();
		this.BPMN2Resource = res;
		// TODO Auto-generated constructor stub
	}
	
	public BPMNProcessTree(){
		super();
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
					BPMNProcessTree subTree = new BPMNProcessTree();
					
					System.out.println("son: "+nodo.getName());
					
					// If the vertex isn't part of the Graph
					if(this.containsVertex(n1.getId())==null){
						
						// If the element found is a subprocess, it's intern graph should be filled
						if(son instanceof SubProcess){
							
							subTree.FillTree(son);
							n1.setSubProcessTree(subTree);
							
						}						
						if(this.addVertex(n1)!=null)
							System.out.println("Vertex "+n1.getName()+" newly added succesfully");}
					else{
						n1 = this.containsVertex(n1.getId());
						System.out.println("Vertex "+n1.getName()+ " not added, already exists");}
					System.out.println("vertices: "+this.countVertices());
					
					System.out.println(n1.getElement().getOutgoing().size());
					
					// Get outgoing SequenceFlows if any
					for(SequenceFlow s : ((FlowNode) n1.getElement()).getOutgoing()){
						
						System.out.println("verticesd: "+this.countVertices());
						String targetid = s.getTargetRef().getId();
						WFNode ntarget = this.containsVertex(targetid);
						WFEdge edge1;
						
						if(ntarget!=null){
							// NOTE: The testing of correct creation of edge is missing!
							
							if(this.addEdge(s,n1,ntarget)!=null){
								
								System.out.println("Edge "+n1.getName()+","+ntarget.getName()+" created succesfully1");
							}
							else{
								System.out.println("Edge not created");
							}
						}
						else{
							ntarget = new WFNode(s.getTargetRef());
							
							// If the already existing vertex is a subprocess, the intern graph is filled
							if(s.getTargetRef() instanceof SubProcess){
								subTree.FillTree(s.getTargetRef());
								ntarget.setSubProcessTree(subTree);
							}
							if(this.addVertex(ntarget)!=null){
								System.out.println("Vertex "+ntarget.getName()+" added succesfully");
								System.out.println("vertices: "+this.countVertices());
								if(this.addEdge(s,n1,ntarget)!=null){
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
				if(son instanceof SubProcess == false){
					FillTree(son);
				}
			
			}
			
		}
		
		//System.out.println("vertices: "+this.countVertices());
		System.out.println("edges: "+this.countEdges());
		
	}
	
	public Collection<RPSTNode> organizeRPST(RPSTNode parent, Collection<RPSTNode> children){
		
		
      	RPSTNode root = parent;
		String rootName = "";
		RPSTNode n = null;
		Collection childrenf = new ArrayList<RPSTNode>();
		Set<RPSTNode> RPSTset = null;
		RPSTNode actualNode = null;
		
		
		// The Parent node is analyzed, when its of type Bond the order isn't important
		if(parent.getType().equals(TCType.B)){
		
			return children;
		}
		else{
			
			// codigo nuevo****
			
				Map<String,Set<RPSTNode>> entry = new Hashtable<String, Set<RPSTNode>>();
				
				// Fill Hash
				for(Object e : children){
					
					n = (RPSTNode) e;
					String startn = n.getEntry().getName();
					if (!entry.containsKey(startn))
						entry.put(startn,new HashSet<RPSTNode>());
					
					entry.get(startn).add(n);
					
				}
				
				// Get the name of the entry node 
				rootName = parent.getEntry().getName();
				
				//Organize the elements and add them to a list
				while(!rootName.equals(parent.getExit().getName())){
					
					RPSTset = entry.get(rootName);
					Iterator<RPSTNode> it = RPSTset.iterator();
					
					// First of all the cyclic components (if any) are added
					while(it.hasNext()){
						
						actualNode = (RPSTNode) it.next();
						if(rootName.equals(actualNode.getExit().getName())){
						
							childrenf.add(actualNode);
							RPSTset.remove(actualNode);						
							
						}
						
					}
					//Then the other.
					it = RPSTset.iterator();
					while(it.hasNext()){
						
						actualNode = (RPSTNode) it.next();
						childrenf.add(actualNode);
						
					}
					
					rootName = actualNode.getExit().getName();
				}
				
				return childrenf;
				
			//codigo nuevo****
		}
		
	}
	
	/*public WFEdge addEdge(WFEdge edge){
		
		this.edges.keySet().add(edge);
		
	}*/
	
	// Returns the Bpel Model asociated with this workflowgraph
	public ProcessImpl BpmnProctree2BpelModel(RPSTNode rpstnode) {
		// TODO Auto-generated method stub

		BPELFactoryImpl mainfact = new BPELFactoryImpl();
		mainproc = (ProcessImpl) mainfact.createProcess();

		BPELFactoryImpl creator = new BPELFactoryImpl();
		SequenceImpl mainSeq = (SequenceImpl) creator.createSequence();
		org.eclipse.bpel.model.Activity a1 = null;
		
		// For every child of root RPSTNode visit and unfold in order to analyze and create BPEL Structures
		
		for(Object e: organizeRPST(rpstnode,rpstg.getChildren(rpstnode))){
			
			RPSTNode node = (RPSTNode) e;

			mainproc.setActivity(mainSeq);
			System.out.println(node.getName()+" - "+node.getEntry().getName()+" - "+node.getExit().getName());
			
			
			// If the node is of trivial type 
			if(node.getType().equals(TCType.T)){
				
				//Call wftree2BpelModelPart

				// The translation of the entry node of the "T" component is added to the BPEL Model
				a1 = BpmnProctree2BpelModelPart(node,rpstg);
				
				// A function that checks whether the task has DataInput and Output Associations is called.
				
				
				
			}
			else if(node.getType().equals(TCType.R)){
				
				WFNode entry = (WFNode) node.getEntry();
				WFNode exit = (WFNode) node.getExit();
				FlowNode entryNode = entry.getElement();
				FlowNode exitNode = exit.getElement();
				Collection<RPSTNode> RigidChildren = rpstg.getChildren(node);
				IDirectedGraph rigidfragm;
				RPST BondGen = null;
				
				for(Object e2 : RigidChildren){
					
					RPSTNode child = (RPSTNode) e2;
					
					if(child.getType().equals(TCType.T)){
						
						rigidfragm = node.getFragment();
						AbstractDirectedEdge edge = (AbstractDirectedEdge) rigidfragm.getEdge(child.getEntry(),child.getExit());
						rigidfragm.removeEdge(edge);
						BondGen = new RPST(rigidfragm);
						
					}
					
				}
				
				if(BondGen!=null){
					a1 = BpmnProctree2BpelModelPart(BondGen.getRoot(),BondGen);}
				
			}
			else if(node.getType().equals(TCType.B)){
				a1 = BpmnProctree2BpelModelPart(node,rpstg);
			}
			else if(node.getType().equals(TCType.P)){
				//this.wftree2BpelModel(node);
			}
			
			if(a1!=null){
				mainSeq.getActivities().add(a1);}
		}
		return mainproc;
		
	}
	
	
	private org.eclipse.bpel.model.Activity BpmnProctree2BpelModelPart(RPSTNode node, RPST rpstParent) {
		// TODO Auto-generated method stub
		// If the node is of trivial type
		
		BPELFactoryImpl mainfact = new BPELFactoryImpl();
		RPSTNode nodeP = rpstParent.getParent(node);
		
			if(node.getType().equals(TCType.T)){
								
				//Call wftree2BpelModelPart
				WFNode nentry = (WFNode) node.getEntry();
				
				// If instance of normal Task
				if(nentry.getElement() instanceof Task){
					
					if(nentry.getElement() instanceof ServiceTask || nentry.getElement() instanceof SendTask){
						
						Task t1 = (Task) nentry.getElement();
						InvokeImpl i1 = (InvokeImpl) mainfact.createInvoke();
						
						// Set name of the Invoke
						i1.setName(nentry.getName());
						
						//Check if the task has a Loop definition
						if(t1.getLoopCharacteristics()!=null){
							
							if(t1.getLoopCharacteristics() instanceof MultiInstanceLoopCharacteristics){
								
								MultiInstanceLoopCharacteristicsImpl mloopchar = (MultiInstanceLoopCharacteristicsImpl) t1.getLoopCharacteristics();
								ForEachImpl fe1 = (ForEachImpl) mainfact.createForEach();
								org.eclipse.bpel.model.impl.ExpressionImpl exprS = (org.eclipse.bpel.model.impl.ExpressionImpl) mainfact.createExpression();
								org.eclipse.bpel.model.impl.ExpressionImpl exprF = (org.eclipse.bpel.model.impl.ExpressionImpl) mainfact.createExpression();
								ExpressionImpl exp = (ExpressionImpl) mloopchar.getCompletionCondition();
								exprS.setBody("1");
								exprF.setBody(exp.getId());
								ScopeImpl scope1 = (ScopeImpl) mainfact.createScope();
								scope1.setActivity(i1);
								fe1.setStartCounterValue(exprS);
								fe1.setFinalCounterValue(exprF);
								fe1.setParallel(!mloopchar.isIsSequential());
								fe1.setActivity(scope1);
								
								return fe1;
								
							}
							else{
								LoopCharacteristicsImpl loopchar = (LoopCharacteristicsImpl) t1.getLoopCharacteristics();
							 	ExpressionImpl loopCond = (ExpressionImpl) loopchar.eContents().get(0);
								WhileImpl while1 = (WhileImpl) mainfact.createWhile();
								
								Condition BpelWhileCond = mainfact.createCondition();
								
								// The body of the condition set to the id. (NOTE)
								BpelWhileCond.setBody(loopCond.getId());
								
								//The Bpel-While structure is filled with the activity (in this case invoke) and condition
								while1.setActivity(i1);
								while1.setCondition(BpelWhileCond);
								
								return while1;
							}
							
						}
						
						// If DataInputAssociations
						if(t1.getDataInputAssociations()!=null){
							
							// For every DataInputAssociation the Dataobjects are obtained
							for(DataInputAssociation datai: t1.getDataInputAssociations()){
								
								
							}
						}
	
						return i1;
					}
					else if(nentry.getElement() instanceof ReceiveTask){
						
						Task t1 = (Task) nentry.getElement();
						ReceiveImpl r1 = (ReceiveImpl) mainfact.createReceive();
						
						// Set name of the Invoke
						r1.setName(nentry.getName());
						
						if(t1.getLoopCharacteristics()!=null){
							
							LoopCharacteristicsImpl loopchar = (LoopCharacteristicsImpl) t1.getLoopCharacteristics();
						 	ExpressionImpl loopCond = (ExpressionImpl) loopchar.eContents().get(0);
							WhileImpl while1 = (WhileImpl) mainfact.createWhile();
							
							Condition BpelWhileCond = mainfact.createCondition();
							
							// The body of the condition set to the id. (NOTE)
							BpelWhileCond.setBody(loopCond.getId());
							
							//The Bpel-While structure is filled with the activity (in this case invoke) and condition
							while1.setActivity(r1);
							while1.setCondition(BpelWhileCond);
							
							return while1;
							
					   }
					 
						return r1;
						
					}
					
					
				}
				else if(nentry.getElement() instanceof ReceiveTask && rpstParent.getParent(node).getDescription().equals("ignore_rcv")){
					
					ReceiveTask rt1 = (ReceiveTask) nentry.getElement();
					ReceiveImpl r1 = (ReceiveImpl) mainfact.createReceive();
					
					// Set name of the receive
					r1.setName(nentry.getName());
					
					return r1;
					
				}
				else if(nentry.getElement() instanceof SubProcess){
					
					ScopeImpl scope1 = (ScopeImpl) mainfact.createScope();
					BPMNProcessTree subprocT = nentry.getSubprocessTree();
					
					RPST rpstgraph = new RPST(subprocT);
					RPSTNode subProcRoot = rpstgraph.getRoot();
					subprocT.setRPST(rpstgraph);
					nentry.setSubProcessTree(subprocT);
					ProcessImpl p1 = (ProcessImpl) subprocT.BpmnProctree2BpelModel(subProcRoot);
					ActivityImpl a1 = (ActivityImpl) p1.getActivity();
					
					scope1.setName(nentry.getName());
					scope1.setActivity(a1);
					
					return scope1;
					
				}
				else if(nentry.getElement() instanceof ParallelGateway && !nodeP.getEntry().equals(nentry)){
					
					return mainfact.createEmpty();
					
				}
				else {
					
                     // If the entry element is a Start event nothing is done (momentarily)
					return null;
					
				}
				
				
			}
			else if(node.getType().equals(TCType.R)){
			
				Collection<RPSTNode> RigidChildren = rpstg.getChildren(node);
				IDirectedGraph rigidfragm;
				RPST BondGen = null;				
				
				for(Object e2 : RigidChildren){
					
					RPSTNode child = (RPSTNode) e2;
					
					if(child.getType().equals(TCType.T)){
						
						rigidfragm = node.getFragment();
						AbstractDirectedEdge edge = (AbstractDirectedEdge) rigidfragm.getEdge(child.getEntry(),child.getExit());
						rigidfragm.removeEdge(edge);
						BondGen = new RPST(rigidfragm);
						
					}
					
				}
				
				if(BondGen!=null){
					return BpmnProctree2BpelModelPart(BondGen.getRoot(),BondGen);}
			}
			else if(node.getType().equals(TCType.B)){
				
				WFNode entry = (WFNode) node.getEntry();
				WFNode exit = (WFNode) node.getExit();
				boolean iscyclic = false;
				Collection fragEdges = node.getFragmentEdges();
				FlowNode entryNode = entry.getElement();
				FlowNode exitNode = exit.getElement();
				Collection<RPSTNode> BondChildren = rpstParent.getChildren(node);
				
				// If the B Component is Acyclic
				if(!entry.isCyclic(BondChildren,exit)){

					// If an XOR Structure is found
					if(entryNode instanceof ExclusiveGateway && exitNode instanceof ExclusiveGateway){
						
						IfImpl if1 = (IfImpl) mainfact.createIf();
						ElseIfImpl elseif1 = null;
						org.eclipse.bpel.model.Activity actif;
						ElseImpl else1 = null;
						FormalExpressionImpl expr1 = null;
						
						// For every Polygon child of the Bond element 
						for(Object e : BondChildren){
							
							RPSTNode child = (RPSTNode) e;
							SequenceFlow conditionFlow = entry.getSourceOfPolygon((AbstractDirectedGraph) child.getFragment());
							expr1 = (FormalExpressionImpl) conditionFlow.getConditionExpression() ;
							ConditionImpl cond1 = (ConditionImpl) mainfact.createCondition();
							
							if(elseif1==null){
								
								if(expr1 != null){
									cond1.setBody(expr1.getMixed().getValue(0));
									if1.setCondition(cond1);
									actif = BpmnProctree2BpelModelPart(child,rpstParent);
									if1.setActivity(actif);
								}
								else{
									// if it doesn't already exist an else
									if(else1 == null){
										else1 = (ElseImpl) mainfact.createElse();
										else1.setActivity(BpmnProctree2BpelModelPart(child,rpstParent));
										if1.setElse(else1);
									}
									else{
										// IF STRUCTURE ERROR! (more than one conditionless branch)
									}
								}
								
							}
							else{
								//If the expression is not null it's an elseif
								if(expr1!=null){
									cond1.setBody(expr1.getMixed().getValue(0));
									elseif1.setCondition(cond1);
									actif = BpmnProctree2BpelModelPart(child,rpstParent);
									elseif1.setActivity(actif);
								    if1.getElseIf().add(elseif1);	
								}
								//Otherwise is a default sequenceflow (the remaining else)
								else{
									// If it doesn't already exist an else
									if(else1 == null){
									    else1 = (ElseImpl) mainfact.createElse();
										else1.setActivity(BpmnProctree2BpelModelPart(child,rpstParent));
										if1.setElse(else1);
									}
									// If there is already an else
									else{
										// IF STRUCTURE ERROR! (more than one conditionless branch)
									}
								}
								
							}
							
							elseif1 = (ElseIfImpl) mainfact.createElseIf();
						}					
						return if1;
						
					}
					//If an AND Structure is found
					else if(entryNode instanceof ParallelGateway && exitNode instanceof ParallelGateway){
						
						FlowImpl flow1 = (FlowImpl) mainfact.createFlow();
						org.eclipse.bpel.model.Activity actflow;
						
						// For every Polygon child of the Bond element 
						for(Object e : BondChildren){
							
							RPSTNode child = (RPSTNode) e;
							
							actflow = BpmnProctree2BpelModelPart(child,rpstParent);
							flow1.getActivities().add(actflow);
						}
						return flow1;
					}
					// If an Inclusive OR Structure is found
					else if(entryNode instanceof InclusiveGateway && exitNode instanceof InclusiveGateway){
						
						FlowImpl flowp = (FlowImpl) mainfact.createFlow();
						FlowImpl flowc = (FlowImpl) mainfact.createFlow();
						org.eclipse.bpel.model.Activity actflow;
						LinksImpl links = (LinksImpl) mainfact.createLinks();
						LinkImpl link1= (LinkImpl) mainfact.createLink();
						LinkImpl link2= (LinkImpl) mainfact.createLink();
						SourcesImpl sources = (SourcesImpl) mainfact.createSources();
						SourcesImpl sourcesc = (SourcesImpl) mainfact.createSources();
						SourceImpl source1 = (SourceImpl) mainfact.createSource();
						TargetsImpl targets = (TargetsImpl) mainfact.createTargets();
						TargetsImpl targetsc = (TargetsImpl) mainfact.createTargets();
						TargetImpl target1 = (TargetImpl) mainfact.createTarget();
						SequenceFlow sourceSeqF,targetSeqF = null;
						FormalExpressionImpl expr1 = null;
						
						// For every Outgoing link of the gateway
						for(Object e : BondChildren){
							
							RPSTNode child = (RPSTNode) e;
							sourceSeqF = entry.getSourceOfPolygon((AbstractDirectedGraph) child.getFragment());
							targetSeqF = exit.getTargetOfPolygon((AbstractDirectedGraph) child.getFragment());
							ConditionImpl cond1 = (ConditionImpl) mainfact.createCondition();
							expr1 = (FormalExpressionImpl) sourceSeqF.getConditionExpression();
							
							// Obtain the child-Bpel-Activity
							actflow = BpmnProctree2BpelModelPart(child,rpstParent);
							
							//Add the activity to the flow child
							flowc.getActivities().add(actflow);
							
							// The Body of the condition is filled with the id of the corresponding BPMN condition (NOTE)
							cond1.setBody(expr1.getMixed().getValue(0));
							
							// Create the corresponding source-SequenceFlow link 
							link1.setName(sourceSeqF.getName());
							
							//Add the created link to the links list
							links.getChildren().add(link1);
							
							
							// Create a Bpel-Source element and associate the already existing link to it
							source1.setLink(link1);
							
							if(expr1!=null){
								source1.setTransitionCondition(cond1);}
							
							// Add the source to the sources list
							sources.getChildren().add(source1);
							
							sourcesc.getChildren().add(source1);
							flowc.setSources(sourcesc);
							
							if(!targetSeqF.equals(sourceSeqF)){
								// Create the corresponding target-SequenceFlow link 
								link2.setName(targetSeqF.getName());
								
								// Create a Bpel-Target element and associate the already existing link to it
								target1.setLink(link2);
								
								// Add the target to the targets list
								targets.getChildren().add(target1);
								
								//Add targets of the flow-child element
								targetsc.getChildren().add(target1);
								flowc.setTargets(targetsc);
								
							}
							
							flowp.setLinks(links);
							flowp.setSources(sources);
							flowp.setTargets(targets);
							flowp.getActivities().add(flowc);
							
							
							
						}
						return flowp;
						
					}
					else if(entryNode instanceof EventBasedGateway && exitNode instanceof ExclusiveGateway){
						
						PickImpl pick1 = (PickImpl) mainfact.createPick();
						
						for(Object e : BondChildren){
							
							RPSTNode child = (RPSTNode) e;
							SequenceFlow gateOut = entry.getSourceOfPolygon((AbstractDirectedGraph) child.getFragment());
							org.eclipse.bpel.model.Activity a1;
							
							
							if(gateOut.getTargetRef() instanceof IntermediateCatchEvent){
								
								IntermediateCatchEventImpl catchEvent = (IntermediateCatchEventImpl) gateOut.getTargetRef();
								
								// It's assumed that the Event definitions of an Intermediate Catch event is just 1
								if(catchEvent.getEventDefinitions().get(0) instanceof MessageEventDefinition){
									
									MessageEventDefinitionImpl msgEvent = (MessageEventDefinitionImpl) catchEvent.getEventDefinitions().get(0);
									
									OnMessageImpl onMsg1 = (OnMessageImpl) mainfact.createOnMessage();
									
									// Momentarily the OnMessage element's operation is given the Event's name + "operation" (NOTE)
									onMsg1.setOperationName(msgEvent.getOperationRef().getName());
									
									// Obtain the child-Bpel-Activity
									a1 = BpmnProctree2BpelModelPart(child,rpstParent);
									
									//Add the activity to the flow child
									onMsg1.setActivity(a1);
									
									pick1.getMessages().add(onMsg1);
									
								}
								else if(catchEvent.getEventDefinitions().get(0) instanceof TimerEventDefinition){
									
									TimerEventDefinitionImpl timeEvent = (TimerEventDefinitionImpl) catchEvent.getEventDefinitions().get(0);

									OnAlarmImpl onalrm1= (OnAlarmImpl) mainfact.createOnAlarm();
									
									Expression exprTime = timeEvent.getTimeDuration();
									org.eclipse.bpel.model.Expression exprOnAlarm = mainfact.createExpression();

									if(timeEvent.getTimeDuration()!=null){
										
										// The Body of the Bpel expression is filled with the Id of the bpmn expression (NOTE)
										exprOnAlarm.setBody(exprTime.getId());
										onalrm1.setFor(exprOnAlarm);
										
									}
									else if(timeEvent.getTimeDate()!=null){
										
										// The Body of the Bpel expression is filled with the Id of the bpmn expression (NOTE)
										exprOnAlarm.setBody(exprTime.getId());
										onalrm1.setUntil(exprOnAlarm);
										
									}
									
									// Obtain the child-Bpel-Activity
									a1 = BpmnProctree2BpelModelPart(child,rpstParent);
									
									//Add the activity to the flow child
									onalrm1.setActivity(a1);
									
									pick1.getAlarm().add(onalrm1);
									
								}
								else if(catchEvent.getEventDefinitions().get(0) instanceof SignalEventDefinition){
									
									SignalEventDefinitionImpl sigEvent = (SignalEventDefinitionImpl) catchEvent.getEventDefinitions().get(0);
									
									OnMessageImpl onMsg1 = (OnMessageImpl) mainfact.createOnMessage();
									
									onMsg1.setOperationName(sigEvent.getSignalRef().getName());
									
									a1 = BpmnProctree2BpelModelPart(child,rpstParent);
									
									onMsg1.setActivity(a1);
									
									pick1.getMessages().add(onMsg1);
								}
								
							}
							else if(gateOut.getTargetRef() instanceof ReceiveTask){
								
								ReceiveTask rt1 = (ReceiveTask) gateOut.getTargetRef();
								
								// Set in the description, that the receive task must be ignored (it's not part of the activity)
								child.setDescription("ignore_rcv");
								
								OnMessageImpl onMsg1 = (OnMessageImpl) mainfact.createOnMessage();
								
								// Momentarily the OnMessage element's operation is given the Event's name + "operation" (NOTE)
								onMsg1.setOperationName(rt1.getName()+"-operation");
								
								// Obtain the child-Bpel-Activity
								a1 = BpmnProctree2BpelModelPart(child,rpstParent);
								
								//Add the activity to the flow child
								onMsg1.setActivity(a1);
								
								pick1.getMessages().add(onMsg1);
								
								}
							
							
							
						}
						return pick1;
						
					}
					
				}
				//If the structure found is cyclic
				else{
					//If the structure is that of Repeat
					if(exit.numberOfPathsto(BondChildren, entry)==1 && exit.numberOfEdgesto(BondChildren, entry)==1 && exitNode instanceof ExclusiveGateway){
						
						RepeatUntil repeat = mainfact.createRepeatUntil();
						org.eclipse.bpel.model.Activity a1 = mainfact.createActivity();
						IfImpl if1 = (IfImpl) mainfact.createIf();
						int EntryToExit = entry.numberOfPathsto(BondChildren, exit);
						FormalExpressionImpl expr1 = null;
						org.eclipse.bpel.model.Activity actif;
						ElseIfImpl elseif1 = null;
						ElseImpl else1 = null;
						
						for(Object e : BondChildren){
							
							RPSTNode child = (RPSTNode) e;
					        WFNode centry = (WFNode) child.getEntry();
					        WFNode cexit = (WFNode) child.getExit();
							
					        // If the entry node of the Polygon equals the entry node of the Bond, we have an activity of the repeat
							if(centry.equals(entry)){
								
								a1 = BpmnProctree2BpelModelPart(child,rpstParent);
								repeat.setActivity(a1);
								
							}
							// Otherwise we have the returning edge that represents the evaluation of the repeat's condition
							else if(centry.equals(exit)){
								
								SequenceFlow backRep = centry.getSourceOfPolygon((AbstractDirectedGraph) child.getFragment());
								Expression exprbackRep = backRep.getConditionExpression();
								Condition Repcond = mainfact.createCondition();
								
								//The condition is set with the negation of the Bpmn-expression's id
								Repcond.setBody("not-"+exprbackRep.getId());
								repeat.setCondition(Repcond);
								
							}
							
						}
						return repeat;
							
					}					
					//If the structure is that of While
					else if(entry.numberOfPathsto(BondChildren, exit)==1 && entry.numberOfEdgesto(BondChildren, exit)==1 && exitNode instanceof ExclusiveGateway){
						
						While while1 = mainfact.createWhile();
						org.eclipse.bpel.model.Activity a1 = mainfact.createActivity();
						int polygons = BondChildren.size();
						
						if(polygons==2){
							
							for(Object e : BondChildren){
							
								RPSTNode child = (RPSTNode) e;
								SequenceFlow whileCondFlow = exit.getSourceOfPolygon((AbstractDirectedGraph) child.getFragment());
								if(whileCondFlow!=null){
									
									Expression whileCond = whileCondFlow.getConditionExpression();
									Condition BpelWhileCond = mainfact.createCondition();
									
									// the main activity of the While is created and the body of the condition set
									BpelWhileCond.setBody(whileCond.getId());
									a1 = BpmnProctree2BpelModelPart(child,rpstParent);
									
									//The Bpel-While structure is filled with the activity and condition
									while1.setActivity(a1);
									while1.setCondition(BpelWhileCond);
									
								}
							}
							return while1;
						}
						else if(polygons>2){
							
							// ***** Create the if ******
							IfImpl if1 = (IfImpl) mainfact.createIf();
							ElseIfImpl elseif1 = null;
							org.eclipse.bpel.model.Activity actif;
							ElseImpl else1 = null;
							FormalExpressionImpl expr1 = null;
							
							// For every Polygon child of the Bond element 
							for(Object e : BondChildren){
								
								
								RPSTNode child = (RPSTNode) e;
								WFNode centry = (WFNode) child.getEntry();
						        WFNode cexit = (WFNode) child.getExit();
								if(centry.equals(entry) && cexit.equals(exit)){
									continue;									
								}
								SequenceFlow conditionFlow = exit.getSourceOfPolygon((AbstractDirectedGraph) child.getFragment());
								expr1 = (FormalExpressionImpl) conditionFlow.getConditionExpression() ;
								Condition cond1 = mainfact.createCondition();
								
								// The Body of the condition is filled with the id of the corresponding BPMN condition (NOTE)
								cond1.setBody(expr1.getMixed().getValue(0));
								
								if(elseif1==null){
									
									if1.setCondition(cond1);
									actif = BpmnProctree2BpelModelPart(child,rpstParent);
									if1.setActivity(actif);
									
								}
								else{
									
									//If the expression is not null it's an elseif
									if(expr1!=null){
										elseif1.setCondition(cond1);
										actif = BpmnProctree2BpelModelPart(child,rpstParent);
										elseif1.setActivity(actif);
									    if1.getElseIf().add(elseif1);	
									}
									//Otherwise is a default sequenceflow (the remaining else)
									else{
										else1.setActivity(BpmnProctree2BpelModelPart(child,rpstParent));
										if1.setElse(else1);
									}
									
								}
								
								elseif1 = (ElseIfImpl) mainfact.createElseIf();
							}
							// ***********************
							
							// Add the if to the while as an activity
							while1.setActivity(if1);
							
							//Add the condition of the while
							// cond1 or cond2 or .... condN
							return while1;
						}
						
						
					}
					// The Remaining structure is that of a Repeat + while 
					else if((exit.numberOfPathsto(BondChildren, entry)>1 || (exit.numberOfPathsto(BondChildren, exit)==1 && exit.numberOfEdgesto(BondChildren, exit)==0)) 
							&& entry.numberOfPathsto(BondChildren, exit)>1 && entry.numberOfEdgesto(BondChildren, exit)==0 && exitNode instanceof ExclusiveGateway)
					{
												
						//The Structure must be rearranged
						SequenceImpl seq1 = (SequenceImpl) mainfact.createSequence();
						SequenceImpl seq2 = (SequenceImpl) mainfact.createSequence();
						SequenceImpl seq3 = (SequenceImpl) mainfact.createSequence();
						IfImpl if1 = (IfImpl) mainfact.createIf();
						org.eclipse.bpel.model.Activity if2 = null;
						ElseIfImpl elseif1 = null;
						org.eclipse.bpel.model.Activity actif;
						ElseImpl else1 = null;
						FormalExpressionImpl expr1 = null;
						WhileImpl while1 = (WhileImpl) mainfact.createWhile();
						Condition whileCondition1 = mainfact.createCondition();
						String WhileCondition = "";

						
					   for(Object e: BondChildren){
							
							RPSTNode child = (RPSTNode) e;
							WFNode centry = (WFNode) child.getEntry();
					        WFNode cexit = (WFNode) child.getExit();
							
					        if(centry.equals(exit) && cexit.equals(entry)){
					        	
					        	SequenceFlow conditionFlow = centry.getSourceOfPolygon((AbstractDirectedGraph) child.getFragment());
								expr1 = (FormalExpressionImpl) conditionFlow.getConditionExpression() ;
								Condition cond1 = mainfact.createCondition();
					        	
								cond1.setBody(expr1.getMixed().getValue(0));
								// Get the text of the expression (value of the first entry of the Map)
								WhileCondition += expr1.getMixed().getValue(0)+" or ";
								
								if(elseif1==null){
									
									if1.setCondition(cond1);
									actif = BpmnProctree2BpelModelPart(child,rpstParent);
									if1.setActivity(actif);
									
								}
								else{
									
									//If the expression is not null it's an elseif
									if(expr1!=null){
										elseif1.setCondition(cond1);
										actif = BpmnProctree2BpelModelPart(child,rpstParent);
										elseif1.setActivity(actif);
									    if1.getElseIf().add(elseif1);	
									}
									//Otherwise is a default sequenceflow, but it's not possible to have to default sequenceflows
									else{
										// The Model is not executable, some OPAQUE message should be added.
									}
									
								}
								elseif1 = (ElseIfImpl) mainfact.createElseIf();
					        }
					        else if(centry.equals(entry) && cexit.equals(exit)){
					        	
					        	seq1 = (SequenceImpl) mainfact.createSequence();
					        	
					        	if2 = BpmnProctree2BpelModelPart(child,rpstParent);
					        	
					        }
							
						}
					    
					    seq1.getActivities().add(if2);
					    seq2.getActivities().add(if1);
					    seq2.getActivities().add(if2);
					    while1.setActivity(seq2);
					    WhileCondition = WhileCondition.substring(0, WhileCondition.length()-4);
					    whileCondition1.setBody(WhileCondition);
					    while1.setCondition(whileCondition1);
					    seq3.getActivities().add(if2);
					    seq3.getActivities().add(while1);
					    
						return seq3;
					}
					
				}
				
			}
			else if(node.getType().equals(TCType.P)){
				
				Collection<RPSTNode> childrenP = organizeRPST(node,rpstParent.getChildren(node));
				int childrenPsize = childrenP.size();
				SequenceImpl seq1 = (SequenceImpl) mainfact.createSequence();
				
				// There's only one component as a son and it's the Trivial (split -> join) SequenceFlow
				if(childrenPsize==1){
					
					return mainfact.createEmpty();
					
				}
				else{
				    
					for(Object e : organizeRPST(node,rpstParent.getChildren(node))){
						
						RPSTNode childP = (RPSTNode) e;
						org.eclipse.bpel.model.Activity act1 = BpmnProctree2BpelModelPart(childP,rpstParent);
						if(act1!=null && childrenPsize == 2){
							
							return act1;
							
						}
						else if(act1!=null && childrenPsize > 2){
							
							seq1.getActivities().add(act1);
							
						}
						
					}
					return seq1;
				}
				
			}
			else{
				return null;
			}
			return null;
	}

	public void AssignImports(List<Import> imports) {
		// TODO Auto-generated method stub
		BpmnImports = imports;
	}
	
	public WFEdge addEdge(SequenceFlow sf, WFNode s, WFNode t) {
		if (s == null || t == null) return null;
		Collection<DirectedEdge> es = this.getEdgesWithSourceAndTarget(s, t);
		if (es.size()>0) {
			Iterator<DirectedEdge> i = es.iterator();
			while (i.hasNext()) {
				WFEdge e = (WFEdge) i.next();
				if (e.getVertices().size()==2)
					return null;
			}
		}
		
		WFEdge e = new WFEdge(this, sf, s, t);
		return e;
	}

	public void restructureQuasi(RPST rpstG, RPSTNode rpstnode, WFNode BondExit, WFNode BondEntry, boolean hasBondParent) {
		
		// The whole RPST is searched in order to find quasi components
		for(Object e: organizeRPST(rpstnode,rpstG.getChildren(rpstnode))){
			
			RPSTNode node = (RPSTNode) e;
			RPST rpstaux = null;
			IDirectedGraph newfrag = null;
			
            if(node.getType().equals(TCType.R)){
				
            	// Analize this case
            	
			}
			else if(node.getType().equals(TCType.B)){
				
				if(BondExit!=null && BondEntry!=null && hasBondParent == true){
					
					// If the bond component has the same exit as its containing Bond then restructure
					if(BondExit.equals(node.getExit())){
						
						WFNode Newgateway = null;
						
						//Add new gateway and redirect edges.
						if(BondExit.getElement() instanceof ExclusiveGateway){
							
							Bpmn2FactoryImpl bpmnFact = new Bpmn2FactoryImpl();
							ExclusiveGateway ex1 = bpmnFact.createExclusiveGateway();
							ex1.setName(BondExit.getName()+"'");
							Newgateway = new WFNode(ex1);
							
						}
						else if(BondExit.getElement() instanceof ParallelGateway){
							
							Bpmn2FactoryImpl bpmnFact = new Bpmn2FactoryImpl();
							ParallelGateway parg = bpmnFact.createParallelGateway();
							parg.setName(BondExit.getName()+"'");
							Newgateway = new WFNode(parg);
						}
						
						//For all children of the childBond that end in the common gateway redirect them to the new one
						 newfrag = redirectEdges(node.getFragment(),Newgateway,BondExit,false);
						 rpstaux = new RPST(newfrag);
						 node = rpstaux.getRoot();
						 
						 for(Object child : rpstaux.getChildren(node)){
							 
							 RPSTNode auxnode = (RPSTNode) child;
							 if(auxnode.getType().equals(TCType.B)){
								 node = auxnode;
							 }
							 
						 }
						
						
					}
					// If the bond component has the same entry as its containing Bond then restructure
					else if(BondEntry.equals(node.getEntry())){
						
						WFNode Newgateway = null;
						
						if(BondEntry.getElement() instanceof ParallelGateway){
							
							Bpmn2FactoryImpl bpmnFact = new Bpmn2FactoryImpl();
							ParallelGateway parg = bpmnFact.createParallelGateway();
							parg.setName(BondEntry.getName()+"'");
							Newgateway = new WFNode(parg);
						}
						
						//For all children of the childBond that start in the common gateway redirect them to the new one
						 newfrag = redirectEdges(node.getFragment(),Newgateway,BondEntry,true);
						 rpstaux = new RPST(newfrag);
						 node = rpstaux.getRoot();
						 
						 for(Object child : rpstaux.getChildren(node)){
							 
							 RPSTNode auxnode = (RPSTNode) child;
							 if(auxnode.getType().equals(TCType.B)){
								 node = auxnode;
							 }
							 
						 }
						
					}
					
				}
				WFNode bondExit = (WFNode) node.getExit();
				WFNode bondEntry = (WFNode) node.getEntry();
				hasBondParent = true;
				// If a Bond component is found, this is traversed in order to find out if it forms a quasi component.
				if(rpstaux!=null){
					restructureQuasi(rpstaux, node,bondExit, bondEntry, hasBondParent);}
				else{
					restructureQuasi(rpstG, node, bondExit, bondEntry, hasBondParent);
				}
				
			}
			else if(node.getType().equals(TCType.P)){
				restructureQuasi(rpstG,node,BondExit,BondEntry,hasBondParent);
			}
		}
		
	}

	private IDirectedGraph redirectEdges(IDirectedGraph fragment, WFNode newgateway, WFNode bondGateway, boolean isEntry) {
		// TODO Auto-generated method stub
		
		this.addVertex(newgateway);
		fragment.addVertex(newgateway);
		
		// If the bond Gateway received as parameter is the Entry node
		// then new edges are created to come out of the new gateway and
		// be directed to the branches of the child bond.
		if(isEntry){
			
			WFNode bondEntry = bondGateway;
			this.addEdge(bondEntry,newgateway);
			
			// All the edges that come out of the old gateway are selected
			// in order to be deleted.
			for(Object e: fragment.getEdgesWithSource(bondEntry)){
				
				AbstractDirectedEdge edge = (AbstractDirectedEdge) e;
				WFNode target = (WFNode) edge.getTarget();
				
				for(Object e2: this.getEdgesWithSource(bondEntry)){
					
					DirectedEdge edge2 = (DirectedEdge) e2;
					WFNode target2 = (WFNode) edge2.getTarget();
					if(target.equals(target2)){
						// edge is removed from the main graph
						this.removeEdge(edge2);
						// edge is removed from the fragment of the rpst component
						fragment.removeEdge(edge);
						// new edge is added to the main graph from the new gateway to the branch target
						this.addEdge(newgateway, target);
						// same new edge is added to the fragment of the rpst component
						fragment.addEdge(newgateway, target);
						break;
					}
					
				}
				
			}
			
			// finally a new edge is added (old gateway -> new gateway)
			fragment.addEdge(bondEntry, newgateway);
			
		}
		// If the bond Gateway received as parameter is the Exit node
		// then the Edges of the bond (child) are redirected to the new exit gateway
		else{
			
			WFNode bondExit = bondGateway;
			this.addEdge(newgateway, bondExit);
			
			// All the edges that end in the old gateway are selected in order to "redirect" them
			// to the new gateway
			for(Object e: fragment.getEdgesWithTarget(bondExit)){
				
				AbstractDirectedEdge edge = (AbstractDirectedEdge) e;
				WFNode source = (WFNode) edge.getSource();
				
				for(Object e2: this.getEdgesWithTarget(bondExit)){
					
					DirectedEdge edge2 = (DirectedEdge) e2;
					WFNode source2 = (WFNode) edge2.getSource();
					if(source.equals(source2)){
						// The Edge is removed from the main graph and the fragment
						this.removeEdge(edge2);
						fragment.removeEdge(edge);
						// The new edge is added to the main graph (source branch -> new gateway)
						this.addEdge(source, newgateway);
						// same edge is added to the fragment
						fragment.addEdge(source, newgateway);
						break;
					}
					
				}
				
			}
			
			// finally a new edge is added (new gateway -> old gateway)
			fragment.addEdge(newgateway, bondExit);
		}
		
		return fragment;
	}

}
