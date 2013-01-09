package org.opentosca.bpmn2bpel.converter.test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentosca.bpmn2bpel.converter.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

//XML-Unit with JUnit4 tip by https://gist.github.com/1297630

public class ConverterTest {
	
	private static final Logger logger = LoggerFactory.getLogger(ConverterTest.class);
	
	
	// temp dir. Using Java 6. For Java 7
	// java.nio.file.Files.createTempDirectory(prefix, attrs) should be used
	// @Rule
	// public TemporaryFolder folder = new TemporaryFolder();
	
	public class FolderEmu {
		
		public File newFile(String fileName) {
			return new File("c:/temp/" + fileName);
		}
	}
	
	public FolderEmu folder = new FolderEmu();
	
	
	@BeforeClass
	public static void ignoreWhitespaces() {
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
	}
	
	private Diff processesAreIdentical(File p1, File p2) throws SAXException, IOException {
		FileReader resBpelReader = new FileReader(p1);
		FileReader bpelReader = new FileReader(p2);
		Diff myDiff = new Diff(resBpelReader, bpelReader);
		return myDiff;
	}
	
	private static File generateBPELfile(String path, String fileName, int i) {
		String bpelFilename = "src/test/resources/bpel/" + path + "/" + fileName + "-v" + Integer.toString(i) + ".bpel";
		File res = new File(bpelFilename);
		return res;
	}
	
	/**
	 * @param path the subpath in src/test/resources/bpmn
	 * @param fileName the file name without suffix
	 * 
	 * @throws IOException
	 * @throws SAXException
	 */
	private void testProcess(String path, String fileName) throws IOException, SAXException {
		String bpmnFilename = "src/test/resources/bpmn/" + path + "/" + fileName + ".xml";
		String resBpelFilename = this.folder.newFile(fileName + ".bpel").toString();
		ConverterTest.logger.debug("Temporary file: {}...", resBpelFilename);
		Converter.convert(bpmnFilename, resBpelFilename);
		File resBpelFile = new File(resBpelFilename);
		
		String toBpelFilename = "src/test/resources/bpel/" + path + "/" + fileName + ".bpel";
		File toBeBpelFile = new File(toBpelFilename);
		if (toBeBpelFile.exists()) {
			Diff myDiff = this.processesAreIdentical(resBpelFile, toBeBpelFile);
			Assert.assertTrue("XML identical " + myDiff.toString(), myDiff.identical());
		} else {
			// Multiple BPEL files
			// They are necessary, because in certain cases, the output is non-deterministic,
			// but also valid
			int i = 1;
			toBeBpelFile = ConverterTest.generateBPELfile(path, fileName, i);
			Diff myDiff;
			if (toBeBpelFile.exists()) {
				do {
					ConverterTest.logger.debug("Testing {}", toBeBpelFile);
					myDiff = this.processesAreIdentical(resBpelFile, toBeBpelFile);
					i++;
					toBeBpelFile = ConverterTest.generateBPELfile(path, fileName, i);
				} while (!myDiff.identical() && toBeBpelFile.exists());
				Assert.assertTrue("XML identical " + myDiff.toString(), myDiff.identical());
			} else {
				Assert.fail("No BPEL file to check found");
			}
		}
	}
	
