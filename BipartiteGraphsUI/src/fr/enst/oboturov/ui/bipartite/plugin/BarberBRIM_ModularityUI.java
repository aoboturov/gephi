package fr.enst.oboturov.ui.bipartite.plugin;

import fr.enst.oboturov.bipartite.plugin.BarberBRIM_ModularityAlgorithm;
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
public class BarberBRIM_ModularityUI implements StatisticsUI {

    private BarberBRIM_ModularityAlgorithm brimAlg;

    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "BarberBRIM_ModularityUI.name");
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
        return BarberBRIM_ModularityAlgorithm.class;
    }

    @Override
    public String getValue() {
        return brimAlg.getQ();
    }

    @Override
    public void setup(Statistics statistics) {
        brimAlg = (BarberBRIM_ModularityAlgorithm)statistics;
    }

    @Override
    public void unsetup() {
        brimAlg = null;
    }

}
