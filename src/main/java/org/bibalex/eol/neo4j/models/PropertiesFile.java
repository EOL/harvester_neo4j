package org.bibalex.eol.neo4j.models;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/**
 * Created by maha.mostafa on 6/2/2018.
 */

@Component
@ConfigurationProperties
public class PropertiesFile {

    public String getResourcesDirectory() {
        return resourcesDirectory;
    }

    public void setResourcesDirectory(String resourcesDirectory) {
        this.resourcesDirectory = resourcesDirectory;
    }

    private String resourcesDirectory;
    private String neo4jDirectory;

    public String getNeo4jDirectory() {
        return neo4jDirectory;
    }

    public void setNeo4jDirectory(String neo4jDirectory) {
        this.neo4jDirectory = neo4jDirectory;
    }
}
