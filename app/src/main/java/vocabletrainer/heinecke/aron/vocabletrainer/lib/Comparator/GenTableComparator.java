package vocabletrainer.heinecke.aron.vocabletrainer.lib.Comparator;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;

/**
 * Generic comparator for lists<br>
 * Checks for top table objects according to their ID<br>
 * can't handle more than one head table
 */
public class GenTableComparator extends GenericComparator<VList, String> {

    private final long headID;

    /**
     * Generic VList Comparator
     *
     * @param retrievers retrievers to use for comparision<br>
     *                   passed array order defines the comparision priority
     * @param headID     ID of table to set on top
     */
    public GenTableComparator(ValueRetriever[] retrievers, final long headID) {
        super(retrievers);
        this.headID = headID;
    }

    @Override
    public int compare(VList o1, VList o2) {
        if (o1.getId() == headID)
            return -1;
        if (o2.getId() == headID)
            return 1;
        return super.compare(o1, o2);
    }

    /**
     * Name retriever
     */
    public static final GenericComparator.ValueRetriever retName = new GenericComparator.ValueRetriever<VList, String>() {
        @Override
        public String getV(VList obj) {
            return obj.getName();
        }
    };

    /**
     * A retriever
     */
    public static final GenericComparator.ValueRetriever retA = new GenericComparator.ValueRetriever<VList, String>() {
        @Override
        public String getV(VList obj) {
            return obj.getNameA();
        }
    };

    /**
     * B retriever
     */
    public static final GenericComparator.ValueRetriever retB = new GenericComparator.ValueRetriever<VList, String>() {
        @Override
        public String getV(VList obj) {
            return obj.getNameB();
        }
    };
}
