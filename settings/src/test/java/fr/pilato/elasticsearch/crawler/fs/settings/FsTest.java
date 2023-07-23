package fr.pilato.elasticsearch.crawler.fs.settings;

import fr.pilato.elasticsearch.crawler.fs.test.framework.AbstractFSCrawlerTestCase;
import org.junit.Test;

import static org.junit.Assert.*;

public class FsTest extends AbstractFSCrawlerTestCase {

    @Test
    public void testDefaultVectorize() {
        Fs fs = new Fs();
        assertFalse(fs.setVectorize()); // Default value should be false
    }

    @Test
    public void testSetAndGetVectorize() {
        Fs fs = new Fs();

        // Test setting vectorize to true
        fs.setVectorize();
        assertTrue(fs.setVectorize());

        // Test setting vectorize to false
        fs.setVectorize();
        assertFalse(fs.setVectorize());
    }
}
