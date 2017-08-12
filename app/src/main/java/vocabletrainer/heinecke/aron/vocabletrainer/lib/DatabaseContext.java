package vocabletrainer.heinecke.aron.vocabletrainer.lib;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;

/**
 * Adapted from http://stackoverflow.com/a/9168969
 */

/**
 * Custom database context, ignoring all other specifics and using only the privded file
 */
class DatabaseContext extends ContextWrapper {

    private static final String DEBUG_CONTEXT = "DatabaseContext";
    private File file;

    /**
     * Creates a new DatabaseContext, which will just use the specified file for any DB requests
     *
     * @param base
     * @param file
     */
    public DatabaseContext(Context base, File file) {
        super(base);
        this.file = file;
    }

    @Override
    public File getDatabasePath(String name) {
        return file;
    }

    /* this version is called for android devices >= api-11. thank to @damccull for fixing this. */
    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
        return openOrCreateDatabase(name, mode, factory);
    }

    /* this version is called for android devices < api-11 */
    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null);
        // SQLiteDatabase result = super.openOrCreateDatabase(name, mode, factory);
        if (Log.isLoggable(DEBUG_CONTEXT, Log.WARN)) {
            Log.w(DEBUG_CONTEXT, "openOrCreateDatabase(" + name + ",,) = " + result.getPath());
        }
        return result;
    }
}