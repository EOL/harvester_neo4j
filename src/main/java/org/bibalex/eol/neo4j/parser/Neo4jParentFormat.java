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
                    " created_at: timestamp(),updated_at: timestamp()})" +
                    "RETURN n.generated_auto_id";
            StatementResult result = getSession().run(create_query, parameters("resourceId", resourceId,
                    "parentUsageId", parentUsageId, "scientificName", Constants.PLACE_HOLDER, "rank", Constants.PLACE_HOLDER, "autoId", autoId));
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

    public int getNodeGivenParentIfExists(int resourceId, String scientificName, String rank, String nodeId, int parentGeneratedNodeId, boolean parentNecessary)
    {
        String query = "MATCH (n {resource_id: {resourceId} , scientific_name: {scientificName}," +
                " rank: {rank}, node_id: {nodeId}})<-[r:IS_PARENT_OF]-(p {generated_auto_id: {parentGeneratedNodeId}}) RETURN n.generated_auto_id LIMIT 1 ";
        // Match the root node that has no parent
        query += (parentNecessary)? "" : "UNION MATCH (n:Root {resource_id: {resourceId} , scientific_name: {scientificName}, rank: {rank}, node_id: {nodeId}}) RETURN n.generated_auto_id";
        StatementResult result = getSession().run(query, parameters( "resourceId",resourceId,
                "scientificName", scientificName, "rank", rank ,"nodeId", nodeId, "parentGeneratedNodeId", parentGeneratedNodeId));
//        logger.debug("query:" + query);
//        logger.debug("resourceId:" + resourceId + " & scientificName:" + scientificName + " & rank:" + rank + " & nodeId:" + nodeId + " & parentGeneratedNodeId:" + parentGeneratedNodeId);
        if (result.hasNext()) {
            Record record = result.next();
            logger.debug("org.bibalex.eol.neo4j.parser.Neo4jParentFormat.getNodeGivenParentIfExists: The result of search: " +record.get("n.generated_auto_id").asInt() );
            return record.get("n.generated_auto_id").asInt();
        } else {
            logger.debug("org.bibalex.eol.neo4j.parser.Neo4jParentFormat.getNodeGivenParentIfExists: The result is -1");
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
        boolean update_scientific_name = false;
        boolean update_rank = false;
        int update_hierarchy = 0;
        int nodegeneratedId = getAcceptedNodeIfExist(new_node.getNodeId(), new_node.getScientificName(), new_node.getResourceId());
        if (nodegeneratedId == -1)
        {
            nodegeneratedId = getNodeIfExist(new_node.getNodeId(), new_node.getResourceId());
            if (nodegeneratedId != -1)
            {
                Node old_node = getNodeProperties(nodegeneratedId);
                if (!old_node.getScientificName().equals(new_node.getScientificName()))
                {
                    logger.debug("Update scientific name of the node");
                    update_scientific_name = UpdateScientificName(nodegeneratedId, new_node.getScientificName());


                }

            }
        }

        nodegeneratedId = getNodeIfExist(new_node.getNodeId(), new_node.getResourceId());
        Node old_node = getNodeProperties(nodegeneratedId);
        if (!old_node.getRank().equals(new_node.getRank()))
        {
            logger.debug("Update rank of the node");
            update_rank = UpdateRank(nodegeneratedId, new_node.getRank());

        }


        new_node.setGeneratedNodeId(nodegeneratedId);
        update_hierarchy = UpdateHierarchy(new_node, new_parent_nodeId);
        if (update_hierarchy != 0) {
            String debugMsg = (update_scientific_name || update_rank)? "Update ancestry done as well as scientific or rank" :
                    "Update ancestry done";
            logger.debug(debugMsg);
            return update_hierarchy;
        } else {
            String debugMsg = (update_scientific_name || update_rank)? "Only scientific or rank update" : "Update failed";
            logger.debug(debugMsg);
            int returnVal = (update_scientific_name || update_rank)? 1 : 0;
            return returnVal;
        }
    }

    public int UpdateHierarchy(Node new_node, String new_parent_nodeId)
    {

        int old_parent_id = getParent(new_node.getGeneratedNodeId());
        int new_parent_id = getNodeIfExist(new_parent_nodeId,new_node.getResourceId());
        int returnCode = 0;
        logger.debug("Old & new(if exists) parents generated ids:" + old_parent_id + "-" + new_parent_id);
//        Node old_parent = getNodeProperties(old_parent_id);
        int new_nodeGeneratedId = getNodeGivenParentIfExists(new_node.getResourceId(), new_node.getScientificName(),
                new_node.getRank(), new_node.getNodeId(), new_parent_id, true);
        logger.debug("Check if link exist or not with parent generated node id:" + new_nodeGeneratedId);
        if (new_nodeGeneratedId == -1)
        {
            // The updated node its parent id changed
            logger.debug("Node with this new parent is not found");
            new_nodeGeneratedId = new_node.getGeneratedNodeId();
//            boolean new_parent_exists = checkIfNodeExists(new_parent_id);
            boolean new_parent_exists = new_parent_id > 0;

            if(new_parent_exists) {
                logger.debug("New Parent exists as a node");
                createChildParentRelation(new_parent_id, new_nodeGeneratedId);
                returnCode = 200;
            } else {
                // New parent doesn't exist, so create it with placeholder and link the new node with it.
                logger.debug("Creating new parent with placeholder");
                int new_parentGenerated_id = createAcceptedNode(new_node.getResourceId(), new_parent_nodeId, Constants.PLACE_HOLDER,
                        Constants.PLACE_HOLDER, -1, -1);
                logger.debug("Creating relation btw new parent generated id(" + new_nodeGeneratedId +") and the updated node generated id(" + new_nodeGeneratedId + ")");
                createChildParentRelation(new_parentGenerated_id, new_nodeGeneratedId);
                logger.debug("Missing parent creation is sent to parser");
                returnCode = 400;
            }

            if(old_parent_id > 0) {
                logger.debug("Delete the old relation btw old parent generated id (" + old_parent_id +") and the updated node generated id(" + new_nodeGeneratedId + ")");
                deleteOldParentRelation(new_nodeGeneratedId, old_parent_id);
                // If has scientific name placeholder, not found before as record in the file
                if (!parentHasAttribute(old_parent_id, Constants.NODE_ATTRIBUTE_SCIENTIFICNAME)) {
                    logger.debug("Old parent is placeholder");
                    // delete it if it has no other children
                    // TODO make procedure for both queries
                    if(!hasChildren(old_parent_id)) deleteNode(old_parent_id);
                }
            }
        } else {
            // The updated node its parent id hasn't changed
            logger.debug("There is no update ancestry too");
        }
        return returnCode;
    }

}