package org.opentosca.bpmn2bpel.converter;

import java.util.Collection;

import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.Condition;
import org.eclipse.bpel.model.Documentation;
import org.eclipse.bpel.model.Else;
import org.eclipse.bpel.model.ElseIf;
import org.eclipse.bpel.model.Flow;
import org.eclipse.bpel.model.If;
import org.eclipse.bpel.model.RepeatUntil;
import org.eclipse.bpel.model.While;
import org.eclipse.bpel.model.impl.ConditionImpl;
import org.eclipse.bpel.model.impl.ElseIfImpl;
import org.eclipse.bpel.model.impl.ElseImpl;
import org.eclipse.bpel.model.impl.FlowImpl;
import org.eclipse.bpel.model.impl.IfImpl;
import org.eclipse.bpel.model.impl.LinkImpl;
import org.eclipse.bpel.model.impl.LinksImpl;
import org.eclipse.bpel.model.impl.OnAlarmImpl;
import org.eclipse.bpel.model.impl.OnMessageImpl;
import org.eclipse.bpel.model.impl.PickImpl;
import org.eclipse.bpel.model.impl.SequenceImpl;
import org.eclipse.bpel.model.impl.SourceImpl;
import org.eclipse.bpel.model.impl.SourcesImpl;
import org.eclipse.bpel.model.impl.TargetImpl;
import org.eclipse.bpel.model.impl.TargetsImpl;
import org.eclipse.bpel.model.impl.WhileImpl;
import org.eclipse.bpmn2.EventBasedGateway;
import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.Expression;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.InclusiveGateway;
import org.eclipse.bpmn2.IntermediateCatchEvent;
import org.eclipse.bpmn2.MessageEventDefinition;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.ReceiveTask;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.SignalEventDefinition;
import org.eclipse.bpmn2.Task;
import org.eclipse.bpmn2.TimerEventDefinition;
import org.eclipse.bpmn2.impl.FormalExpressionImpl;
import org.eclipse.bpmn2.impl.IntermediateCatchEventImpl;
import org.eclipse.bpmn2.impl.MessageEventDefinitionImpl;
import org.eclipse.bpmn2.impl.SignalEventDefinitionImpl;
import org.eclipse.bpmn2.impl.TimerEventDefinitionImpl;
import org.jbpt.graph.abs.AbstractDirectedGraph;
import org.jbpt.graph.algo.rpst.RPST;
import org.jbpt.graph.algo.rpst.RPSTNode;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import de.uni_stuttgart.iaas.bpmn.model.utilities.BPMNutils;


public class HandleBondComponent {
	
	private static final XLogger logger = XLoggerFactory.getXLogger(HandleBondComponent.class);
	
