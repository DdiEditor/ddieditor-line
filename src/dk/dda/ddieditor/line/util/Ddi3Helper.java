package dk.dda.ddieditor.line.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.MultipleQuestionItemType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionConstructDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionConstructType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionItemDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionItemType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.SequenceDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.SequenceType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.StatementItemDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.StatementItemType;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.CategorySchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.CategoryType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.AbstractIdentifiableType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.AbstractMaintainableType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.AbstractVersionableType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.LabelType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.ProgrammingLanguageCodeType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.ReferenceType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.StructuredStringType;
import org.ddialliance.ddieditor.logic.identification.IdentificationManager;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddieditor.ui.dbxml.question.MultipleQuestionItemDao;
import org.ddialliance.ddieditor.ui.model.ElementType;
import org.ddialliance.ddieditor.ui.model.ModelAccessor;
import org.ddialliance.ddieditor.ui.model.ModelIdentifingType;
import org.ddialliance.ddieditor.ui.model.instrument.IfThenElse;
import org.ddialliance.ddieditor.ui.model.question.MultipleQuestionItem;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;
import org.ddialliance.ddiftp.util.xml.XmlBeansUtil;

public class Ddi3Helper {
	static private Log log = LogFactory
			.getLog(LogType.SYSTEM, Ddi3Helper.class);
	List<UniverseSchemeDocument> unisList = new ArrayList<UniverseSchemeDocument>();
	List<ConceptSchemeDocument> consList = new ArrayList<ConceptSchemeDocument>();
	List<QuestionSchemeDocument> quesList = new ArrayList<QuestionSchemeDocument>();
	List<CategorySchemeDocument> catsList = new ArrayList<CategorySchemeDocument>();
	List<ControlConstructSchemeDocument> cocsList = new ArrayList<ControlConstructSchemeDocument>();
	List<MultipleQuestionItem> mqueList = new ArrayList<MultipleQuestionItem>();

	UniverseSchemeDocument unis;
	UniverseType univ;
	ConceptSchemeDocument cons;
	ConceptType conc;
	MultipleQuestionItem mquem;
	boolean mque = false;
	QuestionSchemeDocument ques;
	QuestionItemType quei;
	CategorySchemeDocument cats;
	ControlConstructSchemeDocument cocs;
	SequenceType mainSeq;

	Map<String, LightXmlObjectType> pseudoVarIdToCcIdMap = new HashMap<String, LightXmlObjectType>();
	List<XmlObject> postResolveItemRefs = new ArrayList<XmlObject>();
	XmlOptions xmlOptions = new XmlOptions();
	String agency = "dk.dda";

	public Ddi3Helper() throws DDIFtpException {
		xmlOptions.setSaveAggressiveNamespaces();
		xmlOptions.setSavePrettyPrint();
		xmlOptions.setSaveOuter();
		intDdi3();
	}

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

