package de.uni_stuttgart.iaas.bpmn.model.utilities;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.ComplexGateway;
import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.InclusiveGateway;
import org.eclipse.bpmn2.SequenceFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BPMNutils {
	
	private static final Logger logger = LoggerFactory.getLogger(BPMNutils.class);
	
	public static boolean isDefault(SequenceFlow sequenceFlow) {
		FlowNode source = sequenceFlow.getSourceRef();
		SequenceFlow defaultFlow;
		if (source instanceof Activity) {
			defaultFlow = ((Activity) source).getDefault();
		} else if (source instanceof ComplexGateway) {
			defaultFlow = ((ComplexGateway) source).getDefault();
		} else if (source instanceof ExclusiveGateway) {
			defaultFlow = ((ExclusiveGateway) source).getDefault();
		} else if (source instanceof InclusiveGateway) {
			defaultFlow = ((InclusiveGateway) source).getDefault();
		} else {
			BPMNutils.logger.error("Unhandled instance {}", source.getClass().toString());
			defaultFlow = null;
		}
		boolean res = sequenceFlow.equals(defaultFlow);
		return res;
	}
	
}
