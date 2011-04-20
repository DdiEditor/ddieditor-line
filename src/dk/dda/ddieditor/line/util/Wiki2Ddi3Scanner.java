package dk.dda.ddieditor.line.util;

import java.io.File;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;

public class Wiki2Ddi3Scanner {
	static private Log log = LogFactory.getLog(LogType.SYSTEM,
			Wiki2Ddi3Scanner.class);
	Scanner scanner = null;
	int lineNo = 0;

	Ddi3Helper ddi3Helper = null;
	final static String MQUE_END = "end";

	public Wiki2Ddi3Scanner(Ddi3Helper ddi3Helper) {
		this.ddi3Helper = ddi3Helper;
	}

	public Ddi3Helper getDdi3Helper() {
		return ddi3Helper;
	}

	public void setDdi3Helper(Ddi3Helper ddi3Helper) {
		this.ddi3Helper = ddi3Helper;
	}

	/**
	 * Scan file
	 * 
	 * @param file
	 *            file to scan
	 * @throws Exception
	 */
	public void startScanning(File file) throws Exception {
		scanner = new Scanner(file, "utf-8");
		startScanning();
	}

	/**
	 * Scan string
	 * 
	 * @param content
	 *            string content
	 * @throws Exception
	 */
	public void startScanning(String content) throws Exception {
		scanner = new Scanner(content);
		startScanning();
	}

	private void startScanning() throws Exception {
		String current = null;
		while (scanner.hasNextLine()) {
			++lineNo;
			current = scanner.nextLine();
			processLine(current);
		}
		scanner.close();

		// update refs
		ddi3Helper.postResolve();
	}

	Pattern variNamePattern = Pattern.compile("[vV][1-9]+[0-9]?");
	
	Pattern univPattern = Pattern.compile("[=]{1}.+[=]{1}");
	Pattern quesPattern = Pattern.compile("[=]{2}.+[=]{2}");
	Pattern queiPattern = Pattern.compile("\\*+ ?[vV][1-9]++");
	Pattern mquePattern = Pattern.compile("\\* ?[']{3}.+[']{3}");
	Pattern catePattern = Pattern.compile("\\*+ ?");
	
	String compMatch = "'''''comp'''''";
	String stateMatch = "'''''state'''''";
	String ifThenElseMatch = "'''''ifthenelse'''''";

