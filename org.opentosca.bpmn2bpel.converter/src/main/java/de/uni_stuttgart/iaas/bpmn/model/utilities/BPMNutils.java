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
		boolean res;
		if (source instanceof Activity) {
			res = ((Activity) source).getDefault().equals(sequenceFlow);
		} else if (source instanceof ComplexGateway) {
			res = ((ComplexGateway) source).getDefault().equals(sequenceFlow);
		} else if (source instanceof ExclusiveGateway) {
			res = ((ExclusiveGateway) source).getDefault().equals(sequenceFlow);
		} else if (source instanceof InclusiveGateway) {
			res = ((InclusiveGateway) source).getDefault().equals(sequenceFlow);
		} else {
			BPMNutils.logger.error("Unhandled instance {}", source.getClass().toString());
			res = false;
		}
		return res;
	}
	
}
