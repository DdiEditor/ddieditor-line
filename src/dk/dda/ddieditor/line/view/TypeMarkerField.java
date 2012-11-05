package dk.dda.ddieditor.line.view;

import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerItem;

public class TypeMarkerField extends MarkerField {
	public static final String DDI_TYPE = "ddi_type";

	@Override
	public String getValue(MarkerItem item) {
		return item.getAttributeValue(DDI_TYPE, "NA");
	}
}
