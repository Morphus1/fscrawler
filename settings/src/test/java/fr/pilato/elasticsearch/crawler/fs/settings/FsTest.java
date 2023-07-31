package fr.pilato.elasticsearch.crawler.fs.settings;

import org.junit.Test;

import fr.pilato.elasticsearch.crawler.fs.test.framework.AbstractFSCrawlerTestCase;

import static org.junit.Assert.assertTrue;

public class FsTest extends AbstractFSCrawlerTestCase {

    @Test
    public void testVectorizeTrue() {
        // Arrange
        boolean expectedVectorizeValue = true;

        // Act
        Fs fs = Fs.builder().isVectorize(expectedVectorizeValue).build();

        // Assert
        assertTrue(fs.isVectorize() == expectedVectorizeValue);

        // Log
        logger.error("Vectorize is set to true");
    }

    @Test
    public void testVectorizeFalse() {
        // Arrange
        boolean expectedVectorizeValue = false;

        // Act
        Fs fs = Fs.builder().isVectorize(expectedVectorizeValue).build();

        // Assert
        assertTrue(fs.isVectorize() == expectedVectorizeValue);

        // Log
        logger.error("Vectorize is set to false");
    }
}
