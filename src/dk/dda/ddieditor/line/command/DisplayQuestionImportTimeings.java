package dk.dda.ddieditor.line.command;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.ddialliance.ddieditor.ui.editor.Editor;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class DisplayQuestionImportTimeings extends
		org.eclipse.core.commands.AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// log scan
		StringBuilder text = new StringBuilder();
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile("./logs/System.log", "r");
		} catch (Exception e) {
			Editor.showError(e, this.getClass().getName());
			return null;
		}

		String line;
		String logEntryPattern = "ImportLine.run";
		try {
			while ((line = raf.readLine()) != null) {
				if (line.indexOf(logEntryPattern) > -1) {
					if (text.length() > 1) {
						text.append("\n");
					}
					text.append(line);
				}
			}
		} catch (Exception e) {
			Editor.showError(e, this.getClass().getName());
			return null;
		} finally {
			try {
				raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// dialog
		QuestionImportTimeingsDialog d = new QuestionImportTimeingsDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				"Import Question Timeings", "Log Entries", "Import", text
						.toString());
		d.open();
		return null;
	}

	static class QuestionImportTimeingsDialog extends Dialog {
		String note;
		String title;
		String group;
		String label;

		public QuestionImportTimeingsDialog(Shell parentShell, String title,
				String group, String label, String note) {
			super(parentShell);
			this.note = note;
			this.title = title;
			this.group = group;
			this.label = label;
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			// dialog setup
			this.getShell().setText(title);

			// group
			Editor editor = new Editor();
			Group swtGroup = editor.createGroup(parent, group);
			swtGroup.setLayoutData(new GridData(800, 400));
			editor.createTextAreaInput(swtGroup, label, note, null);

			Label l = editor
					.createLabel(swtGroup,
							"Time is formated as a ISO8601 period: Pyyyy-MM-dd'T'HH:mm:ss.SSSZZ");
			l.setLayoutData(new GridData(SWT.LEFT	, SWT.CENTER, false, false,
					2, 1));
			return null;
		}
	}
}
