package dk.dda.ddieditor.line.command;

import java.util.List;

import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.ConceptSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.ConceptualComponentDocument;
import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.UniverseSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.ControlConstructSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.DataCollectionDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.group.LogicalProductDocument;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.CategorySchemeDocument;
import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddieditor.ui.model.ElementType;
import org.ddialliance.ddieditor.ui.perspective.InfoPerspective;
import org.ddialliance.ddieditor.ui.view.InfoView;
import org.ddialliance.ddieditor.ui.view.View;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import dk.dda.ddieditor.line.util.Ddi3Helper;
import dk.dda.ddieditor.line.wizard.LineWizard;

public class ImportLine extends org.eclipse.core.commands.AbstractHandler {
	private Log log = LogFactory.getLog(LogType.SYSTEM, ImportLine.class);
	ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(
			new ConfigurationScope(), "ddieditor-ui");

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		LineWizard lineWizard = new LineWizard();
		WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench()
				.getDisplay().getActiveShell(), lineWizard);

		int returnCode = dialog.open();
		if (returnCode != Window.CANCEL) {
			try {
				createDdi3(lineWizard.getDdi3Helper());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			refreshView();
		}
		return null;
	}

	private void refreshView() {
		// update info view
		// TODO refactor boiler plate code to refresh a
		// view into a rcp command
		final IWorkbenchWindow[] workbenchWindows = PlatformUI.getWorkbench()
				.getWorkbenchWindows();

		IWorkbenchPage workbenchPage = workbenchWindows[0].getActivePage();
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					PlatformUI.getWorkbench().showPerspective(
							InfoPerspective.ID, workbenchWindows[0]);
				} catch (WorkbenchException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		IViewPart iViewPart = workbenchWindows[0].getActivePage().findView(
				InfoView.ID);
		if (iViewPart == null) {
			try {
				iViewPart = workbenchPage.showView(InfoView.ID);
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// refresh in async to avoid swt thread
		// violation
		final View view = (View) iViewPart;
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				view.refreshView();
			}
		});
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
			DdiManager.getInstance().createElement(doc, compLight.getId(),
					compLight.getVersion(), "ConceptualComponent");
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
					"studyunit__StudyUnit");
		} else {
			dataColLight.setId(datacollectionList.get(0).getId());
			dataColLight.setVersion(datacollectionList.get(0).getVersion());
		}

		// question
		for (QuestionSchemeDocument doc : ddi3Helper.getQuesList()) {
			if (doc.getQuestionScheme().getQuestionItemList().isEmpty()) {
				continue;
			}
			DdiManager.getInstance()
					.createElement(doc, dataColLight.getId(),
							dataColLight.getVersion(),
							"datacollection__DataCollection");
		}

		// TODO multiple question

		// control construct
		for (ControlConstructSchemeDocument doc : ddi3Helper.getCocsList()) {
			if (doc.getControlConstructScheme().getControlConstructList()
					.isEmpty()) {
				continue;
			}
			DdiManager.getInstance()
					.createElement(doc, dataColLight.getId(),
							dataColLight.getVersion(),
							"datacollection__DataCollection");
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

			ddi3Helper.addIdAndVersion(doc.getLogicalProduct()
					.getBaseLogicalProduct(), null, null);
			logProdLight.setId(doc.getLogicalProduct().getBaseLogicalProduct()
					.getId());
			logProdLight.setVersion(doc.getLogicalProduct()
					.getBaseLogicalProduct().getVersion());

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
}
