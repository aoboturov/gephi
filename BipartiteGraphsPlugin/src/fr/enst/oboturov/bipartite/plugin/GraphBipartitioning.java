package fr.enst.oboturov.bipartite.plugin;

import static fr.enst.oboturov.bipartite.algorithms.standard.GraphBipartitioning.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.NbBundle;

/**
 *
 * @author Artem OBOTUROV
 */
public class GraphBipartitioning implements Statistics, LongTask {

    private ProgressTicket progress;
    private boolean isCanceled;
    private boolean isEmptyGraph;
    private boolean isBipartiteGraph;
    private int redPartNum, bluePartNum;
    private final StringBuilder sb = new StringBuilder();

    public int getBluePartNum() {
        return bluePartNum;
    }

    public int getRedPartNum() {
        return redPartNum;
    }

    @Override
    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        final Graph graph = graphModel.getGraph();
        try {
            progress.start();
            graph.writeLock();
            executeBipartAlg(graph, attributeModel);
        } finally {
            graph.writeUnlock();
            graph.readUnlockAll();
            progress.finish();
        }
    }

    public void executeBipartAlg(final Graph graph, AttributeModel attributeModel) {
        isCanceled = false;
        isEmptyGraph = false;
        redPartNum = 0;
        bluePartNum = 0;
        // Reveal if a graph is bipartite.
        if ( 0 == graph.getNodeCount()) {
            isEmptyGraph = true;
            return;
        }
        final Iterator<Node> it = graph.getNodes().iterator();
        it.hasNext();
        final Map<Node,Integer> distances = bipartAlgImpl(graph, it.next());
        if ( null == distances) {
            return;
        }
        // Set attribute for bipartitioning.
        final AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn modCol = nodeTable.getColumn(BIPARTITION_CLASS);
        if (modCol == null) {
            modCol = nodeTable.addColumn(BIPARTITION_CLASS, NbBundle.getMessage(getClass(), "GraphBipartitioning.columnName"), AttributeType.INT, AttributeOrigin.COMPUTED, RED_CLASS);
        }
        // Make two partitions of a bipartite graph.
        for ( Map.Entry<Node,Integer> entry : distances.entrySet()) {
            if ( isCanceled) {
                break;
            }
            final AttributeRow row = (AttributeRow) entry.getKey().getNodeData().getAttributes();
            if ( 0 == entry.getValue() % 2) {
                row.setValue(modCol, RED_CLASS);
                ++redPartNum;
            } else {
                row.setValue(modCol, BLUE_CLASS);
                ++bluePartNum;
            }
        }
        if ( isCanceled) {
            nodeTable.removeColumn(modCol);
        }
    }

    /**
     * Theoretical cost is |V|.
     * Implementation of first part of BIPART algorithm.
     * @param graph to be checked if is bipartite or not.
     * @param initialNode is an initial Node for the algorithm to start work.
     * @return null if graph is not bipartite, or return a map with distances
     * from initialNode.
     */
    private HashMap<Node,Integer> bipartAlgImpl(final Graph graph, final Node initialNode) {
        final HashMap<Node,Integer> distances = new HashMap<Node,Integer>(graph.getNodeCount());
        final Stack<Node> nodesToProcess = new Stack<Node>();
        distances.put(initialNode, 0);
        nodesToProcess.push(initialNode);
        isBipartiteGraph = true;
        while ( !nodesToProcess.empty() && isBipartiteGraph && !isCanceled) {
            final Node curNode = nodesToProcess.pop();
            final Integer curDist = distances.get(curNode);
            for ( Edge e : graph.getEdges(curNode)) {
                Node adjacent = e.getSource();
                if ( adjacent.equals(curNode)) {
                    adjacent = e.getTarget();
                }
                final Integer adjDist = distances.get(adjacent);
                if ( null == adjDist) {
                    distances.put(adjacent, curDist+1);
                    nodesToProcess.push(adjacent);
                } else {
                    if ( adjDist == curDist) {
                        isBipartiteGraph = false;
                    }
                }
            }
        }
        if ( isBipartiteGraph) {
            return distances;
        }
        return null;
    }

    @Override
    public String getReport() {
        sb.setLength(0);
        sb.append("<HTML> <BODY> <h1> Graph Bipartitioning </h1> ");
        if ( isCanceled) {
            sb.append("<hr> Task was canceled.");
        } else {
            sb.append("<hr>");
            if ( isEmptyGraph) {
                sb.append("Empty graph cann't be Bipartite graph.");
            } else {
                if ( isBipartiteGraph) {
                    sb.append("Graph is Bipartite with ")
                            .append(redPartNum).append(" nodes in one part and ")
                            .append(bluePartNum).append(" nodes in the other.");
                } else {
                    sb.append("Graph is NOT Bipartite.");
                }
            }
        }
        sb.append("</BODY> </HTML>");
        return sb.toString();
    }

    @Override
    public boolean cancel() {
        isEmptyGraph = false;
        isBipartiteGraph = false;
        redPartNum = 0;
        bluePartNum = 0;
        return isCanceled = true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }

    public boolean isBipartiteGraph() {
        return isBipartiteGraph;
    }

}
