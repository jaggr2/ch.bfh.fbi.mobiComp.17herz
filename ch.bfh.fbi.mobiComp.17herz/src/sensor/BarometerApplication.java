package sensor;

import ch.quantasy.tinkerforge.tinker.agent.implementation.TinkerforgeStackAgent;
import ch.quantasy.tinkerforge.tinker.application.implementation.AbstractTinkerforgeApplication;
import ch.quantasy.tinkerforge.tinker.core.implementation.TinkerforgeDevice;
import com.tinkerforge.*;
import com.tinkerforge.BrickletBarometer.AirPressureListener;
import com.tinkerforge.BrickletBarometer.AirPressureReachedListener;

import java.text.*;
import java.util.*;


/**
 * This class is responsible for receiving, processing and delegating data about
 * the ambient-temperature and Object-IR-temperature
 * 
 * @author reto
 * 
 */
public class BarometerApplication extends AbstractTinkerforgeApplication
		implements AirPressureReachedListener {

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

    private String Id;
    private String sUid;

    private BrickletBarometer barometer;
    private boolean waitForCalib = false;

    private final int iDiffC = 200; //0.2 mBar
    private final int iCalibrationPointDelayC = 5;
    private final int iCalibrationDelayC = 60000;

    private BarometerCalibration barometerCalibration;
    private Timer CalibTimer;

	public BarometerApplication(String sUid) {
        this.sUid = sUid;

        CalibTimer = new Timer();
	}

    public BrickletBarometer getBarometer() {
        return barometer;
    }

    public String getId() {
        return Id;
    }

    @Override
	public void deviceDisconnected(
			final TinkerforgeStackAgent tinkerforgeStackAgent,
			final Device device) {
		if (TinkerforgeDevice.getDevice(device) == TinkerforgeDevice.Barometer)
        {
			final BrickletBarometer barometerBrick = (BrickletBarometer) device;
            barometer = null;
            barometerBrick.removeAirPressureReachedListener(this);
            CalibTimer.cancel();
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
                barometer.addAirPressureReachedListener(this);

                Id = device.getIdentity().uid;
                barometerCalibration = new BarometerCalibration();

                CalibTimer.scheduleAtFixedRate(barometerCalibration, 0, iCalibrationDelayC);
            }
        }
        catch (final TinkerforgeException ex) {
        }
	}


    @Override
    public void airPressureReached(int iAirPressure)
    {
        try {

            if (!waitForCalib)
            {
                main.The17HerzApplication.logInfo("Ereignis bei " + formatNumber(iAirPressure, "mBar", 3) + " | Von: " + Id + barometer.getAirPressureCallbackThreshold().toString());

                new Thread(barometerCalibration).start();

                waitForCalib = true;

                for(IDoorEventListener listener : eventListeners) {
                    listener.doorEventHappend(this, iAirPressure);
                }
            }
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

    private class BarometerCalibration extends TimerTask implements AirPressureListener
    {

        Object lock = new Object();
        private final long lCalibDurationC = 2000;
        private final Integer iCalibDiffC = 50;
        private long StartTime;

        private ArrayList<Integer> aiCalibPoints = new ArrayList<Integer>() ;

        BarometerCalibration() {
            barometer.addAirPressureListener(this);
        }

        @Override
        public void run()
        {
            try
            {
                barometer.setAirPressureCallbackPeriod(iCalibrationPointDelayC);
            }
            catch (TimeoutException e)
            {
                e.printStackTrace();
            }
            catch (NotConnectedException e)
            {
                e.printStackTrace();
            }

            StartTime = System.currentTimeMillis();
            main.The17HerzApplication.logInfo("Calibration started [SensorID=" + Id + ", ReferenceValue=" + ((aiCalibPoints.size() > 0) ? aiCalibPoints.get(0) : 0) + ", maxDiff=" + iDiffC + ", MeasureInterval=" + iCalibrationPointDelayC + "]" );

            synchronized (lock)
            {
                aiCalibPoints.clear();
            }
            while ((System.currentTimeMillis() - StartTime) < lCalibDurationC)
            {
                try
                {
                    Thread.sleep(10);
                } catch (InterruptedException e)
                {
                }
            }

            // Durchschnitt der Kalibrationspunkte errechnen
            long sum = 0;
            synchronized (lock)
            {
                for (Integer point: aiCalibPoints)
                {

                    sum += point;
                }
                if (aiCalibPoints.size() > 0)
                {
                    int iThresholdValue = (int) (sum / aiCalibPoints.size());

                    main.The17HerzApplication.logInfo("Calibration succeeded [SensorID=" + Id + ", NewThresholdValue=" + iThresholdValue + ", MeasurePointCount=" + aiCalibPoints.size() + "]" );

                    setThreshold(iThresholdValue);
                }
            }

            try
            {
                barometer.setAirPressureCallbackPeriod(0);
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

        @Override
        public void airPressure(int iAirPressure)
        {
            synchronized (lock)
            {
                aiCalibPoints.add(iAirPressure);
                //main.The17HerzApplication.logInfo("Add Calibration point [id=" + Id + ", value=" + iAirPressure + ", iActiveCalibPoint=" + iActiveCalibPoint + "]");



                if(aiCalibPoints.get(0) + iCalibDiffC < iAirPressure || aiCalibPoints.get(0) - iCalibDiffC > iAirPressure) {

                    main.The17HerzApplication.logInfo("Calibration failed [SensorID=" + Id + ", ReferenceValue=" + aiCalibPoints.get(0) + ", airPressure=" + iAirPressure + ", maxDiff=" + iDiffC + "]" );

                    aiCalibPoints.clear();
                    StartTime = System.currentTimeMillis();
                }
            }
        }

        @Override
        protected void finalize()
        {
            if (barometer != null)
            {
                barometer.removeAirPressureListener(this);
            }
        }

        public void setThreshold (int iAirPressure)
        {
            try
            {
                barometer.setAirPressureCallbackThreshold('o', iAirPressure - iDiffC, iAirPressure + iDiffC);
            }
            catch (TimeoutException e)
            {
                e.printStackTrace();
            }
            catch (NotConnectedException e)
            {
                e.printStackTrace();
            }

            waitForCalib = false;

        }
    }
}
