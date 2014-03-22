package main;

import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;
import sensor.BarometerApplication;

import java.util.Date;

/**
* Created by roger.jaggi on 22.03.2014.
*/
class AmplitudeEvent {

    private Date timestamp;
    private Integer airPressure;
    private String barometerId;
    private Integer threshold;
    private Integer thresholdMin;
    private Integer thresholdMax;
    private Boolean isUp;

    AmplitudeEvent(BarometerApplication barometer, Integer airPressure) {
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

        this.isUp = this.threshold < airPressure;
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
        The17HerzApplication.logInfo((isUp ? "Close" : "Open") + " detected! Reason: " + reason + ". " + this);
    }

    @Override
    public String toString() {
        return "AmplitudeEvent{" +
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
