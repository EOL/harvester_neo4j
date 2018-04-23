package org.bibalex.eol.neo4j.backend_api;

import org.bibalex.eol.neo4j.models.Node;

import org.bibalex.eol.neo4j.parser.Neo4jCommon;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;

import java.util.ArrayList;
import java.util.logging.Logger;

import static org.neo4j.driver.v1.Values.parameters;

public class Neo4jTree extends Neo4jCommon{
    Node root = new Node();
    ArrayList <Node> children= new ArrayList<>();
    Logger logger =  Logger.getLogger("Neo4jTree");

    public void setRoot(int generatedNodeId)
    {
        logger.info("The root is the node with generatedNodeId" + generatedNodeId);
        String query = "MATCH (n:Root {generated_auto_id: {generatedNodeId}}) return n";
        StatementResult result = getSession().run(query, parameters("generatedNodeId",generatedNodeId));
        while (result.hasNext())
        {
            Record record = result.next();
            Value  root_data = record.get("n");
            root.setGeneratedNodeId(root_data.get("generated_auto_id").asInt());
            root.setNodeId(root_data.get("node_id").toString());
            root.setResourceId(root_data.get("resource_id").asInt());
            root.setRank(root_data.get("rank").asString());
            root.setScientificName(root_data.get("scientific_name").asString());

        }

    }

    public Node getRoot()
    {
        return root;
    }

    public void setChildren(int RootGeneratedNodeId)
    {
        logger.info("Getting children of node with autoId" + RootGeneratedNodeId);
        String query = "MATCH (n:Root {generated_auto_id: {generatedNodeId}})-[:IS_PARENT_OF*]->(c:Node)  return c";
        StatementResult result = getSession().run(query, parameters("generatedNodeId",RootGeneratedNodeId));
        while (result.hasNext())
       {

            Record record = result.next();
            Node child = new Node();
            Value  child_data = record.get("c");
           child.setGeneratedNodeId(child_data.get("generated_auto_id").asInt());
           child.setNodeId(child_data.get("node_id").toString());
           child.setResourceId(child_data.get("resource_id").asInt());
           child.setRank(child_data.get("rank").asString());
           child.setScientificName(child_data.get("scientific_name").asString());

           children.add(child);
       }

    }

    public ArrayList<Node> getChildren()
    {
        return children;
    }

    public ArrayList<Neo4jTree> getTreeUpdates(String timestamp)
    {
        ArrayList<Object> roots = new ArrayList<>();
        ArrayList<Neo4jTree> trees = new ArrayList<>();
        logger.info("Get the trees harvested after " + timestamp);
        String query = "MATCH(n:Root) WHERE n.updated_at > apoc.date.parse({timestamp}, 'ms', 'dd.mm.yyyy') " +
                " RETURN n.generated_auto_id";
        StatementResult result = getSession().run(query, parameters("timestamp", timestamp));
        while (result.hasNext())
        {
            Record record = result.next();
            roots.add(record.get("n.generated_auto_id"));
        }

        roots.forEach((root) -> {
            Neo4jTree tree = new Neo4jTree();
            tree.setRoot(Integer.valueOf(root.toString()));
            tree.setChildren(Integer.valueOf(root.toString()));
            trees.add(tree);
        });

        return trees;
    }

    public ArrayList<Neo4jTree> getTrees(int resourceId)
    {
        ArrayList<Object> roots = new ArrayList<>();
        ArrayList<Neo4jTree> trees = new ArrayList<>();
        logger.info("Get the trees of resource " + resourceId);
        String query = "MATCH(n:Root) WHERE n.resource_id =  {resourceId} RETURN n.generated_auto_id";
        StatementResult result = getSession().run(query, parameters("resourceId", resourceId));
        while (result.hasNext())
        {
            Record record = result.next();
            roots.add(record.get("n.generated_auto_id"));
        }

        roots.forEach((root) -> {
            Neo4jTree tree = new Neo4jTree();
            tree.setRoot(Integer.valueOf(root.toString()));
            tree.setChildren(Integer.valueOf(root.toString()));
            trees.add(tree);
        });

        return trees;
    }

}
