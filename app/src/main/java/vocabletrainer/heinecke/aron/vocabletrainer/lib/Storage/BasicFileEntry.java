package vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage;

/**
 * Basic File entry only holding size & name
 */
public class BasicFileEntry {
    // type integer ids, to be used by constructor callers
    public static final int TYPE_FILE = 1;
    public static final int TYPE_DIR = 2;
    public static final int TYPE_UP = -2;

    private final long iSize;
    private final String size;
    private final String name;
    private final int typeID;
    private final boolean underline;

    /**
     * New Basic FileEntry
     *
     * @param name      entry name column
     * @param size      String for size column
     * @param iSize     long for size comparision
     * @param typeID    int for specifying the type of this entry (fe.: virtual entry)
     * @param underline underline this entry if true
     */
    public BasicFileEntry(final String name, final String size, final long iSize, final int typeID, final boolean underline) {
        this.name = name;
        this.size = size;
        this.typeID = typeID;
        this.underline = underline;
        this.iSize = iSize;
    }

    public String getName() {
        return name;
    }

    public String getSize() {
        return size;
    }

    public boolean isUnderline() {
        return underline;
    }

    public int getTypeID() {
        return typeID;
    }

    public long getISize() {
        return iSize;
    }
}
