package bpmn2_importer;

import org.eclipse.bpmn2.SequenceFlow;
import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.abs.AbstractDirectedEdge;
import org.jbpt.graph.abs.AbstractMultiDirectedGraph;

public class WFEdge extends DirectedEdge  {
	
	private SequenceFlow edge;
	// What is this graph for?
	
	
	public WFEdge(SequenceFlow edge,WFNode sourcev,WFNode targetv){
		
		super(null, sourcev, targetv);
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

}
