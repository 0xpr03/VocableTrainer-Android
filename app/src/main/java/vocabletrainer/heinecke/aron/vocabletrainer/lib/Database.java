package vocabletrainer.heinecke.aron.vocabletrainer.lib;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import java.io.File;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;

/**
 * Database manager<br>
 * Doing all relevant DB stuff
 */
public class Database {
    private final static String TAG = "Database";
    public final static String DB_NAME_DEV = "test1.db";
    private final static String DB_NAME_PRODUCTION = "voc.db";
    public final static int MIN_ID_TRESHOLD = 0;
    public final static int ID_RESERVED_SKIP = -2;
    private final static String TBL_VOCABLE = "`vocables2`";
    private final static String TBL_TABLES = "`voc_tables2`";
    private final static String TBL_SESSION = "`session`";
    private final static String TBL_SESSION_META = "`session_meta`";
    private final static String TBL_SESSION_TABLES = "`session_tables`";
    private final static String TBL_MEANING_A = "`meaning_a`";
    private final static String TBL_MEANING_B = "`meaning_b`";
    private final static String TBL_SESSION_VOC = "`session_voc`";
    private final static String KEY_VOC = "`voc`";
    private final static String KEY_NAME_A = "`name_a`";
    private final static String KEY_NAME_B = "`name_b`";
    private final static String KEY_TIP = "`tip`";
    private final static String KEY_TABLE = "`table`";
    private final static String KEY_LAST_USED = "`last_used`";
    private final static String KEY_NAME_TBL = "`name`";
    private final static String KEY_MEANING = "`meaning`";
    private final static String KEY_CREATED = "`created`";
    private final static String KEY_CORRECT = "`correct`";
    private final static String KEY_WRONG = "`wrong`";
    private final static String KEY_ADDITION = "`addition`";

