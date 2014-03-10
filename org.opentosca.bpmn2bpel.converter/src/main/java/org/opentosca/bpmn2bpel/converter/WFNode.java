package org.opentosca.bpmn2bpel.converter;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.bpmn2.Expression;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.impl.ExclusiveGatewayImpl;
import org.jbpt.graph.abs.AbstractDirectedEdge;
import org.jbpt.graph.abs.AbstractDirectedGraph;
import org.jbpt.graph.algo.rpst.RPSTNode;
import org.jbpt.graph.algo.tctree.TCType;
import org.jbpt.hypergraph.abs.Vertex;

public class WFNode extends Vertex {
	
	// Attributes of the Node
	private FlowNode elem;
	// private String id;
	private boolean visited;
	private BPMNProcessTree SubprocessTree;
	private List<BPMNProcessTree> BoundaryEventGraphs;
	private boolean mainFlow;
	
	private static int nameCount = 0;
	
	
	/**
	 * Returns a name even if getName() returns null;
	 *
	 * SIDE EFFECT: Sets the name at the element
	 */
	public static String getName(FlowNode e) {
		String name = e.getName();
		if (name == null) {
			name = "Name_" + Integer.toString(++WFNode.nameCount);
			e.setName(name);
		}
		return name;
	}
	
	public WFNode(FlowNode son) {
		super(WFNode.getName(son), "");
		this.elem = son;
		this.SubprocessTree = null;
		this.visited = false;
		this.mainFlow = false;
		this.BoundaryEventGraphs = new LinkedList<BPMNProcessTree>();
	}
	
	@Override
	public String getId() {
		return this.elem.getId();
	}
	
	@Override
	public String getName() {
		return super.getName();
	}
	
	public FlowNode getElement() {
		return this.elem;
		
	}
	
	public boolean isVisited() {
		return this.visited;
	}
	
	/**
	 * @eturn true if the node of the BPMNProcessTree belongs to the main flow
	 * Otherwise (if it belongs to an exception flow) false is returned
	 */
	public boolean isMainFlow() {
		return this.mainFlow;
	}
	
	public BPMNProcessTree getSubprocessTree() {
		return this.SubprocessTree;
		
	}
	
	public List<BPMNProcessTree> getBoundaryEventFlows() {
		return this.BoundaryEventGraphs;
	}
	
	public void addBoundaryEvent(BPMNProcessTree EventFlow) {
		this.BoundaryEventGraphs.add(EventFlow);
		
	}
	
	public void setSubProcessTree(BPMNProcessTree subprocessT) {
		this.SubprocessTree = subprocessT;
		
	}
	
	public void setVisited() {
		this.visited = true;
	}
	
	public void setMainFlow() {
		this.mainFlow = true;
	}
	
	// Gets a List of the edges that belong to the component whose entry node is
	// .this
	public boolean isCyclic(Collection<RPSTNode> BondChildren, WFNode node2) {
		boolean iscyclic = false;
		
		for (Object e : BondChildren) {
			RPSTNode poly = (RPSTNode) e;
			if (poly.getEntry().equals(node2) && poly.getExit().equals(this)) {
				iscyclic = true;
				break;
			}
		}
		
		return iscyclic;
		
	}
	
	// Obtain the condition of the polygon where entry.next equals the polygons
	// entry.next
	public Expression getConditionOfPolygon(AbstractDirectedGraph graph) {
		
		Collection<AbstractDirectedEdge> edge = graph.getEdgesWithSource(this);
		Iterator<AbstractDirectedEdge> it = edge.iterator();
		Expression expr = null;
		ExclusiveGatewayImpl gate = (ExclusiveGatewayImpl) this.getElement();
		
		while (it.hasNext()) {
			AbstractDirectedEdge e = it.next();
			WFNode nodeT = (WFNode) e.getTarget();
			for (SequenceFlow flow : nodeT.getElement().getIncoming()) {
				if (flow.getSourceRef().equals(this.getElement()) && !flow.equals(gate.getDefault())) {
					expr = flow.getConditionExpression();
				}
			}
		}
		
		return expr;
		
	}
	
	public SequenceFlow getSourceOfPolygon(AbstractDirectedGraph graph) {
		
		Collection<AbstractDirectedEdge> edge = graph.getEdgesWithSource(this);
		Iterator<AbstractDirectedEdge> it = edge.iterator();
		Expression expr = null;
		SequenceFlow f2 = null;
		
		while (it.hasNext()) {
			AbstractDirectedEdge e = it.next();
			WFNode nodeT = (WFNode) e.getTarget();
			for (SequenceFlow flow : nodeT.getElement().getIncoming()) {
				
				if (flow.getSourceRef().equals(this.getElement())) {
					return flow;
				}
			}
			
		}
		return f2;
		
	}
	
	public SequenceFlow getTargetOfPolygon(AbstractDirectedGraph graph) {
		
		Collection<AbstractDirectedEdge> edge = graph.getEdgesWithTarget(this);
		Iterator<AbstractDirectedEdge> it = edge.iterator();
		Expression expr = null;
		SequenceFlow f2 = null;
		
		while (it.hasNext()) {
			AbstractDirectedEdge e = it.next();
			WFNode nodeT = (WFNode) e.getSource();
			for (SequenceFlow flow : nodeT.getElement().getOutgoing()) {
				
				if (flow.getTargetRef().equals(this.getElement())) {
					return flow;
				}
			}
			
		}
		return f2;
	}
	
	// Get the number of paths that go from .this to node2
	public int numberOfPathsto(Collection<RPSTNode> BondChildren, WFNode node2) {
		
		int paths = 0;
		
		for (Object e : BondChildren) {
			
			RPSTNode poly = (RPSTNode) e;
			
			if (poly.getEntry().equals(this) && poly.getExit().equals(node2)) {
				
				if (poly.getType().equals(TCType.P) || poly.getType().equals(TCType.T)) {
					paths++;
				} else if (poly.getType().equals(TCType.B)) {
					// If the component is a Bond, then it has at least 2
					// different paths.
					paths = paths + 2;
				}
			}
			
		}
		
		return paths;
		
	}
	
	// Get the number of paths that go directly from .this to node2 (no
	// intermediates)
	public int numberOfEdgesto(Collection<RPSTNode> BondChildren, WFNode node2) {
		int paths = 0;
		
		for (Object e : BondChildren) {
			RPSTNode poly = (RPSTNode) e;
			if (poly.getType().equals(TCType.T) && poly.getEntry().equals(this) && poly.getExit().equals(node2)) {
				paths++;
			}
		}
		
		return paths;
		
	}
	
	//
	public boolean isBasicStartEvent() {
		FlowNode n = this.getElement();
		
		if (n instanceof StartEvent) {
			StartEvent st = (StartEvent) n;
			if (st.getEventDefinitions().size() == 0) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
		
	}
	
}
