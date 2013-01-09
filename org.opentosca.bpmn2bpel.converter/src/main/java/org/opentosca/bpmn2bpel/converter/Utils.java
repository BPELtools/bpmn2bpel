package org.opentosca.bpmn2bpel.converter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.bpel.model.Condition;
import org.eclipse.bpmn2.Expression;
import org.eclipse.bpmn2.FormalExpression;
import org.jbpt.graph.algo.rpst.RPSTNode;
import org.jbpt.graph.algo.tctree.TCType;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

public class Utils {
	
	private static final XLogger logger = XLoggerFactory.getXLogger(Utils.class);
	
	
	/**
	 * @return We need an ArrayList as the calling method checks with the index
	 *         if a last element is a sequence is found
	 */
	static ArrayList<RPSTNode> organizeRPST(RPSTNode parent, ArrayList<RPSTNode> children) {
		String rootName = "";
		RPSTNode n = null;
		ArrayList<RPSTNode> childrenf = new ArrayList<RPSTNode>();
		Set<RPSTNode> RPSTset = null;
		RPSTNode actualNode = null;
		
		// The Parent node is analyzed, when its of type Bond the order isn't
		// important
		if (parent.getType().equals(TCType.B)) {
			return children;
		} else {
			
			// codigo nuevo****
			
			Map<String, Set<RPSTNode>> entry = new Hashtable<String, Set<RPSTNode>>();
			
			// Fill Hash
			for (Object e : children) {
				
				n = (RPSTNode) e;
				String startn = n.getEntry().getName();
				if (!entry.containsKey(startn)) {
					entry.put(startn, new HashSet<RPSTNode>());
				}
				
				entry.get(startn).add(n);
				
			}
			
			// Get the name of the entry node
			rootName = parent.getEntry().getName();
			
			// Organize the elements and add them to a list
			while (!rootName.equals(parent.getExit().getName())) {
				
				RPSTset = entry.get(rootName);
				Iterator<RPSTNode> it = RPSTset.iterator();
				
				// First of all the cyclic components (if any) are added
				while (it.hasNext()) {
					
					actualNode = it.next();
					if (rootName.equals(actualNode.getExit().getName())) {
						
						childrenf.add(actualNode);
						RPSTset.remove(actualNode);
						
					}
					
				}
				// Then the other.
				it = RPSTset.iterator();
				while (it.hasNext()) {
					
					actualNode = it.next();
					childrenf.add(actualNode);
					
				}
				
				rootName = actualNode.getExit().getName();
			}
			
			return childrenf;
			
			// codigo nuevo****
		}
		
	}
	
	private static String convertExpressionToString(Expression expression) {
		String res;
		if (expression instanceof FormalExpression) {
			Utils.logger.debug("Hit formal expression");
			FormalExpression formalExpression = (FormalExpression) expression;
			res = formalExpression.getBody();
		} else {
			Utils.logger.debug("Hit non-formal expression");
			// in non-formal expressions, the "natural language text is captured using the documentation attribute" (BPMN
			// Spec 2.0, 8.3.6)
			List<org.eclipse.bpmn2.Documentation> documentation = expression.getDocumentation();
			StringBuilder sb = new StringBuilder();
			for (org.eclipse.bpmn2.Documentation doc : documentation) {
				sb.append(doc.getText());
			}
			res = sb.toString();
			if (res.isEmpty()) {
				Utils.logger.debug("Hit empty condition. Possibly, an invalid BPMN produced by Yaoqiang BPMN Editor 2.1.16 is used.");
			}
		}
		return res;
	}
	
	/**
	 * @param expression
	 * @return null if expression does not exist or is empty
	 */
	public static Condition convertExpressionToCondition(Expression expression) {
		if (expression == null) {
			return null;
		}
		String body = Utils.convertExpressionToString(expression);
		if (body.isEmpty()) {
			return null;
		} else {
			Condition cond = BPMNProcessTree.getBPELFactory().createCondition();
			cond.setBody(body);
			return cond;
		}
	}
	
	/**
	 * Converts a BPMN expression to a BPEL expression
	 */
	public static org.eclipse.bpel.model.Expression convertExpressionToExpression(org.eclipse.bpmn2.Expression expression) {
		org.eclipse.bpel.model.Expression res;
		if (expression == null) {
			res = null;
		} else {
			res = BPMNProcessTree.getBPELFactory().createExpression();
			String body = Utils.convertExpressionToString(expression);
			res.setBody(body);
		}
		return res;
	}
	
	/**
	 * 
	 * @param wfNode the current wfNode
	 * @param activity the BPEL activity already converted based on bpmnTask,
	 *            this is MODIFIED
	 */
	public static void copyName(final WFNode wfNode, final org.eclipse.bpel.model.Activity bpelActivity) {
		String name = wfNode.getName();
		if (!name.equals("")) {
			bpelActivity.setName(name);
		}
	}
	
}