	/** begin: generated by createTestClass.py **/
	
/*
	@Test
	public void testcomplete_SugarCRM_v1_sugarCRMBuildPlan() throws IOException, SAXException {
		this.testProcess("complete/SugarCRM/v1", "sugarCRMBuildPlan");
	}
	
	@Test
	public void testcomplete_SugarCRM_v2_sugarCRMBuildPlan2() throws IOException, SAXException {
		this.testProcess("complete/SugarCRM/v2", "sugarCRMBuildPlan2");
	}
	
	@Test
	public void testdata_data_1() throws IOException, SAXException {
		this.testProcess("data", "data-1");
	}
	
	@Test
	public void testdata_data_2() throws IOException, SAXException {
		this.testProcess("data", "data-2");
	}
	
	@Test
	public void testdata_data_3() throws IOException, SAXException {
		this.testProcess("data", "data-3");
	}
	
	@Test
	public void testdata_data_4() throws IOException, SAXException {
		this.testProcess("data", "data-4");
	}
	
	@Test
	public void testinvalid_process_definition_testN2() throws IOException, SAXException {
		this.testProcess("invalid-process-definition", "testN2");
	}
	
	@Test
	public void teststructures_only_Grossesbeispiel() throws IOException, SAXException {
		this.testProcess("structures-only", "Grossesbeispiel");
	}
	
	@Test
	public void teststructures_only_boundary_compensation_1() throws IOException, SAXException {
		this.testProcess("structures-only/boundary", "compensation-1");
	}
	
	@Test
	public void teststructures_only_boundary_compensation_2() throws IOException, SAXException {
		this.testProcess("structures-only/boundary", "compensation-2");
	}
	
	@Test
	public void teststructures_only_boundary_error_1() throws IOException, SAXException {
		this.testProcess("structures-only/boundary", "error-1");
	}
	
	@Test
	public void teststructures_only_boundary_error_2() throws IOException, SAXException {
		this.testProcess("structures-only/boundary", "error-2");
	}
	
	@Test
	public void teststructures_only_boundary_error_3_rejoining() throws IOException, SAXException {
		this.testProcess("structures-only/boundary", "error-3-rejoining");
	}
	
	@Test
	public void teststructures_only_boundary_error_4() throws IOException, SAXException {
		this.testProcess("structures-only/boundary", "error-4");
	}
	
	@Test
	public void teststructures_only_end_events_2_end_events() throws IOException, SAXException {
		this.testProcess("structures-only/end-events", "2-end-events");
	}
	
	@Test
	public void teststructures_only_gateways_exclusive_fork_without_join() throws IOException, SAXException {
		this.testProcess("structures-only/gateways/exclusive", "fork-without-join");
	}
	
	@Test
	public void teststructures_only_gateways_exclusive_sequence() throws IOException, SAXException {
		this.testProcess("structures-only/gateways/exclusive", "sequence");
	}
	
	@Test
	public void teststructures_only_generalized_flow_genflow_parallel_only_1() throws IOException, SAXException {
		this.testProcess("structures-only/generalized-flow", "genflow-parallel-only-1");
	}
	
	@Test
	public void teststructures_only_generalized_flow_genflow_parallel_only_2() throws IOException, SAXException {
		this.testProcess("structures-only/generalized-flow", "genflow-parallel-only-2");
	}
	
	@Test
	public void teststructures_only_generalized_flow_genflow_parallel_only_3() throws IOException, SAXException {
		this.testProcess("structures-only/generalized-flow", "genflow-parallel-only-3");
	}
	
	@Test
	public void teststructures_only_generalized_flow_genflow_parallel_only_4() throws IOException, SAXException {
		this.testProcess("structures-only/generalized-flow", "genflow-parallel-only-4");
	}
	
	@Test
	public void teststructures_only_generalized_flow_genflow_parallel_only_5() throws IOException, SAXException {
		this.testProcess("structures-only/generalized-flow", "genflow-parallel-only-5");
	}
	
	@Test
	public void teststructures_only_generalized_flow_genflow_parallel_with_subprocess() throws IOException, SAXException {
		this.testProcess("structures-only/generalized-flow", "genflow-parallel-with-subprocess");
	}
	
	@Test
	public void teststructures_only_loops_repeat_unti_with_inner_if() throws IOException, SAXException {
		this.testProcess("structures-only/loops", "repeat-unti-with-inner-if");
	}
	
	@Test
	public void teststructures_only_loops_repeat_until_with_while() throws IOException, SAXException {
		this.testProcess("structures-only/loops", "repeat-until-with-while");
	}
	
	@Test
	public void teststructures_only_loops_repeat_until() throws IOException, SAXException {
		this.testProcess("structures-only/loops", "repeat-until");
	}
	
	@Test
	public void teststructures_only_loops_while_1() throws IOException, SAXException {
		this.testProcess("structures-only/loops", "while-1");
	}
	
	@Test
	public void teststructures_only_loops_while_2() throws IOException, SAXException {
		this.testProcess("structures-only/loops", "while-2");
	}
	
	@Test
	public void teststructures_only_loops_while_3_with_if() throws IOException, SAXException {
		this.testProcess("structures-only/loops", "while-3-with-if");
	}
	
	@Test
	public void teststructures_only_loops_while_4_with_repeat() throws IOException, SAXException {
		this.testProcess("structures-only/loops", "while-4-with-repeat");
	}
	// */
	
	@Test
	public void teststructures_only_plain_business_rule() throws IOException, SAXException {
		this.testProcess("structures-only/plain", "business-rule");
	}
	
	@Test
	public void teststructures_only_plain_parallel_receive_task_1() throws IOException, SAXException {
		this.testProcess("structures-only/plain", "parallel-receive-task-1");
	}
	
	@Test
	public void teststructures_only_plain_parallel_receive_task_2() throws IOException, SAXException {
		this.testProcess("structures-only/plain", "parallel-receive-task-2");
	}
	
	@Test
	public void teststructures_only_plain_task_loop_test_after() throws IOException, SAXException {
		this.testProcess("structures-only/plain", "task-loop-test-after");
	}
	
	@Test
	public void teststructures_only_quasi_exclusive_gateways_only_1() throws IOException, SAXException {
		this.testProcess("structures-only/quasi", "exclusive-gateways-only-1");
	}
	
	@Test
	public void teststructures_only_quasi_exclusive_gateways_only_2() throws IOException, SAXException {
		this.testProcess("structures-only/quasi", "exclusive-gateways-only-2");
	}
	
	@Test
	public void teststructures_only_quasi_parallel_gateways_only() throws IOException, SAXException {
		this.testProcess("structures-only/quasi", "parallel-gateways-only");
	}
	// */
	
	/*
	@Test
	public void teststructures_only_sequence_non_interrupting_events() throws IOException, SAXException {
		this.testProcess("structures-only/sequence", "non-interrupting-events");
	}
	
	@Test
	public void teststructures_only_sequence_one_task() throws IOException, SAXException {
		this.testProcess("structures-only/sequence", "one-task");
	}
	
	@Test
	public void teststructures_only_start_events_one_task_without_incoming_sequence_flow() throws IOException, SAXException {
		this.testProcess("structures-only/start-events", "one task without incoming sequence flow");
	}
	
	@Test
	public void teststructures_only_structured_no_gateways() throws IOException, SAXException {
		this.testProcess("structures-only/structured", "no-gateways");
	}
	
	@Test
	public void teststructures_only_structured_pick() throws IOException, SAXException {
		this.testProcess("structures-only/structured", "pick");
	}
	
	@Test
	public void teststructures_only_subprocess_subprocess_collapsed() throws IOException, SAXException {
		this.testProcess("structures-only/subprocess", "subprocess-collapsed");
	}
	
	@Test
	public void teststructures_only_subprocess_subprocess_nested_with_error_handling() throws IOException, SAXException {
		this.testProcess("structures-only/subprocess", "subprocess-nested-with-error-handling");
	}
	
	@Test
	public void testWSDL_service_task_service_task() throws IOException, SAXException {
		this.testProcess("WSDL/service-task", "service-task");
	}
	// */
	/** end: generated by createTestClass.py **/
	
}
