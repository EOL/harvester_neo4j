package org.bibalex.eol.neo4j.parser;

import org.bibalex.eol.neo4j.models.Node;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.neo4j.driver.v1.Values.NULL;
import static org.neo4j.driver.v1.Values.parameters;

public class TaxonMatching extends Neo4jCommon{
    public ArrayList<Node> getAncestorsNodes(int generatedNodeId)
    {
        ArrayList<Node> old_branch = new ArrayList<>();
        String query = "MATCH (n:GNode {generated_auto_id: {generatedNodeId}})<-[:IS_PARENT_OF*]-(p) return p";
        StatementResult result = getSession().run(query, parameters("generatedNodeId",generatedNodeId));
        while (result.hasNext())
        {
            Record record = result.next();
            Node node = setNode(record.get("p"));
            old_branch.add(node);
        }
//        Collections.reverse(old_branch);
        return old_branch;
    }

    public ArrayList<Node> getChildrenNode(int generatedNodeId)
    {
        logger.info("Getting children of node with autoId" + generatedNodeId);
        ArrayList<Node> children = new ArrayList<>();
        String query = "MATCH (n:GNode {generated_auto_id: {generatedNodeId}})-[:IS_PARENT_OF]->(c:Node) return c";
        StatementResult result = getSession().run(query, parameters("generatedNodeId",generatedNodeId));
        while (result.hasNext())
        {
            Record record = result.next();
            Node node = setNode(record.get("c"));
            children.add(node);
        }

        return children;
    }

    public ArrayList<Node> getRootNodes(int resourceId)
    {
        String query = "MATCH (n:Root {resource_id:{resourceId}}) RETURN n";
        StatementResult result = getSession().run(query,parameters("resourceId", resourceId));
        ArrayList<Node> roots = new ArrayList<>();
        while (result.hasNext())
        {
            Record record = result.next();
            Node node = setNode(record.get("n"));
            roots.add(node);
        }
        return roots;
    }

    public int addPageIdtoNode(int generatedNodeId, int pageId)
    {

//        int page_Id = -1;
//        logger.debug("Add pageId from Taxon Matching Algorithm to Node with autoId "+ generatedNodeId);
//        String query = "MATCH (c:IdCounter) MATCH (n {generated_auto_id: {generatedNodeId}}) SET n:" + Constants.HAS_PAGE_LABEL + ", n.page_id = c.nextPageId," +
//                " n.updated_at = timestamp() SET c.nextPageId = c.nextPageId + 1 RETURN n.page_id";
//        StatementResult result = getSession().run(query, parameters("generatedNodeId", generatedNodeId));

//        int page_Id = -1;
//        logger.debug("Add pageId from Taxon Matching Algorithm to Node with autoId "+ generatedNodeId);
//        String query = "MATCH (c:GlobalUniqueId) MATCH (n:GNode {generated_auto_id: {generatedNodeId}}) SET n:" + Constants.HAS_PAGE_LABEL + ", n.page_id = c.page_id," +
//                " n.updated_at = timestamp() SET c.page_id = c.page_id + 1 RETURN n.page_id";
//        StatementResult result = getSession().run(query, parameters("generatedNodeId", generatedNodeId));
//        if(result.hasNext())
//        {
//            Record record = result.next();
//            if(record.get("n.page_id")!= NULL)
//                page_Id = record.get("n.page_id").asInt();
//
//        }
//        return page_Id;
        int page_Id = -1;
        if (pageId == -1) {
            logger.debug("Add pageId from Taxon Matching Algorithm to Node with autoId " + generatedNodeId);
            String query = "MATCH (c:GlobalUniqueId) MATCH (n:GNode {generated_auto_id: {generatedNodeId}}) SET n:" + Constants.HAS_PAGE_LABEL + ", n.page_id = c.page_id," +
                    " n.updated_at = timestamp() SET c.page_id = c.page_id + 1 RETURN n.page_id";
            StatementResult result = getSession().run(query, parameters("generatedNodeId", generatedNodeId));
            if(result.hasNext()) {
                Record record = result.next();
                if(record.get("n.page_id") != NULL)
                    page_Id = record.get("n.page_id").asInt();
             }
        return page_Id;
        } else {
            logger.debug("Found Match and adding pageId");
            String query = "MATCH (n:GNode {generated_auto_id: {generatedNodeId}}) SET n:" + Constants.HAS_PAGE_LABEL + ", n.page_id = {pageId}" +
                    " RETURN n.page_id";
            StatementResult result = getSession().run(query, parameters("generatedNodeId", generatedNodeId, "pageId", pageId ));
            if(result.hasNext()) {
                Record record = result.next();
                if(record.get("n.page_id")!= NULL)
                    page_Id = record.get("n.page_id").asInt();
            }
        }
         return page_Id;
    }

