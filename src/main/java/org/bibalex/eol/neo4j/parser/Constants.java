package org.bibalex.eol.neo4j.parser;

import java.util.HashSet;

/**
 * Created by maha.mostafa on 5/23/18.
 */
public class Constants {

    public static final String VIRUS_LABEL = "Virus";
    public static final String NODE_LABEL = "Node";
    public static final String ROOT_LABEL = "Root";
    public static final String HAS_PAGE_LABEL = "Has_Page";

    public static final String NODE_ATTRIBUTE_GENERATEDID = "generated_auto_id";
    public static final String NODE_ATTRIBUTE_SCIENTIFICNAME = "scientific_name";
    public static final String NODE_ATTRIBUTE_NODEID = "node_id";
    public static final String NODE_ATTRIBUTE_PARENT_NODEID = "parent_node_id";
    public static final String NODE_ATTRIBUTE_ACCEPTED_NODEID = "accepted_node_id";
    public static final String NODE_ATTRIBUTE_RESOURCEID = "resource_id";
    public static final String NODE_ATTRIBUTE_RANK = "rank";
    public static final String NODE_ATTRIBUTE_UPDATED_AT = "updated_at";
    public static final String NODE_ATTRIBUTE_PAGEID = "page_id";
    public static final String PLACE_HOLDER = "placeholder";


    public static final HashSet<String> NODE_ATTRIBUTES_STRs = new HashSet<String>();

    static {
        NODE_ATTRIBUTES_STRs.add(NODE_ATTRIBUTE_NODEID);
        NODE_ATTRIBUTES_STRs.add(NODE_ATTRIBUTE_SCIENTIFICNAME);
        NODE_ATTRIBUTES_STRs.add(NODE_ATTRIBUTE_RANK);
        NODE_ATTRIBUTES_STRs.add(NODE_ATTRIBUTE_PARENT_NODEID);
        NODE_ATTRIBUTES_STRs.add(NODE_ATTRIBUTE_ACCEPTED_NODEID);
    }


}
