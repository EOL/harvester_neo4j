package org.bibalex.eol.neo4j.parser;

import org.bibalex.eol.neo4j.models.Node;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;

import static org.neo4j.driver.v1.Values.parameters;

public class Neo4jParentFormat extends Neo4jCommon  {

    public int createParentWithPlaceholder(int resourceId, String parentUsageId)
    {
        int nodeGeneratedNodeId = getNodeIfExist(parentUsageId, resourceId);
        if (nodeGeneratedNodeId == -1)
        {
            autoId = getAutoId();
            String create_query = "CREATE (n:Node {resource_id: {resourceId}, node_id: {parentUsageId}," +
                    " scientific_name: {scientificName}, rank: {rank}, generated_auto_id: {autoId}," +
                    " created_at: apoc.date.currentTimestamp(),updated_at: apoc.date.currentTimestamp()})" +
                    "RETURN n.generated_auto_id";
            StatementResult result = getSession().run(create_query, parameters("resourceId", resourceId,
                    "parentUsageId", parentUsageId, "scientificName", "placeholder", "rank", "placeholder", "autoId", autoId));
            if(parentUsageId == "0")
            {
                logger.debug("Node is a root node");
                create_query = "MATCH (n {generated_auto_id: {autoId}}) SET n:Root RETURN n.generated_auto_id";
                result = getSession().run(create_query, parameters("autoId", autoId));
            }
            autoId++;
            if (result.hasNext()) {
                logger.debug("Node with id " + parentUsageId + " created with placeholders");
                Record record = result.next();
                return record.get("n.generated_auto_id").asInt();
            } else {
                logger.debug("Node with id  " + parentUsageId + " is not created a problem has occurred");
                return -1;
            }
        }
        else
        {
            logger.debug("Node with nodeId "+ parentUsageId +"  found");
            return nodeGeneratedNodeId;
        }

    }

    public int getNodeGivenParentIfExists(int resourceId, String scientificName, String rank, String nodeId, int parentGeneratedNodeId)
    {
        String query = "MATCH (n {resource_id: {resourceId} , scientific_name: {scientificName}," +
                " rank: {rank}, node_id: {nodeId}})<-[r:IS_PARENT_OF]-(p {generated_auto_id: {parentGeneratedNodeId}}) RETURN n.generated_auto_id LIMIT 1 UNION" +
                " MATCH (n:Root {resource_id: {resourceId} , scientific_name: {scientificName}, rank: {rank}}) RETURN n.generated_auto_id";
        StatementResult result = getSession().run(query, parameters( "resourceId",resourceId,
                "scientificName", scientificName, "rank", rank ,"nodeId", nodeId, "parentGeneratedNodeId", parentGeneratedNodeId));
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


    public int deleteNodeParentFormat(String nodeId, int resourceId, String scientificName)
    {
        logger.debug("Deleting Node with nodeId " + nodeId + " of resource " + resourceId );
        int nodeGeneratedId = getAcceptedNodeIfExist(nodeId, scientificName, resourceId);
        if (nodeGeneratedId != -1)
        {
            if (hasChildren(nodeGeneratedId))
            {
                logger.debug("Node has children so just delete");
                deleteNode(nodeGeneratedId);
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

    public void deleteOldParentRelation(int nodegeneratedId, int oldParentId)
    {
        logger.debug("Deleting relation between node and its old parent");
        String query = "MATCH(n {generated_auto_id: {nodegeneratedId}})<-[r:IS_PARENT_OF]-(p {generated_auto_id: {oldParentId}}) DELETE r RETURN  n.generated_auto_id";
        StatementResult result = getSession().run(query, parameters("nodegeneratedId", nodegeneratedId, "oldParentId", oldParentId));
        if (result.hasNext())
            logger.debug("Old relation deleted");
        else
            logger.debug("Old relation not deleted problem occurred");


    }

    public int UpdateNodeParentFormat (Node new_node, String new_parent_nodeId)
    {
//        int nodegeneratedId = getNodeIfExist(new_node.getNodeId(), new_node.getResourceId());
        int new_parent_id = getNodeIfExist(new_parent_nodeId,new_node.getResourceId());
        int nodegeneratedId = getNodeGivenParentIfExists(new_node.getResourceId(), new_node.getScientificName(), new_node.getRank(),
                new_node.getNodeId(), new_parent_id);
        new_node.setGeneratedNodeId(nodegeneratedId);
        boolean update_scientific_name = false;
        boolean update_rank = false;
        if (nodegeneratedId != -1)
        {
            Node old_node = getNodeProperties(nodegeneratedId);
            if (!old_node.getScientificName().equals(new_node.getScientificName()))
            {
                logger.debug("Update scientific Name of the node");
                update_scientific_name = UpdateScientificName(nodegeneratedId, new_node.getScientificName());

            }
            if (!old_node.getRank().equals(new_node.getRank()))
            {
                logger.debug("Update rank of the node");
                update_rank = UpdateRank(nodegeneratedId, new_node.getRank());

            }
            if(update_scientific_name || update_rank)
            {
                return 1;
            }
            else
            {
                logger.debug("Update ancestry in parent format");
                return UpdateHierarchy(new_node, new_parent_nodeId);
            }

        }
            logger.debug("Update failed");
            return 0;

    }

    public int UpdateHierarchy(Node new_node, String new_parent_nodeId)
    {
        int old_parent_id = getParent(new_node.getGeneratedNodeId());
        int new_parent_id = getNodeIfExist(new_parent_nodeId,new_node.getResourceId());
        Node old_parent = getNodeProperties(old_parent_id);
        int new_nodeGeneratedId = getNodeGivenParentIfExists(new_node.getResourceId(), new_node.getScientificName(),
                new_node.getRank(), new_node.getNodeId(), new_parent_id);
        if (new_nodeGeneratedId == -1)
        {
            logger.debug("Node with this new parent is not found");
            new_nodeGeneratedId = createAcceptedNode(new_node.getResourceId(), new_node.getNodeId(), new_node.getScientificName(),
                    new_node.getRank(), -1);
            boolean new_parent_exists = checkIfNodeExists(new_parent_id);
            if(new_parent_exists)
            {
                logger.debug("New Parent exists as a node");
                createChildParentRelation(new_parent_id, new_nodeGeneratedId);
                deleteOldParentRelation(new_nodeGeneratedId, old_parent_id);
                if (!parenthasNodeId(old_parent_id))
                {
                    logger.debug("Old parent is placeholder");
                    deleteNode(old_parent_id);
                }
                return 200;
            }
            else
            {
                logger.debug("Missing parent is sent to parser");
                return 400;
            }


        }
        else
        {
            logger.debug("This node already exists");
            return 400;
        }

    }

}
