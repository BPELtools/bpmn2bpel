package org.opentosca.bpmn2bpel.converter.debug;

import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.Assign;
import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.BPELFactory;
import org.eclipse.bpel.model.BooleanExpression;
import org.eclipse.bpel.model.Branches;
import org.eclipse.bpel.model.Catch;
import org.eclipse.bpel.model.CatchAll;
import org.eclipse.bpel.model.Compensate;
import org.eclipse.bpel.model.CompensateScope;
import org.eclipse.bpel.model.CompensationHandler;
import org.eclipse.bpel.model.CompletionCondition;
import org.eclipse.bpel.model.Condition;
import org.eclipse.bpel.model.Copy;
import org.eclipse.bpel.model.Correlation;
import org.eclipse.bpel.model.CorrelationSet;
import org.eclipse.bpel.model.CorrelationSets;
import org.eclipse.bpel.model.Correlations;
import org.eclipse.bpel.model.Documentation;
import org.eclipse.bpel.model.Else;
import org.eclipse.bpel.model.ElseIf;
import org.eclipse.bpel.model.Empty;
import org.eclipse.bpel.model.EventHandler;
import org.eclipse.bpel.model.Exit;
import org.eclipse.bpel.model.Expression;
import org.eclipse.bpel.model.Extension;
import org.eclipse.bpel.model.ExtensionActivity;
import org.eclipse.bpel.model.Extensions;
import org.eclipse.bpel.model.FaultHandler;
import org.eclipse.bpel.model.Flow;
import org.eclipse.bpel.model.ForEach;
import org.eclipse.bpel.model.From;
import org.eclipse.bpel.model.FromPart;
import org.eclipse.bpel.model.FromParts;
import org.eclipse.bpel.model.If;
import org.eclipse.bpel.model.Import;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.Link;
import org.eclipse.bpel.model.Links;
import org.eclipse.bpel.model.MessageExchange;
import org.eclipse.bpel.model.MessageExchanges;
import org.eclipse.bpel.model.OnAlarm;
import org.eclipse.bpel.model.OnEvent;
import org.eclipse.bpel.model.OnMessage;
import org.eclipse.bpel.model.OpaqueActivity;
import org.eclipse.bpel.model.PartnerActivity;
import org.eclipse.bpel.model.PartnerLink;
import org.eclipse.bpel.model.PartnerLinks;
import org.eclipse.bpel.model.Pick;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Query;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.RepeatUntil;
import org.eclipse.bpel.model.Reply;
import org.eclipse.bpel.model.Rethrow;
import org.eclipse.bpel.model.Scope;
import org.eclipse.bpel.model.Sequence;
import org.eclipse.bpel.model.ServiceRef;
import org.eclipse.bpel.model.Source;
import org.eclipse.bpel.model.Sources;
import org.eclipse.bpel.model.Target;
import org.eclipse.bpel.model.Targets;
import org.eclipse.bpel.model.TerminationHandler;
import org.eclipse.bpel.model.Throw;
import org.eclipse.bpel.model.To;
import org.eclipse.bpel.model.ToPart;
import org.eclipse.bpel.model.ToParts;
import org.eclipse.bpel.model.UnknownExtensibilityAttribute;
import org.eclipse.bpel.model.Validate;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.model.Variables;
import org.eclipse.bpel.model.Wait;
import org.eclipse.bpel.model.While;
import org.eclipse.bpel.model.impl.BPELFactoryImpl;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;


public class LoggingBPELFactoryImpl extends BPELFactoryImpl implements BPELFactory {
	
