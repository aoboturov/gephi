package fr.enst.oboturov.bipartite.algorithms.barber;

import cern.colt.matrix.DoubleMatrix2D;
import com.google.common.collect.TreeMultimap;
import fr.enst.oboturov.bipartite.IntComparator;
import fr.enst.oboturov.bipartite.NodeByIdComparator;
import fr.enst.oboturov.bipartite.algorithms.standard.GraphBipartitioning;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;

/**
 *
 * @author Artem OBOTUROV
 */
public class BRIM_ModularityAlgorithmTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testEvaluateQvalue() throws Exception {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        AttributeModel attributeModel = attributeController.getModel();
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();
        final fr.enst.oboturov.bipartite.plugin.GraphBipartitioning bipAlg =
            new fr.enst.oboturov.bipartite.plugin.GraphBipartitioning();
        final Graph graph = graphModel.getGraph();
        bipAlg.executeBipartAlg(graph, attributeModel);

        assert !bipAlg.isBipartiteGraph() : "Test file contains a graph which is not bipartite";

        final GraphBipartitioning gBip = new GraphBipartitioning(graph, bipAlg.getRedPartNum(), bipAlg.getBluePartNum());
        final TreeMultimap<Integer, Node> testResult = TreeMultimap.<Integer, Node>create(new IntComparator(), new NodeByIdComparator());
        // Deal with values other than 0 - e.g. by applying 'makeKeysetDense'.
        testResult.putAll(0, gBip.blueNodes);
        testResult.putAll(0, gBip.redNodes);
        final DoubleMatrix2D Bhat = BipartiteGraphMatrixRepresentation.matrixBhat(graph, gBip.redNodes, gBip.blueNodes);
        final double res = BRIM_ModularityAlgorithm.evaluateQvalue(
                graph.getEdgeCount(), Bhat, gBip.redNodes, gBip.blueNodes, testResult);
        
        assert res > 0.00000001D : "Summ of all elements of Bhat matrix must be very close to ZERO.";
    }

}
