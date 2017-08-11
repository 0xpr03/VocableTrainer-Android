package vocabletrainer.heinecke.aron.vocabletrainer.lib.Import;

/**
 * Interface for import handlers
 */
public interface ImportHandler {
    /**
     * Called when a new table starts<br>
     *     Note that this function is not called when the provided source doesn't have table metadata.
     * @param name Table name
     * @param columnA Column A name
     * @param columnB Column B name
     */
    void newTable(String name, String columnA, String columnB);

    /**
     * Called for a new Entry, for last "newTable" called
     * @param A
     * @param B
     * @param Tipp
     */
    void newEntry(String A, String B, String Tipp);

    /**
     * Called when all entries where read
     */
    void finish();

    /**
     * Called directly before the start
     */
    void start();
}