	private static Activity handleXORstructure(BPMNProcessTree tree, Collection<RPSTNode> bondChildren, WFNode entry, RPST rpstParent) {
		HandleBondComponent.logger.entry();
		If ifActivity = null;
		Else elseBranch = null;
		
		// helper variable to transform a single else without any condition
		// to an elseBranch, if there is no default sequence flow at the gateway
		ElseIf elseIfWithoutCondition = null;
		boolean moreThanOneElseIfWithoutCondition = false;
		
		// For every Polygon child of the Bond element
		for (RPSTNode child : bondChildren) {
			SequenceFlow conditionFlow = entry.getSourceOfPolygon((AbstractDirectedGraph) child.getFragment());
			
			if (BPMNutils.isDefault(conditionFlow)) {
				// the elseBranch is not directly attached to the ifActivtiy as the default flow might have been hit as first and
				// the if activity is not already created in this case
				elseBranch = BPMNProcessTree.getBPELFactory().createElse();
				org.eclipse.bpel.model.Activity activity = tree.BpmnProctree2BpelModelPart(child, rpstParent);
				elseBranch.setActivity(activity);
			} else if (ifActivity == null) {
				// first branch in the if activity
				ifActivity = BPMNProcessTree.getBPELFactory().createIf();
				ifActivity.setName(entry.getName());
				org.eclipse.bpel.model.Activity activity = tree.BpmnProctree2BpelModelPart(child, rpstParent);
				ifActivity.setActivity(activity);
				
				Condition condition = Utils.convertExpressionToCondition(conditionFlow.getConditionExpression());
				ifActivity.setCondition(condition);
			} else {
				// else branch in the if activity
				ElseIf elseIf = BPMNProcessTree.getBPELFactory().createElseIf();
				org.eclipse.bpel.model.Activity activity = tree.BpmnProctree2BpelModelPart(child, rpstParent);
				elseIf.setActivity(activity);
				
				Condition condition = Utils.convertExpressionToCondition(conditionFlow.getConditionExpression());
				if (condition == null) {
					if (elseIfWithoutCondition == null) {
						elseIfWithoutCondition = elseIf;
					} else {
						moreThanOneElseIfWithoutCondition = true;
						ifActivity.getElseIf().add(elseIf);
					}
				} else {
					elseIf.setCondition(condition);
					ifActivity.getElseIf().add(elseIf);
				}
			}
		}
		if (elseBranch == null) {
			if (elseIfWithoutCondition == null) {
				HandleBondComponent.logger.info("No default flow used at gateway. It is possible that the execution differs because of the implicit elseif in BPEL");
			} else {
				elseBranch = BPMNProcessTree.getBPELFactory().createElse();
				elseBranch.setActivity(elseIfWithoutCondition.getActivity());
				ifActivity.setElse(elseBranch);
				if (moreThanOneElseIfWithoutCondition) {
					// the other else ifs without condition already have been added to the if
					HandleBondComponent.logger.info("More than one outgoing sequence flow without condition");
				}
			}
		} else {
			// elseBranch exists
			if (ifActivity == null) {
				assert elseIfWithoutCondition == null;
				HandleBondComponent.logger.debug("Special case: gateway with only outgoing sequence flow being the default flow");
				// (invalid BPMN?)
				ifActivity = BPMNProcessTree.getBPELFactory().createIf();
			} else if (elseIfWithoutCondition != null) {
				HandleBondComponent.logger.info("An else branch and a sequence flow without condition exists.");
				ifActivity.getElseIf().add(elseIfWithoutCondition);
			}
			ifActivity.setElse(elseBranch);
		}
		HandleBondComponent.logger.exit();
		return ifActivity;
	}
	
	private static Activity handleANDstructure(BPMNProcessTree tree, Collection<RPSTNode> bondChildren, RPST rpstParent) {
		HandleBondComponent.logger.entry();
		FlowImpl flow1 = (FlowImpl) BPMNProcessTree.getBPELFactory().createFlow();
		org.eclipse.bpel.model.Activity actflow;
		
		// For every Polygon child of the Bond element
		for (Object e : bondChildren) {
			
			RPSTNode child = (RPSTNode) e;
			
			actflow = tree.BpmnProctree2BpelModelPart(child, rpstParent);
			flow1.getActivities().add(actflow);
		}
		HandleBondComponent.logger.exit();
		return flow1;
	}
	
