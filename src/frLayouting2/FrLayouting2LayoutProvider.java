package frLayouting2;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.elk.core.AbstractLayoutProvider;
import org.eclipse.elk.core.math.ElkPadding;
import org.eclipse.elk.core.math.ElkRectangle;
import org.eclipse.elk.core.util.IElkProgressMonitor;
import org.eclipse.elk.graph.ElkEdge;
import org.eclipse.elk.graph.ElkEdgeSection;
import org.eclipse.elk.graph.ElkLabel;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.ElkPort;
import org.eclipse.elk.graph.properties.IProperty;
import org.eclipse.elk.graph.properties.IPropertyHolder;
import org.eclipse.elk.graph.util.ElkGraphUtil;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;

import frLayouting2.options.FrLayouting2Options;

/**
 * A simple layout algorithm class. This algorithm already supports a number of layout options, places nodes, and
 * routes edges.
 */
public class FrLayouting2LayoutProvider extends AbstractLayoutProvider {
    
    private Random rdm = new Random();
    private int W = 500;
    private int H = 500;
    
    private double k(int vCount) { return Math.sqrt((W * H) / vCount); }
    private double fa(double x, int vCount) {
        return x * x / k(vCount);
    }
    private double fr(double x, int vCount) {
        double kCache = k(vCount);
        return -(kCache * kCache) / x;
    }
    private double cool(int iteration) {
        if (iteration < 1)
            return 50;
        else
            return 1 / iteration * 50;
    }
    
    @Override
    public void layout(ElkNode layoutGraph, IElkProgressMonitor progressMonitor) {
        // Start progress monitor
        progressMonitor.begin("FrLayouting2", 2);
        progressMonitor.log("Algorithm began");
        
        layoutGraph.setWidth(W);
        layoutGraph.setHeight(H);
                
        // Retrieve several properties
        ElkPadding padding = layoutGraph.getProperty(FrLayouting2Options.PADDING);
        
        double edgeEdgeSpacing = layoutGraph.getProperty(FrLayouting2Options.SPACING_EDGE_EDGE);
        double edgeNodeSpacing = layoutGraph.getProperty(FrLayouting2Options.SPACING_EDGE_NODE);
        double nodeNodeSpacing = layoutGraph.getProperty(FrLayouting2Options.SPACING_NODE_NODE);
        
        int iterations = layoutGraph.getProperty(FrLayouting2Options.ITERATIONS);
        System.out.println("Iterations: " + iterations);
        
        // Get and possibly reverse the list of nodes to lay out
        List<ElkNode> nodes = new ArrayList<>(layoutGraph.getChildren());
        if (layoutGraph.getProperty(FrLayouting2Options.REVERSE_INPUT)) {
            Collections.reverse(nodes);
        }
        
        System.out.println("New Graph requested with: " + nodes.size() + 
                " nodes and " + layoutGraph.getContainedEdges().size() + " edges");
        
        // Create a sub monitor for node placement
        IElkProgressMonitor nodePlacingMonitor = progressMonitor.subTask(1);
        nodePlacingMonitor.begin("Node Spacing", nodes.size());
        
        // Make an output to the debug log
        nodePlacingMonitor.logGraph(layoutGraph, "No node placed yet");
        
        for (ElkNode node : nodes) {
            // Set the node's initial coordinates
            node.setX(rdm.nextDouble() * layoutGraph.getWidth());
            node.setY(rdm.nextDouble() * layoutGraph.getHeight());
            
            //System.out.println("Start coords of node " + node);

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
            
            for (ElkEdge e : layoutGraph.getContainedEdges()) {
                ElkNode src = ElkGraphUtil.connectableShapeToNode(e.getSources().get(0));
                ElkNode tar = ElkGraphUtil.connectableShapeToNode(e.getTargets().get(0));
                Vector2 diff = Vector2.FromELK(src).Sub(Vector2.FromELK(tar));
                disps[nodes.indexOf(src)] = disps[nodes.indexOf(src)].Sub(diff.Div(diff.Length()).
                        Mult(fa(diff.Length(), nodes.size())));
                disps[nodes.indexOf(src)] = disps[nodes.indexOf(src)].Add(diff.Div(diff.Length()).
                        Mult(fa(diff.Length(), nodes.size())));
            }
            
            for (int v = 0; v < nodes.size(); v++) {
                ElkNode n = nodes.get(v);
                if (disps[v].X == Double.NaN && disps[v].Y == Double.NaN ) {
                    // If parts of the disp vector are NaN, a diff vector 
                    // we added to disp must have been NaN, which only happens if the diff vector 
                    // was zero and two nodes where at the same point, this isnt included in the 
                    // pseudocode but the paper states that if two nodes should end up on the 
                    // same point the angle and length of disp should be random
                    
                    disps[v] = new Vector2(rdm.nextInt(W) - W/2, rdm.nextInt(H) - H/2);
                }
                
                // This isn't exactly as in the pseudo code because I can't just set 
                // the position of a node in one assignment but have to set he X and Y 
                // values independently
                double dispLength = disps[v].Length();
                Vector2 normalized = disps[v].Div(dispLength);
                
                n.setX(n.getX() + normalized.X * Math.min(cool(i), dispLength));
                n.setY(n.getY() + normalized.Y * Math.min(cool(i), dispLength));
                
                n.setX(Math.min(W, Math.max(0, n.getX())));
                n.setY(Math.min(H, Math.max(0, n.getY())));
                
                //System.out.println("New coords of node " + nodes.get(v) + "\twith disp " + disps[v]);
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
        
        layoutGraph.setWidth(W);
        layoutGraph.setHeight(H);
        
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
