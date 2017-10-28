package vocabletrainer.heinecke.aron.vocabletrainer.lib.Import;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;

import static android.content.ContentValues.TAG;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.MIN_ID_TRESHOLD;

/**
 * Importer, does actual importing
 */
public class Importer implements ImportHandler {

    private static final int BUFFER_CAPACITY = 100;

    private PreviewParser previewParser;
    private IMPORT_LIST_MODE mode;
    private VList overrideList;
    private VList currentList;
    private Database db;
    private ArrayList<VEntry> insertBuffer = new ArrayList<>(BUFFER_CAPACITY);
    private boolean ignoreEntries;

    public Importer(Context context, PreviewParser previewParser, IMPORT_LIST_MODE mode, VList overrideList) {
        if (previewParser.isRawData() && overrideList == null) {
            Log.e(TAG, "RawData without passed table!");
            throw new IllegalArgumentException("Missing table!");
        }
        this.previewParser = previewParser;
        this.mode = mode;
        this.overrideList = overrideList;
        db = new Database(context);
        ignoreEntries = false;
    }

    @Override
    public void start() {
        // raw data or single list with create flag
        if (previewParser.isRawData() || (!previewParser.isMultiList() && mode == IMPORT_LIST_MODE.CREATE)) {
            currentList = overrideList;
            db.upsertTable(currentList);
        }
    }

    @Override
    public void newTable(String name, String columnA, String columnB) {
        flushBuffer();
        ignoreEntries = false;
        VList tbl = new VList(columnA, columnB, name);
        if (previewParser.isRawData()) {
            Log.w(TAG, "New VList command on raw data list!");
        } else if (previewParser.isMultiList() || mode != IMPORT_LIST_MODE.CREATE) {
            if (db.getTableID(tbl) >= MIN_ID_TRESHOLD) {
                if (mode == IMPORT_LIST_MODE.REPLACE) {
                    db.emptyList(tbl);
                } else if (mode == IMPORT_LIST_MODE.IGNORE) {
                    ignoreEntries = true;
                }
            } else {
                db.upsertTable(tbl);
            }

            currentList = tbl;
        }
    }

    @Override
    public void newEntry(String A, String B, String Tipp) {
        if (!ignoreEntries) {
            insertBuffer.add(new VEntry(A, B, Tipp, currentList, -1L));
            if (insertBuffer.size() >= BUFFER_CAPACITY) {
                flushBuffer();
            }
        }
    }

    /**
     * Flushes the buffer and inserts everything
     */
    private void flushBuffer() {
        if(insertBuffer.isEmpty()){
            return;
        }
        db.upsertEntries(insertBuffer);
        insertBuffer.clear();
        insertBuffer.ensureCapacity(BUFFER_CAPACITY);
    }

    @Override
    public void finish() {
        flushBuffer();
    }

    /**
     * Import list handling mode
     */
    public enum IMPORT_LIST_MODE {
        /**
         * Replace existing list's vocables
         */
        REPLACE,
        /**
         * Add to existing lists
         */
        ADD,
        /**
         * Ignore existing lists
         */
        IGNORE,
        /**
         * Create new list
         */
        CREATE
    }
}
