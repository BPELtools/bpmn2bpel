package org.opentosca.bpmn2bpel.converter;

import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.Catch;
import org.eclipse.bpel.model.CompensationHandler;
import org.eclipse.bpel.model.Condition;
import org.eclipse.bpel.model.Documentation;
import org.eclipse.bpel.model.Empty;
import org.eclipse.bpel.model.FaultHandler;
import org.eclipse.bpel.model.ForEach;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.Link;
import org.eclipse.bpel.model.OnAlarm;
import org.eclipse.bpel.model.OnEvent;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.RepeatUntil;
import org.eclipse.bpel.model.Scope;
import org.eclipse.bpel.model.Source;
import org.eclipse.bpel.model.Sources;
import org.eclipse.bpel.model.Target;
import org.eclipse.bpel.model.Targets;
import org.eclipse.bpel.model.Throw;
import org.eclipse.bpel.model.Wait;
import org.eclipse.bpel.model.impl.WhileImpl;
import org.eclipse.bpmn2.BusinessRuleTask;
import org.eclipse.bpmn2.CallActivity;
import org.eclipse.bpmn2.CancelEventDefinition;
import org.eclipse.bpmn2.CatchEvent;
import org.eclipse.bpmn2.CompensateEventDefinition;
import org.eclipse.bpmn2.ConditionalEventDefinition;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.ErrorEventDefinition;
import org.eclipse.bpmn2.EscalationEventDefinition;
import org.eclipse.bpmn2.EventDefinition;
import org.eclipse.bpmn2.Expression;
import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.IntermediateCatchEvent;
import org.eclipse.bpmn2.IntermediateThrowEvent;
import org.eclipse.bpmn2.ManualTask;
import org.eclipse.bpmn2.MessageEventDefinition;
import org.eclipse.bpmn2.MultiInstanceLoopCharacteristics;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.ReceiveTask;
import org.eclipse.bpmn2.ScriptTask;
import org.eclipse.bpmn2.SendTask;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.ServiceTask;
import org.eclipse.bpmn2.SignalEventDefinition;
import org.eclipse.bpmn2.SubProcess;
import org.eclipse.bpmn2.Task;
import org.eclipse.bpmn2.TerminateEventDefinition;
import org.eclipse.bpmn2.TimerEventDefinition;
import org.eclipse.bpmn2.UserTask;
import org.eclipse.bpmn2.impl.LoopCharacteristicsImpl;
import org.eclipse.bpmn2.impl.MultiInstanceLoopCharacteristicsImpl;
import org.eclipse.wst.wsdl.Operation;
import org.eclipse.wst.wsdl.WSDLFactory;
import org.jbpt.graph.abs.AbstractDirectedGraph;
import org.jbpt.graph.algo.rpst.RPST;
import org.jbpt.graph.algo.rpst.RPSTNode;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;


public class HandleTrivialComponent {
	
	private static final XLogger logger = XLoggerFactory.getXLogger(HandleTrivialComponent.class);
	
	
	private static Activity handleEndNode(RPSTNode node) {
		Activity res;
		WFNode nexit = (WFNode) node.getExit();
		if (nexit.getElement() instanceof EndEvent) {
			EndEvent endEv = (EndEvent) nexit.getElement();
			if (endEv.getEventDefinitions().isEmpty()) {
				res = BPMNProcessTree.getBPELFactory().createEmpty();
			} else {
				EventDefinition eventDefinition = endEv.getEventDefinitions().get(0);
				if (eventDefinition instanceof TerminateEventDefinition || eventDefinition instanceof CancelEventDefinition) {
					res = BPMNProcessTree.getBPELFactory().createExit();
				} else {
					HandleTrivialComponent.logger.debug("Undhandled end event type {}", eventDefinition);
					return null;
				}
			}
		} else {
			HandleTrivialComponent.logger.debug("Unhandled end node type {}", nexit.getClass());
			return null;
		}
		HandleTrivialComponent.copyName(nexit, res);
		return res;
	}
	
