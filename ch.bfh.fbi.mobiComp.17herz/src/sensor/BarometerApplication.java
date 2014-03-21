package sensor;

import ch.quantasy.tinkerforge.tinker.agent.implementation.TinkerforgeStackAgent;
import ch.quantasy.tinkerforge.tinker.application.implementation.AbstractTinkerforgeApplication;
import ch.quantasy.tinkerforge.tinker.core.implementation.TinkerforgeDevice;
import com.tinkerforge.*;
import com.tinkerforge.BrickletBarometer.AirPressureListener;
import com.tinkerforge.BrickletBarometer.AirPressureReachedListener;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.text.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * This class is responsible for receiving, processing and delegating data about
 * the ambient-temperature and Object-IR-temperature
 * 
 * @author reto
 * 
 */
public class BarometerApplication extends AbstractTinkerforgeApplication
		implements AirPressureReachedListener, AirPressureListener {

    private ConcurrentLinkedQueue<Number> guiData = new ConcurrentLinkedQueue<Number>();
    private XYChart.Series<Number, Number> guiSeries;
    private int guiDataPosition = 0;

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
    private boolean waitForCalib = true;

    private final int iDiffC = 100; //0.1 mBar
    private final int iCalibrationPointDelayC = 5;
    private final int iCalibrationDelayC = 60000;

    private BarometerCalibration barometerCalibration;
    private Timer CalibTimer;

    private long lastEventTime;

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
                barometer.addAirPressureListener(this);
                barometer.setAveraging((short)0,(short) 0,(short) 0);

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

                lastEventTime = System.currentTimeMillis();

                for(IDoorEventListener listener : eventListeners) {
                    listener.doorEventHappend(this, iAirPressure);
                }

                new Thread(barometerCalibration).start();

                waitForCalib = true;
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

    public long getLastEventTime() {
        return lastEventTime;
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

    @Override
    public void airPressure(int i) {
        guiData.add(i);
    }

    private class BarometerCalibration extends TimerTask implements AirPressureListener
    {

        Object lock = new Object();
        private final long lCalibDurationC = 3000;
        private final Integer iCalibDiffC = 50;
        private long StartTime;
        private boolean calibInProgress;

        private ArrayList<Integer> aiCalibPoints = new ArrayList<Integer>() ;

        BarometerCalibration() {
            barometer.addAirPressureListener(this);
        }

        @Override
        public void run()
        {
            if (calibInProgress)
            {
                return;
            }

            calibInProgress = true;

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
            //main.The17HerzApplication.logInfo("Calibration started [SensorID=" + Id + ", ReferenceValue=" + ((aiCalibPoints.size() > 0) ? aiCalibPoints.get(0) : 0) + ", maxDiff=" + iCalibDiffC + ", MeasureInterval=" + iCalibrationPointDelayC + "]" );

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

//            try
//            {
//                barometer.setAirPressureCallbackPeriod(0);
//            }
//            catch (TimeoutException e)
//            {
//                e.printStackTrace();
//            }
//            catch (NotConnectedException e)
//            {
//                e.printStackTrace();
//            }

            calibInProgress = false;
        }

        @Override
        public void airPressure(int iAirPressure)
        {
            synchronized (lock)
            {
                aiCalibPoints.add(iAirPressure);
                //main.The17HerzApplication.logInfo("Add Calibration point [id=" + Id + ", value=" + iAirPressure + ", iActiveCalibPoint=" + iActiveCalibPoint + "]");



                if(aiCalibPoints.get(0) + iCalibDiffC < iAirPressure || aiCalibPoints.get(0) - iCalibDiffC > iAirPressure) {

                    //main.The17HerzApplication.logInfo("Calibration failed [SensorID=" + Id + ", ReferenceValue=" + aiCalibPoints.get(0) + ", airPressure=" + iAirPressure + ", maxDiff=" + iCalibDiffC + "]" );

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

    private static final int MAX_DATA_POINTS = 1000;

    /*
	private Series<Number, Number> estimatedAltitudeSeries;
	private Series<Number, Number> barometricAltitudeSeries;
	private int estimatedAltitudeXSeriesDataPosition = 0;
	private int barometricAltitudeXSeriesDataPosition = 0;
	private static ConcurrentLinkedQueue<Number> dataEstimatedAltitude = new ConcurrentLinkedQueue<Number>();
	private static ConcurrentLinkedQueue<Number> dataBarometricAltitude = new ConcurrentLinkedQueue<Number>();


    private NumberAxis xAxis;
    private NumberAxis yAxis;

    private NumberAxis initXAxis() {
        final NumberAxis xAxis = new NumberAxis(0, MAX_DATA_POINTS, MAX_DATA_POINTS / 10);
        xAxis.setTickLabelFont(Font.font("Arial", FontWeight.MEDIUM, 18));
        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false);
        return xAxis;
    }

    private NumberAxis initYAxis() {
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
            @Override
            public String toString(final Number object) {
                return String.format("%6.2f", object);
            }
        });
        yAxis.setTickLabelFont(Font.font("Arial", FontWeight.MEDIUM, 18));
        yAxis.setPrefWidth(120);
        yAxis.setAutoRanging(true);
        yAxis.setLabel("miliBar");
        yAxis.setForceZeroInRange(false);
        yAxis.setAnimated(true);
        return yAxis;
    }


    private LineChart<Number, Number> initChart() {
        // -- Chart
        final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(
                this.xAxis, this.yAxis) {
            // Override to remove symbols on each data point
            @Override
            protected void dataItemAdded(final Series<Number, Number> series,
                                         final int itemIndex, final Data<Number, Number> item) {
            }
        };
        lineChart.setAnimated(false);
        lineChart.setId("Live Altitude Position");
        lineChart.setTitle("Sensor-Fusion (Altitude)");
        return lineChart;
    }*/

    public void initChart(LineChart<Number, Number> chart) {
        this.guiSeries = new XYChart.Series<Number, Number>();
        this.guiSeries.setName("Barometer " + this.getId());
        chart.getData().add(this.guiSeries);

        // Timeline gets called in the JavaFX Main thread
        // Every frame to take any data from queue and add to chart
        new AnimationTimer() {
            @Override
            public void handle(final long now) {
                addDataToSeries();
            }
        }.start();
    }

    /*
    public void initStage(final Stage stage) {
        this.xAxis = this.initXAxis();
        this.yAxis = this.initYAxis();
        final LineChart<Number, Number> chart = this.initChart();

        // Chart Series
        this.guiSeries = new XYChart.Series<Number, Number>();
        this.guiSeries.setName("Barometer " + this.getId());
        chart.getData().add(this.guiSeries);

        stage.setScene(new Scene(chart));

        // Timeline gets called in the JavaFX Main thread
        // Every frame to take any data from queue and add to chart
        new AnimationTimer() {
            @Override
            public void handle(final long now) {
                addDataToSeries();
            }
        }.start();
    }
*/
    private void addDataToSeries() {
        for (int i = 0; i < 50; i++) { // -- add some new samples to the plot
            if (guiData.isEmpty()) {
                break;
            }
            this.guiSeries.getData().add(new LineChart.Data<Number, Number>(this.guiDataPosition++, guiData.remove()));
        }


        // remove points to keep us at no more than MAX_DATA_POINTS
        if (this.guiSeries.getData().size() > MAX_DATA_POINTS) {
            this.guiSeries.getData().remove(0, this.guiSeries.getData().size() - MAX_DATA_POINTS);
        }

        // update Axis
        //this.xAxis.setLowerBound(this.guiDataPosition - MAX_DATA_POINTS);
    }
}
