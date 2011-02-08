package dk.dda.ddieditor.line.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.ConceptSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.ConceptType;
import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.UniverseSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.UniverseType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.ControlConstructSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.ControlConstructType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.DynamicTextType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.IfThenElseDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.IfThenElseType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.LiteralTextDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.LiteralTextType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.MultipleQuestionItemDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.MultipleQuestionItemType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionConstructDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionConstructType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionItemType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.SequenceDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.SequenceType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.StatementItemDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.StatementItemType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.TextType;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.CategorySchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.CategoryType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.AbstractIdentifiableType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.AbstractMaintainableType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.AbstractVersionableType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.LabelType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.NameType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.NoteDocument;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.NoteType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.ProgrammingLanguageCodeType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.ReferenceType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.StructuredStringType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.UserIDType;
import org.ddialliance.ddieditor.logic.identification.IdentificationManager;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddieditor.model.namespace.ddi3.Ddi3NamespaceHelper;
import org.ddialliance.ddieditor.ui.model.ElementType;
import org.ddialliance.ddieditor.ui.model.ModelAccessor;
import org.ddialliance.ddieditor.ui.model.ModelIdentifingType;
import org.ddialliance.ddieditor.ui.model.instrument.IfThenElse;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.Translator;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;
import org.ddialliance.ddiftp.util.xml.XmlBeansUtil;

public class Ddi3Helper {
	static private Log log = LogFactory
			.getLog(LogType.SYSTEM, Ddi3Helper.class);
	List<UniverseSchemeDocument> unisList = new ArrayList<UniverseSchemeDocument>();
	List<NoteDocument> noteList = new ArrayList<NoteDocument>();
	List<ConceptSchemeDocument> consList = new ArrayList<ConceptSchemeDocument>();
	List<QuestionSchemeDocument> quesList = new ArrayList<QuestionSchemeDocument>();
	List<CategorySchemeDocument> catsList = new ArrayList<CategorySchemeDocument>();
	List<ControlConstructSchemeDocument> cocsList = new ArrayList<ControlConstructSchemeDocument>();
	List<MultipleQuestionItemDocument> mqueList = new ArrayList<MultipleQuestionItemDocument>();
	Map<String, LightXmlObjectType> mqueToQuesMap = new HashMap<String, LightXmlObjectType>();

	UniverseSchemeDocument unis;
	public UniverseType univ;
	ConceptSchemeDocument cons;
	public ConceptType conc;
	MultipleQuestionItemDocument mquem;
	boolean mque = false;
	QuestionConstructType mquecc;
	public QuestionSchemeDocument ques;
	public List<String> quesIsNewList = new ArrayList<String>();
	QuestionItemType quei;
	CategorySchemeDocument cats;
	public ControlConstructSchemeDocument cocs;
	public boolean cocsIsNew = false;
	public SequenceType mainSeq;

	Map<String, LightXmlObjectType> pseudoVarIdToCcIdMap = new HashMap<String, LightXmlObjectType>();
	List<XmlObject> postResolveItemRefs = new ArrayList<XmlObject>();
	Map<String, LightXmlObjectType> pseudoVarIdToUnivIdMap = new HashMap<String, LightXmlObjectType>();
	List<String> postCleanMainSeqItems = new ArrayList<String>();

	public XmlOptions xmlOptions = new XmlOptions();

	int LABEL_LENGTH = 35;
	String labelPostFix = " ...";

	// TODO change this later please !!!
	String agency = ElementType.getAgency();

	public Ddi3Helper() throws DDIFtpException {
		xmlOptions.setSaveAggressiveNamespaces();
		xmlOptions.setSavePrettyPrint();
		xmlOptions.setSaveOuter();
	}

