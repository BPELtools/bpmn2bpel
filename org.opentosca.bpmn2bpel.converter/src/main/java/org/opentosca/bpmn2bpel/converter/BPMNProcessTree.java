package org.opentosca.bpmn2bpel.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.bpel.model.Link;
import org.eclipse.bpel.model.impl.BPELFactoryImpl;
import org.eclipse.bpmn2.BoundaryEvent;
import org.eclipse.bpmn2.CompensateEventDefinition;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.GatewayDirection;
import org.eclipse.bpmn2.Import;
import org.eclipse.bpmn2.InclusiveGateway;
import org.eclipse.bpmn2.MessageEventDefinition;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.SubProcess;
import org.eclipse.bpmn2.Task;
import org.eclipse.bpmn2.impl.Bpmn2FactoryImpl;
import org.eclipse.bpmn2.util.Bpmn2Resource;
import org.eclipse.emf.ecore.EObject;
import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.DirectedGraph;
import org.jbpt.graph.abs.AbstractDirectedEdge;
import org.jbpt.graph.abs.IDirectedGraph;
import org.jbpt.graph.algo.rpst.RPST;
import org.jbpt.graph.algo.rpst.RPSTNode;
import org.jbpt.graph.algo.tctree.TCType;
import org.jbpt.hypergraph.abs.Vertex;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

@SuppressWarnings("rawtypes")
public class BPMNProcessTree extends DirectedGraph {
	
	private static final XLogger logger = XLoggerFactory.getXLogger(BPMNProcessTree.class);
	
	// private Bpmn2Resource BPMN2Resource;
	private RPST rpstg;
	private List<Import> BpmnImports;
	private String name;
	private Map<String, Set<Link>> boundaryLinks;
	private boolean interruptsflow = false;
	
	final static BPELFactoryImpl mainfact = new BPELFactoryImpl();
	
	/**
	 * Creates an instance of the tree based on the given resource
	 * 
	 * @param res the EMF BPMN representation to represent in the tree
	 */
	public BPMNProcessTree(Bpmn2Resource res) {
		super();
		// this.BPMN2Resource = res;
		DocumentRoot root = (DocumentRoot) res.getContents().get(0);
		
		for (EObject content : root.eContents()) {
			if (content instanceof Definitions) {
				Definitions def = (Definitions) content;
				BPMNProcessTree.logger.debug("id: " + def.getId());
				BPMNProcessTree.logger.debug("class: " + def.getClass());
				
				this.fillTree(content);
				this.searchBoundaryEvents(content, this, null);
				BPMNProcessTree.logger.debug("BPMNProcessTree", this.toString());
				this.AssignImports(def.getImports());
			} else {
				BPMNProcessTree.logger.debug("unhandled class: " + content.getClass());
			}
		}
	}
	
