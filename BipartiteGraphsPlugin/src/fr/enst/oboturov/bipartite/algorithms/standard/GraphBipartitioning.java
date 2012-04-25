package fr.enst.oboturov.bipartite.algorithms.standard;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Artem OBOTUROV
 */
final public class GraphBipartitioning {

    private static final Logger logger = Logger.getLogger(GraphBipartitioning.class.getName());

    public static final String BIPARTITION_CLASS = "bipartition_class";
    public static final Integer RED_CLASS = 0;
    public static final Integer BLUE_CLASS = 1;

    public final List<Node> redNodes = new ArrayList<Node>();
    public final List<Node> blueNodes = new ArrayList<Node>();

    public GraphBipartitioning(final Graph graph) throws Exception {
        this(graph, graph.getNodeCount()/2, graph.getNodeCount()/2);
    }

    public GraphBipartitioning(final Graph graph, final int redNum, final int blueNum) throws Exception {
        for ( Node node : graph.getNodes().toArray()) {
            if ( RED_CLASS == node.getNodeData().getAttributes().getValue(BIPARTITION_CLASS)) {
                redNodes.add(node);
            }
            if ( BLUE_CLASS == node.getNodeData().getAttributes().getValue(BIPARTITION_CLASS)) {
                blueNodes.add(node);
            }
        }
        if ( (blueNodes.size()+redNodes.size()) != graph.getNodeCount()) {
            throw new Exception(NbBundle.getMessage(GraphBipartitioning.class, "GraphBipartitioning.assertion.not.bipartite", blueNodes.size(), redNodes.size(), graph.getNodeCount()));
        }
        if ( logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, NbBundle.getMessage(GraphBipartitioning.class, "GraphBipartitioning.red.nodes.num", redNodes.size()));
            logger.log(Level.FINE, NbBundle.getMessage(GraphBipartitioning.class, "GraphBipartitioning.blue.nodes.num", blueNodes.size()));
        }
    }

}
