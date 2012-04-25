package fr.enst.oboturov.ui.bipartite.plugin;

import fr.enst.oboturov.bipartite.plugin.BarberSVD_ModularityAlgorithm;
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
public class BarberSVD_ModularityUI implements StatisticsUI {

    private final StatSettings settings = new StatSettings();
    private BarberSVD_ModularityAlgorithm mod;
    private BarberSVD_ModularityPanel panel;

    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "BarberSVD_ModularityUI.name");
    }

    @Override
    public int getPosition() {
        return 599;
    }

    @Override
    public JPanel getSettingsPanel() {
        return null;
//        panel = new BarberSVD_ModularityPanel();
//        return panel;
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return BarberSVD_ModularityAlgorithm.class;
    }

    @Override
    public String getValue() {
        return "  "+String.format("%.4f", mod.getQ_optimized());
    }

    @Override
    public void setup(Statistics statistics) {
        this.mod = (BarberSVD_ModularityAlgorithm) statistics;
        if ( null != panel) {
            settings.load(mod);
            panel.setQ_delta(mod.getDeltaQ_value());
        }
    }

    @Override
    public void unsetup() {
        if ( null != panel) {
            mod.setDeltaQ_value(panel.getQ_delta());
            settings.save(mod);
        }
        mod = null;
    }

    private static class StatSettings {

        private double dQ = BarberSVD_ModularityAlgorithm.SVD_Q_DELTA_DEFAULT;

        private void save(BarberSVD_ModularityAlgorithm stat) {
            this.dQ = stat.getDeltaQ_value();
        }

        private void load(BarberSVD_ModularityAlgorithm stat) {
            stat.setDeltaQ_value(dQ);
        }
        
    }

}
