import ch.quantasy.tinkerforge.tinker.agency.implementation.TinkerforgeStackAgency;
import ch.quantasy.tinkerforge.tinker.agent.implementation.TinkerforgeStackAgent;
import ch.quantasy.tinkerforge.tinker.agent.implementation.TinkerforgeStackAgentIdentifier;
import ch.quantasy.tinkerforge.tinker.application.implementation.AbstractTinkerforgeApplication;
import ch.quantasy.tinkerforge.tinker.core.implementation.TinkerforgeDevice;
import com.tinkerforge.Device;
import com.tinkerforge.TinkerforgeException;
import sensor.BarometerApplication;


public class The17HerzApplication extends AbstractTinkerforgeApplication {


    public BarometerApplication barometerApplication = null;

	public The17HerzApplication() {
        barometerApplication = new BarometerApplication("k5K");
        super.addTinkerforgeApplication(barometerApplication);
        super.addTinkerforgeApplication(new BarometerApplication("k6Y"));

	}

	@Override
	public void deviceDisconnected(final TinkerforgeStackAgent tinkerforgeStackAgent, final Device device) {
        System.out.println("Device " + device + " disconnected!");

	}

	@Override
	public void deviceConnected(final TinkerforgeStackAgent tinkerforgeStackAgent, final Device device) {
        try
        {
            System.out.println("Device " + device + " ID: " + device.getIdentity().uid + " connected!");
        }
        catch (TinkerforgeException ex)
        {}
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equals(final Object obj) {
		return this==obj;
	}

    // Assumes to be connected via USB
    public static final TinkerforgeStackAgentIdentifier BARO_SENSOR = new TinkerforgeStackAgentIdentifier("localhost");

    /**
     * A simple boot-strap. The program will shut-down gracefully if one hits
     * 'any' key on the console
     *
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        final The17HerzApplication the17HerzApplication = new The17HerzApplication();

        TinkerforgeStackAgency.getInstance().getStackAgent(BARO_SENSOR).addApplication(the17HerzApplication);

        System.in.read();  // read a character vom Console input

        TinkerforgeStackAgency.getInstance().getStackAgent(BARO_SENSOR).removeApplication(the17HerzApplication);

    }
}