		// category scheme
		createCategoryScheme();

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
		addIdAndVersion(mainSeq, ElementType.SEQUENCE.getIdPrefix(), null);
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
			result = createMultipleSubQuestion();
			result = mquem.getDocument().getMultipleQuestionItem().addNewSubQuestions().addNewQuestionItem();
		} else {
			result = ques.getQuestionScheme().addNewQuestionItem();
		}
		addIdAndVersion(result, ElementType.QUESTION_ITEM.getIdPrefix(), null);

		result.addNewQuestionItemName().setStringValue(pseudoVariableId);
		XmlBeansUtil.setTextOnMixedElement(result.addNewQuestionText()
				.addNewText(), text);

		// concept ref
		if (cons != null && conc != null) {
			setReference(result.addNewConceptReference(), cons
					.getConceptScheme().getId(), cons.getConceptScheme()
					.getVersion(), conc.getId(), conc.getVersion());
		}

		// control construct
		if (mque) {
			// TODO who is parent to a sub question
			// createQuestionConstruct(parentId, parentVersion, result.getId(),
			// result.getVersion());
		} else {
			QuestionConstructType qc = createQuestionConstruct(ques
					.getQuestionScheme().getId(), ques.getQuestionScheme()
					.getVersion(), result.getId(), result.getVersion());

			pseudoVarIdToCcIdMap.put(
					pseudoVariableId,
					createLightXmlObject(cocs.getControlConstructScheme()
							.getId(), cocs.getControlConstructScheme()
							.getVersion(), qc.getId(), qc.getVersion()));

			quei = result;
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
		if (true) {
			return;
		}
		MultipleQuestionItemType mque = ques.getQuestionScheme()
				.addNewMultipleQuestionItem();
		addIdAndVersion(mque, ElementType.MULTIPLE_QUESTION_ITEM.getIdPrefix(),
				null);

		// create model
		MultipleQuestionItemDao dao = new MultipleQuestionItemDao();
		MultipleQuestionItem model = dao.create(mque.getId(),
				mque.getVersion(), ques.getQuestionScheme().getId(), ques
						.getQuestionScheme().getVersion());
		model.setCreate(true);

		// concept ref
		if (conc != null) {
			model.executeChange(
					createLightXmlObject(null, null, conc.getId(),
							conc.getVersion()), ReferenceType.class);
		}

		// text
		model.executeChange(text, ModelIdentifingType.Type_A.class);
		mqueList.add(model);

		this.mquem = model;
		this.mque = true;
	}

	private QuestionItemType createMultipleSubQuestion() {
		// TODO remenber to update model in list !!!
		String id = mquem.getId();
		MultipleQuestionItem model = null;
		for (MultipleQuestionItem tmp : mqueList) {
			if (tmp.getId().equals(id)) {
				model = tmp;
			}
		}

		// TODO change this in relation to multiple question item
		return QuestionItemDocument.Factory.newInstance().addNewQuestionItem();
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

		// statement item
		StatementItemType stai = (StatementItemType) cocs
				.getControlConstructScheme()
				.addNewControlConstruct()
				.substitute(
						StatementItemDocument.type.getDocumentElementName(),
						StatementItemType.type);
		addIdAndVersion(stai, ElementType.STATEMENT_ITEM.getIdPrefix(), null);
		XmlBeansUtil.setTextOnMixedElement(stai.addNewDisplayText()
				.addNewText().addNewDescription(), statementText);

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
		model.applyChange(
				createLightXmlObject(cocs.getControlConstructScheme().getId(),
						cocs.getControlConstructScheme().getVersion(),
						seq.getId(), seq.getVersion()),
				ModelIdentifingType.Type_C.class);

		// else reference
		model.applyChange(createLightXmlObject(null, null, elze, null),
				ModelIdentifingType.Type_D.class);

		// add to main seq
		ifthenelse.set(model.getDocument().getIfThenElse());
		addControlConstructToMainSequence(ifthenelse);

		// add to post resolve items
		postResolveItemRefs.add(ifthenelse);
	}

	private void addControlConstructToMainSequence(
			ControlConstructType controlConstruct) {
		setReference(mainSeq.addNewControlConstructReference(), cocs
				.getControlConstructScheme().getId(), cocs
				.getControlConstructScheme().getVersion(),
				controlConstruct.getId(), controlConstruct.getVersion());
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
		return !(str != null && (!str.equals(empty)));
	}

	private void setText(LabelType label, String text) {
		XmlBeansUtil.setTextOnMixedElement(label, text);
	}

	private void setText(StructuredStringType struct, String text) {
		XmlBeansUtil.setTextOnMixedElement(struct, text);
	}

	public void postResolveReferences() throws DDIFtpException {
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
				changeCcReference(xml.getThenConstructReference());
				changeCcReference(xml.getElseConstructReference());
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

	private void changeCcReference(ReferenceType reference) {
		String id = reference.getIDList().get(0).getStringValue();
		LightXmlObjectType newId = pseudoVarIdToCcIdMap.get(id);
		if (newId != null) {
			ModelAccessor.setReference(reference, newId);
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

	public List<MultipleQuestionItem> getMqueList() {
		return mqueList;
	}

	public List<CategorySchemeDocument> getCatsList() {
		return catsList;
	}

	public List<ControlConstructSchemeDocument> getCocsList() {
		return cocsList;
	}

	public void resultToString() {
		log.debug("Universes:");
		listToString(unisList);
		log.debug("ConceptScheme:");
		listToString(consList);
		log.debug("QuestionSchemes:");
		listToString(quesList);
		log.debug("MultipleQuestions:");
		mqueListToString(mqueList);
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

	private void mqueListToString(List<MultipleQuestionItem> list) {
		for (MultipleQuestionItem multipleQuestionItem : list) {
			try {
				log.debug(multipleQuestionItem.getDocument()
						.xmlText(xmlOptions));
			} catch (DDIFtpException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void unsetMultipleQuestion() {
		mque = false;
	}
}
