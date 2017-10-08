package vocabletrainer.heinecke.aron.vocabletrainer.lib.Comparator;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Entry;

/**
 * Comparator for entries<br>
 * Checks for top entry objects according to their ID<br>
 * can't handle more than one head entry
 */
public class GenEntryComparator extends GenericComparator<Entry, String> {
    private final int headID;

    /**
     * Generic Entry Comparator
     *
     * @param retrievers retrievers to use for comparision<br>
     *                   passed array order defines the comparision priority
     * @param headID     ID of entry to set on top
     */
    public GenEntryComparator(ValueRetriever[] retrievers, final int headID) {
        super(retrievers);
        this.headID = headID;
    }

    @Override
    public int compare(Entry o1, Entry o2) {
        if (o1.getId() == headID)
            return -1;
        if (o2.getId() == headID)
            return 1;
        return super.compare(o1, o2);
    }

    /**
     * A retriever
     */
    public static final GenericComparator.ValueRetriever retA = new ValueRetriever<Entry, String>() {
        @Override
        public String getV(Entry obj) {
            return obj.getAWord();
        }
    };

    /**
     * B retriever
     */
    public static final GenericComparator.ValueRetriever retB = new ValueRetriever<Entry, String>() {
        @Override
        public String getV(Entry obj) {
            return obj.getBWord();
        }
    };

    /**
     * Tip retriever
     */
    public static final GenericComparator.ValueRetriever retTip = new ValueRetriever<Entry, String>() {
        @Override
        public String getV(Entry obj) {
            return obj.getTip();
        }
    };
}