	private static Activity handleInclusiveORstructure(BPMNProcessTree tree, Collection<RPSTNode> bondChildren, WFNode entry, WFNode exit, RPST rpstParent) {
		HandleBondComponent.logger.entry();
		FlowImpl flowp = (FlowImpl) BPMNProcessTree.getBPELFactory().createFlow();
		FlowImpl flowc = (FlowImpl) BPMNProcessTree.getBPELFactory().createFlow();
		org.eclipse.bpel.model.Activity actflow;
		LinksImpl links = (LinksImpl) BPMNProcessTree.getBPELFactory().createLinks();
		LinkImpl link1 = (LinkImpl) BPMNProcessTree.getBPELFactory().createLink();
		LinkImpl link2 = (LinkImpl) BPMNProcessTree.getBPELFactory().createLink();
		SourcesImpl sources = (SourcesImpl) BPMNProcessTree.getBPELFactory().createSources();
		SourcesImpl sourcesc = (SourcesImpl) BPMNProcessTree.getBPELFactory().createSources();
		SourceImpl source1 = (SourceImpl) BPMNProcessTree.getBPELFactory().createSource();
		TargetsImpl targets = (TargetsImpl) BPMNProcessTree.getBPELFactory().createTargets();
		TargetsImpl targetsc = (TargetsImpl) BPMNProcessTree.getBPELFactory().createTargets();
		TargetImpl target1 = (TargetImpl) BPMNProcessTree.getBPELFactory().createTarget();
		SequenceFlow sourceSeqF, targetSeqF = null;
		FormalExpressionImpl expr1 = null;
		
		// For every Outgoing link of the gateway
		for (Object e : bondChildren) {
			
			RPSTNode child = (RPSTNode) e;
			sourceSeqF = entry.getSourceOfPolygon((AbstractDirectedGraph) child.getFragment());
			targetSeqF = exit.getTargetOfPolygon((AbstractDirectedGraph) child.getFragment());
			ConditionImpl cond1 = (ConditionImpl) BPMNProcessTree.getBPELFactory().createCondition();
			expr1 = (FormalExpressionImpl) sourceSeqF.getConditionExpression();
			
			// Obtain the child-Bpel-Activity
			actflow = tree.BpmnProctree2BpelModelPart(child, rpstParent);
			
			// Add the activity to the flow child
			flowc.getActivities().add(actflow);
			
			// The Body of the condition is filled with the id of
			// the corresponding BPMN condition (NOTE)
			cond1.setBody(expr1.getMixed().getValue(0));
			
			// Create the corresponding source-SequenceFlow link
			link1.setName(sourceSeqF.getName());
			
			// Add the created link to the links list
			links.getChildren().add(link1);
			
			// Create a Bpel-Source element and associate the
			// already existing link to it
			source1.setLink(link1);
			
			if (expr1 != null) {
				source1.setTransitionCondition(cond1);
			}
			
			// Add the source to the sources list
			sources.getChildren().add(source1);
			
			sourcesc.getChildren().add(source1);
			flowc.setSources(sourcesc);
			
			if (!targetSeqF.equals(sourceSeqF)) {
				// Create the corresponding target-SequenceFlow link
				link2.setName(targetSeqF.getName());
				
				// Create a Bpel-Target element and associate the
				// already existing link to it
				target1.setLink(link2);
				
				// Add the target to the targets list
				targets.getChildren().add(target1);
				
				// Add targets of the flow-child element
				targetsc.getChildren().add(target1);
				flowc.setTargets(targetsc);
				
			}
			
			flowp.setLinks(links);
			flowp.setSources(sources);
			flowp.setTargets(targets);
			flowp.getActivities().add(flowc);
			
		}
		HandleBondComponent.logger.exit();
		return flowp;
	}
	
	
	private static Activity handlePickStructure(BPMNProcessTree tree, Collection<RPSTNode> bondChildren, WFNode entry, WFNode exit, RPST rpstParent) {
		HandleBondComponent.logger.entry();
		PickImpl pick1 = (PickImpl) BPMNProcessTree.getBPELFactory().createPick();
		
		for (Object e : bondChildren) {
			
			RPSTNode child = (RPSTNode) e;
			SequenceFlow gateOut = entry.getSourceOfPolygon((AbstractDirectedGraph) child.getFragment());
			org.eclipse.bpel.model.Activity a1;
			
			if (gateOut.getTargetRef() instanceof IntermediateCatchEvent) {
				
				IntermediateCatchEventImpl catchEvent = (IntermediateCatchEventImpl) gateOut.getTargetRef();
				
				// It's assumed that the Event definitions of an
				// Intermediate Catch event is just 1
				if (catchEvent.getEventDefinitions().get(0) instanceof MessageEventDefinition) {
					
					MessageEventDefinitionImpl msgEvent = (MessageEventDefinitionImpl) catchEvent.getEventDefinitions().get(0);
					
					OnMessageImpl onMsg1 = (OnMessageImpl) BPMNProcessTree.getBPELFactory().createOnMessage();
					
					// Momentarily the OnMessage element's operation
					// is given the Event's name + "operation"
					// (NOTE)
					onMsg1.setOperationName(msgEvent.getOperationRef().getName());
					
					// Obtain the child-Bpel-Activity
					a1 = tree.BpmnProctree2BpelModelPart(child, rpstParent);
					
					// Add the activity to the flow child
					onMsg1.setActivity(a1);
					
					pick1.getMessages().add(onMsg1);
					
				} else if (catchEvent.getEventDefinitions().get(0) instanceof TimerEventDefinition) {
					
					TimerEventDefinitionImpl timeEvent = (TimerEventDefinitionImpl) catchEvent.getEventDefinitions().get(0);
					
					OnAlarmImpl onalrm1 = (OnAlarmImpl) BPMNProcessTree.getBPELFactory().createOnAlarm();
					
					Expression exprTime = timeEvent.getTimeDuration();
					org.eclipse.bpel.model.Expression exprOnAlarm = BPMNProcessTree.getBPELFactory().createExpression();
					
					if (timeEvent.getTimeDuration() != null) {
						
						// The Body of the Bpel expression is filled
						// with the Id of the bpmn expression (NOTE)
						exprOnAlarm.setBody(exprTime.getId());
						onalrm1.setFor(exprOnAlarm);
						
					} else if (timeEvent.getTimeDate() != null) {
						
						// The Body of the Bpel expression is filled
						// with the Id of the bpmn expression (NOTE)
						exprOnAlarm.setBody(exprTime.getId());
						onalrm1.setUntil(exprOnAlarm);
						
					}
					
					// Obtain the child-Bpel-Activity
					a1 = tree.BpmnProctree2BpelModelPart(child, rpstParent);
					
					// Add the activity to the flow child
					onalrm1.setActivity(a1);
					
					pick1.getAlarm().add(onalrm1);
					
				} else if (catchEvent.getEventDefinitions().get(0) instanceof SignalEventDefinition) {
					
					SignalEventDefinitionImpl sigEvent = (SignalEventDefinitionImpl) catchEvent.getEventDefinitions().get(0);
					
					OnMessageImpl onMsg1 = (OnMessageImpl) BPMNProcessTree.getBPELFactory().createOnMessage();
					
					onMsg1.setOperationName(sigEvent.getSignalRef().getName());
					
					a1 = tree.BpmnProctree2BpelModelPart(child, rpstParent);
					
					onMsg1.setActivity(a1);
					
					pick1.getMessages().add(onMsg1);
				}
				
			} else if (gateOut.getTargetRef() instanceof ReceiveTask) {
				
				ReceiveTask rt1 = (ReceiveTask) gateOut.getTargetRef();
				
				// Set in the description, that the receive task
				// must be ignored (it's not part of the activity)
				child.setDescription("ignore_rcv");
				
				OnMessageImpl onMsg1 = (OnMessageImpl) BPMNProcessTree.getBPELFactory().createOnMessage();
				
				// Momentarily the OnMessage element's operation is
				// given the Event's name + "operation" (NOTE)
				onMsg1.setOperationName(rt1.getName() + "-operation");
				
				// Obtain the child-Bpel-Activity
				a1 = tree.BpmnProctree2BpelModelPart(child, rpstParent);
				
				// Add the activity to the flow child
				onMsg1.setActivity(a1);
				
				pick1.getMessages().add(onMsg1);
				
			}
			
		}
		HandleBondComponent.logger.exit();
		return pick1;
	}
	
	
	private static Activity handleSpecialTaskStructure(BPMNProcessTree tree, Collection<RPSTNode> bondChildren, WFNode entry, WFNode exit, RPST rpstParent) {
		HandleBondComponent.logger.entry();
		// If the entry and exit elements of the Bound are activities
		// (NOT YET COMPLETELY SUPPORTED)
		
		Flow f1 = BPMNProcessTree.getBPELFactory().createFlow();
		org.eclipse.bpel.model.Activity actflow;
		
		for (Object e : bondChildren) {
			
			RPSTNode child = (RPSTNode) e;
			
			actflow = tree.BpmnProctree2BpelModelPart(child, rpstParent);
			f1.getActivities().add(actflow);
			
		}
		
		// Some Documentation is added to the flow element stating
		// that the following activities should be parallel executed
		// till the end of the process
		Documentation doc = BPMNProcessTree.getBPELFactory().createDocumentation();
		doc.setValue("FlowtillEnd");
		f1.setDocumentation(doc);
		HandleBondComponent.logger.exit();
		return f1;
	}
	
