package fr.enst.oboturov.bipartite.plugin.builder;

import fr.enst.oboturov.bipartite.plugin.GraphBipartitioning;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Artem OBOTUROV
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class GraphBipartitioningBuilder implements StatisticsBuilder {

    @Override
    public String getName() {
        return NbBundle.getMessage(getClass(), "BipartiteGraphBuilder.name");
    }

    @Override
    public Statistics getStatistics() {
        return new GraphBipartitioning();
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return GraphBipartitioning.class;
    }

}
