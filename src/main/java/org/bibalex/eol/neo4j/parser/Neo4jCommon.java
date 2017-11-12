package org.bibalex.eol.neo4j.parser;

import org.neo4j.driver.v1.*;

import static org.neo4j.driver.v1.Values.parameters;

import java.util.logging.Logger;

public class Neo4jCommon {
    Session session;
    public static int autoId = 1;
    Logger logger =  Logger.getLogger("NEO4J_COMMON");

    public Session getSession (){
        if (session == null || !session.isOpen()) {
            Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "neo4j"));
            session = driver.session();
        }
        return session;
    }


    public boolean createRelationBetweenNodeAndSynonyms(int acceptedNodeGeneratedId, int synonymGeneratedNodeId)
    {
        logger.info("Creating synonym relation between acceptedNode" + acceptedNodeGeneratedId + "and synonym " + synonymGeneratedNodeId);
        String query = " MATCH(s:Synonym), (a:Node) WHERE s.generated_auto_id = {synonymGeneratedNodeId} AND " +
                "a.generated_auto_id = {acceptedNodeGeneratedId} CREATE (s)-[r:IS_SYNONYM_OF]->(a) RETURN r";
        StatementResult result = getSession().run(query, parameters("synonymGeneratedNodeId",
                synonymGeneratedNodeId, "acceptedNodeGeneratedId", acceptedNodeGeneratedId));
        if (result.hasNext())
        {
            logger.info("Node created");
            logger.info("Synonym Accepted relation created");
            return true;
        }
        return false;
    }

    public int getNodeIfExist(String nodeId, int resourceId)
    {
        logger.info("Searching for node with nodeId" + nodeId + "in resource with resourceId" + resourceId);
        String query = "MATCH (n) WHERE n.node_id = {nodeId} AND n.resource_id = {resourceId} " +
                " RETURN n.generated_auto_id";
        StatementResult result = getSession().run(query, parameters("nodeId",nodeId, "resourceId",  resourceId ));
        if (!result.hasNext()) {return -1;}

        Record record = result.next();
        return record.get("n.generated_auto_id").asInt();

    }

    public int getAcceptedNodeIfExist(String nodeId, String scientificName, int resourceId)
    {
        logger.info("Searching for accepted node with nodeId" + nodeId + "in resource with resourceId" + resourceId);
        String query = "MATCH (n:Node) WHERE n.node_id = {nodeId} AND n.resource_id = {resourceId} AND " +
                "n.scientific_name = {scientificName} RETURN n.generated_auto_id";
        StatementResult result = getSession().run(query,parameters("nodeId", nodeId, "resourceId",resourceId));
        if (!result.hasNext()) {return -1;}

        Record record = result.next();
        return record.get("n.generated_auto_id").asInt();

    }

    public int createAcceptedNode(int resourceId, String nodeId, String scientificName, String rank,
                                  int parentGeneratedNodeId)
    {
        String create_query = "CREATE (n:Node {resource_id: {resourceId}, node_id: {nodeId}," +
                " scientific_name: {scientificName}, rank: {rank}, generated_auto_id: {autoId}})" +
                "RETURN n.generated_auto_id";
        StatementResult result = getSession().run(create_query, parameters( "resourceId", resourceId,
                "nodeId", nodeId, "scientificName", scientificName, "rank", rank, "autoId", autoId ));
        if (parentGeneratedNodeId != 0) {createChildParentRelation(parentGeneratedNodeId, autoId);}
        autoId ++;
        Record record = result.next();
        return record.get("n.generated_auto_id").asInt();
    }

    public boolean createChildParentRelation(int parentGeneratedNodeId, int childGeneratedNodeId)
    {
        System.out.println("I am creating relations");
        String query = " MATCH(p:Node), (c:Node) WHERE p.generated_auto_id = {parentGeneratedNodeId} AND " +
                "c.generated_auto_id = {childGeneratedNodeId} CREATE (p)-[r:IS_PARENT_OF]->(c) RETURN r";
        StatementResult result = getSession().run(query, parameters("parentGeneratedNodeId",
                parentGeneratedNodeId, "childGeneratedNodeId", childGeneratedNodeId));
        if (result.hasNext())
        {
            logger.info("Node" + childGeneratedNodeId+"created");
            logger.info("Child Parent relation created");
            return true;
        }
        return false;
    }

    public int createSynonymNode(int resourceId, String nodeId, String scientificName, String rank,
                                 String acceptedNodeId, int acceptedNodeGeneratedId)
    {
        String create_query = "CREATE (s:Synonym {resource_id: {resourceId}, node_id: {nodeId}," +
                "scientific_name: {scientificName}, rank: {rank}, generated_auto_id: {autoId}})" +
                " RETURN s.generated_auto_id";
        StatementResult result = getSession().run(create_query, parameters( "resourceId", resourceId,
                "nodeId", nodeId, "scientificName", scientificName, "rank", rank, "autoId", autoId ) );
        Record record = result.next();
        boolean relation = createRelationBetweenNodeAndSynonyms(acceptedNodeGeneratedId,
                record.get("s.generated_auto_id").asInt());
        autoId++;
        if (relation) {logger.info("Synonym created");}
        return record.get("s.generated_auto_id").asInt();
    }

    public int getSynonymNodeIfExist(String nodeId, String scientificName, int resourceId, String acceptedNodeId, int
            acceptedGeneratedId){

        String query = "MATCH (a:Node {node_id: {nodeId}, resource_id: {resourceId} ," +
                " scientific_name: {scientificName}})<-[r:IS_SYNONYM_OF]-(s:Synonym) RETURN DISTINCT s.generated_auto_id";
        StatementResult result = getSession().run(query, parameters("nodeId", nodeId,"resourceId",resourceId,
                "scientificName",scientificName));
        Record record = result.next();
        return record.get("s.generated_auto_id").asInt();
    }
}
