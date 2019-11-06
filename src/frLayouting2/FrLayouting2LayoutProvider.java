package frLayouting2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.eclipse.elk.core.AbstractLayoutProvider;
import org.eclipse.elk.core.math.ElkPadding;
import org.eclipse.elk.core.util.IElkProgressMonitor;
import org.eclipse.elk.graph.ElkEdge;
import org.eclipse.elk.graph.ElkEdgeSection;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.util.ElkGraphUtil;

import frLayouting2.options.FrLayouting2Options;

/**
 * A simple layout algorithm class. This algorithm already supports a number of layout options, places nodes, and
 * routes edges.
 */
public class FrLayouting2LayoutProvider extends AbstractLayoutProvider {
    
    private Random rdm = new Random();
    private int canvasWidth = 500;
    private int canvasHeight = 500;
    
    private double k(int vCount) { return Math.sqrt((canvasWidth * canvasHeight) / vCount); }
    private double fa(double x, int vCount) {
        return x * x / k(vCount);
    }
    private double fr(double x, int vCount) {
        double kCache = k(vCount);
        return -(kCache * kCache) / x;
    }
    
    @Override
    public void layout(ElkNode layoutGraph, IElkProgressMonitor progressMonitor) {
        // Start progress monitor
        progressMonitor.begin("FrLayouting2", 2);
        progressMonitor.log("Algorithm began");
        
        layoutGraph.setWidth(500);
        layoutGraph.setHeight(500);
                
        // Retrieve several properties
        ElkPadding padding = layoutGraph.getProperty(FrLayouting2Options.PADDING);
        
        double edgeEdgeSpacing = layoutGraph.getProperty(FrLayouting2Options.SPACING_EDGE_EDGE);
        double edgeNodeSpacing = layoutGraph.getProperty(FrLayouting2Options.SPACING_EDGE_NODE);
        double nodeNodeSpacing = layoutGraph.getProperty(FrLayouting2Options.SPACING_NODE_NODE);
        
        int iterations = /*layoutGraph.getProperty(FrLayouting2Options.)*/ 50;
        
        // Get and possibly reverse the list of nodes to lay out
        List<ElkNode> nodes = new ArrayList<>(layoutGraph.getChildren());
        if (layoutGraph.getProperty(FrLayouting2Options.REVERSE_INPUT)) {
            Collections.reverse(nodes);
        }
        
        // Create a sub monitor for node placement
        IElkProgressMonitor nodePlacingMonitor = progressMonitor.subTask(1);
        nodePlacingMonitor.begin("Node Spacing", nodes.size());
        
        // Make an output to the debug log
        nodePlacingMonitor.logGraph(layoutGraph, "No node placed yet");
        
        for (ElkNode node : nodes) {
            // Set the node's initial coordinates
            node.setX(rdm.nextDouble() * layoutGraph.getWidth());
            node.setY(rdm.nextDouble() * layoutGraph.getHeight());

            nodePlacingMonitor.logGraph(layoutGraph, node.getIdentifier() + " placed");
        }
        
        // Force directed placement
        Vector2[] disps = new Vector2[nodes.size()];
        for (int i = 0; i < iterations; i++) {
            for (int v = 0; v < nodes.size(); v++) {
                disps[v] = Vector2.Zero();
                
                for (int v2 = 0; v2 < nodes.size(); v2++) {
                    if (v != v2) {
                        Vector2 diff = Vector2.FromELK(nodes.get(v)).Sub(Vector2.FromELK(nodes.get(v2)));
                        double diffLengthCache = diff.Length();
                        disps[v] = disps[v].Add(diff.Div(diffLengthCache).Mult(fr(diffLengthCache, nodes.size())));
                    }
                }
            }
        }
        
        // Close the sub monitor
        nodePlacingMonitor.done();
        progressMonitor.log("Node Placing done!");
        
        // Create sub monitor for edge routing
        IElkProgressMonitor edgeRoutingMonitor = progressMonitor.subTask(1);
        edgeRoutingMonitor.begin("Edge Routing", layoutGraph.getContainedEdges().size());
        edgeRoutingMonitor.logGraph(layoutGraph, "No edge routed yet");
        
        // Route the edges
        if (!layoutGraph.getContainedEdges().isEmpty()) {
            for (ElkEdge edge : layoutGraph.getContainedEdges()) {
                ElkNode source = ElkGraphUtil.connectableShapeToNode(edge.getSources().get(0));
                ElkNode target = ElkGraphUtil.connectableShapeToNode(edge.getTargets().get(0));
                
                ElkEdgeSection section = ElkGraphUtil.firstEdgeSection(edge, true, true);
                
                section.setStartLocation(
                        source.getX() + source.getWidth() / 2,
                        source.getY() + source.getHeight() / 2);
                section.setEndLocation(
                        target.getX() + target.getWidth() / 2,
                        target.getY() + target.getHeight() / 2);
                                
                edgeRoutingMonitor.logGraph(layoutGraph, source.getIdentifier() + " -> " + target.getIdentifier());
            }
        }
        
        // Close the sub monitor
        edgeRoutingMonitor.done();
        
        progressMonitor.log("Edge Routing done!");
        
//        // Set the size of the final diagram dynamically
//        layoutGraph.setWidth(nodes.stream().map(x -> x.getX()).max(Double::compare).get() - 
//                nodes.stream().map(x -> x.getX()).min(Double::compare).get());
//        layoutGraph.setWidth(nodes.stream().map(y -> y.getY()).max(Double::compare).get() - 
//                nodes.stream().map(y -> y.getY()).min(Double::compare).get());
        
        // End the progress monitor
        progressMonitor.log("Algorithm executed");
        progressMonitor.logGraph(layoutGraph, "Final graph");
        progressMonitor.done();
    }
}
