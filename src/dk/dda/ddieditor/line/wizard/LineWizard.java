package dk.dda.ddieditor.line.wizard;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddieditor.model.resource.DDIResourceType;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.ddialliance.ddieditor.ui.editor.Editor;
import org.ddialliance.ddieditor.ui.preference.PreferenceUtil;
import org.ddialliance.ddieditor.ui.util.DialogUtil;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.Translator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.jamwiki.parser.ParserException;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.jflex.JFlexParser;

import dk.dda.ddieditor.line.osgi.Activator;
import dk.dda.ddieditor.line.util.Ddi3Helper;
import dk.dda.ddieditor.line.util.Wiki2Ddi3Scanner;

public class LineWizard extends Wizard {
	static Ddi3Helper ddi3Helper;
	public String wikiToImport;

	public LineWizard(Ddi3Helper ddi3Helper) {
		super();
		LineWizard.ddi3Helper = ddi3Helper;
	}

	public LineWizard(Ddi3Helper ddi3Helper, String wikiSyntax) {
		super();
		LineWizard.ddi3Helper = ddi3Helper;
		WikiPage.wikiSyntax = wikiSyntax;
	}

	public Ddi3Helper getDdi3Helper() {
		return ddi3Helper;
	}

	public ResourcePage resourcePage = new ResourcePage();
	public ParsePage pargePage = new ParsePage();

	@Override
	public void addPages() {
		addPage(resourcePage);
		addPage(new WikiPage());
		addPage(pargePage);
	}

	boolean checkRefSelection(LightXmlObjectType lightXmlObject) {
		if (lightXmlObject.getId() == null || lightXmlObject.getId().equals("")) {
			return false;
		}
		return true;
	}

	public void loadDdi3Helper() throws DDIFtpException, Exception {
		// universe
		if (resourcePage.uniRefSelectCombo != null
				&& resourcePage.uniRefSelectCombo.getResult() != null
				&& checkRefSelection(resourcePage.uniRefSelectCombo.getResult())) {
			ddi3Helper.univ = DdiManager
					.getInstance()
					.getUniverse(
							resourcePage.uniRefSelectCombo.getResult().getId(),
							resourcePage.uniRefSelectCombo.getResult()
									.getVersion(),
							resourcePage.uniRefSelectCombo.getResult()
									.getParentId(),
							resourcePage.uniRefSelectCombo.getResult()
									.getParentVersion()).getUniverse();
		}

		// concept
		if (resourcePage.conRefSelectCombo != null
				&& resourcePage.conRefSelectCombo.getResult() != null
				&& checkRefSelection(resourcePage.conRefSelectCombo.getResult())) {
			ddi3Helper.conc = DdiManager
					.getInstance()
					.getConcept(
							resourcePage.conRefSelectCombo.getResult().getId(),
							resourcePage.conRefSelectCombo.getResult()
									.getVersion(),
							resourcePage.conRefSelectCombo.getResult()
									.getParentId(),
							resourcePage.conRefSelectCombo.getResult()
									.getParentVersion()).getConcept();
		}

		// question scheme
		if (resourcePage.quesRefSelectCombo != null
				&& resourcePage.quesRefSelectCombo.getResult() != null
				&& checkRefSelection(resourcePage.quesRefSelectCombo
						.getResult())) {
			ddi3Helper.ques = DdiManager.getInstance().getQuestionScheme(
					resourcePage.quesRefSelectCombo.getResult().getId(),
					resourcePage.quesRefSelectCombo.getResult().getVersion(),
					resourcePage.quesRefSelectCombo.getResult().getParentId(),
					resourcePage.quesRefSelectCombo.getResult()
							.getParentVersion());
		}

		// main sequence
		if (resourcePage.seqRefSelectCombo != null
				&& resourcePage.seqRefSelectCombo.getResult() != null
				&& checkRefSelection(resourcePage.seqRefSelectCombo.getResult())) {
			ddi3Helper.mainSeq = DdiManager
					.getInstance()
					.getSequence(
							resourcePage.seqRefSelectCombo.getResult().getId(),
							resourcePage.seqRefSelectCombo.getResult()
									.getVersion(),
							resourcePage.seqRefSelectCombo.getResult()
									.getParentId(),
							resourcePage.seqRefSelectCombo.getResult()
									.getParentVersion()).getSequence();

			List<LightXmlObjectType> refCocs = DdiManager.getInstance()
					.getControlConstructSchemesLight(null, null, null, null)
					.getLightXmlObjectList().getLightXmlObjectList();
			for (LightXmlObjectType lightXmlObject : refCocs) {
				if (lightXmlObject.getId().equals(
						resourcePage.seqRefSelectCombo.getResult()
								.getParentId())
						&& lightXmlObject.getVersion().equals(
								resourcePage.seqRefSelectCombo.getResult()
										.getVersion())) {
					ddi3Helper.cocs = DdiManager.getInstance()
							.getControlConstructScheme(lightXmlObject.getId(),
									lightXmlObject.getVersion(),
									lightXmlObject.getParentId(),
									lightXmlObject.getParentVersion());

					break;
				}
			}
		}
		ddi3Helper.setCreateInstrument(resourcePage.createInstrument);
		ddi3Helper.initDdi3();

		// wiki to import
		wikiToImport = WikiPage.wikiSyntax;
	}

