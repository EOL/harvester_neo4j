package org.bibalex.eol.neo4j.api;

//import org.bibalex.eol.neo4j.backend_api.Neo4jForest;
import org.bibalex.eol.neo4j.backend_api.Neo4jTree;
import org.bibalex.eol.neo4j.hbase.HbaseData;
import org.bibalex.eol.neo4j.indexer.Neo4jIndexer;
import org.bibalex.eol.neo4j.models.NodeData;
import org.bibalex.eol.neo4j.parser.Neo4jAncestryFormat;
import org.bibalex.eol.neo4j.parser.Neo4jCommon;
import org.bibalex.eol.neo4j.models.Node;
import org.bibalex.eol.neo4j.parser.Neo4jParentFormat;
import org.json.simple.JSONObject;
import org.neo4j.driver.v1.Session;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class NodesService {
    Neo4jIndexer indexer = new Neo4jIndexer();
    Neo4jCommon parser = new Neo4jCommon();
    Neo4jParentFormat pNode = new Neo4jParentFormat();
    Neo4jAncestryFormat aNode = new Neo4jAncestryFormat();
    HbaseData hbaseData = new HbaseData();
    NodeData nodeData = new NodeData();
    Neo4jTree forest = new Neo4jTree();


    public int createNode(Node n)
    {
       int generatedNodeId =  parser.createAcceptedNode(n.getResourceId(), n.getNodeId(),n.getScientificName(), n.getRank(),
                     n.getParentGeneratedNodeId());
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
               n.getRank(), n.getNodeId(), n.getParentGeneratedNodeId());
       n.setGeneratedNodeId(generatedNodeId);
       return n.getGeneratedNodeId();
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
         ArrayList<Neo4jTree> trees=forest.getTrees(timestamp);
         return trees;
    }


    public ArrayList<JSONObject> getJson(int[] generatedNodeIds) {
        ArrayList<JSONObject> nodes = indexer.Neo4jToJson(generatedNodeIds);
        return nodes;

    }


}
