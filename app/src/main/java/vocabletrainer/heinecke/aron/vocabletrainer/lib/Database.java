package vocabletrainer.heinecke.aron.vocabletrainer.lib;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings;

/**
 * Database manager<br>
 * Doing all relevant DB stuff
 */
public class Database {
    public final static String DB_NAME_DEV = "test1.db";
    public final static String DB_NAME_PRODUCTION = "voc.db";
    public static final int MIN_ID_TRESHOLD = 0;
    public static final int ID_RESERVED_SKIP = -2;
    private final static String TAG = "Database";
    private final static String TBL_VOCABLE = "vocables";
    private final static String TBL_TABLES = "voc_tables";
    private final static String TBL_SESSION = "session";
    private final static String TBL_SESSION_META = "session_meta";
    private final static String TBL_SESSION_TABLES = "session_tables";
    private final static String KEY_VOC = "voc";
    private final static String KEY_WORD_A = "word_a";
    private final static String KEY_WORD_B = "word_b";
    private final static String KEY_TIP = "tip";
    private final static String KEY_TABLE = "table";
    private final static String KEY_LAST_USED = "last_used";
    private final static String KEY_NAME_TBL = "name";
    private final static String KEY_NAME_A = "name_a";
    private final static String KEY_NAME_B = "name_b";
    private final static String KEY_POINTS = "points";
    private final static String KEY_MKEY = "key";
    private final static String KEY_MVALUE = "value";
    private static SQLiteDatabase dbIntern = null; // DB to internal file, 99% of the time used
    private SQLiteDatabase db = null; // pointer to DB used in this class
    private SQLiteOpenHelper helper = null;

    /**
     * Database for export / import
     *
     * @param context
     * @param file    // file to use for this DB
     */
    public Database(Context context, final File file) {
        helper = new internalDB(context, file);
        this.db = helper.getWritableDatabase();
    }

    /**
     * Database object, using internal storage for this App (default DB file)
     *
     * @param context
     * @param dev     set to true for unit tests<br>
     *                no data will be saved
     */
    public Database(Context context, boolean dev) {
        if (dbIntern == null) {
            helper = new internalDB(context, dev);
            dbIntern = helper.getWritableDatabase();
        }
        this.db = dbIntern;
    }

    /**
     * Database object
     *
     * @param context
     */
    public Database(Context context) {
        this(context, false);
    }

