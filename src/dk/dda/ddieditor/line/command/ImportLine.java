package dk.dda.ddieditor.line.command;

import java.io.File;
import java.util.List;

import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.ConceptSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.ConceptualComponentDocument;
import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.UniverseSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.ControlConstructSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.DataCollectionDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionSchemeDocument;
import org.ddialliance.ddieditor.DdiEditor;
import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddieditor.ui.model.ElementType;
import org.ddialliance.ddieditor.ui.perspective.InfoPerspective;
import org.ddialliance.ddieditor.ui.view.InfoView;
import org.ddialliance.ddieditor.ui.view.View;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.Translator;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import dk.dda.ddieditor.line.controler.FileScanner;
import dk.dda.ddieditor.line.dialog.ImportLineDialog;

public class ImportLine extends org.eclipse.core.commands.AbstractHandler {
	private Log log = LogFactory.getLog(LogType.SYSTEM, ImportLine.class);
	ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(
			new ConfigurationScope(), "ddieditor-ui");

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ImportLineDialog dialog = new ImportLineDialog(PlatformUI
				.getWorkbench().getDisplay().getActiveShell());
		int returnCode = dialog.open();
		if (returnCode == Dialog.CANCEL) {
			return null;
		}

		// check prefs
		if (dialog.selectedResource == null && dialog.fileName != null) {
			new MessageDialog(null,
					Translator.trans("line.selectedresourcenull.title"), null,
					Translator.trans("line.selectedresourcenull.message"),
					MessageDialog.ERROR, new String[] { "Ok" }, 0).open();
			returnCode = dialog.open();
			if (returnCode == Dialog.CANCEL) {
				return null;
			}
		}

		// confirm
		if (MessageDialog
				.openConfirm(
						PlatformUI.getWorkbench().getDisplay().getActiveShell(),
						Translator.trans("line.confirm.title"),
						Translator.trans("line.confirm.import",
								new Object[] { dialog.fileName,
										dialog.selectedResource.getOrgName() }))) {

			// import
			FileScanner fs = new FileScanner();
			try {
				fs.startScanning(new File(dialog.fileName), new File(
						"test/spr-out"), null, null, null);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			fs.resultToString();
			// study unit
			// create data coll
			LightXmlObjectType studyUnitLight = null;
			try {
				List<LightXmlObjectType> studyUnits = DdiManager.getInstance()
						.getStudyUnitsLight(null, null, null, null)
						.getLightXmlObjectList().getLightXmlObjectList();

				if (studyUnits.isEmpty()) {
					throw new DDIFtpException("No study unit in:"
							+ dialog.selectedResource.getOrgName());
				} else {
					studyUnitLight = fs.createLightXmlObject(null, null,
							studyUnits.get(0).getId(), studyUnits.get(0)
									.getVersion());
				}
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			// conceptual component
			LightXmlObjectType compLight = fs.createLightXmlObject(null, null,
					null, null);
			try {
				List<LightXmlObjectType> compList = DdiManager.getInstance()
						.getConceptualComponentsLight(null, null, null, null)
						.getLightXmlObjectList().getLightXmlObjectList();
				if (compList.isEmpty()) {
					// comp
					ConceptualComponentDocument compDoc = ConceptualComponentDocument.Factory
							.newInstance();
					fs.addIdAndVersion(compDoc.addNewConceptualComponent(),
							null, null);
					DdiManager.getInstance()
							.createElement(compDoc, studyUnitLight.getId(),
									studyUnitLight.getVersion(),
									"studyunit__StudyUnit");
				} else {
					compLight.setId(compList.get(0).getId());
					compLight.setVersion(compList.get(0).getVersion());
				}

				// universe
				for (UniverseSchemeDocument doc : fs.getUnisList()) {
					DdiManager.getInstance().createElement(doc,
							compLight.getId(), compLight.getVersion(),
							"ConceptualComponent");
				}
				
				// concept
				for (ConceptSchemeDocument doc : fs.getConsList()) {
					DdiManager.getInstance().createElement(doc,
							compLight.getId(), compLight.getVersion(),
							"ConceptualComponent");
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			
			// data collection
			LightXmlObjectType dataColLight = fs.createLightXmlObject(null,
					null, null, null);
			try {
				List<LightXmlObjectType> datacollectionList = DdiManager
						.getInstance()
						.getDataCollectionsLight(null, null, null, null)
						.getLightXmlObjectList().getLightXmlObjectList();
				if (datacollectionList.isEmpty()) {
					// new data collection
					DataCollectionDocument dataColDoc = DataCollectionDocument.Factory
							.newInstance();
					dataColDoc.addNewDataCollection();
					// to element type datacollection
					fs.addIdAndVersion(dataColDoc.getDataCollection(), null,
							null);
					dataColLight.setId(dataColDoc.getDataCollection().getId());
					dataColLight.setVersion(dataColDoc.getDataCollection()
							.getVersion());

					DdiManager.getInstance()
							.createElement(dataColDoc, studyUnitLight.getId(),
									studyUnitLight.getVersion(),
									"studyunit__StudyUnit");
				} else {
					dataColLight.setId(datacollectionList.get(0).getId());
					dataColLight.setVersion(datacollectionList.get(0)
							.getVersion());
				}

				// question
				for (QuestionSchemeDocument doc : fs.getQuesList()) {
					DdiManager.getInstance().createElement(doc,
							dataColLight.getId(), dataColLight.getVersion(),
							"datacollection__DataCollection");
				}
				
				// control construct
				for (ControlConstructSchemeDocument doc : fs.getCocsList()) {
					DdiManager.getInstance().createElement(doc,
							dataColLight.getId(), dataColLight.getVersion(),
							"datacollection__DataCollection");
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// update info view
			// TODO refactor boiler plate code to refresh a
			// view into a rcp command
			final IWorkbenchWindow[] workbenchWindows = PlatformUI
					.getWorkbench().getWorkbenchWindows();

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
		return null;
	}
}
