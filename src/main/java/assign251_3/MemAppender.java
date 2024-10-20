/**
 * A memory-based Log4j appender that stores log events up to a specified maximum size as seen below
 * Older log events are binned after we reach max size
 */



package assign251_3;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Plugin(name = "MemAppender", category = "Core", elementType = Appender.ELEMENT_TYPE, printObject = true)
public class MemAppender extends AbstractAppender {

    private final List<LogEvent> logEvents;
    private final int maxSize;
    private long discardedLogCount = 0;

    /**
     * Constructs a new MemAppender with the given parameters.
     *
     * @param name the name of the appender
     * @param filter the filter to apply
     * @param layout the layout for formatting log events
     * @param maxSize the maximum number of log events to store
     */
    protected MemAppender(String name, Filter filter, Layout<? extends Serializable> layout, int maxSize) {
        super(name, filter, layout, true);
        this.logEvents = new ArrayList<>();
        this.maxSize = maxSize;
    }

    @Override
    public void append(LogEvent event) {
        if (logEvents.size() >= maxSize) {
            discardedLogCount++;
            logEvents.remove(0); // Remove the oldest log event
        }
        logEvents.add(event.toImmutable());
        System.out.println("Captured log: " + event.getMessage().getFormattedMessage());
    }







    // Return current logs as an unmodifiable list
    public List<LogEvent> getCurrentLogs() {
        return Collections.unmodifiableList(logEvents);
    }


    public List<String> getEventStrings() {
        if (getLayout() == null) {
            return Collections.emptyList();
        }
        return logEvents.stream()
                .map(event -> getLayout().toSerializable(event).toString())
                .collect(Collectors.toList());
    }




    // Print logs to the console and clear the memory
    public void printLogs() {
        Layout<? extends Serializable> layout = getLayout();
        for (LogEvent event : logEvents) {
            System.out.println(layout.toSerializable(event).toString());
        }
        logEvents.clear();
    }

    // Get the number of discarded logs
    public long getDiscardedLogCount() {
        return discardedLogCount;
    }

    // Factory method to create an instance of MemAppender
    @PluginFactory
    public static MemAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginAttribute(value = "maxSize", defaultInt = 100) int maxSize,
            @PluginAttribute("layout") Layout<? extends Serializable> layout) {

        if (name == null) {
            LOGGER.error("No name provided for MemAppender");
            return null;
        }

        if (layout == null) {
            LOGGER.error("No layout provided for MemAppender");
            return null;
        }

        return new MemAppender(name, null, layout, maxSize);
    }
}