    /**
     * Retrieve vocable by ID & list ID
     * @param vocID
     * @param listID
     * @return VEntry with set List<br>
     *     Null on failure
     */
    public VEntry getVocable(final int vocID, final int listID){
        try (Cursor cursor = db.rawQuery("SELECT `" + KEY_WORD_A + "`,`" + KEY_WORD_B + "`,`" + KEY_TIP + "`,`" + KEY_VOC + "`,tVoc.`" + KEY_TABLE + "`,`" + KEY_LAST_USED + "`,`"
                + KEY_NAME_A+"`,`"+KEY_NAME_B+"`,`"+KEY_NAME_TBL+"` "
                + "FROM `" + TBL_VOCABLE + "` tVoc "
                + "JOIN `"+TBL_TABLES+"` tList ON tVoc.`"+KEY_TABLE+"` = tList.`"+KEY_TABLE+"` "
                + "WHERE tVoc.`" + KEY_TABLE + "` = ? AND `"+KEY_VOC+"` = ?", new String[]{String.valueOf(listID),String.valueOf(vocID)})){
            if(cursor.moveToNext()){
                VList tbl = new VList(listID,cursor.getString(6),cursor.getString(7),cursor.getString(8));
                return new VEntry(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), tbl, cursor.getLong(5));
            }else{
                Log.w(TAG,"vocable not found by ID!");
                return null;
            }
        }
    }

    /**
     * Wipe all session points
     *
     * @return
     */
    public boolean wipeSessionPoints() {
        try {
            db.delete("`" + TBL_SESSION + "`", null, null);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    /**
     * Retruns a List of Entries for the specified list
     *
     * @param list VList for which all entries should be retrieved
     * @return List<VEntry>
     */
    public List<VEntry> getVocablesOfTable(VList list) {
        try (
                Cursor cursor = db.rawQuery("SELECT `" + KEY_WORD_A + "`,`" + KEY_WORD_B + "`,`" + KEY_TIP + "`,`" + KEY_VOC + "`,`" + KEY_TABLE + "`,`" + KEY_LAST_USED + "` "
                        + "FROM `" + TBL_VOCABLE + "` "
                        + "WHERE `" + KEY_TABLE + "` = ?", new String[]{String.valueOf(list.getId())})) {
            List<VEntry> lst = new ArrayList<>();
            while (cursor.moveToNext()) {
                lst.add(new VEntry(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), list, cursor.getLong(5)));
            }
            return lst;
        }
    }

    /**
     * Debug function to retreive points of entry
     *
     * @return
     */
    @Deprecated
    public int getEntryPoints(final VEntry ent) {
        try (
                Cursor cursor = db.rawQuery("SELECT `" + KEY_POINTS + "` "
                        + "FROM `" + TBL_SESSION + "` WHERE `" + KEY_TABLE + "` = ? AND `" + KEY_VOC + "` = ?", new String[]{String.valueOf(ent.getList().getId()), String.valueOf(ent.getId())});
        ) {
            if (cursor.moveToNext())
                return cursor.getInt(0);
            else
                return -1;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Get a list of all lists
     *
     * @return ArrayList<\VList>
     */
    public List<VList> getTables() {
        try (
                Cursor cursor = db.rawQuery("SELECT `" + KEY_TABLE + "`,`" + KEY_NAME_A + "`,`" + KEY_NAME_B + "`,`" + KEY_NAME_TBL + "` "
                        + "FROM `" + TBL_TABLES + "` WHERE 1", null)
        ) {
            List<VList> list = new ArrayList<>();
            while (cursor.moveToNext()) {
                list.add(new VList(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3)));
            }
            return list;
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return null;
        }
    }

    /**
     * Update or insert the provided VList datac
     *
     * @param tbl
     * @return true on succuess
     */
    public boolean upsertTable(VList tbl) {
        if (tbl.getId() >= MIN_ID_TRESHOLD) {
            try (
                    SQLiteStatement upd = db.compileStatement("UPDATE `" + TBL_TABLES + "` SET `" + KEY_NAME_A + "` = ?, `" + KEY_NAME_B + "` = ?, `" + KEY_NAME_TBL + "` = ? "
                            + "WHERE `" + KEY_TABLE + "` = ? ")) {
                upd.clearBindings();
                upd.bindString(1, tbl.getNameA());
                upd.bindString(2, tbl.getNameB());
                upd.bindString(3, tbl.getName());
                upd.bindLong(4, tbl.getId());
                upd.execute();
                return true;
            } catch (Exception e) {
                Log.e(TAG, "", e);
                return false;
            }
        } else {
            try (
                    SQLiteStatement ins = db.compileStatement("INSERT INTO `" + TBL_TABLES + "` (`" + KEY_NAME_TBL + "`,`" + KEY_NAME_A + "`,`" + KEY_NAME_B + "`,`"
                            + KEY_TABLE + "`) VALUES (?,?,?,?)")) {
                int tbl_id = getHighestTableID(db) + 1;
                Log.d(TAG, "highest TBL ID: " + tbl_id);
                ins.bindString(1, tbl.getName());
                ins.bindString(2, tbl.getNameA());
                ins.bindString(3, tbl.getNameB());
                ins.bindLong(4, tbl_id);
                Log.d(TAG, ins.toString());
                ins.execute();
                tbl.setId(tbl_id);
                return true;
            } catch (Exception e) {
                Log.wtf(TAG, "", e);
                return false;
            }
        }
    }

    /**
     * Test is table exists
     *
     * @param db  Writeable database
     * @param tbl VList
     * @return true if it exists
     */
    private boolean testTableExists(SQLiteDatabase db, VList tbl) {
        if (db == null)
            throw new IllegalArgumentException("illegal sql db");
        if (tbl.getId() < MIN_ID_TRESHOLD)
            return true;

        try (
                Cursor cursor = db.rawQuery("SELECT 1 "
                        + "FROM `" + TBL_TABLES + "`"
                        + "WHERE `" + KEY_TABLE + "` = ?", new String[]{String.valueOf(tbl.getId())})) {
            return cursor.moveToNext();
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    /**
     * Update and/or insert all Entries<br>
     * This function uses delete and changed flags in entries
     *
     * @param lst
     * @return
     */
    public boolean upsertEntries(final List<VEntry> lst) {
        try (
                SQLiteStatement delStm = db.compileStatement("DELETE FROM `" + TBL_VOCABLE + "` WHERE `" + KEY_VOC + "` = ? AND `" + KEY_TABLE + "` = ?");
                SQLiteStatement updStm = db.compileStatement("UPDATE `" + TBL_VOCABLE + "` SET `" + KEY_WORD_A + "` = ?, `" + KEY_WORD_B + "` = ?, `" + KEY_TIP + "` = ?, `"
                        + KEY_LAST_USED + "` = ? "
                        + "WHERE `" + KEY_TABLE + "`= ? AND `" + KEY_VOC + "` = ?");
                SQLiteStatement insStm = db.compileStatement("INSERT INTO `" + TBL_VOCABLE + "` (`" + KEY_WORD_A + "`,`" + KEY_WORD_B + "`,`" + KEY_TIP + "`,`"
                        + KEY_LAST_USED + "`,`" + KEY_TABLE + "`,`" + KEY_VOC + "`) VALUES (?,?,?,?,?,?)")

        ) {

            db.beginTransaction();
            int lastTableID = -1;
            int lastID = -1;

            for (VEntry entry : lst) {
                //Log.d(TAG, "processing " + entry + " of " + entry.getList());
                if (entry.getId() == ID_RESERVED_SKIP) // skip spacer
                    continue;
                if (entry.getId() >= MIN_ID_TRESHOLD) {
                    if (entry.isDelete()) {
                        delStm.clearBindings();
                        delStm.bindLong(1, entry.getId());
                        delStm.bindLong(2, entry.getList().getId());
                        delStm.execute();
                    } else if (entry.isChanged()) {
                        updStm.clearBindings();
                        updStm.bindString(1, entry.getAWord());
                        updStm.bindString(2, entry.getBWord());
                        updStm.bindString(3, entry.getTip());
                        updStm.bindLong(4, entry.getDate());
                        updStm.bindLong(5, entry.getList().getId());
                        updStm.bindLong(6, entry.getId());
                        updStm.execute();
                    }
                } else if (!entry.isDelete()) {
                    if (entry.getList().getId() != lastTableID || lastID < MIN_ID_TRESHOLD) {
                        lastTableID = entry.getList().getId();
                        Log.d(TAG, "lastTableID: " + lastTableID + " lastID: " + lastID);
                        lastID = getHighestVocID(db, lastTableID);
                    }
                    lastID++;
                    insStm.clearBindings();
                    insStm.bindString(1, entry.getAWord());
                    insStm.bindString(2, entry.getBWord());
                    insStm.bindString(3, entry.getTip());
                    insStm.bindLong(4, entry.getDate());
                    insStm.bindLong(5, entry.getList().getId());
                    insStm.bindLong(6, lastID);
                    insStm.execute();
                    entry.setId(lastID);
                }
            }
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return false;
        } finally {
            if (db.inTransaction()) {
                Log.d(TAG, "in transaction");
                db.endTransaction();
            }
        }
    }

    /**
     * Returns the ID of a table with the exact same naming <br>
     * this also updates the VList element itself to contains the right ID
     *
     * @param tbl VList to be used a search source
     * @return ID or  -1 if not found, -2 if an error occurred
     */
    public int getTableID(final VList tbl) {
        if (tbl.getId() > -1) {
            return tbl.getId();
        }
        String[] args = new String[]{tbl.getName(), tbl.getNameA(), tbl.getNameB()};
        try (
                Cursor cursor = db.rawQuery("SELECT `" + KEY_TABLE + "` "
                        + "FROM `" + TBL_TABLES + "` "
                        + "WHERE `" + KEY_NAME_TBL + "` = ? "
                        + "AND `" + KEY_NAME_A + "` = ? "
                        + "AND `" + KEY_NAME_B + "`  = ? "
                        + "LIMIT 1", args)
        ) {
            int id = -1;
            if (cursor.moveToNext()) {
                id = cursor.getInt(0);
            }
            tbl.setId(id);
            return id;
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return -1;
        }
    }

    public SQLiteStatement prepareInsertStatement() {
        return db.compileStatement("");
    }

    /**
     * Returns the highest vocable ID for the specified table
     *
     * @param db
     * @param table table ID<br>
     *              This is on purpose no VList object
     * @return highest ID <b>or -1 if none is found</b>
     */
    private int getHighestVocID(final SQLiteDatabase db, final int table) throws Exception {
        if (table < MIN_ID_TRESHOLD)
            throw new IllegalArgumentException("table ID is negative!");

        try (Cursor cursor = db.rawQuery("SELECT MAX(`" + KEY_VOC + "`) "
                + "FROM `" + TBL_VOCABLE + "` "
                + "WHERE `" + KEY_TABLE + "` = ? ", new String[]{String.valueOf(table)})) {
            if (cursor.moveToNext()) {
                return cursor.getInt(0);
            } else {
                return MIN_ID_TRESHOLD - 1;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Returns the highest table ID
     *
     * @param db
     * @return highest ID,  <b>-1 is none if found</b>
     */
    private int getHighestTableID(final SQLiteDatabase db) throws Exception {
        if (db == null)
            throw new IllegalArgumentException("invalid DB");

        try (Cursor cursor = db.rawQuery("SELECT MAX(`" + KEY_TABLE + "`) "
                + "FROM `" + TBL_TABLES + "` ", new String[]{})) {
            if (cursor.moveToNext()) {
                Log.d(TAG, Arrays.toString(cursor.getColumnNames()));
                return cursor.getInt(0);
            } else {
                return MIN_ID_TRESHOLD - 1;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Deletes the given table and all its vocables
     *
     * @param tbl VList to delete
     * @return true on success
     */
    public boolean deleteTable(final VList tbl) {
        try {
            db.beginTransaction();

            String[] arg = new String[]{String.valueOf(tbl.getId())};
            emptyList_(arg);
            db.delete("`" + TBL_TABLES + "`", "`" + KEY_TABLE + "` = ?", arg);
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return false;
        } finally {
            if (db.inTransaction()) {
                db.endTransaction();
            }
        }
    }

    /**
     * directly calls table empty SQL<br>
     * does not handle any transactions
     *
     * @param arg String array containing the tbl ID at [0]
     */
    private void emptyList_(String[] arg) {
        db.delete("`" + TBL_SESSION + "`", "`" + KEY_TABLE + "` = ?", arg);
        db.delete("`" + TBL_SESSION_TABLES + "`", "`" + KEY_TABLE + "` = ?", arg);
        db.delete("`" + TBL_VOCABLE + "`", "`" + KEY_TABLE + "` = ?", arg);
    }

    /**
     * Clear vocable list from all entries
     *
     * @param tbl
     * @return
     */
    public boolean emptyList(final VList tbl) {
        try {
            db.beginTransaction();

            emptyList_(new String[]{String.valueOf(tbl.getId())});

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return false;
        } finally {
            if (db.inTransaction())
                db.endTransaction();
        }
    }

    /**
     * Deletes the current session
     *
     * @return
     */
    public boolean deleteSession() {
        Log.d(TAG, "entry deleteSession");
        db.beginTransaction();
        try {
            db.delete(TBL_SESSION, "1", null);
            db.delete(TBL_SESSION_META, "1", null);
            db.delete(TBL_SESSION_TABLES, "1", null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return false;
        } finally {
            if (db.inTransaction())
                db.endTransaction();
        }
        Log.d(TAG, "exit deleteSession");
        return true;
    }

    /**
     * Updates a transaction VEntry
     *
     * @param entry VEntry to update
     * @return true on success
     */
    public boolean updateEntryProgress(VEntry entry) {
        try (
                SQLiteStatement updStm = db.compileStatement("INSERT OR REPLACE INTO `" + TBL_SESSION + "` ( `" + KEY_TABLE + "`,`" + KEY_VOC + "`,`" + KEY_POINTS + "` )"
                        + "VALUES (?,?,?)")
        ) {
            Log.d(TAG, entry.toString());
            //TODO: update date
            updStm.bindLong(1, entry.getList().getId());
            updStm.bindLong(2, entry.getId());
            updStm.bindLong(3, entry.getPoints());
            if (updStm.executeInsert() > 0) { // possible problem ( insert / update..)
                Log.d(TAG, "updated voc points");
                return true;
            } else {
                Log.e(TAG, "Inserted < 1 columns!");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    /**
     * Starts a new session based on the table entries<br>
     * Overriding any old session data!
     *
     * @param lists The VList to use for this sessions
     * @return true on success
     */
    public boolean createSession(Collection<VList> lists) {
        Log.d(TAG, "entry createSession");

        db.beginTransaction();

        try (SQLiteStatement insStm = db.compileStatement("INSERT INTO `" + TBL_SESSION_TABLES + "` (`" + KEY_TABLE + "`) VALUES (?)")) {
            //TODO: update last_used
            for (VList tbl : lists) {
                insStm.clearBindings();
                insStm.bindLong(1, tbl.getId());
                if (insStm.executeInsert() < 0) {
                    Log.wtf(TAG, "no new table inserted");
                }
            }
            db.setTransactionSuccessful();
            Log.d(TAG, "exit createSession");
            return true;
        } catch (Exception e) {
            Log.wtf(TAG, "", e);
            return false;
        } finally {
            if (db.inTransaction())
                db.endTransaction();
        }
    }

    /**
     * Returns the table selection from the stored session
     *
     * @return never null
     */
    public ArrayList<VList> getSessionTables() {
        ArrayList<VList> lst = new ArrayList<>(10);
        try (Cursor cursor = db.rawQuery("SELECT ses.`" + KEY_TABLE + "` tbl,`" + KEY_NAME_A + "`,`" + KEY_NAME_B + "`,`" + KEY_NAME_TBL + "` FROM `" + TBL_SESSION_TABLES + "` ses "
                + "JOIN `" + TBL_TABLES + "` tbls ON tbls.`" + KEY_TABLE + "` == ses.`" + KEY_TABLE + "`", null)) {
            while (cursor.moveToNext()) {
                lst.add(new VList(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3)));
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        return lst;
    }

    public boolean isSessionStored() {
        Log.d(TAG, "entry isSessionStored");
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM `" + TBL_SESSION_TABLES + "` WHERE 1", null)) {
            cursor.moveToNext();
            if (cursor.getInt(0) > 0) {
                Log.d(TAG, "found session");
                return true;
            }
        } catch (Exception e) {
            Log.wtf(TAG, "unable to get session lists row count", e);
        }
        return false;
    }

    /**
     * Set total and unfinished vocables for each table, generate list of finished
     *
     * @param lists           list of lists to process
     * @param unfinishedLists List to which to unfinished lists are added onto
     * @param settings         TrainerSettings, used for points treshold etc
     * @return true on success
     */
    public boolean getSessionTableData(final List<VList> lists, final List<VList> unfinishedLists, TrainerSettings settings) {
        if (lists == null || unfinishedLists == null || lists.size() == 0) {
            throw new IllegalArgumentException();
        }
        unfinishedLists.clear();
        for (VList list : lists) {
            try (
                    Cursor curLeng = db.rawQuery("SELECT COUNT(*) FROM `" + TBL_VOCABLE + "` WHERE `" + KEY_TABLE + "`  = ?", new String[]{String.valueOf(list.getId())});
                    Cursor curFinished = db.rawQuery("SELECT COUNT(*) FROM `" + TBL_SESSION + "` WHERE `" + KEY_TABLE + "`  = ? AND `" + KEY_POINTS + "` >= ?", new String[]{String.valueOf(list.getId()), String.valueOf(settings.timesToSolve)});
            ) {
                if (!curLeng.moveToNext())
                    return false;
                list.setTotalVocs(curLeng.getInt(0));
                if (!curFinished.moveToNext())
                    return false;
                int unfinished = list.getTotalVocs() - curFinished.getInt(0);
                list.setUnfinishedVocs(unfinished);
                if (unfinished > 0) {
                    unfinishedLists.add(list);
                }
                Log.d(TAG, list.toString());
                curLeng.close();
                curFinished.close();
            } catch (Exception e) {
                Log.e(TAG, "", e);
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a random entry from the specified table, which matche the trainer settings criteria<br>
     * The VEntry is guaranteed to be not the "lastEntry" provided here
     *
     * @param tbl
     * @param ts
     * @param allowRepetition set to true to allow selecting the same vocable as lastEntry again
     * @return null on error
     */
    public VEntry getRandomTrainerEntry(final VList tbl, final VEntry lastEntry, final TrainerSettings ts, final boolean allowRepetition) {
        Log.d(TAG, "getRandomTrainerEntry");
        int lastID = MIN_ID_TRESHOLD - 1;
        if (lastEntry != null && lastEntry.getList().getId() == tbl.getId() && !allowRepetition)
            lastID = lastEntry.getId();

        String[] arg = new String[]{String.valueOf(tbl.getId()), String.valueOf(lastID), String.valueOf(ts.timesToSolve)};
        Log.d(TAG, Arrays.toString(arg));
        try (
                Cursor cursor = db.rawQuery("SELECT tbl.`" + KEY_VOC + "`, tbl.`" + KEY_TABLE + "`,`" + KEY_WORD_A + "`, `" + KEY_WORD_B + "`, `" + KEY_TIP + "`, `" + KEY_POINTS + "`, `" + KEY_LAST_USED + "` "
                        + "FROM `" + TBL_VOCABLE + "` tbl LEFT JOIN  `" + TBL_SESSION + "` ses"
                        + " ON tbl.`" + KEY_VOC + "` = ses.`" + KEY_VOC + "` AND tbl.`" + KEY_TABLE + "` = ses.`" + KEY_TABLE + "` "
                        + " WHERE tbl.`" + KEY_TABLE + "` = ?"
                        + " AND tbl.`" + KEY_VOC + "` != ?"
                        + " AND ( `" + KEY_POINTS + "` IS NULL OR `" + KEY_POINTS + "` < ? ) "
                        + " ORDER BY RANDOM() LIMIT 1", arg);
        ) {
            if (cursor.moveToNext()) {
                if (cursor.isNull(5)) { // KEY_POINTS optional
                    Log.d(TAG, "no points for entry");
                    VEntry newEntry = new VEntry(cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getInt(0), tbl, 0, cursor.getLong(6));
                    return newEntry;
                }
                return new VEntry(cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getInt(0), tbl, cursor.getInt(5), cursor.getLong(6));
            } else {
                Log.w(TAG, "no entries found!");
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "", e);
            return null;
        }
    }

    /**
     * Get session meta value for specified key
     *
     * @param key
     * @return null if no entry is found
     */
    public String getSessionMetaValue(final String key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }

        try (
                Cursor cursor = db.rawQuery("SELECT `" + KEY_MVALUE + "` FROM `" + TBL_SESSION_META + "` WHERE `" + KEY_MKEY + "` = ?"
                        , new String[]{String.valueOf(key)})
        ) {
            if (cursor.moveToNext()) {
                return cursor.getString(1);
            } else {
                Log.d(TAG, "No value for key " + key);
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "", e);
            return null;
        }
    }

    /**
     * Set session key-value pair
     *
     * @param key
     * @param value
     * @return true on success
     */
    public boolean setSessionMetaValue(final String key, final String value) {
        try (
                SQLiteStatement updStm = db.compileStatement("INSERT OR REPLACE INTO `" + TBL_SESSION_META + "` ( `" + KEY_MKEY + "`,`" + KEY_MVALUE + "` )"
                        + "(?,?)")
        ) {
            updStm.bindString(1, key);
            updStm.bindString(2, value);
            if (updStm.executeInsert() > 0) { // possible problem ( insert / update..)
                return true;
            } else {
                Log.e(TAG, "Inserted < 1 columns!");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    /**
     * Returns a statement to insert / replace session meta storage values
     *
     * @return
     */
    public SQLiteStatement getSessionInsertStm() {
        db.beginTransaction();
        return db.compileStatement("INSERT OR REPLACE INTO " + TBL_SESSION_META + " (`" + KEY_MKEY + "`,`" + KEY_MVALUE + "`) VALUES (?,?)");
    }

    /**
     * Ends a transaction created by the getSessionInsert Statement
     */
    public void endSessionTransaction(boolean success) {
        if (!db.inTransaction()) {
            throw new IllegalStateException("No transaction ongoing!");
        }
        Log.d(TAG, "transaction success: " + success);
        if (success)
            db.setTransactionSuccessful();
        db.endTransaction();
    }

    /**
     * Returns a cursor on the session data
     *
     * @return map of all key-value pairs or <b>null</b> on errors
     */
    public HashMap<String, String> getSessionData() {
        Log.d(TAG, "entry getSessionData");
        HashMap<String, String> map = new HashMap<>(10);
        try (Cursor cursor = db.rawQuery("SELECT `" + KEY_MKEY + "`, `" + KEY_MVALUE + "` FROM `" + TBL_SESSION_META + "` WHERE 1", null)) {
            while (cursor.moveToNext()) {
                map.put(cursor.getString(0), cursor.getString(1));
            }
            return map;
        } catch (Exception e) {
            Log.wtf(TAG, "", e);
            return null;
        }

    }

    class internalDB extends SQLiteOpenHelper {
        private final static int DATABASE_VERSION = 1;

        internalDB(final Context context, File databaseFile) {
            super(new DatabaseContext(context, databaseFile), "", null, DATABASE_VERSION);
        }

        public internalDB(Context context) {
            this(context, false);
        }

        public internalDB(Context context, final boolean dev) {
            super(context, dev ? DB_NAME_DEV : DB_NAME_PRODUCTION, null, DATABASE_VERSION);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                final String sql_a = "CREATE TABLE `" + TBL_VOCABLE + "` (`" + KEY_TABLE + "` INTEGER NOT NULL, "
                        + "`" + KEY_VOC + "` INTEGER NOT NULL,"
                        + "`" + KEY_WORD_A + "` TEXT NOT NULL, `" + KEY_WORD_B + "` TEXT NOT NULL, `" + KEY_TIP + "` TEXT, "
                        + "`" + KEY_LAST_USED + "` INTEGER, PRIMARY KEY (`" + KEY_TABLE + "`,`" + KEY_VOC + "`) )";
                final String sql_b = "CREATE TABLE `" + TBL_TABLES + "` ("
                        + "`" + KEY_NAME_TBL + "` TEXT NOT NULL, `" + KEY_TABLE + "` INTEGER PRIMARY KEY,"
                        + "`" + KEY_NAME_A + "` TEXT NOT NULL, `" + KEY_NAME_B + "` TEXT NOT NULL )";
                final String sql_c = "CREATE TABLE `" + TBL_SESSION + "` ("
                        + "`" + KEY_TABLE + "` INTEGER NOT NULL,"
                        + "`" + KEY_VOC + "` INTEGER NOT NULL,"
                        + "`" + KEY_POINTS + "` INTEGER NOT NULL,"
                        + "PRIMARY KEY (`" + KEY_TABLE + "`,`" + KEY_VOC + "`))";
                final String sql_d = "CREATE TABLE `" + TBL_SESSION_META + "` (`" + KEY_MKEY + "` TEXT NOT NULL,"
                        + "`" + KEY_MVALUE + "` TEXT NOT NULL,"
                        + "PRIMARY KEY (`" + KEY_MKEY + "`,`" + KEY_MVALUE + "`))";
                final String sql_e = "CREATE TABLE `" + TBL_SESSION_TABLES + "` (`" + KEY_TABLE + "` INTEGER PRIMARY KEY)";
                Log.d(TAG, sql_a);
                Log.d(TAG, sql_b);
                Log.d(TAG, sql_c);
                Log.d(TAG, sql_d);
                Log.d(TAG, sql_e);
                db.execSQL(sql_a);
                db.execSQL(sql_b);
                db.execSQL(sql_c);
                db.execSQL(sql_d);
                db.execSQL(sql_e);
            } catch (Exception e) {
                Log.e(TAG, "", e);
                throw e;
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }


    }
}
