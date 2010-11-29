package dk.dda.ddieditor.line.controler;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class FileScannerTest {
	@Before
	public void cleanUp() {
		File result = new File("testresult");
		if (result.exists()) {
			result.delete();
		}
		result.mkdir();
	}
	
	@Test
	public void testname() throws Exception {
		FileScanner fs = new FileScanner();
		fs.startScanning(new File("resources/spr-in.txt"), new File(
				"test/spr-out"), null, null, null);
		fs.resultToString();
	}
}
