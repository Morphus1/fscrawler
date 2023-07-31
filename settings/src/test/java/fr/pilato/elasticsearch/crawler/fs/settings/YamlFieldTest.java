package fr.pilato.elasticsearch.crawler.fs.settings;

import fr.pilato.elasticsearch.crawler.fs.test.framework.AbstractFSCrawlerTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

public class YamlFieldTest extends AbstractFSCrawlerTestCase {

    @Test
    public void testVectorizeTrueInYaml() {
        // Arrange
        String yamlConfig = "name: \"test\"\nfs:\n  url: \"/path/to/data\"\n  vectorize: true";
        boolean expectedVectorizeValue = true;

        // Act
        FsSettings settings = null;
        try {
            settings = FsSettingsParser.fromYaml(yamlConfig);
        } catch (IOException e) {
            fail("Parsing YAML failed: " + e.getMessage());
        }

        // Assert
        assertEquals(expectedVectorizeValue, settings.getFs().isVectorize());

        // Log
        logger.info("Vectorize is set to true in YAML config");
    }

    @Test
    public void testVectorizeFalseInYaml() {
        // Arrange
        String yamlConfig = "name: \"test\"\nfs:\n  url: \"/path/to/data\"\n  vectorize: false";
        boolean expectedVectorizeValue = false;

        // Act
        FsSettings settings = null;
        try {
            settings = FsSettingsParser.fromYaml(yamlConfig);
        } catch (IOException e) {
            fail("Parsing YAML failed: " + e.getMessage());
        }

        // Assert
        assertEquals(expectedVectorizeValue, settings.getFs().isVectorize());

        // Log
        logger.info("Vectorize is set to false in YAML config");
    }
}
