package fr.pilato.elasticsearch.crawler.fs.settings;

import org.junit.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.junit.Assert.assertTrue;

public class FsTest {
    private static final Logger logger = LogManager.getLogger(FsTest.class);

    @Test
    public void testVectorizeTrue() {
        // Arrange
        boolean expectedVectorizeValue = true;

        // Act
        Fs fs = Fs.builder().setVectorize(expectedVectorizeValue).build();

        // Assert
        assertTrue(fs.setVectorize() == expectedVectorizeValue);

        // Log
        logger.info("Vectorize is set to true");
    }

    @Test
    public void testVectorizeFalse() {
        // Arrange
        boolean expectedVectorizeValue = false;

        // Act
        Fs fs = Fs.builder().setVectorize(expectedVectorizeValue).build();

        // Assert
        assertTrue(fs.setVectorize() == expectedVectorizeValue);

        // Log
        logger.info("Vectorize is set to false");
    }
}
