package dk.dda.ddieditor.line.util;

import java.math.BigInteger;
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
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.CodeDomainDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.CodeDomainType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.ComputationItemDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.ComputationItemType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.ConditionalTextDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.ConditionalTextType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.ControlConstructSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.ControlConstructType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.DynamicTextType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.IfThenElseDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.IfThenElseType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.InstructionType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.InterviewerInstructionSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.LiteralTextDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.LiteralTextType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.MultipleQuestionItemDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.MultipleQuestionItemType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.NumericDomainDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.NumericDomainType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionConstructDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionConstructType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionItemDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionItemType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.SequenceDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.SequenceType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.StatementItemDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.StatementItemType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.TextType;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.CategorySchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.CategoryType;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.CodeSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.CodeType;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.VariableDocument;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.AbstractIdentifiableType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.AbstractMaintainableType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.AbstractVersionableType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.LabelType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.NameType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.NoteDocument;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.NumericTypeCodeType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.ProgrammingLanguageCodeType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.ReferenceType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.RepresentationType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.StructuredStringType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.UserIDType;
import org.ddialliance.ddieditor.logic.identification.IdentificationManager;
import org.ddialliance.ddieditor.logic.urn.ddi.ReferenceResolution;
import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddieditor.model.lightxmlobject.CustomListType;
import org.ddialliance.ddieditor.model.lightxmlobject.CustomType;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectListDocument;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectListType;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddieditor.model.lightxmlobject.impl.LabelTypeImpl;
import org.ddialliance.ddieditor.model.namespace.ddi3.Ddi3NamespaceHelper;
import org.ddialliance.ddieditor.model.resource.DDIResourceType;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.ddialliance.ddieditor.ui.dbxml.code.CodeSchemeDao;
import org.ddialliance.ddieditor.ui.dbxml.variable.VariableDao;
import org.ddialliance.ddieditor.ui.model.ElementType;
import org.ddialliance.ddieditor.ui.model.ModelIdentifingType;
import org.ddialliance.ddieditor.ui.model.code.CodeScheme;
import org.ddialliance.ddieditor.ui.model.instrument.ComputationItem;
import org.ddialliance.ddieditor.ui.model.instrument.ConditionalUtil;
import org.ddialliance.ddieditor.ui.model.instrument.IfThenElse;
import org.ddialliance.ddieditor.ui.model.variable.Variable;
import org.ddialliance.ddieditor.ui.preference.PreferenceUtil;
import org.ddialliance.ddieditor.ui.util.DialogUtil;
import org.ddialliance.ddieditor.ui.util.LanguageUtil;
import org.ddialliance.ddieditor.util.DdiEditorConfig;
import org.ddialliance.ddieditor.util.LightXmlObjectUtil;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.Translator;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;
import org.ddialliance.ddiftp.util.xml.XmlBeansUtil;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import dk.dda.ddieditor.line.osgi.Activator;
import dk.dda.ddieditor.line.view.ProblemView;
import dk.dda.ddieditor.line.view.TypeMarkerField;

public class Ddi3Helper {
	static private Log log = LogFactory
			.getLog(LogType.SYSTEM, Ddi3Helper.class);

	final String VARI_NAME = "Name";
	final String VARI_VAL_REP = "ValueRepresentation";
	final String VARI_CODES_REF = "CodeSchemeReference";

	List<UniverseSchemeDocument> unisList = new ArrayList<UniverseSchemeDocument>();
	List<NoteDocument> noteList = new ArrayList<NoteDocument>();
	List<ConceptSchemeDocument> consList = new ArrayList<ConceptSchemeDocument>();
	List<SequenceDocument> seqList = new ArrayList<SequenceDocument>();
	List<QuestionSchemeDocument> quesList = new ArrayList<QuestionSchemeDocument>();
	List<CategorySchemeDocument> catsList = new ArrayList<CategorySchemeDocument>();
	int catIndex;
	CodeScheme cods = null;
	List<ControlConstructSchemeDocument> cocsList = new ArrayList<ControlConstructSchemeDocument>();
	List<MultipleQuestionItemDocument> mqueList = new ArrayList<MultipleQuestionItemDocument>();
	Map<String, LightXmlObjectType> mqueToQuesMap = new HashMap<String, LightXmlObjectType>();

	UniverseSchemeDocument unis;
	public UniverseType univ;
	public SequenceDocument curSubSeq = null;
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
	public InterviewerInstructionSchemeDocument invs = null;
	private List<LightXmlObjectType> instructionList = new ArrayList<LightXmlObjectType>();
	CodeSchemeDao codeSchemedao;
	VariableDao variabledao;
	private List<LightXmlObjectType> vars = null;

	Map<String, LightXmlObjectType> pseudoVarIdToCcIdMap = new HashMap<String, LightXmlObjectType>();
	List<XmlObject> postResolveItemRefs = new ArrayList<XmlObject>();
	Map<String, LightXmlObjectType> pseudoVarIdToUnivIdMap = new HashMap<String, LightXmlObjectType>();
	List<String> postCleanSeqItems = new ArrayList<String>();
	Map<String, SequenceDocument> postResolveSeqRefs = new HashMap<String, SequenceDocument>();

	Map<String, LightXmlObjectType> postResolveSeqUniVarRefs = new HashMap<String, LightXmlObjectType>();

	public XmlOptions xmlOptions = new XmlOptions();

	int LABEL_LENGTH = 35;
	String labelPostFix = " ...";

	String agency = PreferenceUtil.getDdiAgency();
	String language = LanguageUtil.getOriginalLanguage();
	boolean isBatchMode = false;

	public Ddi3Helper() throws DDIFtpException {
		xmlOptions.setSaveAggressiveNamespaces();
		xmlOptions.setSavePrettyPrint();
		xmlOptions.setSaveOuter();
	}

	public Ddi3Helper(boolean isBatchMode) throws DDIFtpException {
		super();
		this.yesToAllErrors = isBatchMode;
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
		if (ques != null) {
			quesList.add(ques);
		}

		// code scheme
		codeSchemedao = new CodeSchemeDao();

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

		// interviewer instruction scheme
		if (invs == null) {
			invs = InterviewerInstructionSchemeDocument.Factory.newInstance();
			invs.addNewInterviewerInstructionScheme();
			IdentificationManager.getInstance().addIdentification(
					invs.getInterviewerInstructionScheme(),
					ElementType.INTERVIEWER_INSTRUCTION_SCHEME.getIdPrefix(),
					null);
			IdentificationManager.getInstance().addVersionInformation(
					invs.getInterviewerInstructionScheme(), null, null);
		}

		// variables
		variabledao = new VariableDao();
	}

	// =universe label=universe description
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

