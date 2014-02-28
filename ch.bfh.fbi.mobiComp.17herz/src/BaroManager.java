import ch.quantasy.tinkerforge.tinker.agency.implementation.TinkerforgeStackAgency;
import ch.quantasy.tinkerforge.tinker.agent.implementation.TinkerforgeStackAgentIdentifier;
import ch.quantasy.tinkerforge.tinker.application.definition.TinkerforgeApplication;

public class BaroManager {

	// Assumes to be connected via USB
	public final TinkerforgeStackAgentIdentifier BARO_SENSOR = new TinkerforgeStackAgentIdentifier("localhost");

	private final TinkerforgeApplication baroApp;

	public BaroManager() {
		this.baroApp = new BaroApplication();
	}

	public void start() {
		TinkerforgeStackAgency.getInstance().getStackAgent(BARO_SENSOR)
				.addApplication(baroApp);
	}

	public void stop() {
		TinkerforgeStackAgency.getInstance().getStackAgent(BARO_SENSOR)
				.removeApplication(baroApp);
	}

	/**
	 * A simple boot-strap. The program will shut-down gracefully if one hits
	 * 'any' key on the console
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception {
		final BaroManager manager = new BaroManager();
		manager.start();
		System.in.read();
		manager.stop();

	}

}
