package dk.dda.ddieditor.line.command;

import java.util.List;

import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.ConceptSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.ConceptualComponentDocument;
import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.UniverseSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.ControlConstructSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.DataCollectionDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.MultipleQuestionItemDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.SequenceDocument;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.CategorySchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.LogicalProductDocument;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.NoteDocument;
import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddieditor.ui.editor.Editor;
import org.ddialliance.ddieditor.ui.model.ElementType;
import org.ddialliance.ddieditor.ui.view.ViewManager;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import dk.dda.ddieditor.line.util.Ddi3Helper;
import dk.dda.ddieditor.line.wizard.LineWizard;

public class ImportLine extends org.eclipse.core.commands.AbstractHandler {
	public static String ID = "dk.dda.ddieditor.line.command.ImportLine";
	private Log log = LogFactory.getLog(LogType.SYSTEM, ImportLine.class);

	ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(
			new ConfigurationScope(), "ddieditor-ui");

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Ddi3Helper ddi3Helper = null;
		try {
			ddi3Helper = new Ddi3Helper();
		} catch (Exception e) {
			Editor.showError(e, ID);
			return null;
		}

		// collect info aka wizard
		LineWizard lineWizard = new LineWizard(ddi3Helper);
		WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench()
				.getDisplay().getActiveShell(), lineWizard);
		int returnCode = dialog.open();

		if (returnCode != Window.CANCEL) {
			// import questions
			RefreshRunnable longJob = new RefreshRunnable(ddi3Helper);
			BusyIndicator.showWhile(PlatformUI.getWorkbench().getDisplay(),
					longJob);

			// refresh views
			ViewManager.getInstance().addAllViewsToRefresh();
			ViewManager.getInstance().refesh();
		}
		return null;
	}

	/**
	 * Import the generated ddi into persistence
	 * 
	 * @param ddi3Helper
	 * @throws Exception
	 */
	private void createDdi3(Ddi3Helper ddi3Helper) throws Exception {
		// study unit
		LightXmlObjectType studyUnitLight = null;
		List<LightXmlObjectType> studyUnits = DdiManager.getInstance()
				.getStudyUnitsLight(null, null, null, null)
				.getLightXmlObjectList().getLightXmlObjectList();

		if (studyUnits.isEmpty()) {
			throw new DDIFtpException("No study unit");
		} else {
			studyUnitLight = ddi3Helper.createLightXmlObject(null, null,
					studyUnits.get(0).getId(), studyUnits.get(0).getVersion());
		}

		// conceptual component
		LightXmlObjectType compLight = ddi3Helper.createLightXmlObject(null,
				null, null, null);

		List<LightXmlObjectType> compList = DdiManager.getInstance()
				.getConceptualComponentsLight(null, null, null, null)
				.getLightXmlObjectList().getLightXmlObjectList();
		if (compList.isEmpty()) {
			// comp
			ConceptualComponentDocument compDoc = ConceptualComponentDocument.Factory
					.newInstance();
			ddi3Helper.addIdAndVersion(compDoc.addNewConceptualComponent(),
					null, null);
			DdiManager.getInstance().createElement(compDoc,
					studyUnitLight.getId(), studyUnitLight.getVersion(),
					"studyunit__StudyUnit");
		} else {
			compLight.setId(compList.get(0).getId());
			compLight.setVersion(compList.get(0).getVersion());
		}

		// universe
		for (UniverseSchemeDocument doc : ddi3Helper.getUnisList()) {
			if (doc.getUniverseScheme().getUniverseList().isEmpty()) {
				continue;
			}
			DdiManager.getInstance().createElement(doc, compLight.getId(),
					compLight.getVersion(), "ConceptualComponent");
		}

		// concept
		for (ConceptSchemeDocument doc : ddi3Helper.getConsList()) {
			if (doc.getConceptScheme().getConceptList().isEmpty()) {
				continue;
			}
			DdiManager.getInstance()
					.createElement(
							doc,
							compLight.getId(),
							compLight.getVersion(),
							"ConceptualComponent",
							new String[] { "VersionRationale",
									"VersionResponsibility" },
							new String[] { "ConceptScheme",
									"ConceptSchemeReference", "UniverseScheme",
									"UniverseSchemeReference",
									"GeographicStructureScheme",
									"GeographicStructureSchemeReference",
									"GeographicLocationScheme",
									"GeographicLocationSchemeReference" },
							new String[] {});
		}

		// data collection
		LightXmlObjectType dataColLight = ddi3Helper.createLightXmlObject(null,
				null, null, null);

		List<LightXmlObjectType> datacollectionList = DdiManager.getInstance()
				.getDataCollectionsLight(null, null, null, null)
				.getLightXmlObjectList().getLightXmlObjectList();
		if (datacollectionList.isEmpty()) {
			// new data collection
			DataCollectionDocument dataColDoc = DataCollectionDocument.Factory
					.newInstance();
			dataColDoc.addNewDataCollection();
			// id
			ddi3Helper.addIdAndVersion(dataColDoc.getDataCollection(),
					ElementType.DATA_COLLECTION.getIdPrefix(), null);
			// update light
			dataColLight.setId(dataColDoc.getDataCollection().getId());
			dataColLight
					.setVersion(dataColDoc.getDataCollection().getVersion());
			// create
			DdiManager.getInstance().createElement(dataColDoc,
					studyUnitLight.getId(), studyUnitLight.getVersion(),
					"studyunit__StudyUnit",
					new String[] { "ConceptualComponent" }, new String[] {},
					new String[] {});
		} else {
			dataColLight.setId(datacollectionList.get(0).getId());
			dataColLight.setVersion(datacollectionList.get(0).getVersion());
		}

		// question
		for (QuestionSchemeDocument doc : ddi3Helper.getQuesList()) {
			if (doc.getQuestionScheme().getQuestionItemList().isEmpty()) {
				continue;
			}
			if (ddi3Helper.quesIsNewList.contains(doc.getQuestionScheme()
					.getId())) {
				// create
				DdiManager.getInstance().createElement(
						doc,
						dataColLight.getId(),
						dataColLight.getVersion(),
						"datacollection__DataCollection",
						new String[] { "Note", "Description", "Label",
								"VersionRationale", "VersionResponsibility" },
						new String[] { "CollectionEvent", "QuestionScheme",
								"ControlConstructScheme",
								"InterviewerInstructionScheme", "Instrument",
								"ProcessingEvent" }, new String[] {});
			} else {
				// update
				DdiManager.getInstance().updateElement(doc,
						doc.getQuestionScheme().getId(),
						doc.getQuestionScheme().getVersion());
			}
		}

		// multiple question
		for (MultipleQuestionItemDocument doc : ddi3Helper.getMqueList()) {
			LightXmlObjectType quesLight = ddi3Helper.getMqueToQuesMap().get(
					doc.getMultipleQuestionItem().getId());
			DdiManager.getInstance().createElement(
					doc,
					quesLight.getId(),
					quesLight.getVersion(),
					"QuestionScheme",
					new String[] { "Description", "Label", "VersionRationale",
							"VersionResponsibility" },
					new String[] { "QuestionItem", "MultipleQuestionItem" },
					new String[] {});
		}

		// control construct
		if (!ddi3Helper.cocsIsNew) {
			DdiManager.getInstance().updateElement(ddi3Helper.cocs,
					ddi3Helper.cocs.getControlConstructScheme().getId(),
					ddi3Helper.cocs.getControlConstructScheme().getVersion());
		} else {
			for (ControlConstructSchemeDocument doc : ddi3Helper.getCocsList()) {
				if (doc.getControlConstructScheme().getControlConstructList()
						.isEmpty()) {
					continue;
				} else {
					DdiManager.getInstance().createElement(
							doc,
							dataColLight.getId(),
							dataColLight.getVersion(),
							"datacollection__DataCollection",
							new String[] { "UserID", "VersionRationale",
									"VersionResponsibility",
									"DataCollectionModuleName", "Label",
									"Description", "Coverage", "OtherMaterial",
									"Note", "CollectionEvent" },
							new String[] { "ControlConstructScheme",
									"InterviewerInstructionScheme",
									"Instrument", "ProcessingEvent" },
							new String[] { "Methodology", "QuestionScheme" });
				}
			}
		}

		// sequence
		SequenceDocument seqDoc = SequenceDocument.Factory
				.parse(ddi3Helper.mainSeq.xmlText(ddi3Helper.xmlOptions));
		DdiManager.getInstance().updateElement(seqDoc,
				ddi3Helper.mainSeq.getId(), ddi3Helper.mainSeq.getVersion());

		// notes
		for (NoteDocument doc : ddi3Helper.getNotes()) {
			DdiManager.getInstance().createElement(
					doc,
					dataColLight.getId(),
					dataColLight.getVersion(),
					"datacollection__DataCollection",
					new String[] { "VersionRationale", "VersionResponsibility",
							"DataCollectionModuleName", "Label", "Description",
							"Coverage", "OtherMaterial" },
					new String[] { "Note", "Methodology", "CollectionEvent",
							"QuestionScheme", "ControlConstructScheme",
							"InterviewerInstructionScheme", "Instrument",
							"ProcessingEvent" }, new String[] {});
		}

		// logical product
		LightXmlObjectType logProdLight = ddi3Helper.createLightXmlObject(null,
				null, null, null);

		List<LightXmlObjectType> logProdList = DdiManager.getInstance()
				.getLogicalProductsLight(null, null, null, null)
				.getLightXmlObjectList().getLightXmlObjectList();
		if (logProdList.isEmpty()) {
			// new logical product
			LogicalProductDocument doc = LogicalProductDocument.Factory
					.newInstance();
			doc.addNewLogicalProduct();

			ddi3Helper.addIdAndVersion(doc.getLogicalProduct(), null, null);
			logProdLight.setId(doc.getLogicalProduct().getId());
			logProdLight.setVersion(doc.getLogicalProduct().getVersion());

			DdiManager.getInstance().createElement(doc, studyUnitLight.getId(),
					studyUnitLight.getVersion(), "studyunit__StudyUnit");
		} else {
			logProdLight.setId(logProdList.get(0).getId());
			logProdLight.setVersion(logProdList.get(0).getVersion());
		}

		// category
		for (CategorySchemeDocument doc : ddi3Helper.getCatsList()) {
			if (doc.getCategoryScheme().getCategoryList().isEmpty()) {
				continue;
			}
			DdiManager.getInstance()
					.createElement(doc, logProdLight.getId(),
							logProdLight.getVersion(),
							"logicalproduct__LogicalProduct");
		}
	}

	/**
	 * Runnable wrapping view refresh to enable RCP busy indicator
	 */
	class RefreshRunnable implements Runnable {
		Ddi3Helper ddi3Helper;

		RefreshRunnable(Ddi3Helper ddi3Helper) {
			this.ddi3Helper = ddi3Helper;
		}

		@Override
		public void run() {
			try {
				createDdi3(ddi3Helper);
			} catch (Exception e) {
				Editor.showError(e, ID);
			}
		}
	}
}
