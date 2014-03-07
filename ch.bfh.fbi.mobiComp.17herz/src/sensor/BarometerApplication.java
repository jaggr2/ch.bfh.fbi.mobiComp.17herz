package sensor;

import ch.quantasy.tinkerforge.tinker.agent.implementation.TinkerforgeStackAgent;
import ch.quantasy.tinkerforge.tinker.application.implementation.AbstractTinkerforgeApplication;
import ch.quantasy.tinkerforge.tinker.core.implementation.TinkerforgeDevice;
import com.tinkerforge.*;
import com.tinkerforge.BrickletBarometer.AirPressureListener;
import com.tinkerforge.BrickletBarometer.AltitudeListener;
import com.tinkerforge.BrickletBarometer.AirPressureReachedListener;

import java.util.Date;

/**
 * This class is responsible for receiving, processing and delegating data about
 * the ambient-temperature and Object-IR-temperature
 * 
 * @author reto
 * 
 */
public class BarometerApplication extends AbstractTinkerforgeApplication
		implements AirPressureListener, AltitudeListener, AirPressureReachedListener {

    private int iMaxAltitude = 0;
    private int iMinAltitude = 500000;

    private int iMaxAirPressure = 0;
    private int iMinAirPressure = 10000000;

    private String Id;
    private String sUid;

    private  BrickletBarometer barometer;

    private final int iDiffC = 200; //0.2 mBar

	public BarometerApplication(String sUid) {
        this.sUid = sUid;
	}

	@Override
	public void deviceDisconnected(
			final TinkerforgeStackAgent tinkerforgeStackAgent,
			final Device device) {
		if (TinkerforgeDevice.getDevice(device) == TinkerforgeDevice.Barometer)
        {
			final BrickletBarometer barometerBrick = (BrickletBarometer) device;
            barometer = null;
            barometerBrick.removeAirPressureListener(this);
            barometerBrick.removeAltitudeListener(this);
            barometerBrick.removeAirPressureReachedListener(this);

		}

	}

	@Override
	public void deviceConnected(
			final TinkerforgeStackAgent tinkerforgeStackAgent,
			final Device device) {
        try
        {
            if ((TinkerforgeDevice.getDevice(device) == TinkerforgeDevice.Barometer) &&
                    device.getIdentity().uid.equalsIgnoreCase(sUid) )
            {
                barometer = (BrickletBarometer) device;
                barometer.addAirPressureListener(this);
                barometer.addAltitudeListener(this);
                barometer.addAirPressureReachedListener(this);

                Id = device.getIdentity().uid;
                barometer.setAirPressureCallbackPeriod(2000);

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
            //System.out.println(new Date().toString() + ": Air Pressure Max " + Id + ": " + iAirPressure);
        }
        if (iAirPressure < iMinAirPressure)
        {
            iMinAirPressure = iAirPressure;
            //System.out.println(new Date().toString() + ": Air Pressure Min " + Id + ": " + iAirPressure);
        }
        try {
            barometer.setAirPressureCallbackThreshold('o', iAirPressure - iDiffC, iAirPressure + iDiffC);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (NotConnectedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void altitude(int iAltitude) {
        if (iAltitude > iMaxAltitude)
        {
            iMaxAltitude = iAltitude;
            //System.out.println(new Date().toString() + ": Altitude Max " + Id + ": " + iAltitude);
        }
        if (iAltitude < iMinAltitude)
        {
            iMinAltitude = iAltitude;
            //System.out.println(new Date().toString() + ": Altitude Min " + Id + ": " + iAltitude);
        }
    }

    @Override
    public void airPressureReached(int iAirPressure)
    {
        try {

            System.out.println(new Date().toString() + " Ereigniss aufgetreten!  Bei: " + iAirPressure + " Von: " + Id + barometer.getAirPressureCallbackThreshold().toString());

            barometer.setAirPressureCallbackThreshold('o', iAirPressure - iDiffC, iAirPressure + iDiffC);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (NotConnectedException e) {
            e.printStackTrace();
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
