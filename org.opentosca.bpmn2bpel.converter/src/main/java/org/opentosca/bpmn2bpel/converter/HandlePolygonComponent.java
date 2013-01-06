package org.opentosca.bpmn2bpel.converter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.Catch;
import org.eclipse.bpel.model.Empty;
import org.eclipse.bpel.model.Flow;
import org.eclipse.bpel.model.Link;
import org.eclipse.bpel.model.Links;
import org.eclipse.bpel.model.Scope;
import org.eclipse.bpel.model.Sequence;
import org.eclipse.bpel.model.Source;
import org.eclipse.bpel.model.Sources;
import org.eclipse.bpel.model.Target;
import org.eclipse.bpel.model.Targets;
import org.eclipse.bpel.model.impl.SequenceImpl;
import org.jbpt.graph.algo.rpst.RPST;
import org.jbpt.graph.algo.rpst.RPSTNode;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;


// because of the RPST
@SuppressWarnings("rawtypes")
public class HandlePolygonComponent {
	
	private static final XLogger logger = XLoggerFactory.getXLogger(HandlePolygonComponent.class);
	
	
	public static Activity handlePolygonComponent(BPMNProcessTree tree, RPSTNode node, RPST rpstParent) {
		HandlePolygonComponent.logger.entry("handlePolygonComponent", node);
		
		// implementation assumption that getChidren() returns an ArrayList
		@SuppressWarnings("unchecked")
		ArrayList<RPSTNode> origChildren = (ArrayList<RPSTNode>) rpstParent.getChildren(node);
		
		ArrayList<RPSTNode> childrenP = Utils.organizeRPST(node, origChildren);
		int childrenPsize = childrenP.size();
		SequenceImpl seq1 = (SequenceImpl) BPMNProcessTree.mainfact.createSequence();
		Flow flow1 = null;
		Flow flow2 = null;
		// Hashtable with the activity where a boundary event ends and the
		// corresponding link to be added to it.
		Map<String, Set<Link>> boundaryLinks = new Hashtable<String, Set<Link>>();
		Links flowLinks = BPMNProcessTree.mainfact.createLinks();
		Scope act1scope = null;
		org.eclipse.bpel.model.Activity act1 = null;
		org.eclipse.bpel.model.Activity lastAct = null;
		WFNode ChildPentry = null;
		Link nextActlink = null;
		RPSTNode lastChild = null;
		String actId;
		
		if (childrenPsize == 1) {
			HandlePolygonComponent.logger.debug("One child");
			// There's only one component as a son and it's the Trivial (split
			// -> join) SequenceFlow
			return BPMNProcessTree.mainfact.createEmpty();
			
		} else {
			HandlePolygonComponent.logger.debug("Multiple children");
			for (RPSTNode childP : childrenP) {
				// If the obtained child of the polygon is the last element
				// of the sequence a description is added
				if (childrenP.indexOf(childP) == childrenP.size() - 1) {
					lastChild = childP;
				}
				
				act1 = tree.BpmnProctree2BpelModelPart(childP, rpstParent);
				ChildPentry = (WFNode) childP.getEntry();
				if (act1 != null) {
					actId = ChildPentry.getElement().getId();
				} else {
					actId = "";
				}
				
				if (act1 != null && childrenPsize == 2) {
					
					return act1;
					
				} else if (act1 != null && childrenPsize > 2) {
					
					if (act1 instanceof Scope) {
						
						act1scope = (Scope) act1;
						
						// If the previous activity had a source link, this
						// one is added as the target link of the current
						// activity
						if (nextActlink != null) {
							
							Targets scopeTs = BPMNProcessTree.mainfact.createTargets();
							Target scopeT = BPMNProcessTree.mainfact.createTarget();
							scopeT.setLink(nextActlink);
							scopeTs.getChildren().add(scopeT);
							act1scope.setTargets(scopeTs);
							
						}
						// Adds the targets that the activity in the
						// bpel-flow needs.
						if (act1scope.getTargets() != null) {
							
							Set<Link> linksact = boundaryLinks.get(actId);
							
							if (linksact != null) {
								
								for (Iterator<Link> p = linksact.iterator(); p.hasNext();) {
									
									Link l = p.next();
									Target t = BPMNProcessTree.mainfact.createTarget();
									t.setLink(l);
									act1scope.getTargets().getChildren().add(t);
									
								}
								
								boundaryLinks.remove(actId);
								
							}
							
						} else {
							
							Targets scopeTs = BPMNProcessTree.mainfact.createTargets();
							
							Set<Link> linksact = boundaryLinks.get(actId);
							
							if (linksact != null) {
								
								for (Iterator<Link> p = linksact.iterator(); p.hasNext();) {
									
									Link l = p.next();
									Target t = BPMNProcessTree.mainfact.createTarget();
									t.setLink(l);
									scopeTs.getChildren().add(t);
									
								}
								
								act1scope.setTargets(scopeTs);
								boundaryLinks.remove(actId);
								
							}
							
						}
						
						// If the Scope of the Activity does have
						// FaultHandlers
						if (act1scope.getFaultHandlers() != null) {
							// for every catch present in the fault handlers
							// it's proved if it ends in the mainflow or
							// not.
							for (Catch a : act1scope.getFaultHandlers().getCatch()) {
								
								// An interrupting Boundary event is found,
								// therefore a bpel-Flow structure should be
								// created
								if (flow1 == null) {
									flow1 = BPMNProcessTree.mainfact.createFlow();
								}
								
								// If it ends in an activity, the id of the
								// activity is added to the Hashtable that
								// contains
								if (!a.getDocumentation().getValue().equals("N")) {
									
									// p[0] contains the id of the activity
									// where the Boundary Event ends
									// p[1] contains the name of the
									// sequence flow corresponding to the
									// created link
									String p[] = a.getDocumentation().getValue().split("/");
									
									// The link corresponding to the end of
									// the Handler's activity is created and
									// added.
									Empty emptyL = BPMNProcessTree.mainfact.createEmpty();
									Sources sourcesL = BPMNProcessTree.mainfact.createSources();
									Source sourceL = BPMNProcessTree.mainfact.createSource();
									Link afterBoundExec = BPMNProcessTree.mainfact.createLink();
									afterBoundExec.setName(p[1]);
									
									// The Link corresponding to the correct
									// execution of the activity (without
									// influence of the Boundary) is created
									Sources sourcesScope = BPMNProcessTree.mainfact.createSources();
									Source sScope = BPMNProcessTree.mainfact.createSource();
									Link linkScope = BPMNProcessTree.mainfact.createLink();
									linkScope.setName(ChildPentry.getElement().getOutgoing().get(0).getName());
									sScope.setLink(linkScope);
									sourcesScope.getChildren().add(sScope);
									
									sourceL.setLink(afterBoundExec);
									sourcesL.getChildren().add(sourceL);
									emptyL.setSources(sourcesL);
									
									// The empty activity containing the
									// link is added at the end of the catch
									// activity.
									// by creating a new sequence that
									// contains both.
									Sequence catchSeq = BPMNProcessTree.mainfact.createSequence();
									catchSeq.getActivities().add(a.getActivity());
									catchSeq.getActivities().add(emptyL);
									
									a.setActivity(catchSeq);
									
									// The Hash with the containing links of
									// the activities that act as a
									// boundaryevent end is filled.
									String endActivId = p[0];
									if (!boundaryLinks.containsKey(endActivId)) {
										boundaryLinks.put(endActivId, new HashSet<Link>());
									}
									
									boundaryLinks.get(endActivId).add(afterBoundExec);
									flowLinks.getChildren().add(afterBoundExec);
									flowLinks.getChildren().add(linkScope);
									flow1.setLinks(flowLinks);
									act1scope.setSources(sourcesScope);
									flow1.getActivities().add(act1scope);
									
									nextActlink = linkScope;
									a.unsetDocumentation();
									
								} else {
									// NOTE with this case. With the
									// Boundary Event activities that have
									// no influence on the main flow, no
									// link at the end
									// is added (the behavior is assumed.)
									// The Link corresponding to the correct
									// execution of the activity (without
									// influence of the Boundary) is created
									Sources sourcesScope = BPMNProcessTree.mainfact.createSources();
									Source sScope = BPMNProcessTree.mainfact.createSource();
									Link linkScope = BPMNProcessTree.mainfact.createLink();
									linkScope.setName(ChildPentry.getElement().getOutgoing().get(0).getName());
									sScope.setLink(linkScope);
									sourcesScope.getChildren().add(sScope);
									
									flowLinks.getChildren().add(linkScope);
									flow1.setLinks(flowLinks);
									act1scope.setSources(sourcesScope);
									flow1.getActivities().add(act1scope);
									
									nextActlink = linkScope;
									a.unsetDocumentation();
									
								}
								
							}
						}
						
					}
					// If the current activity is not a scope
					else {
						
						// If an interrupting event was found across the
						// Polygon
						if (flow1 != null) {
							// if the previous activity had a source link,
							// this one is added as target link of the
							// current activity
							if (nextActlink != null) {
								
								Targets scopeTs = BPMNProcessTree.mainfact.createTargets();
								Target scopeT = BPMNProcessTree.mainfact.createTarget();
								scopeT.setLink(nextActlink);
								scopeTs.getChildren().add(scopeT);
								act1.setTargets(scopeTs);
								
								// The link is set to null to avoid assign
								// in the next cycle
								nextActlink = null;
								
							}
							// Adds the targets that the activity in the
							// bpel-flow needs.
							if (act1.getTargets() != null) {
								
								Set<Link> linksact = boundaryLinks.get(actId);
								
								if (linksact != null) {
									
									for (Iterator<Link> p = linksact.iterator(); p.hasNext();) {
										
										Link l = p.next();
										Target t = BPMNProcessTree.mainfact.createTarget();
										t.setLink(l);
										act1.getTargets().getChildren().add(t);
										
									}
									
									boundaryLinks.remove(actId);
									
								}
								
							} else {
								
								Targets scopeTs = BPMNProcessTree.mainfact.createTargets();
								
								Set<Link> linksact = boundaryLinks.get(actId);
								
								if (linksact != null) {
									
									for (Iterator<Link> p = linksact.iterator(); p.hasNext();) {
										
										Link l = p.next();
										Target t = BPMNProcessTree.mainfact.createTarget();
										t.setLink(l);
										scopeTs.getChildren().add(t);
										
									}
									
									act1.setTargets(scopeTs);
									boundaryLinks.remove(actId);
									
								}
								
							}
							flow1.getActivities().add(act1);
							
						} else {
							
							// If the activity returned is a flow that has
							// the Documentation "FlowtillEnd" we have a
							// parallel execution till the end of the
							// Process
							if (flow2 != null && flow2.getDocumentation().getValue().equals("FlowtillEnd")) {
								flow2.getActivities().add(act1);
							} else {
								seq1.getActivities().add(act1);
							}
							
						}
						
					}
					
					Set<Link> linksact = null;
					if (actId != null) {
						linksact = boundaryLinks.get(actId);
					}
					
					if (linksact != null) {
						
						for (Iterator<Link> p = linksact.iterator(); p.hasNext();) {
							
							Link l = p.next();
							
						}
						
					}
					
				}
				
			}
			lastChild.setDescription("Last-Trivial");
			lastAct = tree.BpmnProctree2BpelModelPart(lastChild, rpstParent);
			// If the flow corresponding to the sequence of activities with
			// interrupting boundary events is not null
			if (flow1 != null) {
				
				if (flow2 != null) {
					
				} else {
					seq1.getActivities().add(flow1);
				}
				
			}
			// Otherwise the activity or the scope is added to the normal
			// sequence
			else {
				if (act1scope != null) {
					seq1.getActivities().add(act1scope);
				} else if (act1 != null) {
					seq1.getActivities().add(act1);
				}
			}
			if (lastAct != null) {
				seq1.getActivities().add(lastAct);
			}
			return seq1;
		}
	}
	
}
