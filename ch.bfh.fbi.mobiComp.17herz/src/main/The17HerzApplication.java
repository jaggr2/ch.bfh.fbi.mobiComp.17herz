package main;

import ch.quantasy.tinkerforge.tinker.agency.implementation.TinkerforgeStackAgency;
import ch.quantasy.tinkerforge.tinker.agent.implementation.TinkerforgeStackAgent;
import ch.quantasy.tinkerforge.tinker.agent.implementation.TinkerforgeStackAgentIdentifier;
import ch.quantasy.tinkerforge.tinker.application.implementation.AbstractTinkerforgeApplication;
import ch.quantasy.tinkerforge.tinker.core.implementation.TinkerforgeDevice;
import com.tinkerforge.Device;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;
import com.tinkerforge.TinkerforgeException;
import sensor.BarometerApplication;
import sensor.IDoorEventListener;
import sensor.JoystickApplication;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;


public class The17HerzApplication extends AbstractTinkerforgeApplication implements IDoorEventListener {

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");


    public HashMap<Device, AbstractTinkerforgeApplication> connectedApps = new HashMap<Device, AbstractTinkerforgeApplication>();

	@Override
	public void deviceDisconnected(final TinkerforgeStackAgent tinkerforgeStackAgent, final Device device) {

        if(connectedApps.containsKey(device)) {

            super.removeTinkerforgeApplication(connectedApps.get(device));

            connectedApps.remove(device);

            System.out.println("Device " + device + " disconnected and connected application removed!");
        }
        else {
            System.out.println("Device " + device + " disconnected without connected application!");
        }
	}

	@Override
	public void deviceConnected(final TinkerforgeStackAgent tinkerforgeStackAgent, final Device device) {
        try
        {
            if (TinkerforgeDevice.getDevice(device) == TinkerforgeDevice.Barometer)
            {
                BarometerApplication app = new BarometerApplication(device.getIdentity().uid);
                app.addDoorEventListener(this);
                addApplication(device, app);
            }
            else if(TinkerforgeDevice.getDevice(device) == TinkerforgeDevice.Joystick) {
                addApplication(device, new JoystickApplication(device.getIdentity().uid));
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

    public void addApplication(final Device device, final AbstractTinkerforgeApplication newApplication) {
        connectedApps.put(device, newApplication);

        super.addTinkerforgeApplication(newApplication);

        System.out.println("Application  " + newApplication + " connected with Device " + device + " !");
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

    LinkedList<DoorEvent> events = new LinkedList<DoorEvent>() ;

    @Override
    public void doorEventHappend(BarometerApplication source, Integer airPressure) {

        DoorEvent currentEvent = new DoorEvent(source, airPressure);

        Boolean eventLogged = false;

        // check last events

//        if(events.size() > 0) {
//
//            // get last event from same sensor, and if different, log it
//            for (final DoorEvent event : new ListReverser<DoorEvent>(events)) {
//                if(event.getBarometerId().equals(currentEvent.getBarometerId())) {
//                    if(event.isUp != currentEvent.isUp) {
//                        currentEvent.logEvent("same sensor, other direction");
//                        eventLogged = true;
//                        break;
//                    }
//                }
//                else if(Math.abs(event.getTimestamp().getTime() - currentEvent.getTimestamp().getTime()) < 200 ) { // not the same sensor, but between 10ms
//                    currentEvent.logEvent("other sensor had same event");
//                    eventLogged = true;
//                    break;
//                }
//            }
//        }
//
//        if(!eventLogged) {
//            currentEvent.logEvent("no combination of event detected, so just log it");
//        }

        boolean TimeDiffToHigh = false;
        for (AbstractTinkerforgeApplication app : connectedApps.values())
        {
             if (app instanceof BarometerApplication)
             {
                 if ((source.getLastEventTime() - ((BarometerApplication) app).getLastEventTime()) > 200 )
                 {
                       TimeDiffToHigh = true;
                 }
             }
        }

        if (!TimeDiffToHigh)
        {
            currentEvent.logEvent("all Barometer have event");
        }

        events.add(currentEvent);
    }


    private class DoorEvent {

        private Date timestamp;
        private Integer airPressure;
        private String barometerId;
        private Integer threshold;
        private Integer thresholdMin;
        private Integer thresholdMax;
        private Boolean isUp;

        private DoorEvent(BarometerApplication barometer, Integer airPressure) {
            this.timestamp = new Date();
            this.airPressure = airPressure;
            this.barometerId = barometer.getId();
            try {
                this.thresholdMax = barometer.getBarometer().getAirPressureCallbackThreshold().max;
                this.thresholdMin = barometer.getBarometer().getAirPressureCallbackThreshold().min;
                this.threshold = (thresholdMax + thresholdMin) / 2;
            } catch (TimeoutException e) {
                e.printStackTrace();
            } catch (NotConnectedException e) {
                e.printStackTrace();
            }

            isUp = this.threshold < airPressure;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public Integer getAirPressure() {
            return airPressure;
        }

        public String getBarometerId() {
            return barometerId;
        }

        public Integer getThreshold() {
            return threshold;
        }

        public Integer getThresholdMin() {
            return thresholdMin;
        }

        public Integer getThresholdMax() {
            return thresholdMax;
        }

        public Boolean getIsUp() {
            return isUp;
        }

        public void logEvent(String reason) {
            logInfo((isUp ? "Close" : "Open") + " detected! Reason: " + reason + ". " + this);
        }

        @Override
        public String toString() {
            return "DoorEvent{" +
                    "timestamp=" + timestamp +
                    ", airPressure=" + airPressure +
                    ", barometerId='" + barometerId + '\'' +
                    ", threshold=" + threshold +
                    ", thresholdMin=" + thresholdMin +
                    ", thresholdMax=" + thresholdMax +
                    ", isUp=" + isUp +
                    '}';
        }
    }

    public static void logInfo(String message) {
        System.out.println("[" + dateFormat.format(new Date()) + "]" + message);
    }
}
