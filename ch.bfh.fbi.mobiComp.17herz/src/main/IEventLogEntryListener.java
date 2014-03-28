package main;

/**
 * Created by roger.jaggi on 28.03.2014.
 */
public interface IEventLogEntryListener {
    public void logEventHappened(Object source, EventLogEntry entry);
}
