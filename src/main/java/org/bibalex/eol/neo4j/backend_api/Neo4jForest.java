//package org.bibalex.eol.neo4j.backend_api;
//
//import org.bibalex.eol.neo4j.parser.Neo4jCommon;
//import org.neo4j.driver.v1.Record;
//import org.neo4j.driver.v1.StatementResult;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.logging.Logger;
//
//import static org.neo4j.driver.v1.Values.parameters;
//
//public class Neo4jForest extends Neo4jCommon{
//
//    ArrayList<Neo4jTree> trees = new ArrayList<>();
//    Logger logger =  Logger.getLogger("Neo4jTree");
//
//
//    public ArrayList<Neo4jTree> getTrees(String timestamp)
//    {
//        ArrayList<Object> roots = new ArrayList<>();
//        logger.info("Get the trees harvested after " + timestamp);
//        String query = "MATCH(n:Node) WHERE n.updated_at > apoc.date.parse({timestamp}, 'ms', 'dd.mm.yyyy') " +
//                "AND NOT (n)<-[:IS_PARENT_OF]-() RETURN n.generated_auto_id";
//        StatementResult result = getSession().run(query, parameters("timestamp", timestamp));
//        while (result.hasNext())
//        {
//            Record record = result.next();
//            roots.add(record.get("n.generated_auto_id"));
//        }
////        for(int i = 0; i<roots.size();i++)
////        {
////            System.out.println(roots.get(i));
////        }
//        roots.forEach((root) -> {
//            Neo4jTree tree = new Neo4jTree();
//             tree.setRoot(Integer.valueOf(root.toString()));
//             tree.setChildren(Integer.valueOf(root.toString()));
//            trees.add(tree);
//        });
////        for(int i = 0; i<trees.size();i++)
////        {
////            System.out.println(trees.get(i).root.getScientificName());
////            for(int j = 0;j<trees.get(i).children.size();j++)
////            System.out.println(trees.get(i).children.get(j).getScientificName());
////        }
//        return trees;
//    }
//
//}