	public boolean performFinish() {
		try {
			// clean up problem view
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IMarker[] markers = root.findMarkers(IMarker.TEXT, false,
					IResource.DEPTH_ZERO);
			for (int i = 0; i < markers.length; i++) {
				// switch to selected load resource
				if (pargePage.resourceSelectionLister != null) {
					// more than one resource loaded
					DDIResourceType selectedLoadResource = pargePage.resourceSelectionLister
							.getSelectedResource();
					PersistenceManager.getInstance().setWorkingResource(
							selectedLoadResource.getOrgName());
				}
			}

			// set prefs
			loadDdi3Helper();
		} catch (Exception e) {
			// quietly quit when decided
			if (e instanceof DDIFtpException
					&& ((DDIFtpException) e).getRealThrowable() instanceof InterruptedException) {
				return false;
			}
			Editor.showError(e, this.getClass().getName());
			return false;
		}

		unInitialize();
		return true;
	}

	@Override
	public boolean performCancel() {
		unInitialize();
		return true;
	}

	private void unInitialize() {
		WikiPage.fileName = "";
		WikiPage.wikiSyntax = "";
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		IWizardPage nextPage = super.getNextPage(page);
		return nextPage;
	}

	public static String readFile(String fileName) {
		return readFile(fileName, "utf-8");
	}

	public static String readFile(String fileName, String charset) {
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(fileName), charset);
		} catch (FileNotFoundException e2) {
			MessageDialog.openError(PlatformUI.getWorkbench().getDisplay()
					.getActiveShell(), Translator.trans("ErrorTitle"),
					e2.getMessage());
		}
		StringBuilder wikiSyntax = new StringBuilder();
		while (scanner.hasNextLine()) {
			wikiSyntax.append(scanner.nextLine()
					+ System.getProperty("line.separator"));
		}
		scanner.close();
		return wikiSyntax.toString();
	}

	public static void displayWiki(String wikiSyntax, Browser browser,
			boolean validateSyntax) {
		if (validateSyntax) {
			Wiki2Ddi3Scanner scanner = new Wiki2Ddi3Scanner(ddi3Helper);
			try {
				scanner.startScanning(wikiSyntax, false);
			} catch (Exception e) {
				new Editor().showError(e);
			}

			if (!scanner.errorList.isEmpty()) {
				StringBuilder result = new StringBuilder();
				for (Iterator<String> iterator = scanner.errorList.iterator(); iterator
						.hasNext();) {
					result.append(iterator.next());
					if (iterator.hasNext()) {
						result.append("\n");
					}
				}

				DialogUtil.infoDialog(PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell(),
						Activator.PLUGIN_ID,
						Translator.trans("line.errortitle"), result.toString());

				// file writer allways to the same file
				// content
				IProject project;
				String errorLogFile = "questionparseerrorlog.txt";
				File f;
				try {
					IWorkspace ws = ResourcesPlugin.getWorkspace();
					project = ws.getRoot().getProject("ddieditor-externalfiles");
					if (!project.exists())
						project.create(null);
					if (!project.isOpen())
						project.open(null);

					f = new File(project.getLocation().toFile()
							.getAbsoluteFile()
							+ File.separator + errorLogFile);
					Writer output = new BufferedWriter(new OutputStreamWriter(
							new FileOutputStream(f), "UTF8"));
					try {
						output.write(result.toString());
					} finally {
						output.close();
					}

					IPath location = new Path(f.getAbsolutePath());// +
																		// f.getAbsolutePath());
					IFile file = project.getFile(location.lastSegment());
					if (!file.exists()) {
						file.createLink(location, IResource.NONE, null);
					}

					IWorkbenchPage page = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
					if (page != null)
						page.openEditor(new FileEditorInput(file),
								IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
				} catch (Exception e2) {
					Editor.showError(e2, "");
				}
			}
		}

		// parse wiki text
		ParserInput parserInput = new ParserInput();
		parserInput.setTopicName("");
		parserInput.setVirtualWiki("");
		parserInput.setContext("");
		JFlexParser parser = new JFlexParser(parserInput);
		try {
			String html = parser.parseHTML(new ParserOutput(), wikiSyntax
					+ "__FORCETOC__" + System.getProperty("line.separator"));
			if (browser != null) {
				browser.setText(html);
			}
		} catch (ParserException e) {
			new Editor().showError(e);
		}
	}
}

