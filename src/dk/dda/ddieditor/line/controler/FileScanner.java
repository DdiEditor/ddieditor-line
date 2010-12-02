package dk.dda.ddieditor.line.controler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.ConceptSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.ConceptType;
import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.UniverseSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.UniverseType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.ControlConstructSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.ControlConstructType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.IfThenElseDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.IfThenElseType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionConstructDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionConstructType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionItemType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.SequenceDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.SequenceType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.StatementItemDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.StatementItemType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.AbstractIdentifiableType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.AbstractVersionableType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.LabelType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.ProgrammingLanguageCodeType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.ReferenceType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.StructuredStringType;
import org.ddialliance.ddieditor.logic.identification.IdentificationManager;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddieditor.ui.model.ElementType;
import org.ddialliance.ddieditor.ui.model.ModelAccessor;
import org.ddialliance.ddieditor.ui.model.ModelIdentifingType;
import org.ddialliance.ddieditor.ui.model.instrument.IfThenElse;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;
import org.ddialliance.ddiftp.util.xml.XmlBeansUtil;

public class FileScanner {
	static private Log log = LogFactory.getLog(LogType.SYSTEM,
			FileScanner.class);

	int lineNo = 0;

	XmlOptions xmlOptions = new XmlOptions();

	/**
	 * Constructor
	 */
	public FileScanner() {
		xmlOptions.setSaveAggressiveNamespaces();
		xmlOptions.setSavePrettyPrint();
		xmlOptions.setSaveOuter();
	}

	/**
	 * Scan file
	 * 
	 * @param file
	 * @throws Exception
	 */
	public void startScanning(File file) throws Exception {
		intDdi3();

		Scanner scanner = null;
		scanner = new Scanner(file, "utf-8");

		String current = null;
		while (scanner.hasNextLine()) {
			++lineNo;
			current = scanner.nextLine();
			processLine(current);
		}
		scanner.close();

		// TODO update refs
	}

	Pattern pattern = Pattern.compile("[1-9]");

