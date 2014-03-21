package gui;

import gui.view.AltitudeProfileView;
import ch.quantasy.tinkerforge.tinker.agent.implementation.TinkerforgeStackAgent;
import ch.quantasy.tinkerforge.tinker.application.implementation.AbstractTinkerforgeApplication;
import ch.quantasy.tinkerforge.tinker.core.implementation.TinkerforgeDevice;

import com.tinkerforge.BrickletBarometer;
import com.tinkerforge.BrickletBarometer.AltitudeListener;
import com.tinkerforge.Device;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

public class BarometerApplication2 extends AbstractTinkerforgeApplication implements AltitudeListener{
	private BrickletBarometer barometer;

	private final long updatePeriodeInMilliseconds = 1;
	

	@Override
	public void deviceConnected(
			final TinkerforgeStackAgent tinkerforgeStackAgent,
			final Device device) {
		if (TinkerforgeDevice.getDevice(device) == TinkerforgeDevice.Barometer) {
			if (this.barometer == null) {
				this.barometer = (BrickletBarometer) device;
				this.barometer
						.addAltitudeListener(this);
				// Turn averaging of in the Barometer Bricklet to make sure that
				// the data is without delay
				try {
					this.barometer
							.setAveraging((short) 0, (short) 0, (short) 0);
					this.barometer
							.setAltitudeCallbackPeriod(this.updatePeriodeInMilliseconds);
				} catch (final TimeoutException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (final NotConnectedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

	}

	@Override
	public void deviceDisconnected(
			final TinkerforgeStackAgent tinkerforgeStackAgent,
			final Device device) {
		if (TinkerforgeDevice.areEqual(this.barometer, device)) {
			this.barometer.removeAltitudeListener(this);
			try {
				this.barometer.setAltitudeCallbackPeriod(0);
			} catch (final TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (final NotConnectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.barometer = null;

		}
	}
	
	@Override
	public void altitude(int altitude) {
		//AltitudeProfileView.addBarometricAltitudeData(altitude);	
		//AltitudeProfileView.addEstimatedAltitudeData(altitude);
	}

	@Override
	public boolean equals(final Object obj) {
		return this == obj;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}

}
