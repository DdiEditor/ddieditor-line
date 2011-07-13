package dk.dda.ddieditor.line.view;

import org.eclipse.ui.views.markers.MarkerSupportView;

public class ProblemView extends MarkerSupportView {
	static String contentGeneratorId = "dk.dda.ddieditor.line.view.problemMarkerContentGenerator";

	public ProblemView(String contentGeneratorId) {
		super(ProblemView.contentGeneratorId);
	}

	public ProblemView() {
		super(ProblemView.contentGeneratorId);
	}
}
