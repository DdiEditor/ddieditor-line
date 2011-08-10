package dk.dda.ddieditor.line.view;

import java.lang.reflect.Field;

import org.ddialliance.ddieditor.ui.editor.Editor;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.views.markers.ExtendedMarkersView;
import org.eclipse.ui.views.markers.MarkerSupportView;

import dk.dda.ddieditor.line.util.Ddi3Helper;

public class ProblemView extends MarkerSupportView  {
	static final String contentGeneratorId = "dk.dda.ddieditor.line.view.problemMarkerContentGenerator";
	public static final String ID = "dk.dda.ddieditor.line.view.ProblemView";	
	private static final String VALUE_MEMENTO_ID = "va";
	private static final String DELIMITER = "___";
	
	public ProblemView(String contentGeneratorId) {
		super(ProblemView.contentGeneratorId);
	}

	public ProblemView() {
		super(ProblemView.contentGeneratorId);
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento==null) {
			return;
		}
		
		IMemento[] mementos = memento.getChildren(VALUE_MEMENTO_ID);
		for (int i = 0; i < mementos.length; i++) {
			String[] value = mementos[i].getTextData().split(DELIMITER);
			try {
				Ddi3Helper.createMarker(
				Integer.parseInt(value[2]),
						value[1],
						value[0]);
			} catch (NumberFormatException e) {
				Editor.showError(e, ID);
			} catch (DDIFtpException e) {
				Editor.showError(e, ID);
			}
		}
	}

	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		try {
			TreeItem[] items = getViewerImpl().getTree().getItems();
			StringBuilder value = new StringBuilder();

			for (int i = 0; i < items.length; i++) {
				IMemento child = memento.createChild(VALUE_MEMENTO_ID, "" + i);
				for (int k = 0; k < 3; k++) {
					value.append(items[i].getText(k));
					if (k<3) {
						value.append(DELIMITER);
					}
				}
				child.putTextData(value.toString());
			}
		} catch (Exception e) {
			Editor.showError(e, ID);
		}
	}
	
	// hack to access marker viewer
	TreeViewer getViewerImpl() throws Exception {
		Field f = ExtendedMarkersView.class.getDeclaredField("viewer");
		f.setAccessible(true);
		return (TreeViewer) f.get(ExtendedMarkersView.class.cast(this));
	}
}
