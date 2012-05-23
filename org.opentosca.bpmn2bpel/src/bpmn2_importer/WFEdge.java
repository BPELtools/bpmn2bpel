package bpmn2_importer;

import org.eclipse.bpmn2.SequenceFlow;
import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.DirectedGraph;
import org.jbpt.graph.abs.AbstractMultiDirectedGraph;
import org.jbpt.hypergraph.abs.Vertex;

import java.util.List;

public class WFEdge extends DirectedEdge {
	
	private SequenceFlow edge;
	// What is this graph for?
	
	
	public WFEdge(DirectedGraph g,SequenceFlow edge,WFNode sourcev,WFNode targetv){
		
		super(g, sourcev, targetv);
		this.edge = edge;
		
	}
	
	public String getName(){
	
		return edge.getName();
	}
	
	public String getId(){
		
		return edge.getId();
	}
	
	public SequenceFlow getElement(){
		
		return edge;
	}
	
	public void setElement(SequenceFlow edge){
		
		this.edge = edge;
	}
	
	public boolean iscontainedInto(List<SequenceFlow> edges){
		
		boolean iscontained = false;
		
		for(SequenceFlow e: edges){
			
			if(this.getElement().equals(e)){
				iscontained = true;
				break;
			}
			
		}
		
		return iscontained;
		
	}

}
