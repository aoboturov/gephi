package fr.enst.oboturov.bipartite.algorithms.barber;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import com.google.common.collect.TreeMultimap;
import fr.enst.oboturov.bipartite.IntComparator;
import fr.enst.oboturov.bipartite.NodeByIdComparator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Artem OBOTUROV
 */
public class BRIM_ModularityAlgorithm {

    private static final Logger logger = Logger.getLogger(BRIM_ModularityAlgorithm.class.getName());

    public final static int ILLEGAL_KEY_MAP_VAL = -1;
    public final static int INITIAL_KEY_MAP_VAL = 0;

    /**
     * Select a half of the total number of nodes to be reassigned and do
     * actual reassignment.
     * @param partitioning a collection used to store nodes allocated in with
     * respect to modules.
     * @param nodesNumToBeReassigned the number of nodes to be reassigned from
     * old modules to new ones.
     * @param oldModulesNum the number of old modules used for allocation.
     * @param newModulesNum the number of new modules available for allocation.
     */
    public static void doNodesReassignment(
            final TreeMultimap<Integer, Node> partitioning,
            final int oldModulesNum,
            final int newModulesNum,
            final int nodesNumToBeReassigned) {
        final Random nodesOriginRnd = new Random(System.nanoTime());
        final Random modulesOriginRnd = new Random(System.nanoTime());
        final Random modulesAssignmentRnd = new Random(System.nanoTime());
        for (int nodesReassigned = 0; nodesReassigned <= nodesNumToBeReassigned; ++nodesReassigned) {
            // Choose a node to be reassigned.
            Node nodeToReassign = null;
            SortedSet<Node> moduleOfOrigin = null;
            int moduleOfOriginNum = -1;
            //  Get a module - origin of the node to be reassigned.
            do {
                moduleOfOriginNum = modulesOriginRnd.nextInt(oldModulesNum);
                moduleOfOrigin = partitioning.get(moduleOfOriginNum);
                if (null == moduleOfOrigin || moduleOfOrigin.isEmpty()) {
                    continue;
                }
                break;
            } while (true);
            // Get a node to be reassigned.
            final Object[] moduleOfOriginContent = moduleOfOrigin.toArray();
            nodeToReassign = (Node) moduleOfOriginContent[nodesOriginRnd.nextInt(moduleOfOrigin.size())];
            partitioning.remove(moduleOfOriginNum, nodeToReassign);
            // Get a module where to assign to.
            int moduleOfAssignment = oldModulesNum + modulesAssignmentRnd.nextInt(newModulesNum - oldModulesNum);
            // Put node there.
            partitioning.put(moduleOfAssignment, nodeToReassign);
        }
        makeKeysetDense(partitioning);
    }

