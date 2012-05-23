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
import org.jbpt.hypergraph.abs.Vertex;

public class WFNode extends Vertex {
	
	// Properties of the Node
	private FlowNode elem;
	private String id;
	
	public WFNode(FlowNode son){
		
		super(son.getName(),"");
		this.elem = son;
		
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
	
	// Gets a List of the edges that belong to the component whose entry node is .this 
	public boolean isCyclic(Collection<AbstractDirectedEdge> fragmEdges){
		
		boolean iscyclic = false;
		
		Iterator<AbstractDirectedEdge> i = fragmEdges.iterator();
		while (i.hasNext()) {
			AbstractDirectedEdge e = (AbstractDirectedEdge) i.next();
			WFNode nodeT = (WFNode) e.getTarget();
			if(nodeT.equals(this)){
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

	public int numberOfEdgesTo(AbstractDirectedGraph graph, WFNode node2) {
		
		Collection<AbstractDirectedEdge> edge = graph.getEdgesWithSource(this);
		Iterator<AbstractDirectedEdge> it = edge.iterator();
		int i = 0;
		
		while(it.hasNext()){
			AbstractDirectedEdge e = (AbstractDirectedEdge) it.next();
			WFNode nodeS = (WFNode) e.getSource();
				
			if(e.getTarget().equals(node2)){
				i++;
			}
			
		}
		
		
		return i;
	}

}
