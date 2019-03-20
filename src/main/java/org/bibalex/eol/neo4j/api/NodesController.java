package org.bibalex.eol.neo4j.api;

import org.bibalex.eol.neo4j.backend_api.Neo4jTree;
import org.bibalex.eol.neo4j.models.NodeData;
import org.bibalex.eol.neo4j.parser.Constants;
import org.json.simple.JSONObject;

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



    @RequestMapping(value="/createNode", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int createNode(@RequestBody Node n)
    {
        int  generatedNodeId = service.createNode(n);
        return generatedNodeId;
    }

    @RequestMapping(value="/createSynonymNode", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int createSynonym(@RequestBody Node n)
    {
        int  generatedNodeId = service.createSynonym(n);
        return generatedNodeId;
    }

    @RequestMapping(value="/createSynonymRelation", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public boolean createSynonymRelation(@RequestBody Node n)
    {
        boolean  created = service.createRelationBetweenNodeAndSynonyms(n);
        return created;
    }

    @RequestMapping(value="/createAncestorNode", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int createAncestorNode(@RequestBody Node n)
    {
        int  generatedNodeId = service.createAncestorNode(n);
        return generatedNodeId;
    }

    /**
     *
     * @param n the node that has a parent not created before
     * @return the generated auto id of the new created parent
     */
    @RequestMapping(value="/createParentWithPlaceholder", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int createParentNode(@RequestBody Node n)
    {
        int  generatedNodeId = service.createParentNode(n);
        return generatedNodeId;
    }

    @RequestMapping(value="/createNodewithFulldata", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int createNodewithFulldata(@RequestBody Node n)
    {
        int  generatedNodeId = service.createNodewithFulldata(n);
        return generatedNodeId;
    }

    @RequestMapping(value="/deleteNodeAncestoryFormat", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int deleteNodeAncestorForm(@RequestBody Node n)
    {
        int node_deleted_id = service.deleteNodeAncestoryFormat(n);
        return node_deleted_id;
    }


    @RequestMapping(value="/deleteNodeParentFormat", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int deleteNodeParentForm(@RequestBody Node n)
    {
        int node_deleted_id = service.deleteNodeParentFormat(n);
        return node_deleted_id;
    }

    @RequestMapping(value="/updateNodeParentFormat/{parentNodeId}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int updateNodeParentForm(@RequestBody Node n , @PathVariable("parentNodeId") String parentNodeId)
    {
        int result = service.updateNodeParentFormat(n,parentNodeId);
        return result;
    }

    @RequestMapping(value = "/updateNodeAncestoryFormat", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public boolean updateNodeAncestoryFormat(@RequestBody ArrayList<Node> nodes)
    {
        boolean result = service.updateNodeAncestoryFormat(nodes);
        return result;
    }


    @RequestMapping(value="/getNode", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int getNode(@RequestBody Node n)
    {
        int generatedNodeId =  service.getNode(n);
        return generatedNodeId;
    }

    @RequestMapping(value="/getAcceptedNode", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int getAcceptedNode(@RequestBody Node n)
    {
        int generatedNodeId =  service.getAcceptedNode(n);
        return generatedNodeId;
    }


    @RequestMapping(value="/getSynonymNode", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int getSynonymNode(@RequestBody Node n)
    {
        int generatedNodeId =  service.getSynonymNode(n);
        return generatedNodeId;
    }

    @RequestMapping(value="/getNodeData/{generatedNodeId}", method = RequestMethod.GET)
    public NodeData getNodeData(@PathVariable("generatedNodeId") String generatedNodeId)
    {
        NodeData data = service.getData(generatedNodeId);
        return data;
    }

    @RequestMapping(value="/getNeo4jUpdates", method = RequestMethod.POST, consumes = "text/plain")
    public ArrayList<Neo4jTree> getChangesfromTimestamp(@RequestBody String timestamp )
    {
        ArrayList<Neo4jTree> trees = service.getUpdates(timestamp);
        return trees;
    }

    @RequestMapping(value="/getResourceTrees/{resourceId}", method = RequestMethod.GET)
    public ArrayList<Neo4jTree> getResourceTrees(@PathVariable("resourceId") int resourceId) {
        ArrayList<Neo4jTree> trees = service.getResourceTrees(resourceId);
        return trees;
    }

    @RequestMapping(value="/getNodesJson", method = RequestMethod.POST , consumes = "application/json", produces = "application/json")
    public ArrayList<JSONObject> getNodesJson(@RequestBody int[] generatedNodeIds) throws IOException {
        ArrayList<JSONObject> nodes = service.getJson(generatedNodeIds);
        return nodes;
    }

    @RequestMapping(value="/getParentsOfNodes", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    public HashMap<Integer,Integer> getParentNodes(@RequestBody ArrayList<Integer> nodeIds)
    {
        HashMap<Integer,Integer> parents = service.getParentNodes(nodeIds);
        return parents;
    }

    @RequestMapping(value="/getRootNodes/{resourceId}", method = RequestMethod.GET, produces = "application/json")
    public ArrayList<Node> getRootNodes(@PathVariable("resourceId") int resourceId)
    {
        ArrayList <Node> roots = service.getRoots(resourceId);
        return roots;
    }

    @RequestMapping(value="/getAncestors/{generatedNodeId}", method = RequestMethod.GET, produces = "application/json")
    public ArrayList<Node> getAncestors(@PathVariable("generatedNodeId") int generatedNodeId)
    {
        ArrayList<Node> ancestors = service.getAncestors(generatedNodeId);
        return ancestors;
    }

    @RequestMapping(value="/getChildren/{generatedNodeId}", method = RequestMethod.GET, produces = "application/json")
    public ArrayList<Node> getchildren(@PathVariable("generatedNodeId") int generatedNodeId)
    {
        ArrayList<Node> children = service.getChildren(generatedNodeId);
        return children;
    }

    @RequestMapping(value="/hasChildren/{generatedNodeId}", method = RequestMethod.GET, produces = "application/json")
    public boolean hasChildren(@PathVariable("generatedNodeId") int generatedNodeId)
    {
        boolean children = service.hasChildren(generatedNodeId);
        return children;
    }

    @RequestMapping(value="/addPageIdtoNode/{generatedNodeId}/{pageId}", method = RequestMethod.GET, produces = "application/json")
    public boolean addPageIdtoNode( @PathVariable("generatedNodeId") int generatedNodeId , @PathVariable("pageId") int pageId )
    {

        boolean flag =  service.addPageIdtoNode(generatedNodeId,pageId);
        return flag;

    }

    @RequestMapping(value = "/addPagestoNode", method = RequestMethod.POST, consumes = "application/json")
    public boolean addPagestoNode(@RequestBody HashMap<Integer,Integer> results) throws IOException
    {
        boolean flag = service.addPagestoNode(results);
        return flag;
    }

    @RequestMapping(value="/addPageIdtoNode/{generatedNodeId}", method = RequestMethod.GET, produces = "application/json")
    public int addPageIdtoNode(@PathVariable("generatedNodeId") int generatedNodeId) {
        return service.addPageIdtoNode(generatedNodeId);
    }

    @RequestMapping(value="/getNodePageId/{generatedNodeId}", method = RequestMethod.GET, produces = "application/json")
    public int getNodePageId(@PathVariable("generatedNodeId") int generatedNodeId) {
        return service.getNodePageId(generatedNodeId);
    }

    @RequestMapping(value="/getNativeVirusNode", method = RequestMethod.GET, produces = "application/json")
    public ArrayList<Node> getNativeVirusNode() {
        return service.getNativeVirusNode();
    }

    @RequestMapping(value="/setNativeVirusNode/{generatedNodeId}", method = RequestMethod.POST)
    public boolean setNativeVirusNode(@PathVariable("generatedNodeId") int generatedNodeId) {
        return service.setNativeVirusNode(generatedNodeId);
    }

    /**
     *
     * @param type
     * @param resourceId
     * @return integer harvest status 1 for success, 2 for failure, and 3 for unsupported format
     */
    @RequestMapping(value="/harvestResource/{type}/{resourceId}", method = RequestMethod.POST)
    public int harvestResource(@PathVariable("type") String type, @PathVariable("resourceId") String resourceId) {
        return service.harvestResource(type, resourceId);
    }


    /**
     * fetches the nodes data by their ids.
     * @param genetaredIds the ids of the wanted nodes in json format, i.e "[1,2]" in case of ids=1,2
     * @return json format of the nodes attributes.
     */
    @RequestMapping(value="/getNodes", method = RequestMethod.POST, produces = "application/json")
    public ArrayList<Node> getNodesByGeneratedIds(@RequestBody ArrayList<String> genetaredIds) {
        return service.getNodesByAttribute(Constants.NODE_ATTRIBUTE_GENERATEDID, genetaredIds);
    }

    @RequestMapping(value="/getSynonyms/{generatedNodeId}", method = RequestMethod.GET, produces = "application/json")
    public ArrayList<Node> getSynonyms(@PathVariable("generatedNodeId") int generatedNodeId)
    {
        ArrayList<Node> synonyms = service.getSynonyms(generatedNodeId);
        return synonyms;
    }

    @RequestMapping(value="/getAncestors", method = RequestMethod.POST, produces = "application/json")
    public List<HashMap<Integer, Integer>> getNodeAncestors(@RequestBody List<Integer> generatedNodesIds) {
        return service.getNodeAncestors(generatedNodesIds);
    }

    @RequestMapping(value="/getPageIds", method = RequestMethod.POST, produces = "application/json")
    public List< Integer> getPageIds(@RequestBody List<Integer> generatedNodesIds) {
        return service.getPageIds(generatedNodesIds);
    }

    @RequestMapping(value="/updateAcceptedNode", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int updateAcceptedNode(@RequestBody Node n)
    {
        int generatedNodeId = service.updateAcceptedNode(n);
        return generatedNodeId;
    }
}
