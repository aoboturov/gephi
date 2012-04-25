package fr.enst.oboturov.bipartite.plugin;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.SingularValueDecomposition;
import com.google.common.collect.Maps;
import com.google.common.collect.TreeMultimap;
import fr.enst.oboturov.bipartite.IntComparator;
import fr.enst.oboturov.bipartite.NodeByIdComparator;
import fr.enst.oboturov.bipartite.algorithms.barber.BRIM_ModularityAlgorithm;
import fr.enst.oboturov.bipartite.algorithms.barber.BipartiteGraphMatrixRepresentation;
import fr.enst.oboturov.bipartite.algorithms.barber.SVD_ModularityAlgorithm;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.NbBundle;

import static fr.enst.oboturov.bipartite.algorithms.barber.BRIM_ModularityAlgorithm.INITIAL_KEY_MAP_VAL;
import static fr.enst.oboturov.bipartite.algorithms.barber.SVD_ModularityAlgorithm.SVD_MODULARITY;

/**
 * Since Gephi @see org.gephi.graph.dhns.node.AbstractNode doesn't override
 * neither {@code hashCode()} and nor {@code equals()} by using
 * {@code nodeId()} we have a problem with the standard collections, so there
 * are some dirty hacks in code, sorry :(.
 *
 * This algorithm makes a depth-first recursive decomposition of a module
 * descending first to a module having positive value of Q function.
 *
 * @author Artem OBOTUROV
 */
public class BarberSVD_ModularityAlgorithm implements Statistics, LongTask {

    public static final Double SVD_Q_DELTA_DEFAULT = 0.001D;

    private static final Logger logger = Logger.getLogger(BarberSVD_ModularityAlgorithm.class.getName());

    private final Stack<Map.Entry<GraphView, Double>> graphPartitions = new Stack<Map.Entry<GraphView, Double>>();
    private final GraphBipartitioning bip = new GraphBipartitioning();
    private final StringBuilder sb = new StringBuilder();
    private ProgressTicket progress;
    private boolean isCanceled;
    private double modularityQ_value;
    private double deltaQ_value;
    private int modulesNumber;

    public BarberSVD_ModularityAlgorithm() {
        progress = null;
        modularityQ_value = 0D;
        isCanceled = false;
        modulesNumber = 0;
        deltaQ_value = SVD_Q_DELTA_DEFAULT;
    }

    public double getDeltaQ_value() {
        return deltaQ_value;
    }

    public void setDeltaQ_value(double deltaQ_value) {
        this.deltaQ_value = deltaQ_value;
    }

    public double getQ_optimized() {
        return modularityQ_value;
    }

    @Override
    public void execute(final GraphModel graphModel, final AttributeModel attributeModel) {
        progress.start();
        final GraphView mainView = graphModel.getVisibleView();
        final Graph mainGraph = graphModel.getGraph();
        try {
            mainGraph.writeLock();
            // Recover bipartite structure of the graph.
            bip.executeBipartAlg(mainGraph, attributeModel);
            if ( isCanceled) {
                return;
            }
            if ( bip.isBipartiteGraph()) {
                // Set attribute for bipartitioning.
                final AttributeTable nodeTable = attributeModel.getNodeTable();
                AttributeColumn modCol = nodeTable.getColumn(SVD_MODULARITY);
                if (modCol == null) {
                    modCol = nodeTable.addColumn(SVD_MODULARITY,
                            NbBundle.getMessage(BarberSVD_ModularityAlgorithm.class, "SVD_Modularity.columnName"), AttributeType.INT, AttributeOrigin.COMPUTED, mainView.getViewId());
                }
                graphModel.getGraph().readUnlockAll();
                doModularisation(graphModel);
            }
        } catch(Exception ex) {
            if ( logger.isLoggable(Level.FINE)) {
                logger.log(Level.SEVERE, "Unexpected error:\n", ex);
            } else {
                logger.log(Level.SEVERE, ex.getMessage());
            }
        } finally {
            // Clear views if algorithm didn't work correctly.
            while ( !graphPartitions.isEmpty()) {
                try {
                    graphModel.destroyView(graphPartitions.pop().getKey());
                } catch (Exception ex) {
                    System.err.print(ex);
                }
            }
            graphModel.setVisibleView(mainView);
            mainGraph.writeUnlock();
            mainGraph.readUnlockAll();
            progress.finish();
        }
    }

