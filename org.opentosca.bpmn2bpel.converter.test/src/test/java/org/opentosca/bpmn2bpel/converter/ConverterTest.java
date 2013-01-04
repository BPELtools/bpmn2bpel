package org.opentosca.bpmn2bpel.converter;

import org.eclipse.emf.common.util.URI;
import org.junit.Test;

public class ConverterTest {
	
	private void testProcess() {
		
	}
	
	@Test
	public void testLoopingTask() {
		URI uri = URI.createFileURI("src/test/resources/bpmn/structures-only/plain/looping-task.xml");
		System.out.println(uri.toString());
	}
}
