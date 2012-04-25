package fr.enst.oboturov.bipartite.algorithms.barber;

import cern.colt.function.DoubleDoubleFunction;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import fr.enst.oboturov.bipartite.NodeByIdComparator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.openide.util.NbBundle;

/**
 * @author Artem OBOTUROV
 */
public class BipartiteGraphMatrixRepresentation {

    private static final Logger logger = Logger.getLogger(BipartiteGraphMatrixRepresentation.class.getName());

    /**
     * Theoretical cost is |E|.
     * @return part of adjacency matrix for bipartite graph.
     */
    public static DoubleMatrix2D adjacencyMatrix(final Graph graph, final List<Node> redNodes, final List<Node> blueNodes) {
        DoubleMatrix2D adjacencyMatrix = DoubleFactory2D.sparse.make(redNodes.size(), blueNodes.size());
        final Comparator<Node> nIdCmp = new NodeByIdComparator();
        Collections.sort(redNodes, nIdCmp);
        Collections.sort(blueNodes, nIdCmp);
        for (Edge e : graph.getEdges()) {
            int redIndex = Collections.binarySearch(redNodes, e.getSource(), nIdCmp);
            int blueIndex = Collections.binarySearch(blueNodes, e.getSource(), nIdCmp);
            // In case this edge is of our consern (belongs to considered part of the graph).
            if ( !( 0 > redIndex && 0 > blueIndex)) {
                if ( 0 > redIndex) {
                    redIndex = Collections.binarySearch(redNodes, e.getTarget(), nIdCmp);
                } else {
                    blueIndex = Collections.binarySearch(blueNodes, e.getTarget(), nIdCmp);
                }
                adjacencyMatrix.set(redIndex, blueIndex, 1D);
            }
        }
        if ( logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, NbBundle.getMessage(BipartiteGraphMatrixRepresentation.class,
                    "BipartiteGraphMatrixRepresentation.matrix.adjacency", adjacencyMatrix));
        }
        return adjacencyMatrix;
    }

    /**
     * @return B-hat matrix from Newman paper.
     */
    public static DoubleMatrix2D matrixBhat(final Graph graph, final List<Node> redNodes, final List<Node> blueNodes) {
        final DoubleMatrix2D A = adjacencyMatrix(graph, redNodes, blueNodes);
        final double m = graph.getEdgeCount();
        final Algebra algebra = new Algebra();
        final DoubleMatrix1D rOnes = A.like1D(redNodes.size());
        final DoubleMatrix1D bOnes = A.like1D(blueNodes.size());
        rOnes.assign(1);
        bOnes.assign(1/m);
        final DoubleMatrix1D kRed = algebra.mult(A, bOnes);
        final DoubleMatrix1D dBlue = algebra.mult(algebra.transpose(A), rOnes);
        final DoubleMatrix2D P = algebra.multOuter(kRed, dBlue, A.like());
        A.assign(P, new DoubleDoubleFunction() {

            @Override
            public double apply(double lval, double rval) {
                return lval-rval;
            }
            
        });
        if ( logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, NbBundle.getMessage(BipartiteGraphMatrixRepresentation.class,
                    "BipartiteGraphMatrixRepresentation.matrix.beta_hat", A));
        }
        return A;
    }

    /**
     * Use null-model matrix from M. E. J. Newman paper.
     * @return
     */
    public static DoubleMatrix2D nullModelMatrix(final Graph graph, final List<Node> redNodes, final List<Node> blueNodes) {
        final DoubleMatrix2D nullModelMatrix = DoubleFactory2D.sparse.make(redNodes.size(), blueNodes.size());
        final double twoM = 2*graph.getEdgeCount();
        for (int i=0; i<redNodes.size(); ++i) {
            for (int j=0; j<blueNodes.size(); ++j) {
                final double prod = graph.getNeighbors(redNodes.get(i)).toArray().length*
                        graph.getNeighbors(blueNodes.get(j)).toArray().length;
                nullModelMatrix.set(i, j, prod/twoM);
            }
        }
        if ( logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, NbBundle.getMessage(BipartiteGraphMatrixRepresentation.class
                    , "BipartiteGraphMatrixRepresentation.matrix.null_model", nullModelMatrix));
        }
        return nullModelMatrix;
    }

}
