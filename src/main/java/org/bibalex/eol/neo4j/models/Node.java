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
    long created_at;
    long updated_at;

    public String getNodeId()
    {
        return nodeId;
    }

    public void setNodeId(String nodeId)
    {
        this.nodeId = nodeId;
    }

    public int getResourceId()
    {
        return resourceId;
    }

    public void setResourceId(int resourceId)
    {
        this.resourceId = resourceId;
    }

    public String getScientificName()
    {
        return scientificName;
    }

    public void setScientificName(String scientificName)
    {
        this.scientificName = scientificName;
    }

    public int getGeneratedNodeId()
    {
        return generatedNodeId;
    }

    public void setGeneratedNodeId(int generatedNodeId)
    {
        this.generatedNodeId = generatedNodeId;
    }

    public String getRank()
    {
        return rank;
    }

    public void setRank(String rank)
    {
        this.rank = rank;
    }

    public int getParentGeneratedNodeId()
    {
        return parentGeneratedNodeId;
    }

    public void setParentGeneratedNodeId(int parentGeneratedNodeId)
    {
        this.parentGeneratedNodeId = parentGeneratedNodeId;
    }

    public String getParentNodeId()
    {
        return parentNodeId;
    }

    public void setParentNodeId(String parentNodeId)
    {
        this.parentNodeId = parentNodeId;
    }

    public String getAcceptedNodeId()
    {
        return acceptedNodeId;
    }

    public void setAcceptedNodeId(String acceptedNodeId)
    {

        this.acceptedNodeId = acceptedNodeId;
    }

    public int getAcceptedNodeGeneratedId()
    {
        return acceptedNodeGeneratedId;
    }

    public void setAcceptedNodeGeneratedId(int acceptedNodeGeneratedId)
    {
        this.acceptedNodeGeneratedId = acceptedNodeGeneratedId;
    }

    public long getCreated_at()
    {
        return created_at;
    }

    public void setCreated_at(long created_at)
    {
        this.created_at = created_at;
    }

    public long getUpdated_at()
    {
        return updated_at;
    }

    public void setUpdated_at(long updated_at)
    {
        this.updated_at = updated_at;
    }
}
