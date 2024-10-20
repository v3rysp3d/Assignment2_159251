

/**
 * Tests for the MemAppender class.
 * Tests the functionality of capturing, formatting, and managing log events in memory.
 */


package assign251_3;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MemAppenderTest {

    private MemAppender memAppender;
    private Logger logger;



    /**
    * Setups for enviro for testing
    *
    *
    *
     */

    @BeforeEach
    public void setUp() {
        // Create a layout for formatting log events
        PatternLayout layout = PatternLayout.newBuilder().withPattern("[%p] %c: %m%n").build();

        // Create and start the memory appender with maxSize of 3
        memAppender = MemAppender.createAppender("MemAppender", 3, layout);
        memAppender.start();

        // Get the LoggerContext and configure it
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();

        // Clear existing appenders
        config.getRootLogger().removeAppender(memAppender.getName());

        // Add the MemAppender to the root logger
        config.getRootLogger().addAppender(memAppender, null, null);
        config.getRootLogger().setLevel(org.apache.logging.log4j.Level.INFO);
        ctx.updateLoggers();

        // Initialize the logger
        logger = LogManager.getLogger(MemAppenderTest.class);
    }


    /**
     * TESTS BELOW
     * Tests capturing, formatting, and managing log events in memory
     *
     */

    @Test
    public void testLogCapture() {
        // Log three messages
        logger.info("First message");
        logger.info("Second message");
        logger.info("Third message");

        // Get captured log events
        List<LogEvent> events = memAppender.getCurrentLogs();


        // Check if three events are captured
        assertEquals(3, events.size(), "Expected 3 log events to be captured");

        // Verify the captured messages
        if (!events.isEmpty()) {
            assertEquals("First message", events.get(0).getMessage().getFormattedMessage(), "First message mismatch");
            assertEquals("Second message", events.get(1).getMessage().getFormattedMessage(), "Second message mismatch");
            assertEquals("Third message", events.get(2).getMessage().getFormattedMessage(), "Third message mismatch");
        }
    }



    @Test
    public void testDiscardingOldLogs() {
        // Log more than maxSize (3 messages)
        logger.info("Message 1");
        logger.info("Message 2");
        logger.info("Message 3");
        logger.info("Message 4");

        // Get captured log events
        List<LogEvent> events = memAppender.getCurrentLogs();
        assertEquals(3, events.size(), "Expected 3 log events to be captured after discarding");

        // Verify that the oldest message was discarded
        assertEquals("Message 2", events.get(0).getMessage().getFormattedMessage(), "Expected 'Message 2' as the first log");
        assertEquals("Message 3", events.get(1).getMessage().getFormattedMessage(), "Expected 'Message 3' as the second log");
        assertEquals("Message 4", events.get(2).getMessage().getFormattedMessage(), "Expected 'Message 4' as the third log");

        // Verify the discarded log count
        assertEquals(1, memAppender.getDiscardedLogCount(), "Expected 1 discarded log");
    }

    @Test
    public void testPrintLogsAndClear() {
        // Log two messages
        logger.info("Print Test 1");
        logger.info("Print Test 2");

        // Print logs to console and clear
        memAppender.printLogs();

        // Verify that the logs are cleared after printing
        List<LogEvent> events = memAppender.getCurrentLogs();
        assertTrue(events.isEmpty(), "Expected no log events after print and clear");
    }

    @Test
    public void testGetEventStrings() {
        // Log two messages
        logger.info("String Test 1");
        logger.info("String Test 2");

        // Get event strings
        List<String> eventStrings = memAppender.getEventStrings();

        // Verify that the strings are correctly formatted
        assertEquals(2, eventStrings.size(), "Expected 2 log strings");
        assertTrue(eventStrings.get(0).contains("String Test 1"), "Expected 'String Test 1' in the first log string");
        assertTrue(eventStrings.get(1).contains("String Test 2"), "Expected 'String Test 2' in the second log string");
    }
}
