package dk.dda.ddieditor.line.dialog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import org.ddialliance.ddieditor.ui.editor.Editor;
import org.ddialliance.ddiftp.util.Translator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import dk.dda.ddieditor.line.wizard.LineWizard;

public class EditWikiSyntaxDialog extends Dialog {
	public String fileName;
	public String result = "";

	public EditWikiSyntaxDialog(Shell parentShell, String fileName) {
		super(parentShell);
		// setShellStyle(getShellStyle()|SWT.V_S CROLL|SWT.H_SCROLL);
		this.fileName = fileName;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		this.getShell().setText(Translator.trans("line.wikieditdialog.title"));
		final Shell currentShell = this.getShell();

		// group
		Editor editor = new Editor("");
		Group group = editor.createGroup(parent,
				Translator.trans("line.wikieditdialog.title"));
		group.setLayoutData(new GridData(1000, 600));

		Label markup = editor.createLabel(group,
				Translator.trans("line.wikieditdialog.markup"));
		markup.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
				false, 1, 1));
		Label edit = editor.createLabel(group,
				Translator.trans("line.wikieditdialog.edit"));
		edit.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
				false, 1, 1));
		final Browser browser = editor.createBrowser(group);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		final StyledText input = editor.createTextAreaInput(group,
				Translator.trans("line.wikieditdialog.edit.inittext"));
		input.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		input.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				result = input.getText();
			}
		});

		// file
		if (fileName != null && !fileName.equals("")) {
			String wikiSyntax = LineWizard.readFile(fileName);
			input.setText(wikiSyntax);
			LineWizard.displayWiki(input.getText(), browser);
			result = input.getText();
		}

		// holder
		Composite holder = new Composite(group, SWT.NONE);
		holder.setLayout(new FillLayout());
		holder.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false, 2,
				1));

		// parse
		Button parse = new Button(holder, 0);
		parse.setText(Translator.trans("line.wikieditdialog.parsebutton"));

		parse.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				LineWizard.displayWiki(input.getText(), browser);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
		});

		// save
		Button save = new Button(holder, 0);
		save.setText(Translator.trans("line.wikieditdialog.savebutton"));
		save.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// location
				FileDialog fd = new FileDialog(currentShell, SWT.SAVE);
				fd.setText(Translator.trans("line.wikieditdialog.savebutton"));
				String[] filterExt = { "*.txt" };
				fd.setFilterExtensions(filterExt);
				if (fileName!=null&&!fileName.equals("")) {
					fd.setFileName(fileName);
				}				
				fileName = fd.open();

				// content
				try {
					File f = new File(fileName);
					if (!f.exists()) {
						f.createNewFile();
					}
					Writer output = new BufferedWriter(new FileWriter(f));
					try {
						output.write(result);
					} finally {
						output.close();
					}
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
		});

		return null;
	}
}
