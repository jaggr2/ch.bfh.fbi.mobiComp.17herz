import ch.quantasy.tinkerforge.tinker.agent.implementation.TinkerforgeStackAgent;
import ch.quantasy.tinkerforge.tinker.application.implementation.AbstractTinkerforgeApplication;
import com.tinkerforge.Device;


public class BaroApplication extends AbstractTinkerforgeApplication {
	// A heuristic value representing the illuminance-threshold above which
	// the light in the fridge is lit.
	// Why is it not 0? The sensor gives some noise!
	// Why is it not 0? Old cheese might be glowing in the dark ;-)
	public static final int LIGHT_IS_SWITCHED_ON = 8;
	public static final int LIGHT_IS_SWITCHED_OFF = 7;

	// A heuristic value representing the distance from the sensors to the door
	// in mm
	public static final int DOOR_IS_CLOSED = 50;
	private static final int DOOR_IS_OPENED = 60;


	//private final AmbientLightApplication ambientLight;
	//private final DistanceApplication distance;
	//private final HumidityApplication humidity;
	//private final TemperatureApplication temperature;

	public BaroApplication() {

		//this.ambientLight = new AmbientLightApplication(this);
		//this.distance = new DistanceApplication(this);
		//this.humidity = new HumidityApplication(this);
		//this.temperature = new TemperatureApplication(this);
		//super.addTinkerforgeApplication(this.fridgeViewer);
		//super.addTinkerforgeApplication(this.ambientLight, this.distance,
		//		this.humidity, this.temperature);
        //
		//this.ambientLight.setAmbientHistereseMax(fridgeit.BaroApplication.LIGHT_IS_SWITCHED_ON);
		//this.ambientLight
		//		.setAmbientHistereseMin(fridgeit.BaroApplication.LIGHT_IS_SWITCHED_OFF);
        //
		//this.distance.setDistanceHistereseMin(fridgeit.BaroApplication.DOOR_IS_CLOSED);
		//this.distance.setDistanceHistereseMax(fridgeit.BaroApplication.DOOR_IS_OPENED);
	}

	public void setHumidity(final int humidityInPercent) {
		//this.fridgeViewer.setHumidity(humidityInPercent / 10.0);
	}

	public void setObjectIRTemperature(final int temperature) {
		//this.fridgeViewer.setObjectTemp(temperature);
	}

	public void setAmbientTemperature(final int temperature) {
		//this.fridgeViewer.setAmbientTemp(temperature);
	}

	public void setAmbientDarkState(final boolean latestAnswerIsItDark) {
		//this.fridgeViewer.setLightStatus(!latestAnswerIsItDark);
	}

	/**
	 * This class is responsible for receiving, processing and delegating data
	 * about the door-state.
	 * 
	 * @author reto
	 * 
	 */
	public void setDoorShutState(final boolean latestAnswerIsItClosed) {
		//fridgeit.BaroApplication.this.fridgeViewer.setDoorStatus(!latestAnswerIsItClosed);
	}

	@Override
	public void deviceDisconnected(
			final TinkerforgeStackAgent tinkerforgeStackAgent,
			final Device device) {
		// We do not care here...

	}

	@Override
	public void deviceConnected(
			final TinkerforgeStackAgent tinkerforgeStackAgent,
			final Device device) {
		// We do not care here...

	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equals(final Object obj) {
		return this==obj;
	}

}
