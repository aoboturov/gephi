package fr.enst.oboturov.ui.bipartite.plugin;

import fr.enst.oboturov.bipartite.plugin.GraphBipartitioning;
import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Artem OBOTUROV
 */
@ServiceProvider(service = StatisticsUI.class)
public class GraphBipartitionUI implements StatisticsUI {

    private GraphBipartitioning bipartitioning;

    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "GraphBipartitionUI.name");
    }

    @Override
    public int getPosition() {
        return 598;
    }

    @Override
    public JPanel getSettingsPanel() {
        return null;
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return GraphBipartitioning.class;
    }

    @Override
    public String getValue() {
        return bipartitioning.isBipartiteGraph() ? "yes" : "not";
    }

    @Override
    public void setup(Statistics statistics) {
        bipartitioning = (GraphBipartitioning)statistics;
    }

    @Override
    public void unsetup() {
        bipartitioning = null;
    }
 
}