	private static Activity handleIntermediateThrowEvent(WFNode wfNode, IntermediateThrowEvent inEv) {
		if (inEv.getEventDefinitions().isEmpty()) {
			HandleTrivialComponent.logger.debug("Intermediate throw wihtout any event definitions");
			return null;
		} else {
			// type checking
			EventDefinition eventDefinition = inEv.getEventDefinitions().get(0);
			Activity res;
			if (eventDefinition instanceof MessageEventDefinition) {
				Invoke i1 = BPMNProcessTree.getBPELFactory().createInvoke();
				res = i1;
			} else {
				HandleTrivialComponent.logger.debug("Unknown intermediate event type {}.", eventDefinition.getClass());
				return null;
			}
			HandleTrivialComponent.copyName(wfNode, res);
			return res;
		}
	}
	
	/**
	 * 
	 * @param bpmnTask the BPMN task
	 * @param activity the BPEL activity already converted based on bpmnTask
	 */
	private static org.eclipse.bpel.model.Activity handleLoopCharacteristics(Task bpmnTask, org.eclipse.bpel.model.Activity bpelActivity) {
		if (bpmnTask.getLoopCharacteristics() == null) {
			return bpelActivity;
		} else if (bpmnTask.getLoopCharacteristics() instanceof MultiInstanceLoopCharacteristics) {
			ForEach fe1 = BPMNProcessTree.getBPELFactory().createForEach();
			
			org.eclipse.bpel.model.impl.ExpressionImpl exprS = (org.eclipse.bpel.model.impl.ExpressionImpl) BPMNProcessTree.getBPELFactory().createExpression();
			exprS.setBody("1");
			fe1.setStartCounterValue(exprS);
			
			MultiInstanceLoopCharacteristicsImpl mloopchar = (MultiInstanceLoopCharacteristicsImpl) bpmnTask.getLoopCharacteristics();
			Condition exprF = Utils.convertExpressionToCondition(mloopchar.getCompletionCondition());
			fe1.setFinalCounterValue(exprF);
			
			fe1.setParallel(!mloopchar.isIsSequential());
			
			Scope scope1 = BPMNProcessTree.getBPELFactory().createScope();
			scope1.setActivity(bpelActivity);
			fe1.setActivity(scope1);
			return fe1;
		} else {
			WhileImpl while1 = (WhileImpl) BPMNProcessTree.getBPELFactory().createWhile();
			
			LoopCharacteristicsImpl loopchar = (LoopCharacteristicsImpl) bpmnTask.getLoopCharacteristics();
			Condition cond = Utils.convertExpressionToCondition((Expression) loopchar.eContents().get(0));
			
			// The Bpel-While structure is filled with the
			// activity and condition
			while1.setActivity(bpelActivity);
			while1.setCondition(cond);
			
			return while1;
		}
	}
	
	/**
	 * 
	 * @param bpmnTask the BPMN task
	 * @param activity the BPEL activity already converted based on bpmnTask
	 */
	private static org.eclipse.bpel.model.Activity handleBoundaryEvents(WFNode wfNode, org.eclipse.bpel.model.Activity bpelActivity) {
		// Obtain the boundary event flows
		List<BPMNProcessTree> eventFlows = wfNode.getBoundaryEventFlows();
		if (eventFlows.isEmpty()) {
			return bpelActivity;
		} else {
			Scope actScope = null;
			for (int i = 0; i < eventFlows.size(); i++) {
				actScope = HandleTrivialComponent.addHandlertoScope(eventFlows.get(i), actScope);
			}
			actScope.setActivity(bpelActivity);
			return actScope;
		}
	}
	
	/**
	 * 
	 * @param wfNode.getElement() the BPMN task
	 * @param activity the BPEL activity already converted based on bpmnTask
	 */
	private static org.eclipse.bpel.model.Activity genericTaskHandling(WFNode wfNode, org.eclipse.bpel.model.Activity bpelActivity) {
		org.eclipse.bpel.model.Activity res = bpelActivity;
		res = HandleTrivialComponent.handleLoopCharacteristics((Task) wfNode.getElement(), res);
		res = HandleTrivialComponent.handleBoundaryEvents(wfNode, res);
		HandleTrivialComponent.copyName(wfNode, res);
		return res;
	}
	
