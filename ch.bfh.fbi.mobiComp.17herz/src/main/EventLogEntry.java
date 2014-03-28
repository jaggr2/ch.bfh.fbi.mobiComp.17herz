package main;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

/**
* Created by roger.jaggi on 28.03.2014.
*/
public class EventLogEntry {

    private final SimpleLongProperty timestamp;
    private final SimpleStringProperty description;

    public EventLogEntry(Long timestamp, String description) {
        this.timestamp = new SimpleLongProperty(timestamp);
        this.description = new SimpleStringProperty(description);
    }

    public long getTimestamp() {
        return timestamp.get();
    }

    public void setTimestamp(long timestamp) {
        this.timestamp.set(timestamp);
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }
}