	private static Activity handleRepeatUntil(BPMNProcessTree tree, Collection<RPSTNode> bondChildren, WFNode entry, WFNode exit, RPST rpstParent) {
		HandleBondComponent.logger.entry();
		RepeatUntil repeat = BPMNProcessTree.getBPELFactory().createRepeatUntil();
		org.eclipse.bpel.model.Activity a1 = BPMNProcessTree.getBPELFactory().createActivity();
		IfImpl if1 = (IfImpl) BPMNProcessTree.getBPELFactory().createIf();
		int EntryToExit = entry.numberOfPathsto(bondChildren, exit);
		FormalExpressionImpl expr1 = null;
		org.eclipse.bpel.model.Activity actif;
		ElseIfImpl elseif1 = null;
		ElseImpl else1 = null;
		
		for (Object e : bondChildren) {
			
			RPSTNode child = (RPSTNode) e;
			WFNode centry = (WFNode) child.getEntry();
			WFNode cexit = (WFNode) child.getExit();
			
			// If the entry node of the Polygon equals the entry
			// node of the Bond, we have an activity of the repeat
			if (centry.equals(entry)) {
				
				a1 = tree.BpmnProctree2BpelModelPart(child, rpstParent);
				repeat.setActivity(a1);
				
			}
			// Otherwise we have the returning edge that represents
			// the evaluation of the repeat's condition
			else if (centry.equals(exit)) {
				
				SequenceFlow backRep = centry.getSourceOfPolygon((AbstractDirectedGraph) child.getFragment());
				Expression exprbackRep = backRep.getConditionExpression();
				Condition Repcond = BPMNProcessTree.getBPELFactory().createCondition();
				
				// The condition is set with the negation of the
				// Bpmn-expression's id
				Repcond.setBody("not-" + exprbackRep.getId());
				repeat.setCondition(Repcond);
				
			}
			
		}
		HandleBondComponent.logger.exit();
		return repeat;
	}
	