    /**
     * Evaluate the value of the Q-modularity function.
     *
     * First, a matrix multiplication of R^T*B-hat is done: for each module c and
     * each red-node belonging to c made an accumulation in temporary buffer.
     * It should be quite effective since R is 0-1 matrix, and multiplication
     * of a row of the R^T to a column of the B-hat is just summation of elements
     * from a column of the B-hat where corresponding values for a row of the R^T
     * are not equal to zero.
     *
     * Q-function is defined as tray of matrix product. In this case we have to
     * obtain only diagonal elements of resulted matrix.
     *
     * @param m number of edges.
     * @param Bhat matrix of graph.
     * @param redNodes from the result of bipartitioning algorithm.
     * @param blueNodes from the result of bipartitioning algorithm.
     * @param partitioning used when calculating Q-function value.
     * @return the value of the modularity Q-function.
     */
    public static double evaluateQvalue(
            final int m,
            final DoubleMatrix2D Bhat,
            final List<Node> redNodes,
            final List<Node> blueNodes,
            final TreeMultimap<Integer, Node> partitioning
            ) {
        final int modulesCnt = partitioning.keySet().size();
        final DoubleMatrix2D Rt = Bhat.like(modulesCnt, redNodes.size());
        final DoubleMatrix2D T = Bhat.like(blueNodes.size(), modulesCnt);
        final Comparator<Node> nIdCmp = new NodeByIdComparator();
        Collections.sort(redNodes, nIdCmp);
        Collections.sort(blueNodes, nIdCmp);
        for ( Entry<Integer, Node> entry : partitioning.entries()) {
            final Node node = entry.getValue();
            if ( fr.enst.oboturov.bipartite.algorithms.standard.GraphBipartitioning.RED_CLASS == node.getNodeData().getAttributes().getValue(
                        fr.enst.oboturov.bipartite.algorithms.standard.GraphBipartitioning.BIPARTITION_CLASS)) {
                final int redNodeIndex = Collections.binarySearch(redNodes, node, nIdCmp);
//                        redNodes.indexOf(node);
                Rt.set(entry.getKey(), redNodeIndex, 1D);
            } else {
                final int blueNodeIndex = Collections.binarySearch(blueNodes, node, nIdCmp);
//                final int blueNodeIndex = blueNodes.indexOf(node);
                T.set(blueNodeIndex, entry.getKey(), 1D);
            }
        }
        final Algebra a = new Algebra();
        final DoubleMatrix2D RtBhat = a.mult(Rt, Bhat);
        final DoubleMatrix2D prod = a.mult(RtBhat, T);
        final double res = a.trace(prod);
        if ( logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, Rt.toString());
            logger.log(Level.FINER, T.toString());
            logger.log(Level.FINER, prod.toString());
        }
        return res/m;
    }

    /**
     * Produce a partition with optimized modularity when blue nodes are
     * assumed to be fixated.
     * @param Bhat matrix of bipartite graph.
     * @param redNodes of a bipartite graph.
     * @param blueNodes of a bipartite graph.
     * @param partitioning to used as a start point for optimization.
     * @return an optimized partition on assumption of fixed blue nodes.
     */
    public static TreeMultimap<Integer, Node> redLocalOptimisation(
            final DoubleMatrix2D Bhat,
            final List<Node> redNodes,
            final List<Node> blueNodes,
            final TreeMultimap<Integer, Node> partitioning
            )
    {
        final TreeMultimap<Integer, Node> redOptimalPart = TreeMultimap
                .<Integer, Node>create(new IntComparator(), new NodeByIdComparator());

        for ( Entry<Integer, Node> entry : partitioning.entries()) {
            final int redNodeIndex = redNodes.indexOf(entry.getValue());
            if ( fr.enst.oboturov.bipartite.algorithms.standard.GraphBipartitioning.RED_CLASS ==
                    entry.getValue().getNodeData().getAttributes().getValue(
                        fr.enst.oboturov.bipartite.algorithms.standard.GraphBipartitioning.BIPARTITION_CLASS)
                        && -1 != redNodeIndex)
            {
                // Evaluate redNodeIndex-th row of Bhat*T matrix.
                double maxVal = -1D;
                int argMax = -1;
                for ( int c=0; c<partitioning.keySet().size(); ++c) {
                    double cVal = 0D;
                    for ( Node blueNode : partitioning.get(c)) {
                        int blueNodeIndex = blueNodes.indexOf(blueNode);
                        if ( -1 != blueNodeIndex) {
                            cVal += Bhat.get(redNodeIndex, blueNodeIndex);
                        }
                    }
                    if ( cVal > maxVal) {
                        maxVal = cVal;
                        argMax = c;
                    }
                }
                // Assign argmax of this row as new module allocation of red node.
                redOptimalPart.put(argMax, entry.getValue());
            } else {
                redOptimalPart.put(entry.getKey(), entry.getValue());
            }
        }
        makeKeysetDense(redOptimalPart);
        return redOptimalPart;
    }

    /**
     * When some indices were pivoted it could exist an index which is bigger
     * than the total number of indices, so the partitioning must be made dense.
     * @param partitioning to be made dense.
     * @return gripped partitioning.
     */
    private static TreeMultimap<Integer, Node> makeKeysetDense(
            final TreeMultimap<Integer, Node> partitioning)
    {
        // Make key mapping.
        final Integer maxKeyVal = partitioning.keySet().last();
        final Integer[] keyMap = new Integer[maxKeyVal+1];
        Arrays.fill(keyMap, ILLEGAL_KEY_MAP_VAL);
        int c=0;
        for ( Integer key : partitioning.keySet()) {
            keyMap[key] = c++;
        }
        // Check if mapping need to be done: no need for ID mapping.
        boolean noMappingNeedToBeDone = true;
        for ( int i=0; i<keyMap.length; ++i) {
            if ( keyMap[i] != i) {
                noMappingNeedToBeDone = false;
                break;
            }
        }
        if ( noMappingNeedToBeDone) {
            return partitioning;
        }
        // Make keyset denense.
        for ( int i=0; i<keyMap.length; ++i) {
            if ( ILLEGAL_KEY_MAP_VAL != keyMap[i]) {
                partitioning.putAll(keyMap[i], partitioning.removeAll(i));
            }
        }
        return partitioning;
    }

    /**
     * Produce a partition with optimized modularity when red nodes are
     * assumed to be fixated.
     * @param Bhat matrix of bipartite graph.
     * @param redNodes of a bipartite graph.
     * @param blueNodes of a bipartite graph.
     * @param partitioning to used as a start point for optimization.
     * @return an optimized partition on assumption of fixed red nodes.
     */
    public static TreeMultimap<Integer, Node> blueLocalOptimisation(
            final DoubleMatrix2D Bhat,
            final List<Node> redNodes,
            final List<Node> blueNodes,
            final TreeMultimap<Integer, Node> partitioning
            )
    {
        final TreeMultimap<Integer, Node> blueOptimalPart = TreeMultimap
                .<Integer, Node>create(new IntComparator(), new NodeByIdComparator());

        for ( Entry<Integer, Node> entry : partitioning.entries()) {
            final int blueNodeIndex = blueNodes.indexOf(entry.getValue());
            if ( fr.enst.oboturov.bipartite.algorithms.standard.GraphBipartitioning.BLUE_CLASS ==
                    entry.getValue().getNodeData().getAttributes().getValue(
                        fr.enst.oboturov.bipartite.algorithms.standard.GraphBipartitioning.BIPARTITION_CLASS)
                        && -1 != blueNodeIndex)
            {
                // Evaluate redNodeIndex-th row of Bhat*T matrix.
                double maxVal = -1D;
                int argMax = -1;
                for ( int c=0; c<partitioning.keySet().size(); ++c) {
                    double cVal = 0D;
                    for ( Node redNode : partitioning.get(c)) {
                        int redNodeIndex = redNodes.indexOf(redNode);
                        if ( -1 != redNodeIndex) {
                            cVal += Bhat.get(redNodeIndex, blueNodeIndex);
                        }
                    }
                    if ( cVal > maxVal) {
                        maxVal = cVal;
                        argMax = c;
                    }
                }
                // Assign argmax of this row as new module allocation of red node.
                blueOptimalPart.put(argMax, entry.getValue());
            } else {
                blueOptimalPart.put(entry.getKey(), entry.getValue());
            }
        }
        makeKeysetDense(blueOptimalPart);
        return blueOptimalPart;
    }

    /**
     * Recursive Identification of Bipartite Modules procedure.
     * @param edgesCount of the original graph.
     * @param Bhat of the original graph.
     * @param redNodes of the graph bipartitioning.
     * @param blueNodes of the graph bipartitioning.
     * @param originalPartitioning to be used as a start point of optimization.
     * @return optimized partitioning.
     */
    public static TreeMultimap<Integer, Node> brimOptimizationProcedure(
            final int edgesCount,
            final DoubleMatrix2D Bhat,
            final List<Node> redNodes,
            final List<Node> blueNodes,
            final TreeMultimap<Integer, Node> originalPartitioning
            )
    {
        double dQ = BRIM_ModularityAlgorithm.evaluateQvalue(edgesCount, Bhat, redNodes, blueNodes, originalPartitioning);
        TreeMultimap<Integer, Node> optimizedPartitioning = originalPartitioning;
        TreeMultimap<Integer, Node> localOptimumPartitioning = originalPartitioning;
        for ( int i = 0, cnt = 0; cnt < 3; ++i, ++cnt) {
            double newDQ = 0D;
            optimizedPartitioning = i%2 == 0 ?
                BRIM_ModularityAlgorithm.redLocalOptimisation(Bhat, redNodes, blueNodes, optimizedPartitioning)
                :
                BRIM_ModularityAlgorithm.blueLocalOptimisation(Bhat, redNodes, blueNodes, optimizedPartitioning);
            newDQ = BRIM_ModularityAlgorithm.evaluateQvalue(edgesCount, Bhat, redNodes, blueNodes, optimizedPartitioning);
            if ( logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, NbBundle.getMessage(BRIM_ModularityAlgorithm.class,
                        "BRIM_ModularityAlgorithm.trace.brim.optimization.procedure", newDQ,
                        optimizedPartitioning.keySet().size(), cnt));
            }
            if ( newDQ > dQ) {
                localOptimumPartitioning = optimizedPartitioning;
                dQ = newDQ;
                cnt = 0;
            }
        }
        return localOptimumPartitioning;
    }

}
