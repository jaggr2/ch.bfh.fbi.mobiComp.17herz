package gui;

import gui.view.GUIApplication;
import ch.quantasy.tinkerforge.tinker.agency.implementation.TinkerforgeStackAgency;
import ch.quantasy.tinkerforge.tinker.agent.implementation.TinkerforgeStackAgent;
import ch.quantasy.tinkerforge.tinker.agent.implementation.TinkerforgeStackAgentIdentifier;

public class BarometerManager {
	public static void main(final String[] args) throws Exception {
		final TinkerforgeStackAgentIdentifier identifier = new TinkerforgeStackAgentIdentifier(
				"localhost");
		final TinkerforgeStackAgent agent1 = TinkerforgeStackAgency
				.getInstance().getStackAgent(identifier);

		new Thread() {
			@Override
			public void run() {
				javafx.application.Application.launch(GUIApplication.class,
						args);
			}
		}.start();
		final BarometerApplication2 application = new BarometerApplication2();
		agent1.addApplication(application);
		System.in.read();
		agent1.removeApplication(application);
		GUIApplication.finish();

	}
}