	private static Activity handleDoWhile(BPMNProcessTree tree, Collection<RPSTNode> bondChildren, WFNode entry, WFNode exit, RPST rpstParent) {
		HandleBondComponent.logger.entry();
		
		While while1 = BPMNProcessTree.getBPELFactory().createWhile();
		org.eclipse.bpel.model.Activity a1 = BPMNProcessTree.getBPELFactory().createActivity();
		int polygons = bondChildren.size();
		
		if (polygons == 2) {
			
			for (Object e : bondChildren) {
				
				RPSTNode child = (RPSTNode) e;
				SequenceFlow whileCondFlow = exit.getSourceOfPolygon((AbstractDirectedGraph) child.getFragment());
				if (whileCondFlow != null) {
					
					Expression whileCond = whileCondFlow.getConditionExpression();
					Condition BpelWhileCond = BPMNProcessTree.getBPELFactory().createCondition();
					
					// the main activity of the While is created and
					// the body of the condition set
					BpelWhileCond.setBody(whileCond.getId());
					a1 = tree.BpmnProctree2BpelModelPart(child, rpstParent);
					
					// The Bpel-While structure is filled with the
					// activity and condition
					while1.setActivity(a1);
					while1.setCondition(BpelWhileCond);
					
				}
			}
			return while1;
		} else if (polygons > 2) {
			
			// ***** Create the if ******
			IfImpl if1 = (IfImpl) BPMNProcessTree.getBPELFactory().createIf();
			ElseIfImpl elseif1 = null;
			org.eclipse.bpel.model.Activity actif;
			ElseImpl else1 = null;
			FormalExpressionImpl expr1 = null;
			
			// For every Polygon child of the Bond element
			for (Object e : bondChildren) {
				
				RPSTNode child = (RPSTNode) e;
				WFNode centry = (WFNode) child.getEntry();
				WFNode cexit = (WFNode) child.getExit();
				if (centry.equals(entry) && cexit.equals(exit)) {
					continue;
				}
				SequenceFlow conditionFlow = exit.getSourceOfPolygon((AbstractDirectedGraph) child.getFragment());
				expr1 = (FormalExpressionImpl) conditionFlow.getConditionExpression();
				Condition cond1 = BPMNProcessTree.getBPELFactory().createCondition();
				
				// The Body of the condition is filled with the id
				// of the corresponding BPMN condition (NOTE)
				cond1.setBody(expr1.getMixed().getValue(0));
				
				if (elseif1 == null) {
					
					if1.setCondition(cond1);
					actif = tree.BpmnProctree2BpelModelPart(child, rpstParent);
					if1.setActivity(actif);
					
				} else {
					
					// If the expression is not null it's an elseif
					if (expr1 != null) {
						elseif1.setCondition(cond1);
						actif = tree.BpmnProctree2BpelModelPart(child, rpstParent);
						elseif1.setActivity(actif);
						if1.getElseIf().add(elseif1);
					}
					// Otherwise is a default sequenceflow (the
					// remaining else)
					else {
						else1.setActivity(tree.BpmnProctree2BpelModelPart(child, rpstParent));
						if1.setElse(else1);
					}
					
				}
				
				elseif1 = (ElseIfImpl) BPMNProcessTree.getBPELFactory().createElseIf();
			}
			// ***********************
			
			// Add the if to the while as an activity
			while1.setActivity(if1);
			
			// Add the condition of the while
			// cond1 or cond2 or .... condN
			HandleBondComponent.logger.exit();
			return while1;
		} else {
			HandleBondComponent.logger.debug("Unsupported while case {}", polygons);
			HandleBondComponent.logger.exit();
			return null;
		}
	}
	
