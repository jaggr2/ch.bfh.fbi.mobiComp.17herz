package sensor;

import ch.quantasy.tinkerforge.tinker.agent.implementation.TinkerforgeStackAgent;
import ch.quantasy.tinkerforge.tinker.application.implementation.AbstractTinkerforgeApplication;
import ch.quantasy.tinkerforge.tinker.core.implementation.TinkerforgeDevice;
import com.tinkerforge.BrickletJoystick;
import com.tinkerforge.BrickletJoystick.PressedListener;
import com.tinkerforge.BrickletJoystick.PositionReachedListener;
import com.tinkerforge.Device;
import com.tinkerforge.TinkerforgeException;

/**
 * This class is responsible for receiving, processing and delegating data about
 * the ambient-temperature and Object-IR-temperature
 * 
 * @author reto
 * 
 */
public class JoystickApplication extends AbstractTinkerforgeApplication
		implements PressedListener, PositionReachedListener {

    private String Id;
    private String sUid;

	public JoystickApplication(String sUid) {
        this.sUid = sUid;
	}

	@Override
	public void deviceDisconnected(
			final TinkerforgeStackAgent tinkerforgeStackAgent,
			final Device device) {
		if (TinkerforgeDevice.getDevice(device) == TinkerforgeDevice.Joystick) {
            final BrickletJoystick joystickBrick = (BrickletJoystick) device;
            joystickBrick.removePressedListener(this);
            joystickBrick.removePositionReachedListener(this);
		}

	}

	@Override
	public void deviceConnected(
			final TinkerforgeStackAgent tinkerforgeStackAgent,
			final Device device) {
        try
        {
            if ((TinkerforgeDevice.getDevice(device) == TinkerforgeDevice.Joystick) &&
                    device.getIdentity().uid.equalsIgnoreCase(sUid) ){
                final BrickletJoystick joystickBrick = (BrickletJoystick) device;
                joystickBrick.addPressedListener(this);

                Id = device.getIdentity().toString();

                // Get threshold callbacks with a debounce time of 0.2 seconds (200ms)
                joystickBrick.setDebouncePeriod(200);

                // Configure threshold for "x or y value outside of [-99..99]"
                joystickBrick.setPositionCallbackThreshold('o', (short) -99, (short) 99, (short) -99, (short) 99);

            }
        }
        catch (final TinkerforgeException ex) {
        }
	}

    @Override
    public void positionReached(short x, short y) {

        System.out.print("Joystick Position reached:");

        if(y == 100) {
            System.out.print(" Top");
        } else if(y == -100) {
            System.out.print(" Bottom");
        }

        if(x == 100) {
            System.out.println(" Right");
        } else if(x == -100) {
            System.out.println(" Left");
        }

        System.out.println();
    }

    @Override
    public void pressed() {
        System.out.println("Joystick pressed!");
    }

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final JoystickApplication other = (JoystickApplication) obj;
		return this == other;
	}
}
