package vocabletrainer.heinecke.aron.vocabletrainer.lib.Comparator;

import java.util.Comparator;

/**
 * Generic Comparator<br>
 * Allows for comparision over multiple values<br>
 *
 * @param <T>
 */
public class GenericComparator<T, V extends Comparable> implements Comparator<T> {
    private ValueRetriever<T, V>[] retrievers;

    /**
     * Generic Comparator
     *
     * @param retrievers retrievers to use for comparision<br>
     *                   passed array order defines the comparision priority
     */
    GenericComparator(ValueRetriever[] retrievers) {
        this.retrievers = retrievers;
    }

    @Override
    public int compare(T o1, T o2) {
        int v = 0;
        int i = 0;
        while (v == 0 && i < retrievers.length) {
            v = retrievers[i].getV(o1).compareTo(retrievers[i].getV(o2));
            i++;
        }
        return v;
    }

    /**
     * Retrieves values for comparision
     *
     * @param <T>
     * @param <V>
     */
    public static abstract class ValueRetriever<T, V> {
        /**
         * Returns value of object to compare with
         *
         * @param obj
         * @return
         */
        public abstract V getV(T obj);
    }
}
