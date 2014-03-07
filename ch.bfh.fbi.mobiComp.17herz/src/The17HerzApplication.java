import ch.quantasy.tinkerforge.tinker.agency.implementation.TinkerforgeStackAgency;
import ch.quantasy.tinkerforge.tinker.agent.implementation.TinkerforgeStackAgent;
import ch.quantasy.tinkerforge.tinker.agent.implementation.TinkerforgeStackAgentIdentifier;
import ch.quantasy.tinkerforge.tinker.application.implementation.AbstractTinkerforgeApplication;
import ch.quantasy.tinkerforge.tinker.core.implementation.TinkerforgeDevice;
import com.tinkerforge.Device;
import com.tinkerforge.TinkerforgeException;
import sensor.BarometerApplication;

import java.util.HashMap;


public class The17HerzApplication extends AbstractTinkerforgeApplication {


    public HashMap<String, AbstractTinkerforgeApplication> connectedApps = new HashMap<String, AbstractTinkerforgeApplication>();

	@Override
	public void deviceDisconnected(final TinkerforgeStackAgent tinkerforgeStackAgent, final Device device) {
        try
        {
            String uid = device.getIdentity().uid;

            if(connectedApps.containsKey(uid)) {
                super.removeTinkerforgeApplication(connectedApps.get(uid));
                connectedApps.remove(uid);
                System.out.println("Device " + device + " ID: " + uid + " disconnected and connected application removed!");
            }
            else {
                System.out.println("Device " + device + " ID: " + uid + " disconnected without connected application!");
            }
        }
        catch (TinkerforgeException ex)
        {
            System.out.println("ERROR: Failed to disconnect Device " + device + "!");
        }
	}

	@Override
	public void deviceConnected(final TinkerforgeStackAgent tinkerforgeStackAgent, final Device device) {
        try
        {
            if (TinkerforgeDevice.getDevice(device) == TinkerforgeDevice.Barometer)
            {
                addApplication(device.getIdentity().uid, new BarometerApplication(device.getIdentity().uid));
            }
            else {
                System.out.println("INFO: Device " + device + " with ID " + device.getIdentity().uid + " has no connectable Application!");
            }
        }
        catch (TinkerforgeException ex)
        {
            System.out.println("ERROR: Failed to connect Device " + device + "!");
        }
	}

    public void addApplication(final String uid, final AbstractTinkerforgeApplication newApplication) {
        connectedApps.put(uid, newApplication);

        super.addTinkerforgeApplication(newApplication);

        System.out.println("Application  " + newApplication + " connected with Device ID: " + uid + " !");
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