	public void initDdi3() throws DDIFtpException {
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
		if (ques == null) {
			QuestionSchemeDocument quesDoc = QuestionSchemeDocument.Factory
					.newInstance();
			quesDoc.addNewQuestionScheme();
			addIdAndVersion(quesDoc.getQuestionScheme(),
					ElementType.QUESTION_SCHEME.getIdPrefix(), null);
			quesList.add(quesDoc);
			ques = quesDoc;
			quesIsNewList.add(ques.getQuestionScheme().getId());
		} else {
			quesList.add(ques);
		}

		// category scheme
		createCategoryScheme();

		// control construct scheme
		if (cocs == null) {
			ControlConstructSchemeDocument cocsDoc = ControlConstructSchemeDocument.Factory
					.newInstance();
			cocsDoc.addNewControlConstructScheme();
			addIdAndVersion(cocsDoc.getControlConstructScheme(),
					ElementType.CONTROL_CONSTRUCT_SCHEME.getIdPrefix(), null);
			cocsList.add(cocsDoc);
			cocs = cocsDoc;
			cocsIsNew = true;
		} else {
			cocsList.add(cocs);
		}

		// main seq
		if (mainSeq == null) {
			ControlConstructType cc = cocs.getControlConstructScheme()
					.addNewControlConstruct();
			mainSeq = (SequenceType) cc.substitute(
					SequenceDocument.type.getDocumentElementName(),
					SequenceType.type);
			setText(mainSeq.addNewLabel(), "Main sequence");
			addIdAndVersion(mainSeq, ElementType.SEQUENCE.getIdPrefix(), null);
		}
	}

	// =universe label=universe beskrivelse
	public void createUniverse(String label, String description)
			throws DDIFtpException {
		UniverseType result = unis.getUniverseScheme().addNewUniverse();
		addIdAndVersion(result, ElementType.UNIVERSE.getIdPrefix(), null);

		// label
		if (checkString(label))
			setText(result.addNewLabel(), label);

		// description
		if (checkString(description))
			setText(result.addNewHumanReadable(), description);
		univ = result;
	}

	public void createQuestionScheme(String label, String description)
			throws DDIFtpException {
		QuestionSchemeDocument result = QuestionSchemeDocument.Factory
				.newInstance();
		result.addNewQuestionScheme();
		addIdAndVersion(result.getQuestionScheme(),
				ElementType.QUESTION_SCHEME.getIdPrefix(), null);

		// label
		if (checkString(label))
			setText(result.getQuestionScheme().addNewLabel(), label);

		// description
		if (checkString(description))
			setText(result.getQuestionScheme().addNewDescription(), description);

		ques = result;
		quesList.add(result);
		quesIsNewList.add(result.getQuestionScheme().getId());
		createConcept(label, description);
	}

	public void createConcept(String label, String description)
			throws DDIFtpException {
		ConceptType result = cons.getConceptScheme().addNewConcept();
		addIdAndVersion(result, ElementType.CONCEPT.getIdPrefix(), null);
		setText(result.addNewLabel(), label);
		setText(result.addNewDescription(), description);
		conc = result;
	}

	public void createQuestion(String pseudoVariableId, String text)
			throws DDIFtpException {
		// reset category scheme
		cats = null;

		QuestionItemType result = null;
		if (mque) {
			result = mquem.getMultipleQuestionItem().getSubQuestions()
					.addNewQuestionItem();
		} else {
			result = ques.getQuestionScheme().addNewQuestionItem();
		}
		addIdAndVersion(result, ElementType.QUESTION_ITEM.getIdPrefix(), null);

		// name
		NameType name = result.addNewQuestionItemName();
		name.setStringValue(getLabelText(text));
		XmlBeansUtil.addTranslationAttributes(name,
				Translator.getLocaleLanguage(), false, true);

		// variable ref pseudoVariableId
		UserIDType userId = result.addNewUserID();
		userId.setType(Ddi3NamespaceHelper.QUEI_VAR_USER_ID_TYPE);
		userId.setStringValue(pseudoVariableId);

		// univ ref as note
		if (univ != null) {
			createQueiRefToUnivNote(
					createLightXmlObject(ques.getQuestionScheme().getId(), ques
							.getQuestionScheme().getVersion(), result.getId(),
							result.getVersion()),
					createLightXmlObject(unis.getUniverseScheme().getId(), unis
							.getUniverseScheme().getVersion(), univ.getId(),
							univ.getVersion()));
		}

		// set text
		DynamicTextType dynamicText = result.addNewQuestionText();
		TextType textType = dynamicText.addNewText();
		XmlBeansUtil.addTranslationAttributes(dynamicText,
				Translator.getLocaleLanguage(), false, true);
		LiteralTextType lTextType = (LiteralTextType) textType.substitute(
				LiteralTextDocument.type.getDocumentElementName(),
				LiteralTextType.type);
		lTextType.addNewText();
		XmlBeansUtil.setTextOnMixedElement(lTextType.getText(), text);

		// concept ref
		if (cons != null && conc != null) {
			setReference(result.addNewConceptReference(), cons
					.getConceptScheme().getId(), cons.getConceptScheme()
					.getVersion(), conc.getId(), conc.getVersion());
		}

		// control construct
		if (!mque) {
			QuestionConstructType qc = createQuestionConstruct(ques
					.getQuestionScheme().getId(), ques.getQuestionScheme()
					.getVersion(), result.getId(), result.getVersion());
			setText(qc.addNewLabel(), getLabelText(text));

			// pseudo var id map
			pseudoVarIdToCcIdMap.put(
					pseudoVariableId,
					createLightXmlObject(cocs.getControlConstructScheme()
							.getId(), cocs.getControlConstructScheme()
							.getVersion(), qc.getId(), qc.getVersion()));

			quei = result;
		} else {
			// pseudo var id map
			pseudoVarIdToCcIdMap
					.put(pseudoVariableId,
							createLightXmlObject(cocs
									.getControlConstructScheme().getId(), cocs
									.getControlConstructScheme().getVersion(),
									mquecc.getId(), mquecc.getVersion()));
		}
	}