    /**
     * TODO: remove dependency on GraphView, since it's totally possible to
     * work only with partitions.
     * TODO: check if no memory leaks while disposing views
     * {@see org.gephi.graph.api.GraphModel.destroyView}
     *
     * TODO: we do not need to make full SVD decomposition because we use only
     *   maximal eigenvalue and corresponding eigenvectors, so there exist
     *   faster algorithms and they MUST be applied here.
     *
     * @param graphModel
     */
    private void doModularisation(GraphModel graphModel) throws Exception {
        modulesNumber = 0;
        modularityQ_value = 0D;
        graphPartitions.push(Maps.immutableEntry(graphModel.newView(), modularityQ_value));
        final Graph initialGraph = graphPartitions.peek().getKey().getGraphModel().getGraph();
        final int initialEdgesNumber = initialGraph.getEdgeCount();
        final fr.enst.oboturov.bipartite.algorithms.standard.GraphBipartitioning initialBip =
                new fr.enst.oboturov.bipartite.algorithms.standard.GraphBipartitioning(initialGraph);
        final DoubleMatrix2D initialBhat = BipartiteGraphMatrixRepresentation
                .matrixBhat(initialGraph, initialBip.redNodes, initialBip.blueNodes);
        boolean isFirstIteration = true;
        do {
            final Map.Entry<GraphView, Double> stackTop = graphPartitions.pop();
            final Graph posValsGraph = graphModel.getGraph(stackTop.getKey());
            // Get Bipartite parts.
            final fr.enst.oboturov.bipartite.algorithms.standard.GraphBipartitioning gBip =
                    new fr.enst.oboturov.bipartite.algorithms.standard.GraphBipartitioning(posValsGraph);
            if ( gBip.redNodes.size()<=0 || gBip.blueNodes.size()<=0) {
                continue;
            }
            // Make this decomposition to discover the singular values.
            DoubleMatrix2D B = BipartiteGraphMatrixRepresentation.matrixBhat(posValsGraph, gBip.redNodes, gBip.blueNodes);
            posValsGraph.readUnlockAll();
            // SVD works for an m x n matrix B with m >= n, otherwise B should be transposed.
            boolean isTransposed = B.rows() < B.columns() ? true : false;
            if ( B.rows() < B.columns()) {
                B = new Algebra().transpose(B);
            }
            final SingularValueDecomposition svd = new SingularValueDecomposition(B);
            final DoubleMatrix1D u = isTransposed ? svd.getV().viewRow(0) : svd.getU().viewColumn(0);
            final DoubleMatrix1D v = isTransposed ? svd.getU().viewColumn(0) : svd.getV().viewRow(0);
            if ( logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, NbBundle.getMessage(
                        BarberSVD_ModularityAlgorithm.class, "BarberSVD_ModularityAlgorithm.log.B_hat", B));
                logger.log(Level.FINER, NbBundle.getMessage(
                        BarberSVD_ModularityAlgorithm.class, "BarberSVD_ModularityAlgorithm.log.singular.vector.u", u));
                logger.log(Level.FINER, NbBundle.getMessage(
                        BarberSVD_ModularityAlgorithm.class, "BarberSVD_ModularityAlgorithm.log.singular.vector.v", v));
                logger.log(Level.FINER, NbBundle.getMessage(
                        BarberSVD_ModularityAlgorithm.class, "BarberSVD_ModularityAlgorithm.log.maximal.singular.value", svd.getSingularValues()[0]));
            }
            // Do not split if singular value is relatively small.
            if ( 0.01D >= svd.getSingularValues()[0]) {
                if ( logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, NbBundle.getMessage(
                            BarberSVD_ModularityAlgorithm.class, "BarberSVD_ModularityAlgorithm.log.zero.max.singular.val"));
                }
                modularityQ_value += stackTop.getValue();
                modulesNumber++;
                graphModel.destroyView(stackTop.getKey());
                continue;
            }
            // Evaluate value of the modulatiry function for an unsplitted module.
            final TreeMultimap<Integer, Node> partition = TreeMultimap
                    .<Integer, Node>create(new IntComparator(), new NodeByIdComparator());
            for ( Node node : posValsGraph.getNodes()) {
                partition.put(INITIAL_KEY_MAP_VAL, node);
            }
            final double qUnsplitted = BRIM_ModularityAlgorithm
                    .evaluateQvalue(initialEdgesNumber, initialBhat, initialBip.redNodes, initialBip.blueNodes, partition);
            // Evaluate value of the modulatiry function after split of the module.
            final double qNeg = BRIM_ModularityAlgorithm
                    .evaluateQvalue(initialEdgesNumber, initialBhat, initialBip.redNodes, initialBip.blueNodes,
                        SVD_ModularityAlgorithm.modularityFunctionNegativeComponent(gBip.redNodes, gBip.blueNodes, u, v));
            final double qPos = BRIM_ModularityAlgorithm
                    .evaluateQvalue(initialEdgesNumber, initialBhat, initialBip.redNodes, initialBip.blueNodes,
                        SVD_ModularityAlgorithm.modularityFunctionPositiveComponent(gBip.redNodes, gBip.blueNodes, u, v));
            // Do not descend on the dihotomy's branch if a module's modularity
            // is bigger than sum of modularities of the decomposition of this module.
            if ( isFirstIteration) {
                isFirstIteration = false;
            } else {
                if ( qNeg < SVD_Q_DELTA_DEFAULT || qPos < SVD_Q_DELTA_DEFAULT 
                        || qUnsplitted < SVD_Q_DELTA_DEFAULT
                        || (qNeg+qPos) <= qUnsplitted) {

                    if ( logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, NbBundle.getMessage(
                                BarberSVD_ModularityAlgorithm.class, "BarberSVD_ModularityAlgorithm.log.dQ.value",
                                stackTop.getValue(), posValsGraph.getNodeCount(), qNeg, qPos, qUnsplitted));
                    }
                    modularityQ_value += stackTop.getValue();
                    modulesNumber++;
                    graphModel.destroyView(stackTop.getKey());
                    continue;
                }
            }
            if ( logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, NbBundle.getMessage(
                        BarberSVD_ModularityAlgorithm.class, "BarberSVD_ModularityAlgorithm.log.expain.split", qNeg, qPos));
            }
            // Copy view nodes and edges (by removing nodes from copy of main view
            // which are not on curView).
            final GraphView negValsView = graphModel.newView();
            final Graph negValsGraph = negValsView.getGraphModel().getGraph(negValsView);
            final Comparator<Node> nIdCmp = new NodeByIdComparator();
            Collections.sort(gBip.redNodes, nIdCmp);
            Collections.sort(gBip.blueNodes, nIdCmp);
            for (Node node : negValsGraph.getNodes().toArray()) {
                if ( 0 > Collections.binarySearch(gBip.redNodes, node, nIdCmp)
                        && 0 > Collections.binarySearch(gBip.blueNodes, node, nIdCmp)) {
                    final Node toDelete = negValsGraph.getNode(node.getId());
                    if ( null != toDelete) {
                        negValsGraph.removeNode(toDelete);
                    }
                }
            }
            // Apply partition algorithm for graph (binary allocation of nodes
            // to red and blue parts modules of graph).
            SVD_ModularityAlgorithm.vectorElementSignBasedNodeAssignmentProcedure(
                    u, gBip.redNodes, negValsGraph, posValsGraph);
            SVD_ModularityAlgorithm.vectorElementSignBasedNodeAssignmentProcedure(
                    v, gBip.blueNodes, negValsGraph, posValsGraph);
            // Check if graphs could be further decomposed.
            if ( 0 < negValsGraph.getNodeCount() && qNeg > 0D) {
                graphPartitions.push(Maps.immutableEntry(negValsView, qNeg));
            } else {
                if ( qNeg == 0D && 0 < negValsGraph.getNodeCount()) {
                    if ( logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, NbBundle.getMessage(
                                BarberSVD_ModularityAlgorithm.class, "BarberSVD_ModularityAlgorithm.log.dQ.zero.value", negValsGraph.getNodeCount()));
                    }
                    modulesNumber++;
                }
                graphModel.destroyView(negValsView);
            }
            if ( 0 < posValsGraph.getNodeCount() && qPos > 0D) {
                graphPartitions.push(Maps.immutableEntry(posValsGraph.getView(), qPos));
            } else {
                if ( qPos == 0D && 0 < posValsGraph.getNodeCount()) {
                    if ( logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, NbBundle.getMessage(
                                BarberSVD_ModularityAlgorithm.class, "BarberSVD_ModularityAlgorithm.log.dQ.zero.value", posValsGraph.getNodeCount()));
                    }
                    modulesNumber++;
                }
                graphModel.destroyView(posValsGraph.getView());
            }
        } while ( !graphPartitions.empty() && !isCanceled);
        if ( logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, NbBundle.getMessage(
                    BarberSVD_ModularityAlgorithm.class, "BarberSVD_ModularityAlgorithm.report.modules.discovered", modulesNumber));
            logger.log(Level.FINE, NbBundle.getMessage(
                    BarberSVD_ModularityAlgorithm.class, "BarberSVD_ModularityAlgorithm.report.modularity.Q.value", modularityQ_value));
        }
    }

    @Override
    public String getReport() {
        sb.setLength(0);
        sb.append("<HTML><BODY><h1>").append(NbBundle.getMessage(
                BarberSVD_ModularityAlgorithm.class, "BarberSVD_ModularityAlgorithm.report.alg.name")).append("</h1>");
        if ( isCanceled) {
            sb.append("<hr>").append(NbBundle.getMessage(
                    BarberSVD_ModularityAlgorithm.class, "BarberSVD_ModularityAlgorithm.report.was.canceled"));
        } else {
            sb.append("<hr>").append(NbBundle.getMessage(BarberSVD_ModularityAlgorithm.class,
                    "BarberSVD_ModularityAlgorithm.report.modules.discovered", modulesNumber))
                    .append("<br/>");
//            sb.append(NbBundle.getMessage(BarberSVD_ModularityAlgorithm.class,
//                    "BarberSVD_ModularityAlgorithm.report.deltaQ.value", String.format("%.3f", deltaQ_value)))
//                    .append("<br/>");
            sb.append(NbBundle.getMessage(BarberSVD_ModularityAlgorithm.class,
                    "BarberSVD_ModularityAlgorithm.report.modularity.Q.value", String.format("%.3f", modularityQ_value)));
        }
        sb.append("</BODY> </HTML>");
        return sb.toString();
    }

    @Override
    public boolean cancel() {
        return isCanceled = true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }

}
