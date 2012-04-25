import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.junit.Test;
import org.openide.util.Lookup;

/**
 * @author Artem OBOTUROV
 */
public class SimpleTest {

    @Test
    public void testEvaluateQvalue() throws Exception {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        AttributeModel attributeModel = attributeController.getModel();
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();
    }

}
