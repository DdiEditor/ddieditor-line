package dk.dda.ddieditor.line.wizard;

import java.util.ArrayList;
import java.util.List;

import org.ddialliance.ddi3.xml.xmlbeans.reusable.ReferenceType;
import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddieditor.ui.editor.Editor;
import org.ddialliance.ddieditor.ui.editor.widgetutil.referenceselection.ReferenceSelectionCombo;
import org.ddialliance.ddiftp.util.Translator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;

public class ResourcePage extends WizardPage {
	public static final String PAGE_NAME = "Ref";

	public ReferenceSelectionCombo uniRefSelectCombo = null;
	public ReferenceSelectionCombo conRefSelectCombo = null;
	public ReferenceSelectionCombo seqRefSelectCombo = null;
	public ReferenceSelectionCombo quesRefSelectCombo = null;
	boolean createConceptScheme = false;
	boolean createUniverseScheme = false;

	public ResourcePage() {
		super(PAGE_NAME, Translator.trans("line.wizard.refpage.title"), null);
	}

	@Override
	public void createControl(Composite parent) {
		// init
		Group group = new Editor().createGroup(parent,
				Translator.trans("line.wizard.refpage.group"));

		// create
		RefreshRunnable longJob = new RefreshRunnable(group);
		BusyIndicator
				.showWhile(PlatformUI.getWorkbench().getDisplay(), longJob);

		// finalize
		setControl(group);
		setPageComplete(true);
	}

	/**
	 * Runnable wrapping view refresh to enable RCP busy indicator
	 */
	class RefreshRunnable implements Runnable {
		Group group;
		Editor editor = new Editor();

		RefreshRunnable(Group group) {
			this.group = group;
		}

		@Override
		public void run() {
			try {
				// universe ref
				List<LightXmlObjectType> uniRefList = new ArrayList<LightXmlObjectType>();
				try {
					uniRefList = DdiManager.getInstance()
							.getUniversesLight(null, null, null, null)
							.getLightXmlObjectList().getLightXmlObjectList();
				} catch (Exception e) {
					Editor.showError(e, this.getClass().getName());
				}
				uniRefSelectCombo = editor.createRefSelection(group,
						Translator.trans("VariableEditor.label.universeref"),
						Translator.trans("VariableEditor.label.universeref"),
						ReferenceType.Factory.newInstance(), uniRefList, false);
				uniRefSelectCombo.addSelectionListener(
						Translator.trans("VariableEditor.label.universeref"),
						null);

				// create universe scheme - comment out 20110208
				// Button universeSchemeCreate = editor.createCheckBox(group,
				// "",
				// Translator.trans("line.wizard.createunislabel"));
				// universeSchemeCreate.addSelectionListener(new
				// SelectionListener() {
				// @Override
				// public void widgetSelected(SelectionEvent e) {
				// createUniverseScheme = ((Button)
				// e.getSource()).getSelection();
				// }
				//
				// @Override
				// public void widgetDefaultSelected(SelectionEvent e) {
				// // do nothing
				// }
				// });

				// concept ref
				List<LightXmlObjectType> conceptRefList = new ArrayList<LightXmlObjectType>();
				try {
					conceptRefList = DdiManager.getInstance()
							.getConceptsLight(null, null, null, null)
							.getLightXmlObjectList().getLightXmlObjectList();
				} catch (Exception e) {
					editor.showError(e);
				}
				conRefSelectCombo = editor.createRefSelection(group,
						Translator.trans("VariableEditor.label.conceptref"),
						Translator.trans("VariableEditor.label.conceptref"),
						ReferenceType.Factory.newInstance(), conceptRefList,
						false);
				conRefSelectCombo.addSelectionListener(
						Translator.trans("VariableEditor.label.conceptref"),
						null);

				// create concept scheme - comment out 20110208
				// Button conceptSchemeCreate = editor.createCheckBox(group, "",
				// Translator.trans("line.wizard.createconslabel"));
				// conceptSchemeCreate.addSelectionListener(new
				// SelectionListener() {
				// @Override
				// public void widgetSelected(SelectionEvent e) {
				// createConceptScheme = ((Button)
				// e.getSource()).getSelection();
				// }
				//
				// @Override
				// public void widgetDefaultSelected(SelectionEvent e) {
				// // do nothing
				// }
				// });

				// question scheme ref
				List<LightXmlObjectType> questionSchemeRefList = new ArrayList<LightXmlObjectType>();
				try {
					questionSchemeRefList = DdiManager.getInstance()
							.getQuestionSchemesLight(null, null, null, null)
							.getLightXmlObjectList().getLightXmlObjectList();
				} catch (Exception e) {
					editor.showError(e);
				}
				quesRefSelectCombo = editor.createRefSelection(group,
						Translator.trans("line.wizard.refpage.ques"),
						Translator.trans("line.wizard.refpage.ques"),
						ReferenceType.Factory.newInstance(),
						questionSchemeRefList, false);
				quesRefSelectCombo.addSelectionListener(
						Translator.trans("line.wizard.refpage.ques"), null);

				// main seq ref
				List<LightXmlObjectType> seqRefList = new ArrayList<LightXmlObjectType>();
				try {
					seqRefList = DdiManager.getInstance()
							.getSequencesLight(null, null, null, null)
							.getLightXmlObjectList().getLightXmlObjectList();
				} catch (Exception e) {
					editor.showError(e);
				}
				seqRefSelectCombo = editor.createRefSelection(group,
						Translator.trans("line.wizard.refpage.mainseqref"),
						Translator.trans("line.wizard.refpage.mainseqref"),
						ReferenceType.Factory.newInstance(), seqRefList, false);

				seqRefSelectCombo.addSelectionListener(
						Translator.trans("line.wizard.refpage.mainseqref"),
						null);
			} catch (Exception e) {
				Editor.showError(e, this.getClass().getName());
			}
		}
	}
}