	// ==sequence-id==sequence label
	public void createSequence(String pseudoId, String label)
			throws DDIFtpException {
		SequenceDocument result = SequenceDocument.Factory.newInstance();
		result.addNewSequence();
		addIdAndVersion(result.getSequence(),
				ElementType.SEQUENCE.getIdPrefix(), null);

		// pseudoSequenceId
		UserIDType userId = result.getSequence().addNewUserID();
		userId.setType(Ddi3NamespaceHelper.SEQ_USER_ID_TYPE);
		userId.setStringValue(pseudoId);
		postResolveSeqRefs.put(pseudoId, result);

		// label
		if (checkString(label))
			setText(result.getSequence().addNewLabel(), label);

		curSubSeq = result;
		seqList.add(result);
		// comment out 20121013
		// createUniverse(label, label);
	}

	// ==end==
	public void endSequence() {
		curSubSeq = null;
	}

	/**
	 * Get Value Representation of Variable.
	 * 
	 * @param pseudoVariRef
	 *            - Pseudo Variable reference e.g. 'v1'
	 * @return Custom Type e.g. Numeric or Code Scheme reference.
	 * @throws DDIFtpException
	 */
	private CustomType getValueRepresentation(String pseudoVariRef)
			throws DDIFtpException {
		CustomType result = null;

		String id = "";
		boolean variFound = false;
		// for all light variable elements
		for (LightXmlObjectType vari : getVariablesLight()) {
			// for all custom list elements of this variable
			for (CustomListType cusList : vari.getCustomListList()) {
				// get pseudo variable ID
				if (cusList.getType().equals(VARI_NAME)) {
					for (CustomType cusQueiRef : cusList.getCustomList()) {
						id = XmlBeansUtil.getTextOnMixedElement(cusQueiRef);
						if (id.equals(pseudoVariRef)) {
							variFound = true;
							continue;
						}
					}
				}
				// get Value Representation of matching variable
				if (variFound && cusList.getType().equals(VARI_VAL_REP)) {
					for (CustomType valueRep : cusList.getCustomList()) {
						result = valueRep;
					}
				}
			}
			if (variFound) {
				break;
			}
		}
		return result;
	}

	private void createInitQuestionScheme() throws DDIFtpException {
		createQuestionScheme(null, null);
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

		createConcept(label, description);

		ques = result;
		quesList.add(result);
		quesIsNewList.add(result.getQuestionScheme().getId());
	}

	public void createConcept(String label, String description)
			throws DDIFtpException {
		// avoid init concept
		if (label != null) {
			ConceptType result = cons.getConceptScheme().addNewConcept();
			addIdAndVersion(result, ElementType.CONCEPT.getIdPrefix(), null);
			setText(result.addNewLabel(), label);
			setText(result.addNewDescription(), description);
			conc = result;
		}
	}

