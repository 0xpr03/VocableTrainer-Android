package vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage;

/**
 * Basic File entry only holding size & name
 */
public class BasicFileEntry {
    // type integer ids, to be used by constructor callers
    public static final int TYPE_FILE = 1;
    public static final int TYPE_DIR = 2;
    public static final int TYPE_VIRTUAL = -1;
    public static final int TYPE_UP = -2;

    final String size;
    final String name;
    final int typeID;
    final boolean underline;

    /**
     * New Basic FileEntry
     * @param name entry name column
     * @param size String for size column
     * @param typeID int for specifying the type of this entry (fe.: virtual entry)
     * @param underline underline this entry if true
     */
    public BasicFileEntry(final String name, final String size, final int typeID, final boolean underline){
        this.name = name;
        this.size = size;
        this.typeID = typeID;
        this.underline = underline;
    }

    public String getName(){
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
}