	private QuestionConstructType createQuestionConstruct(String parentId,
			String parentVersion, String id, String version)
			throws DDIFtpException {
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
		setReference(questionConstruct.addNewQuestionReference(), parentId,
				parentVersion, id, version);

		// add cc ref to main seq
		addControlConstructToMainSequence(questionConstruct);

		return questionConstruct;
	}

	public void createMultipleQuestion(String text) throws Exception {
		this.mque = true;
		MultipleQuestionItemDocument doc = MultipleQuestionItemDocument.Factory
				.newInstance();
		MultipleQuestionItemType type = doc.addNewMultipleQuestionItem();
		addIdAndVersion(type, ElementType.MULTIPLE_QUESTION_ITEM.getIdPrefix(),
				null);
		type.addNewSubQuestions();

		// concept ref
		if (conc != null) {
			ModelAccessor.setReference(
					type.addNewConceptReference(),
					createLightXmlObject(null, null, conc.getId(),
							conc.getVersion()));
		}

		// text
		DynamicTextType questionText = type.addNewQuestionText();
		TextType textType = questionText.addNewText();
		LiteralTextType lTextType = (LiteralTextType) textType.substitute(
				LiteralTextDocument.type.getDocumentElementName(),
				LiteralTextType.type);
		XmlBeansUtil.setTextOnMixedElement(lTextType.addNewText(), text);

		// control construct
		this.mquecc = createQuestionConstruct(ques.getQuestionScheme().getId(),
				ques.getQuestionScheme().getVersion(), type.getId(),
				type.getVersion());
		setText(mquecc.addNewLabel(), getLabelText(text));

		mqueList.add(doc);
		mqueToQuesMap.put(
				type.getId(),
				createLightXmlObject(null, null, ques.getQuestionScheme()
						.getId(), ques.getQuestionScheme().getVersion()));
		this.mquem = doc;
	}

	public Map<String, LightXmlObjectType> getMqueToQuesMap() {
		return mqueToQuesMap;
	}

	public void createCategory(String text) throws DDIFtpException {
		if (cats == null) {
			createCategoryScheme();
		}
		CategoryType cat = cats.getCategoryScheme().addNewCategory();
		addIdAndVersion(cat, ElementType.CATEGORY.getIdPrefix(), null);

		setText(cat.addNewLabel(), text);
	}

	private CategorySchemeDocument createCategoryScheme()
			throws DDIFtpException {
		CategorySchemeDocument catsDoc = CategorySchemeDocument.Factory
				.newInstance();
		catsDoc.addNewCategoryScheme();
		addIdAndVersion(catsDoc.getCategoryScheme(),
				ElementType.CATEGORY_SCHEME.getIdPrefix(), null);
		catsList.add(catsDoc);
		cats = catsDoc;
		return cats;
	}

