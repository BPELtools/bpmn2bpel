package bpmn2_importer;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.bpel.model.Source;
import org.eclipse.bpmn2.Expression;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.impl.ExclusiveGatewayImpl;
import org.eclipse.bpmn2.impl.ExpressionImpl;
import org.eclipse.bpmn2.impl.GatewayImpl;

import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.DirectedGraph;
import org.jbpt.graph.abs.AbstractDirectedEdge;
import org.jbpt.graph.abs.AbstractDirectedGraph;
import org.jbpt.graph.algo.rpst.RPSTNode;
import org.jbpt.graph.algo.tctree.TCType;
import org.jbpt.hypergraph.abs.Vertex;

public class WFNode extends Vertex {
	
	// Properties of the Node
	private FlowNode elem;
	private String id;
	private String nome;
	private BPMNProcessTree SubprocessTree;
	
	
	public WFNode(FlowNode son){
		
		super(son.getName(),"");
		this.elem = son;
		this.SubprocessTree = null;
		
	}
	
	public WFNode(){
		
		super("","");
	}
	
	public String getId(){
		
		return elem.getId();
	}
	
	public String getName(){
		
		return elem.getName();
	}
	
	public FlowNode getElement(){
		
		return elem;
		
	}
	
	public BPMNProcessTree getSubprocessTree(){
		
		return SubprocessTree;
		
	}
	
	public void setSubProcessTree(BPMNProcessTree subprocessT){
		
		this.SubprocessTree = subprocessT;
		
	}
	
	// Gets a List of the edges that belong to the component whose entry node is .this 
	public boolean isCyclic(Collection<RPSTNode> BondChildren, WFNode node2){
		
		boolean iscyclic = false;
		
		for(Object e: BondChildren){
			
			RPSTNode poly = (RPSTNode) e;
			if(poly.getEntry().equals(node2) && poly.getExit().equals(this)){
				
				iscyclic = true;
				break;
			}
			
		}
		
		return iscyclic;
		
	}
	
	// Obtain the condition of the polygon where entry.next equals the polygons entry.next
	public Expression getConditionOfPolygon(AbstractDirectedGraph graph){
		
		Collection<AbstractDirectedEdge> edge = graph.getEdgesWithSource((WFNode) this);
		Iterator<AbstractDirectedEdge> it = edge.iterator();
		Expression expr = null;
		ExclusiveGatewayImpl gate = (ExclusiveGatewayImpl) this.getElement();
		
		while(it.hasNext()){
			AbstractDirectedEdge e = (AbstractDirectedEdge) it.next();
			WFNode nodeT = (WFNode) e.getTarget();
			for(SequenceFlow flow : nodeT.getElement().getIncoming()){
				
				if(flow.getSourceRef().equals(this.getElement()) && !flow.equals(gate.getDefault())){
					expr = (Expression) flow.getConditionExpression();
				}
			}
		}
		
		return expr;
		
	}
	
	public SequenceFlow getSourceOfPolygon(AbstractDirectedGraph graph){
		
		Collection<AbstractDirectedEdge> edge = graph.getEdgesWithSource((WFNode) this);
		Iterator<AbstractDirectedEdge> it = edge.iterator();
		Expression expr = null;
		SequenceFlow f2 = null;
		ExclusiveGatewayImpl gate = (ExclusiveGatewayImpl) this.getElement();
		
		while(it.hasNext()){
			AbstractDirectedEdge e = (AbstractDirectedEdge) it.next();
			WFNode nodeT = (WFNode) e.getTarget();
			for(SequenceFlow flow : nodeT.getElement().getIncoming()){
				
				if(flow.getSourceRef().equals(this.getElement()) && !flow.equals(gate.getDefault())){
					return flow;
				}
			}
			
			
		}
		return f2;
		
	}

	public SequenceFlow getTargetOfPolygon(AbstractDirectedGraph graph) {
		
		Collection<AbstractDirectedEdge> edge = graph.getEdgesWithTarget((WFNode) this);
		Iterator<AbstractDirectedEdge> it = edge.iterator();
		Expression expr = null;
		SequenceFlow f2 = null;
		ExclusiveGatewayImpl gate = (ExclusiveGatewayImpl) this.getElement();
		
		while(it.hasNext()){
			AbstractDirectedEdge e = (AbstractDirectedEdge) it.next();
			WFNode nodeT = (WFNode) e.getSource();
			for(SequenceFlow flow : nodeT.getElement().getOutgoing()){
				
				if(flow.getTargetRef().equals(this.getElement())){
					return flow;
				}
			}
			
			
		}
		return f2;
	}

	// Get the number of paths that go from .this to node2
	public int numberOfPathsto(Collection<RPSTNode> BondChildren, WFNode node2) {
		
		int paths = 0;
		
		for(Object e : BondChildren){
			
			RPSTNode poly = (RPSTNode) e;
			
			if(poly.getEntry().equals(this) && poly.getExit().equals(node2)){
				
				if(poly.getType().equals(TCType.P) || poly.getType().equals(TCType.T)){
					paths++;
				}
				else if(poly.getType().equals(TCType.B)){
					// If the component is a Bond, then it has at least 2 different paths.
					paths = paths + 2;
				}
			}
			
		}
		
		
		return paths;
		
	}
	
	// Get the number of paths that go directly from .this to node2 (no intermediates)
	public int numberOfEdgesto(Collection<RPSTNode> BondChildren, WFNode node2){
		
		int paths = 0;
		
		for(Object e : BondChildren){
			
			RPSTNode poly = (RPSTNode) e;
			
			if(poly.getType().equals(TCType.T) && poly.getEntry().equals(this) && poly.getExit().equals(node2)){
				
				paths++;
			}
			
		}
		
		
		return paths;
		
	}

}
