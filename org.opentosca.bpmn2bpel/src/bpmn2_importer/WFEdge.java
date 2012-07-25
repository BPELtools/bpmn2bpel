package bpmn2_importer;

import java.util.List;

import org.eclipse.bpmn2.SequenceFlow;
import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.DirectedGraph;

public class WFEdge extends DirectedEdge {
	
	private SequenceFlow edge;
	
	
	// What is this graph for?
	
	public WFEdge(DirectedGraph g, SequenceFlow edge, WFNode sourcev, WFNode targetv) {
		
		super(g, sourcev, targetv);
		this.edge = edge;
		
	}
	
	@Override
	public String getName() {
		
		return this.edge.getName();
	}
	
	@Override
	public String getId() {
		
		return this.edge.getId();
	}
	
	public SequenceFlow getElement() {
		
		return this.edge;
	}
	
	public void setElement(SequenceFlow edge) {
		
		this.edge = edge;
	}
	
	public boolean iscontainedInto(List<SequenceFlow> edges) {
		
		boolean iscontained = false;
		
		for (SequenceFlow e : edges) {
			
			if (this.getElement().equals(e)) {
				iscontained = true;
				break;
			}
			
		}
		
		return iscontained;
		
	}
	
}
