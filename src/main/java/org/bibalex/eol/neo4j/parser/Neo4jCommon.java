package org.bibalex.eol.neo4j.parser;

import org.bibalex.eol.neo4j.models.Node;
import org.neo4j.driver.v1.*;

import static org.neo4j.driver.v1.Values.NULL;
import static org.neo4j.driver.v1.Values.parameters;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class Neo4jCommon {
    Session session;
//    int autoId = 0;
//    int pageId = 0;
    Logger logger = LoggerFactory.getLogger(Neo4jCommon.class);


    public void createInitNode() {
        String query = "Merge (n:IdCounter) ON create set n.nextId= 1 , n.nextPageId= 1;";
        StatementResult result = getSession().run(query);

        if (result.hasNext()) {
            logger.debug("Created init node");
        } else {
            logger.debug("Error in init node creation");
        }
    }


//    public int getAutoId() {
//        String query = "MATCH (n:GNode) RETURN n.generated_auto_id ORDER BY n.generated_auto_id DESC LIMIT 1";
//        StatementResult result = getSession().run(query);
//
//        if (result.hasNext()) {
//            logger.debug("AutoId found ");
//            Record record = result.next();
//            autoId = record.get("n.generated_auto_id").asInt() + 1;
//
//        } else {
//            logger.debug("Autoid IS 1");
//            autoId = 1;
//        }
//        logger.debug("Resulted autoId:" + autoId);
//        return autoId;
//    }

//    public int getPageId() {
//        String query = "MATCH (n:GNode:" + Constants.HAS_PAGE_LABEL + ") RETURN n.page_id ORDER BY n.page_id DESC LIMIT 1";
//        StatementResult result = getSession().run(query);
//
//        if (result.hasNext()) {
//            Record record = result.next();
//            pageId = record.get("n.page_id").asInt();
//            logger.debug("Page id " + pageId + " is retrieved.");
//            pageId++;
//        } else {
//            logger.debug("First page id.");
//            pageId = 1;
//        }
//        return pageId;
//    }

    public Session getSession() {
        if (session == null || !session.isOpen()) {
            // local
//            Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "root"));
//            Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "eol"));
            Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("root", "root"));
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
//            autoId = getAutoId();
            String hasPage = (pageId > 0)? (":" + Constants.HAS_PAGE_LABEL) : "";
            String create_query = "MATCH (c:IdCounter) CREATE (n:Node:GNode" + hasPage + " {resource_id: {resourceId}, node_id: {nodeId}," +
                    " scientific_name: {scientificName}, rank: {rank}, generated_auto_id: c.nextId, " +
                    ((pageId > 0)? ("page_id:c.nextPageId,") : "") + "created_at: timestamp()," +
                    "updated_at: timestamp()}) SET c.nextId = c.nextId + 1 " + ((parentGeneratedNodeId == 0)? ", n:Root " : "")
                    + ((pageId > 0)? (", c.nextPageId = c.nextPageId + 1 ") : "")
                    + "  RETURN n.generated_auto_id";

            logger.debug("Create Node query:" + create_query);
            Value values;
            values = parameters("resourceId", resourceId,
                    "nodeId", nodeId, "scientificName", scientificName, "rank", rank);

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
//                if (parentGeneratedNodeId == 0)
//                {
//                    logger.debug("Node is a root node");
//                    create_query = "MATCH (n {generated_auto_id: {autoId}}) SET n:Root RETURN n.generated_auto_id";
//                    result = getSession().run(create_query, parameters("autoId", autoId));
//                }
//                autoId++;

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

    public int createAcceptedNodeUpdated(int resourceId, String nodeId, String scientificName, String rank,
                                  int parentGeneratedNodeId, int pageId) {

//        int nodeGeneratedNodeId = getAcceptedNodeIfExist( nodeId, scientificName, resourceId);
//        if (nodeGeneratedNodeId == -1)
//        {
            logger.debug("Node "+ scientificName +" not found creating a new node");
//            autoId = getAutoId();
            String hasPage = (pageId > 0)? (":" + Constants.HAS_PAGE_LABEL) : "";
            String create_query = "MATCH (c:IdCounter) CREATE (n:Node:GNode" + ((parentGeneratedNodeId == 0)? (":" + Constants.ROOT_LABEL) : "") + hasPage + " {resource_id: {resourceId}, node_id: {nodeId}," +
                    " scientific_name: {scientificName}, rank: {rank}, generated_auto_id: c.nextId, " +
                    ((pageId > 0)? ("page_id:c.nextPageId,") : "") + " created_at: timestamp()," +
                    "updated_at: timestamp()}) SET c.nextId = c.nextId + 1 "
                    + ((pageId > 0)? (", c.nextPageId = c.nextPageId + 1 ") : "")
                    + " RETURN n.generated_auto_id";

            logger.debug("Create Node query:" + create_query);
            Value values;
            values = parameters("resourceId", resourceId,
                    "nodeId", nodeId, "scientificName", scientificName, "rank", rank);

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
//                if (parentGeneratedNodeId == 0)
//                {
//                    logger.debug("Node is a root node");
//                    create_query = "MATCH (n {generated_auto_id: {autoId}}) SET n:Root RETURN n.generated_auto_id";
//                    result = getSession().run(create_query, parameters("autoId", autoId));
//                }
//                autoId++;

                logger.debug("Node  " + scientificName + " created ");
                return record.get("n.generated_auto_id").asInt();
            } else {
                logger.debug("Node  " + scientificName + " is not created a problem has occurred");
                return -1;
            }
//        }
//        else
//        {
//            logger.debug("Node "+ scientificName +"  found");
//            return nodeGeneratedNodeId;
//        }
    }

    public int createNodewithFulldata(int resourceId, String nodeId, String scientificName, String rank,
                                      int parentGeneratedNodeId, int pageId)
    {
//        int nodeGeneratedId = getNodeIfExist(nodeId, resourceId);
//        if(nodeGeneratedId != -1)
//        {
            logger.debug("Node is found but needs to update its data");
            int nodeGeneratedId = updateMissingParentNode(resourceId, nodeId, scientificName, rank, pageId);
            System.out.println("parentGeneratedNodeId:" + parentGeneratedNodeId);
            if(parentGeneratedNodeId != 0)
                createChildParentRelation(parentGeneratedNodeId, nodeGeneratedId);
//        }
//        else
//        {
//            nodeGeneratedId = createAcceptedNodeUpdated(resourceId, nodeId, scientificName, rank, parentGeneratedNodeId, pageId);
//            logger.debug("Node is not found so created");
//        }
        return nodeGeneratedId;
    }

    public void createChildParentRelation(int parentGeneratedNodeId, int childGeneratedNodeId) {
//        boolean node_exists = checkIfNodeExists(childGeneratedNodeId);
//        boolean parent_exists = checkIfNodeExists(parentGeneratedNodeId);
//        if(node_exists && parent_exists) {
            String query = " MATCH(p:Node), (c:Node) WHERE p.generated_auto_id = {parentGeneratedNodeId} AND " +
                    "c.generated_auto_id = {childGeneratedNodeId} CREATE (p)-[r:IS_PARENT_OF]->(c) RETURN r";
            StatementResult result = getSession().run(query, parameters("parentGeneratedNodeId",
                    parentGeneratedNodeId, "childGeneratedNodeId", childGeneratedNodeId));
            if (result.hasNext()) {
                logger.debug("Node" + childGeneratedNodeId + "created");
                logger.debug("Child Parent relation created with parentgeneratedNodeId " + parentGeneratedNodeId);

            } else
                logger.debug("Can't create relation between generated auto ids: " + parentGeneratedNodeId + " and " + childGeneratedNodeId);
//        }else
//            logger.debug("Child Parent relation not created for child " + childGeneratedNodeId + "and parent " + parentGeneratedNodeId);
    }

    public int createSynonymNode(int resourceId, String nodeId, String scientificName, String rank,
                                 String acceptedNodeId, int acceptedNodeGeneratedId) {

        int nodeGeneratedNodeId = getSynonymNodeIfExist(nodeId, scientificName, resourceId, acceptedNodeId, acceptedNodeGeneratedId);
        if (nodeGeneratedNodeId == -1)
        {
            logger.debug("Node "+ scientificName +" not found creating a new node");
//            autoId = getAutoId();
            String create_query = "MATCH (c:IdCounter) CREATE (s:Synonym:GNode {resource_id: {resourceId}, node_id: {nodeId}," +
                    "scientific_name: {scientificName}, rank: {rank}, generated_auto_id: c.nextId," +
                    " created_at: timestamp(), updated_at: timestamp()}) SET c.nextId = c.nextId + 1 RETURN s.generated_auto_id";
            StatementResult result = getSession().run(create_query, parameters("resourceId", resourceId,
                    "nodeId", nodeId, "scientificName", scientificName, "rank", rank));
            Record record = result.next();

            if (record != null)
            {
                createRelationBetweenNodeAndSynonyms(acceptedNodeGeneratedId,
                        record.get("s.generated_auto_id").asInt());
//                autoId++;

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
        String query = "MATCH (n:GNode) WHERE n.node_id = {nodeId} AND n.resource_id = {resourceId} " +
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
        String query = "MATCH (n:Node:GNode) WHERE n.node_id = {nodeId} AND n.resource_id = {resourceId} AND " +
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

        String query = "MATCH (a:Node:GNode {node_id: {nodeId}, resource_id: {resourceId} ," +
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
        String query = "MATCH (n:GNode {generated_auto_id: {generatedNodeId}})-[:IS_PARENT_OF]->(c:Node) RETURN c";
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
            if(parentGeneratedNodeId != -1 && !parentHasAttribute(parentGeneratedNodeId, Constants.NODE_ATTRIBUTE_NODEID))
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

    public boolean parentHasAttribute(int generatedNodeId, String attr) {
        logger.debug("Check the " + attr + " of the node with autoId " + generatedNodeId);
        String query = "MATCH (n {generated_auto_id: {generatedNodeId}}) RETURN n." + attr;
        StatementResult result = getSession().run(query, parameters("generatedNodeId", generatedNodeId));
        Record record = result.next();
        String attrVal = record.get("n." + attr).toString().replace("\"", "");
        if (attrVal.equals("placeholder")) {
            logger.debug("This node has " + attr + " as placeholder");
            return false;
        }
        else {
            logger.debug("This node has " + attr + " " + record.toString());
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


    public int updateMissingParentNode(int resourceId, String nodeId, String sciName, String rank, int pageId)
    {
        System.out.println("PageId: =--------------->"  +pageId);
        logger.debug("Update missing parent of nodeId(" + nodeId + ") and resource(" + resourceId + ")");
        String query = "MATCH (n:GNode {" + Constants.NODE_ATTRIBUTE_NODEID + ": {nodeId}, " + Constants.NODE_ATTRIBUTE_RESOURCEID
                + ": {resourceId}}) SET " + ((pageId > 0)? "n:" + Constants.HAS_PAGE_LABEL + ", n." + Constants.NODE_ATTRIBUTE_PAGEID + "=" + pageId  + ",": "" )
                + " n." + Constants.NODE_ATTRIBUTE_RANK + " = {rank}, n." + Constants.NODE_ATTRIBUTE_SCIENTIFICNAME + " = {sciName}," +
                "n." + Constants.NODE_ATTRIBUTE_UPDATED_AT + " = timestamp() RETURN n.generated_auto_id";
        StatementResult result = getSession().run(query, parameters("nodeId", nodeId,
                "resourceId", resourceId, "sciName", sciName, "rank", rank));
        if (result.hasNext()) {
            Record record = result.next();
            return record.get("n.generated_auto_id").asInt();
        } else {
            logger.debug("Problem occurred while updating missing parent.");
            return -1;
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
        String query ="MATCH(n {generated_auto_id : {generatedNodeId}})-[rel:IS_PARENT_OF]-(p) DELETE rel" ;
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
        String query = "MATCH (n {generated_auto_id : {generatedNodeId}}) SET n:" + label + ", n.updated_at=timestamp() return n.generated_auto_id";
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
        String query = "MATCH (n:GNode" + ((label.length() > 0)? ":" + label:"") + ")" + ((attributeVals.size() > 0)?
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
            String query = " MATCH len = (p)-[:IS_PARENT_OF*0..]->(n:GNode {generated_auto_id: {generatedNodeId}}) " +
                    "return length(len) AS len, p.generated_auto_id as id";
            HashMap<Integer, Integer> nodeList = new HashMap<Integer, Integer>();
            StatementResult result = getSession().run(query, parameters("generatedNodeId",
                    gNodeId));
            while (result.hasNext()) {
                logger.debug("Node:" + gNodeId);
                Record record = result.next();
                nodeList.put(record.get("len").asInt(), record.get("id").asInt());
            }
            nodes.add(nodeList);
        }

//            String query = " MATCH len = (p)-[:IS_PARENT_OF*0..]->(n:Node) where n.generated_auto_id in [ " +
//                    generatedNodesIds.stream().map(x -> x+"").collect(Collectors.joining(",")) +
//                    "] return collect(length(len)) as l,collect(p.generated_auto_id) as nId, n.generated_auto_id as id";
//            StatementResult result = getSession().run(query);
//            while (result.hasNext()) {
//                Record record = result.next();
//                Node node = new Node();
//                Value lList = record.get("l");
//                Value nList = record.get("nId");
//                Value id = record.get("id");
//                logger.debug("->" + id.asInt());
//                HashMap<Integer, Integer> nodeList = new HashMap<Integer, Integer>();
//
//                for(int i = 0; i < lList.size(); i++){
//                    logger.debug("------------> " + lList.get(i).asInt() + ":" + nList.get(i).asInt());
//                    nodeList.put(lList.get(i).asInt(), nList.get(i).asInt());
//                }
//                nodes.add(nodeList);
//            }

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

    public int updateAcceptedNode(int resourceId, String nodeId, int parentGeneratedNodeId, String sciName) {

        int nodeGeneratedNodeId = getAcceptedNodeIfExist(nodeId, Constants.PLACE_HOLDER, resourceId);
        if (nodeGeneratedNodeId == -1)
        {
            logger.error("Node "+ nodeId +" not found.");
            return -1;
        }
        else
        {
            // update node, set scientific name
            boolean updated = UpdateScientificName(nodeGeneratedNodeId, sciName);
            if(updated) {
                logger.debug("Node " + nodeId + " found");
                logger.debug("parentGeneratedNodeId: " + parentGeneratedNodeId);
                if (parentGeneratedNodeId > 0) {
                    logger.debug("Parent available with id " + parentGeneratedNodeId);
                    logger.debug("record.get(\"n.generated_auto_id\"):" + nodeGeneratedNodeId);
                    createChildParentRelation(parentGeneratedNodeId, nodeGeneratedNodeId);
                }
                if (parentGeneratedNodeId == 0) {
                    logger.debug("Node is a root node");
                    String create_query = "MATCH (n {generated_auto_id: {autoId}}) SET n:Root RETURN n.generated_auto_id";
                    StatementResult result = getSession().run(create_query, parameters("autoId", nodeGeneratedNodeId));
                }
            } else {
                logger.error("Can't update Accepted node scientific name ..");
            }
            return nodeGeneratedNodeId;
        }
    }

    public int harvestParentFormatResource(String resourceId, String resDirectory, String neo4jDirectory) throws IOException {
//        String[] args = new String[] {"/bin/bash", "-c", "your_command", "with", "args"};
        // cat scriptPath | cypher-shell-path > outputFile
        // (echo "export abc='12'"; cat queries.cpl;)  | ./neo4j-shell
        // (echo "export resourceId='12' taxaName='file:///taxa_9.txt'";  cat queries.cpl;)  | ./neo4j-shell
        // (echo "export taxaName='taxa_9.txt'"; echo "export resourceId='12'";  cat queries.cpl;)  | ./neo4j-shell
        String[] args = new String[] {"ping", "www.google.com"};
        Process proc = new ProcessBuilder(args).start();
        //Added 30/9
        return 0;
    }

}
