package org.bibalex.eol.neo4j.parser;

import org.bibalex.eol.neo4j.hbase.HbaseData;
import org.bibalex.eol.neo4j.models.Node;
import org.neo4j.csv.reader.Mark;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


import static org.neo4j.driver.v1.Values.NULL;
import static org.neo4j.driver.v1.Values.parameters;

public class Neo4jAncestryFormat extends Neo4jCommon {

    public int createAncestorIfNotExist(int resourceId, String scientificName, String rank, String nodeId,
                                        int parentGeneratedNodeId)
    {

        int nodeGeneratedNodeId = getAncestoryFormatNodeIfExist(resourceId, scientificName, rank, parentGeneratedNodeId);
        if (nodeGeneratedNodeId == -1)
        {
            logger.debug("Node "+ scientificName +" not found creating a new node");
            autoId = getAutoId();
            String create_query = "CREATE (n:Node {resource_id: {resourceId}, node_id: {nodeId}," +
                    " scientific_name: {scientificName}, rank: {rank}, generated_auto_id: {autoId}, created_at: apoc.date.currentTimestamp(), " +
                    "updated_at: apoc.date.currentTimestamp()})RETURN n.generated_auto_id";
            StatementResult result = getSession().run(create_query, parameters( "resourceId", resourceId,
                    "nodeId", nodeId, "scientificName", scientificName, "rank", rank, "autoId", autoId ));
            if (parentGeneratedNodeId > 0) {
                logger.debug("Parent available with id " + parentGeneratedNodeId);
                createChildParentRelation(parentGeneratedNodeId, autoId);
            }
            if (parentGeneratedNodeId < 0)
            {
                logger.debug("This node is created now and parent relation created later on");
            }
            if (parentGeneratedNodeId == 0)
            {
                logger.debug("Node is a root node");
                create_query = "MATCH (n {generated_auto_id: {autoId}}) SET n:Root RETURN n.generated_auto_id";
                result = getSession().run(create_query, parameters("autoId", autoId));
            }
            autoId ++;
            if (result.hasNext()) {
                logger.debug("Node  " + scientificName + " created ");
                Record record = result.next();
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

    public int getAncestoryFormatNodeIfExist(int resourceId, String scientificName, String rank, int parentGeneratedNodeId )
    {

        String query = "MATCH (n {resource_id: {resourceId} , scientific_name: {scientificName}," +
                " rank: {rank}})<-[r:IS_PARENT_OF]-(p {generated_auto_id: {parentGeneratedNodeId}}) RETURN n.generated_auto_id UNION" +
                " MATCH (n:Root {resource_id: {resourceId} , scientific_name: {scientificName}, rank: {rank}}) RETURN n.generated_auto_id";
        StatementResult result = getSession().run(query, parameters( "resourceId",resourceId,
                "scientificName", scientificName, "rank", rank , "parentGeneratedNodeId", parentGeneratedNodeId));

        if (result.hasNext())
        {
            Record record = result.next();
            logger.debug("The result of search" +record.get("n.generated_auto_id").asInt() );
            return record.get("n.generated_auto_id").asInt();
        }

        else
        {
            logger.debug("The result is -1");
            return -1;
        }

    }


    public int deleteNodeAncestoryFormat(String nodeId, int resourceId, String scientificName)
    {
        logger.debug("Deleting Node with nodeId " + nodeId + " of resource " + resourceId );
        int nodeGeneratedId = getAcceptedNodeIfExist(nodeId, scientificName, resourceId );
        if (nodeGeneratedId != -1)
        {
            if (hasChildren(nodeGeneratedId))
            {
                logger.debug("Node has children so just delete");
                MarkNodePlaceHolder(nodeGeneratedId);
            }
            else
            {
                CommonDeleteMethod(nodeGeneratedId);

            }
            if(checkIfNodeExists(nodeGeneratedId))
                return -1;
            else
                return nodeGeneratedId;

        }
        else
            return -1;

    }


    public boolean UpdateNodeAncestoryFormat(ArrayList<Node> nodes)
    {

            Node new_node = new Node();
            new_node = nodes.get(nodes.size()-1);
            int nodegeneratedId = getNodeIfExist(new_node.getNodeId(), new_node.getResourceId());
            new_node.setGeneratedNodeId(nodegeneratedId);
            boolean update_scientific_name = false;
            boolean update_rank = false;
            if (nodegeneratedId != -1)
            {
                Node old_node = getNodeProperties(nodegeneratedId);
                if (!old_node.getScientificName().equals(new_node.getScientificName())) {
                    logger.debug("Update scientific Name of the node");
                    update_scientific_name = UpdateScientificName(nodegeneratedId, new_node.getScientificName());

                }
                if (!old_node.getRank().equals(new_node.getRank())) {
                    logger.debug("Update rank of the node");
                    update_rank = UpdateRank(nodegeneratedId, new_node.getRank());

                }
                if (update_scientific_name || update_rank) {
                    return true;
                }
                else {
                    logger.debug("Update ancestry in ancestory format");
                    return UpdateHierarchy(nodes);
                }
            }

            logger.debug("Update failed");
            return false;


    }

    public boolean UpdateHierarchy(ArrayList<Node> nodes)
    {
        int nodegeneratedId;
        ArrayList<Object> old_branch  = new ArrayList<>();
        old_branch =  getOldBranchOfNode(nodes.get(nodes.size()-1));
        int updated_node_index = getIndexOfUpdatedNode(nodes);

        Node updated_node = new Node();
        Node parent_node  =  new Node();
        updated_node = nodes.get(updated_node_index);
        parent_node = nodes.get(updated_node_index-1);
        logger.debug("Create subtree from updated node till the end");
        nodegeneratedId = createAncestorIfNotExist(updated_node.getResourceId(), updated_node.getScientificName(), updated_node.getRank(),
                updated_node.getNodeId(), -1);
        updated_node.setGeneratedNodeId(nodegeneratedId);
        Node node = new Node();
        for(int i = updated_node_index+1; i < nodes.size(); i++)
        {
               logger.debug("Create subtree from updated node till the end");
               node = nodes.get(i);
               nodegeneratedId = createAncestorIfNotExist(node.getResourceId(), node.getScientificName(), node.getRank(),
                       node.getNodeId(), nodegeneratedId);
        }

        int old_node_generated_id  = (int)old_branch.get(old_branch.size()-1);
        CommonDeleteMethod(old_node_generated_id);
        logger.debug("the parent node here is " + parent_node.getScientificName() + parent_node.getGeneratedNodeId());
        createChildParentRelation(parent_node.getGeneratedNodeId(), updated_node.getGeneratedNodeId());

        int new_node_generated_id = getAncestoryFormatNodeIfExist(updated_node.getResourceId(), updated_node.getScientificName(),
                updated_node.getRank(), parent_node.getGeneratedNodeId());
        if (new_node_generated_id != old_node_generated_id)
        {
           logger.debug("New node created due to update ancestory");
           return true;
        }
        else
        {
            logger.debug("Update stopped some problem occurred");
            return false;
        }
    }

    public ArrayList<Object> getOldBranchOfNode(Node node)
    {
        ArrayList<Object> old_branch = new ArrayList<>();
        node.setGeneratedNodeId(getNodeIfExist(node.getNodeId(), node.getResourceId()));
        old_branch = getAncestors(node.getGeneratedNodeId());
        return old_branch;

    }

    public ArrayList<Object> getAncestors(int generatedNodeId)
    {
        ArrayList<Object> old_branch = new ArrayList<>();
        String query = "MATCH (n:Node {generated_auto_id: {generatedNodeId}})<-[:IS_PARENT_OF*]-(p) return p.generated_auto_id";
       StatementResult result = getSession().run(query, parameters("generatedNodeId",generatedNodeId));
        while (result.hasNext())
        {
            Record record = result.next();
            old_branch.add(record.get("p.generated_auto_id").asInt());
        }
        Collections.reverse(old_branch);
        return old_branch;
    }


    public int getIndexOfUpdatedNode(ArrayList <Node> nodes)
    {
        int nodegeneratedId;
        Node node = new Node();
        node = nodes.get(0);
        nodegeneratedId = getAncestoryFormatNodeIfExist(node.getResourceId(), node.getScientificName(), node.getRank(), 0);
        if (nodegeneratedId != -1)
        {
            int i;
            for (i = 1; i < nodes.size(); i++)
            {
                logger.debug("Search for the updated node in the branch");
                node = nodes.get(i);
                nodegeneratedId = getAncestoryFormatNodeIfExist(node.getResourceId(), node.getScientificName(), node.getRank(), nodegeneratedId);
                node.setGeneratedNodeId(nodegeneratedId);
                if (nodegeneratedId == -1)
                {
                    logger.debug("Update found starting from node with Scientific Name " + node.getScientificName());
                    break;
                }

            }
            return i;

        }
        else
        {
            logger.debug("The update is in Kingdom");
            return 0;
        }

    }



}
