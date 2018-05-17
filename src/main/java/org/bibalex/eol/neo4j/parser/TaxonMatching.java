package org.bibalex.eol.neo4j.parser;

import org.bibalex.eol.neo4j.models.Node;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;

import java.util.ArrayList;
import java.util.Collections;

import static org.neo4j.driver.v1.Values.NULL;
import static org.neo4j.driver.v1.Values.parameters;

public class TaxonMatching extends Neo4jCommon{
    public ArrayList<Node> getAncestorsNodes(int generatedNodeId)
    {
        ArrayList<Node> old_branch = new ArrayList<>();
        String query = "MATCH (n:Node {generated_auto_id: {generatedNodeId}})<-[:IS_PARENT_OF*]-(p) return p";
        StatementResult result = getSession().run(query, parameters("generatedNodeId",generatedNodeId));
        while (result.hasNext())
        {
            Record record = result.next();
            Node node = new Node();
            Value node_data = record.get("p");
            if( node_data.get("generated_auto_id")!= NULL ){node.setGeneratedNodeId(node_data.get("generated_auto_id").asInt());}
            if(node_data.get("node_id")!= NULL){node.setNodeId(node_data.get("node_id").toString());}
            if(node_data.get("resource_id")!= NULL){node.setResourceId(node_data.get("resource_id").asInt());}
            if(node_data.get("rank")!= NULL){node.setRank(node_data.get("rank").asString());}
            if(node_data.get("scientific_name")!= NULL){node.setScientificName(node_data.get("scientific_name").asString());}
            if(node_data.get("page_id")!= NULL){node.setPageId(node_data.get("page_id").asInt());}
            if(node_data.get("accepted_node_generated_id")!= NULL){node.setAcceptedNodeGeneratedId(node_data.get("accepted_node_generated_id").asInt());}
            if(node_data.get("accepted_node_id")!= NULL){node.setAcceptedNodeId(node_data.get("accepted_node_id").asString());}
            if(node_data.get("parent_node_generated_id")!= NULL){node.setParentGeneratedNodeId(node_data.get("parent_node_generated_id").asInt());}
            if(node_data.get("parent_node_id")!= NULL){node.setParentNodeId(node_data.get("parent_node_id").asString());}

            old_branch.add(node);
        }
//        Collections.reverse(old_branch);
        return old_branch;
    }

    public ArrayList<Node> getChildrenNode(int generatedNodeId)
    {
        logger.info("Getting children of node with autoId" + generatedNodeId);
        ArrayList<Node> children = new ArrayList<>();
        String query = "MATCH (n {generated_auto_id: {generatedNodeId}})-[:IS_PARENT_OF]->(c:Node) return c";
        StatementResult result = getSession().run(query, parameters("generatedNodeId",generatedNodeId));
        while (result.hasNext())
        {
            Record record = result.next();
            Node node = new Node();
            Value node_data = record.get("c");
            if( node_data.get("generated_auto_id")!= NULL ){node.setGeneratedNodeId(node_data.get("generated_auto_id").asInt());}
            if(node_data.get("node_id")!= NULL){node.setNodeId(node_data.get("node_id").toString());}
            if(node_data.get("resource_id")!= NULL){node.setResourceId(node_data.get("resource_id").asInt());}
            if(node_data.get("rank")!= NULL){node.setRank(node_data.get("rank").asString());}
            if(node_data.get("scientific_name")!= NULL){node.setScientificName(node_data.get("scientific_name").asString());}
            if(node_data.get("page_id")!= NULL){node.setPageId(node_data.get("page_id").asInt());}
            if(node_data.get("accepted_node_generated_id")!= NULL){node.setAcceptedNodeGeneratedId(node_data.get("accepted_node_generated_id").asInt());}
            if(node_data.get("accepted_node_id")!= NULL){node.setAcceptedNodeId(node_data.get("accepted_node_id").asString());}
            if(node_data.get("parent_node_generated_id")!= NULL){node.setParentGeneratedNodeId(node_data.get("parent_node_generated_id").asInt());}
            if(node_data.get("parent_node_id")!= NULL){node.setParentNodeId(node_data.get("parent_node_id").asString());}

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
            Node node = new Node();
            Value  node_data = record.get("n");
            if(node_data.get("generated_auto_id")!= NULL ){node.setGeneratedNodeId(node_data.get("generated_auto_id").asInt());}
            if(node_data.get("node_id")!= NULL){node.setNodeId(node_data.get("node_id").toString());}
            if(node_data.get("resource_id")!= NULL){node.setResourceId(node_data.get("resource_id").asInt());}
            if(node_data.get("rank")!= NULL){node.setRank(node_data.get("rank").asString());}
            if(node_data.get("scientific_name")!= NULL){node.setScientificName(node_data.get("scientific_name").asString());}
            if(node_data.get("page_id")!= NULL){node.setPageId(node_data.get("page_id").asInt());}
            if(node_data.get("accepted_node_generated_id")!= NULL){node.setAcceptedNodeGeneratedId(node_data.get("accepted_node_generated_id").asInt());}
            if(node_data.get("accepted_node_id")!= NULL){node.setAcceptedNodeId(node_data.get("accepted_node_id").asString());}
            if(node_data.get("parent_node_generated_id")!= NULL){node.setParentGeneratedNodeId(node_data.get("parent_node_generated_id").asInt());}
            if(node_data.get("parent_node_id")!= NULL){node.setParentNodeId(node_data.get("parent_node_id").asString());}

            roots.add(node);
        }
        return roots;
    }

    public void addPageIdtoNode(int generatedNodeId , int pageId)
    {
        logger.debug("Add pageId from Taxon Matching Algorithm to Node with autoId "+ generatedNodeId);
        String query = "MATCH (n:Node {generated_auto_id: {generatedNodeId}}) SET n.page_id = {pageId}";
        getSession().run(query, parameters("generatedNodeId", generatedNodeId, "pageId", pageId));
    }
}
