package fr.enst.oboturov.bipartite.plugin;

import cern.colt.matrix.DoubleMatrix2D;
import com.google.common.collect.Maps;
import com.google.common.collect.TreeMultimap;
import fr.enst.oboturov.bipartite.IntComparator;
import fr.enst.oboturov.bipartite.NodeByIdComparator;
import fr.enst.oboturov.bipartite.algorithms.barber.BRIM_ModularityAlgorithm;
import fr.enst.oboturov.bipartite.algorithms.barber.BipartiteGraphMatrixRepresentation;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.data.attributes.api.AttributeModel;
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
public class BarberBRIM_ModularityAlgorithm implements Statistics, LongTask {

    private static final Logger logger = Logger.getLogger(BarberBRIM_ModularityAlgorithm.class.getName());

    private final fr.enst.oboturov.bipartite.plugin.GraphBipartitioning bipAlg =
            new fr.enst.oboturov.bipartite.plugin.GraphBipartitioning();
    private final StringBuilder sb = new StringBuilder();

    private ProgressTicket progress;
    private boolean isCanceled;
    private Double Q = 0D;
    private int modulesNumber = 0;

    @Override
    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        final Graph graph = graphModel.getGraph();
        try {
            graph.writeLock();
            int modNum = 1;
            Double dQ = 0D;
            bipAlg.executeBipartAlg(graph, attributeModel);
            if ( !bipAlg.isBipartiteGraph()) {
                return;
            }
            // Set all nodes attribute specifying BRIM modulatiry equal to 0.
            for (Node node : graph.getNodes()) {
                node.getNodeData().getAttributes().setValue(
                        NbBundle.getMessage(BarberBRIM_ModularityAlgorithm.class, "BRIM_Modularity.columnName"), 0);
            }
            final int nodesNumToBeReassigned =
                    ((graph.getNodeCount()%2==0) ? graph.getNodeCount() : graph.getNodeCount()-1)/2;
            Entry<Double, TreeMultimap<Integer, Node>> leftPart = Maps.immutableEntry(Q, TreeMultimap.<Integer, Node>create(new IntComparator(), new NodeByIdComparator()));
            leftPart.getValue().putAll(0, graph.getNodes());
            Entry<Double, TreeMultimap<Integer, Node>> rightPart = null;
            // Do dichotomy to find local optimum of modules number.
            final fr.enst.oboturov.bipartite.algorithms.standard.GraphBipartitioning gBip =
                    new fr.enst.oboturov.bipartite.algorithms.standard.GraphBipartitioning(graph, bipAlg.getRedPartNum(), bipAlg.getBluePartNum());
            final DoubleMatrix2D Bhat =
                    BipartiteGraphMatrixRepresentation.matrixBhat(graph, gBip.redNodes, gBip.blueNodes);
            // Determination of Cmax.
            // Extrapolation stage.
            do {
                // Double number of modules.
                final int newModeNum = 2*modNum;
                final TreeMultimap<Integer, Node> rightPartitioning = TreeMultimap.<Integer, Node>create(new IntComparator(), new NodeByIdComparator());
                rightPartitioning.putAll(leftPart.getValue());
                BRIM_ModularityAlgorithm.doNodesReassignment(rightPartitioning, modNum, newModeNum, nodesNumToBeReassigned);
                final TreeMultimap<Integer, Node> brimLocalOptimum =
                        BRIM_ModularityAlgorithm.brimOptimizationProcedure(graph.getEdgeCount(), Bhat, gBip.redNodes, gBip.blueNodes, rightPartitioning);
                dQ = BRIM_ModularityAlgorithm.evaluateQvalue(graph.getEdgeCount(), Bhat, gBip.redNodes, gBip.blueNodes, brimLocalOptimum);
                if ( logger.isLoggable(Level.FINER)) {
                    logger.log(Level.FINER, NbBundle.getMessage(
                            BarberBRIM_ModularityAlgorithm.class,
                            "BarberBRIM_ModularityAlgorithm.log.extrapolation.stage.iteration.step", newModeNum, dQ));
                }
                modNum = newModeNum;
                rightPart = Maps.immutableEntry(dQ, brimLocalOptimum);
                if ( dQ<=Q) {
                    break;
                }
                Q = dQ;
                leftPart = rightPart;
                rightPart = null;
            } while (true);
            // Interpolation stage.
            if ( 2<modNum) {
                int leftBound = modNum/2;
                int rightBound = modNum;
                int betweenBounds = 0;
                while ( leftBound != rightBound && leftBound != (betweenBounds = (rightBound-leftBound)/2+leftBound)) {
                    final TreeMultimap<Integer, Node> betweenPartitioning = TreeMultimap.<Integer, Node>create(new IntComparator(), new NodeByIdComparator());
                    betweenPartitioning.putAll(leftPart.getValue());
                    final int nodesInBetween = graph.getNodeCount()*(rightBound-leftBound)/(2*rightBound);
                    BRIM_ModularityAlgorithm.doNodesReassignment(betweenPartitioning, leftBound, betweenBounds, nodesInBetween);
                    final TreeMultimap<Integer, Node> brimLocalOptimum =
                        BRIM_ModularityAlgorithm.brimOptimizationProcedure(graph.getEdgeCount(), Bhat, gBip.redNodes, gBip.blueNodes, betweenPartitioning);
                    dQ = BRIM_ModularityAlgorithm.evaluateQvalue(graph.getEdgeCount(), Bhat, gBip.redNodes, gBip.blueNodes, brimLocalOptimum);
                    if ( leftPart.getKey() < dQ) {
                        leftPart = Maps.immutableEntry(dQ, brimLocalOptimum);
                        leftBound = betweenBounds;
                    } else {
                        rightPart = Maps.immutableEntry(dQ, brimLocalOptimum);
                        rightBound = betweenBounds;
                    }
                    if ( logger.isLoggable(Level.FINER)) {
                        logger.log(Level.FINER, NbBundle.getMessage(
                                BarberBRIM_ModularityAlgorithm.class,
                                "BarberBRIM_ModularityAlgorithm.log.interpolation.stage.iteration.step", betweenBounds, dQ));
                    }
                }
                Q = leftPart.getKey();
            }
            modulesNumber = leftPart.getValue().keySet().size();
            // Do graph coloring.
            for ( Integer key : leftPart.getValue().keySet()) {
                for ( Node node : leftPart.getValue().get(key)) {
                    node.getNodeData().getAttributes().setValue(
                            NbBundle.getMessage(BarberBRIM_ModularityAlgorithm.class, "BRIM_Modularity.columnName"), key);
                }
            }
        } catch (Exception ex) {
            if ( logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, ex.getMessage());
            }
            if ( logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Unexpected exception:\n", ex);
            }
            System.err.print(ex);
            System.out.print(ex);
        } finally {
            graph.readUnlockAll();
            graph.writeUnlock();
        }
    }

    @Override
    public String getReport() {
        sb.setLength(0);
        sb.append("<HTML><BODY><h1>").append(NbBundle.getMessage(
                BarberSVD_ModularityAlgorithm.class, "BarberBRIM_ModularityAlgorithm.report.alg.name")).append("</h1>");
        if ( isCanceled) {
            sb.append("<hr>").append(NbBundle.getMessage(
                    BarberSVD_ModularityAlgorithm.class, "BarberBRIM_ModularityAlgorithm.report.was.canceled"));
        } else {
            sb.append("<hr>").append(NbBundle.getMessage(BarberSVD_ModularityAlgorithm.class,
                    "BarberBRIM_ModularityAlgorithm.report.modules.discovered", modulesNumber))
                    .append("<br/>");
            sb.append(NbBundle.getMessage(BarberSVD_ModularityAlgorithm.class,
                    "BarberBRIM_ModularityAlgorithm.report.modularity.Q.value", getQ()));
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

    public String getQ() {
        return String.format("%.3f", Q);
    }

}