	public void createIfThenElse(String condition, String then, String elze,
			String statementText) throws Exception {
		// universe
		UniverseType prevUniv = univ;
		createUniverse(getLabelText(statementText), getLabelText(statementText));
		pseudoVarIdToUnivIdMap.put(
				then,
				createLightXmlObject(unis.getUniverseScheme().getId(), unis
						.getUniverseScheme().getVersion(), univ.getId(), univ
						.getVersion()));

		// statement item
		StatementItemType stai = (StatementItemType) cocs
				.getControlConstructScheme()
				.addNewControlConstruct()
				.substitute(
						StatementItemDocument.type.getDocumentElementName(),
						StatementItemType.type);
		addIdAndVersion(stai, ElementType.STATEMENT_ITEM.getIdPrefix(), null);

		// statement label
		setText(stai.addNewLabel(), getLabelText(statementText));

		// set text
		DynamicTextType dynamicText = stai.addNewDisplayText();
		TextType textType = dynamicText.addNewText();
		XmlBeansUtil.addTranslationAttributes(dynamicText,
				Translator.getLocaleLanguage(), false, true);
		LiteralTextType lTextType = (LiteralTextType) textType.substitute(
				LiteralTextDocument.type.getDocumentElementName(),
				LiteralTextType.type);
		lTextType.addNewText();
		XmlBeansUtil.setTextOnMixedElement(lTextType.getText(), statementText);

		// seq
		SequenceType seq = (SequenceType) cocs
				.getControlConstructScheme()
				.addNewControlConstruct()
				.substitute(SequenceDocument.type.getDocumentElementName(),
						SequenceType.type);
		addIdAndVersion(seq, ElementType.SEQUENCE.getIdPrefix(), null);
		setText(seq.addNewLabel(), ElementType.SEQUENCE.getIdPrefix()
				+ "-"
				+ getLabelText(quei.getQuestionItemNameArray(0)
						.getStringValue()));

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
						cocs.getControlConstructScheme().getVersion(), then,
						null));
		postResolveItemRefs.add(seq);

		// if then else
		IfThenElseType ifthenelse = (IfThenElseType) cocs
				.getControlConstructScheme()
				.addNewControlConstruct()
				.substitute(IfThenElseDocument.type.getDocumentElementName(),
						IfThenElseType.type);
		addIdAndVersion(ifthenelse, ElementType.IF_THEN_ELSE.getIdPrefix(),
				null);

		IfThenElse model = new IfThenElse(
				IfThenElseDocument.Factory
						.parse(ifthenelse.xmlText(xmlOptions)),
				null, null);

		model.setCreate(true);

		// ProgrammingLanguageCodeType
		condition = "value " + condition;
		model.applyChange(condition, ProgrammingLanguageCodeType.class);
		// ProgrammingLanguageCodeType/@programmingLanguage
		model.applyChange(agency, ModelIdentifingType.Type_A.class);

		// question reference
		model.applyChange(
				createLightXmlObject(ques.getQuestionScheme().getId(), ques
						.getQuestionScheme().getVersion(), quei.getId(), quei
						.getVersion()), ModelIdentifingType.Type_B.class);

		// then reference
		// TODO
		// java.lang.NullPointerException
		// at
		// org.ddialliance.ddieditor.ui.model.ModelAccessor.setReference(ModelAccessor.java:19)
		// at
		// org.ddialliance.ddieditor.ui.model.instrument.IfThenElse.executeChange(IfThenElse.java:53)
		// at
		// org.ddialliance.ddieditor.ui.model.Model.applyChange(Model.java:101)
		// at
		// dk.dda.ddieditor.line.util.Ddi3Helper.createIfThenElse(Ddi3Helper.java:461)
		// at
		// dk.dda.ddieditor.line.util.Wiki2Ddi3Scanner.createIfThenElse(Wiki2Ddi3Scanner.java:282)
		// at
		// dk.dda.ddieditor.line.util.Wiki2Ddi3Scanner.processLine(Wiki2Ddi3Scanner.java:118)
		model.applyChange(
				createLightXmlObject(cocs.getControlConstructScheme().getId(),
						cocs.getControlConstructScheme().getVersion(),
						seq.getId(), seq.getVersion()),
				ModelIdentifingType.Type_C.class);

		// else reference
		if (elze != null) {
			model.applyChange(createLightXmlObject(null, null, elze, null),
					ModelIdentifingType.Type_D.class);
			postCleanMainSeqItems.add(elze);
		}

		// label
		setText(model.getDocument().getIfThenElse().addNewLabel(),
				getLabelText(statementText));
		ifthenelse.set(model.getDocument().getIfThenElse());

		// add to main seq
		addControlConstructToMainSequence(ifthenelse);

		// add to post resolve items
		postResolveItemRefs.add(ifthenelse);

		// post clean seq for created quei cc
		postCleanMainSeqItems.add(then);

		// reset univ
		univ = prevUniv;
	}

	private void addControlConstructToMainSequence(
			ControlConstructType controlConstruct) {
		setReference(mainSeq.addNewControlConstructReference(), cocs
				.getControlConstructScheme().getId(), cocs
				.getControlConstructScheme().getVersion(),
				controlConstruct.getId(), controlConstruct.getVersion());
	}

	/**
	 * Create a processing note reflecting the relationship between a question
	 * item and a universe
	 * 
	 * @param queiReference
	 *            reference to the question item
	 * @param univReference
	 *            reference to the universe
	 * @return note
	 * @throws DDIFtpException
	 */
	public NoteDocument createQueiRefToUnivNote(
			LightXmlObjectType queiReference, LightXmlObjectType univReference)
			throws DDIFtpException {
		NoteDocument doc = NoteDocument.Factory.newInstance();
		NoteType type = doc.addNewNote();
		addIdAndVersion(type, ElementType.NOTE.getIdPrefix(), null);
		// processing
		type.setType(org.ddialliance.ddi3.xml.xmlbeans.reusable.NoteTypeCodeType.Enum
				.forInt(1));
		// label
		XmlBeansUtil.setTextOnMixedElement(type.addNewHeader(),
				"Quei to univ relation");
		// element reference
		setReference(type.addNewRelationship().addNewRelatedToReference(),
				queiReference.getParentId(), queiReference.getParentVersion(),
				queiReference.getId(), queiReference.getVersion());

		// note reference
		XmlBeansUtil.setTextOnMixedElement(type.addNewContent(),
				getLihtXmlObjectAsText(univReference));
		noteList.add(doc);
		return doc;
	}

	public String getLihtXmlObjectAsText(LightXmlObjectType lightXmlObject) {
		StringBuilder result = new StringBuilder();
		result.append("parentid__");
		result.append(lightXmlObject.getParentId());
		result.append("__parentVersion__");
		result.append(lightXmlObject.getParentVersion());
		result.append("__id__");
		result.append(lightXmlObject.getId());
		result.append("__version__");
		result.append(lightXmlObject.getVersion());
		// String[] test = result.toString().split("__");
		return result.toString();
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

	private void setReference(ReferenceType ref, String parentId,
			String parentVersion, String id, String version) {
		ModelAccessor.setReference(ref,
				createLightXmlObject(parentId, parentVersion, id, version));
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

	public void addIdAndVersion(AbstractMaintainableType abstractIdentifiable,
			String prefix, String postfix) throws DDIFtpException {
		IdentificationManager.getInstance().addIdentification(
				abstractIdentifiable, prefix, postfix);
		IdentificationManager.getInstance().addVersionInformation(
				(AbstractVersionableType) abstractIdentifiable, null, null);
		abstractIdentifiable.setAgency(agency);
	}

	String empty = "";

	private boolean checkString(String str) {
		return str != null && (!str.equals(empty));
	}

	private void setText(LabelType label, String text) throws DDIFtpException {
		XmlBeansUtil.setTextOnMixedElement(label, text);
		XmlBeansUtil.addTranslationAttributes(label,
				Translator.getLocaleLanguage(), false, true);
	}

	private void setText(StructuredStringType struct, String text)
			throws DDIFtpException {
		XmlBeansUtil.setTextOnMixedElement(struct, text);
		XmlBeansUtil.addTranslationAttributes(struct,
				Translator.getLocaleLanguage(), false, true);
	}

	private String getLabelText(String text) {
		if (text.length() > LABEL_LENGTH) {
			return text.substring(0, LABEL_LENGTH) + labelPostFix;
		} else
			return text;
	}

	/**
	 * Resolve references and label category schemes
	 * 
	 * @throws DDIFtpException
	 */
	public void postResolve() throws DDIFtpException {
		postResolveReferences();
		for (CategorySchemeDocument cats : catsList) {
			postResolveCategorySchemeLabels(cats);
		}
		changeUnivRefOnQuei();
		cleanSequenceForDublicateCcRefs();
	}

	// Links the control constructs to the real id of question constructs
	private void postResolveReferences() throws DDIFtpException {
		for (XmlObject xmlobject : postResolveItemRefs) {
			// sequence
			if (xmlobject instanceof SequenceType) {
				SequenceType xml = (SequenceType) xmlobject;
				for (ReferenceType ref : xml.getControlConstructReferenceList()) {
					changeCcReference(ref);
				}
			}
			// if then else
			else if (xmlobject instanceof IfThenElseType) {
				IfThenElseType xml = (IfThenElseType) xmlobject;
				// then
				changeCcReference(xml.getThenConstructReference());
				// elze
				if (xml.getElseConstructReference() != null) {
					changeCcReference(xml.getElseConstructReference());
				}

			}
			// debug guard
			else {
				throw new DDIFtpException(
						"Post resolve reference, type not implemented: "
								+ xmlobject.getClass().getName(),
						new Throwable());
			}
		}
	}

	private void cleanSequenceForDublicateCcRefs() throws DDIFtpException {
		String[] ccIds = new String[postCleanMainSeqItems.size()];
		int count = 0;
		for (String pseudoVarId : postCleanMainSeqItems) {
			if (pseudoVarIdToCcIdMap.get(pseudoVarId) != null) {
				ccIds[count] = pseudoVarIdToCcIdMap.get(pseudoVarId).getId();
			} else {
				throw new DDIFtpException(
						"Variable label name is not containing in list: "
								+ pseudoVarId, new Throwable());
			}
			count++;
		}
		for (Iterator<ReferenceType> iterator = mainSeq
				.getControlConstructReferenceList().iterator(); iterator
				.hasNext();) {
			String id = iterator.next().getIDArray(0).getStringValue();
			for (int i = 0; i < ccIds.length; i++) {
				if (ccIds[i].equals(id)) {
					iterator.remove();
					break;
				}
			}
		}
	}

	// Creates a category scheme label of a subset of of the labels the scheme
	// contains
	private void postResolveCategorySchemeLabels(CategorySchemeDocument cats)
			throws DDIFtpException {
		StringBuilder result = new StringBuilder();
		for (Iterator<CategoryType> iterator = cats.getCategoryScheme()
				.getCategoryList().iterator(); iterator.hasNext();) {
			CategoryType cat = iterator.next();
			if (!cat.getLabelList().isEmpty()) {
				result.append(getLabelText(XmlBeansUtil
						.getTextOnMixedElement(cat.getLabelList().get(0))));
				if (iterator.hasNext()) {
					result.append(", ");
				}
			} else {
				continue;
			}
		}
		if (result.length() > 0) {
			setText(cats.getCategoryScheme().addNewLabel(), result.toString());
		}
	}

	private ReferenceType changeCcReference(ReferenceType reference) {
		ReferenceType ref = null;
		String id = reference.getIDList().get(0).getStringValue();
		LightXmlObjectType newId = pseudoVarIdToCcIdMap.get(id);
		if (newId != null) {
			ref = ModelAccessor.setReference(reference, newId);
		}
		return ref;
	}

	private void changeUnivRefOnQuei() throws DDIFtpException {
		for (Entry<String, LightXmlObjectType> entry : pseudoVarIdToUnivIdMap
				.entrySet()) {
			for (QuestionSchemeDocument ques : quesList) {
				for (QuestionItemType quei : ques.getQuestionScheme()
						.getQuestionItemList()) {
					if (!quei.getUserIDList().isEmpty()
							&& quei.getUserIDArray(0).equals(entry.getKey())) {

						// create new note
						createQueiRefToUnivNote(
								createLightXmlObject(ques.getQuestionScheme()
										.getId(), ques.getQuestionScheme()
										.getVersion(), quei.getId(),
										quei.getVersion()), entry.getValue());
					}
				}
			}
		}
	}

	public List<UniverseSchemeDocument> getUnisList() {
		return unisList;
	}

	public List<ConceptSchemeDocument> getConsList() {
		return consList;
	}

	public List<QuestionSchemeDocument> getQuesList() {
		return quesList;
	}

	public List<MultipleQuestionItemDocument> getMqueList() {
		return mqueList;
	}

	public List<CategorySchemeDocument> getCatsList() {
		return catsList;
	}

	public List<ControlConstructSchemeDocument> getCocsList() {
		return cocsList;
	}

	public List<NoteDocument> getNotes() {
		return noteList;
	}

	public void resultToString() {
		log.debug("Universes:");
		listToString(unisList);
		log.debug("ConceptScheme:");
		listToString(consList);
		log.debug("QuestionSchemes:");
		listToString(quesList);
		log.debug("MultipleQuestions:");
		listToString(quesList);
		log.debug("Notes:");
		listToString(noteList);
		log.debug("CategorySchemes:");
		listToString(catsList);
		log.debug("Instrument:");
		listToString(cocsList);
	}

	private void listToString(List list) {
		for (Object doc : list) {
			log.debug(((XmlObject) doc).xmlText(xmlOptions));
		}
	}

	public void unsetMultipleQuestion() {
		mque = false;
	}
}
