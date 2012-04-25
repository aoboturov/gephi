package fr.enst.oboturov.bipartite.plugin.builder;

import fr.enst.oboturov.bipartite.plugin.BarberSVD_ModularityAlgorithm;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.NbBundle;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;

/**
 *
 * @author Artem OBOTUROV
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class BarberSVD_ModularityBuilder implements StatisticsBuilder {

    @Override
    public String getName() {
        return NbBundle.getMessage(BarberSVD_ModularityBuilder.class, "SVD.Modularity.name");
    }

    @Override
    public Statistics getStatistics() {
        return new BarberSVD_ModularityAlgorithm();
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return BarberSVD_ModularityAlgorithm.class;
    }

}
