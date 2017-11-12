package org.bibalex.eol.neo4j.api;

import org.bibalex.eol.neo4j.models.NodesData;
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

    @RequestMapping(value="/createSynRelation/{id}", method = RequestMethod.POST)
    public ResponseEntity<String> uploadResource(@PathVariable("id") String id, @RequestParam("nodeId") String nodeId)  {
        System.out.println(nodeId);
//        service.createSynRelation(Integer.parseInt(id), Integer.parseInt(nodeId));
        return new ResponseEntity("Successfully added - " + id, new HttpHeaders(), HttpStatus.OK);

    }

//    @RequestMapping(value="/createNode/", method = RequestMethod.POST)
//    public int createNode(@RequestParam("resourceId") String resourceId, @RequestParam("taxonId") String taxonId,
//                                          @RequestParam("scientificName") String scientificName, @RequestParam("rank") String rank,
//                                          @RequestParam("parentGeneratedNodeId") String parentGeneratedNodeId)
//    {
//       int nodeId =  service.createNode(Integer.parseInt(resourceId),taxonId,scientificName,rank,Integer.parseInt(parentGeneratedNodeId));
//       System.out.print(new ResponseEntity("Successfully added - " , new HttpHeaders(), HttpStatus.OK));
//       return nodeId;
//    }
    @RequestMapping(value="/createNode/", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
     public int createNode(@RequestBody Node n)
    {
        int  generatedNodeId = service.createNode(n);
        return generatedNodeId;
    }

    @RequestMapping(value="/getNode/", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public int getNode(@RequestBody Node n)
    {
       int generatedNodeId =  service.getNode(n);
       return generatedNodeId;
    }

    @RequestMapping(value="/getNodeData/{generatedNodeId}", method = RequestMethod.POST)
    public NodesData getNodeData(@PathVariable("generatedNodeId") String generatedNodeId)
    {
        NodesData data = service.getData(generatedNodeId);
        return data;
    }

}
