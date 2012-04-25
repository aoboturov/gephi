package fr.enst.oboturov.bipartite;

import java.util.Comparator;

/**
 *
 * @author Artem OBOTUROV
 */
public class IntComparator implements Comparator<Integer> {

    @Override
    public int compare(Integer o1, Integer o2) {
        if ( null == o1) {
            return 0;
        }
        return o1.compareTo(o2);
    }

}
