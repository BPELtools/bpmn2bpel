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
import org.xml.sax.SAXException;

//XML-Unit with JUnit4 tip by https://gist.github.com/1297630

public class ConverterTest {
	
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
	
	/**
	 * @param path the subpath in src/test/resources/bpmn
	 * @param fileName the file name without suffix
	 * 
	 * @throws IOException
	 * @throws SAXException
	 */
	private void testProcess(String path, String fileName) throws IOException, SAXException {
		String bpmnFile = "src/test/resources/bpmn/" + path + "/" + fileName + ".xml";
		String resBpelFile = this.folder.newFile(fileName + ".bpel").toString();
		System.out.println(resBpelFile);
		Converter.convert(bpmnFile, resBpelFile);
		String bpelFile = "src/test/resources/bpel/" + path + "/" + fileName + ".bpel";
		FileReader resBpelReader = new FileReader(new File(resBpelFile));
		FileReader bpelReader = new FileReader(new File(bpelFile));
		Diff myDiff = new Diff(resBpelReader, bpelReader);
		Assert.assertTrue("XML identical " + myDiff.toString(), myDiff.identical());
	}
	
	@Test
	public void testLoopingTask() throws IOException, SAXException {
		this.testProcess("structures-only/plain", "task-loop-test-after");
	}
}
