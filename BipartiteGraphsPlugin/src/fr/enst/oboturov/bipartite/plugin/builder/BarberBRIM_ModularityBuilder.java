package fr.enst.oboturov.bipartite.plugin.builder;

import fr.enst.oboturov.bipartite.plugin.BarberBRIM_ModularityAlgorithm;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Artem OBOTUROV
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class BarberBRIM_ModularityBuilder implements StatisticsBuilder {

    @Override
    public String getName() {
        return NbBundle.getMessage(BarberBRIM_ModularityBuilder.class, "BRIM.Modularity.name");
    }

    @Override
    public Statistics getStatistics() {
        return new BarberBRIM_ModularityAlgorithm();
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return BarberBRIM_ModularityAlgorithm.class;
    }

}
