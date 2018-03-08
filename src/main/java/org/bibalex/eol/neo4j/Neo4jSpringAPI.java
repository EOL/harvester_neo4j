package org.bibalex.eol.neo4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class Neo4jSpringAPI extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Neo4jSpringAPI.class);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Neo4jSpringAPI.class, args);
    }
}