class ResourceSelectionLister implements SelectionListener {
	List<DDIResourceType> resources;
	DDIResourceType selectedResource = null;
	WizardPage page = null;

	public ResourceSelectionLister(List<DDIResourceType> resources,
			WizardPage page) {
		this.resources = resources;
		this.page = page;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		Combo c = (Combo) e.getSource();
		selectedResource = resources.get(c.getSelectionIndex());
		page.setPageComplete(true);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// do nothing
	}

	public DDIResourceType getSelectedResource() {
		return selectedResource;
	}
}

class PathSelectionListener implements SelectionListener {
	// String fileName;
	Text pathText;
	Browser browser;
	WizardPage page;

	public PathSelectionListener(Text pathText, Browser browser, WizardPage page) {
		super();
		this.pathText = pathText;
		this.browser = browser;
		this.page = page;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		FileDialog fileChooser = new FileDialog(PlatformUI.getWorkbench()
				.getDisplay().getActiveShell());
		fileChooser.setText(Translator.trans("line.filechooser.title"));
		fileChooser.setFilterExtensions(new String[] { "*.txt" });
		fileChooser.setFilterNames(new String[] { Translator
				.trans("line.filechooser.filternames") });
		PreferenceUtil.setPathFilter(fileChooser);
		WikiPage.fileName = fileChooser.open();
		pathText.setText(WikiPage.fileName);
		PreferenceUtil.setLastBrowsedPath(WikiPage.fileName);

		// read in file
		WikiPage.wikiSyntax = LineWizard.readFile(WikiPage.fileName);

		// parse in browser
		LineWizard.displayWiki(WikiPage.wikiSyntax, browser, true);

		// set page complete
		page.setPageComplete(true);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// do nothing
	}
}

class ParsePage extends WizardPage {
	public static final String PAGE_NAME = "Parse";
	ResourceSelectionLister resourceSelectionLister = null;

	public ParsePage() {
		super(PAGE_NAME, Translator.trans("line.wizard.refpage.title"), null);
	}

	@Override
	public void createControl(Composite parent) {
		Editor editor = new Editor();
		Group group = editor.createGroup(parent,
				Translator.trans("line.wizard.refpage.parse"));

		// loaded resources
		List<DDIResourceType> resources = null;
		try {
			resources = PersistenceManager.getInstance().getResources();
		} catch (DDIFtpException e) {
			MessageDialog.openError(PlatformUI.getWorkbench().getDisplay()
					.getActiveShell(), Translator.trans("ErrorTitle"),
					e.getMessage());
		}
		String[] options = new String[resources.size()];
		int count = 0;
		for (DDIResourceType resource : resources) {
			options[count] = resource.getOrgName();
			count++;
		}
		editor.createLabel(group, Translator.trans("line.resource.select"));
		Combo combo = editor.createCombo(group, options);
		// resource selection
		if (options.length == 1) {
			combo.select(0);
			setPageComplete(true);
		} else {
			resourceSelectionLister = new ResourceSelectionLister(resources,
					this);
			combo.addSelectionListener(resourceSelectionLister);
			setPageComplete(false);
		}

		// finalize
		setControl(group);
		combo.setFocus();
	}
}