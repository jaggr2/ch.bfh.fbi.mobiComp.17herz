package sensor;

import ch.quantasy.tinkerforge.tinker.agent.implementation.TinkerforgeStackAgent;
import ch.quantasy.tinkerforge.tinker.application.implementation.AbstractTinkerforgeApplication;
import ch.quantasy.tinkerforge.tinker.core.implementation.TinkerforgeDevice;
import com.tinkerforge.BrickletBarometer;
import com.tinkerforge.BrickletBarometer.AirPressureListener;
import com.tinkerforge.BrickletBarometer.AltitudeListener;
import com.tinkerforge.Device;
import com.tinkerforge.TinkerforgeException;

/**
 * This class is responsible for receiving, processing and delegating data about
 * the ambient-temperature and Object-IR-temperature
 * 
 * @author reto
 * 
 */
public class BarometerApplication extends AbstractTinkerforgeApplication
		implements AirPressureListener, AltitudeListener {

	public BarometerApplication() {

	}

	@Override
	public void deviceDisconnected(
			final TinkerforgeStackAgent tinkerforgeStackAgent,
			final Device device) {
		if (TinkerforgeDevice.getDevice(device) == TinkerforgeDevice.Barometer) {
			final BrickletBarometer barometerBrick = (BrickletBarometer) device;
            barometerBrick.removeAirPressureListener(this);
            barometerBrick.removeAltitudeListener(this);

			try {
                barometerBrick.setAirPressureCallbackPeriod(500);
                barometerBrick.setAltitudeCallbackPeriod(500);
			} catch (final TinkerforgeException ex) {
			}

		}

	}

	@Override
	public void deviceConnected(
			final TinkerforgeStackAgent tinkerforgeStackAgent,
			final Device device) {
		if (TinkerforgeDevice.getDevice(device) == TinkerforgeDevice.Barometer) {
            final BrickletBarometer barometerBrick = (BrickletBarometer) device;
            barometerBrick.addAirPressureListener(this);
            barometerBrick.addAltitudeListener(this);

            try {
                barometerBrick.setAirPressureCallbackPeriod(500);
                barometerBrick.setAltitudeCallbackPeriod(500);
            } catch (final TinkerforgeException ex) {
            }

		}
	}

    @Override
    public void airPressure(int i) {
        System.out.println("Air Pressure: " + i);

    }

    @Override
    public void altitude(int i) {
        System.out.println("Altitude: " + i);
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
		final BarometerApplication other = (BarometerApplication) obj;
		return this == other;
	}
}