	public void processLine(String line) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug(lineNo + " - " + line);
		}

		// check for empty input
		if (line == null || line.equals("")) {
			// state.linetype = LineType.NONE;
			return;
		}

		// weed out comments
		String[] commentWeed = { "//", };
		for (int i = 0; i < commentWeed.length; i++) {
			if (line.indexOf("//") > -1) {
				return;
			}
		}

		// question scheme
		// concept scheme
		if (line.startsWith("==")) {
			// ==FC1 Første concept==title,beskrivelse
			createQuestionScheme(line);
		}
		// universe
		else if (line.startsWith("=")) {
			createUniverse(line);
		}

		// ifthense, value, then id ref idref
		// sequence
		// universe
		// syntax: filter if [==, >, <]code_value the goto
		// controlconstuct_ft_id else goto controlconstuct_2nd_id
		// **ifthenelse <8 4 2 Nogle flere spr om fugle
		else if (line.startsWith("**")) {
			// ifthenelse
			int index = line.indexOf("ifthenelse");
			if (index > -1) {
				createIfThenElse(index, line);
			}
		}

		// question item
		// control construct
		String queiTag = line.substring(0, 1);
		Matcher matcher = pattern.matcher(queiTag);
		if (matcher.matches()) {
			quei = createQuestion(line);
		}

		// guard
		return;
	}

	List<UniverseSchemeDocument> unisList = new ArrayList<UniverseSchemeDocument>();
	List<ConceptSchemeDocument> consList = new ArrayList<ConceptSchemeDocument>();
	List<QuestionSchemeDocument> quesList = new ArrayList<QuestionSchemeDocument>();
	List<ControlConstructSchemeDocument> cocsList = new ArrayList<ControlConstructSchemeDocument>();

	public List<UniverseSchemeDocument> getUnisList() {
		return unisList;
	}

	public List<ConceptSchemeDocument> getConsList() {
		return consList;
	}

	public List<QuestionSchemeDocument> getQuesList() {
		return quesList;
	}

	public List<ControlConstructSchemeDocument> getCocsList() {
		return cocsList;
	}

	UniverseSchemeDocument unis;
	UniverseType univ;
	ConceptSchemeDocument cons;
	ConceptType conc;
	QuestionSchemeDocument ques;
	QuestionItemType quei;
	ControlConstructSchemeDocument cocs;
	SequenceType mainSeq;

	private void intDdi3() throws DDIFtpException {
		// universe
		UniverseSchemeDocument unisDoc = UniverseSchemeDocument.Factory
				.newInstance();
		unisDoc.addNewUniverseScheme();
		addIdAndVersion(unisDoc.getUniverseScheme(),
				ElementType.UNIVERSE_SCHEME.getIdPrefix(), null);
		unisList.add(unisDoc);
		unis = unisDoc;

		// concept scheme
		ConceptSchemeDocument consDoc = ConceptSchemeDocument.Factory
				.newInstance();
		consDoc.addNewConceptScheme();
		addIdAndVersion(consDoc.getConceptScheme(),
				ElementType.CONCEPT_SCHEME.getIdPrefix(), null);
		consList.add(consDoc);
		cons = consDoc;

		// question scheme
		QuestionSchemeDocument quesDoc = QuestionSchemeDocument.Factory
				.newInstance();
		quesDoc.addNewQuestionScheme();
		addIdAndVersion(quesDoc.getQuestionScheme(),
				ElementType.QUESTION_SCHEME.getIdPrefix(), null);
		quesList.add(quesDoc);
		ques = quesDoc;

		// control construct scheme
		ControlConstructSchemeDocument cocsDoc = ControlConstructSchemeDocument.Factory
				.newInstance();
		cocsDoc.addNewControlConstructScheme();
		addIdAndVersion(cocsDoc.getControlConstructScheme(),
				ElementType.CONTROL_CONSTRUCT_SCHEME.getIdPrefix(), null);
		cocsList.add(cocsDoc);
		cocs = cocsDoc;
		// main seq
		ControlConstructType cc = cocsDoc.getControlConstructScheme()
				.addNewControlConstruct();
		mainSeq = (SequenceType) cc.substitute(
				SequenceDocument.type.getDocumentElementName(),
				SequenceType.type);
		setText(mainSeq.addNewLabel(), "Main sequence");
	}

	// =universe label=universe beskrivelse
	private void createUniverse(String line) throws DDIFtpException {
		UniverseType result = unis.getUniverseScheme().addNewUniverse();
		addIdAndVersion(result, ElementType.UNIVERSE.getIdPrefix(), null);

		// label
		int index = line.indexOf("=", 1);
		if (index > -1) {
			setText(result.addNewLabel(), line.substring(1, index));
		}

		// description
		if (!(line.substring(index+1).equals(""))) {
			setText(result.addNewHumanReadable(), line.substring(index+1));
		}
		univ = result;
	}

	private void createQuestionScheme(String line) throws DDIFtpException {
		QuestionSchemeDocument result = QuestionSchemeDocument.Factory
				.newInstance();
		result.addNewQuestionScheme();
		addIdAndVersion(result.getQuestionScheme(),
				ElementType.QUESTION_SCHEME.getIdPrefix(), null);

		// label
		String label = null;
		int index = line.indexOf("==", 2);
		if (index > -1) {
			label = line.substring(2, index);
			setText(result.getQuestionScheme().addNewLabel(), label);
		}

		// description
		String description = null;
		if (!(line.substring(index+2).equals(""))) {
			description = line.substring(index+2);
			setText(result.getQuestionScheme().addNewDescription(), description);
		}
		ques = result;
		quesList.add(result);

		createConcept(label, description);
	}

	private void createConcept(String label, String description)
			throws DDIFtpException {
		ConceptType result = cons.getConceptScheme().addNewConcept();
		addIdAndVersion(result, ElementType.CONCEPT.getIdPrefix(), null);
		setText(result.addNewLabel(), label);
		setText(result.addNewDescription(), description);
		conc = result;
	}

	private QuestionItemType createQuestion(String line) throws DDIFtpException {
		QuestionItemType result = ques.getQuestionScheme().addNewQuestionItem();
		addIdAndVersion(result, ElementType.QUESTION_ITEM.getIdPrefix(), null);

		String no = "";
		int index = line.indexOf(" ");
		if (index > -1) {
			no = line.substring(0, index);
		}
		result.addNewQuestionItemName().setStringValue(no);
		XmlBeansUtil.setTextOnMixedElement(result.addNewQuestionText()
				.addNewText(), line.substring(index + 1));

		// concept ref
		if (cons != null && conc != null) {
			createReference(result.addNewConceptReference(), cons
					.getConceptScheme().getId(), cons.getConceptScheme()
					.getVersion(), conc.getId(), conc.getVersion());
		}

		// question construct
		QuestionConstructType questionConstruct = (QuestionConstructType) cocs
				.getControlConstructScheme()
				.addNewControlConstruct()
				.substitute(
						QuestionConstructDocument.type.getDocumentElementName(),
						QuestionConstructType.type);
		addIdAndVersion(questionConstruct,
				ElementType.QUESTION_CONSTRUCT.getIdPrefix(), null);

		// question ref
		createReference(questionConstruct.addNewQuestionReference(), ques
				.getQuestionScheme().getId(), ques.getQuestionScheme()
				.getVersion(), result.getId(), result.getVersion());

		// add cc ref to main seq
		createReference(mainSeq.addNewControlConstructReference(), cocs
				.getControlConstructScheme().getId(), cocs
				.getControlConstructScheme().getVersion(),
				questionConstruct.getId(), questionConstruct.getVersion());
		return result;
	}

	private void createIfThenElse(int index, String line) throws Exception {
		// **ifthenelse <8 6 2 Hvor mange gange om dagen?
		String params[] = line.split(" ");

		// statement item
		StatementItemType stai = (StatementItemType) cocs
				.getControlConstructScheme()
				.addNewControlConstruct()
				.substitute(
						StatementItemDocument.type.getDocumentElementName(),
						StatementItemType.type);
		addIdAndVersion(stai, ElementType.STATEMENT_ITEM.getIdPrefix(), null);
		StringBuilder stament = new StringBuilder();
		for (int i = 4; i < params.length; i++) {
			stament.append(params[i]);
			stament.append(" ");
		}
		XmlBeansUtil.setTextOnMixedElement(stai.addNewDisplayText()
				.addNewText().addNewDescription(), stament.toString().trim());

		// seq
		SequenceType seq = (SequenceType) cocs
				.getControlConstructScheme()
				.addNewControlConstruct()
				.substitute(SequenceDocument.type.getDocumentElementName(),
						SequenceType.type);
		addIdAndVersion(seq, ElementType.SEQUENCE.getIdPrefix(), null);
		setText(seq.addNewLabel(),
				XmlBeansUtil.getTextOnMixedElement(
						quei.getQuestionTextList().get(0).getTextList().get(0))
						.substring(0, 10));

		// ref statement item
		ModelAccessor.setReference(
				seq.addNewControlConstructReference(),
				createLightXmlObject(cocs.getControlConstructScheme().getId(),
						cocs.getControlConstructScheme().getVersion(),
						stai.getId(), stai.getVersion()));
		// ref next question item
		ModelAccessor.setReference(
				seq.addNewControlConstructReference(),
				createLightXmlObject(cocs.getControlConstructScheme().getId(),
						cocs.getControlConstructScheme().getVersion(),
						// TODO resolve this param later aka question construct
						// with questernref nameid == params[2]
						params[2], null));

		// if then else
		IfThenElseType ifthenelse = (IfThenElseType) cocs
				.getControlConstructScheme()
				.addNewControlConstruct()
				.substitute(IfThenElseDocument.type.getDocumentElementName(),
						IfThenElseType.type);
		addIdAndVersion(ifthenelse,
				ElementType.QUESTION_CONSTRUCT.getIdPrefix(), null);

		IfThenElse model = new IfThenElse(
				IfThenElseDocument.Factory
						.parse(ifthenelse.xmlText(xmlOptions)),
				null, null);

		model.setCreate(true);
		// ProgrammingLanguageCodeType
		model.applyChange(params[1], ProgrammingLanguageCodeType.class);
		// ProgrammingLanguageCodeType/@programmingLanguage
		model.applyChange("dda.dk", ModelIdentifingType.Type_A.class);

		// question reference
		model.applyChange(
				createLightXmlObject(ques.getQuestionScheme().getId(), ques
						.getQuestionScheme().getVersion(), quei.getId(), quei
						.getVersion()), ModelIdentifingType.Type_B.class);

		// then reference
		model.applyChange(
				createLightXmlObject(cocs.getControlConstructScheme().getId(),
						cocs.getControlConstructScheme().getVersion(),
						seq.getId(), seq.getVersion()),
				ModelIdentifingType.Type_C.class);

		// else reference
		// TODO resolve later
		model.applyChange(createLightXmlObject(null, null, params[3], null),
				ModelIdentifingType.Type_D.class);

		ifthenelse.set(model.getDocument().getIfThenElse());
	}

	public void addIdAndVersion(AbstractIdentifiableType abstractIdentifiable,
			String prefix, String postfix) throws DDIFtpException {
		IdentificationManager.getInstance().addIdentification(
				abstractIdentifiable, prefix, postfix);
	}

	public void addIdAndVersion(AbstractVersionableType abstractVersionable,
			String prefix, String postfix) throws DDIFtpException {
		IdentificationManager.getInstance().addIdentification(
				abstractVersionable, prefix, postfix);

		IdentificationManager.getInstance().addVersionInformation(
				(AbstractVersionableType) abstractVersionable, null, null);
	}

	private void setText(LabelType label, String text) {
		XmlBeansUtil.setTextOnMixedElement(label, text);
	}

	private void setText(StructuredStringType struct, String text) {
		XmlBeansUtil.setTextOnMixedElement(struct, text);
	}

	public LightXmlObjectType createLightXmlObject(String parentId,
			String parentVersion, String id, String version) {
		LightXmlObjectType lightXmlObject = LightXmlObjectType.Factory
				.newInstance();
		lightXmlObject.setParentId(parentId);
		lightXmlObject.setParentVersion(parentVersion);
		lightXmlObject.setId(id);
		lightXmlObject.setVersion(version);
		return lightXmlObject;
	}

	private void createReference(ReferenceType ref, String parentId,
			String parentVersion, String id, String version) {
		ModelAccessor.setReference(ref,
				createLightXmlObject(parentId, parentVersion, id, version));
	}

	public void resultToString() {
		log.debug("Universes:");
		listToString(unisList);
		log.debug("ConceptScheme:");
		listToString(consList);
		log.debug("QuestionSchemes:");
		listToString(quesList);
		log.debug("Instrument:");
		listToString(cocsList);
	}

	private void listToString(List list) {
		for (Object doc : list) {
			log.debug(((XmlObject) doc).xmlText(xmlOptions));
		}
	}
}
