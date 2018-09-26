package vocabletrainer.heinecke.aron.vocabletrainer.lib.Import;

import java.util.List;

/**
 * Interface for import handlers
 */
public interface ImportHandler {
    /**
     * Called when a new table starts<br>
     * Note that this function is not called when the provided source doesn't have table metadata.
     *
     * @param name    VList name
     * @param columnA Column A name
     * @param columnB Column B name
     */
    void newTable(String name, String columnA, String columnB);

    /**
     * Called for a new VEntry, for last "newTable" called
     *
     * @param A list of A meanings
     * @param B list of B meanings
     * @param Tip tip
     * @param addition addition for vocable
     */
    void newEntry(List<String> A, List<String> B, String Tip, String addition);

    /**
     * Called when all entries where read
     */
    void finish();

    /**
     * Called directly before the start
     */
    void start();

    /**
     * Called on cancel
     */
    void cancel();
}
