package dk.dda.ddieditor.line.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ddialliance.ddieditor.ui.model.ElementType;
import org.ddialliance.ddieditor.ui.model.instrument.ConditionalUtil;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.Translator;
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

	public Wiki2Ddi3Scanner() {
		// default ;- )
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
	public void startScanning(File file, boolean create) throws Exception {
		scanner = new Scanner(file, "utf-8");
		startScanning(create);
	}

	/**
	 * Scan string
	 * 
	 * @param content
	 *            string content
	 * @throws Exception
	 */
	public void startScanning(String content, boolean create) throws Exception {
		scanner = new Scanner(content);
		startScanning(create);
	}

	private void startScanning(boolean create) throws Exception {
		errorList.clear();
		String current = null;
		while (scanner.hasNextLine()) {
			++lineNo;
			current = scanner.nextLine();
			processLine(current, create);
		}
		scanner.close();

		// update refs
		if (create) {
			ddi3Helper.postResolve();
		}
	}

	Pattern variNamePattern = Pattern.compile("[vV][1-9]+[0-9]*");

	Pattern univPattern = Pattern.compile("^[=]{1}.+[=]{1}");
	Pattern quesPattern = Pattern.compile("^[=]{2}.+[=]{2}");
	Pattern queiPattern = Pattern.compile("^\\*+ ?[vV][1-9]++");
	Pattern mquePattern = Pattern.compile("^'{3}.+'{3}");
	Pattern catePattern = Pattern.compile("^\\*{2} ?");
	Pattern cateReusePattern = Pattern.compile("^\\*{2} ?[vV][1-9]+[0-9]*");
	Pattern conditionPattern = Pattern
			.compile(ConditionalUtil.conditionalPattern);

	String compMatch = "''comp''";
	String stateMatch = "''state''";
	String ifThenElseMatch = "''ifthenelse''";

	public List<String> errorList = new ArrayList<String>();

	protected void processLine(String line, boolean create) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug(lineNo + " - " + line);
		}
		ddi3Helper.setLineNo(lineNo);

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

		if (defineLine(line, quesPattern)) {
			if (create) {
				createQuestionScheme(line);
			}
			return;
		}
		if (defineLine(line, univPattern)) {
			if (create)
				createUniverse(line);
			return;
		}

		if (defineLine(line.trim(), cateReusePattern)) {
			if (create)
				reuseCategory(line);
			return;
		}
		if (defineLine(line, catePattern)) {
			if (create)
				createCategory(line);
			return;
		}
		if (defineLine(line, queiPattern)) {
			if (create)
				createQuestion(line);
			return;
		}
		if (line.indexOf(ifThenElseMatch) > -1) {
			if (create) {
				createIfThenElse(line, true);
			} else {
				if (!validateExpression(line.split(" ")[1], conditionPattern)) {
					errorList.add(Translator.trans(
							"line.parse.errorifthenelse.condition", line));
				}
			}
			return;
		}
		if (line.indexOf(compMatch) > -1) {
			if (create)
				createComputationItem(line);
			return;
		}
		if (line.indexOf(stateMatch) > -1) {
			if (create)
				createStatementItem(line);
			return;
		}
		if (defineLine(line, mquePattern)) {
			if (create)
				createMultipleQuestion(line);
			return;
		}

		errorList.add(Translator.trans("processLine.error.undefined",
				new Object[] { lineNo, line }));
		return;
	}

	private boolean defineLine(String line, Pattern pattern) {
		Matcher matcher = pattern.matcher(line);
		return matcher.find();
	}

	protected boolean validateExpression(String expression, Pattern pattern) {
		Matcher matcher = pattern.matcher(expression);
		return matcher.matches();
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

		no = "V" + line.substring(matcher.start() + 1, matcher.end());
		String text = line.substring(matcher.end()).trim();
		ddi3Helper.createQuestion(no, text);
	}

	/**
	 * Create multiple question item
	 * 
	 * @param line
	 *            of '* '''Multiple Question text''''
	 */
	private void createMultipleQuestion(String line) throws Exception {
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
			ddi3Helper.createMultipleQuestion(text);
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

	private void reuseCategory(String line) throws DDIFtpException {
		Matcher matcher = catePattern.matcher(line);
		if (matcher.find()) {
			int index = matcher.end();
			String catLine = line.substring(index).trim();

			// reuse categories
			matcher = variNamePattern.matcher(catLine);
			if (matcher.find()) {
				ddi3Helper.reuseCategories(catLine);
			}
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
	 *            of '''''ifthenelse''''' v1>2||V1==10&&V2==10 v6 v2 How many
	 *            times a day?
	 * @throws Exception
	 */
	private void createIfThenElse(String line, boolean create) throws Exception {
		String params[] = line.split(" ");
		// check for multiple white spaces
		for (String string : params) {
			if (string.isEmpty()) {
				ddi3Helper
						.handleParseError(
								ElementType.IF_THEN_ELSE,
								Translator
										.trans("line.parse.errorifthenelse",
												new Object[] {
														line,
														Translator
																.trans("line.parse.errormultiplewhitespaces") }));
				return;
			}
		}
		try {
			// Concatenate statement elements
			StringBuilder text = new StringBuilder();
			for (int i = 4; i < params.length; i++) {
				text.append(params[i]);
				text.append(" ");
			}

			// if condition
			if (!validateExpression(params[1], conditionPattern)) {
				ddi3Helper.handleParseError(ElementType.IF_THEN_ELSE,
						Translator.trans(
								"line.parse.errorifthenelse.condition", line));
			}

			// then
			if (variNamePattern.matcher(params[2]).find()) {
				params[2] = "V" + params[2].substring(1);
			}

			// else
			if (params[3].equals("na")||params[3].equals("NA")) {
				params[3] = null;
			} else if (variNamePattern.matcher(params[3]).find()) {
				params[3] = "V" + params[3].substring(1);
			}

			if (create) {
				ddi3Helper.createIfThenElse(params[1], params[2], params[3],
						text.toString().trim());
			}
		} catch (Exception e) {
			ddi3Helper.handleParseError(ElementType.IF_THEN_ELSE, Translator
					.trans("line.parse.errorifthenelse",
							new Object[] { line, e.getMessage() }));
		}
	}

	private void createStatementItem(String line) throws Exception {
		try {
			int index = line.indexOf(stateMatch);
			String result = line.substring(index + stateMatch.length());
			ddi3Helper.createStatementItem(result.trim());
		} catch (Exception e) {
			ddi3Helper.handleParseError(ElementType.STATEMENT_ITEM,
					Translator.trans("line.parse.errorstate", line));
		}
	}

	private void createComputationItem(String line) throws Exception {
		String params[] = line.split(" ");

		// varref params[1]
		// code params[2]
		// label params[3]
		// statement
		try {
			StringBuilder text = new StringBuilder();
			for (int i = 3; i < params.length; i++) {
				text.append(params[i]);
				text.append(" ");
			}

			ddi3Helper.createComputationItem(params[1], params[2],
					text.toString());
		} catch (Exception e) {
			ddi3Helper.handleParseError(ElementType.STATEMENT_ITEM,
					Translator.trans("line.parse.errorcomputation", line));
		}
	}
}
