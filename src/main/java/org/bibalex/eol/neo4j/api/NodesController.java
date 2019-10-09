package org.bibalex.eol.neo4j.api;

import org.bibalex.eol.neo4j.backend_api.Neo4jTree;
import org.bibalex.eol.neo4j.models.NodeData;
import org.bibalex.eol.neo4j.parser.Constants;
import org.json.simple.JSONObject;

//import org.neo4j.register.Register;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.bibalex.eol.neo4j.models.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/neo4j")
public class NodesController {

    @Autowired
    private NodesService service;
    private static final Logger logger = LoggerFactory.getLogger(NodesController.class);


    @RequestMapping(value = "/createNode", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int createNode(@RequestBody Node n) {
        int generatedNodeId = service.createNode(n);
        logger.info("Created Node with Generated Node Id: " + generatedNodeId);
        return generatedNodeId;
    }

    @RequestMapping(value = "/createSynonymNode", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int createSynonym(@RequestBody Node n) {
        int generatedNodeId = service.createSynonym(n);
        logger.info("Created Synonym Node with Generated Node Id: " + generatedNodeId);
        return generatedNodeId;
    }

    @RequestMapping(value = "/createSynonymRelation", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public boolean createSynonymRelation(@RequestBody Node n) {
        boolean created = service.createRelationBetweenNodeAndSynonyms(n);
        if (created)
            logger.info("Created Synonym Relation Successfully");
        else
            logger.info("Couldn't Create Synonym Relation");
        return created;
    }

    @RequestMapping(value = "/createAncestorNode", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int createAncestorNode(@RequestBody Node n) {
        int generatedNodeId = service.createAncestorNode(n);
        if (generatedNodeId == -1)
            logger.info("Failed to Create Ancestor Node");
        else
            logger.info("Created Ancestor Node with Generated Node Id: " + generatedNodeId);
        return generatedNodeId;
    }

    /**
     * @param n the node that has a parent not created before
     * @return the generated auto id of the new created parent
     */
    @RequestMapping(value = "/createParentWithPlaceholder", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int createParentNode(@RequestBody Node n) {
        int generatedNodeId = service.createParentNode(n);
        if (generatedNodeId == -1)
            logger.info("Couldn't Create Parent Node");
        else
            logger.info("Created Parent Node with Generated Node Id: " + generatedNodeId);
        return generatedNodeId;
    }

    @RequestMapping(value = "/createNodewithFulldata", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int createNodewithFulldata(@RequestBody Node n) {
        int generatedNodeId = service.createNodewithFulldata(n);
        logger.info("Created Full Data of Node with Generated Node Id: " + generatedNodeId);
        return generatedNodeId;
    }

    @RequestMapping(value = "/deleteNodeAncestoryFormat", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int deleteNodeAncestorForm(@RequestBody Node n) {
        int node_deleted_id = service.deleteNodeAncestoryFormat(n);
        if (node_deleted_id == -1)
            logger.info("Couldn't Delete Ancestry Format of Node: " + n.getGeneratedNodeId());
        else
            logger.info("Deleted Ancestry Format of Node: " + n.getGeneratedNodeId());
        return node_deleted_id;
    }


    @RequestMapping(value = "/deleteNodeParentFormat", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int deleteNodeParentForm(@RequestBody Node n) {
        int node_deleted_id = service.deleteNodeParentFormat(n);
        if (node_deleted_id == -1)
            logger.info("Couldn't Delete Parent Format of Node: " + n.getGeneratedNodeId());
        else
            logger.info("Deleted Parent Format of Node: " + n.getGeneratedNodeId());
        return node_deleted_id;
    }

    @RequestMapping(value = "/updateNodeParentFormat/{parentNodeId}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int updateNodeParentForm(@RequestBody Node n, @PathVariable("parentNodeId") String parentNodeId) {
        int result = service.updateNodeParentFormat(n, parentNodeId);
        switch (result) {
            case (0):
                logger.info("Update Parent Format Failed for Node: " + n.getGeneratedNodeId());
            case (1):
                logger.info("Updated Only Scientific Name or Rank for Node: " + n.getGeneratedNodeId());
            case (200): {
                logger.info("Couldn't Find Node with Given New Parent ID");
                logger.info("The New Parent Exists as a Node");
                logger.info("Created ChildParent Relation between New Parent and Node: " + n.getGeneratedNodeId());
            }
            case (400): {
                logger.info("Couldn't Find Node with Given New Parent ID");
                logger.info("The New Parent Doesn't Exist as a Node, Creating New Node");
                logger.info("Created ChildParent Relation between New Parent and Node: " + n.getGeneratedNodeId());
            }
        }
        return result;
    }

    @RequestMapping(value = "/updateNodeAncestoryFormat", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public boolean updateNodeAncestoryFormat(@RequestBody ArrayList<Node> nodes) {
        boolean result = service.updateNodeAncestoryFormat(nodes);
        if (result)
            logger.info("Successfully Updated Ancestry Format for Nodes");
        else
            logger.info("Couldn't Update Ancestry Format for Nodes");
        return result;
    }


    @RequestMapping(value = "/getNode", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int getNode(@RequestBody Node n) {
        int generatedNodeId = service.getNode(n);
        if (generatedNodeId == -1)
            logger.info("Couldn't Find Node");
        else
            logger.info("Found Node: " + generatedNodeId);
        return generatedNodeId;
    }

    @RequestMapping(value = "/getAcceptedNode", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int getAcceptedNode(@RequestBody Node n) {
        int generatedNodeId = service.getAcceptedNode(n);
        if (generatedNodeId == -1)
            logger.info("Couldn't Find Accepted Node");
        else
            logger.info("Found Accepted Node: " + generatedNodeId);
        return generatedNodeId;
    }


    @RequestMapping(value = "/getSynonymNode", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int getSynonymNode(@RequestBody Node n) {
        int generatedNodeId = service.getSynonymNode(n);
        if (generatedNodeId == -1)
            logger.info("Couldn't Find Synonym Node");
        else
            logger.info("Found Synonym Node: " + generatedNodeId);
        return generatedNodeId;
    }

    @RequestMapping(value = "/getNodeData/{generatedNodeId}", method = RequestMethod.GET)
    public NodeData getNodeData(@PathVariable("generatedNodeId") String generatedNodeId) {
        NodeData data = service.getData(generatedNodeId);
        logger.info("Got Data for Node: " + generatedNodeId);
        return data;
    }

    @RequestMapping(value = "/getNeo4jUpdates", method = RequestMethod.POST, consumes = "text/plain")
    public ArrayList<Neo4jTree> getChangesfromTimestamp(@RequestBody String timestamp) {
        ArrayList<Neo4jTree> trees = service.getUpdates(timestamp);
        logger.info("Got Neo4j Updates from Start Timestamp: " + timestamp);
        return trees;
    }

    @RequestMapping(value = "/getResourceTrees/{resourceId}", method = RequestMethod.GET)
    public ArrayList<Neo4jTree> getResourceTrees(@PathVariable("resourceId") int resourceId) {
        ArrayList<Neo4jTree> trees = service.getResourceTrees(resourceId);
        logger.info("Got Resource Trees for Resource: " + resourceId);
        return trees;
    }

    @RequestMapping(value = "/getNodesJson", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ArrayList<JSONObject> getNodesJson(@RequestBody int[] generatedNodeIds) throws IOException {
        ArrayList<JSONObject> nodes = service.getJson(generatedNodeIds);
        logger.info("Got JSON for Nodes: " + generatedNodeIds);
        return nodes;
    }

    @RequestMapping(value = "/getParentsOfNodes", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    public HashMap<Integer, Integer> getParentNodes(@RequestBody ArrayList<Integer> nodeIds) {
        HashMap<Integer, Integer> parents = service.getParentNodes(nodeIds);
        logger.info("Got Parents Nodes in a HashMap");
        logger.debug(String.valueOf(parents));
        return parents;
    }

    @RequestMapping(value = "/getRootNodes/{resourceId}", method = RequestMethod.GET, produces = "application/json")
    public ArrayList<Node> getRootNodes(@PathVariable("resourceId") int resourceId) {
        ArrayList<Node> roots = service.getRoots(resourceId);
        logger.info("Got Root Nodes of Resource: " + resourceId);
        logger.debug(String.valueOf(roots));
        return roots;
    }

    @RequestMapping(value = "getNodesWithPlaceholder/{resourceId}", method = RequestMethod.GET, produces = "application/json")
    public List<Node> getNodesWithPlaceholder(@PathVariable("resourceId") int resourceId) {
        List<Node> nodes = service.getNodesWithPlaceholder(resourceId);
        logger.info("Got Nodes with Placeholder of Resource: " + resourceId);
        logger.debug(String.valueOf(nodes));
        return nodes;
    }

    @RequestMapping(value = "/getAncestors/{generatedNodeId}", method = RequestMethod.GET, produces = "application/json")
    public ArrayList<Node> getAncestors(@PathVariable("generatedNodeId") int generatedNodeId) {
        ArrayList<Node> ancestors = service.getAncestors(generatedNodeId);
        logger.info("Get Ancestors of Node: " + generatedNodeId);
        logger.debug(String.valueOf(ancestors));
        return ancestors;
    }

    @RequestMapping(value = "/getChildren/{generatedNodeId}", method = RequestMethod.GET, produces = "application/json")
    public ArrayList<Node> getchildren(@PathVariable("generatedNodeId") int generatedNodeId) {
        ArrayList<Node> children = service.getChildren(generatedNodeId);
        logger.info("Get Children of Node: " + generatedNodeId);
        logger.debug(String.valueOf(children));
        return children;
    }

    @RequestMapping(value = "/hasChildren/{generatedNodeId}", method = RequestMethod.GET, produces = "application/json")
    public boolean hasChildren(@PathVariable("generatedNodeId") int generatedNodeId) {
        boolean children = service.hasChildren(generatedNodeId);
        if (children)
            logger.info("Node: " + generatedNodeId + " Has Children");
        else
            logger.info("Node: " + generatedNodeId + " Has No Children");
        return children;
    }

    @RequestMapping(value = "/addPageIdtoNode/{generatedNodeId}/{pageId}", method = RequestMethod.GET, produces = "application/json")
    public boolean addPageIdtoNode(@PathVariable("generatedNodeId") int generatedNodeId, @PathVariable("pageId") int pageId) {

        boolean flag = service.addPageIdtoNode(generatedNodeId, pageId);
        if (flag)
            logger.info("Added Page ID: " + pageId + " to Node: " + generatedNodeId + " Successfully");
        else
            logger.info("Failed to Add Page ID: " + pageId + " to Node: " + generatedNodeId);
        return flag;

    }

    @RequestMapping(value = "/addPagestoNode", method = RequestMethod.POST, consumes = "application/json")
    public boolean addPagestoNode(@RequestBody HashMap<Integer, Integer> results) throws IOException {
        boolean flag = service.addPagestoNode(results);
        if (flag) {
            logger.info("Added Page IDs to Nodes Successfully");
            logger.debug(String.valueOf(results));
        } else
            logger.info("Failed to Add Page IDs to Nodes");
        return flag;
    }

    @RequestMapping(value = "/addPageIdtoNode/{generatedNodeId}", method = RequestMethod.GET, produces = "application/json")
    public int addPageIdtoNode(@PathVariable("generatedNodeId") int generatedNodeId) {
        int pageId = service.addPageIdtoNode(generatedNodeId);
        logger.info("Added Page ID: " + pageId + " to Node: " + generatedNodeId + " Successfully");
        return pageId;
    }

    @RequestMapping(value = "/getNodePageId/{generatedNodeId}", method = RequestMethod.GET, produces = "application/json")
    public int getNodePageId(@PathVariable("generatedNodeId") int generatedNodeId) {
        int pageId = service.getNodePageId(generatedNodeId);
        logger.info("Node: " + generatedNodeId + " Has Page ID: " + pageId);
        return pageId;
    }

    @RequestMapping(value = "/getNativeVirusNode", method = RequestMethod.GET, produces = "application/json")
    public ArrayList<Node> getNativeVirusNode() {
        ArrayList<Node> nodeList = service.getNativeVirusNode();
        logger.info("Got Native Virus Nodes");
        logger.debug(String.valueOf(nodeList));
        return nodeList;
    }

    @RequestMapping(value = "/setNativeVirusNode/{generatedNodeId}", method = RequestMethod.POST)
    public boolean setNativeVirusNode(@PathVariable("generatedNodeId") int generatedNodeId) {
        boolean result = service.setNativeVirusNode(generatedNodeId);
        if (result)
            logger.info("Native Virus Node Set Successfully for Node: " + generatedNodeId);
        else
            logger.info("Failed to Set Native Virus Node for Node: " + generatedNodeId);
        return result;
    }

    /**
     * @param type
     * @param resourceId
     * @return integer harvest status 1 for success, 2 for failure, and 3 for unsupported format
     */
    @RequestMapping(value = "/harvestResource/{type}/{resourceId}", method = RequestMethod.POST)
    public int harvestResource(@PathVariable("type") String type, @PathVariable("resourceId") String resourceId) {
        int responseCode = service.harvestResource(type, resourceId);
        switch (responseCode) {
            case (1):
                logger.info("Successfully Harvested Resource: " + resourceId);
            case (2):
                logger.info("Harvest Failed for Resource: " + resourceId);
            case (3):
                logger.error("Unsupported Format Type of Resource: " + resourceId + ", Harvest Failed");
        }
        return responseCode;
    }


    /**
     * fetches the nodes data by their ids.
     *
     * @param genetaredIds the ids of the wanted nodes in json format, i.e "[1,2]" in case of ids=1,2
     * @return json format of the nodes attributes.
     */
    @RequestMapping(value = "/getNodes", method = RequestMethod.POST, produces = "application/json")
    public ArrayList<Node> getNodesByGeneratedIds(@RequestBody ArrayList<String> genetaredIds) {
        ArrayList<Node> nodeList = service.getNodesByAttribute(Constants.NODE_ATTRIBUTE_GENERATEDID, genetaredIds);
        logger.info("Called Get Nodes By Generated IDs");
        logger.debug(String.valueOf(nodeList));
        return nodeList;
    }

    @RequestMapping(value = "/getSynonyms/{generatedNodeId}", method = RequestMethod.GET, produces = "application/json")
    public ArrayList<Node> getSynonyms(@PathVariable("generatedNodeId") int generatedNodeId) {
        ArrayList<Node> synonyms = service.getSynonyms(generatedNodeId);
        logger.info("Get Synonyms of Node: " + generatedNodeId);
        logger.debug(String.valueOf(synonyms));
        return synonyms;
    }

    @RequestMapping(value = "/getAncestors", method = RequestMethod.POST, produces = "application/json")
    public List<HashMap<Integer, Integer>> getNodeAncestors(@RequestBody List<Integer> generatedNodesIds) {
        List<HashMap<Integer, Integer>> ancestors = service.getNodeAncestors(generatedNodesIds);
        logger.info("Got Node Ancestors");
        logger.debug(String.valueOf(ancestors));
        return ancestors;
    }

//    @RequestMapping(value="/getPageIds", method = RequestMethod.POST, produces = "application/json")
//    public List< Integer> getPageIds(@RequestBody List<Integer> generatedNodesIds) {
//        List<Integer> pageIds = service.getPageIds(generatedNodesIds);
//        logger.info("Got Page IDs");
//        logger.debug(String.valueOf(pageIds));
//        return pageIds;
//    }

    @RequestMapping(value = "/updateAcceptedNode", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int updateAcceptedNode(@RequestBody Node n) {
        int generatedNodeId = service.updateAcceptedNode(n);
        if (generatedNodeId == -1)
            logger.info("Could Not Find Node to Update");
        else
            logger.info("Called Update Accepted Node for: " + generatedNodeId);

        return generatedNodeId;
    }
}