	/**
	 * 
	 * @param wfNode the current wfNode
	 * @param activity the BPEL activity already converted based on bpmnTask,
	 *            this is MODIFIED
	 */
	private static void copyName(final WFNode wfNode, final org.eclipse.bpel.model.Activity bpelActivity) {
		String name = wfNode.getName();
		if (!name.equals("")) {
			bpelActivity.setName(name);
		}
	}
	
	private static org.eclipse.bpel.model.Activity convertToInvoke(org.eclipse.bpmn2.Operation opRef, WFNode wfNode) {
		Invoke i1 = BPMNProcessTree.getBPELFactory().createInvoke();
		WSDLFactory wsdlfact = new org.eclipse.wst.wsdl.internal.impl.WSDLFactoryImpl();
		Operation i1Op = wsdlfact.createOperation();
		if (opRef != null) {
			i1Op.setName(opRef.getName());
			i1.setOperation(i1Op);
		}
		i1.setName(wfNode.getName());
		return HandleTrivialComponent.genericTaskHandling(wfNode, i1);
	}
	
	private static org.eclipse.bpel.model.Activity handleCallActivity(WFNode wfNode, CallActivity element) {
		// TODO: real implementation, not a pseudo invoke
		Invoke i1 = BPMNProcessTree.getBPELFactory().createInvoke();
		return HandleTrivialComponent.genericTaskHandling(wfNode, i1);
	}
	
	private static org.eclipse.bpel.model.Activity handleServiceTask(WFNode wfNode, ServiceTask t) {
		HandleTrivialComponent.logger.entry();
		Activity res = HandleTrivialComponent.convertToInvoke(t.getOperationRef(), wfNode);
		HandleTrivialComponent.logger.exit();
		return res;
	}
	
	private static org.eclipse.bpel.model.Activity handleSendTask(WFNode wfNode, SendTask t) {
		HandleTrivialComponent.logger.entry();
		Activity res = HandleTrivialComponent.convertToInvoke(t.getOperationRef(), wfNode);
		HandleTrivialComponent.logger.exit();
		return res;
	}
	
	private static org.eclipse.bpel.model.Activity handleReceiveTask(WFNode wfNode, ReceiveTask t) {
		HandleTrivialComponent.logger.entry();
		Receive r1 = BPMNProcessTree.getBPELFactory().createReceive();
		org.eclipse.bpmn2.Operation rt1Op = t.getOperationRef();
		if (rt1Op != null) {
			WSDLFactory wsdlfact = new org.eclipse.wst.wsdl.internal.impl.WSDLFactoryImpl();
			Operation i1Op = wsdlfact.createOperation();
			String opName = rt1Op.getName();
			if (opName != null) {
				i1Op.setName(opName);
				r1.setOperation(i1Op);
			}
		}
		
		// Set name of the Receive
		r1.setName(t.getName());
		
		HandleTrivialComponent.logger.exit();
		return HandleTrivialComponent.genericTaskHandling(wfNode, r1);
	}
	
	private static org.eclipse.bpel.model.Activity handleIntermediateCatchEvent(WFNode wfNode, IntermediateCatchEvent inEv) {
		if (inEv.getEventDefinitions().isEmpty()) {
			HandleTrivialComponent.logger.debug("Intermediate catch wihtout any event definitions");
			return null;
		} else {
			// If the entry element of the Trivial component is an
			// intermediate catch event its definition is checked to
			// determine the concrete type type
			EventDefinition eventDefinition = inEv.getEventDefinitions().get(0);
			Activity res;
			if (eventDefinition instanceof MessageEventDefinition) {
				res = HandleTrivialComponent.handleIntermediateMessageCatchEvent((MessageEventDefinition) eventDefinition);
			} else if (eventDefinition instanceof TimerEventDefinition) {
				res = HandleTrivialComponent.handleIntermediateTimerEvent((TimerEventDefinition) eventDefinition);
			} else if (eventDefinition instanceof ConditionalEventDefinition) {
				res = HandleTrivialComponent.handleIntermediateConditionalEvent((ConditionalEventDefinition) eventDefinition);
			} else {
				HandleTrivialComponent.logger.debug("Unknown intermediate event type {}.", eventDefinition.getClass());
				return null;
			}
			HandleTrivialComponent.copyName(wfNode, res);
			return res;
		}
	}
	
