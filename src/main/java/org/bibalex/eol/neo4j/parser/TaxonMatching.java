package org.bibalex.eol.neo4j.parser;

import org.bibalex.eol.neo4j.models.Node;
import org.neo4j.driver.Record;
import org.neo4j.driver.StatementResult;
import org.neo4j.driver.Value;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.neo4j.driver.Values.NULL;
import static org.neo4j.driver.Values.parameters;

public class TaxonMatching extends Neo4jCommon{
    private static final Logger logger = LogManager.getLogger(TaxonMatching.class);
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
        logger.info("Getting Children of Node: " + generatedNodeId);
        ArrayList<Node> children = new ArrayList<>();
        String query = "MATCH (n:GNode {generated_auto_id: {generatedNodeId}})-[:IS_PARENT_OF]->(c:Node) return c";
        StatementResult result = getSession().run(query, parameters("generatedNodeId",generatedNodeId));
        while (result.hasNext())
        {
            Record record = result.next();
            Node node = setNode(record.get("c"));
            children.add(node);
        }
        logger.debug("Children: \n" + children);

        return children;
    }

    public ArrayList<Node> getRootNodes(int resourceId)
    {
        logger.info("Getting Root Nodes of Resource: " + resourceId);
        String query = "MATCH (n:Root {resource_id:{resourceId}}) RETURN n";
        StatementResult result = getSession().run(query,parameters("resourceId", resourceId));
        ArrayList<Node> roots = new ArrayList<>();
        while (result.hasNext())
        {
            Record record = result.next();
            Node node = setNode(record.get("n"));
            roots.add(node);
        }
        logger.debug("Root Nodes: \n" + roots);
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
            logger.debug("Add pageId from Taxon Matching Algorithm to Node:" + generatedNodeId);
            String query = "MATCH (c:GlobalUniqueId) MATCH (n:GNode {generated_auto_id: {generatedNodeId}}) SET n:" + Constants.HAS_PAGE_LABEL + ", n.page_id = c.page_id," +
                    " n.updated_at = timestamp() SET c.page_id = c.page_id + 1 RETURN n.page_id";
            StatementResult result = getSession().run(query, parameters("generatedNodeId", generatedNodeId));
            if(result.hasNext()) {
                Record record = result.next();
                if(record.get("n.page_id")!= NULL)
                    page_Id = record.get("n.page_id").asInt();

             }
             logger.debug("Page ID: " + page_Id);
        return page_Id;
        }

        else {
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
        logger.debug("Page ID: " + page_Id);
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
        logger.info("Getting Synonyms of Node: " + generatedNodeId);
        ArrayList<Node> synonyms = new ArrayList<>();
        String query = "MATCH (a:GNode {generated_auto_id: {generatedNodeId}})<-[:IS_SYNONYM_OF]-(s:Synonym) return s";
        StatementResult result = getSession().run(query, parameters("generatedNodeId",generatedNodeId));
        while (result.hasNext())
        {

            Record record = result.next();
            Node node = setNode(record.get("s"));
            synonyms.add(node);
        }
        logger.debug("Synonyms: \n" + synonyms);
        return synonyms;
    }

    public Node setNode(Value node_data)
    {   Node node=new Node();
        if( node_data.get("generated_auto_id")!= NULL ){node.setGeneratedNodeId(node_data.get("generated_auto_id").asInt());}
        if(node_data.get("node_id")!= NULL){node.setNodeId(node_data.get("node_id").asString());}
        if(node_data.get("resource_id")!= NULL){node.setResourceId(node_data.get("resource_id").asInt());}
        if(node_data.get("rank")!= NULL){node.setRank(node_data.get("rank").asString());}
        if(node_data.get("scientific_name")!= NULL){node.setScientificName(node_data.get("scientific_name").asString());}
        if(node_data.get("page_id")!= NULL){node.setPageId(node_data.get("page_id").asInt());}
        if(node_data.get("accepted_node_generated_id")!= NULL){node.setAcceptedNodeGeneratedId(node_data.get("accepted_node_generated_id").asInt());}
        if(node_data.get("accepted_node_id")!= NULL){node.setAcceptedNodeId(node_data.get("accepted_node_id").asString());}
        if(node_data.get("parent_node_generated_id")!= NULL){node.setParentGeneratedNodeId(node_data.get("parent_node_generated_id").asInt());}
        if(node_data.get("parent_node_id")!= NULL){node.setParentNodeId(node_data.get("parent_node_id").asString());}
        if(node_data.get("updated_at")!= NULL){node.setUpdated_at(node_data.get("updated_at").asLong());}
        if(node_data.get("created_at")!= NULL){node.setCreated_at(node_data.get("created_at").asLong());}
        if(node_data.get("canonical_name")!= NULL){node.setCanonicalName(node_data.get("canonical_name").asString());}

        return node;
    }

}
