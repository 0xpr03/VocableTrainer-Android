package vocabletrainer.heinecke.aron.vocabletrainer.lib.Comparator;


import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.BasicFileEntry;

/**
 * Generic FileEntry comparator
 *
 * @param <V>
 */
public class GenFileEntryComparator<V extends Comparable> extends GenericComparator<BasicFileEntry, V> {
    /**
     * Generic Comparator for BasicFileEntry
     *
     * @param retrievers retrievers to use for comparision<br>
     *                   passed array order defines the comparision priority
     */
    public GenFileEntryComparator(ValueRetriever[] retrievers) {
        super(retrievers);
    }

    /**
     * type retriever<br>
     * should be used as first retriever to guarantee correct type orders
     */
    public static final GenericComparator.ValueRetriever retType = new ValueRetriever<BasicFileEntry, Integer>() {
        @Override
        public Integer getV(BasicFileEntry obj) {
            if (obj.getTypeID() == BasicFileEntry.TYPE_DIR)
                return -1;
            if (obj.getTypeID() == BasicFileEntry.TYPE_UP)
                return -2;
            else
                return 0;
        }
    };

    /**
     * name retriever
     */
    public static final GenericComparator.ValueRetriever retName = new ValueRetriever<BasicFileEntry, String>() {
        @Override
        public String getV(BasicFileEntry obj) {
            return obj.getName();
        }
    };

    /**
     * size retriever
     */
    public static final GenericComparator.ValueRetriever retSize = new ValueRetriever<BasicFileEntry, Long>() {
        @Override
        public Long getV(BasicFileEntry obj) {
            return obj.getISize();
        }
    };
}