	public void createQuestion(String pseudoVariableId, String text)
			throws DDIFtpException {
		if (cats != null && cods != null) {
			verifyCodeSchmeCategorySchemeSizes(); // TODO mque probem
		}

		QuestionItemType result = null;
		if (mque) {
			result = mquem.getMultipleQuestionItem().getSubQuestions()
					.addNewQuestionItem();
		} else {
			if (ques == null) {
				createInitQuestionScheme();
			}
			result = ques.getQuestionScheme().addNewQuestionItem();
		}
		addIdAndVersion(result, ElementType.QUESTION_ITEM.getIdPrefix(), null);

		// name
		NameType name = result.addNewQuestionItemName();
		name.setStringValue(getLabelText(text));
		XmlBeansUtil.addTranslationAttributes(name, language, false, true);

		// variable reference pseudoVariableId
		UserIDType userId = result.addNewUserID();
		userId.setType(Ddi3NamespaceHelper.QUEI_VAR_USER_ID_TYPE);
		userId.setStringValue(pseudoVariableId);

		// set text
		DynamicTextType dynamicText = result.addNewQuestionText();
		TextType textType = dynamicText.addNewText();
		XmlBeansUtil.addTranslationAttributes(dynamicText, language, false,
				true);
		LiteralTextType lTextType = (LiteralTextType) textType.substitute(
				LiteralTextDocument.type.getDocumentElementName(),
				LiteralTextType.type);
		lTextType.addNewText();
		XmlBeansUtil.setTextOnMixedElement(lTextType.getText(), text);

		// concept reference
		if (cons != null && conc != null) {
			setReference(result.addNewConceptReference(), cons
					.getConceptScheme().getId(), cons.getConceptScheme()
					.getVersion(), conc.getId(), conc.getVersion());
		}

		CustomType customType = getValueRepresentation(pseudoVariableId);
		if (customType == null) {
			handleParseError(ElementType.CATEGORY, Translator.trans(
					"line.error.valueRepresentationnotfound",
					new Object[] { pseudoVariableId }));
		} else {
			if (customType.getOption() != null
					&& customType.getOption().equals("NumericTypeCodeType")) {
				if (XmlBeansUtil.getTextOnMixedElement(customType).equals(
						"Numeric")) {
					// This is a Numeric Representation:
					RepresentationType rt = result.addNewResponseDomain();
					NumericDomainType ndt = (NumericDomainType) rt
							.substitute(NumericDomainDocument.type
									.getDocumentElementName(),
									NumericDomainType.type);
					if (customType.getValue() != null
							&& customType.getValue().equals("Double")) {
						ndt.setType(NumericTypeCodeType.DOUBLE);
						// TODO Get Decimal Position from Variable
						ndt.setDecimalPositions(new BigInteger("0"));
					}
				}
			} else if (customType.getValue() != null
					&& customType.getValue().equals("CodeSchemeReference")) {
				// This is a Code Scheme reference representation:
				String codeSchemeReference = XmlBeansUtil
						.getTextOnMixedElement(customType);
				if (codeSchemeReference.length() > 0) {
					RepresentationType rt = result.addNewResponseDomain();
					CodeDomainType cdt = (CodeDomainType) rt.substitute(
							CodeDomainDocument.type.getDocumentElementName(),
							CodeDomainType.type);
					cdt.addNewCodeSchemeReference().addNewID()
							.setStringValue(codeSchemeReference);
				}
			}
		}

		// control construct
		if (!mque) {
			QuestionConstructType qc = createQuestionConstruct(ques
					.getQuestionScheme().getId(), ques.getQuestionScheme()
					.getVersion(), result.getId(), result.getVersion());
			setText(qc.addNewLabel(), getLabelText(text));

			// pseudo variable id map
			pseudoVarIdToCcIdMap.put(
					pseudoVariableId.substring(1),
					createLightXmlObject(cocs.getControlConstructScheme()
							.getId(), cocs.getControlConstructScheme()
							.getVersion(), qc.getId(), qc.getVersion()));

		} else {
			// pseudo var id map
			pseudoVarIdToCcIdMap
					.put(pseudoVariableId.substring(1),
							createLightXmlObject(cocs
									.getControlConstructScheme().getId(), cocs
									.getControlConstructScheme().getVersion(),
									mquecc.getId(), mquecc.getVersion()));
		}
		quei = result;
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

		// add interview instruction ref
		addInterviewerInstructions(questionConstruct);

		// add cc ref to seq
		addControlConstructToSequence(questionConstruct);

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
			IdentificationManager.getInstance().addReferenceInformation(
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

		// name
		StringBuilder name = new StringBuilder();
		if (text.length() > LABEL_LENGTH) {
			name.append(text.substring(0, LABEL_LENGTH));
			name.append(" ...");
		} else {
			name.append(text);
		}
		type.addNewMultipleQuestionItemName().setStringValue(name.toString());

		if (ques == null) {
			createInitQuestionScheme();
		}
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

	private CategorySchemeDocument createCategoryScheme()
			throws DDIFtpException {
		CategorySchemeDocument catsDoc = CategorySchemeDocument.Factory
				.newInstance();
		catsDoc.addNewCategoryScheme();
		addIdAndVersion(catsDoc.getCategoryScheme(),
				ElementType.CATEGORY_SCHEME.getIdPrefix(), null);
		catsList.add(catsDoc);
		cats = catsDoc;

		// set pseudo variable id as UserID element
		UserIDType userId = null;
		if (quei != null) {
			for (UserIDType userIdTmp : quei.getUserIDList()) {
				if (userIdTmp.getType().equals(
						Ddi3NamespaceHelper.QUEI_VAR_USER_ID_TYPE)) {
					userId = userIdTmp;
					break;
				}
			}
			cats.getCategoryScheme()
					.setUserIDArray(new UserIDType[] { userId });
		}

		return cats;
	}

	public void createCategory(String text) throws DDIFtpException {
		// create category scheme - if not already done
		if (cats == null) {
			createCategoryScheme();
			catIndex = 0;
		} else {
			catIndex++;
		}

		// create category
		CategoryType cat = cats.getCategoryScheme().addNewCategory();
		addIdAndVersion(cat, ElementType.CATEGORY.getIdPrefix(), null);
		setText(cat.addNewLabel(), text);

		// assign category reference to code of code scheme
		// get id of code scheme
		if (quei == null) { // question item guard
			handleParseError(ElementType.CATEGORY,
					Translator.trans("line.error.noqueitocategory", text));
			return;
		}

		// identification
		UserIDType pseudoVariableId = getPseudoVariableId();
		String queiName = getQuestionItemName();
		resolveCodeScheme(text, pseudoVariableId.getStringValue(), queiName);

		// add cate
		try {
			if (cods != null) {
				IdentificationManager.getInstance()
						.addReferenceInformation(
								cods.getCodes().get(catIndex)
										.addNewCategoryReference(),
								LightXmlObjectUtil.createLightXmlObject(cats
										.getCategoryScheme().getAgency(), cats
										.getCategoryScheme().getId(), cats
										.getCategoryScheme().getVersion(), cat
										.getId(), cat.getVersion(),
										ElementType.CATEGORY.getElementName()));
			}
		} catch (IndexOutOfBoundsException e) {
			handleParseError(ElementType.CATEGORY, Translator.trans(
					"line.error.nocodetocategory", new Object[] { text,
							queiName, pseudoVariableId.getStringValue() }));
			return;
		}
	}

	/**
	 * Reuse referred category scheme
	 * 
	 * @param reuseVarId
	 *            variable id where category scheme were defined
	 * @throws DDIFtpException
	 */
	public void reuseCategories(String reuseVarId) throws DDIFtpException {
		// cods to set
		UserIDType pseudoVariableId = getPseudoVariableId();
		String queiName = getQuestionItemName();
		String errorText = Translator.trans("line.category.textrefered",
				reuseVarId);
		resolveCodeScheme(errorText, pseudoVariableId.getStringValue(),
				queiName);

		// resolve cods to reuse
		String reuseCodsId = XmlBeansUtil
				.getTextOnMixedElement(getValueRepresentation(reuseVarId
						.toUpperCase()));
		CodeScheme reuseCods = null;
		try {
			// lookup reuse cods
			LightXmlObjectListDocument codsList = DdiManager.getInstance()
					.getCodeSchemesLight(reuseCodsId, null, null, null);
			if (codsList == null
					|| codsList.getLightXmlObjectList()
							.sizeOfLightXmlObjectArray() != 1) {
				handleParseError(ElementType.CATEGORY, Translator.trans(
						"line.error.codscatUnexpect", new Object[] { errorText,
								pseudoVariableId, queiName }));
				return;
			}
			LightXmlObjectType reuseCodsLight = codsList
					.getLightXmlObjectList().getLightXmlObjectArray(0);
			reuseCods = (CodeScheme) codeSchemedao.getModel(reuseCodsId,
					reuseCodsLight.getVersion(), reuseCodsLight.getParentId(),
					reuseCodsLight.getParentVersion());

			// find reuse cods ref cats in this.catsList
			ReferenceResolution refResolv = new ReferenceResolution(
					reuseCods.getCategorySchemeReference());
			for (CategorySchemeDocument catsTmp : catsList) {
				if (catsTmp.getCategoryScheme().getId()
						.equals(refResolv.getId())) {
					cats = catsTmp;
					break;
				}
			}

			// guard lookup cods ref cats
			if (cats == null) {
				LightXmlObjectListType lightCatsList = DdiManager
						.getInstance()
						.getCategorySchemesLight(refResolv.getId(), null, null,
								null).getLightXmlObjectList();
				for (LightXmlObjectType lightCats : lightCatsList
						.getLightXmlObjectList()) {
					if (lightCats.getId().equals(refResolv.getId())) {
						cats = DdiManager.getInstance().getCategoryScheme(
								refResolv.getId(), lightCats.getVersion(),
								lightCats.getParentId(),
								lightCats.getParentVersion());
						break;
					}
				}
			}
		} catch (Exception e) {
			throw new DDIFtpException(e.getMessage(), e);
		}
		if (cats == null) { // guard
			handleParseError(ElementType.CATEGORY, Translator.trans(
					"line.error.nocodetocategory", new Object[] { errorText,
							queiName, pseudoVariableId.getStringValue() }));
			return;
		}

		// set default cats on cods
		cods.getDocument()
				.getCodeScheme()
				.setCategorySchemeReference(
						reuseCods.getCategorySchemeReference());

		// set cate on code
		int count = 0;
		for (Iterator<CodeType> iterator = cods.getCodes().iterator(); iterator
				.hasNext(); count++) {
			CodeType code = iterator.next();
			try {
				IdentificationManager.getInstance().addReferenceInformation(
						code.addNewCategoryReference(),
						LightXmlObjectUtil.createLightXmlObject(
								cats.getCategoryScheme().getAgency(),
								cats.getCategoryScheme().getId(),
								cats.getCategoryScheme().getVersion(),
								cats.getCategoryScheme().getCategoryList()
										.get(count).getId(), cats
										.getCategoryScheme().getCategoryList()
										.get(count).getVersion(),
								ElementType.CATEGORY_SCHEME.getElementName()));
			} catch (IndexOutOfBoundsException e) {
				handleParseError(ElementType.CATEGORY, Translator.trans(
						"line.error.nocodetocategory",
						new Object[] { errorText, queiName,
								pseudoVariableId.getStringValue() }));
				return;
			}
		}
	}

	public static CodeSchemeDocument getRPCodeSchemeByReference(
			ReferenceResolution refRes) throws Exception {
		CodeSchemeDocument result = null;

		List<DDIResourceType> resources = PersistenceManager.getInstance()
				.getResources();
		String workingresource = PersistenceManager.getInstance()
				.getWorkingResource();

		for (DDIResourceType resource : resources) {
			if (!resource.equals(workingresource)) {
				PersistenceManager.getInstance().setWorkingResource(
						resource.getOrgName());
				List<LightXmlObjectType> codeSchemeRefList = DdiManager
						.getInstance()
						.getCodeSchemesLight(null, null, null, null)
						.getLightXmlObjectList().getLightXmlObjectList();
				for (LightXmlObjectType lightXmlObject : codeSchemeRefList) {
					if (lightXmlObject.getId().equals(refRes.getId())) {
						result = DdiManager.getInstance().getCodeScheme(
								lightXmlObject.getId(),
								lightXmlObject.getVersion(),
								lightXmlObject.getParentId(),
								lightXmlObject.getParentVersion());
						break;
					}
				}
				if (result != null) {
					break;
				}
			}
		}

		PersistenceManager.getInstance().setWorkingResource(workingresource);
		return result;
	}

	private static CategorySchemeDocument getRPCategorySchemeByLabel(
			String refRes) throws Exception {
		List<LightXmlObjectType> categorySchemeRefList = DdiManager
				.getInstance().getCategorySchemesLight(null, null, null, null)
				.getLightXmlObjectList().getLightXmlObjectList();
		CategorySchemeDocument result = null;
		for (LightXmlObjectType lightXmlObject : categorySchemeRefList) {
			LabelTypeImpl labelType = (LabelTypeImpl) XmlBeansUtil
					.getDefaultLangElement(lightXmlObject.getLabelList());
			String label = XmlBeansUtil.getTextOnMixedElement(labelType);
			if (label.equals(refRes)) {
				result = DdiManager.getInstance().getRPCategoryScheme(
						lightXmlObject.getId(), lightXmlObject.getVersion(),
						lightXmlObject.getParentId(),
						lightXmlObject.getParentVersion());
				break;
			}
		}
		return result;
	}

	/**
	 * Reuse referred RP category scheme
	 * 
	 * @param reuseVarId
	 *            variable id where category scheme were defined
	 * @throws DDIFtpException
	 */
	public void useRPCategories(String reuseCodsId) throws DDIFtpException {

		// set cods
		UserIDType pseudoVariableId = getPseudoVariableId();
		String queiName = getQuestionItemName();
		String errorText = Translator.trans("line.category.textrefered",
				reuseCodsId);
		resolveCodeScheme(errorText, pseudoVariableId.getStringValue(),
				queiName);

		CategorySchemeDocument result = null;

		List<DDIResourceType> resources = PersistenceManager.getInstance()
				.getResources();
		String workingresource = PersistenceManager.getInstance()
				.getWorkingResource();

		for (DDIResourceType resource : resources) {
			if (!resource.getOrgName().equals(workingresource)) {
				PersistenceManager.getInstance().setWorkingResource(
						resource.getOrgName());
				try {
					result = getRPCategorySchemeByLabel(reuseCodsId);
				} catch (Exception e) {
					handleParseError(ElementType.CATEGORY, Translator.trans(
							"line.error.rpcategoryerror",
							new Object[] { reuseCodsId, queiName,
									pseudoVariableId.getStringValue() },
							e.getMessage()));
					return;
				}
				if (result != null) {
					break;
				}
			}
		}
		if (result == null) {
			handleParseError(ElementType.CATEGORY, Translator.trans(
					"line.error.norpcategoryfound", new Object[] { reuseCodsId,
							queiName, pseudoVariableId.getStringValue() }));
			return;
		}
		cats = result;

		// set cate on code
		int count = 0;
		for (Iterator<CodeType> iterator = cods.getCodes().iterator(); iterator
				.hasNext(); count++) {
			CodeType code = iterator.next();
			try {
				IdentificationManager.getInstance().addReferenceInformation(
						code.addNewCategoryReference(),
						LightXmlObjectUtil.createLightXmlObject(
								cats.getCategoryScheme().getAgency(),
								cats.getCategoryScheme().getId(),
								cats.getCategoryScheme().getVersion(),
								cats.getCategoryScheme().getCategoryList()
										.get(count).getId(), cats
										.getCategoryScheme().getCategoryList()
										.get(count).getVersion(),
								ElementType.CATEGORY_SCHEME.getElementName()));
			} catch (IndexOutOfBoundsException e) {
				handleParseError(ElementType.CATEGORY, Translator.trans(
						"line.error.nocodetocategory",
						new Object[] { errorText, queiName,
								pseudoVariableId.getStringValue() }));
				return;
			}
		}

		PersistenceManager.getInstance().setWorkingResource(workingresource);
	}

	private void resolveCodeScheme(String text, String pseudoVariableId,
			String queiName) throws DDIFtpException {
		CustomType customType = getValueRepresentation(pseudoVariableId);
		if (customType == null || customType.getValue() == null) {
			handleParseError(
					ElementType.CATEGORY,
					Translator.trans("line.error.noncodevari", new Object[] {
							text, pseudoVariableId, queiName }));
			return;
		}
		String codsId = XmlBeansUtil.getTextOnMixedElement(customType);
		if (codsId.equals("")) {
			handleParseError(
					ElementType.CATEGORY,
					Translator.trans("line.error.noncodevari", new Object[] {
							text, pseudoVariableId, queiName }));
			return;
		}

		// get code scheme
		LightXmlObjectListDocument list = null;
		if (cods == null) {
			try {
				list = DdiManager.getInstance().getCodeSchemesLight(codsId,
						null, null, null);
				if (list == null
						|| list.getLightXmlObjectList()
								.sizeOfLightXmlObjectArray() != 1) {
					handleParseError(ElementType.CATEGORY, Translator.trans(
							"line.error.codscatUnexpect", new Object[] { text,
									pseudoVariableId, queiName }));
					return;
				}
				String version = XmlBeansUtil.getXmlAttributeValue(list
						.getLightXmlObjectList().xmlText(), "version=\"");
				String parentId = XmlBeansUtil.getXmlAttributeValue(list
						.getLightXmlObjectList().xmlText(), "parentId=\"");
				String parentVersion = XmlBeansUtil.getXmlAttributeValue(list
						.getLightXmlObjectList().xmlText(), "parentVersion=\"");
				cods = (CodeScheme) codeSchemedao.getModel(codsId, version,
						parentId, parentVersion);
			} catch (Exception e) {
				throw new DDIFtpException(e.getMessage());
			}
		}
	}

	private UserIDType getPseudoVariableId() {
		UserIDType pseudoVariableId = null;
		for (UserIDType userIdTmp : quei.getUserIDList()) {
			if (userIdTmp.getType().equals(
					Ddi3NamespaceHelper.QUEI_VAR_USER_ID_TYPE)) {
				pseudoVariableId = userIdTmp;
				return pseudoVariableId;
			}
		}
		return pseudoVariableId;
	}

	private String getQuestionItemName() throws DDIFtpException {
		String queiName = "";
		if (!quei.getQuestionItemNameList().isEmpty()) {
			queiName = ((NameType) XmlBeansUtil.getDefaultLangElement(quei
					.getQuestionItemNameList())).getStringValue();
		} else {
			queiName = quei.getId();
		}

		return queiName;
	}

	private void verifyCodeSchmeCategorySchemeSizes() throws DDIFtpException {
		// check size of cats and cods
		if (cats.getCategoryScheme().getCategoryList().size() != cods
				.getDocument().getCodeScheme().getCodeList().size()) {
			UserIDType pVariId = getPseudoVariableId();
			String queiLabel = getQuestionItemName();
			handleParseError(ElementType.CATEGORY, Translator.trans(
					"line.error.categoriesmissing", new Object[] {
							pVariId == null ? "" : pVariId.getStringValue(),
							queiLabel == null ? "" : queiLabel,
							cods.getDocument().getCodeScheme().getCodeList()
									.size()
									+ "",
							cats.getCategoryScheme().getCategoryList().size()
									+ "" }));
		}

		// add default cats
		if (cods.getCategorySchemeReference() == null) {
			IdentificationManager.getInstance().addReferenceInformation(
					cods.getDocument().getCodeScheme()
							.addNewCategorySchemeReference(),
					LightXmlObjectUtil.createLightXmlObject(cats
							.getCategoryScheme().getAgency(), null, null, cats
							.getCategoryScheme().getId(), cats
							.getCategoryScheme().getVersion(),
							ElementType.CATEGORY_SCHEME.getElementName()));
		}

		// cods update
		codeSchemedao.update(cods);

		// reset
		cats = null;
		cods = null;
	}

	public void createStatementItem(String statementText) throws Exception {
		SequenceType seq = null;
		if (curSubSeq != null) {
			seq = curSubSeq.getSequence();
		} else {
			seq = mainSeq;
		}
		createStatementItem(statementText, seq);
	}

	private void createStatementItem(String statementText, SequenceType seq)
			throws Exception {
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
		XmlBeansUtil.addTranslationAttributes(dynamicText, language, false,
				true);

		TextType textType = dynamicText.addNewText();
		LiteralTextType lTextType = (LiteralTextType) textType.substitute(
				LiteralTextDocument.type.getDocumentElementName(),
				LiteralTextType.type);
		lTextType.addNewText();
		XmlBeansUtil.setTextOnMixedElement(lTextType.getText(), statementText);

		// ref statement item
		IdentificationManager.getInstance().addReferenceInformation(
				seq.addNewControlConstructReference(),
				createLightXmlObject(cocs.getControlConstructScheme().getId(),
						cocs.getControlConstructScheme().getVersion(),
						stai.getId(), stai.getVersion()));
	}

	public void createInterviewerInstruction(String text, String condition)
			throws DDIFtpException {
		// create interviewer instruction
		InstructionType intv = invs.getInterviewerInstructionScheme()
				.addNewInstruction();
		IdentificationManager.getInstance().addIdentification(intv,
				ElementType.INSTRUCTION.getIdPrefix(), null);
		IdentificationManager.getInstance().addVersionInformation(intv, null,
				null);

		// label
		LabelType labelType = intv.addNewLabel();
		XmlBeansUtil.addTranslationAttributes(labelType,
				DdiEditorConfig.get(DdiEditorConfig.DDI_LANGUAGE), false, true);
		XmlBeansUtil.setTextOnMixedElement(labelType, getLabelText(text));

		// set text
		DynamicTextType dText = intv.addNewInstructionText();
		XmlBeansUtil.addTranslationAttributes(dText,
				DdiEditorConfig.get(DdiEditorConfig.DDI_LANGUAGE), false, true);
		TextType aTextType = dText.addNewText();
		LiteralTextType lTextType = (LiteralTextType) aTextType.substitute(
				LiteralTextDocument.type.getDocumentElementName(),
				LiteralTextType.type);
		XmlBeansUtil.setTextOnMixedElement(lTextType.addNewText(), text);

		// condition
		if (condition != null) {
			TextType bTextType = dText.addNewText();
			ConditionalTextType cTextType = (ConditionalTextType) bTextType
					.substitute(ConditionalTextDocument.type
							.getDocumentElementName(), ConditionalTextType.type);

			ProgrammingLanguageCodeType code = cTextType.addNewExpression()
					.addNewCode();
			code.setProgrammingLanguage(DdiEditorConfig
					.get(DdiEditorConfig.DDI_INSTRUMENT_PROGRAM_LANG));
			code.setStringValue(condition);
		}

		// add for inclusion in other control constructs
		instructionList.add(LightXmlObjectUtil.createLightXmlObject(invs
				.getInterviewerInstructionScheme().getAgency(), invs
				.getInterviewerInstructionScheme().getId(), invs
				.getInterviewerInstructionScheme().getVersion(), intv.getId(),
				intv.getVersion(), ElementType.INTERVIEWER_INSTRUCTION_SCHEME
						.getElementName()));
	}

	private void addInterviewerInstructions(ControlConstructType cc)
			throws DDIFtpException {
		for (LightXmlObjectType intvLight : instructionList) {
			IdentificationManager.getInstance().addReferenceInformation(
					cc.addNewInterviewerInstructionReference(), intvLight);
		}
		instructionList.clear();
	}

	public void createComputationItem(String condition, String variref,
			String label) throws Exception {
		// if then else
		ComputationItemType comp = (ComputationItemType) cocs
				.getControlConstructScheme()
				.addNewControlConstruct()
				.substitute(
						ComputationItemDocument.type.getDocumentElementName(),
						ComputationItemType.type);
		addIdAndVersion(comp, ElementType.COMPUTATION_ITEM.getIdPrefix(), null);
		setText(comp.addNewLabel(), label);

		// model
		ComputationItem model = getComputationItemModel(comp);
		// ProgrammingLanguageCodeType
		condition = "value " + condition;
		model.applyChange(condition, ProgrammingLanguageCodeType.class);
		// ProgrammingLanguageCodeType/@programmingLanguage
		model.applyChange(agency, ModelIdentifingType.Type_A.class);

		// variable reference
		model.applyChange(createLightXmlObject("", "", variref, ""),
				ModelIdentifingType.Type_B.class);
		postResolveItemRefs.add(model.getDocument().getComputationItem());
		addControlConstructToSequence(model.getDocument().getComputationItem());
	}

	private ComputationItem getComputationItemModel(ComputationItemType comp)
			throws Exception {
		ComputationItem model = new ComputationItem(
				ComputationItemDocument.Factory.parse(comp.xmlText(xmlOptions)),
				null, null);
		model.setCreate(true);
		return model;
	}

	private Variable getVariable(String id, String version, String parentId,
			String parentVersion) throws Exception {

		return (Variable) variabledao.getModel(id, version, parentId,
				parentVersion);
	}

	private void setUniverseRefOnVariable(boolean add, Variable variable,
			LightXmlObjectType universe) throws Exception {
		variable.setCreate(true);
		variable.executeChange(universe, add ? ModelIdentifingType.Type_M.class
				: ModelIdentifingType.Type_C.class);
		new VariableDao().update(variable);
	}

	private void updateVariableUniverseReference(boolean add,
			String pseudoVariRef, LightXmlObjectType univerId) throws Exception {
		String pseudoVariableId = "";
		String variableId = "";
		String variableVersion = "";
		String parentId = "";
		String parentVersion = "";
		boolean variFound = false;
		// for all light variable elements
		for (LightXmlObjectType vari : getVariablesLight()) {
			// for all custom list elements of this variable
			for (CustomListType cusList : vari.getCustomListList()) {
				// get pseudo variable ID
				if (cusList.getType().equals(VARI_NAME)) {
					for (CustomType cusQueiRef : cusList.getCustomList()) {
						pseudoVariableId = XmlBeansUtil
								.getTextOnMixedElement(cusQueiRef);
						if (pseudoVariableId.substring(1).equals(
								pseudoVariRef.substring(1))) {
							variFound = true;
							variableId = vari.getId();
							variableVersion = XmlBeansUtil
									.getXmlAttributeValue(vari.xmlText(),
											"version=\"");
							parentId = XmlBeansUtil.getXmlAttributeValue(
									vari.xmlText(), "parentId=\"");
							parentVersion = XmlBeansUtil.getXmlAttributeValue(
									vari.xmlText(), "parentVersion=\"");
							continue;
						}
					}
				}
			}
			if (variFound) {
				break;
			}
		}
		if (!variFound) {
			// look into sequences and apply reference to varis
			postResolveSeqUniVarRefs.put(pseudoVariRef, univerId);
			return;
		}

		// get variable and assign universe reference
		setUniverseRefOnVariable(
				add,
				getVariable(variableId, variableVersion, parentId,
						parentVersion), univerId);
	}

	public void createIfThenElse(String condition, String then, String elze,
			String statementText) throws Exception {
		UniverseType prevUniv = univ;
		SequenceType seq = null;

		// create new universe
		createUniverse(getLabelText(statementText), getLabelText(statementText));
		LightXmlObjectType univLight = createLightXmlObject(unis
				.getUniverseScheme().getId(), unis.getUniverseScheme()
				.getVersion(), univ.getId(), univ.getVersion());
		pseudoVarIdToUnivIdMap.put(then, univLight);
		updateVariableUniverseReference(false, then, univLight);

		// create sub-sequence
		seq = (SequenceType) cocs
				.getControlConstructScheme()
				.addNewControlConstruct()
				.substitute(SequenceDocument.type.getDocumentElementName(),
						SequenceType.type);
		addIdAndVersion(seq, ElementType.SEQUENCE.getIdPrefix(), null);
		String labelSeq = ElementType.SEQUENCE.getIdPrefix() + "-";

		// - statementText;
		if (quei != null) {
			labelSeq = labelSeq
					+ getLabelText(quei.getQuestionItemNameArray(0)
							.getStringValue());
		} else {
			labelSeq = labelSeq + statementText;
		}
		setText(seq.addNewLabel(), labelSeq);

		// create statement addControlConstructToSequenceitem
		createStatementItem(statementText, seq);

		// ref. next question item
		IdentificationManager.getInstance().addReferenceInformation(
				seq.addNewControlConstructReference(),
				createLightXmlObject(cocs.getControlConstructScheme().getId(),
						cocs.getControlConstructScheme().getVersion(), then,
						"1.0.0"));
		postResolveItemRefs.add(seq);

		// create if then else
		IfThenElseType ifthenelse = (IfThenElseType) cocs
				.getControlConstructScheme()
				.addNewControlConstruct()
				.substitute(IfThenElseDocument.type.getDocumentElementName(),
						IfThenElseType.type);
		addIdAndVersion(ifthenelse, ElementType.IF_THEN_ELSE.getIdPrefix(),
				null);

		IfThenElseDocument doc = IfThenElseDocument.Factory.newInstance();
		doc.setIfThenElse(ifthenelse);
		IfThenElse model = new IfThenElse(doc, null, null);
		model.setCreate(true);

		// ProgrammingLanguageCodeType
		model.applyChange(condition, ProgrammingLanguageCodeType.class);
		// ProgrammingLanguageCodeType/@programmingLanguage
		model.applyChange(DdiEditorConfig
				.get(DdiEditorConfig.DDA_DDI_INSTRUMENT_PROGRAM_LANG),
				ModelIdentifingType.Type_A.class);

		// question reference(s)
		String[] varIDs = ConditionalUtil.extractUniqueIDs(condition);
		for (int i = 0; i < varIDs.length; i++) {
			model.applyChange(
					createLightXmlObject(null, null, varIDs[i], null),
					ModelIdentifingType.Type_B.class);
		}

		// then reference
		if (then.indexOf("V") == 0) {
			// question ref.
			model.applyChange(
					createLightXmlObject(cocs.getControlConstructScheme()
							.getId(), cocs.getControlConstructScheme()
							.getVersion(), seq.getId(), seq.getVersion()),
					ModelIdentifingType.Type_C.class);
			postCleanSeqItems.add(then.substring(1));
		} else {
			// sequence ref.
			model.applyChange(createLightXmlObject(null, null, then, null),
					ModelIdentifingType.Type_C.class);
		}

		// else reference
		if (elze != null) {
			model.applyChange(createLightXmlObject(null, null, elze, null),
					ModelIdentifingType.Type_D.class);
			if (then.indexOf("V") == 0) {
				// post clean seq for created quei cc
				postCleanSeqItems.add(elze.substring(1));
			}
		}

		// label
		setText(model.getDocument().getIfThenElse().addNewLabel(),
				getLabelText(statementText));
		ifthenelse.set(model.getDocument().getIfThenElse());

		// add to seq
		addControlConstructToSequence(ifthenelse);

		// add to post resolve items
		postResolveItemRefs.add(ifthenelse);

		// reset univ
		univ = prevUniv;
	}

	private void addControlConstructToSequence(
			ControlConstructType controlConstruct) throws DDIFtpException {
		SequenceType seq = null;
		if (curSubSeq != null) {
			seq = curSubSeq.getSequence();
		} else {
			seq = mainSeq;
		}
		setReference(seq.addNewControlConstructReference(), cocs
				.getControlConstructScheme().getId(), cocs
				.getControlConstructScheme().getVersion(),
				controlConstruct.getId(), controlConstruct.getVersion());
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
			String parentVersion, String id, String version)
			throws DDIFtpException {
		IdentificationManager.getInstance().addReferenceInformation(ref,
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
		XmlBeansUtil.addTranslationAttributes(label, language, false, true);
	}

	private void setText(StructuredStringType struct, String text)
			throws DDIFtpException {
		XmlBeansUtil.setTextOnMixedElement(struct, text);
		XmlBeansUtil.addTranslationAttributes(struct, language, false, true);
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
	public void postResolve() throws Exception {
		// check cats and cods
		if (cats != null && cods != null) {
			verifyCodeSchmeCategorySchemeSizes();
		}

		// post resolve seq uni var refs
		postResolveSeqUniVarRefs();

		// post resolve refs
		// TODO optimization at loot db hits on question id ref
		postResolveReferences();

		// cat ref resolve
		for (CategorySchemeDocument cats : catsList) {
			postResolveCategorySchemeLabels(cats);
		}

		// clean cc for duplicates
		cleanSequenceForDublicateCcRefs();
	}

	// 1 Links the control constructs to the real id of question constructs
	// 2 Links the control constructs to the real id of variable
	private void postResolveReferences() throws Exception {
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
				// else
				if (xml.getElseConstructReference() != null) {
					changeCcReference(xml.getElseConstructReference());
				}
				// if source question ref
				if (!xml.getIfCondition().getSourceQuestionReferenceList()
						.isEmpty()) {
					List<ReferenceType> sourceQReferenceTypes = xml
							.getIfCondition().getSourceQuestionReferenceList();
					for (int i = 0; i < sourceQReferenceTypes.size(); i++) {
						postResolveQueiReference(sourceQReferenceTypes.get(i));
					}
				}
			}
			// computation item
			else if (xmlobject instanceof ComputationItemType) {
				ComputationItemType xml = (ComputationItemType) xmlobject;

				// lookup pseudo vari ref
				boolean found = false;
				String pseudoVariRef = xml.getAssignedVariableReference()
						.getIDArray(0).getStringValue();
				for (LightXmlObjectType vari : getVariablesLight()) {
					if (pseudoVariRef.equals(vari.getId())) {
						ComputationItem model = getComputationItemModel(xml);

						// variable reference
						model.applyChange(vari,
								ModelIdentifingType.Type_B.class);
						found = true;
						break;
					}
				}
				if (!found) {
					// TODO hmmm, variable reference is still pseudo vari id
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

	/**
	 * Pseudo singleton does only query once
	 * 
	 * @return list of light variables
	 * @throws DDIFtpException
	 */
	private List<LightXmlObjectType> getVariablesLight() throws DDIFtpException {
		if (vars == null) {
			vars = new ArrayList<LightXmlObjectType>();
			try {
				vars = DdiManager.getInstance()
						.getVariablesLightPlus(null, null, null, null)
						.getLightXmlObjectList().getLightXmlObjectList();
			} catch (Exception e) {
				if (!(e instanceof DDIFtpException)) {
					throw new DDIFtpException(e);
				} else {
					throw (DDIFtpException) e;
				}
			}
		}
		return vars;
	}

	private void cleanSequenceForDublicateCcRefs() throws DDIFtpException {
		if (postCleanSeqItems.isEmpty()) {
			return;
		}
		String[] ccIds = new String[postCleanSeqItems.size()];
		int count = 0;
		for (String pseudoVarId : postCleanSeqItems) {
			if (pseudoVarIdToCcIdMap.get(pseudoVarId) != null) {
				ccIds[count] = pseudoVarIdToCcIdMap.get(pseudoVarId).getId();
			} else {
				try {
					VariableDocument varDoc = DdiManager.getInstance()
							.getVariableByVariableName("V" + pseudoVarId);
					if (varDoc == null) {
						// check if sequence exist
						LightXmlObjectListDocument lightXmlDoc = DdiManager
								.getInstance().getSequencesLight(pseudoVarId,
										null, null, null);
						if (lightXmlDoc != null) {
							// do nothing
						} else {
							throw new DDIFtpException(Translator.trans(
									"variable.label.notfound",
									new Object[] { pseudoVarId }),
									new Throwable());
						}
					}
				} catch (Exception e) {
					if (e instanceof DDIFtpException) {
						throw (DDIFtpException) e;
					} else {
						throw new DDIFtpException(e);
					}
				}
			}
			count++;
		}
		for (Iterator<SequenceDocument> seqDocs = seqList.iterator(); seqDocs
				.hasNext();) {
			SequenceDocument seq = (SequenceDocument) seqDocs.next();
			for (Iterator<ReferenceType> ref = seq.getSequence()
					.getControlConstructReferenceList().iterator(); ref
					.hasNext();) {
				String id = ref.next().getIDArray(0).getStringValue();
				for (int i = 0; i < ccIds.length; i++) {
					if (ccIds[i] != null && ccIds[i].equals(id)) {
						ref.remove();
						break;
					}
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

	private ReferenceType changeCcReference(ReferenceType reference)
			throws DDIFtpException, Exception {
		ReferenceType ref = null;
		String id = reference.getIDList().get(0).getStringValue();
		LightXmlObjectType newId = pseudoVarIdToCcIdMap.get(id.substring(1));
		if (newId == null) {
			QuestionItemDocument quei = DdiManager.getInstance()
					.getQuestionItembyUserId(id);
			if (quei != null) {
				QuestionConstructDocument queCC = DdiManager.getInstance()
						.getQuestionConstructByQuestionId(
								quei.getQuestionItem().getId());
				if (queCC != null) {
					newId = LightXmlObjectType.Factory.newInstance();
					newId.setId(queCC.getQuestionConstruct().getId());
					newId.setVersion(queCC.getQuestionConstruct().getVersion());
				}
			} else {
				SequenceDocument seqDoc = postResolveSeqRefs.get(id);
				if (seqDoc != null) {
					newId = LightXmlObjectType.Factory.newInstance();
					newId.setId(seqDoc.getSequence().getId());
					newId.setVersion(seqDoc.getSequence().getVersion());
				}
			}
		}
		if (newId != null) {
			ref = IdentificationManager.getInstance().addReferenceInformation(
					reference, newId);
		}
		return ref;
	}

	private void postResolveQueiReference(ReferenceType reference)
			throws Exception {
		String id = XmlBeansUtil.getTextOnMixedElement(reference);

		// resolve reference
		// search in all Question Schemes
		for (QuestionSchemeDocument ques : quesList) {
			// amount all Question Items
			for (QuestionItemType quei : ques.getQuestionScheme()
					.getQuestionItemList()) {

				if (!quei.getUserIDList().isEmpty()
						&& quei.getUserIDArray(0).getStringValue().substring(1)
								.equals(id.substring(1))) {
					IdentificationManager.getInstance()
							.addReferenceInformation(
									reference,
									createLightXmlObject(ques
											.getQuestionScheme().getId(), ques
											.getQuestionScheme().getVersion(),
											quei.getId(), quei.getVersion()));

					break;
				}
			}
		}

		// not found in list look it up
		if (XmlBeansUtil.getTextOnMixedElement(reference).equals(id)) {
			List<LightXmlObjectType> queiPlusList = DdiManager.getInstance()
					.getQuestionItemsLightPlus(true, null, null, null, null)
					.getLightXmlObjectList().getLightXmlObjectList();

			for (LightXmlObjectType queiPlus : queiPlusList) {
				for (CustomType userIdCus : LightXmlObjectUtil
						.getCustomListbyType(queiPlus, "UserID")) {
					if (userIdCus.getValue() != null
							&& userIdCus.getValue().toLowerCase().equals(id)) {
						// set reference
						IdentificationManager.getInstance()
								.addReferenceInformation(
										reference,
										createLightXmlObject(
												queiPlus.getParentId(),
												queiPlus.getParentVersion(),
												queiPlus.getId(),
												queiPlus.getVersion()));
						id = null;
						break;
					}
				}
				if (id == null) {
					break;
				}
			}
		}
	}

	private void postResolveSeqUniVarRefs() throws Exception {
		// for speed
		List<String> mqueIds = new ArrayList<String>();
		for (MultipleQuestionItemDocument mqueDoc : mqueList) {
			mqueIds.add(mqueDoc.getMultipleQuestionItem().getId());
		}

		boolean found = false;
		for (Entry<String, LightXmlObjectType> postResolveSeqUniVarRefEntry : postResolveSeqUniVarRefs
				.entrySet()) {

			for (SequenceDocument seqe : postResolveSeqRefs.values()) {
				for (UserIDType pseudoVarId : seqe.getSequence()
						.getUserIDList()) {
					// get pseudo seq ID
					if (pseudoVarId.getType().equals(
							Ddi3NamespaceHelper.SEQ_USER_ID_TYPE)
							&& pseudoVarId.getStringValue().equals(
									postResolveSeqUniVarRefEntry.getKey())) {

						// loop seq and get vari refs
						for (ReferenceType seqCcRef : seqe.getSequence()
								.getControlConstructReferenceList()) {

							// loop cc to get question construct
							for (ControlConstructType cc : cocs
									.getControlConstructScheme()
									.getControlConstructList()) {

								if (found) {
									break;
								}
								if (cc instanceof QuestionConstructType
										&& cc.getId().equals(
												seqCcRef.getIDList().get(0)
														.getStringValue())) {
									// from cc to variable
									for (Entry<String, LightXmlObjectType> pseudoVarIdToCcEntry : pseudoVarIdToCcIdMap
											.entrySet()) {
										if (cc.getId().equals(
												pseudoVarIdToCcEntry.getValue()
														.getId())) {

											// check multiple question item ref
											String mId = ((QuestionConstructType) cc)
													.getQuestionReference()
													.getIDArray(0)
													.getStringValue();

											if (mqueIds.contains(mId)) {
												for (MultipleQuestionItemDocument mqueDoc : mqueList) {
													if (mqueDoc
															.getMultipleQuestionItem()
															.getId()
															.equals(mId)) {
														for (QuestionItemType quei : mqueDoc
																.getMultipleQuestionItem()
																.getSubQuestions()
																.getQuestionItemList()) {

															for (UserIDType userId : quei
																	.getUserIDList()) {
																if (userId
																		.getType()
																		.equals(Ddi3NamespaceHelper.QUEI_VAR_USER_ID_TYPE)) {

																	updateVariableUniverseReference(true, 
																			userId.getStringValue(),
																			postResolveSeqUniVarRefEntry
																					.getValue());
																}
															}
														}
													}
												}
											} else
											// default: set univref on variable
											{
												updateVariableUniverseReference(true, 
														"V"
																+ pseudoVarIdToCcEntry
																		.getKey(),
														postResolveSeqUniVarRefEntry
																.getValue());
											}

											// declare found
											found = true;
											break;
										}
									}

									if (!found) {
										// TODO enhance error code
										// throw new
										// Exception("Variable not found");
										System.out.println("iuouououou");
									}
								}
							}
							found = false;
						}
					}
				}
			}
		}
	}

	public List<UniverseSchemeDocument> getUnisList() {
		return unisList;
	}

	public List<SequenceDocument> getSeqList() {
		return seqList;
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

	public void unsetMultipleQuestion() {
		mque = false;
	}

	int lineNo = 0;

	public int getLineNo() {
		return lineNo;
	}

	public void setLineNo(int lineNo) {
		this.lineNo = lineNo;
	}

	public static void createMarker(int lineNo, String msg, String elemetName)
			throws DDIFtpException {
		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IResource resource = workspace.getRoot();

			IMarker marker = (IMarker) resource.createMarker(ProblemView.MARKER_ID);
			marker.setAttribute(IMarker.LOCATION, lineNo);
			marker.setAttribute(IMarker.MESSAGE, msg);
			marker.setAttribute(IMarker.SOURCE_ID, Activator.PLUGIN_ID);
			if (elemetName != null) {
				marker.setAttribute(TypeMarkerField.DDI_TYPE, elemetName);
			}
		} catch (CoreException e) {
			throw new DDIFtpException(e.getMessage(), e);
		}
	}

	boolean yesToAllErrors = false;

	public void handleParseError(ElementType elementType, String msg)
			throws DDIFtpException {
		// add to ques import prob view
		createMarker(lineNo, msg,
				elementType == null ? null : elementType.getElementName());

		// if yes to all - continue
		if (yesToAllErrors) {
			return;
		}

		// display warning
		StringBuilder msgTxt = new StringBuilder(msg);
		msgTxt.append("\n\n");
		msgTxt.append(Translator
				.trans("line.valueRepresentationnotfound.continue"));

		String[] labels = new String[] { Translator.trans("line.error.ok"),
				Translator.trans("line.error.oktoall"),
				Translator.trans("line.error.cancel") };
		int result = DialogUtil.customConfirmDialog(
				Translator.trans("line.continue"), msgTxt.toString(), labels);
		switch (result) {
		case 0:
			// yes
			break;
		case 1:
			// yes to all
			yesToAllErrors = true;
			break;
		case 2:
			// cancel
			throw new DDIFtpException(IMPORT_STOPPED, new Throwable());
		default:
			break;
		}
	}

	public final static String IMPORT_STOPPED = "Import stopped";

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

	private void listToString(List<?> list) {
		for (Object doc : list) {
			log.debug(((XmlObject) doc).xmlText(xmlOptions));
		}
	}
}
