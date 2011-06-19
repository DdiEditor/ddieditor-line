package dk.dda.ddieditor.line.util;

import java.io.File;

import org.junit.Test;

public class Wiki2Ddi3ScannerTest {
	@Test
	public void processLine() throws Exception {
		Wiki2Ddi3Scanner scanner = new Wiki2Ddi3Scanner(new Ddi3Helper());
		scanner.startScanning(new File("resources/easy.txt"), true);
		scanner.getDdi3Helper().resultToString();
	}
}
