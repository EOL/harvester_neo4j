package org.bibalex.eol.neo4j.api;

import org.bibalex.eol.neo4j.hbase.HbaseData;
import org.bibalex.eol.neo4j.models.NodesData;
import org.bibalex.eol.neo4j.parser.Neo4jCommon;
import org.bibalex.eol.neo4j.models.Node;
import org.neo4j.driver.v1.Session;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class NodesService {
    Neo4jCommon parser = new Neo4jCommon();
    HbaseData hbaseData = new HbaseData();
    NodesData nodesData = new NodesData();

    public int createNode(Node n)
    {
       int generatedNodeId =  parser.createAcceptedNode(n.getResourceId(), n.getNodeId(),n.getScientificName(), n.getRank(),
                     n.getParentGeneratedNodeId());
       n.setGeneratedNodeId(generatedNodeId);
       return n.getGeneratedNodeId();
    }

    public int getNode(Node n)
    {
        int generatedNodeId = parser.getNodeIfExist(n.getNodeId(),n.getResourceId());
        return generatedNodeId;
    }


    public NodesData getData(String generatedNodeId)
    {
       ArrayList<String> ancestors = hbaseData.getAncestors(Integer.parseInt(generatedNodeId));
       ArrayList<String> children = hbaseData.getChildren(Integer.parseInt(generatedNodeId));
       ArrayList<String> synonyms = hbaseData.getSynonyms(Integer.parseInt(generatedNodeId));
       nodesData.setData(ancestors,children,synonyms);
       return nodesData;
    }

}
