package dk.dda.ddieditor.line.wizard;

import org.ddialliance.ddieditor.ui.editor.Editor;
import org.ddialliance.ddiftp.util.Translator;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import dk.dda.ddieditor.line.dialog.EditWikiSyntaxDialog;

public class WikiPage extends WizardPage {
	public static final String PAGE_NAME = "Wiki";
	public static String fileName = "";
	public static String wikiSyntax = "";

	public WikiPage() {
		super(PAGE_NAME, Translator.trans("line.wizard.refpage.title"), null);
	}

	private void readAndDisplayFile(String filename, Browser browser) {
		// read in file
		WikiPage.wikiSyntax = LineWizard.readFile(filename);
		// parse in browser
		LineWizard.displayWiki(WikiPage.wikiSyntax, browser, true);
	}

	@Override
	public void createControl(Composite parent) {
		Editor editor = new Editor();
		Group group = editor.createGroup(parent,
				Translator.trans("line.dialog.group"));

		// line.filecreateIfThenElse
		editor.createLabel(group, Translator.trans("line.filechooser.title"));
		final Text pathText = editor.createText(group, "", false);
		Button pathBrowse = editor.createButton(group,
				Translator.trans("line.filechooser.browse"));
		final Browser browser = editor.createBrowser(group, "Question markup");
		if (browser != null) {
			browser.setText("");
		}

		pathText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				// on a CR - read and display file:
				if (e.keyCode == SWT.CR) {
					readAndDisplayFile(pathText.getText(), browser);
					// set page complete
					setPageComplete(true);
				}
			}
		});
		pathText.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				switch (e.detail) {
				case SWT.TRAVERSE_TAB_NEXT:
					break;
				case SWT.TRAVERSE_TAB_PREVIOUS: {
					readAndDisplayFile(pathText.getText(), browser);
					// set page complete
					setPageComplete(true);
					e.doit = true;
					break;
				}
				default:
					break;
				}
			}
		});

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
						fileName, WikiPage.wikiSyntax);
				int returnCode = dialog.open();
				if (returnCode == Window.CANCEL) {
					return;
				}

				if (dialog.result != null && !dialog.result.equals("")) {
					wikiSyntax = dialog.result;
					WikiPage.wikiSyntax = wikiSyntax;

					// parse in browser
					LineWizard.displayWiki(dialog.result, browser, false);

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