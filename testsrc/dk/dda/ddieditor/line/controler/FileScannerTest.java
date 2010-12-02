package dk.dda.ddieditor.line.controler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.jamwiki.parser.ParserException;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.bliki.BlikiParser;
import org.jamwiki.parser.jflex.JFlexParser;
import org.junit.Before;
import org.junit.Ignore;
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

	@Ignore
	public void testname() throws Exception {
		FileScanner fs = new FileScanner();
		fs.startScanning(new File("resources/spr-in.txt"));
		fs.resultToString();
	}

	@Test
	public void testname2() throws Exception {
		// parse wiki text
		int lineNo = 0;
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File("resources/easy.txt"), "utf-8");
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		StringBuilder wiki = new StringBuilder();
		while (scanner.hasNextLine()) {
			++lineNo;
			wiki.append(scanner.nextLine()+System.getProperty("line.separator"));
		}
		scanner.close();

		ParserInput parserInput = new ParserInput();
		parserInput.setTopicName("");
		parserInput.setVirtualWiki("");
		parserInput.setContext("");
		JFlexParser parser = new JFlexParser(parserInput);
		String html = parser.parseHTML(new ParserOutput(), wiki.toString());
		System.out.println(html);
		// System.out
		// .println(blikiParser.parseHTML(parserOutput, wiki.toString()));
	}
}