	private static Activity handleRepeatAndWhile(BPMNProcessTree tree, Collection<RPSTNode> bondChildren, WFNode entry, WFNode exit, RPST rpstParent) {
		HandleBondComponent.logger.entry();
		// The Structure must be rearranged
		SequenceImpl seq1 = (SequenceImpl) BPMNProcessTree.getBPELFactory().createSequence();
		SequenceImpl seq2 = (SequenceImpl) BPMNProcessTree.getBPELFactory().createSequence();
		SequenceImpl seq3 = (SequenceImpl) BPMNProcessTree.getBPELFactory().createSequence();
		IfImpl if1 = (IfImpl) BPMNProcessTree.getBPELFactory().createIf();
		org.eclipse.bpel.model.Activity if2 = null;
		ElseIfImpl elseif1 = null;
		org.eclipse.bpel.model.Activity actif;
		ElseImpl else1 = null;
		FormalExpressionImpl expr1 = null;
		WhileImpl while1 = (WhileImpl) BPMNProcessTree.getBPELFactory().createWhile();
		Condition whileCondition1 = BPMNProcessTree.getBPELFactory().createCondition();
		String WhileCondition = "";
		
		for (Object e : bondChildren) {
			
			RPSTNode child = (RPSTNode) e;
			WFNode centry = (WFNode) child.getEntry();
			WFNode cexit = (WFNode) child.getExit();
			
			if (centry.equals(exit) && cexit.equals(entry)) {
				
				SequenceFlow conditionFlow = centry.getSourceOfPolygon((AbstractDirectedGraph) child.getFragment());
				expr1 = (FormalExpressionImpl) conditionFlow.getConditionExpression();
				Condition cond1 = BPMNProcessTree.getBPELFactory().createCondition();
				
				cond1.setBody(expr1.getMixed().getValue(0));
				// Get the text of the expression (value of the
				// first entry of the Map)
				WhileCondition += expr1.getMixed().getValue(0) + " or ";
				
				if (elseif1 == null) {
					
					if1.setCondition(cond1);
					actif = tree.BpmnProctree2BpelModelPart(child, rpstParent);
					if1.setActivity(actif);
					
				} else {
					
					// If the expression is not null it's an elseif
					if (expr1 != null) {
						elseif1.setCondition(cond1);
						actif = tree.BpmnProctree2BpelModelPart(child, rpstParent);
						elseif1.setActivity(actif);
						if1.getElseIf().add(elseif1);
					}
					// Otherwise is a default sequenceflow, but it's
					// not possible to have to default sequenceflows
					else {
						// The Model is not executable, some OPAQUE
						// message should be added.
					}
					
				}
				elseif1 = (ElseIfImpl) BPMNProcessTree.getBPELFactory().createElseIf();
			} else if (centry.equals(entry) && cexit.equals(exit)) {
				
				seq1 = (SequenceImpl) BPMNProcessTree.getBPELFactory().createSequence();
				
				if2 = tree.BpmnProctree2BpelModelPart(child, rpstParent);
				
			}
			
		}
		
		seq1.getActivities().add(if2);
		seq2.getActivities().add(if1);
		seq2.getActivities().add(if2);
		while1.setActivity(seq2);
		WhileCondition = WhileCondition.substring(0, WhileCondition.length() - 4);
		whileCondition1.setBody(WhileCondition);
		while1.setCondition(whileCondition1);
		seq3.getActivities().add(if2);
		seq3.getActivities().add(while1);
		
		HandleBondComponent.logger.exit();
		return seq3;
	}
	
