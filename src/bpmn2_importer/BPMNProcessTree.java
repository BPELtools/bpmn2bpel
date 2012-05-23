package bpmn2_importer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.RepaintManager;


import org.eclipse.bpel.model.BPELFactory;
import org.eclipse.bpel.model.Condition;
import org.eclipse.bpel.model.ElseIf;
import org.eclipse.bpel.model.OnAlarm;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.RepeatUntil;
import org.eclipse.bpel.model.Sequence;
import org.eclipse.bpel.model.Variables;
import org.eclipse.bpel.model.impl.ActivityImpl;
import org.eclipse.bpel.model.impl.BPELFactoryImpl;
import org.eclipse.bpel.model.impl.ConditionImpl;
import org.eclipse.bpel.model.impl.ElseIfImpl;
import org.eclipse.bpel.model.impl.ElseImpl;
import org.eclipse.bpel.model.impl.FlowImpl;
import org.eclipse.bpel.model.impl.IfImpl;
import org.eclipse.bpel.model.impl.InvokeImpl;
import org.eclipse.bpel.model.impl.LinkImpl;
import org.eclipse.bpel.model.impl.LinksImpl;
import org.eclipse.bpel.model.impl.OnAlarmImpl;
import org.eclipse.bpel.model.impl.OnMessageImpl;
import org.eclipse.bpel.model.impl.PartnerLinkImpl;
import org.eclipse.bpel.model.impl.PickImpl;
import org.eclipse.bpel.model.impl.ReceiveImpl;
import org.eclipse.bpel.model.impl.SequenceImpl;
import org.eclipse.bpel.model.impl.ProcessImpl;
import org.eclipse.bpel.model.impl.SourceImpl;
import org.eclipse.bpel.model.impl.SourcesImpl;
import org.eclipse.bpel.model.impl.TargetImpl;
import org.eclipse.bpel.model.impl.TargetsImpl;
import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.CallActivity;
import org.eclipse.bpmn2.CatchEvent;
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
import org.eclipse.bpmn2.MessageEventDefinition;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.ReceiveTask;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.SubProcess;
import org.eclipse.bpmn2.Task;
import org.eclipse.bpmn2.ThrowEvent;
import org.eclipse.bpmn2.TimerEventDefinition;
import org.eclipse.bpmn2.impl.DataObjectImpl;
import org.eclipse.bpmn2.impl.ExpressionImpl;
import org.eclipse.bpmn2.impl.GatewayImpl;
import org.eclipse.bpmn2.impl.IntermediateCatchEventImpl;
import org.eclipse.bpmn2.impl.MessageEventDefinitionImpl;
import org.eclipse.bpmn2.impl.TimerEventDefinitionImpl;
import org.eclipse.bpmn2.util.Bpmn2Resource;
import org.eclipse.emf.ecore.EObject;
import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.DirectedGraph;
import org.jbpt.graph.abs.AbstractDirectedGraph;
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
				FillTree(son);
			
			}
			
		}
		
		//System.out.println("vertices: "+this.countVertices());
		System.out.println("edges: "+this.countEdges());
		
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
	
	/*public WFEdge addEdge(WFEdge edge){
		
		this.edges.keySet().add(edge);
		
	}*/
	
	// Returns the Bpel Model asociated with this workflowgraph
	public ProcessImpl BpmnProctree2BpelModel(RPSTNode rpstnode) {
		// TODO Auto-generated method stub
		
		RPSTNode rpstgRoot;
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
				a1 = BpmnProctree2BpelModelPart(node);
				
				// A function that checks whether the task has DataInput and Output Associations is called.
				
				
				
			}
			else if(node.getType().equals(TCType.R)){
			}
			else if(node.getType().equals(TCType.B)){
				a1 = BpmnProctree2BpelModelPart(node);
			}
			else if(node.getType().equals(TCType.P)){
				//this.wftree2BpelModel(node);
			}
			
			if(a1!=null){
				mainSeq.getActivities().add(a1);}
		}
		return mainproc;
		
	}
	
	@SuppressWarnings("unused")
	private org.eclipse.bpel.model.Activity BpmnProctree2BpelModelPart(RPSTNode node) {
		// TODO Auto-generated method stub
		// If the node is of trivial type
		
		BPELFactoryImpl mainfact = new BPELFactoryImpl();
		
			if(node.getType().equals(TCType.T)){
				
				//Call wftree2BpelModelPart
				WFNode nentry = (WFNode) node.getEntry();
				
				// If instance of normal Task
				if(nentry.getElement() instanceof Task){
					
					Task t1 = (Task) nentry.getElement();
					InvokeImpl i1 = (InvokeImpl) mainfact.createInvoke();
					
					// Set name of the Invoke
					i1.setName(nentry.getName());
					
					// If DataInputAssociations
					if(t1.getDataInputAssociations()!=null){
						
						// For every DataInputAssociation the Dataobjects are obtained
						for(DataInputAssociation datai: t1.getDataInputAssociations()){
							
							
						}
					}

					return i1;
					
					
				}
				else if(nentry.getElement() instanceof ReceiveTask && rpstg.getParent(node).getDescription().equals("ignore_rcv")){
					
					ReceiveTask rt1 = (ReceiveTask) nentry.getElement();
					ReceiveImpl r1 = (ReceiveImpl) mainfact.createReceive();
					
					// Set name of the receive
					r1.setName(nentry.getName());
					
					return r1;
					
				}
				else {
					
                     // If the entry element is a Start event nothing is done (momentarily)
					return null;
					
				}
				
				
			}
			else if(node.getType().equals(TCType.R)){
				
				return null;
			}
			else if(node.getType().equals(TCType.B)){
				
				WFNode entry = (WFNode) node.getEntry();
				WFNode exit = (WFNode) node.getExit();
				boolean iscyclic = false;
				Collection fragEdges = node.getFragmentEdges();
				FlowNode entryNode = entry.getElement();
				FlowNode exitNode = exit.getElement();
				
				// If the B Component is Acyclic
				if(!entry.isCyclic(fragEdges)){
					
					// If an XOR Structure is found
					if(entryNode instanceof ExclusiveGateway && exitNode instanceof ExclusiveGateway){
						
						IfImpl if1 = (IfImpl) mainfact.createIf();
						ElseIfImpl elseif1 = null;
						org.eclipse.bpel.model.Activity actif;
						ElseImpl else1 = null;
						ExpressionImpl expr1 = null;
						
						// For every Polygon child of the Bond element 
						for(Object e : rpstg.getChildren(node)){
							
							RPSTNode child = (RPSTNode) e;
							expr1 = (ExpressionImpl) entry.getConditionOfPolygon((AbstractDirectedGraph) child.getFragment());
							ConditionImpl cond1 = (ConditionImpl) mainfact.createCondition();
							
							// The Body of the condition is filled with the id of the corresponding BPMN condition (NOTE)
							cond1.setBody(expr1.getId());
							
							if(elseif1==null){
								
								if1.setCondition(cond1);
								actif = BpmnProctree2BpelModelPart(child);
								if1.setActivity(actif);
								
							}
							else{
								
								//If the expression is not null it's an elseif
								if(expr1!=null){
									elseif1.setCondition(cond1);
									actif = BpmnProctree2BpelModelPart(child);
									elseif1.setActivity(actif);
								    if1.getElseIf().add(elseif1);	
								}
								//Otherwise is a default sequenceflow (the remaining else)
								else{
									else1.setActivity(BpmnProctree2BpelModelPart(child));
									if1.setElse(else1);
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
						for(Object e : rpstg.getChildren(node)){
							
							RPSTNode child = (RPSTNode) e;
							
							actflow = BpmnProctree2BpelModelPart(child);
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
						ExpressionImpl expr1 = null;
						
						// For every Outgoing link of the gateway
						for(Object e : rpstg.getChildren(node)){
							
							RPSTNode child = (RPSTNode) e;
							sourceSeqF = entry.getSourceOfPolygon((AbstractDirectedGraph) child.getFragment());
							targetSeqF = exit.getTargetOfPolygon((AbstractDirectedGraph) child.getFragment());
							ConditionImpl cond1 = (ConditionImpl) mainfact.createCondition();
							expr1 = (ExpressionImpl) sourceSeqF.getConditionExpression();
							
							// Obtain the child-Bpel-Activity
							actflow = BpmnProctree2BpelModelPart(child);
							
							//Add the activity to the flow child
							flowc.getActivities().add(actflow);
							
							// The Body of the condition is filled with the id of the corresponding BPMN condition (NOTE)
							cond1.setBody(expr1.getId());
							
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
						
						for(Object e : rpstg.getChildren(node)){
							
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
									onMsg1.setOperationName(catchEvent.getName()+"-operation");
									
									// Obtain the child-Bpel-Activity
									a1 = BpmnProctree2BpelModelPart(child);
									
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
									a1 = BpmnProctree2BpelModelPart(child);
									
									//Add the activity to the flow child
									onalrm1.setActivity(a1);
									
									pick1.getAlarm().add(onalrm1);
									
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
								a1 = BpmnProctree2BpelModelPart(child);
								
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
					if(exit.numberOfEdgesTo((AbstractDirectedGraph) node.getFragment(), entry)==1 && exitNode instanceof ExclusiveGateway){
						
						RepeatUntil repeat = mainfact.createRepeatUntil();
						org.eclipse.bpel.model.Activity a1 = mainfact.createActivity();
						
						for(Object e : rpstg.getChildren(node)){
							
							RPSTNode child = (RPSTNode) e;
					        WFNode centry = (WFNode) child.getEntry();
					        WFNode cexit = (WFNode) child.getExit();
							
							if(centry.equals(entry)){
								
								a1 = BpmnProctree2BpelModelPart(child);
								repeat.setActivity(a1);
								
							}
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
					else if(entry.numberOfEdgesTo((AbstractDirectedGraph) node.getFragment(), exit)==1){
						
						
						
					}
					
				}
				
			}
			else if(node.getType().equals(TCType.P)){
				
				Collection<RPSTNode> childrenP = organizeRPST(node,rpstg.getChildren(node));
				int childrenPsize = childrenP.size();
				SequenceImpl seq1 = (SequenceImpl) mainfact.createSequence();
				
				// There's only one component as a son and it's the Trivial (split -> join) SequenceFlow
				if(childrenPsize==1){
					
					return mainfact.createEmpty();
					
				}
				else{
				    
					for(Object e : organizeRPST(node,rpstg.getChildren(node))){
						
						RPSTNode childP = (RPSTNode) e;
						org.eclipse.bpel.model.Activity act1 = BpmnProctree2BpelModelPart(childP);
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

}
