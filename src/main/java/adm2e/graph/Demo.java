package adm2e.graph;

import adm2e.graph.representations.AdjacencyListGraph;
import adm2e.graph.traversal.BreadthFirstTraversal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;

public class Demo {
    public static void main(String[] args) {
        AdjacencyListGraph graph = new AdjacencyListGraph();
        graph.addEdge("A", "B", true);
        graph.addEdge("A", "C", true);
        graph.addEdge("B", "D", true);
        graph.addEdge("D", "C", true);
        graph.addEdge("C", "E", true);
        List<String> list = new ArrayList<>();
        AdjacencyListGraph.EdgeConsumer edgeConsumer = (x, y) -> {
            list.add(String.format("%s -> %s", graph.labelOfVertex(x).get(), graph.labelOfVertex(y).get()));
        };
        IntPredicate stoppingCondition = i ->
            graph.labelOfVertex(i).get().equalsIgnoreCase("e");
        BreadthFirstTraversal.traverse(graph, edgeConsumer, stoppingCondition);
        System.out.println(list.stream().collect(Collectors.joining(", ")));
    }
}
