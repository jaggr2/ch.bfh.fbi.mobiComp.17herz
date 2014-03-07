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

    private int iMaxAltitude = 0;
    private int iMinAltitude = 500000;

    private int iMaxAirPressure = 0;
    private int iMinAirPressure = 10000000;

    private String Id;
    private String sUid;

	public BarometerApplication(String sUid) {
        this.sUid = sUid;
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
        try
        {
            if ((TinkerforgeDevice.getDevice(device) == TinkerforgeDevice.Barometer) &&
                    device.getIdentity().uid.equalsIgnoreCase(sUid) ){
                final BrickletBarometer barometerBrick = (BrickletBarometer) device;
                barometerBrick.addAirPressureListener(this);
                barometerBrick.addAltitudeListener(this);

                Id = device.getIdentity().toString();
                barometerBrick.setAirPressureCallbackPeriod(500);
                barometerBrick.setAltitudeCallbackPeriod(500);

            }
        }
        catch (final TinkerforgeException ex) {
        }
	}

    @Override
    public void airPressure(int iAirPressure) {
        if (iAirPressure > iMaxAirPressure)
        {
            iMaxAirPressure = iAirPressure;
            System.out.println("Air Pressure Max " + Id + ": " + iAirPressure);
        }
        if (iAirPressure < iMinAirPressure)
        {
            iMinAirPressure = iAirPressure;
            System.out.println("Air Pressure Min " + Id + ": " + iAirPressure);
        }

    }

    @Override
    public void altitude(int iAltitude) {
        if (iAltitude > iMaxAltitude)
        {
            iMaxAltitude = iAltitude;
            System.out.println("Altitude Max " + Id + ": " + iAltitude);
        }
        if (iAltitude < iMinAltitude)
        {
            iMinAltitude = iAltitude;
            System.out.println("Altitude Min " + Id + ": " + iAltitude);
        }
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
