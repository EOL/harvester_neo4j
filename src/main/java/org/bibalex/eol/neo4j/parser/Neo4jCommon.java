package org.bibalex.eol.neo4j.parser;

import org.bibalex.eol.neo4j.models.Node;
import org.neo4j.driver.v1.*;

import static org.neo4j.driver.v1.Values.NULL;
import static org.neo4j.driver.v1.Values.parameters;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class Neo4jCommon {
    Session session;
    int autoId = 0;
    int pageId = 0;
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

    public int getPageId() {
        String query = "MATCH (n:" + Constants.HAS_PAGE_LABEL + ") RETURN n.page_id ORDER BY n.page_id DESC LIMIT 1";
        StatementResult result = getSession().run(query);

        if (result.hasNext()) {
            Record record = result.next();
            pageId = record.get("n.page_id").asInt();
            logger.debug("Page id " + pageId + " is retrieved.");
            pageId++;
        } else {
            logger.debug("First page id.");
            pageId = 1;
        }
        return pageId;
    }

    public Session getSession() {
        if (session == null || !session.isOpen()) {
//            Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "root"));
            Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "eol"));
            session = driver.session();
        }
        return session;
    }

    public int createAcceptedNode(int resourceId, String nodeId, String scientificName, String rank,
                                  int parentGeneratedNodeId, int pageId) {

        int nodeGeneratedNodeId = getAcceptedNodeIfExist( nodeId, scientificName, resourceId);
        if (nodeGeneratedNodeId == -1)
        {
            logger.debug("Node "+ scientificName +" not found creating a new node");
            autoId = getAutoId();
            String hasPage = (pageId > 0)? (":" + Constants.HAS_PAGE_LABEL) : "";
            String create_query = "CREATE (n:Node" + hasPage + " {resource_id: {resourceId}, node_id: {nodeId}," +
                    " scientific_name: {scientificName}, rank: {rank}, generated_auto_id: {autoId}, " +
                    ((pageId > 0)? ("page_id:{pageId},") : "") + "created_at: timestamp()," +
                    "updated_at: timestamp()}) RETURN n.generated_auto_id";

            logger.debug("Create Node query:" + create_query);
            Value values;
            values = (pageId > 0)? parameters("resourceId", resourceId,
                        "nodeId", nodeId, "scientificName", scientificName, "rank", rank, "autoId", autoId, "pageId", pageId) :  parameters("resourceId", resourceId,
                    "nodeId", nodeId, "scientificName", scientificName, "rank", rank, "autoId", autoId);

            logger.debug("Values: " + values.size());
            StatementResult result = getSession().run(create_query, values);
            Record record = result.next();

            if (record != null)
            {
                logger.debug("parentGeneratedNodeId: " + parentGeneratedNodeId);
                if (parentGeneratedNodeId > 0) {
                    logger.debug("Parent available with id " + parentGeneratedNodeId);
                    logger.debug("record.get(\"n.generated_auto_id\"):" + record.get("n.generated_auto_id"));
                    createChildParentRelation(parentGeneratedNodeId, record.get("n.generated_auto_id").asInt() );
                }
                if (parentGeneratedNodeId == 0)
                {
                    logger.debug("Node is a root node");
                    create_query = "MATCH (n {generated_auto_id: {autoId}}) SET n:Root RETURN n.generated_auto_id";
                    result = getSession().run(create_query, parameters("autoId", autoId));
                }
                autoId++;

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

    public int createNodewithFulldata(int resourceId, String nodeId, String scientificName, String rank,
                                      int parentGeneratedNodeId, int pageId)
    {
        int nodeGeneratedId = getNodeIfExist(nodeId, resourceId);
        if(nodeGeneratedId != -1)
        {
            logger.debug("Node is found but needs to update its data");
            UpdateScientificName(nodeGeneratedId, scientificName);
            UpdateRank(nodeGeneratedId, rank);
            createChildParentRelation(parentGeneratedNodeId, nodeGeneratedId);
        }
        else
        {
            nodeGeneratedId = createAcceptedNode(resourceId, nodeId, scientificName, rank, parentGeneratedNodeId, pageId);
            logger.debug("Node is not found so created");
        }
        return nodeGeneratedId;
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
                    " created_at: timestamp(), updated_at: timestamp()}) RETURN s.generated_auto_id";
            StatementResult result = getSession().run(create_query, parameters("resourceId", resourceId,
                    "nodeId", nodeId, "scientificName", scientificName, "rank", rank, "autoId", autoId));
            Record record = result.next();

            if (record != null)
            {
                createRelationBetweenNodeAndSynonyms(acceptedNodeGeneratedId,
                        record.get("s.generated_auto_id").asInt());
                autoId++;

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

    public boolean createRelationBetweenNodeAndSynonyms(int acceptedNodeGeneratedId, int synonymGeneratedNodeId) {
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
                return true;
            }
        }
        else
            logger.debug("Synonym Accepted relation is not created");
        return false;

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
            logger.debug("The result of search" + record.get("n.generated_auto_id").asInt());
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

    public int getParent(int generatedNodeId)
    {
        int parentGeneratedNodeId;
        logger.debug("Get parent of node with autoId  " + generatedNodeId );
        String query = "MATCH(a:Node {generated_auto_id : {generatedNodeId}})<-[:IS_PARENT_OF*]-(n) RETURN n.generated_auto_id LIMIT 1";
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
        return parentGeneratedNodeId;
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
        String query = "MATCH (n {generated_auto_id: {generatedNodeId}})-[:IS_PARENT_OF]->(c:Node) RETURN c";
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
                //TODO: check this with the case reported by Mirona
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
        if (nodeId.equals("placeholder")) {
            logger.debug("This node has nodeId as placeholder");
            return false;
        }
        else {
            logger.debug("This node has nodeId "+ record.toString());
            return true;
        }

    }


    public Node getNodeProperties (int generatedNodeId)
    {
        logger.info("Get data of  node with generatedNodeId" + generatedNodeId);
        Node node = new Node();
        String query = "MATCH (n {generated_auto_id : {generatedNodeId}}) return n";
        StatementResult result = getSession().run(query, parameters("generatedNodeId",generatedNodeId));
        while (result.hasNext())
        {
            Record record = result.next();
            Value node_data = record.get("n");
            node.setGeneratedNodeId(node_data.get("generated_auto_id").asInt());
            node.setNodeId(node_data.get("node_id").toString());
            node.setResourceId(node_data.get("resource_id").asInt());
            if(node_data.get("page_id") != NULL) node.setPageId(node_data.get("page_id").asInt());
            node.setRank(node_data.get("rank").asString());
            node.setScientificName(node_data.get("scientific_name").asString());
        }
        return node;
    }


    public boolean UpdateScientificName(int generatedNodeId, String ScientificName)
    {
        logger.debug("Update Scientific Name of  node with generatedNodeId " + generatedNodeId);
        String query = "MATCH(n {generated_auto_id : {generatedNodeId}}) SET n.scientific_name = {ScientificName}," +
                " n.updated_at =  timestamp() RETURN n.generated_auto_id";
        StatementResult result =  getSession().run(query, parameters("generatedNodeId", generatedNodeId,
                "ScientificName", ScientificName));
        if (result.hasNext())
         return true;
        else
        {
            logger.debug("Problem occurred while updating scientific name");
            return false;
        }

    }

    public boolean UpdateRank(int generatedNodeId, String rank)
    {
        logger.debug("Update rank of  node with generatedNodeId " + generatedNodeId);
        String query = "MATCH (n {generated_auto_id: {generatedNodeId}}) SET n.rank = {rank}, " +
                "n.updated_at = timestamp()RETURN n.generated_auto_id";
        StatementResult result = getSession().run(query, parameters("generatedNodeId", generatedNodeId,
                "rank", rank));
        if (result.hasNext())
            return true;
        else
        {
            logger.debug("Problem occurred while updating scientific name");
            return false;
        }

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
        parentGeneratedNodeId = getParent(generatedNodeId);
        deleteParentRelation(generatedNodeId);
        deleteNode(generatedNodeId);
        return parentGeneratedNodeId;

    }

    public boolean setNodeLabel(int generatedNodeId, String label) {
        logger.debug("Add Label "+ label + " to Node with autoId "+ generatedNodeId);
        String query = "MATCH (n{generated_auto_id : {generatedNodeId}}) SET n:" + label + ", n.updated_at=timestamp() return n.generated_auto_id";
        logger.debug("query: " + query);
        StatementResult result = getSession().run(query, parameters("generatedNodeId", generatedNodeId));
        if (result.hasNext()) {
            logger.debug("Label " + label + " is added.");
            return true;
        } else {
            logger.debug("Problem occurred while adding node label.");
            return false;
        }
    }

    public ArrayList<Node> getLabeledNodesByAttribute(String attribute, String label, ArrayList<String> attributeVals) {
        String valsStr = "";
        if(Constants.NODE_ATTRIBUTES_STRs.contains(label))
            valsStr = attributeVals.stream().map(str -> "'" + str + "'").collect(Collectors.joining(","));
        else
            valsStr = attributeVals.stream().collect(Collectors.joining(","));
        logger.info("Get data of nodes attribute: " + attribute + " --vals--> " + valsStr);
        ArrayList<Node> nodes = new ArrayList<Node>();
        String query = "MATCH (n" + ((label.length() > 0)? ":" + label:"") + ")" + ((attributeVals.size() > 0)?
                ((attributeVals.size() == 1)? (" where n." + attribute + " = " + valsStr + " ") :
                        (" where n." + attribute + " in [" + valsStr + "]")
                ) : "" )
                + "return n";
        logger.debug("Neo4jCommon.getNodesByAttribute.query:" + query);
        StatementResult result = getSession().run(query);
        while (result.hasNext()) {
            Record record = result.next();
            Value node_data = record.get("n");
            nodes.add(getNode(node_data));
        }
        return nodes;
    }

    public List<HashMap<Integer, Integer>> getNodeAncestors(List<Integer> generatedNodesIds) {
        List<HashMap<Integer, Integer>> nodes = new ArrayList<HashMap<Integer, Integer>>();
        for(Integer gNodeId : generatedNodesIds) {
            HashMap<Integer, Integer> nodeList = new HashMap<Integer, Integer>();
            String query = " MATCH len = (p)-[:IS_PARENT_OF*0..]->(n:Node {generated_auto_id: {generatedNodeId}}) " +
                    "return length(len) AS len, p.generated_auto_id as id";
            StatementResult result = getSession().run(query, parameters("generatedNodeId",
                    gNodeId));
            while (result.hasNext()) {
                logger.debug("Node:" + gNodeId);
                Record record = result.next();
                nodeList.put(record.get("len").asInt(), record.get("id").asInt());
            }
            nodes.add(nodeList);


//            String query = " MATCH len = (p)-[:IS_PARENT_OF*0..]->(n:Node) where n.generated_auto_id in [ " +
//                    generatedNodesIds.stream().map(x -> x+"").collect(Collectors.joining(",")) +
//                    "] return collect(length(len)) as l,collect(p) as node, n.generated_auto_id as id";
//            StatementResult result = getSession().run(query);
//            while (result.hasNext()) {
//                logger.debug("Node:" + gNodeId);
//                Record record = result.next();
//                Node node = new Node();
//                Value lList = record.get("l");
//                Value nList = record.get("node");
//                Value id = record.get("id");
//
//                for(int i = 0; i < lList.size(); i++){
//
//                    nodeList.put(lList.get(i).asInt(), getNode(nList.get(i)));
//                }
//                nodes.add(nodeList);
//            }
        }
        return nodes;
    }

    private Node getNode(Value node_data) {
        Node node = new Node();
        node.setGeneratedNodeId(node_data.get("generated_auto_id").asInt());
        node.setNodeId(node_data.get("node_id").toString());
        node.setResourceId(node_data.get("resource_id").asInt());
        node.setRank(node_data.get("rank").asString());
        node.setScientificName(node_data.get("scientific_name").asString());
        if(node_data.get("page_id") != NULL)
            node.setPageId(node_data.get("page_id").asInt());
        return node;
    }
}

