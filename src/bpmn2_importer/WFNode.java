package bpmn2_importer;

import org.eclipse.bpmn2.FlowNode;

import org.jbpt.hypergraph.abs.Vertex;

public class WFNode extends Vertex {
	
	// Properties of the Node
	private FlowNode elem;
	private String id;
	private boolean visited = false;
	
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
	
	public boolean isvisited(){
		
		return visited;
	}
	
	public boolean visited(){
		
		visited = true;
		return visited;
	}

}
