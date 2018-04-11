package org.bibalex.eol.neo4j.parser;

import org.bibalex.eol.neo4j.models.Node;
import org.neo4j.driver.v1.*;

import static org.neo4j.driver.v1.Values.parameters;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;


public class Neo4jCommon {
    Session session;
    int autoId = 0;
    Logger logger = LoggerFactory.getLogger(Neo4jCommon.class);

    public int getAutoId() {
        String query = "MATCH (n) RETURN n.generated_auto_id ORDER BY n.generated_auto_id DESC LIMIT 1";
        StatementResult result = getSession().run(query);

        if (result.hasNext()) {
            logger.debug("AutoId found ");
            Record record = result.next();
            autoId = record.get("n.generated_auto_id").asInt() + 1;

        } else {
            logger.debug("Autoid IS 1");
            autoId = 1;
        }

        return autoId;

    }

    public Session getSession() {
        if (session == null || !session.isOpen()) {
            Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "eol"));
            session = driver.session();

        }
        return session;
    }

    public int createAcceptedNode(int resourceId, String nodeId, String scientificName, String rank,
                                  int parentGeneratedNodeId) {
        int nodeGeneratedNodeId = getAcceptedNodeIfExist( nodeId, scientificName, resourceId);
        if (nodeGeneratedNodeId == -1)
        {
            logger.debug("Node "+ scientificName +" not found creating a new node");
            autoId = getAutoId();
            String create_query = "CREATE (n:Node {resource_id: {resourceId}, node_id: {nodeId}," +
                    " scientific_name: {scientificName}, rank: {rank}, generated_auto_id: {autoId}, created_at: apoc.date.currentTimestamp()," +
                    "updated_at: apoc.date.currentTimestamp()}) RETURN n.generated_auto_id";
            StatementResult result = getSession().run(create_query, parameters("resourceId", resourceId,
                    "nodeId", nodeId, "scientificName", scientificName, "rank", rank, "autoId", autoId));
            Record record = result.next();
            if (parentGeneratedNodeId != 0) {
                logger.debug("Parent avaliable with id " + parentGeneratedNodeId);
                createChildParentRelation(parentGeneratedNodeId, record.get("n.generated_auto_id").asInt() );
            }
            autoId++;
            if (result.hasNext())
            {
                logger.debug("Node  " + scientificName + " created ");
                return record.get("n.generated_auto_id").asInt();
            } else {
                logger.debug("Node  " + scientificName + " is not created a problem has occurred");
                return -1;
            }
        }
        else
        {
            logger.debug("Node "+ scientificName +"  found");
            return nodeGeneratedNodeId;
        }
    }

    public void createChildParentRelation(int parentGeneratedNodeId, int childGeneratedNodeId) {
        boolean node_exists = checkIfNodeExists(childGeneratedNodeId);
        boolean parent_exists = checkIfNodeExists(parentGeneratedNodeId);
        if(node_exists && parent_exists) {
            String query = " MATCH(p:Node), (c:Node) WHERE p.generated_auto_id = {parentGeneratedNodeId} AND " +
                    "c.generated_auto_id = {childGeneratedNodeId} CREATE (p)-[r:IS_PARENT_OF]->(c) RETURN r";
            StatementResult result = getSession().run(query, parameters("parentGeneratedNodeId",
                    parentGeneratedNodeId, "childGeneratedNodeId", childGeneratedNodeId));
            if (result.hasNext()) {
                logger.debug("Node" + childGeneratedNodeId + "created");
                logger.debug("Child Parent relation created with parentgeneratedNodeId " + parentGeneratedNodeId);

            }
        }else
            logger.debug("Child Parent relation not created for child " + childGeneratedNodeId + "and parent " + parentGeneratedNodeId);
    }

    public int createSynonymNode(int resourceId, String nodeId, String scientificName, String rank,
                                 String acceptedNodeId, int acceptedNodeGeneratedId) {

        int nodeGeneratedNodeId = getSynonymNodeIfExist(nodeId, scientificName, resourceId, acceptedNodeId, acceptedNodeGeneratedId);
        if (nodeGeneratedNodeId == -1)
        {
            logger.debug("Node "+ scientificName +" not found creating a new node");
            autoId = getAutoId();
            String create_query = "CREATE (s:Synonym {resource_id: {resourceId}, node_id: {nodeId}," +
                    "scientific_name: {scientificName}, rank: {rank}, generated_auto_id: {autoId}," +
                    " created_at: apoc.date.currentTimestamp(), updated_at: apoc.date.currentTimestamp()}) RETURN s.generated_auto_id";
            StatementResult result = getSession().run(create_query, parameters("resourceId", resourceId,
                    "nodeId", nodeId, "scientificName", scientificName, "rank", rank, "autoId", autoId));
            Record record = result.next();
             createRelationBetweenNodeAndSynonyms(acceptedNodeGeneratedId,
                    record.get("s.generated_auto_id").asInt());
            autoId++;
            if (result.hasNext())
            {
                logger.debug("Node  " + scientificName + " created ");
                return record.get("s.generated_auto_id").asInt();
            } else {
                logger.debug("Node  " + scientificName + " is not created a problem has occurred");
                return -1;
            }

        }
        else
        {
            logger.debug("Node "+ scientificName +"  found");
            return nodeGeneratedNodeId;

        }
    }

    public void createRelationBetweenNodeAndSynonyms(int acceptedNodeGeneratedId, int synonymGeneratedNodeId) {
        boolean synonym_exists = checkIfNodeExists(synonymGeneratedNodeId);
        boolean node_exists = checkIfNodeExists(acceptedNodeGeneratedId);
        if (synonym_exists && node_exists) {
            logger.debug("Creating synonym relation between acceptedNode" + acceptedNodeGeneratedId + "and synonym " + synonymGeneratedNodeId);
            String query = " MATCH(s:Synonym), (a:Node) WHERE s.generated_auto_id = {synonymGeneratedNodeId} AND " +
                    "a.generated_auto_id = {acceptedNodeGeneratedId} CREATE (s)-[r:IS_SYNONYM_OF]->(a) RETURN r";
            StatementResult result = getSession().run(query, parameters("synonymGeneratedNodeId",
                    synonymGeneratedNodeId, "acceptedNodeGeneratedId", acceptedNodeGeneratedId));
            if (result.hasNext()) {
                logger.debug("Node created");
                logger.debug("Synonym Accepted relation created");

            }
        }
        else
         logger.debug("Synonym Accepted relation is not created");
    }

    public boolean checkIfNodeExists(int generatedNodeId)
    {
        String query = "MATCH(n {generated_auto_id: {generatedNodeId}}) RETURN n.generated_auto_id";
        StatementResult result = getSession().run(query, parameters("generatedNodeId", generatedNodeId));
        if (result.hasNext())
        {
            logger.debug("Node exists");
            return true;
        }
        else
        {
            logger.debug("Node doesn't exist");
            return false;
        }

    }

    public int getNodeIfExist(String nodeId, int resourceId) {
        logger.debug("Searching for node with nodeId" + nodeId + "in resource with resourceId" + resourceId);
        String query = "MATCH (n) WHERE n.node_id = {nodeId} AND n.resource_id = {resourceId} " +
                " RETURN n.generated_auto_id";
        StatementResult result = getSession().run(query, parameters("nodeId", nodeId, "resourceId", resourceId));
        if (result.hasNext()) {
            Record record = result.next();
            logger.debug("THe result of search" + record.get("n.generated_auto_id").asInt());
            return record.get("n.generated_auto_id").asInt();
        } else {
            logger.debug("result is -1");
            return -1;
        }

    }

    public int getAcceptedNodeIfExist(String nodeId, String scientificName, int resourceId) {

        logger.debug("Searching for accepted node with nodeId" + nodeId + "in resource with resourceId " + resourceId + scientificName);
        String query = "MATCH (n:Node) WHERE n.node_id = {nodeId} AND n.resource_id = {resourceId} AND " +
                "n.scientific_name = {scientificName} RETURN n.generated_auto_id";
        StatementResult result = getSession().run(query, parameters("nodeId", nodeId, "resourceId", resourceId,
                "scientificName", scientificName));

        if (result.hasNext()) {
            Record record = result.next();
            logger.debug("THe result of search" + record.get("n.generated_auto_id").asInt());
            return record.get("n.generated_auto_id").asInt();
        } else {
            logger.debug("result is -1");
            return -1;
        }

    }

    public int getSynonymNodeIfExist(String nodeId, String scientificName, int resourceId, String acceptedNodeId, int
            acceptedGeneratedId) {

        String query = "MATCH (a:Node {node_id: {nodeId}, resource_id: {resourceId} ," +
                " scientific_name: {scientificName}})<-[r:IS_SYNONYM_OF]-(s:Synonym) RETURN DISTINCT s.generated_auto_id";
        StatementResult result = getSession().run(query, parameters("nodeId", nodeId, "resourceId", resourceId,
                "scientificName", scientificName));
        if (result.hasNext()) {
            Record record = result.next();
            logger.debug("The result of search Synonym" + record.get("s.generated_auto_id").asInt());
            return record.get("s.generated_auto_id").asInt();
        } else {
            logger.debug("result is -1");
            return -1;
        }

    }


    public boolean hasSiblings(int generatedNodeId) {
        logger.debug("Checking if node with generated_auto_id " + generatedNodeId + " has siblings");
        String query = "MATCH (n:Node {generated_auto_id: {generatedNodeId}})<-[:IS_PARENT_OF]-(r:Node)-[:IS_PARENT_OF]->(s:Node)" +
                " RETURN s";
        StatementResult result = getSession().run(query, parameters("generatedNodeId", generatedNodeId));

        if (result.hasNext()) {
            logger.debug("Yes node has siblings");
            return true;
        }

        else
          return false;
    }

    public boolean hasChildren(int generatedNodeId) {
        logger.debug("Checking if node with generated_auto_id " + generatedNodeId + " has children");
        String query = "MATCH (n:Node {generated_auto_id: {generatedNodeId}})-[:IS_PARENT_OF]->(c:Node) RETURN c";
        StatementResult result = getSession().run(query, parameters("generatedNodeId", generatedNodeId));
        if (result.hasNext()) {
            logger.debug("Yes node has children");
            return true;
        }

        else
            return false;

    }

    public void CommonDeleteMethod(int generatedNodeId)
    {
        logger.debug("Deleting node it has no children ");
        if (hasSiblings(generatedNodeId))
        {
            logger.debug("Node has  siblings");
            deleteNode(generatedNodeId);
        }
        else
        {
            int parentGeneratedNodeId = getParentAndDelete(generatedNodeId);
            logger.debug("Node has no siblings");
            if(parentGeneratedNodeId != -1 && !parenthasNodeId(parentGeneratedNodeId))
            {
                logger.debug("Parent is placeholder node");
                CommonDeleteMethod(parentGeneratedNodeId);
            }
        }
    }

    public int deleteNode(int generatedNodeId)
    {
        logger.debug("Delete node with autoId "+ generatedNodeId);
        String query = "MATCH (n {generated_auto_id: {generatedNodeId}}) DETACH DELETE n";
        StatementResult result = getSession().run(query, parameters("generatedNodeId", generatedNodeId));
        return generatedNodeId;
    }

    public boolean parenthasNodeId(int generatedNodeId) {
        logger.debug("Check the nodeId of the node with autoId " + generatedNodeId);
        String query = "MATCH (n {generated_auto_id: {generatedNodeId}}) RETURN n.node_id";
        StatementResult result = getSession().run(query, parameters("generatedNodeId", generatedNodeId));
        Record record = result.next();
        String nodeId = record.get("n.node_id").toString().replace("\"", "");
        if (nodeId.equals("pc")) {
            logger.debug("This node has nodeId as placeholder");
            return false;
        }
        else {
            logger.debug("This node has nodeId "+ record.toString());
            return true;
        }

    }

    public void UpdateScientificName(int generatedNodeId, String ScientificName)
    {
        logger.debug("Update Scientific Name of  node with generatedNodeId " + generatedNodeId);
        String query = "MATCH(n {generated_auto_id : {generatedNodeId}}) SET n.scientific_name = {ScientificName}";
         getSession().run(query, parameters("generatedNodeId", generatedNodeId,
                "ScientificName", ScientificName));

    }

    public void UpdateRank(int generatedNodeId, String rank)
    {
        logger.debug("Update rank of  node with generatedNodeId " + generatedNodeId);
        String query = "MATCH (n {generated_auto_id: {generatedNodeId}}) SET n.rank = {rank}";
        getSession().run(query, parameters("generatedNodeId", generatedNodeId,
                "rank", rank));

    }

    public void MarkNodePlaceHolder(int generatedNodeId)
    {
        String query = "MATCH(n {generated_auto_id : {generatedNodeId}}) SET n.node_id = 'pc'";
        getSession().run(query, parameters("generatedNodeId", generatedNodeId));

    }

    public void deleteParentRelation(int generatedNodeId )
    {
        logger.debug("Delete is parent relations between node with autoId " + generatedNodeId );
        String query ="MATCH(n{generated_auto_id : {generatedNodeId}})-[rel:IS_PARENT_OF]-(p) DELETE rel" ;
        StatementResult result = getSession().run(query, parameters("generatedNodeId", generatedNodeId));

    }


    public int getParentAndDelete(int generatedNodeId)
    {
        int parentGeneratedNodeId;
        logger.debug("Delete the node with generated auto id  " + generatedNodeId + " and gets its parent");
        String query = "MATCH(a:Node {generated_auto_id : {generatedNodeId}})<-[:IS_PARENT_OF]-(n:Node) RETURN n.generated_auto_id";
        StatementResult result = getSession().run(query, parameters("generatedNodeId", generatedNodeId));
        if(result.hasNext())
        {
            Record record  = result.next();
            parentGeneratedNodeId = record.get("n.generated_auto_id").asInt();
            logger.debug("The parent of the node is node with auto Id "+ parentGeneratedNodeId);

        }
        else
        {
            parentGeneratedNodeId = -1;
            logger.debug("has no parent nodes so returned -1");
        }
        deleteParentRelation(generatedNodeId);
        deleteNode(generatedNodeId);
        return parentGeneratedNodeId;

    }

    // Taxon Matching requests
    public void addPageIdtoNode(int generatedNodeId , int pageId)
    {
        logger.debug("Add pageId from Taxon Matching Algorithm to Node with autoId "+ generatedNodeId);
        String query = "MATCH (n:Node {generated_auto_id: {generatedNodeId}}) SET n.page_id = {pageId}";
        getSession().run(query, parameters("generatedNodeId", generatedNodeId, "pageId", pageId));
    }


}
