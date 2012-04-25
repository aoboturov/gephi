package fr.enst.oboturov.bipartite;

import java.util.Comparator;
import org.gephi.graph.api.Node;

/**
 * Since {@link org.gephi.graph.api.Node} equals operation treats different
 * nodes with the same ID like different object we have to create a
 * {@link java.util.Comparator} suitable for considering nodes equal when they
 * have equal IDs.
 * @author Artem OBOTUROV
 */
public class NodeByIdComparator implements Comparator<Node> {

    /**
     * @exception is thrown if one of objects in null.
     */
    @Override
    public int compare(Node lhs, Node rhs) throws NullPointerException {
        return lhs.getId() - rhs.getId();
    }
    
}
