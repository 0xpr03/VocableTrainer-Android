package vocabletrainer.heinecke.aron.vocabletrainer.lib.Import;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Entry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Table;

import static android.content.ContentValues.TAG;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.MIN_ID_TRESHOLD;

/**
 * Importer, does actual importing
 */
public class Importer implements ImportHandler {

    private static final int BUFFER_CAPACITY = 100;

    private PreviewParser previewParser;
    private IMPORT_LIST_MODE mode;
    private Table overrideTable;
    private Table currentTable;
    private Database db;
    private ArrayList<Entry> insertBuffer = new ArrayList<>(BUFFER_CAPACITY);
    private boolean ignoreEntries;

    public Importer(Context context, PreviewParser previewParser, IMPORT_LIST_MODE mode, Table overrideTable) {
        if (previewParser.isRawData() && overrideTable == null) {
            Log.e(TAG, "RawData without passed table!");
            throw new IllegalArgumentException("Missing table!");
        }
        this.previewParser = previewParser;
        this.mode = mode;
        this.overrideTable = overrideTable;
        db = new Database(context);
        ignoreEntries = false;
    }

    @Override
    public void start() {
        // raw data or single list with create flag
        if (previewParser.isRawData() || (!previewParser.isMultiList() && mode == IMPORT_LIST_MODE.CREATE)) {
            currentTable = overrideTable;
            db.upsertTable(currentTable);
        }
    }

    @Override
    public void newTable(String name, String columnA, String columnB) {
        flushBuffer();
        ignoreEntries = false;
        Table tbl = new Table(columnA, columnB, name);
        if (previewParser.isRawData()) {
            Log.w(TAG, "New Table command on raw data list!");
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

            currentTable = tbl;
        }
    }

    @Override
    public void newEntry(String A, String B, String Tipp) {
        if (!ignoreEntries) {
            insertBuffer.add(new Entry(A, B, Tipp, currentTable, -1L));
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