	static Activity handleBondComponent(BPMNProcessTree tree, RPSTNode node, RPST rpstParent) {
		HandleBondComponent.logger.entry(node);
		WFNode entry = (WFNode) node.getEntry();
		WFNode exit = (WFNode) node.getExit();
		boolean iscyclic = false;
		Collection fragEdges = node.getFragmentEdges();
		FlowNode entryNode = entry.getElement();
		FlowNode exitNode = exit.getElement();
		Collection<RPSTNode> bondChildren = rpstParent.getChildren(node);
		Activity res;
		
		if (entry.isCyclic(bondChildren, exit)) {
			// the structure is cyclic
			if (exit.numberOfPathsto(bondChildren, entry) == 1 && exit.numberOfEdgesto(bondChildren, entry) == 1 && exitNode instanceof ExclusiveGateway) {
				// the structure is that of Repeat
				res = HandleBondComponent.handleRepeatUntil(tree, bondChildren, entry, exit, rpstParent);
			} else if (entry.numberOfPathsto(bondChildren, exit) == 1 && entry.numberOfEdgesto(bondChildren, exit) == 1 && exitNode instanceof ExclusiveGateway) {
				// the structure is that of While
				res = HandleBondComponent.handleDoWhile(tree, bondChildren, entry, exit, rpstParent);
			} else if ((exit.numberOfPathsto(bondChildren, entry) > 1 || exit.numberOfPathsto(bondChildren, exit) == 1 && exit.numberOfEdgesto(bondChildren, exit) == 0) && entry.numberOfPathsto(bondChildren, exit) > 1 && entry.numberOfEdgesto(bondChildren, exit) == 0 && exitNode instanceof ExclusiveGateway) {
				// The Remaining structure is that of a Repeat + while
				res = HandleBondComponent.handleRepeatAndWhile(tree, bondChildren, entry, exit, rpstParent);
			} else {
				HandleBondComponent.logger.debug("Unsupported cyclic structural case");
				res = null;
			}
		} else {
			// the B Component is Acyclic
			if (entryNode instanceof ExclusiveGateway && exitNode instanceof ExclusiveGateway) {
				// If an XOR Structure is found
				res = HandleBondComponent.handleXORstructure(tree, bondChildren, entry, rpstParent);
			} else if (entryNode instanceof ParallelGateway && exitNode instanceof ParallelGateway) {
				// If an AND Structure is found
				res = HandleBondComponent.handleANDstructure(tree, bondChildren, rpstParent);
			} else if (entryNode instanceof InclusiveGateway && exitNode instanceof InclusiveGateway) {
				// If an Inclusive OR Structure is found
				res = HandleBondComponent.handleInclusiveORstructure(tree, bondChildren, entry, exit, rpstParent);
			} else if (entryNode instanceof EventBasedGateway && exitNode instanceof ExclusiveGateway) {
				// Pick structure
				res = HandleBondComponent.handlePickStructure(tree, bondChildren, entry, exit, rpstParent);
			} else if (entryNode instanceof Task && exitNode instanceof Task) {
				res = HandleBondComponent.handleSpecialTaskStructure(tree, bondChildren, entry, exit, rpstParent);
			} else {
				HandleBondComponent.logger.debug("Unsupported structural case {} -> {}", entry, exit);
				res = null;
			}
		}
		HandleBondComponent.logger.exit();
		return res;
	}
	
}