	private static Activity handleIntermediateConditionalEvent(ConditionalEventDefinition condDef) {
		RepeatUntil repeat1 = BPMNProcessTree.getBPELFactory().createRepeatUntil();
		FormalExpression condition = (FormalExpression) condDef.getCondition();
		
		if (condition != null) {
			Condition repeatExp = BPMNProcessTree.getBPELFactory().createCondition();
			repeatExp.setBody(condition.getMixed().getValue(0));
			repeat1.setCondition(repeatExp);
		}
		
		Empty empty1 = BPMNProcessTree.getBPELFactory().createEmpty();
		repeat1.setActivity(empty1);
		
		return repeat1;
	}
	
	private static Activity handleIntermediateTimerEvent(TimerEventDefinition timerDef) {
		Wait wait1 = BPMNProcessTree.getBPELFactory().createWait();
		Expression timedur = timerDef.getTimeDuration();
		Expression timeDate = timerDef.getTimeDate();
		org.eclipse.bpel.model.Expression waitExp = BPMNProcessTree.getBPELFactory().createExpression();
		
		if (timedur != null) {
			waitExp.setBody(timedur.getAnyAttribute());
			wait1.setFor(waitExp);
		} else if (timeDate != null) {
			waitExp.setBody(timeDate.getAnyAttribute());
			wait1.setUntil(waitExp);
		}
		
		return wait1;
	}
	
	private static org.eclipse.bpel.model.Activity handleIntermediateMessageCatchEvent(MessageEventDefinition messageDef) {
		Receive r1 = BPMNProcessTree.getBPELFactory().createReceive();
		return r1;
	}
	
	private static org.eclipse.bpel.model.Activity handleSubProcess(WFNode wfNode) {
		BPMNProcessTree subprocT = wfNode.getSubprocessTree();
		
		RPST rpstgraph = new RPST(subprocT);
		RPSTNode subProcRoot = rpstgraph.getRoot();
		subprocT.setRPST(rpstgraph);
		wfNode.setSubProcessTree(subprocT);
		org.eclipse.bpel.model.Process p1 = subprocT.BpmnProctree2BpelModel(subProcRoot);
		return HandleTrivialComponent.genericTaskHandling(wfNode, p1.getActivity());
	}
	
	private static org.eclipse.bpel.model.Activity handlPlainTask(WFNode wfNode) {
		HandleTrivialComponent.logger.debug("Converted {} to empty", wfNode);
		Empty e1 = BPMNProcessTree.getBPELFactory().createEmpty();
		return HandleTrivialComponent.genericTaskHandling(wfNode, e1);
	}
	
