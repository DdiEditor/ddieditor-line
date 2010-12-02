package dk.dda.ddieditor.line.dialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

import org.ddialliance.ddieditor.model.resource.DDIResourceType;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.ddialliance.ddieditor.ui.editor.Editor;
import org.ddialliance.ddieditor.ui.view.Messages;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.Translator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.jamwiki.parser.ParserException;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.jflex.JFlexParser;

public class ImportLineDialog extends Dialog {
	private List<DDIResourceType> resources = null;
	public DDIResourceType selectedResource = null;
	public String searchTxt;
	public String fileName;

	public ImportLineDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// dialog setup
		this.getShell().setText(Translator.trans("line.dialog.title"));

		// group
		Editor editor = new Editor();
		Group group = editor.createGroup(parent,
				Translator.trans("line.dialog.group"));
		group.setLayoutData(new GridData(700, 400));

		// line.file
		editor.createLabel(group, Translator.trans("line.filechooser.title"));
		final Text pathText = editor.createText(group, "", false);
		Button pathBrowse = editor.createButton(group,
				Translator.trans("line.filechooser.browse"));
		
		final Browser browser = editor.createBrowser(group, "Question markup");
		pathBrowse.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileChooser = new FileDialog(PlatformUI
						.getWorkbench().getDisplay().getActiveShell());
				fileChooser.setText(Translator.trans("line.filechooser.title"));
				fileChooser.setFilterExtensions(new String[] { "*.txt" });
				fileChooser.setFilterNames(new String[] { Translator
						.trans("line.filechooser.filternames") });
				fileName = fileChooser.open();
				pathText.setText(fileName);
				
				// read in file
				Scanner scanner = null;
				try {
					scanner = new Scanner(new File(fileName), "utf-8");
				} catch (FileNotFoundException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				StringBuilder wiki = new StringBuilder();
				while (scanner.hasNextLine()) {
					wiki.append(scanner.nextLine()+System.getProperty("line.separator"));
				}
				scanner.close();

				// parse wiki text
				ParserInput parserInput = new ParserInput();
				parserInput.setTopicName("");
				parserInput.setVirtualWiki("");
				parserInput.setContext("");
				JFlexParser parser = new JFlexParser(parserInput);
				try {
					String html = parser.parseHTML(new ParserOutput(), wiki.toString());
					browser.setText(html);
				} catch (ParserException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
		});

		// loaded resources
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
		combo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				Combo c = (Combo) event.getSource();
				selectedResource = resources.get(c.getSelectionIndex());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				// do nothing
			}
		});

		return null;
	}
}
