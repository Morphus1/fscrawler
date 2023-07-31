package fr.pilato.elasticsearch.crawler.fs.settings;

import fr.pilato.elasticsearch.crawler.fs.test.framework.AbstractFSCrawlerTestCase;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FsSettingsTest extends AbstractFSCrawlerTestCase {

    @Test
    public void testCloudId() {
        final TestAppender appender = new TestAppender();
        final Logger logger = Logger.getRootLogger();
        logger.addAppender(appender);
        try {
            String cloudId = "foobar:dXMtZWFzdC0xLmF3cy5mb3VuZC5pbyRmb29iYXJlbGFzdGljc2VhcmNoJGZvb2JhcmtpYmFuYQ==";
            String httpHost = ServerUrl.decodeCloudId(cloudId);

            assertThat(httpHost, is("https://foobarelasticsearch.us-east-1.aws.found.io"));

            // Log the message "THIS IS WORKING!!!!!!!!!!!!!!!!!!!" using the logger instance
            logger.info("THIS IS WORKING!!!!!!!!!!!!!!!!!!!");

        } finally {
            logger.removeAppender(appender);
        }

        final List<LoggingEvent> log = appender.getLog();
        final LoggingEvent LogEntry = log.get(0);
        assertThat(LogEntry.getLevel(), is(Level.OFF));
    }
}

class TestAppender extends AppenderSkeleton {
    private final List<LoggingEvent> log = new ArrayList<LoggingEvent>();

    @Override
    public boolean requiresLayout() {
        return false;
    }

    @Override
    protected void append(final LoggingEvent loggingEvent) {
        log.add(loggingEvent);
    }

    @Override
    public void close() {
    }

    public List<LoggingEvent> getLog() {
        return new ArrayList<>(log);
    }
}