	private static Scope addHandlertoScope(BPMNProcessTree eventFlow, Scope actScope) {
		BPMNProcessTree eventHandler = eventFlow;
		RPST EvHandlerRPST = new RPST(eventHandler);
		FaultHandler faultH = null;
		eventHandler.setRPST(EvHandlerRPST);
		
		// The scope containing all the boundary events (Handlers) is created if
		// the one
		// passed as argument is null
		if (actScope == null) {
			actScope = BPMNProcessTree.getBPELFactory().createScope();
			faultH = BPMNProcessTree.getBPELFactory().createFaultHandler();
		} else {
			faultH = actScope.getFaultHandlers();
			
			if (faultH == null) {
				faultH = BPMNProcessTree.getBPELFactory().createFaultHandler();
			}
		}
		
		RPSTNode EvHandlerRoot = EvHandlerRPST.getRoot();
		WFNode HandlerEntry = (WFNode) EvHandlerRoot.getEntry();
		WFNode HandlerExit = (WFNode) EvHandlerRoot.getExit();
		
		// If the ending node of the boundary event handler is in the main flow
		if (HandlerExit.isMainFlow()) {
			
			// Check if the Event Definition is interruptive
			if (eventHandler.isInterrupting()) {
				
				BPMNProcessTree t = new BPMNProcessTree();
				org.eclipse.bpel.model.Activity ActHandler = t.BpmnProctree2BpelModelPart(EvHandlerRoot, EvHandlerRPST);
				
				CatchEvent event = (CatchEvent) HandlerEntry.getElement();
				Catch faultCatch = BPMNProcessTree.getBPELFactory().createCatch();
				Documentation handlerInfo = BPMNProcessTree.getBPELFactory().createDocumentation();
				
				// The End specifies that the following handler ends without
				// entering the main flow.
				
				SequenceFlow HndlrExFlow = HandlerExit.getTargetOfPolygon((AbstractDirectedGraph) EvHandlerRoot.getFragment());
				
				// If the Sequence Flow that exits the exception flow and enters
				// the main flow has a name
				if (HndlrExFlow.getName() != "") {
					handlerInfo.setValue(HandlerExit.getElement().getId() + "/" + HndlrExFlow.getName());
				}
				// Otherwise the id of the sequenceFlow is used
				else {
					handlerInfo.setValue(HandlerExit.getElement().getId() + "/" + HndlrExFlow.getId());
				}
				faultCatch.setDocumentation(handlerInfo);
				
				// If the Boundary Event is of type Error a FaultHandler is
				// created
				if (event.getEventDefinitions().get(0) instanceof ErrorEventDefinition) {
					
					faultCatch.setFaultName(new QName(event.getName()));
					faultH.getCatch().add(faultCatch);
					actScope.setFaultHandlers(faultH);
					
					faultCatch.setActivity(ActHandler);
					
				}
				// If the Boundary Event is of type Message or Time an OnAlarm
				// is created linking to a Fault Handler
				else if (event.getEventDefinitions().get(0) instanceof TimerEventDefinition) {
					
					TimerEventDefinition timevdef = (TimerEventDefinition) event.getEventDefinitions().get(0);
					org.eclipse.bpel.model.EventHandler EvHandler = actScope.getEventHandlers();
					
					if (EvHandler == null) {
						EvHandler = BPMNProcessTree.getBPELFactory().createEventHandler();
					}
					
					OnAlarm alarmEv = BPMNProcessTree.getBPELFactory().createOnAlarm();
					Empty alarmScope = BPMNProcessTree.getBPELFactory().createEmpty();
					
					Link link2Fault = BPMNProcessTree.getBPELFactory().createLink();
					Target target2Fault = BPMNProcessTree.getBPELFactory().createTarget();
					Targets targetCont = BPMNProcessTree.getBPELFactory().createTargets();
					Source faultFromTarget = BPMNProcessTree.getBPELFactory().createSource();
					Sources sourceCont = BPMNProcessTree.getBPELFactory().createSources();
					
					link2Fault.setName(event.getName() + " to Fault");
					
					// The Target and target container are assigned and filled
					target2Fault.setLink(link2Fault);
					targetCont.getChildren().add(target2Fault);
					
					// The Link, sources and source are assigned and filled
					faultFromTarget.setLink(link2Fault);
					sourceCont.getChildren().add(faultFromTarget);
					alarmScope.setSources(sourceCont);
					
					// The Activity of the alarm scope is set (in this case an
					// empty with a source to the Fault Handler)
					FormalExpression timeCyc = (FormalExpression) timevdef.getTimeCycle();
					org.eclipse.bpel.model.Expression timeexp = BPMNProcessTree.getBPELFactory().createExpression();
					
					// Set the date, duration or repeat cycle.
					if (timeCyc != null) {
						timeexp.setBody(timeCyc.getMixed().getValue(0));
						alarmEv.setRepeatEvery(timeexp);
					}
					timeCyc = (FormalExpression) timevdef.getTimeDate();
					if (timeCyc != null) {
						timeexp.setBody(timeCyc.getMixed().getValue(0));
						alarmEv.setUntil(timeexp);
					}
					timeCyc = (FormalExpression) timevdef.getTimeDuration();
					if (timeCyc != null) {
						timeexp.setBody(timeCyc.getMixed().getValue(0));
						alarmEv.setFor(timeexp);
					}
					
					alarmEv.setActivity(alarmScope);
					EvHandler.getAlarm().add(alarmEv);
					
					ActHandler.setTargets(targetCont);
					faultCatch.setFaultName(new QName(event.getName()));
					faultCatch.setActivity(ActHandler);
					faultH.getCatch().add(faultCatch);
					actScope.setFaultHandlers(faultH);
					actScope.setEventHandlers(EvHandler);
					
				} else if (event.getEventDefinitions().get(0) instanceof MessageEventDefinition) {
					
					MessageEventDefinition msgevdef = (MessageEventDefinition) event.getEventDefinitions().get(0);
					org.eclipse.bpel.model.EventHandler EvHandler = actScope.getEventHandlers();
					
					if (EvHandler == null) {
						EvHandler = BPMNProcessTree.getBPELFactory().createEventHandler();
					}
					
					OnEvent eventEv = BPMNProcessTree.getBPELFactory().createOnEvent();
					Empty eventScope = BPMNProcessTree.getBPELFactory().createEmpty();
					
					Link link2Fault = BPMNProcessTree.getBPELFactory().createLink();
					Target target2Fault = BPMNProcessTree.getBPELFactory().createTarget();
					Targets targetCont = BPMNProcessTree.getBPELFactory().createTargets();
					Source faultFromTarget = BPMNProcessTree.getBPELFactory().createSource();
					Sources sourceCont = BPMNProcessTree.getBPELFactory().createSources();
					
					link2Fault.setName(event.getName() + " to Fault");
					
					// The Target and target container are assigned and filled
					target2Fault.setLink(link2Fault);
					targetCont.getChildren().add(target2Fault);
					
					// The Link, sources and source are assigned and filled
					faultFromTarget.setLink(link2Fault);
					sourceCont.getChildren().add(faultFromTarget);
					eventScope.setSources(sourceCont);
					
					// The Activity of the alarm scope is set (in this case an
					// empty with a source to the Fault Handler)
					String opname = msgevdef.getOperationRef().getName();
					WSDLFactory wsdlfact = new org.eclipse.wst.wsdl.internal.impl.WSDLFactoryImpl();
					Operation op = wsdlfact.createOperation();
					op.setName(opname);
					
					eventEv.setOperation(op);
					eventEv.setActivity(eventScope);
					EvHandler.getEvents().add(eventEv);
					
					ActHandler.setTargets(targetCont);
					faultCatch.setFaultName(new QName(event.getName()));
					faultCatch.setActivity(ActHandler);
					faultH.getCatch().add(faultCatch);
					actScope.setFaultHandlers(faultH);
					actScope.setEventHandlers(EvHandler);
					
				} else if (event.getEventDefinitions().get(0) instanceof EscalationEventDefinition) {
					
					// TODO
					
				} else if (event.getEventDefinitions().get(0) instanceof SignalEventDefinition) {
					
					// TODO
					
				} else if (event.getEventDefinitions().get(0) instanceof ConditionalEventDefinition) {
					
					// TODO
				}
				
			}
			// If the Boundary-Flow isn't interrupting
			else {
				
			}
			
		}
		// Otherwise then this boundary event flow doesn't communicate with the
		// main flow
		else {
			
			// Check if the Event Definition is interruptive
			if (eventHandler.isInterrupting()) {
				
				BPMNProcessTree t = new BPMNProcessTree();
				org.eclipse.bpel.model.Activity ActHandler = t.BpmnProctree2BpelModelPart(EvHandlerRoot, EvHandlerRPST);
				
				CatchEvent event = (CatchEvent) HandlerEntry.getElement();
				Documentation handlerInfo = BPMNProcessTree.getBPELFactory().createDocumentation();
				Catch faultCatch = BPMNProcessTree.getBPELFactory().createCatch();
				
				// The End specifies that the following handler ends without
				// entering the main flow.
				handlerInfo.setValue("N");
				faultCatch.setDocumentation(handlerInfo);
				
				// If the Boundary Event is of type Error a FaultHandler is
				// created
				if (event.getEventDefinitions().get(0) instanceof ErrorEventDefinition) {
					
					faultCatch.setFaultName(new QName(event.getName()));
					faultCatch.setActivity(ActHandler);
					faultH.getCatch().add(faultCatch);
					actScope.setFaultHandlers(faultH);
					
				}
				// If the Boundary Event is of type Message or Time an OnAlarm
				// is created linking to a Fault Handler
				else if (event.getEventDefinitions().get(0) instanceof TimerEventDefinition) {
					
					// TODO
					TimerEventDefinition timevdef = (TimerEventDefinition) event.getEventDefinitions().get(0);
					org.eclipse.bpel.model.EventHandler EvHandler = actScope.getEventHandlers();
					Throw throwEv = BPMNProcessTree.getBPELFactory().createThrow();
					
					if (EvHandler == null) {
						EvHandler = BPMNProcessTree.getBPELFactory().createEventHandler();
					}
					OnAlarm alarmEv = BPMNProcessTree.getBPELFactory().createOnAlarm();
					
					faultCatch.setFaultName(new QName(event.getName() + "fault"));
					throwEv.setFaultName(new QName(event.getName() + "fault"));
					
					FormalExpression timeCyc = (FormalExpression) timevdef.getTimeCycle();
					org.eclipse.bpel.model.Expression timeexp = BPMNProcessTree.getBPELFactory().createExpression();
					
					// Set the date, duration or repeat cycle.
					if (timeCyc != null) {
						timeexp.setBody(timeCyc.getMixed().getValue(0));
						alarmEv.setRepeatEvery(timeexp);
					}
					timeCyc = (FormalExpression) timevdef.getTimeDate();
					if (timeCyc != null) {
						timeexp.setBody(timeCyc.getMixed().getValue(0));
						alarmEv.setUntil(timeexp);
					}
					timeCyc = (FormalExpression) timevdef.getTimeDuration();
					if (timeCyc != null) {
						timeexp.setBody(timeCyc.getMixed().getValue(0));
						alarmEv.setFor(timeexp);
					}
					alarmEv.setActivity(throwEv);
					EvHandler.getAlarm().add(alarmEv);
					faultCatch.setActivity(ActHandler);
					faultH.getCatch().add(faultCatch);
					actScope.setFaultHandlers(faultH);
					
				} else if (event.getEventDefinitions().get(0) instanceof MessageEventDefinition) {
					
					MessageEventDefinition msgdef = (MessageEventDefinition) event.getEventDefinitions().get(0);
					org.eclipse.bpmn2.Operation msgOp = msgdef.getOperationRef();
					WSDLFactory wsdlfact = new org.eclipse.wst.wsdl.internal.impl.WSDLFactoryImpl();
					
					// Create new WSDL Operation to be added to the BPEL
					Operation onMsgOperation = wsdlfact.createOperation();
					onMsgOperation.setName(msgOp.getName());
					
					org.eclipse.bpel.model.EventHandler EvHandler = actScope.getEventHandlers();
					Throw throwEv = BPMNProcessTree.getBPELFactory().createThrow();
					
					if (EvHandler == null) {
						EvHandler = BPMNProcessTree.getBPELFactory().createEventHandler();
					}
					OnEvent msgEv = BPMNProcessTree.getBPELFactory().createOnEvent();
					msgEv.setOperation(onMsgOperation);
					
					faultCatch.setFaultName(new QName(event.getName() + "fault"));
					throwEv.setFaultName(new QName(event.getName() + "fault"));
					
					msgEv.setActivity(throwEv);
					EvHandler.getEvents().add(msgEv);
					faultCatch.setActivity(ActHandler);
					faultH.getCatch().add(faultCatch);
					actScope.setFaultHandlers(faultH);
					
				} else if (event.getEventDefinitions().get(0) instanceof EscalationEventDefinition) {
					
					// TODO
					
				} else if (event.getEventDefinitions().get(0) instanceof CompensateEventDefinition) {
					
					CompensationHandler compH = BPMNProcessTree.getBPELFactory().createCompensationHandler();
					compH.setActivity(ActHandler);
					actScope.setCompensationHandler(compH);
					
				}
				
			}
			// If the Boundary Event isn't interrupting
			else {
				/*
				 * org.eclipse.bpel.model.Activity ActHandler =
				 * this.BpmnProctree2BpelModelPart(EvHandlerRoot,
				 * EvHandlerRPST); CatchEvent event = (CatchEvent)
				 * HandlerEntry.getElement();
				 * 
				 * if (event.getEventDefinitions().get(0) instanceof
				 * MessageEventDefinition) {
				 * 
				 * faultCatch.setFaultName(new QName(event.getName()));
				 * faultH.getCatch().add(faultCatch);
				 * actScope.setFaultHandlers(faultH);
				 * 
				 * faultCatch.setActivity(ActHandler);
				 * 
				 * }
				 */
			}
			
		}
		
		return actScope;
	}
	
