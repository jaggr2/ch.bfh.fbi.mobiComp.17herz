package sensor;

import ch.quantasy.tinkerforge.tinker.agent.implementation.TinkerforgeStackAgent;
import ch.quantasy.tinkerforge.tinker.application.implementation.AbstractTinkerforgeApplication;
import ch.quantasy.tinkerforge.tinker.core.implementation.TinkerforgeDevice;
import com.tinkerforge.*;
import com.tinkerforge.BrickletBarometer.AirPressureListener;
import com.tinkerforge.BrickletBarometer.AltitudeListener;
import com.tinkerforge.BrickletBarometer.AirPressureReachedListener;

import java.text.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class is responsible for receiving, processing and delegating data about
 * the ambient-temperature and Object-IR-temperature
 * 
 * @author reto
 * 
 */
public class BarometerApplication extends AbstractTinkerforgeApplication
		implements AirPressureListener, AltitudeListener, AirPressureReachedListener {

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");


    public static String formatNumber(Integer number, String unit, double kommastellen) {
        double doubleNumber = (double)number / Math.pow(10, kommastellen);

        return (new DecimalFormat("#,###,##0.000")).format(doubleNumber) + unit;
    }

    private final List<IDoorEventListener> eventListeners = new ArrayList<IDoorEventListener>();

    public void addDoorEventListener( IDoorEventListener listener )
    {
        if ( ! eventListeners.contains( listener ) ) {
            eventListeners.add( listener );
        }
    }

    public void removeDoorEventListener( IDoorEventListener observer )
    {
        eventListeners.remove( observer );
    }



    private int iMaxAltitude = 0;
    private int iMinAltitude = 500000;

    private int iMaxAirPressure = 0;
    private int iMinAirPressure = 10000000;

    private String Id;
    private String sUid;

    private  BrickletBarometer barometer;
    private int iActiveCalibPoint = 0;

    private final int iDiffC = 200; //0.2 mBar
    private final int iCalibrationPointsC = 40;
    private final int iCalibrationPointDelayC = 50;
    private final int iCalibrationDelayC = 60000;
    private ArrayList<Integer> aiCalibPoints = new ArrayList<Integer>() ;

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
                barometer.setAirPressureCallbackPeriod(iCalibrationPointDelayC);

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

        if (++iActiveCalibPoint == iCalibrationPointsC)
        {
            // Durchschnitt der Kalibrationspunkte errechnen
            long sum = 0;
            for (Integer point: aiCalibPoints)
            {
                sum += point;
            }
            if (aiCalibPoints.size() > 0)
            {
                int iThresholdValue = (int) (sum / aiCalibPoints.size());
                setThreshold(iThresholdValue);
                System.out.println(dateFormat.format(new Date()) + ": Neuer Kalibwert : " + iThresholdValue + " | Von: " + Id);
            }
        }
        else if (iActiveCalibPoint > iCalibrationPointsC)
        {
            // Kalibration zurücksetzen
            iActiveCalibPoint  = 0;
            aiCalibPoints.clear();
            try
            {
                barometer.setAirPressureCallbackPeriod(iCalibrationDelayC);
            }
            catch (TimeoutException e)
            {
                e.printStackTrace();
            }
            catch (NotConnectedException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            aiCalibPoints.add(iAirPressure);
        }

    }


    public void setThreshold (int iAirPressure)
    {
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


            System.out.println(dateFormat.format(new Date()) + ": Ereignis bei " + formatNumber(iAirPressure, "mBar", 3) + " | Von: " + Id + barometer.getAirPressureCallbackThreshold().toString());

            setThreshold(iAirPressure);

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
