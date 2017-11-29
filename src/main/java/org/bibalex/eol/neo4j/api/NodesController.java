package org.bibalex.eol.neo4j.api;

import org.bibalex.eol.neo4j.models.NodeData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.bibalex.eol.neo4j.models.Node;

@RestController
@RequestMapping("/neo4j")
public class NodesController {

    @Autowired
    private NodesService service;



    @RequestMapping(value="/createNode/", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
     public int createNode(@RequestBody Node n)
    {
        int  generatedNodeId = service.createNode(n);
        return generatedNodeId;
    }

    @RequestMapping(value="/createSynonymNode/", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
     public int createSynonym(@RequestBody Node n)
    {
        int  generatedNodeId = service.createSynonym(n);
        return generatedNodeId;
    }

     @RequestMapping(value="/createSynonymRelation/", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
     public boolean createSynonymRelation(@RequestBody Node n)
    {
        boolean  created = service.createRelationBetweenNodeAndSynonyms(n);
        return created;
    }

     @RequestMapping(value="/createAncestorNode/", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
     public int createAncestorNode(@RequestBody Node n)
    {
        int  generatedNodeId = service.createAncestorNode(n);
        return generatedNodeId;
    }


    @RequestMapping(value="/createParentWithPlaceholder/", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
     public int createParentNode(@RequestBody Node n)
    {
        int  generatedNodeId = service.createParentNode(n);
        return generatedNodeId;
    }


    @RequestMapping(value="/getNode/", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int getNode(@RequestBody Node n)
    {
       int generatedNodeId =  service.getNode(n);
       return generatedNodeId;
    }

    @RequestMapping(value="/getAcceptedNode/", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int getAcceptedNode(@RequestBody Node n)
    {
       int generatedNodeId =  service.getAcceptedNode(n);
       return generatedNodeId;
    }


    @RequestMapping(value="/getSynonymNode/", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
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

}