	static Activity handleTrivialComponent(RPSTNode node, RPST rpstParent) {
		HandleTrivialComponent.logger.entry(node);
		Activity res;
		// The Node is the last one in a sequence (Polygon)
		boolean isLastOneInSequence = node.getDescription().equals("Last-Trivial");
		if (isLastOneInSequence) {
			res = HandleTrivialComponent.handleEndNode(node);
		} else {
			WFNode nentry = (WFNode) node.getEntry();
			if (nentry.getElement() instanceof Task) {
				if (nentry.getElement() instanceof ServiceTask) {
					res = HandleTrivialComponent.handleServiceTask(nentry, (ServiceTask) nentry.getElement());
				} else if (nentry.getElement() instanceof SendTask) {
					res = HandleTrivialComponent.handleSendTask(nentry, (SendTask) nentry.getElement());
				} else if (nentry.getElement() instanceof ReceiveTask) {
					res = HandleTrivialComponent.handleReceiveTask(nentry, (ReceiveTask) nentry.getElement());
				} else if (nentry.getElement() instanceof BusinessRuleTask) {
					HandleTrivialComponent.logger.error("Business Rule Tasks not yet supported.");
					res = HandleTrivialComponent.handlPlainTask(nentry);
				} else if (nentry.getElement() instanceof ManualTask) {
					HandleTrivialComponent.logger.error("Manual tasks may not appear in automated workflows");
					res = HandleTrivialComponent.handlPlainTask(nentry);
				} else if (nentry.getElement() instanceof ScriptTask) {
					HandleTrivialComponent.logger.error("Script Tasks not yet supported.");
					res = HandleTrivialComponent.handlPlainTask(nentry);
				} else if (nentry.getElement() instanceof UserTask) {
					HandleTrivialComponent.logger.error("User Tasks not yet supported.");
					res = HandleTrivialComponent.handlPlainTask(nentry);
				} else {
					// Plain task
					// There is no explicit type for a task without marker. The
					// interface "Task" also models the plain task. Therefore,
					// we cannot check for completeness here, but current
					// implementation of bpmn2 model does not have more subtypes
					// as checked above
					res = HandleTrivialComponent.handlPlainTask(nentry);
				}
			} else if (nentry.getElement() instanceof CallActivity) {
				res = HandleTrivialComponent.handleCallActivity(nentry, (CallActivity) nentry.getElement());
			} else if (nentry.getElement() instanceof SubProcess) {
				res = HandleTrivialComponent.handleSubProcess(nentry);
			} else if (nentry.getElement() instanceof IntermediateCatchEvent) {
				res = HandleTrivialComponent.handleIntermediateCatchEvent(nentry, (IntermediateCatchEvent) nentry.getElement());
			} else if (nentry.getElement() instanceof IntermediateThrowEvent) {
				res = HandleTrivialComponent.handleIntermediateThrowEvent(nentry, (IntermediateThrowEvent) nentry.getElement());
			} else {
				RPSTNode nodeP = rpstParent.getParent(node);
				if (nentry.getElement() instanceof ParallelGateway && !nodeP.getEntry().equals(nentry)) {
					res = BPMNProcessTree.getBPELFactory().createEmpty();
				} else {
					// If the entry element is a Start event or another event
					// nothing is done (momentarily)
					HandleTrivialComponent.logger.debug("Not handled event {}", nentry.getElement().getClass());
					res = null;
				}
			}
		}
		HandleTrivialComponent.logger.exit();
		return res;
	}
	
}
