package dk.dda.ddieditor.line.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ddialliance.ddieditor.ui.model.DdiModelException;
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
	HashMap<String, Integer> nbrVariableCodesMap = null;
	
	final static String SEQ_END = "end";
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
		// InputStream is = new ByteArrayInputStream(content.getBytes("UTF-8"));
		scanner = new Scanner(content);
		startScanning(create);
	}

	private void startScanning(boolean create) throws Exception {
		errorList.clear();
		ddi3Helper.clean();
		String current = null;
		while (scanner.hasNextLine()) {
			++lineNo;
			current = scanner.nextLine();
			processLine(current, create);
		}
		scanner.close();

		// check refs to pseudo variable ids
		Set<String> varIdRefs = ddi3Helper.pseudoVarIdRefMap.keySet();
		for (String varIdRef : varIdRefs) {
			if (!ddi3Helper.pseudoVarIdExists(varIdRef)) {
				reportError(ElementType.IF_THEN_ELSE, Translator.trans(
						"line.parse.errorifthenelse.variableref", varIdRef,
						ddi3Helper.getPseudoVarIdRefLineNo(varIdRef)), create);
			}
		}
		
		// check refts to sequences
		Set<String> seqIdRefs = ddi3Helper.pseudoSeqIdRefMap.keySet();
		for (String seqIdRef : seqIdRefs) {
			if (!ddi3Helper.pseudoSeqIdExists(seqIdRef)) {
				reportError(ElementType.IF_THEN_ELSE, Translator.trans(
						"line.parse.errorifthenelse.sequenceref", seqIdRef,
						ddi3Helper.getPseudoSeqIdRefLineNo(seqIdRef)), create);
			}
		}
		
		// update refs
		if (create) {
			ddi3Helper.postResolve();
		}
	}

	Pattern variNamePattern = Pattern.compile("[vV][1-9]+[0-9]*");
	Pattern variNameParentesPattern = Pattern.compile("\\([vV][1-9]+[0-9]*\\)");
	Pattern univPattern = Pattern.compile("^[=]{1}.+[=]{1}");
	Pattern seqPattern = Pattern.compile("^[=]{2}.+[=]{2}");
	Pattern quesPattern = Pattern.compile("^[=]{3}.+[=]{3}");
	Pattern queiPattern = Pattern.compile("^\\*+ ?[vV][1-9]++");
	// Pattern mquePattern = Pattern.compile("^'{3}.+ ?#{1}/D{2}. +'{3}");
	Pattern mquePattern = Pattern.compile("^'{3}.+#?+'{3}");
	Pattern catePattern = Pattern.compile("^\\*{2} ?");
	Pattern cateReusePattern = Pattern.compile("^\\*{2} ?[vV][1-9]+[0-9]*");
	Pattern conditionPattern = Pattern
			.compile(ConditionalUtil.conditionalPattern);

	String compMatch = "''comp''";
	String stateMatch = "''state''";
	String ifThenElseMatch = "''ifthenelse''";
	String intervMatch = "''interview''";
	String catsRpMatch = "**cats_";

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
		if (defineLine(line, seqPattern)) {
			createSequence(line, create);
			return;
		}
		if (defineLine(line, univPattern)) {
			if (create)
				createUniverse(line);
			return;
		}

		if (defineLine(line.trim(), cateReusePattern)) {
			reuseCategory(line, create);
			return;
		}
		if (defineLine(line, catePattern)) {
			if (nbrVariableCodesMap == null) {
				nbrVariableCodesMap = ddi3Helper.getAllNbrVariableCodes();
			}
			if (create) {
				if (line.indexOf(catsRpMatch) > -1) {
					useRPCategory(line);
					return;
				}
				createCategory(line);
			} else {
				if (line.indexOf(catsRpMatch) > -1) {
					ddi3Helper.setNbrVariableCategories(ddi3Helper
						.getCurrentPseudoVarId(), nbrVariableCodesMap.get(ddi3Helper
									.getCurrentPseudoVarId()));
					return;
				}
				// count nbr. of categories for given variable
				ddi3Helper.incrementNbrVariableCategories(ddi3Helper
						.getCurrentPseudoVarId());
			}
			return;
		}
		if (defineLine(line, queiPattern)) {
			createQuestion(line, create);
			return;
		}
		if (line.indexOf(ifThenElseMatch) > -1) {
			createIfThenElse(line, create);
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
		if (line.indexOf(intervMatch) > -1) {
			if (create)
				createInterviewerInstruction(line);
			else {
				validateInterviewerInstruction(line);
			}
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

	private void reportError(ElementType elementType, String msg, boolean create)
			throws DDIFtpException {
		if (create) {
			ddi3Helper.handleParseError(elementType, msg);
		} else {
			errorList.add(msg);
		}
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
	 * Create Sequence
	 * 
	 * @param line
	 *            of '==sequence-id==sequence label'
	 * @throws DDIFtpException
	 */
	private void createSequence(String line, boolean create) throws DDIFtpException {
		
		// label
		String id = null;
		int index = line.indexOf("==", 2);
		if (index > -1) {
			id = line.substring(2, index);
		}
		if (!id.equals(SEQ_END)) {
			ddi3Helper.setPseudoSeqId(id);
		}
		if (!create) {
			return;
		}

		// label
		String label = null;
		if (!(line.substring(index + 2).equals(""))) {
			label = line.substring(index + 2);
		}
		if (id.equals(SEQ_END)) {
			ddi3Helper.endSequence();
		} else {
			ddi3Helper.createSequence(id, label);
		}
	}

	/**
	 * Create question scheme
	 * 
	 * @param line
	 *            of '===questionscheme label===questionscheme description'
	 * @throws DDIFtpException
	 */
	private void createQuestionScheme(String line) throws DDIFtpException {
		// label
		String label = null;
		int index = line.indexOf("===", 2);
		if (index > -1) {
			label = line.substring(3, index);
		}

		// description
		String description = null;
		if (!(line.substring(index + 3).equals(""))) {
			description = line.substring(index + 3);
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
	private void createQuestion(String line, boolean create)
			throws DDIFtpException {
		String no = "";
		Matcher matcher = variNamePattern.matcher(line);
		matcher.find();
		
		if (!create
				&& nbrVariableCodesMap != null
				&& nbrVariableCodesMap.get(ddi3Helper.getCurrentPseudoVarId()) != null) {
			// check if nbr of codes matches categories for a given variable
			if (!ddi3Helper.mismatchOfVariableCodeAndCategories(nbrVariableCodesMap
					.get(ddi3Helper.getCurrentPseudoVarId()))) {
				reportError(
						ElementType.CATEGORY,
						Translator
								.trans("line.parse.errorcategory", ddi3Helper
								.getCurrentPseudoVarId()),
						create);
			}
		}

		no = "V" + line.substring(matcher.start() + 1, matcher.end());
		String text = line.substring(matcher.end()).trim();
		ddi3Helper.setCurrentPseudoVarId(no);
		if (create) {
			ddi3Helper.createQuestion(no, text);
		}
		ddi3Helper.setPseudoVarId(no);
	}

	/**
	 * Create multiple question item
	 * 
	 * @param line
	 *            of '* '''#groupingID Multiple Question text''''
	 */
	private void createMultipleQuestion(String line) throws Exception {
		String text = null;
		String label = null;
		String groupingId = null;

		int index = line.indexOf("'''");
		if (index > -1) {
			int end = line.indexOf("'''", index + 3);
			if (end > -1) {
				text = line.substring(index + 3, end);
				int hashIndex = text.indexOf("#");
				if (hashIndex > -1) {
					int endIndex = text.indexOf(' ');
					groupingId = text.substring(hashIndex + 1, endIndex);
					label = text.substring(endIndex + 1);
				} else {
					label = text;
				}
			}
		}
		if (label != null) {
			// unset mque
			if (text.equals(MQUE_END)) {
				ddi3Helper.unsetMultipleQuestion();
				return;
			}
			ddi3Helper.createMultipleQuestion(groupingId, label);
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

	private void reuseCategory(String line, boolean create) throws DDIFtpException {
		Matcher matcher = catePattern.matcher(line);
		if (matcher.find()) {
			int index = matcher.end();
			String catLine = line.substring(index).trim();

			// reuse categories
			matcher = variNamePattern.matcher(catLine);
			if (matcher.find()) {
				if (create) {
					ddi3Helper.reuseCategories(catLine);
				} else {
					// check if nbr. of reuse categories exceeds number of codes
					Integer nbrCodes = nbrVariableCodesMap.get(catLine);
					if (nbrCodes == null) {
						reportError(
								ElementType.CATEGORY,
								Translator
										.trans("line.parse.errorcategory.variableref", catLine, ddi3Helper.getLineNo()),
								create);
						return;
					}
					if (ddi3Helper.getNbrVariableCategories(catLine) != nbrCodes) {
						reportError(
								ElementType.CATEGORY,
								Translator
										.trans("line.parse.errorreusecategory", ddi3Helper
										.getCurrentPseudoVarId(), ddi3Helper.getLineNo()),
								create);
					}
					ddi3Helper.setNbrVariableCategories(ddi3Helper
										.getCurrentPseudoVarId(), nbrCodes);
				}
			}
		}
	}

	private void useRPCategory(String line) throws DDIFtpException {
		Matcher matcher = catePattern.matcher(line);
		if (matcher.find()) {
			int index = matcher.end();
			String catLine = line.substring(index).trim();

			// reuse RP categories
			ddi3Helper.useRPCategories(catLine);
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
	 *            of ''ifthenelse'' v1>2||V1==10&&V2==10 v6 v2 How many times a
	 *            day?
	 * @throws Exception
	 */
	private void createIfThenElse(String line, boolean create) throws Exception {
		String params[] = line.split(" ");
		// check for multiple white spaces
		for (String string : params) {
			if (string.isEmpty()) {
				reportError(
						ElementType.IF_THEN_ELSE,
						Translator
								.trans("line.parse.errorifthenelse",
										new Object[] {
												line,
												Translator
														.trans("line.parse.errormultiplewhitespaces") }),
						create);
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
			if (!ConditionalUtil.validCondition(params[1])) {
				reportError(ElementType.IF_THEN_ELSE, Translator.trans(
						"line.parse.errorifthenelse.condition", params[1],
						ddi3Helper.getLineNo()), create);
			}
			String[] varIDs = ConditionalUtil.extractUniqueIDs(params[1]);
			for (String varID : varIDs) {
				ddi3Helper.setPseudoVarIdRef(varID);
			}
			

			// then
			if (variNamePattern.matcher(params[2]).find()) {
				params[2] = "V" + params[2].substring(1);
				ddi3Helper.setPseudoVarIdRef(params[2]);
			} else {
				// sequence ref. expected
				ddi3Helper.setPseudoSeqIdRef(params[2]);
			}

			// else
			if (params[3].equals("na") || params[3].equals("NA")) {
				params[3] = null;
			} else if (variNamePattern.matcher(params[3]).find()) {
				params[3] = "V" + params[3].substring(1);
				ddi3Helper.setPseudoVarIdRef(params[3]);
			} else {
				// sequence ref. expected
				ddi3Helper.setPseudoSeqIdRef(params[3]);
			}

			if (create) {
				try {
					ddi3Helper.createIfThenElse(params[1], params[2],
							params[3], text.toString().trim());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw e;
				}
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

	private void createInterviewerInstruction(String line)
			throws DDIFtpException {
		try {
			int index = line.indexOf(intervMatch);
			String result = line.substring(index + intervMatch.length()).trim();

			// validate condition
			String condition = validateInterviewerInstruction(line);
			if (condition != null) {
				int cIndex = result.indexOf(condition);
				result = result.substring(0, cIndex - 1);
				if (condition.equals("na") || condition.equals("NA")) {
					condition = null;
				}
			}

			// create
			ddi3Helper.createInterviewerInstruction(result.trim(), condition);
		} catch (Exception e) {
			ddi3Helper.handleParseError(ElementType.INSTRUCTION,
					Translator.trans("line.parse.errorstate", line));
		}
	}

	private String validateInterviewerInstruction(String line)
			throws DdiModelException, DDIFtpException {
		String[] lineSplit = line.split(" ");
		String condition = lineSplit[lineSplit.length - 1];

		Matcher matcherParentes = variNameParentesPattern.matcher(condition);
		if (matcherParentes.find()) {
			return null;
		}

		Matcher matcher = variNamePattern.matcher(condition);
		if (matcher.find()) {
			// check condition
			if (!ConditionalUtil.validCondition(condition)) {
				reportError(ElementType.INSTRUCTION,
						Translator.trans("line.parse.errorinterview", line),
						false);
			}
			return condition;
		}
		if (condition.equals("na") || condition.equals("NA")) {
			return condition;
		}
		return null;
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
