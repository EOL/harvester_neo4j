package org.bibalex.eol.neo4j.api;

import org.bibalex.eol.neo4j.backend_api.Neo4jTree;
import org.bibalex.eol.neo4j.hbase.HbaseData;
import org.bibalex.eol.neo4j.indexer.Neo4jIndexer;
import org.bibalex.eol.neo4j.models.NodeData;
import org.bibalex.eol.neo4j.parser.*;
import org.bibalex.eol.neo4j.models.Node;
import org.json.simple.JSONObject;
import org.neo4j.driver.v1.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class NodesService {
    Neo4jIndexer indexer = new Neo4jIndexer();
    Neo4jCommon parser = new Neo4jCommon();
    Neo4jParentFormat pNode = new Neo4jParentFormat();
    Neo4jAncestryFormat aNode = new Neo4jAncestryFormat();
    HbaseData hbaseData = new HbaseData();
    NodeData nodeData = new NodeData();
    Neo4jTree forest = new Neo4jTree();
    TaxonMatching TaxonM = new TaxonMatching();
    Logger logger = LoggerFactory.getLogger(Neo4jCommon.class);

    public int createNode(Node n)
    {
        int generatedNodeId =  parser.createAcceptedNode(n.getResourceId(), n.getNodeId(),n.getScientificName(), n.getRank(),
                n.getParentGeneratedNodeId(), n.getPageId());
        n.setGeneratedNodeId(generatedNodeId);
        return n.getGeneratedNodeId();
    }

    public int createSynonym(Node n)
    {
        int generatedNodeId =  parser.createSynonymNode(n.getResourceId(), n.getNodeId(), n.getScientificName(),
                n.getRank(), n.getAcceptedNodeId(), n.getAcceptedNodeGeneratedId());
        n.setGeneratedNodeId(generatedNodeId);
        return n.getGeneratedNodeId();
    }


    public int createParentNode(Node n)
    {
        int generatedNodeId =  pNode.createParentWithPlaceholder(n.getResourceId(), n.getParentNodeId());
        n.setGeneratedNodeId(generatedNodeId);
        return n.getGeneratedNodeId();
    }

    public int createAncestorNode(Node n)
    {
        int generatedNodeId =  aNode.createAncestorIfNotExist(n.getResourceId(), n.getScientificName(),
                n.getRank(), n.getNodeId(), n.getParentGeneratedNodeId(), n.getPageId());
        n.setGeneratedNodeId(generatedNodeId);
        return n.getGeneratedNodeId();
    }

    public int createNodewithFulldata(Node n)
    {
        int generatedNodeId = parser.createNodewithFulldata(n.getResourceId(), n.getNodeId(), n.getScientificName(),
                n.getRank(), n.getParentGeneratedNodeId(), n.getPageId());
        return generatedNodeId;
    }

    public int deleteNodeAncestoryFormat(Node n)
    {
        int node_deleted_id  = aNode.deleteNodeAncestoryFormat(n.getNodeId(), n.getResourceId(), n.getScientificName());
        return node_deleted_id;
    }

    public int deleteNodeParentFormat(Node n)
    {
        int node_deleted_id  = pNode.deleteNodeParentFormat(n.getNodeId(), n.getResourceId(), n.getScientificName());
        return node_deleted_id;
    }

    public int updateNodeParentFormat(Node new_node, String parent)
    {
        int result =  pNode.UpdateNodeParentFormat(new_node, parent);
        return result;
    }

    public boolean updateNodeAncestoryFormat(ArrayList<Node> nodes)
    {
        boolean result = aNode.UpdateNodeAncestoryFormat(nodes);
        return result;
    }

    public boolean createRelationBetweenNodeAndSynonyms(Node n)
    {
        boolean created = parser.createRelationBetweenNodeAndSynonyms(n.getAcceptedNodeGeneratedId(), n.getGeneratedNodeId());
        return created;
    }

    public int getNode(Node n)
    {
        int generatedNodeId = parser.getNodeIfExist(n.getNodeId(),n.getResourceId());
        return generatedNodeId;
    }


    public int getAcceptedNode(Node n)
    {
        int generatedNodeId = parser.getAcceptedNodeIfExist(n.getNodeId(),n.getScientificName(),n.getResourceId());
        return generatedNodeId;
    }

    public int getSynonymNode(Node n)
    {
        int generatedNodeId = parser.getSynonymNodeIfExist(n.getNodeId(), n.getScientificName(),
                n.getResourceId(), n.getAcceptedNodeId(), n.getAcceptedNodeGeneratedId());
        return generatedNodeId;
    }


    public NodeData getData(String generatedNodeId)
    {
        ArrayList<String> ancestors = hbaseData.getAncestors(Integer.parseInt(generatedNodeId));
        ArrayList<String> children = hbaseData.getChildren(Integer.parseInt(generatedNodeId));
        ArrayList<String> synonyms = hbaseData.getSynonyms(Integer.parseInt(generatedNodeId));
        nodeData.setData(ancestors,children,synonyms);
        return nodeData;
    }

    public ArrayList<Neo4jTree> getUpdates(String timestamp)
    {
        ArrayList<Neo4jTree> trees=forest.getTreeUpdates(timestamp);
        return trees;
    }

    public ArrayList<JSONObject> getJson(int[] generatedNodeIds) {
        ArrayList<JSONObject> nodes = indexer.Neo4jToJson(generatedNodeIds);
        return nodes;

    }

    public ArrayList<Neo4jTree> getResourceTrees(int resourceId)
    {
        ArrayList<Neo4jTree> trees=forest.getTrees(resourceId);
        return trees;
    }

    public HashMap<Integer,Integer> getParentNodes(ArrayList<Integer> nodeIds)
    {
        HashMap<Integer,Integer> ParentNodes = new HashMap<>();
        nodeIds.forEach((nodeId) -> {
            int parentGeneratedNodeId;
            parentGeneratedNodeId = parser.getParent((int)nodeId);
            ParentNodes.put(nodeId,parentGeneratedNodeId);
        });
        return ParentNodes;
    }

    public ArrayList<Node> getRoots(int resourceId)
    {
        ArrayList<Node> roots = TaxonM.getRootNodes(resourceId);
        return roots;
    }

    public ArrayList<Node> getAncestors(int generatedNodeId)
    {
        ArrayList<Node> ancestors = TaxonM.getAncestorsNodes(generatedNodeId);
        return ancestors;
    }

    public ArrayList<Node> getChildren(int generatedNodeId)
    {
        ArrayList<Node> children = TaxonM.getChildrenNode(generatedNodeId);
        return children;
    }

    public boolean hasChildren(int generatedNodeId)
    {
        boolean children = parser.hasChildren(generatedNodeId);
        return children;
    }

    public boolean addPageIdtoNode(int generatedNodeId , int pageId)
    {
        boolean flag = TaxonM.addPageIdtoNode(generatedNodeId,pageId);
        return flag;
    }

    public int addPageIdtoNode(int generatedNodeId) {
        int pageId = parser.getPageId();
        if(TaxonM.addPageIdtoNode(generatedNodeId, pageId))
            return pageId;
        else
            return -1;
    }

    public ArrayList<Node> getNativeVirusNode() {
        return parser.getLabeledNodesByAttribute("", Constants.VIRUS_LABEL, new ArrayList<String>());
    }

    public boolean setNativeVirusNode(int generatedNodeId) {
        return TaxonM.setNodeLabel(generatedNodeId, Constants.VIRUS_LABEL);
    }

    public ArrayList<Node> getNodesByAttribute(String attribute, ArrayList<String> ids) {
        return parser.getLabeledNodesByAttribute(attribute, "", ids);
    }

    public ArrayList<Node> getSynonyms(int generatedNodeId)
    {
        ArrayList<Node> synonyms = TaxonM.getSynonyms(generatedNodeId);
        return synonyms;
    }

    public int getNodePageId(int generatedNodeId) {
        ArrayList<String> vals = new ArrayList<String>();
        vals.add(generatedNodeId + "");
        ArrayList<Node> nodes = parser.getLabeledNodesByAttribute(Constants.NODE_ATTRIBUTE_GENERATEDID, Constants.HAS_PAGE_LABEL, vals);
        return (nodes.size() > 0)? nodes.get(0).getPageId() : 0;
    }

    public List<HashMap<Integer, Integer>> getNodeAncestors(List<Integer> generatedNodesIds) {
        return parser.getNodeAncestors(generatedNodesIds);
    }

    public int updateAcceptedNode(Node n)
    {
        int generatedNodeId =  parser.updateAcceptedNode(n.getResourceId(), n.getNodeId(),
                n.getParentGeneratedNodeId(), n.getScientificName());
        return generatedNodeId;
    }
}