	public void processLine(String line) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug(lineNo + " - " + line);
		}

		// check for empty input
		if (line == null || line.equals("")) {
			return;
		}

		// weed out comments
		String[] commentWeed = { "<!--", };
		for (int i = 0; i < commentWeed.length; i++) {
			if (line.indexOf(commentWeed[i]) > -1) {
				return;
			}
		}

		if (difineLine(line, quesPattern)) {
			createQuestionScheme(line);
			return;
		}
		if (difineLine(line, univPattern)) {
			createUniverse(line);
			return;
		}
		if (difineLine(line, queiPattern)) {
			createQuestion(line);
			return;
		}
		if (difineLine(line, mquePattern)) {
			createMultipleQuestion(line);
			return;
		}
		if (difineLine(line, catePattern)) {
			createCategory(line);
			return;
		}
		if (line.indexOf(ifThenElseMatch) > -1) {
			createIfThenElse(line);
			return;
		}
		if (line.indexOf(compMatch) > -1) {
			createComputationItem(line);
			return;
		}
		if (line.indexOf(stateMatch) > -1) {
			createStatementItem(line);
			return;
		}		
		
		return;
	}

	private boolean difineLine(String line, Pattern pattern) {
		return pattern.matcher(line).find();
	}

	/**
	 * Create universe
	 * 
	 * @param line
	 *            of '=universe scheme label=universe scheme description'
	 * @throws DDIFtpException
	 */
	private void createUniverse(String line) throws DDIFtpException {
		String label = null;
		int index = line.indexOf("=", 1);
		if (index > -1) {
			label = line.substring(1, index);
		}

		String description = null;
		if (!(line.substring(index + 1).equals(""))) {
			description = line.substring(index + 1);
		}
		ddi3Helper.createUniverse(label, description);
	}

	/**
	 * Create question scheme
	 * 
	 * @param line
	 *            of '==questionscheme label==questionscheme description'
	 * @throws DDIFtpException
	 */
	private void createQuestionScheme(String line) throws DDIFtpException {
		// label
		String label = null;
		int index = line.indexOf("==", 2);
		if (index > -1) {
			label = line.substring(2, index);
		}

		// description
		String description = null;
		if (!(line.substring(index + 2).equals(""))) {
			description = line.substring(index + 2);
		}
		ddi3Helper.createQuestionScheme(label, description);
	}

	/**
	 * Create question item
	 * 
	 * @param line
	 *            of '* v1 question item text here'
	 * @throws DDIFtpException
	 */
	private void createQuestion(String line) throws DDIFtpException {
		String no = "";
		Matcher matcher = variNamePattern.matcher(line);
		matcher.find();
		
		no = "V"+line.substring(matcher.start()+1, matcher.end());
		String text = line.substring(matcher.end()).trim();
		ddi3Helper.createQuestion(no, text);
	}

	/**
	 * Create multiple question item
	 * 
	 * @param line
	 *            of '* '''Multiple Question text''''
	 */
	private void createMultipleQuestion(String line) {
		String text = null;
		int index = line.indexOf("'''");
		if (index > -1) {
			int end = line.indexOf("'''", index + 3);
			if (end > -1) {
				text = line.substring(index + 3, end);
			}
		}
		if (text != null) {
			// unset mque
			if (text.equals(MQUE_END)) {
				ddi3Helper.unsetMultipleQuestion();
				return;
			}
			try {
				ddi3Helper.createMultipleQuestion(text);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Create category
	 * 
	 * @param line
	 *            of '** category'
	 * @throws DDIFtpException
	 */
	private void createCategory(String line) throws DDIFtpException {
		Matcher matcher = catePattern.matcher(line);
		if (matcher.find()) {
			int index = matcher.end();
			ddi3Helper.createCategory(line.substring(index));
		}
	}

	/**
	 * Create if then else control construct<br>
	 * When creating an if then else control construct the following ddi3
	 * elements are being created:
	 * <ul>
	 * <li>A universe for the split population</li>
	 * <li>A sequence containing the statement item and the then question
	 * reference</li>
	 * <li>The sequence is added to the main sequence</li>
	 * </ul>
	 * 
	 * @param line
	 *            of '''''ifthenelse''''' >2 v6 v2 How many times a day?
	 * @throws Exception
	 */
	private void createIfThenElse(String line) throws Exception {
		String params[] = line.split(" ");

		// statement
		StringBuilder text = new StringBuilder();
		for (int i = 4; i < params.length; i++) {
			text.append(params[i]);
			text.append(" ");
		}

		// condition
		// params[1];

		// then
		if (variNamePattern.matcher(params[2]).find()) {
			params[2] = "V" + params[2].substring(1);
		}

		// else
		// params[3]
		if (params[3].equals("na")) {
			params[3] = null;
		} else if (variNamePattern.matcher(params[3]).find()) {
			params[3] = "V" + params[3].substring(1);
		}

		ddi3Helper.createIfThenElse(params[1], params[2], params[3], text
				.toString().trim());
	}

	private void createStatementItem(String line) throws Exception {
		int index = line.indexOf(stateMatch);
		String result = line.substring(index+stateMatch.length());
		ddi3Helper.createStatementItem(result.trim());
	}

	private void createComputationItem(String line) throws Exception {
		String params[] = line.split(" ");
		
		// varref params[1]		
		// code params[2]
		// label params[3]
		// statement
		StringBuilder text = new StringBuilder();
		for (int i = 3; i < params.length; i++) {
			text.append(params[i]);
			text.append(" ");
		}
		
		ddi3Helper.createComputationItem(params[1], params[2], text.toString());
	}
}