    private final static String KEY_POINTS = "`points`";
    private final static String KEY_MKEY = "`key`";
    private final static String KEY_MVALUE = "`value`";
    private static SQLiteDatabase dbIntern = null; // DB to internal file, 99% of the time used
    private SQLiteDatabase db = null; // pointer to DB used in this class
    private internalDB helper = null;

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
        try (Cursor cV = db.rawQuery(
                "SELECT "+ KEY_TIP + "," + KEY_ADDITION + "," + KEY_LAST_USED
                    + ",tVoc." + KEY_CREATED +"," + KEY_CORRECT + "," + KEY_WRONG
                    + ",tVoc." + KEY_TABLE + "," + KEY_NAME_A + "," + KEY_NAME_B + "," + KEY_NAME_TBL
                    + ",tList." + KEY_CREATED
                    + "," + KEY_POINTS
                + " FROM " + TBL_VOCABLE + " tVoc"
                + " JOIN " + TBL_TABLES + " tList ON tVoc." + KEY_TABLE+" = tList." + KEY_TABLE
                + " LEFT JOIN " + TBL_SESSION + " ses ON tVoc." + KEY_TABLE + " = ses." + KEY_TABLE
                    + " AND tVoc." + KEY_VOC + " = ses." + KEY_VOC
                + " WHERE tVoc." + KEY_TABLE + " = ? AND tVoc." + KEY_VOC+" = ?",
                new String[]{String.valueOf(listID),String.valueOf(vocID)})){
            if(cV.moveToNext()){
                VList list = new VList(listID,cV.getString(7),cV.getString(8),
                        cV.getString(9),new Date(cV.getLong(10)));

                List<String> meaningA = new LinkedList<>();
                List<String> meaningB = new LinkedList<>();

                VEntry vocable = new VEntry(meaningA,meaningB,cV.getString(0),
                    cV.getString(1),vocID,list,
                    cV.isNull(11) ? 0 : cV.getInt(11),
                    new Date(cV.getLong(2)), new Date(cV.getLong(3)),
                    cV.getInt(4),cV.getInt(5));

                getVocableMeanings(TBL_MEANING_A,vocable,meaningA);
                getVocableMeanings(TBL_MEANING_B,vocable,meaningB);

                return vocable;
            }else{
                Log.w(TAG,"vocable not found by ID!");
                return null;
            }
        }
    }

    /**
     * Fill meaning list of vocable entry
     *
     * @param table Table to use
     * @param vocable Vocable as identifier
     * @param list List in which to insert
     */
    private void getVocableMeanings(@NonNull final String table,@NonNull final VEntry vocable,@NonNull final List<String> list) {
        try (Cursor cM = db.query(table,new String[]{KEY_MEANING},
            KEY_TABLE+" = ? AND "+KEY_VOC+" = ?",
            new String[]{Integer.toString(vocable.getList().getId()), Integer.toString(vocable.getId())},
            null,null,null)) {
            while(cM.moveToNext()) {
                list.add(cM.getString(0));
            }
            if(list.size() == 0){
                Log.w(TAG,"No meanings in "+table+" for "+vocable);
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
            db.delete(TBL_SESSION + "", null, null);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    /**
     * Retruns a List of Entries for the specified list<br>
     *     <u>No point data is being loaded</u>
     *
     * @param list VList for which all entries should be retrieved
     * @return List<VEntry>
     */
    public List<VEntry> getVocablesOfTable(@NonNull final VList list) {
        final String sqlMeaning = "SELECT "+KEY_MEANING+","+KEY_VOC+" voc FROM %s WHERE "
                +KEY_TABLE+" = ? ORDER BY voc";
        try (
                Cursor cV = db.rawQuery("SELECT " + KEY_TIP + "," + KEY_ADDITION + "," + KEY_LAST_USED
                        + "," + KEY_CREATED +"," + KEY_CORRECT + "," + KEY_WRONG
                        +"," + KEY_VOC
                        + " FROM " + TBL_VOCABLE
                        + " WHERE " + KEY_TABLE + " = ?", new String[]{String.valueOf(list.getId())});
                Cursor cMA = db.rawQuery(String.format(sqlMeaning,TBL_MEANING_A),new String[]{Integer.toString(list.getId())});
                Cursor cMB = db.rawQuery(String.format(sqlMeaning,TBL_MEANING_B),new String[]{Integer.toString(list.getId())});
        ) {
            List<VEntry> lst = new ArrayList<>();
            SparseArray<List<String>> mapA = new SparseArray<>();
            SparseArray<List<String>> mapB = new SparseArray<>();
            while (cV.moveToNext()) {

                List<String> meaningA = new LinkedList<>();
                List<String> meaningB = new LinkedList<>();

                VEntry vocable = new VEntry(meaningA,meaningB,cV.getString(0),
                        cV.getString(1),cV.getInt(6),list,
                        new Date(cV.getLong(2)), new Date(cV.getLong(3)),
                        cV.getInt(4),cV.getInt(5));

                mapA.put(vocable.getId(),meaningA);
                mapB.put(vocable.getId(),meaningB);

                lst.add(vocable);
            }

            handleMeaningData(cMA,mapA);
            handleMeaningData(cMB,mapB);

            return lst;
        }
    }

    /**
     * Sort meanings from cursor into correct List:String from map
     *
     * @param cursor expected as [0] = String meaning, [1] = int ID
     * @param map
     */
    private void handleMeaningData(Cursor cursor, SparseArray<List<String>> map){
        List<String> lst = null;
        int lastID = ID_RESERVED_SKIP;
        while(cursor.moveToNext()){
            int id = cursor.getInt(1);
            if(id == ID_RESERVED_SKIP)
                Log.wtf(TAG,"ID is -1!");
            if(id != lastID || lst == null) {
                lst = map.get(id);
                lastID = id;
            }

            lst.add(cursor.getString(0));
        }
    }

    /**
     * Debug function to retrieve points of entry
     *
     * @return
     */
    @Deprecated
    public int getEntryPoints(@NonNull final VEntry ent) {
        try (
                Cursor cursor = db.rawQuery("SELECT " + KEY_POINTS + " "
                        + "FROM " + TBL_SESSION + " WHERE " + KEY_TABLE + " = ? AND " + KEY_VOC + " = ?", new String[]{String.valueOf(ent.getList().getId()), String.valueOf(ent.getId())});
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
        final String[] column = {KEY_TABLE,KEY_NAME_A,KEY_NAME_B,KEY_NAME_TBL,KEY_CREATED};
        try (
                Cursor cursor = db.query(TBL_TABLES,column,null,null,null,null,null);
        ) {
            List<VList> list = new ArrayList<>();
            while (cursor.moveToNext()) {
                list.add(new VList(cursor.getInt(0), cursor.getString(1),
                    cursor.getString(2), cursor.getString(3),
                    new Date(cursor.getLong(4))));
            }
            return list;
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return null;
        }
    }

    /**
     * Update or insert the provided VList data
     *
     * @param tbl
     * @return true on succuess
     */
    public boolean upsertVList(@NonNull final VList tbl) {
        Log.v(TAG,"upsertVList");
        if (tbl.isExisting()) {
            try {
                final String[] args = {String.valueOf(tbl.getId())};
                ContentValues values = new ContentValues();
                values.put(KEY_NAME_TBL,tbl.getName());
                values.put(KEY_NAME_A,tbl.getNameA());
                values.put(KEY_NAME_B,tbl.getNameB());

                int updated = db.update(TBL_TABLES,values,KEY_TABLE + " = ?",
                        args);
                if (updated != 1){
                    throw new SQLException("Update error, updated: "+updated);
                }
                return true;
            } catch (Exception e) {
                Log.e(TAG, "", e);
                return false;
            }
        } else {
            try {
                int tbl_id = getHighestTableID(db) + 1;
                Log.d(TAG, "highest TBL ID: " + tbl_id);

                ContentValues values = new ContentValues();
                values.put(KEY_TABLE,tbl_id);
                values.put(KEY_NAME_A,tbl.getNameA());
                values.put(KEY_NAME_B,tbl.getNameB());
                values.put(KEY_NAME_TBL,tbl.getName());
                values.put(KEY_CREATED,tbl.getCreated().getTime());
                db.insertOrThrow(TBL_TABLES,null,values);

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
        if (tbl.isExisting())
            return false;

        try (
                Cursor cursor = db.rawQuery("SELECT 1 "
                        + "FROM " + TBL_TABLES
                        + " WHERE " + KEY_TABLE + " = ?", new String[]{String.valueOf(tbl.getId())})) {
            return cursor.moveToNext();
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    /**
     * Update and/or insert all Entries<br>
     * This function uses delete and changed flags in entries<br>
     *     <u>Does not update vocable metadata such as last used etc on changed flag.</u>
     *
     * @param lst
     * @return
     */
    public boolean upsertEntries(@NonNull final List<VEntry> lst) {
        try (
                SQLiteStatement delStm = db.compileStatement("DELETE FROM " + TBL_VOCABLE + " WHERE " + KEY_VOC + " = ? AND " + KEY_TABLE + " = ?");
                SQLiteStatement updStm = db.compileStatement("UPDATE " + TBL_VOCABLE + " SET " + KEY_ADDITION + " = ?," + KEY_TIP + " = ?"
                        + "WHERE " + KEY_TABLE + "= ? AND " + KEY_VOC + " = ?");
                SQLiteStatement insStm = db.compileStatement("INSERT INTO " + TBL_VOCABLE + " ("
                        + KEY_TABLE + "," + KEY_VOC + "," + KEY_TIP + "," + KEY_ADDITION
                        + "," + KEY_CREATED + "," + KEY_CORRECT + "," + KEY_WRONG
                        + ") VALUES (?,?,?,?,?,?,?)");
                SQLiteStatement insMeanA = db.compileStatement("INSERT INTO " + TBL_MEANING_A + "("
                        + KEY_TABLE + ","+KEY_VOC+","+KEY_MEANING+") VALUES (?,?,?)");
                SQLiteStatement insMeanB = db.compileStatement("INSERT INTO " + TBL_MEANING_B + "("
                        + KEY_TABLE + ","+KEY_VOC+","+KEY_MEANING+") VALUES (?,?,?)");

        ) {
            final String whereDelMeaning = KEY_TABLE + " = ? AND "+KEY_VOC+" = ?";

            db.beginTransaction();
            int lastTableID = -1;
            int lastID = -1;
            boolean insertMeanings;
            for (VEntry entry : lst) {
                //Log.d(TAG, "processing " + entry + " of " + entry.getList());
                if (entry.getId() == ID_RESERVED_SKIP) // skip spacer
                    continue;

                insertMeanings = false;
                if (entry.isExisting()) {
                    if (entry.isDelete() || entry.isChanged()) {
                        // we need to clear meanings anyway
                        final String[] args = {Integer.toString(entry.getList().getId()), Integer.toString(entry.getId())};
                        db.delete(TBL_MEANING_B, whereDelMeaning, args);
                        db.delete(TBL_MEANING_A, whereDelMeaning, args);

                        if (entry.isDelete()) {
                            delStm.clearBindings();
                            delStm.bindLong(1, entry.getId());
                            delStm.bindLong(2, entry.getList().getId());
                            delStm.execute();
                        } else if (entry.isChanged()) {
                            updStm.clearBindings();
                            updStm.bindString(1, entry.getAddition());
                            updStm.bindString(2, entry.getTip());
                            updStm.bindLong(3, entry.getList().getId());
                            updStm.bindLong(4, entry.getId());
                            updStm.execute();
                            insertMeanings = true;
                        }
                    }
                } else if (!entry.isDelete()) { // vocable created & deleted in editor
                    if (entry.getList().getId() != lastTableID || lastID < MIN_ID_TRESHOLD) {
                        lastTableID = entry.getList().getId();
                        Log.d(TAG, "lastTableID: " + lastTableID + " lastID: " + lastID);
                        lastID = getHighestVocID(db, lastTableID);
                    }
                    lastID++; // make last ID to new ID
                    insStm.clearBindings();
                    insStm.bindLong(1, entry.getList().getId());
                    insStm.bindLong(2, lastID);
                    insStm.bindString(3, entry.getTip());
                    insStm.bindString(4, entry.getAddition());
                    insStm.bindLong(5, entry.getCreated().getTime());
                    insStm.bindLong(6, entry.getCorrect());
                    insStm.bindLong(7,entry.getWrong());
                    insStm.execute();
                    insertMeanings = true;
                    entry.setId(lastID);
                }

                if(insertMeanings){
                    insMeanA.bindLong(1,entry.getList().getId());
                    insMeanA.bindLong(2,entry.getId());
                    for(String meaning : entry.getAMeanings()) {
                        insMeanA.bindString(3,meaning);
                        if(insMeanA.executeInsert() == -1){
                            throw new Exception("unable to insert meaning");
                        }
                    }

                    insMeanB.bindLong(1,entry.getList().getId());
                    insMeanB.bindLong(2,entry.getId());
                    for(String meaning : entry.getBMeanings()) {
                        insMeanB.bindString(3, meaning);
                        if(insMeanB.executeInsert() == -1){
                            throw new Exception("unable to insert meaning");
                        }
                    }
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
    public int getSetTableID(@NonNull final VList tbl) {
        if (tbl.getId() > -1) {
            return tbl.getId();
        }
        String[] args = new String[]{tbl.getName(), tbl.getNameA(), tbl.getNameB()};
        try (
                Cursor cursor = db.rawQuery("SELECT " + KEY_TABLE + " "
                        + "FROM " + TBL_TABLES + " "
                        + "WHERE " + KEY_NAME_TBL + " = ? "
                        + "AND " + KEY_NAME_A + " = ? "
                        + "AND " + KEY_NAME_B + "  = ? "
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
        if (VList.isIDValid(table)) {
            try (Cursor cursor = db.rawQuery("SELECT MAX(" + KEY_VOC + ") "
                    + "FROM " + TBL_VOCABLE + " "
                    + "WHERE " + KEY_TABLE + " = ? ", new String[]{String.valueOf(table)})) {
                if (cursor.moveToNext()) {
                    return cursor.getInt(0);
                } else {
                    return MIN_ID_TRESHOLD - 1;
                }
            } catch (Exception e) {
                throw e;
            }
        } else {
            throw new IllegalArgumentException("invalid table ID!");
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

        try (Cursor cursor = db.rawQuery("SELECT MAX(" + KEY_TABLE + ") "
                + "FROM " + TBL_TABLES, new String[]{})) {
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
    public boolean deleteTable(@NonNull final VList tbl) {
        try {
            db.beginTransaction();

            String[] arg = new String[]{String.valueOf(tbl.getId())};
            emptyList_(arg);
            db.delete(TBL_TABLES, KEY_TABLE + " = ?", arg);
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
     * <u>does not handle any transactions</u>
     *
     * @param arg String array containing the tbl ID at [0]
     */
    private void emptyList_(String[] arg) {
        db.delete(TBL_SESSION, KEY_TABLE + " = ?", arg);
        db.delete(TBL_SESSION_TABLES, KEY_TABLE + " = ?", arg);
        db.delete(TBL_SESSION_VOC, KEY_TABLE + " = ?",arg);
        db.delete(TBL_VOCABLE, KEY_TABLE + " = ?", arg);
        db.delete(TBL_MEANING_B, KEY_TABLE + " = ?",arg);
        db.delete(TBL_MEANING_A, KEY_TABLE + " = ?",arg);
    }

    /**
     * Clear vocable list from all entries
     *
     * @param tbl
     * @return
     */
    public boolean emptyList(@NonNull final VList tbl) {
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
            db.delete(TBL_SESSION, null, null);
            db.delete(TBL_SESSION_META, null, null);
            db.delete(TBL_SESSION_TABLES, null, null);
            db.delete(TBL_SESSION_VOC,null,null);
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
    public boolean updateEntryProgress(@NonNull VEntry entry) {
        try (
                SQLiteStatement updStm = db.compileStatement("INSERT OR REPLACE INTO " + TBL_SESSION + " ( " + KEY_TABLE + "," + KEY_VOC + "," + KEY_POINTS + " )"
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
    public boolean createSession(@NonNull Collection<VList> lists) {
        Log.d(TAG, "entry createSession");

        db.beginTransaction();

        try (SQLiteStatement insStm = db.compileStatement("INSERT INTO " + TBL_SESSION_TABLES + " (" + KEY_TABLE + ") VALUES (?)")) {
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
        try (Cursor cursor = db.rawQuery("SELECT ses." + KEY_TABLE + " tbl," + KEY_NAME_A + "," + KEY_NAME_B + "," + KEY_NAME_TBL
                    + "," + KEY_CREATED
                + " FROM " + TBL_SESSION_TABLES + " ses "
                + "JOIN " + TBL_TABLES + " tbls ON tbls." + KEY_TABLE + " == ses." + KEY_TABLE, null)) {
            while (cursor.moveToNext()) {
                lst.add(new VList(cursor.getInt(0), cursor.getString(1),
                    cursor.getString(2), cursor.getString(3),
                    new Date(cursor.getLong(4))));
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        return lst;
    }

    public boolean isSessionStored() {
        Log.d(TAG, "entry isSessionStored");
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TBL_SESSION_TABLES + " WHERE 1", null)) {
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
     * @param lists           list of VList to process
     * @param unfinishedLists List into which unfinished VList are added into
     * @param settings         TrainerSettings, used for points threshold etc
     * @return true on success
     */
    public boolean getSessionTableData(@NonNull final List<VList> lists,@NonNull final List<VList> unfinishedLists,@NonNull final TrainerSettings settings) {
        if (lists == null || unfinishedLists == null || lists.size() == 0) {
            throw new IllegalArgumentException();
        }
        unfinishedLists.clear();
        for (VList list : lists) {
            try (
                    Cursor curLeng = db.rawQuery("SELECT COUNT(*) FROM " + TBL_VOCABLE + " WHERE " + KEY_TABLE + "  = ?", new String[]{String.valueOf(list.getId())});
                    Cursor curFinished = db.rawQuery("SELECT COUNT(*) FROM " + TBL_SESSION + " WHERE " + KEY_TABLE + "  = ? AND " + KEY_POINTS + " >= ?", new String[]{String.valueOf(list.getId()), String.valueOf(settings.timesToSolve)});
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
     * @param list
     * @param ts
     * @param allowRepetition set to true to allow selecting the same vocable as lastEntry again
     * @return null on error
     */
    public VEntry getRandomTrainerEntry(final VList list, final VEntry lastEntry, final TrainerSettings ts, final boolean allowRepetition) {
        Log.d(TAG, "getRandomTrainerEntry");
        int lastID = MIN_ID_TRESHOLD - 1;
        if (lastEntry != null && lastEntry.getList().getId() == list.getId() && !allowRepetition)
            lastID = lastEntry.getId();

        final String[] arg = new String[]{String.valueOf(list.getId()), String.valueOf(lastID), String.valueOf(ts.timesToSolve)};
        Log.v(TAG, Arrays.toString(arg));
        try (
                Cursor cV = db.rawQuery(
                        "SELECT "+ KEY_TIP + "," + KEY_ADDITION + "," + KEY_LAST_USED
                                + ",tbl." + KEY_CREATED +"," + KEY_CORRECT + "," + KEY_WRONG
                                + ",tbl." + KEY_VOC + "," + KEY_POINTS
                        + " FROM " + TBL_VOCABLE + " tbl LEFT JOIN  " + TBL_SESSION + " ses"
                        + " ON tbl." + KEY_VOC + " = ses." + KEY_VOC + " AND tbl." + KEY_TABLE + " = ses." + KEY_TABLE + " "
                        + " WHERE tbl." + KEY_TABLE + " = ?"
                        + " AND tbl." + KEY_VOC + " != ?"
                        + " AND ( " + KEY_POINTS + " IS NULL OR " + KEY_POINTS + " < ? ) "
                        + " ORDER BY RANDOM() LIMIT 1", arg);
        ) {
            if (cV.moveToNext()) {
                List<String> meaningA = new LinkedList<>();
                List<String> meaningB = new LinkedList<>();

                VEntry vocable = new VEntry(meaningA,meaningB,cV.getString(0),
                        cV.getString(1),cV.getInt(6),list,
                        cV.isNull(7) ? 0 : cV.getInt(7),
                        new Date(cV.getLong(2)), new Date(cV.getLong(3)),
                        cV.getInt(4),cV.getInt(5));

                getVocableMeanings(TBL_MEANING_A,vocable,meaningA);
                getVocableMeanings(TBL_MEANING_B,vocable,meaningB);

                return vocable;
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
                Cursor cursor = db.rawQuery("SELECT " + KEY_MVALUE + " FROM " + TBL_SESSION_META + " WHERE " + KEY_MKEY + " = ?"
                        , new String[]{String.valueOf(key)})
        ) {
            if (cursor.moveToNext()) {
                return cursor.getString(1);
            } else {
                Log.d(TAG, "No value for key " + key);
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "error on session meta retrieval", e);
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
                SQLiteStatement updStm = db.compileStatement("INSERT OR REPLACE INTO " + TBL_SESSION_META + " ( " + KEY_MKEY + "," + KEY_MVALUE + " )"
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
        return db.compileStatement("INSERT OR REPLACE INTO " + TBL_SESSION_META + " (" + KEY_MKEY + "," + KEY_MVALUE + ") VALUES (?,?)");
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
        try (Cursor cursor = db.rawQuery("SELECT " + KEY_MKEY + ", " + KEY_MVALUE + " FROM " + TBL_SESSION_META + " WHERE 1", null)) {
            while (cursor.moveToNext()) {
                map.put(cursor.getString(0), cursor.getString(1));
            }
            return map;
        } catch (Exception e) {
            Log.wtf(TAG, "Session data retrieval failure", e);
            return null;
        }

    }

    class internalDB extends SQLiteOpenHelper {
        private final static int DATABASE_VERSION = 2;

        private final String sql_a = "CREATE TABLE " + TBL_TABLES + " ("
                + KEY_NAME_TBL + " TEXT NOT NULL,"
                + KEY_TABLE + " INTEGER PRIMARY KEY,"
                + KEY_NAME_A + " TEXT NOT NULL,"
                + KEY_NAME_B + " TEXT NOT NULL,"
                + KEY_CREATED + " INTEGER NOT NULL )";
        private final String sql_b = "CREATE TABLE " + TBL_VOCABLE + " ("
                + KEY_TABLE + " INTEGER NOT NULL, "
                + KEY_VOC + " INTEGER NOT NULL,"
                + KEY_TIP + " TEXT,"
                + KEY_ADDITION + "TEXT,"
                + KEY_LAST_USED + " INTEGER,"
                + KEY_CREATED + " INTEGER NOT NULL,"
                + KEY_CORRECT + " INTEGER NOT NULL,"
                + KEY_WRONG + " INTEGER NOT NULL,"
                + "PRIMARY KEY (" + KEY_TABLE + "," + KEY_VOC + ") )";
        private final String sql_c = "CREATE TABLE " + TBL_MEANING_A + " ("
                + KEY_TABLE + " INTEGER NOT NULL,"
                + KEY_VOC + " INTEGER NOT NULL,"
                + KEY_MEANING + " TEXT NOT NULL )";
        private final String sql_d = "CREATE TABLE " + TBL_MEANING_B + " ("
                + KEY_TABLE + " INTEGER NOT NULL,"
                + KEY_VOC + " INTEGER NOT NULL,"
                + KEY_MEANING + " TEXT NOT NULL )";
        private final String sql_e = "CREATE INDEX primA ON " + TBL_MEANING_A
                + "( " + KEY_TABLE + "," + KEY_VOC + " )";
        private final String sql_f = "CREATE INDEX primB ON " + TBL_MEANING_B
                + "( " + KEY_TABLE + "," + KEY_VOC + " )";
        private final String sql_g = "CREATE TABLE " + TBL_SESSION + " ("
                + KEY_TABLE + " INTEGER NOT NULL,"
                + KEY_VOC + " INTEGER NOT NULL,"
                + KEY_POINTS + " INTEGER NOT NULL,"
                + "PRIMARY KEY (" + KEY_TABLE + "," + KEY_VOC + "))";
        private final String sql_h = "CREATE TABLE " + TBL_SESSION_META + " ("
                + KEY_MKEY + " TEXT NOT NULL,"
                + KEY_MVALUE + " TEXT NOT NULL,"
                + "PRIMARY KEY (" + KEY_MKEY + "," + KEY_MVALUE + "))";
        private final String sql_i = "CREATE TABLE " + TBL_SESSION_TABLES + " ("
                + KEY_TABLE + " INTEGER PRIMARY KEY )";
        private final String sql_j = "CREATE TABLE " + TBL_SESSION_VOC + " ("
                + KEY_TABLE + " INTEGER NOT NULL,"
                + KEY_VOC + " INTEGER NOT NULL,"
                + "PRIMARY KEY (" + KEY_TABLE + "," + KEY_VOC + "))";

        @Deprecated
        private final static String TBL_VOCABLE_V1 = "`vocables`";
        @Deprecated
        private final static String TBL_TABLES_V1 = "`voc_tables`";
        @Deprecated
        private final static String KEY_WORD_A = "`word_a`";
        @Deprecated
        private final static String KEY_WORD_B = "`word_b`";

        internalDB(final Context context, File databaseFile) {
            super(new DatabaseContext(context, databaseFile), "", null, DATABASE_VERSION);
        }

        public internalDB(Context context) {
            this(context, false);
        }

        public internalDB(Context context, final boolean dev) {
            super(context, dev ? DB_NAME_DEV : DB_NAME_PRODUCTION, null, DATABASE_VERSION);

        }

        /**
         * Check for illegal ID entries below the threshold
         * @param key key to compare
         * @param table table to look into
         */
        private void checkIllegalIDs(@NonNull SQLiteDatabase db,@NonNull String key, @NonNull String table) {
            try (Cursor cursor = db.rawQuery("SELECT COUNT(*) "
                    + "FROM " + table + " WHERE " + key + " < 0", null)
            ) {
                if (cursor.moveToNext()) {
                    int amount = cursor.getInt(0);
                    if(amount > 0)
                        throw new IllegalStateException("Illegal entries of "+key+" in "+table+": "+amount);
                    else
                        Log.v(TAG,"check passed for "+key+" "+table);
                }
            } catch (Exception e) {
                throw e;
            }
        }

        public void checkForIllegalIds(@NonNull SQLiteDatabase db) {
            Log.d(TAG,"checking for illegal IDs");
            checkIllegalIDs(db,KEY_VOC,TBL_VOCABLE);
            checkIllegalIDs(db,KEY_TABLE,TBL_VOCABLE);
            checkIllegalIDs(db,KEY_TABLE,TBL_TABLES);
            checkIllegalIDs(db,KEY_VOC,TBL_MEANING_A);
            checkIllegalIDs(db,KEY_TABLE,TBL_MEANING_A);
            checkIllegalIDs(db,KEY_VOC,TBL_MEANING_B);
            checkIllegalIDs(db,KEY_TABLE,TBL_MEANING_B);
            Log.d(TAG,"check passed");
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG,"creating db");

            final String[] tables = {sql_a,sql_b,sql_c,sql_d,sql_e,sql_f,sql_g,sql_h,sql_i,sql_j};
            int i = 0;
            db.beginTransaction();
            try {
                for(i = 0; i < tables.length; i++) {
                    db.compileStatement(tables[i])
                            .execute();
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.wtf(TAG, "Database creation error", e);
                Log.w(TAG,"At Table "+tables[i]);
                throw e;
            } finally {
                db.endTransaction();
            }
        }

        @SuppressWarnings( "deprecation" )
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG,"db upgrade triggered old:"+oldVersion+" new:"+newVersion);
            if (oldVersion < 2) {
                final String[] newTables = {sql_a,sql_b,sql_c,sql_d,sql_e,sql_f,sql_j};
                for(String sql : newTables)
                    db.execSQL(sql);

                final String time = Long.toString(System.currentTimeMillis());
                {
                    db.execSQL("ALTER TABLE "+TBL_VOCABLE_V1+" ADD COLUMN "+KEY_ADDITION+" TEXT");
                    db.execSQL("ALTER TABLE "+TBL_VOCABLE_V1+" ADD COLUMN "+KEY_CORRECT+" INTEGER NOT NULL DEFAULT 0");
                    db.execSQL("ALTER TABLE "+TBL_VOCABLE_V1+" ADD COLUMN "+KEY_WRONG+" INTEGER NOT NULL DEFAULT 0");
                    db.execSQL("ALTER TABLE "+TBL_VOCABLE_V1+" ADD COLUMN "+KEY_CREATED+" INTEGER NOT NULL DEFAULT "+time);

                    final String args = String.format("%s,%s,%s,%s,%s,%s,%s,%s",KEY_TABLE, KEY_VOC,
                            KEY_TIP, KEY_ADDITION, KEY_LAST_USED, KEY_CREATED, KEY_CORRECT,
                            KEY_WRONG);
                    final String sqlCpy = String.format("INSERT INTO %s (%s) SELECT"
                                    + " %s FROM %s",
                            TBL_VOCABLE,args,args, TBL_VOCABLE_V1);
                    db.execSQL(sqlCpy);

                    final String colMA = String.format("%s,%s,%s",KEY_TABLE,KEY_VOC,KEY_MEANING);
                    final String selMA = String.format("%s,%s,%s",KEY_TABLE,KEY_VOC,KEY_WORD_A);
                    final String sqlMeaningA = String.format("INSERT INTO %s (%s) SELECT"
                                    + " %s FROM %s",TBL_MEANING_A,colMA,selMA,TBL_VOCABLE_V1);
                    db.execSQL(sqlMeaningA);

                    final String sqlMeaningB = sqlMeaningA.replaceAll(KEY_WORD_A,KEY_WORD_B)
                            .replaceAll(TBL_MEANING_A,TBL_MEANING_B);
                    db.execSQL(sqlMeaningB);

                    db.execSQL("DROP TABLE " + TBL_VOCABLE_V1);
                }

                {
                    db.execSQL("ALTER TABLE " + TBL_TABLES_V1 + " ADD COLUMN " + KEY_CREATED + " INTEGER NOT NULL DEFAULT " + time);
                    final String args = String.format("%s,%s,%s,%s,%s",KEY_NAME_TBL,KEY_TABLE,
                            KEY_NAME_A,KEY_NAME_B,KEY_CREATED);
                    final String sqlCpy = String.format("INSERT INTO %s (%s) SELECT"
                            + " %s FROM %s",TBL_TABLES,args,args,TBL_TABLES_V1);
                    db.execSQL(sqlCpy);
                    db.execSQL("DROP TABLE "+TBL_TABLES_V1);
                }

                {
                    checkForIllegalIds(db);
                }
            }
            Log.v(TAG,"upgrade end");
        }


    }
}