	public BPMNProcessTree() {
		super();
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public void setName(String procname) {
		this.name = procname;
	}
	
	public void setInterrupting() {
		this.interruptsflow = true;
	}
	
	public boolean isInterrupting() {
		return this.interruptsflow;
	}
	
	// NOTE: Here it could be factorized the function by returning the vertex or
	// null instead of iterating twice
	// the VertexSet
	@SuppressWarnings("unchecked")
	public WFNode containsVertex(String id) {
		
		Collection<Vertex> vertices = this.getVertices();
		// Set<WFNode> vSet = (Set<WFNode>) vertexSet();
		WFNode node = null;
		
		WFNode nodei;
		
		for (Iterator<Vertex> p = vertices.iterator(); p.hasNext();) {
			
			nodei = (WFNode) p.next();
			
			if (nodei.getId().equals(id)) {
				
				node = nodei;
				break;
			}
			
		}
		
		return node;
	}
	
	public WFNode getRoot() {
		
		Collection<Vertex> vertices = this.getVertices();
		WFNode node = null;
		WFNode nodei;
		
		for (Iterator<Vertex> p = vertices.iterator(); p.hasNext();) {
			
			nodei = (WFNode) p.next();
			
			if (nodei.getElement().getIncoming().size() == 0) {
				
				node = nodei;
				break;
			}
		}
		
		return node;
	}
	
	public void setRPST(RPST rpstGraph) {
		
		this.rpstg = rpstGraph;
	}
	
	public void fillTree(EObject eobj) {
		
		for (EObject son : eobj.eContents()) {
			
			WFNode n1 = null;
			
			if (son != null) {
				
				// Check if element is a Flow WFNode of the bpmn2 File
				if (son instanceof FlowNode && !(son instanceof BoundaryEvent) && son instanceof StartEvent) {
					
					// It's defined what type of element it is Event, Activity
					// or Gateway
					FlowNode nodo = (FlowNode) son;
					n1 = new WFNode(nodo);
					this.fillProcTree(n1);
					break;
					
				}
				// When a BoundaryEvent is found the creation of the main flow
				// BPMN Process Tree is finished
				else if (son instanceof BoundaryEvent) {
					break;
				} else if (son instanceof SubProcess) {
					if (((SubProcess) son).isTriggeredByEvent()) {
						break;
					}
				}
				BPMNProcessTree.logger.debug("clase: " + son.getClass());
				if (son instanceof Process) {
					this.setName(((Process) son).getName());
					this.fillTree(son);
				}
				
			}
			
		}
		
	}
	
	// Returns the graph corresponding to an exception flow either error,
	// compensation, signal.. etc
	private BPMNProcessTree generateBoundGraph(WFNode ExFlowNode, BPMNProcessTree excepFlowTree, boolean interrupting) {
		
		BPMNProcessTree subTree = new BPMNProcessTree();
		FlowNode ExFlowElem = ExFlowNode.getElement();
		
		if (excepFlowTree.containsVertex(ExFlowNode.getId()) == null) {
			
			// If the element found is a subprocess, it's intern graph should be
			// filled
			if (ExFlowElem instanceof SubProcess) {
				
				subTree.fillTree(ExFlowElem);
				ExFlowNode.setSubProcessTree(subTree);
				
			}
			if (excepFlowTree.addVertex(ExFlowNode) != null) {
				BPMNProcessTree.logger.debug("Vertex " + ExFlowNode.getName() + " newly added succesfully");
				ExFlowNode.setVisited();
			}
		} else {
			ExFlowNode = excepFlowTree.containsVertex(ExFlowNode.getId());
			BPMNProcessTree.logger.debug("Vertex " + ExFlowNode.getName() + " not added, already exists");
			ExFlowNode.setVisited();
		}
		
		// Get outgoing SequenceFlows if any
		for (SequenceFlow s : ExFlowNode.getElement().getOutgoing()) {
			
			String targetid = s.getTargetRef().getId();
			WFNode ntarget = excepFlowTree.containsVertex(targetid);
			WFNode ntargetmain = this.containsVertex(targetid);
			WFEdge edge1;
			
			// If the target node is part of the main flow
			if (ntargetmain != null) {
				
				if (interrupting) {
					
					if (ntargetmain.getElement() instanceof Task || ntargetmain.getElement() instanceof ExclusiveGateway || ntargetmain.getElement() instanceof EndEvent) {
						
						excepFlowTree.addVertex(ntargetmain);
						excepFlowTree.addEdge(s, ExFlowNode, ntargetmain);
						
					} else {
						
						// ERROR!
						
					}
					
				} else {
					
					if (ntargetmain.getElement() instanceof ParallelGateway || ntargetmain.getElement() instanceof Task) {
						
						excepFlowTree.addVertex(ntargetmain);
						excepFlowTree.addEdge(s, ExFlowNode, ntargetmain);
						excepFlowTree.setInterrupting();
						
					}
					
				}
				
			} else {
				// If the target node is part of the exception flow
				if (ntarget != null) {
					
					if (excepFlowTree.addEdge(s, ExFlowNode, ntarget) != null) {
						
						BPMNProcessTree.logger.debug("Edge " + ExFlowNode.getName() + "," + ntarget.getName() + " created succesfully1");
					}
					if (!ntarget.isVisited()) {
						excepFlowTree = this.generateBoundGraph(ntarget, excepFlowTree, interrupting);
					}
				} else {
					ntarget = new WFNode(s.getTargetRef());
					
					if (s.getTargetRef() instanceof SubProcess) {
						// subTree.FillTree(s.getTargetRef());
						// ntarget.setSubProcessTree(subTree);
						// TODO!!! CHECK!
					}
					
					if (excepFlowTree.addVertex(ntarget) != null) {
						BPMNProcessTree.logger.debug("Vertex " + ntarget.getName() + " added succesfully");
						if (excepFlowTree.addEdge(s, ExFlowNode, ntarget) != null) {
							BPMNProcessTree.logger.debug("Edge " + ExFlowNode.getName() + "," + ntarget.getName() + " created succesfully2");
							
						} else {
							BPMNProcessTree.logger.debug("Edge not created");
						}
						if (!ntarget.isVisited()) {
							excepFlowTree = this.generateBoundGraph(ntarget, excepFlowTree, interrupting);
						}
					}
				}
				
			}
			
		}
		
		return excepFlowTree;
		
	}
	
	private void fillProcTree(WFNode n1) {
		FlowNode son = n1.getElement();
		BPMNProcessTree.logger.debug("son: " + son.getName());
		
		BPMNProcessTree subTree = new BPMNProcessTree();
		// If the vertex isn't part of the Graph
		if (this.containsVertex(n1.getId()) == null) {
			
			// If the element found is a subprocess, it's intern graph should be
			// filled
			if (son instanceof SubProcess) {
				
				subTree.fillTree(son);
				n1.setSubProcessTree(subTree);
				
			}
			if (this.addVertex(n1) != null) {
				BPMNProcessTree.logger.debug("Vertex " + n1.getName() + " newly added succesfully");
				n1.setVisited();
				n1.setMainFlow();
			}
		} else {
			n1 = this.containsVertex(n1.getId());
			BPMNProcessTree.logger.debug("Vertex " + n1.getName() + " not added, already exists");
			n1.setVisited();
			n1.setMainFlow();
		}
		BPMNProcessTree.logger.debug("vertices: " + this.countVertices());
		
		BPMNProcessTree.logger.debug(Integer.toString(n1.getElement().getOutgoing().size()));
		
		// Get outgoing SequenceFlows if any
		for (SequenceFlow s : n1.getElement().getOutgoing()) {
			
			BPMNProcessTree.logger.debug("verticesd: " + this.countVertices());
			String targetid = s.getTargetRef().getId();
			WFNode ntarget = this.containsVertex(targetid);
			WFEdge edge1;
			
			if (ntarget != null) {
				// NOTE: The testing of correct creation of edge is missing!
				
				if (this.addEdge(s, n1, ntarget) != null) {
					BPMNProcessTree.logger.debug("Edge " + n1.getName() + "," + ntarget.getName() + " created succesfully1");
				} else {
					BPMNProcessTree.logger.debug("Edge not created");
				}
				// visit the child
				if (!ntarget.isVisited()) {
					this.fillProcTree(ntarget);
				}
			} else {
				ntarget = new WFNode(s.getTargetRef());
				
				// If the already existing vertex is a subprocess, the intern
				// graph is filled
				if (s.getTargetRef() instanceof SubProcess) {
					
					// The subtree is filled.
					subTree.fillTree(s.getTargetRef());
					subTree.transfrom2SESE();
					ntarget.setSubProcessTree(subTree);
					// The Handlers are searched and established as Boundaries.
					
				}
				if (this.addVertex(ntarget) != null) {
					BPMNProcessTree.logger.debug("Vertex " + ntarget.getName() + " added succesfully");
					BPMNProcessTree.logger.debug("vertices: " + this.countVertices());
					if (this.addEdge(s, n1, ntarget) != null) {
						BPMNProcessTree.logger.debug("verticess: " + this.countVertices());
						BPMNProcessTree.logger.debug("Edge " + n1.getName() + "," + ntarget.getName() + " created succesfully2");
						
					} else {
						BPMNProcessTree.logger.debug("Edge not created");
					}
					if (!ntarget.isVisited()) {
						this.fillProcTree(ntarget);
					}
				}
				
			}
			BPMNProcessTree.logger.debug("->  outgoing: " + s.getName());
			BPMNProcessTree.logger.debug("out target: " + s.getTargetRef().getName());
			
		}
		
	}
	
	/*
	 * public WFEdge addEdge(WFEdge edge){
	 *
	 * this.edges.keySet().add(edge);
	 *
	 * }
	 */
	
	/**
	 * Transforms the given workflow graph to a BPEL process
	 */
	public org.eclipse.bpel.model.Process BpmnProctree2BpelModel(RPSTNode rpstnode) {
		BPMNProcessTree.logger.entry("BpmnProctree2BpelModel");
		
		org.eclipse.bpel.model.Process mainProc = this.mainfact.createProcess();
		
		org.eclipse.bpel.model.Activity a1 = null;
		
		// For every child of root RPSTNode visit and unfold in order to analyze
		// and create BPEL Structures
		a1 = this.BpmnProctree2BpelModelPart(rpstnode, this.rpstg);
		
		mainProc.setActivity(a1);
		
		BPMNProcessTree.logger.exit("BpmnProctree2BpelModel");
		return mainProc;
	}
	
	
	public org.eclipse.bpel.model.Activity BpmnProctree2BpelModelPart(RPSTNode node, RPST rpstParent) {
		BPMNProcessTree.logger.entry("BpmnProctree2BpelModelPart");
		org.eclipse.bpel.model.Activity res;
		switch (node.getType()) {
		case T:
			res = HandleTrivialComponent.handleTrivialComponent(node, rpstParent);
			break;
		case B:
			res = HandleBondComponent.handleBondComponent(this, node, rpstParent);
			break;
		case P:
			res = HandlePolygonComponent.handlePolygonComponent(this, node, rpstParent);
			break;
		default:
			res = null;
			break;
		}
		BPMNProcessTree.logger.exit("BpmnProctree2BpelModelPart");
		return res;
	}
	
	public void AssignImports(List<Import> imports) {
		this.BpmnImports = imports;
	}
	
	public WFEdge addEdge(SequenceFlow sf, WFNode s, WFNode t) {
		if (s == null || t == null) {
			return null;
		}
		Collection<DirectedEdge> es = this.getEdgesWithSourceAndTarget(s, t);
		if (es.size() > 0) {
			Iterator<DirectedEdge> i = es.iterator();
			while (i.hasNext()) {
				WFEdge e = (WFEdge) i.next();
				if (e.getVertices().size() == 2) {
					return null;
				}
			}
		}
		
		WFEdge e = new WFEdge(this, sf, s, t);
		return e;
	}
	
	public void restructureQuasi(RPST rpstG, RPSTNode rpstnode, WFNode BondExit, WFNode BondEntry, boolean hasBondParent) {
		
		// The whole RPST is searched in order to find quasi components
		for (Object e : Utils.organizeRPST(rpstnode, (ArrayList<RPSTNode>) rpstG.getChildren(rpstnode))) {
			
			RPSTNode node = (RPSTNode) e;
			RPST rpstaux = null;
			IDirectedGraph newfrag = null;
			
			if (node.getType().equals(TCType.B)) {
				
				if (BondExit != null && BondEntry != null && hasBondParent == true) {
					
					// If the bond component has the same exit as its containing
					// Bond then restructure
					if (BondExit.equals(node.getExit())) {
						
						WFNode Newgateway = null;
						
						// Add new gateway and redirect edges.
						if (BondExit.getElement() instanceof ExclusiveGateway) {
							
							Bpmn2FactoryImpl bpmnFact = new Bpmn2FactoryImpl();
							ExclusiveGateway ex1 = bpmnFact.createExclusiveGateway();
							ex1.setName(BondExit.getName() + "'");
							Newgateway = new WFNode(ex1);
							
						} else if (BondExit.getElement() instanceof ParallelGateway) {
							
							Bpmn2FactoryImpl bpmnFact = new Bpmn2FactoryImpl();
							ParallelGateway parg = bpmnFact.createParallelGateway();
							parg.setName(BondExit.getName() + "'");
							Newgateway = new WFNode(parg);
						}
						
						// For all children of the childBond that end in the
						// common gateway redirect them to the new one
						newfrag = this.redirectEdges(node.getFragment(), Newgateway, BondExit, false);
						rpstaux = new RPST(newfrag);
						node = rpstaux.getRoot();
						
						for (Object child : rpstaux.getChildren(node)) {
							
							RPSTNode auxnode = (RPSTNode) child;
							if (auxnode.getType().equals(TCType.B)) {
								node = auxnode;
							}
							
						}
						
					}
					// If the bond component has the same entry as its
					// containing Bond then restructure
					else if (BondEntry.equals(node.getEntry())) {
						
						WFNode Newgateway = null;
						
						if (BondEntry.getElement() instanceof ParallelGateway) {
							
							Bpmn2FactoryImpl bpmnFact = new Bpmn2FactoryImpl();
							ParallelGateway parg = bpmnFact.createParallelGateway();
							parg.setName(BondEntry.getName() + "'");
							Newgateway = new WFNode(parg);
						}
						
						// For all children of the childBond that start in the
						// common gateway redirect them to the new one
						newfrag = this.redirectEdges(node.getFragment(), Newgateway, BondEntry, true);
						rpstaux = new RPST(newfrag);
						node = rpstaux.getRoot();
						
						for (Object child : rpstaux.getChildren(node)) {
							
							RPSTNode auxnode = (RPSTNode) child;
							if (auxnode.getType().equals(TCType.B)) {
								node = auxnode;
							}
							
						}
						
					}
					
				}
				WFNode bondExit = (WFNode) node.getExit();
				WFNode bondEntry = (WFNode) node.getEntry();
				hasBondParent = true;
				// If a Bond component is found, this is traversed in order to
				// find out if it forms a quasi component.
				if (rpstaux != null) {
					this.restructureQuasi(rpstaux, node, bondExit, bondEntry, hasBondParent);
				} else {
					this.restructureQuasi(rpstG, node, bondExit, bondEntry, hasBondParent);
				}
				
			} else if (node.getType().equals(TCType.P)) {
				this.restructureQuasi(rpstG, node, BondExit, BondEntry, hasBondParent);
			}
		}
		
	}
	
	public void TransformGenFlows(RPST rpstG, RPSTNode rpstnode) {
		
		// The whole RPST is searched in order to find quasi components
		for (Object e : Utils.organizeRPST(rpstnode, (ArrayList<RPSTNode>) rpstG.getChildren(rpstnode))) {
			
			RPSTNode node = (RPSTNode) e;
			RPST newRpstg;
			
			if (node.getType().equals(TCType.R)) {
				
				Collection<RPSTNode> RigidChildren = this.rpstg.getChildren(node);
				IDirectedGraph rigidfragm;
				rigidfragm = node.getFragment();
				WFNode childEntry = null;
				WFNode childExit = null;
				
				for (Object e2 : RigidChildren) {
					
					RPSTNode child = (RPSTNode) e2;
					childEntry = (WFNode) child.getEntry();
					childExit = (WFNode) child.getExit();
					
					if (child.getType().equals(TCType.T)) {
						
						if (childEntry.getElement() instanceof ParallelGateway && childExit.getElement() instanceof ParallelGateway) {
							
							ParallelGateway spar = (ParallelGateway) childEntry.getElement();
							ParallelGateway tpar = (ParallelGateway) childExit.getElement();
							
							if (spar.getGatewayDirection().equals(GatewayDirection.DIVERGING) && tpar.getGatewayDirection().equals(GatewayDirection.CONVERGING) || spar.getGatewayDirection().equals(GatewayDirection.DIVERGING) && tpar.getGatewayDirection().equals(GatewayDirection.MIXED) || spar.getGatewayDirection().equals(GatewayDirection.MIXED) && tpar.getGatewayDirection().equals(GatewayDirection.MIXED) || spar.getGatewayDirection().equals(GatewayDirection.MIXED) && tpar.getGatewayDirection().equals(GatewayDirection.CONVERGING)) {
								
								AbstractDirectedEdge edge = (AbstractDirectedEdge) rigidfragm.getEdge(child.getEntry(), child.getExit());
								DirectedEdge edgep = this.getEdge((WFNode) child.getEntry(), (WFNode) child.getExit());
								rigidfragm.removeEdge(edge);
								this.removeEdge(edgep);
								
							} else if (spar.getGatewayDirection().equals(GatewayDirection.DIVERGING) && tpar.getGatewayDirection().equals(GatewayDirection.DIVERGING)) {
								
								AbstractDirectedEdge edge = (AbstractDirectedEdge) rigidfragm.getEdge(child.getEntry(), child.getExit());
								DirectedEdge edgep = this.getEdge((WFNode) child.getEntry(), (WFNode) child.getExit());
								rigidfragm.removeEdge(edge);
								this.removeEdge(edgep);
								Collection<AbstractDirectedEdge> outEdgestpar = rigidfragm.getOutgoingEdges(child.getExit());
								Collection<DirectedEdge> outEdgesrparp = this.getOutgoingEdges((WFNode) child.getExit());
								
								// for every outgoing Edge of the target
								// parallel
								// Gateway tpar the Edge is deleted and a new
								// edge
								// is added from the source Gateway
								for (AbstractDirectedEdge dirE : outEdgestpar) {
									
									rigidfragm.addEdge(child.getEntry(), dirE.getTarget());
									this.addEdge((WFNode) child.getEntry(), (WFNode) dirE.getTarget());
									this.removeEdge(this.getEdge((WFNode) child.getExit(), (WFNode) dirE.getTarget()));
									rigidfragm.removeEdge(dirE);
									
								}
								
								rigidfragm.removeVertex(child.getExit());
								this.removeVertex((WFNode) child.getExit());
								
							}
							
						}
						
					}
				}
				for (Object e2 : RigidChildren) {
					
					RPSTNode child = (RPSTNode) e2;
					
					if (child.getType().equals(TCType.P)) {
						
						WFNode cEntry = (WFNode) child.getEntry();
						WFNode cExit = (WFNode) child.getExit();
						Collection<DirectedEdge> cEntryinc = rigidfragm.getIncomingEdges(cEntry);
						
						// For every gateway with more than one incoming edge
						if (cEntry.getElement() instanceof ParallelGateway && cExit.getElement() instanceof ParallelGateway && cEntryinc.size() > 1) {
							
							ArrayList<AbstractDirectedEdge> inEdgesPar = (ArrayList<AbstractDirectedEdge>) rigidfragm.getIncomingEdges(child.getEntry());
							ArrayList<AbstractDirectedEdge> outEdgesPar = (ArrayList<AbstractDirectedEdge>) rigidfragm.getOutgoingEdges(child.getEntry());
							
							// The first incoming edge of the gateway is deleted
							// and
							// redirected to the node: next(gateway)
							rigidfragm.addEdge(inEdgesPar.get(0).getSource(), outEdgesPar.get(0).getTarget());
							this.addEdge((WFNode) inEdgesPar.get(0).getSource(), (WFNode) outEdgesPar.get(0).getTarget());
							rigidfragm.removeEdge(inEdgesPar.get(0));
							this.removeEdge(this.getEdge((WFNode) inEdgesPar.get(0).getSource(), (WFNode) inEdgesPar.get(0).getTarget()));
							
							// for the rest, the edges are redirected to the
							// next nearest Gateway after the current one
							for (AbstractDirectedEdge inE : inEdgesPar) {
								
								if (inEdgesPar.indexOf(inE) != 0) {
									
									rigidfragm.addEdge(inE.getSource(), child.getExit());
									this.addEdge((WFNode) inE.getSource(), (WFNode) child.getExit());
									rigidfragm.removeEdge(inE);
									this.removeEdge(this.getEdge((WFNode) inE.getSource(), (WFNode) inE.getTarget()));
									
								}
								
							}
							
							rigidfragm.removeEdge(outEdgesPar.get(0));
							this.removeEdge(this.getEdge((WFNode) outEdgesPar.get(0).getSource(), (WFNode) outEdgesPar.get(0).getTarget()));
							rigidfragm.removeVertex(child.getEntry());
							this.removeVertex((WFNode) child.getEntry());
							
						}
						
					}
					
				}
				
				newRpstg = new RPST(rigidfragm);
				node = newRpstg.getRoot();
				this.TransformGenFlows(newRpstg, node);
				
			} else if (node.getType().equals(TCType.P) || node.getType().equals(TCType.B)) {
				this.TransformGenFlows(rpstG, node);
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
		if (isEntry) {
			
			WFNode bondEntry = bondGateway;
			this.addEdge(bondEntry, newgateway);
			
			// All the edges that come out of the old gateway are selected
			// in order to be deleted.
			for (Object e : fragment.getEdgesWithSource(bondEntry)) {
				
				AbstractDirectedEdge edge = (AbstractDirectedEdge) e;
				WFNode target = (WFNode) edge.getTarget();
				
				for (Object e2 : this.getEdgesWithSource(bondEntry)) {
					
					DirectedEdge edge2 = (DirectedEdge) e2;
					WFNode target2 = (WFNode) edge2.getTarget();
					if (target.equals(target2)) {
						// edge is removed from the main graph
						this.removeEdge(edge2);
						// edge is removed from the fragment of the rpst
						// component
						fragment.removeEdge(edge);
						// new edge is added to the main graph from the new
						// gateway to the branch target
						this.addEdge(newgateway, target);
						// same new edge is added to the fragment of the rpst
						// component
						fragment.addEdge(newgateway, target);
						break;
					}
					
				}
				
			}
			
			// finally a new edge is added (old gateway -> new gateway)
			fragment.addEdge(bondEntry, newgateway);
			
		}
		// If the bond Gateway received as parameter is the Exit node
		// then the Edges of the bond (child) are redirected to the new exit
		// gateway
		else {
			
			WFNode bondExit = bondGateway;
			this.addEdge(newgateway, bondExit);
			
			// All the edges that end in the old gateway are selected in order
			// to "redirect" them
			// to the new gateway
			for (Object e : fragment.getEdgesWithTarget(bondExit)) {
				
				AbstractDirectedEdge edge = (AbstractDirectedEdge) e;
				WFNode source = (WFNode) edge.getSource();
				
				for (Object e2 : this.getEdgesWithTarget(bondExit)) {
					
					DirectedEdge edge2 = (DirectedEdge) e2;
					WFNode source2 = (WFNode) edge2.getSource();
					if (source.equals(source2)) {
						// The Edge is removed from the main graph and the
						// fragment
						this.removeEdge(edge2);
						fragment.removeEdge(edge);
						// The new edge is added to the main graph (source
						// branch -> new gateway)
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
	
	/**
	 * Restructures a BPMN Process Tree and converts it to a Single Entry Single
	 * Exit Graph
	 * in order for it to be used in the RPST functions
	 */
	public void transfrom2SESE() {
		// TODO Auto-generated method stub
		
		Collection vertices = this.getVertices();
		Collection<WFNode> entries = new LinkedList<WFNode>();
		Collection<WFNode> exits = new LinkedList<WFNode>();
		Bpmn2FactoryImpl bpmnFact = new Bpmn2FactoryImpl();
		StartEvent startb = bpmnFact.createStartEvent();
		startb.setName(this.getName() + "start_delta");
		EndEvent endb = bpmnFact.createEndEvent();
		endb.setName(this.getName() + "end_delta");
		InclusiveGateway inclGate = bpmnFact.createInclusiveGateway();
		inclGate.setName(this.getName() + "incl_end");
		WFNode entryb = new WFNode(startb);
		WFNode exitb = new WFNode(endb);
		WFNode exitGw = new WFNode(inclGate);
		
		// Fill the collections entries and exits
		for (Object e : vertices) {
			
			WFNode node = (WFNode) e;
			// Fill the collections of nodes that act as entries
			if (this.getIncomingEdges(node).size() == 0) {
				
				entries.add(node);
				
			}
			// Fill the collections of nodes that act as exits
			else if (this.getOutgoingEdges(node).size() == 0) {
				
				exits.add(node);
				
			}
			
		}
		
		// the entry vertices are checked to find normal start events and
		// connect them to the new single entry
		if (entries.size() > 1) {
			this.addVertex(entryb);
			
			for (Object e : entries) {
				
				WFNode node = (WFNode) e;
				
				if (node.isBasicStartEvent()) {
					
					this.addEdge(entryb, node);
				}
				
			}
		}
		
		// The exit vertices are checked
		if (exits.size() > 1) {
			this.addVertex(exitb);
			this.addVertex(exitGw);
			
			for (Object e : exits) {
				
				WFNode node = (WFNode) e;
				
				if (node.getElement() instanceof EndEvent) {
					
					EndEvent end = (EndEvent) node.getElement();
					if (end.getEventDefinitions().size() == 0) {
						
						this.addEdge(node, exitGw);
						
					} else {
						if (end.getEventDefinitions().get(0) instanceof MessageEventDefinition) {
							
							Task msgTask = bpmnFact.createTask();
							msgTask.setName(end.getName());
							WFNode MsgTaskNode = new WFNode(msgTask);
							this.addVertex(MsgTaskNode);
							this.addEdge(MsgTaskNode, exitGw);
							
						}
					}
					
				}
				
			}
			this.addEdge(exitGw, exitb);
		}
	}
	
	
	void searchBoundaryEvents(EObject eobj, BPMNProcessTree parent, BPMNProcessTree gparent) {
		// TODO Auto-generated method stub
		
		WFNode subprocGrandP = null;
		WFNode startHandler = null;
		
		for (EObject son : eobj.eContents()) {
			
			if (son != null) {
				
				BPMNProcessTree ExcepFlowTree = new BPMNProcessTree();
				boolean interrupting = false;
				
				if (son instanceof BoundaryEvent) {
					
					BoundaryEvent boundEvent = (BoundaryEvent) son;
					String taskRef = boundEvent.getAttachedToRef().getId();
					WFNode task1 = this.containsVertex(taskRef);
					WFNode boundEventRoot = new WFNode(boundEvent);
					interrupting = boundEvent.isCancelActivity();
					
					// It's checked if the BoundaryEvent is of compensation type
					// or not
					// compensation types have associations instead of normal
					// sequence flows.
					if (boundEvent.getEventDefinitions().get(0) instanceof CompensateEventDefinition) {
						
						BPMNProcessTree.logger.debug("associations: " + boundEvent.getOutgoing());
						
					} else {
						
						ExcepFlowTree = this.generateBoundGraph(boundEventRoot, ExcepFlowTree, interrupting);
						task1.addBoundaryEvent(ExcepFlowTree);
						
					}
					
				} else if (son instanceof StartEvent) {
					
					// If the start event found has an event definition then
					// this one is
					// added to the Boundary event flows list because they have
					// the same behaviour.
					StartEvent StartAux = (StartEvent) son;
					
					if (StartAux.getEventDefinitions().size() != 0) {
						
						StartEvent startEv = (StartEvent) son;
						// If its grandparent is a Subprocess and also its
						// parent
						if (son.eContainer().eContainer() instanceof SubProcess && son.eContainer() instanceof SubProcess) {
							
							interrupting = startEv.isIsInterrupting();
							SubProcess psubp = (SubProcess) son.eContainer().eContainer();
							
							startHandler = new WFNode(startEv);
							
							if (gparent != null) {
								subprocGrandP = gparent.containsVertex(psubp.getId());
							}
							
							if (startHandler != null && subprocGrandP != null) {
								
								ExcepFlowTree = this.generateBoundGraph(startHandler, ExcepFlowTree, interrupting);
								subprocGrandP.addBoundaryEvent(ExcepFlowTree);
								
							}
							
						}
						
					}
					
				} else if (son instanceof SubProcess) {
					
					BPMNProcessTree subpTree = null;
					WFNode subpnode = this.containsVertex(((SubProcess) son).getId());
					if (subpnode != null) {
						subpTree = subpnode.getSubprocessTree();
					}
					
					if (((SubProcess) son).isTriggeredByEvent()) {
						
						this.searchBoundaryEvents(son, this, gparent);
						
					} else {
						
						subpTree.searchBoundaryEvents(son, subpTree, this); // segunda
						// funcion
					}
					
				} else if (son instanceof Process) {
					this.setName(((Process) son).getName());
					this.searchBoundaryEvents(son, parent, gparent);
				}
				
			}
			
		}
		
	}
}
