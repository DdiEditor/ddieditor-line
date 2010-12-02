package dk.dda.ddieditor.line.wizard;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.ddialliance.ddi3.xml.xmlbeans.reusable.ReferenceType;
import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddieditor.model.resource.DDIResourceType;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.ddialliance.ddieditor.ui.editor.Editor;
import org.ddialliance.ddieditor.ui.editor.widgetutil.referenceselection.ReferenceSelectionCombo;
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

	public LineWizard() {
		super();
	}

	public Ddi3Helper getDdi3Helper() {
		return ddi3Helper;
	}

	@Override
	public void addPages() {
		addPage(new ResourcePage());
		addPage(new WikiPage());
		addPage(new ParsePage());
	}

	@Override
	public boolean performFinish() {
		try {
			// gather input
			ddi3Helper = new Ddi3Helper();

			// parse file
			Wiki2Ddi3Scanner wiki2Ddi3Scanner = new Wiki2Ddi3Scanner(ddi3Helper);
			wiki2Ddi3Scanner.startScanning(WikiPage.wikiSyntax);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

class ResourcePage extends WizardPage {
	public static final String PAGE_NAME = "Ref";

	ReferenceSelectionCombo uniRefSelectCombo = null;
	ReferenceSelectionCombo conRefSelectCombo = null;
	// conRefSelectCombo.getResult()
	ReferenceSelectionCombo seqRefSelectCombo = null;
	ReferenceSelectionCombo quesRefSelectCombo = null;
	boolean createConceptScheme = false;
	boolean createUniverseScheme = false;

	public ResourcePage() {
		super(PAGE_NAME, Translator.trans("line.wizard.refpage.title"), null);
	}

	@Override
	public void createControl(Composite parent) {
		Editor editor = new Editor();
		Group group = editor.createGroup(parent,
				Translator.trans("line.wizard.refpage.group"));

		// universe ref
		List<LightXmlObjectType> uniRefList = new ArrayList<LightXmlObjectType>();
		try {
			uniRefList = DdiManager.getInstance()
					.getUniversesLight(null, null, null, null)
					.getLightXmlObjectList().getLightXmlObjectList();
		} catch (Exception e) {
			// TODO
		}
		uniRefSelectCombo = editor.createRefSelection(group,
				Messages.getString("VariableEditor.label.universeref"),
				Messages.getString("VariableEditor.label.universeref"),
				ReferenceType.Factory.newInstance(), uniRefList, false);

		// create universe scheme
		Button universeSchemeCreate = editor.createCheckBox(group, "",
				Translator.trans("line.wizard.createunislabel"));
		universeSchemeCreate.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				createUniverseScheme = ((Button) e.getSource()).getSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
		});

		// concept ref
		List<LightXmlObjectType> conceptRefList = new ArrayList<LightXmlObjectType>();
		try {
			conceptRefList = DdiManager.getInstance()
					.getConceptsLight(null, null, null, null)
					.getLightXmlObjectList().getLightXmlObjectList();
		} catch (Exception e) {
			// TODO
		}
		conRefSelectCombo = editor.createRefSelection(group,
				Messages.getString("VariableEditor.label.conceptref"),
				Messages.getString("VariableEditor.label.conceptref"),
				ReferenceType.Factory.newInstance(), conceptRefList, false);

		// create concept scheme
		Button conceptSchemeCreate = editor.createCheckBox(group, "",
				Translator.trans("line.wizard.createconslabel"));
		conceptSchemeCreate.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				createConceptScheme = ((Button) e.getSource()).getSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
		});

		// question scheme ref
		List<LightXmlObjectType> questionSchemeRefList = new ArrayList<LightXmlObjectType>();
		try {
			questionSchemeRefList = DdiManager.getInstance()
					.getQuestionSchemesLight(null, null, null, null)
					.getLightXmlObjectList().getLightXmlObjectList();
		} catch (Exception e) {
			// TODO
		}
		quesRefSelectCombo = editor.createRefSelection(group,
				Translator.trans("line.wizard.refpage.ques"),
				Translator.trans("line.wizard.refpage.ques"),
				ReferenceType.Factory.newInstance(), questionSchemeRefList,
				false);

		// main seq ref
		List<LightXmlObjectType> seqRefList = new ArrayList<LightXmlObjectType>();
		try {
			seqRefList = DdiManager.getInstance()
					.getSequencesLight(null, null, null, null)
					.getLightXmlObjectList().getLightXmlObjectList();
		} catch (Exception e) {
			// TODO
		}
		seqRefSelectCombo = editor.createRefSelection(group,
				Translator.trans("line.wizard.refpage.mainseqref"),
				Translator.trans("line.wizard.refpage.mainseqref"),
				ReferenceType.Factory.newInstance(), seqRefList, false);

		// finalize
		setControl(group);
		setPageComplete(true);
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
		setPageComplete(false);
	}
}