package fr.enst.oboturov.bipartite.algorithms.barber;

import cern.colt.matrix.DoubleMatrix1D;
import com.google.common.collect.TreeMultimap;
import fr.enst.oboturov.bipartite.IntComparator;
import fr.enst.oboturov.bipartite.NodeByIdComparator;
import java.util.List;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

import static fr.enst.oboturov.bipartite.algorithms.barber.BRIM_ModularityAlgorithm.INITIAL_KEY_MAP_VAL;

/**
 *
 * @author Artem OBOTUROV
 */
public class SVD_ModularityAlgorithm {

    public static final String SVD_MODULARITY = "svd_modularity";

    public static TreeMultimap<Integer, Node> modularityFunctionNegativeComponent(
            final List<Node> redNodes,
            final List<Node> blueNodes,
            final DoubleMatrix1D u,
            final DoubleMatrix1D v
            ) {
        final TreeMultimap<Integer, Node> negativePartition = TreeMultimap
            .<Integer, Node>create(new IntComparator(), new NodeByIdComparator());
        final int redSize = redNodes.size();
        for ( int i=0; i<redSize; ++i) {
            if ( u.get(i) < 0D) {
                negativePartition.put(INITIAL_KEY_MAP_VAL, redNodes.get(i));
            }
        }
        final int blueSize = blueNodes.size();
        for ( int i=0; i<blueSize; ++i) {
            try {
            if ( v.get(i) < 0D) {
                negativePartition.put(INITIAL_KEY_MAP_VAL, blueNodes.get(i));
            }
            } catch (Exception ex){
                System.out.println(ex);
            }
        }
        return negativePartition;
    }

    public static TreeMultimap<Integer, Node> modularityFunctionPositiveComponent(
            final List<Node> redNodes,
            final List<Node> blueNodes,
            final DoubleMatrix1D u,
            final DoubleMatrix1D v
            ) {
        final TreeMultimap<Integer, Node> positivePartition = TreeMultimap
            .<Integer, Node>create(new IntComparator(), new NodeByIdComparator());
        final int redSize = redNodes.size();
        for ( int i=0; i<redSize; ++i) {
            if ( u.get(i) >= 0D) {
                positivePartition.put(INITIAL_KEY_MAP_VAL, redNodes.get(i));
            }
        }
        final int blueSize = blueNodes.size();
        for ( int i=0; i<blueSize; ++i) {
            if ( v.get(i) >= 0D) {
                positivePartition.put(INITIAL_KEY_MAP_VAL, blueNodes.get(i));
            }
        }
        return positivePartition;
    }

    /**
     * Maximize the value of an approximation of the modularity function done
     * by substituting B-hat matrix by scalar product u*v', where u and v
     * are singular vectors for the maximal singular value in SVD decomposition.
     * @param vector singular vector of B-hat matrix decomposition for maximal singular value.
     * @param nodes of red or blue type.
     * @param negValsGraph a graph which will have only nodes corresponding to
     * negative values of the singular vector.
     * @param posValsGraph a graph which will have only nodes corresponding to
     * positive values of the singular vector.
     */
    public static void vectorElementSignBasedNodeAssignmentProcedure(
            final DoubleMatrix1D vector, final List<Node> nodes, final Graph negValsGraph, final Graph posValsGraph)
    {
        for ( int i=0; i<vector.size(); ++i) {
            Node blueNode = nodes.get(i);
            Attributes row = blueNode.getNodeData().getAttributes();
            if ( vector.get(i) < 0D) {
                final Node toDelete = posValsGraph.getNode(blueNode.getId());
                if ( null != toDelete) {
                    posValsGraph.removeNode(toDelete);
                }
                row.setValue(SVD_MODULARITY, negValsGraph.getView().getViewId());
            } else {
                final Node toDelete = negValsGraph.getNode(blueNode.getId());
                if ( null != toDelete) {
                    negValsGraph.removeNode(toDelete);
                }
                row.setValue(SVD_MODULARITY, posValsGraph.getView().getViewId());
            }
        }
    }

}
