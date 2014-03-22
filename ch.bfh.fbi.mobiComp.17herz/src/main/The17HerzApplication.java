package main;

import ch.quantasy.tinkerforge.tinker.agency.implementation.TinkerforgeStackAgency;
import ch.quantasy.tinkerforge.tinker.agent.implementation.TinkerforgeStackAgent;
import ch.quantasy.tinkerforge.tinker.agent.implementation.TinkerforgeStackAgentIdentifier;
import ch.quantasy.tinkerforge.tinker.application.implementation.AbstractTinkerforgeApplication;
import ch.quantasy.tinkerforge.tinker.core.implementation.TinkerforgeDevice;
import com.tinkerforge.Device;
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
    // Assumes to be connected via USB
    public static final TinkerforgeStackAgentIdentifier BARO_SENSOR = new TinkerforgeStackAgentIdentifier("localhost");
    public static HashMap<Device, AbstractTinkerforgeApplication> connectedApps = new HashMap<Device, AbstractTinkerforgeApplication>();
    LinkedList<AmplitudeEvent> events = new LinkedList<AmplitudeEvent>();

    /**
     * A simple boot-strap. The program will shut-down gracefully if one hits
     * 'any' key on the console
     *
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        final The17HerzApplication the17HerzApplication = new The17HerzApplication();

        final Thread guiThread = new Thread() {
            @Override
            public void run() {
                javafx.application.Application.launch(JavaFxGUI.class, args);
            }
        };
        guiThread.start();

        TinkerforgeStackAgency.getInstance().getStackAgent(BARO_SENSOR).addApplication(the17HerzApplication);

        guiThread.join(); //wait until GUI Thread finishes

        TinkerforgeStackAgency.getInstance().getStackAgent(BARO_SENSOR).removeApplication(the17HerzApplication);

    }

    public static void logInfo(String message) {
        System.out.println("[" + dateFormat.format(new Date()) + "]" + message);
    }

    @Override
    public void deviceDisconnected(final TinkerforgeStackAgent tinkerforgeStackAgent, final Device device) {

        if (connectedApps.containsKey(device)) {

            super.removeTinkerforgeApplication(connectedApps.get(device));

            connectedApps.remove(device);

            System.out.println("Device " + device + " disconnected and connected application removed!");
        } else {
            System.out.println("Device " + device + " disconnected without connected application!");
        }
    }

    @Override
    public void deviceConnected(final TinkerforgeStackAgent tinkerforgeStackAgent, final Device device) {
        try {
            if (TinkerforgeDevice.getDevice(device) == TinkerforgeDevice.Barometer) {
                BarometerApplication app = new BarometerApplication(device.getIdentity().uid);
                app.addDoorEventListener(this);
                addApplication(device, app);
            } else if (TinkerforgeDevice.getDevice(device) == TinkerforgeDevice.Joystick) {
                addApplication(device, new JoystickApplication(device.getIdentity().uid));
            } else {
                System.out.println("INFO: Device " + device + " with ID " + device.getIdentity().uid + " has no connectable Application!");
            }
        } catch (TinkerforgeException ex) {
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
        return this == obj;
    }

    @Override
    public void doorEventHappend(BarometerApplication source, Integer airPressure) {

        AmplitudeEvent currentEvent = new AmplitudeEvent(source, airPressure);

//        Boolean eventLogged = false;
//
        // check last events
//
//        if(events.size() > 0) {
//
//            // get last event from same sensor, and if different, log it
//            for (final AmplitudeEvent event : new ListReverseIterator<AmplitudeEvent>(events)) {
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
        for (AbstractTinkerforgeApplication app : connectedApps.values()) {
            if (app instanceof BarometerApplication) {
                if ((source.getLastEventTime() - ((BarometerApplication) app).getLastEventTime()) > 200) {
                    TimeDiffToHigh = true;
                }
            }
        }

        if (!TimeDiffToHigh) {
            currentEvent.logEvent("all Barometer have event");
        }

        events.add(currentEvent);
    }

}
