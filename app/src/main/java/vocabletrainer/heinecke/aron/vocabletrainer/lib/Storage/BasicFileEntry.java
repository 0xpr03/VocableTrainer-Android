package vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage;

/**
 * Basic File entry only holding size & name
 */
public class BasicFileEntry {
    // type integer ids, to be used by constructor callers
    public static final int TYPE_FILE = 1;
    public static final int TYPE_DIR = -1;
    public static final int TYPE_UP = -2;
    public static final int TYPE_SD_CARD = -3;
    public static final int TYPE_INTERNAL = -4;

    private final long iSize;
    private final String size;
    private final String name;
    private final int typeID;
    private final boolean underline;
    private boolean selected;

    /**
     * New Basic FileEntry, unselected
     *
     * @param name      entry name column
     * @param size      String for size column
     * @param iSize     long for size comparision
     * @param typeID    int for specifying the type of this entry (fe.: virtual entry)
     * @param underline underline this entry if true
     */
    public BasicFileEntry(final String name, final String size, final long iSize, final int typeID,
                          final boolean underline) {
        this(name,size,iSize,typeID,underline,false);
    }

    /**
     * New Basic FileEntry
     *
     * @param name      entry name column
     * @param size      String for size column
     * @param iSize     long for size comparision
     * @param typeID    int for specifying the type of this entry (fe.: virtual entry)
     * @param underline underline this entry if true
     * @param selected  Whether item is selected
     */
    public BasicFileEntry(final String name, final String size, final long iSize, final int typeID, final boolean underline,
                          boolean selected) {
        this.name = name;
        this.size = size;
        this.typeID = typeID;
        this.underline = underline;
        this.iSize = iSize;
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
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
