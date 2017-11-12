package org.bibalex.eol.neo4j.parser;

import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;

public class Neo4jAncestoryFormat extends Neo4jCommon {

    public void createIfNotExistNode_ancestryFormat(Transaction tx, int resourceId, String scientificName, String rank, String taxonId,
                                                    ArrayList currentAncestry){

       //Search for the node using resourceid, scientificnbame rand and ancestoters (done in next funxtion)
        // if doesnot exist create one here in this function
    }

    // This returns either the generated node id if exists or -1 if not exist
    public int getNodeIfExist_ancestryFormat(String scientificName, String rank, ArrayList ancestry){
        //TODO call neo4j and return true if it exists; false otherwise
        return 1;
    }

    public boolean updateNode_ancestryFormat(String nodeId, int generatedNodeId){
        //TODO call neo4j and update the value of the nodeid
        return true;
    }

}
