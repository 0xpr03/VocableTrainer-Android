package vocabletrainer.heinecke.aron.vocabletrainer.lib.Comparator;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Table;

import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.ID_RESERVED_SKIP;

/**
 * Generic comparator for tables<br>
 * Checks for top table objects according to their ID<br>
 * can't handle more than one head table
 */
public class GenTableComparator extends GenericComparator<Table, String> {

    private final int headID;

    /**
     * Generic Table Comparator
     *
     * @param retrievers retrievers to use for comparision<br>
     *                   passed array order defines the comparision priority
     * @param headID     ID of table to set on top
     */
    public GenTableComparator(ValueRetriever[] retrievers, final int headID) {
        super(retrievers);
        this.headID = headID;
    }

    @Override
    public int compare(Table o1, Table o2) {
        if (o1.getId() == headID)
            return -1;
        if (o2.getId() == headID)
            return 1;
        return super.compare(o1, o2);
    }

    /**
     * Name retriever
     */
    public static final GenericComparator.ValueRetriever retName = new GenericComparator.ValueRetriever<Table, String>() {
        @Override
        public String getV(Table obj) {
            return obj.getName();
        }
    };

    /**
     * A retriever
     */
    public static final GenericComparator.ValueRetriever retA = new GenericComparator.ValueRetriever<Table, String>() {
        @Override
        public String getV(Table obj) {
            return obj.getNameA();
        }
    };

    /**
     * B retriever
     */
    public static final GenericComparator.ValueRetriever retB = new GenericComparator.ValueRetriever<Table, String>() {
        @Override
        public String getV(Table obj) {
            return obj.getNameB();
        }
    };
}
