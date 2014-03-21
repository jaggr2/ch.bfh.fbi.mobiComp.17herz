package gui.view;

import java.util.LinkedList;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 */
public class GUIApplication extends Application {
	private static List<Stage> stages = new LinkedList<Stage>();

	public GUIApplication() {

	}

	@Override
	public void start(final Stage primaryStage) throws Exception {
		// primaryStage.show();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				final Stage altitudeStage = new Stage();
				final AltitudeProfileView e = new AltitudeProfileView(
						altitudeStage);
				GUIApplication.stages.add(altitudeStage);
				
				for (final Stage stage : GUIApplication.stages) {
					stage.show();
				}
			}
		});

	}

	public static void finish() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				for (final Stage stage : GUIApplication.stages) {
					stage.close();
				}
			}
		});
	}

}
