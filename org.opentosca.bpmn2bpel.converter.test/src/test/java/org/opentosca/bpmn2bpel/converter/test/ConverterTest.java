package org.opentosca.bpmn2bpel.converter.test;

import java.io.IOException;

import org.junit.Test;
import org.opentosca.bpmn2bpel.converter.Converter;

public class ConverterTest {
	
	private void testProcess() {
		
	}
	
	@Test
	public void testLoopingTask() throws IOException {
		Converter.convert("src/test/resources/bpmn/structures-only/plain/looping-task.xml", "C:/git-repositories/BPMN2BPEL/results/Result-bpel.xml");
	}
}