    public boolean addPagestoNode(HashMap<Integer,Integer> results)
    {
            for (Integer generatedNodeId : results.keySet()) {
                Integer pageId = results.get(generatedNodeId);
                String query =  "MATCH (n:GNode {generated_auto_id: {generatedNodeId}}) SET n:" + Constants.HAS_PAGE_LABEL + ", n.page_id = {pageId}" +
                    " RETURN n.page_id";
                StatementResult qresult = getSession().run(query, parameters("generatedNodeId", generatedNodeId, "pageId", pageId ));
                if(qresult.hasNext()) {
                    Record record = qresult.next();
                    logger.debug(" The pageId " +record.get("n.page_id"));
                    if (record.get("n.page_id") == NULL)
                        return  false;
                }
        }
        return true;
    }

    public ArrayList<Node> getSynonyms(int generatedNodeId)
    {
        logger.info("Getting synonyms of node with autoId" + generatedNodeId);
        ArrayList<Node> synonyms = new ArrayList<>();
        String query = "MATCH (a:GNode {generated_auto_id: {generatedNodeId}})<-[:IS_SYNONYM_OF]-(s:Synonym) return s";
        StatementResult result = getSession().run(query, parameters("generatedNodeId",generatedNodeId));
        while (result.hasNext())
        {

            Record record = result.next();
            Node node = setNode(record.get("s"));
            synonyms.add(node);
        }
        return synonyms;
    }

    public Node setNode(Value node_data)
    {
        Node node=new Node();

        if(node_data.get(Constants.NODE_ATTRIBUTE_GENERATEDID) != NULL ){
            node.setGeneratedNodeId(node_data.get(Constants.NODE_ATTRIBUTE_GENERATEDID).asInt());}
        if(node_data.get(Constants.NODE_ATTRIBUTE_NODEID) != NULL){
            node.setNodeId(node_data.get(Constants.NODE_ATTRIBUTE_NODEID).asString());}
        if(node_data.get(Constants.NODE_ATTRIBUTE_RESOURCEID) != NULL){
            node.setResourceId(node_data.get(Constants.NODE_ATTRIBUTE_RESOURCEID).asInt());}
        if(node_data.get(Constants.NODE_ATTRIBUTE_RANK)!= NULL){
            node.setRank(node_data.get(Constants.NODE_ATTRIBUTE_RANK).asString());}
        if(node_data.get(Constants.NODE_ATTRIBUTE_SCIENTIFICNAME) != NULL){
            node.setScientificName(node_data.get(Constants.NODE_ATTRIBUTE_SCIENTIFICNAME).asString());}
        if(node_data.get(Constants.NODE_ATTRIBUTE_PAGEID) != NULL){
            node.setPageId(node_data.get(Constants.NODE_ATTRIBUTE_PAGEID).asInt());}
        if(node_data.get(Constants.NODE_ATTRIBUTE_ACCEPTED_NODE_GENERATED_ID) != NULL){
            node.setAcceptedNodeGeneratedId(node_data.get(Constants.NODE_ATTRIBUTE_ACCEPTED_NODE_GENERATED_ID).asInt());}
        if(node_data.get(Constants.NODE_ATTRIBUTE_ACCEPTED_NODEID) != NULL){
            node.setAcceptedNodeId(node_data.get(Constants.NODE_ATTRIBUTE_ACCEPTED_NODEID).asString());}
        if(node_data.get(Constants.NODE_ATTRIBUTE_PARENT_NODE_GENERATED_ID) != NULL){
            node.setParentGeneratedNodeId(node_data.get(Constants.NODE_ATTRIBUTE_PARENT_NODE_GENERATED_ID).asInt());}
        if(node_data.get(Constants.NODE_ATTRIBUTE_PARENT_NODEID) != NULL){
            node.setParentNodeId(node_data.get(Constants.NODE_ATTRIBUTE_PARENT_NODEID).asString());}
        if(node_data.get(Constants.NODE_ATTRIBUTE_UPDATED_AT) != NULL){
            node.setUpdated_at(node_data.get(Constants.NODE_ATTRIBUTE_UPDATED_AT).asLong());}
        if(node_data.get(Constants.NODE_ATTRIBUTE_CREATED_AT) != NULL){
            node.setCreated_at(node_data.get(Constants.NODE_ATTRIBUTE_CREATED_AT).asLong());}
        if(node_data.get(Constants.NODE_ATTRIBUTE_CANONICAL_NAME) != NULL){
            node.setCanonicalName(node_data.get(Constants.NODE_ATTRIBUTE_CANONICAL_NAME).asString());}

        return node;
    }

}
