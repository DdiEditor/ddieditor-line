package dk.dda.ddieditor.line.wizard;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddieditor.model.resource.DDIResourceType;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.ddialliance.ddieditor.ui.editor.Editor;
import org.ddialliance.ddieditor.ui.view.Messages;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.Translator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.jamwiki.parser.ParserException;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.jflex.JFlexParser;

import dk.dda.ddieditor.line.dialog.EditWikiSyntaxDialog;
import dk.dda.ddieditor.line.util.Ddi3Helper;
import dk.dda.ddieditor.line.util.Wiki2Ddi3Scanner;

public class LineWizard extends Wizard {
	Ddi3Helper ddi3Helper;

	public LineWizard(Ddi3Helper ddi3Helper) {
		super();
		this.ddi3Helper = ddi3Helper;
	}

	public Ddi3Helper getDdi3Helper() {
		return ddi3Helper;
	}

	public ResourcePage resourcePage = new ResourcePage();

	@Override
	public void addPages() {
		addPage(resourcePage);
		addPage(new WikiPage());
		addPage(new ParsePage());
	}

	public boolean performFinish() {
		try {
			// universe
			if (resourcePage.uniRefSelectCombo.getResult() != null) {
				ddi3Helper.univ = DdiManager
						.getInstance()
						.getUniverse(
								resourcePage.uniRefSelectCombo.getResult()
										.getId(),
								resourcePage.uniRefSelectCombo.getResult()
										.getVersion(),
								resourcePage.uniRefSelectCombo.getResult()
										.getParentId(),
								resourcePage.uniRefSelectCombo.getResult()
										.getParentVersion()).getUniverse();
			}

			// concept
			if (resourcePage.conRefSelectCombo.getResult() != null) {
				ddi3Helper.conc = DdiManager
						.getInstance()
						.getConcept(
								resourcePage.conRefSelectCombo.getResult()
										.getId(),
								resourcePage.conRefSelectCombo.getResult()
										.getVersion(),
								resourcePage.conRefSelectCombo.getResult()
										.getParentId(),
								resourcePage.conRefSelectCombo.getResult()
										.getParentVersion()).getConcept();
			}

			// question scheme
			if (resourcePage.quesRefSelectCombo.getResult() != null) {
				ddi3Helper.ques = DdiManager.getInstance().getQuestionScheme(
						resourcePage.quesRefSelectCombo.getResult().getId(),
						resourcePage.quesRefSelectCombo.getResult()
								.getVersion(),
						resourcePage.quesRefSelectCombo.getResult()
								.getParentId(),
						resourcePage.quesRefSelectCombo.getResult()
								.getParentVersion());
			}

			// main sequence
			if (resourcePage.seqRefSelectCombo.getResult() != null) {
				ddi3Helper.mainSeq = DdiManager
						.getInstance()
						.getSequence(
								resourcePage.seqRefSelectCombo.getResult()
										.getId(),
								resourcePage.seqRefSelectCombo.getResult()
										.getVersion(),
								resourcePage.seqRefSelectCombo.getResult()
										.getParentId(),
								resourcePage.seqRefSelectCombo.getResult()
										.getParentVersion()).getSequence();

				List<LightXmlObjectType> refCocs = DdiManager
						.getInstance()
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
								.getControlConstructScheme(
										lightXmlObject.getId(),
										lightXmlObject.getVersion(),
										lightXmlObject.getParentId(),
										lightXmlObject.getParentVersion());
						break;
					}
				}
			}

			ddi3Helper.initDdi3();

			// parse file
			Wiki2Ddi3Scanner wiki2Ddi3Scanner = new Wiki2Ddi3Scanner(ddi3Helper);
			wiki2Ddi3Scanner.startScanning(WikiPage.wikiSyntax);
		} catch (Exception e) {
			Editor.showError(e, this.getClass().getName());
			return false;
		} finally {
			unInitialize();
		}

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
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(fileName), "utf-8");
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		StringBuilder wikiSyntax = new StringBuilder();
		while (scanner.hasNextLine()) {
			wikiSyntax.append(scanner.nextLine()
					+ System.getProperty("line.separator"));
		}
		scanner.close();
		return wikiSyntax.toString();
	}

	public static void displayWiki(String wikiSyntax, Browser browser) {
		// parse wiki text
		ParserInput parserInput = new ParserInput();
		parserInput.setTopicName("");
		parserInput.setVirtualWiki("");
		parserInput.setContext("");
		JFlexParser parser = new JFlexParser(parserInput);
		try {
			String html = parser.parseHTML(new ParserOutput(), wikiSyntax
					+ "__FORCETOC__" + System.getProperty("line.separator"));
			browser.setText(html);
		} catch (ParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
}

class WikiPage extends WizardPage {
	public static final String PAGE_NAME = "Wiki";
	public static String fileName = "";
	public static String wikiSyntax = "";

	public WikiPage() {
		super(PAGE_NAME, Translator.trans("line.wizard.refpage.title"), null);
	}

	@Override
	public void createControl(Composite parent) {
		Editor editor = new Editor();
		Group group = editor.createGroup(parent,
				Translator.trans("line.dialog.group"));

		// line.file
		editor.createLabel(group, Translator.trans("line.filechooser.title"));
		final Text pathText = editor.createText(group, "", false);
		Button pathBrowse = editor.createButton(group,
				Translator.trans("line.filechooser.browse"));
		final Browser browser = editor.createBrowser(group, "Question markup");
		PathSelectionListener pathSelectionListener = new PathSelectionListener(
				pathText, browser, this);
		pathBrowse.addSelectionListener(pathSelectionListener);

		// edit wiki
		Button editWiki = editor.createButton(group,
				Translator.trans("line.wizard.wiki.editbutton"));
		editWiki.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				EditWikiSyntaxDialog dialog = new EditWikiSyntaxDialog(
						PlatformUI.getWorkbench().getDisplay().getActiveShell(),
						fileName);
				int returnCode = dialog.open();
				if (returnCode == Window.CANCEL) {
					return;
				}

				if (dialog.result != null && !dialog.result.equals("")) {
					wikiSyntax = dialog.result;

					// parse in browser
					LineWizard.displayWiki(dialog.result, browser);

					// check save
					if (dialog.fileName != null && !dialog.fileName.equals("")) {
						fileName = dialog.fileName;
						pathText.setText(fileName);
					}

					// set page complete
					setPageComplete(true);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
		});

		setControl(group);
		setPageComplete(false);
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
		WikiPage.fileName = fileChooser.open();
		pathText.setText(WikiPage.fileName);

		// read in file
		WikiPage.wikiSyntax = LineWizard.readFile(WikiPage.fileName);

		// parse in browser
		LineWizard.displayWiki(WikiPage.wikiSyntax, browser);

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
					.getActiveShell(), Messages.getString("ErrorTitle"),
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
		resourceSelectionLister = new ResourceSelectionLister(resources, this);
		combo.addSelectionListener(resourceSelectionLister);

		// finalize
		setControl(group);
		combo.setFocus();
		setPageComplete(false);
	}
}