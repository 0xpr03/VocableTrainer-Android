package vocabletrainer.heinecke.aron.vocabletrainer.lib.Comparator;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VEntry;

/**
 * Comparator for entries<br>
 * Checks for top entry objects according to their ID<br>
 * can't handle more than one head entry
 */
public class GenEntryComparator extends GenericComparator<VEntry, String> {
    private final int headID;

    /**
     * Generic VEntry Comparator
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
    public int compare(VEntry o1, VEntry o2) {
        if (o1.getId() == headID)
            return -1;
        if (o2.getId() == headID)
            return 1;
        return super.compare(o1, o2);
    }

    /**
     * A retriever
     */
    public static final GenericComparator.ValueRetriever retA = new ValueRetriever<VEntry, String>() {
        @Override
        public String getV(VEntry obj) {
            return obj.getAString();
        }
    };

    /**
     * B retriever
     */
    public static final GenericComparator.ValueRetriever retB = new ValueRetriever<VEntry, String>() {
        @Override
        public String getV(VEntry obj) {
            return obj.getBString();
        }
    };

    /**
     * Tip retriever
     */
    public static final GenericComparator.ValueRetriever retTip = new ValueRetriever<VEntry, String>() {
        @Override
        public String getV(VEntry obj) {
            return obj.getTip();
        }
    };
}