	private static final XLogger logger = XLoggerFactory.getXLogger(LoggingBPELFactoryImpl.class);
	
	
	@Override
	public Process createProcess() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createProcess();

	}
	
	@Override
	public PartnerLink createPartnerLink() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createPartnerLink();
		
	}
	
	@Override
	public FaultHandler createFaultHandler() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createFaultHandler();
		
	}
	
	@Override
	public Activity createActivity() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createActivity();
		
	}
	
	@Override
	public CorrelationSet createCorrelationSet() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createCorrelationSet();
		
	}
	
	@Override
	public Invoke createInvoke() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createInvoke();
		
	}
	
	@Override
	public Link createLink() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createLink();
		
	}
	
	@Override
	public Catch createCatch() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createCatch();
		
	}
	
	@Override
	public Reply createReply() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createReply();
		
	}
	
	@Override
	public PartnerActivity createPartnerActivity() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createPartnerActivity();
		
	}
	
	@Override
	public Receive createReceive() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createReceive();
		
	}
	
	@Override
	public Throw createThrow() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createThrow();
		
	}
	
	@Override
	public Wait createWait() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createWait();
		
	}
	
	@Override
	public Empty createEmpty() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createEmpty();
		
	}
	
	@Override
	public Sequence createSequence() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createSequence();
		
	}
	
	@Override
	public While createWhile() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createWhile();
		
	}
	
	@Override
	public Pick createPick() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createPick();
		
	}
	
	@Override
	public Flow createFlow() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createFlow();
		
	}
	
	@Override
	public OnAlarm createOnAlarm() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createOnAlarm();
		
	}
	
	@Override
	public Assign createAssign() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createAssign();
		
	}
	
	@Override
	public Copy createCopy() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createCopy();
		
	}
	
	@Override
	public Extension createExtension() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createExtension();
		
	}
	
	@Override
	public Scope createScope() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createScope();
		
	}
	
	@Override
	public Compensate createCompensate() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createCompensate();
		
	}
	
	@Override
	public FromParts createFromParts() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createFromParts();
		
	}
	
	@Override
	public ToParts createToParts() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createToParts();
		
	}
	
	@Override
	public CompensationHandler createCompensationHandler() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createCompensationHandler();
		
	}
	
	@Override
	public To createTo() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createTo();
		
	}
	
	@Override
	public From createFrom() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createFrom();
		
	}
	
	@Override
	public OnMessage createOnMessage() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createOnMessage();
		
	}
	
	@Override
	public Expression createExpression() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createExpression();
		
	}
	
	@Override
	public BooleanExpression createBooleanExpression() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createBooleanExpression();
		
	}
	
	@Override
	public Correlation createCorrelation() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createCorrelation();
		
	}
	
	@Override
	public EventHandler createEventHandler() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createEventHandler();
		
	}
	
	@Override
	public Source createSource() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createSource();
		
	}
	
	@Override
	public Target createTarget() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createTarget();
		
	}
	
	@Override
	public PartnerLinks createPartnerLinks() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createPartnerLinks();
		
	}
	
	@Override
	public Variables createVariables() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createVariables();
		
	}
	
	@Override
	public CorrelationSets createCorrelationSets() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createCorrelationSets();
		
	}
	
	@Override
	public Links createLinks() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createLinks();
		
	}
	
	@Override
	public CatchAll createCatchAll() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createCatchAll();
		
	}
	
	@Override
	public Correlations createCorrelations() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createCorrelations();
		
	}
	
	@Override
	public Variable createVariable() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createVariable();
		
	}
	
	@Override
	public UnknownExtensibilityAttribute createUnknownExtensibilityAttribute() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createUnknownExtensibilityAttribute();
		
	}
	
	@Override
	public OnEvent createOnEvent() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createOnEvent();
		
	}
	
	@Override
	public Import createImport() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createImport();
		
	}
	
	@Override
	public Rethrow createRethrow() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createRethrow();
		
	}
	
	@Override
	public Condition createCondition() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createCondition();
		
	}
	
	@Override
	public Targets createTargets() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createTargets();
		
	}
	
	@Override
	public Sources createSources() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createSources();
		
	}
	
	@Override
	public Query createQuery() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createQuery();
		
	}
	
	@Override
	public ServiceRef createServiceRef() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createServiceRef();
		
	}
	
	@Override
	public Exit createExit() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createExit();
		
	}
	
	@Override
	public Extensions createExtensions() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createExtensions();
		
	}
	
	@Override
	public ExtensionActivity createExtensionActivity() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createExtensionActivity();
		
	}
	
	@Override
	public FromPart createFromPart() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createFromPart();
		
	}
	
	@Override
	public ToPart createToPart() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createToPart();
		
	}
	
	@Override
	public OpaqueActivity createOpaqueActivity() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createOpaqueActivity();
		
	}
	
	@Override
	public ForEach createForEach() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createForEach();
		
	}
	
	@Override
	public RepeatUntil createRepeatUntil() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createRepeatUntil();
		
	}
	
	@Override
	public TerminationHandler createTerminationHandler() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createTerminationHandler();
		
	}
	
	@Override
	public If createIf() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createIf();
		
	}
	
	@Override
	public ElseIf createElseIf() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createElseIf();
		
	}
	
	@Override
	public Else createElse() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createElse();
		
	}
	
	@Override
	public CompletionCondition createCompletionCondition() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createCompletionCondition();
		
	}
	
	@Override
	public Branches createBranches() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createBranches();
		
	}
	
	@Override
	public BPELExtensibleElement createBPELExtensibleElement() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createBPELExtensibleElement();
		
	}
	
	@Override
	public Validate createValidate() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createValidate();
		
	}
	
	@Override
	public Documentation createDocumentation() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createDocumentation();
		
	}
	
	@Override
	public MessageExchanges createMessageExchanges() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createMessageExchanges();
		
	}
	
	@Override
	public MessageExchange createMessageExchange() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createMessageExchange();
		
	}
	
	@Override
	public CompensateScope createCompensateScope() {
		LoggingBPELFactoryImpl.logger.entry();
		return super.createCompensateScope();
		
	}
	
}
