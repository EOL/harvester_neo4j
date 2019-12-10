package org.bibalex.eol.neo4j.models;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
