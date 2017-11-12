package org.bibalex.eol.neo4j.models;

import java.io.Serializable;

public class Node implements Serializable{

    String nodeId;
    int resourceId;
    String scientificName;
    int generatedNodeId;
    String rank;
    int parentGeneratedNodeId;
    String parentNodeId;
    String acceptedNodeId;
    int acceptedNodeGeneratedId;

//    public Node(String nodeId, int resourceId, String scientificName, int generatedNodeId, String rank, int parentGeneratedNodeId,
//                String parentNodeId, String acceptedNodeId, int acceptedNodeGeneratedId) {
//        this.nodeId = nodeId;
//        this.resourceId = resourceId;
//        this.scientificName = scientificName;
//        this.generatedNodeId = generatedNodeId;
//        this.rank = rank;
//        this.parentGeneratedNodeId = parentGeneratedNodeId;
//        this.parentNodeId = parentNodeId;
//        this.acceptedNodeId = acceptedNodeId;
//        this.acceptedNodeGeneratedId = acceptedNodeGeneratedId;
//    }
//
//    public Node(int resourceId, String nodeId, String scientificName, String rank, int parentGeneratedNodeId) {
//        this.nodeId = nodeId;
//        this.resourceId = resourceId;
//        this.scientificName = scientificName;
//        this.rank = rank;
//        this.parentGeneratedNodeId = parentGeneratedNodeId;
//    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public int getGeneratedNodeId() {
        return generatedNodeId;
    }

    public void setGeneratedNodeId(int generatedNodeId) {
        this.generatedNodeId = generatedNodeId;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public int getParentGeneratedNodeId() {
        return parentGeneratedNodeId;
    }

    public void setParentGeneratedNodeId(int parentGeneratedNodeId) {
        this.parentGeneratedNodeId = parentGeneratedNodeId;
    }

    public String getParentNodeId() {
        return parentNodeId;
    }

    public void setParentNodeId(String parentNodeId) {
        this.parentNodeId = parentNodeId;
    }

    public String getAcceptedNodeId() {
        return acceptedNodeId;
    }

    public void setAcceptedNodeId(String acceptedNodeId) {
        this.acceptedNodeId = acceptedNodeId;
    }

    public int getAcceptedNodeGeneratedId() {
        return acceptedNodeGeneratedId;
    }

    public void setAcceptedNodeGeneratedId(int acceptedNodeGeneratedId) {
        this.acceptedNodeGeneratedId = acceptedNodeGeneratedId;
    }
}
