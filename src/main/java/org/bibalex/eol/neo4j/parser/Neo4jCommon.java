package org.bibalex.eol.neo4j.parser;

import org.neo4j.driver.v1.*;

import static org.neo4j.driver.v1.Values.parameters;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class Neo4jCommon {
    Session session;
    int autoId = 0;
    Logger logger =  LoggerFactory.getLogger(Neo4jCommon.class);

    public int getAutoId()
    {
        String query = "MATCH (n) RETURN n.generated_auto_id ORDER BY n.generated_auto_id DESC LIMIT 1";
        StatementResult result = getSession().run(query);

        if (result.hasNext())
        {
            logger.debug("AutoId found ");
            Record record = result.next();
            autoId = record.get("n.generated_auto_id").asInt() + 1;

        }
        else
        {
            logger.debug("Autoid IS 1");
            autoId =  1;
        }

        return autoId;

    }

    public Session getSession (){
        if (session == null || !session.isOpen()) {
            Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "eol"));
            session = driver.session();

        }
        return session;
    }


    public boolean createRelationBetweenNodeAndSynonyms(int acceptedNodeGeneratedId, int synonymGeneratedNodeId)
    {
        logger.debug("Creating synonym relation between acceptedNode" + acceptedNodeGeneratedId + "and synonym " + synonymGeneratedNodeId);
        String query = " MATCH(s:Synonym), (a:Node) WHERE s.generated_auto_id = {synonymGeneratedNodeId} AND " +
                "a.generated_auto_id = {acceptedNodeGeneratedId} CREATE (s)-[r:IS_SYNONYM_OF]->(a) RETURN r";
        StatementResult result = getSession().run(query, parameters("synonymGeneratedNodeId",
                synonymGeneratedNodeId, "acceptedNodeGeneratedId", acceptedNodeGeneratedId));
        if (result.hasNext())
        {
            logger.debug("Node created");
            logger.debug("Synonym Accepted relation created");
            return true;
        }
        return false;
    }

    public int getNodeIfExist(String nodeId, int resourceId)
    {
        logger.debug("Searching for node with nodeId" + nodeId + "in resource with resourceId" + resourceId);
        String query = "MATCH (n) WHERE n.node_id = {nodeId} AND n.resource_id = {resourceId} " +
                " RETURN n.generated_auto_id";
        StatementResult result = getSession().run(query, parameters("nodeId",nodeId, "resourceId",  resourceId ));
        if (result.hasNext())
        {
            Record record = result.next();
            logger.debug("THe result of search" +record.get("n.generated_auto_id").asInt() );
            return record.get("n.generated_auto_id").asInt();
        }

        else
            {
            logger.debug("result is -1");
            return -1;
        }

    }

    public int getAcceptedNodeIfExist(String nodeId, String scientificName, int resourceId)
    {
        logger.debug("Searching for accepted node with nodeId" + nodeId + "in resource with resourceId" + resourceId);
        String query = "MATCH (n:Node) WHERE n.node_id = {nodeId} AND n.resource_id = {resourceId} AND " +
                "n.scientific_name = {scientificName} RETURN n.generated_auto_id";
        StatementResult result = getSession().run(query,parameters("nodeId", nodeId,  "resourceId", resourceId,
                "scientificName",scientificName));

        if (result.hasNext())
        {
            Record record = result.next();
            logger.debug("THe result of search" +record.get("n.generated_auto_id").asInt() );
            return record.get("n.generated_auto_id").asInt();
        }

        else
        {
            logger.debug("result is -1");
            return -1;
        }

    }

    public int createAcceptedNode(int resourceId, String nodeId, String scientificName, String rank,
                                  int parentGeneratedNodeId)
    {
        autoId = getAutoId();
        String create_query = "CREATE (n:Node {resource_id: {resourceId}, node_id: {nodeId}," +
                " scientific_name: {scientificName}, rank: {rank}, generated_auto_id: {autoId}, created_at: apoc.date.currentTimestamp()," +
                "updated_at: apoc.date.currentTimestamp()}) RETURN n.generated_auto_id";
        StatementResult result = getSession().run(create_query, parameters( "resourceId", resourceId,
                "nodeId", nodeId, "scientificName", scientificName, "rank", rank, "autoId", autoId ));
        if (parentGeneratedNodeId != 0) {
            logger.debug("Parent avaliable with id " + parentGeneratedNodeId);
            createChildParentRelation(parentGeneratedNodeId, autoId);
        }
        autoId ++;
        Record record = result.next();
        return record.get("n.generated_auto_id").asInt();
    }

    public void createChildParentRelation(int parentGeneratedNodeId, int childGeneratedNodeId)
    {

        String query = " MATCH(p:Node), (c:Node) WHERE p.generated_auto_id = {parentGeneratedNodeId} AND " +
                "c.generated_auto_id = {childGeneratedNodeId} CREATE (p)-[r:IS_PARENT_OF]->(c) RETURN r";
        StatementResult result = getSession().run(query, parameters("parentGeneratedNodeId",
                parentGeneratedNodeId, "childGeneratedNodeId", childGeneratedNodeId));
        if (result.hasNext())
        {
            logger.debug("Node" + childGeneratedNodeId+"created");
            logger.debug("Child Parent relation created with parentgeneratedNodeId " + parentGeneratedNodeId);

        }
        else
         logger.debug("Child Parent relation not created for child "+ childGeneratedNodeId + "and parent "+ parentGeneratedNodeId);
    }

    public int createSynonymNode(int resourceId, String nodeId, String scientificName, String rank,
                                 String acceptedNodeId, int acceptedNodeGeneratedId)
    {
        autoId = getAutoId();
        String create_query = "CREATE (s:Synonym {resource_id: {resourceId}, node_id: {nodeId}," +
                "scientific_name: {scientificName}, rank: {rank}, generated_auto_id: {autoId}," +
                " created_at: apoc.date.currentTimestamp(), updated_at: apoc.date.currentTimestamp()}) RETURN s.generated_auto_id";
        StatementResult result = getSession().run(create_query, parameters( "resourceId", resourceId,
                "nodeId", nodeId, "scientificName", scientificName, "rank", rank, "autoId", autoId ) );
        Record record = result.next();
        boolean relation = createRelationBetweenNodeAndSynonyms(acceptedNodeGeneratedId,
                record.get("s.generated_auto_id").asInt());
        autoId++;
        if (relation) {logger.debug("Synonym created");}
        return record.get("s.generated_auto_id").asInt();
    }

    public int getSynonymNodeIfExist(String nodeId, String scientificName, int resourceId, String acceptedNodeId, int
            acceptedGeneratedId){

        String query = "MATCH (a:Node {node_id: {nodeId}, resource_id: {resourceId} ," +
                " scientific_name: {scientificName}})<-[r:IS_SYNONYM_OF]-(s:Synonym) RETURN DISTINCT s.generated_auto_id";
        StatementResult result = getSession().run(query, parameters("nodeId", nodeId,"resourceId",resourceId,
                "scientificName",scientificName));
        if (result.hasNext())
        {
            Record record = result.next();
            logger.debug("THe result of search Synoym" +record.get("s.generated_auto_id").asInt() );
            return record.get("s.generated_auto_id").asInt();
        }

        else
        {
            logger.debug("result is -1");
            return -1;
        }

    }

    public void addPageIdtoNode(int generatedNodeId , int pageId)
    {
        logger.debug("Add pageId from Taxon Matching Algorithm to Node with autoId "+ generatedNodeId);
        String query = "MATCH (n:Node {generated_auto_id: {generatedNodeId}}) SET n.page_id = {pageId}";
        getSession().run(query, parameters("generatedNodeId", generatedNodeId, "pageId", pageId));
    }
}
