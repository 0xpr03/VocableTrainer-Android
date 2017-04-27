package vocabletrainer.heinecke.aron.vocabletrainer;

/**
 * Created by aron on 04.04.17.
 */

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Database manager<br>
 * Doing all releveant DB stuff
 */
public class Database {
    final String path;
    private static final String TAG = "Database";
    private static SQLiteOpenHelper openHelper = null;
    private final static String TBL_VOCABLE = "vocables";
    private final static String TBL_TABLES = "voc_tables";
    private final static String TBL_SESSION = "session";
    private final static String TBL_SESSION_META = "session_meta";
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


        class internalDB extends SQLiteOpenHelper {

            public internalDB(Context context) {
                super(context, "test.db", null, 1);
            }

            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL("CREATE TABLE `" + TBL_VOCABLE + "` (`" + KEY_TABLE + "` INTEGER PRIMARY KEY, "
                        + "`" + KEY_VOC + "` INTEGER NOT NULL"
                        + "`" + KEY_WORD_A + "` TEXT NOT NULL, `" + KEY_WORD_B + "` TEXT NOT NULL, `" + KEY_TIP + "` TEXT, "
                        + "`" + KEY_LAST_USED + " INTEGER, PRIMARY KEY (`" + KEY_TABLE + "`,`" + KEY_VOC + "`) )");
                db.execSQL("CREATE TABLE `" + TBL_TABLES + "` ("
                        + "`" + KEY_NAME_TBL + "` TEXT NOT NULL, `" + KEY_TABLE + "` INTEGER NOT NULL,"
                        + "`" + KEY_NAME_A + "` TEXT NOT NULL, `" + KEY_NAME_B + "` TEXT NOT NULL," +
                        "PRIAMRY KEY (`" + KEY_TABLE + "`))");
                db.execSQL("CREATE TABLE `" + TBL_SESSION + "` ("
                        + "`" + KEY_TABLE + "` INTEGER NOT NULL"
                        + "`" + KEY_VOC + "` INTEGER NOT NULL"
                        + "`" + KEY_POINTS + "` INTEGER NOT NULL"
                        + "PRIMARY KEY (`" + KEY_TABLE + "`,`" + KEY_VOC + "`)");
                db.execSQL("CREATE TABLE `" + TBL_SESSION_META + "` (`" + KEY_TABLE + "`)");
            }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }


    }

    /**
     * Database object
     *
     * @param path Path to DB file
     */
    public Database(String path, Context context) {
        this.path = path;
        if(openHelper == null)
            openHelper = new internalDB(context);
    }

    /**
     * Retrns a List of Entries for the specified table
     *
     * @param table Table for which all entries should be retrieved
     * @return List<Entry>
     */
    public List<Entry> getVocableTable(int table) {
        try (
                SQLiteDatabase db = openHelper.getReadableDatabase();
                Cursor cursor = db.rawQuery("SELECT `" + KEY_WORD_A + "`,`" + KEY_WORD_B + "`,`" + KEY_TIP + "`,`" + KEY_VOC + "`,`" + KEY_TABLE + "`,`" + KEY_LAST_USED + "` "
                + "FROM `" + TBL_VOCABLE + "` "
                + "WHERE `" + KEY_TABLE + "` = ?", new String[]{String.valueOf(table)})) {
            List<Entry> lst = new ArrayList<>();
            while (cursor.moveToNext()) {
                lst.add(new Entry(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4), cursor.getInt(5), cursor.getLong(6)));
            }
            return lst;
        }
    }

    /**
     * Update or insert the provided Table datac
     *
     * @param tbl
     * @return true on succuess
     */
    public boolean upsertTable(Table tbl) {
        if (tbl.getId() > 0) {
            try (SQLiteDatabase db = openHelper.getWritableDatabase();
                 SQLiteStatement upd = db.compileStatement("UPDATE `" + TBL_TABLES + "` SET `" + KEY_NAME_A + "` = ?, `" + KEY_NAME_B + "` = ?, `" + KEY_NAME_TBL + "` = ? `"
                    + "WHERE `" + KEY_TABLE + "`= ? ")) {
                upd.clearBindings();
                upd.bindString(1, tbl.getNameA());
                upd.bindString(2, tbl.getNameB());
                upd.bindString(3, tbl.getName());
                ;
                upd.bindLong(4, tbl.getId());
                upd.execute();
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            try (SQLiteDatabase db = openHelper.getWritableDatabase();
                 SQLiteStatement ins = db.compileStatement("INSERT INTO `" + TBL_TABLES + "`(`" + KEY_NAME_TBL + "`,`" + KEY_NAME_A + "`,`" + KEY_NAME_B + "`,`"
                    + KEY_TABLE + "`) (?,?,?,?,?)")) {
                int tbl_id = getHighestTableID(db) + 1;
                ins.bindString(1, tbl.getName());
                ins.bindString(2, tbl.getNameA());
                ins.bindString(3, tbl.getNameB());
                ins.bindLong(4, tbl_id);
                ins.execute();
                tbl.setId(tbl_id);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    /**
     * Test is table exists
     *
     * @param db  Writeable database
     * @param tbl Table
     * @return true if it exists
     */
    private boolean testTableExists(SQLiteDatabase db, Table tbl) {
        if (db == null)
            throw new IllegalArgumentException("illegal sql db");
        if (tbl.getId() < 1)
            return true;

        try (
                Cursor cursor = db.rawQuery("SELECT 1 "
                + "FROM `" + TBL_TABLES + "`"
                + "WHERE `" + KEY_TABLE + "` = ?", new String[]{String.valueOf(tbl.getId())})) {
            List<Entry> lst = new ArrayList<>();
            if (cursor.moveToNext()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Update and/or insert all Entries<br>
     * This function used the delete and changed flags
     *
     * @param lst
     * @return
     */
    public boolean upsertEntries(final List<Entry> lst) {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        try (
                SQLiteStatement delStm = db.compileStatement("DELETE * FROM `" + TBL_VOCABLE + "` WHERE `" + KEY_VOC + "` = ? AND `" + KEY_TABLE + "` = ?");
                SQLiteStatement updStm = db.compileStatement("UPDATE `" + TBL_VOCABLE + "` SET `" + KEY_WORD_A + "` = ?, `" + KEY_WORD_B + "` = ?, `" + KEY_TIP + "` = ? `"
                        + KEY_LAST_USED + "` = ? "
                        + "WHERE `" + KEY_TABLE + "`= ? AND `" + KEY_VOC + "` = ?");
                SQLiteStatement insStm = db.compileStatement("INSERT INTO `" + TBL_VOCABLE + "`(`" + KEY_WORD_A + "`,`" + KEY_WORD_B + "`,`" + KEY_TIP + "`,`"
                        + KEY_LAST_USED + "`,`" + KEY_TABLE + "`,`" + "`,`" + KEY_VOC + "`) (?,?,?,?,?,?)");

        ) {

            db.beginTransaction();
            int lastTableID = -1;
            int lastID = -1;

            for (Entry entry : lst) {
                if (entry.getTable() > 0) {
                    if (entry.isDelete()) {
                        delStm.clearBindings();
                        delStm.bindLong(1, entry.getId());
                        delStm.bindLong(2, entry.getTable());
                        delStm.execute();
                    } else if (entry.isChanged()) {
                        updStm.clearBindings();
                        updStm.bindString(1, entry.getAWord());
                        updStm.bindString(2, entry.getBWord());
                        updStm.bindString(3, entry.getTip());
                        updStm.bindLong(4, entry.getDate());
                        updStm.bindLong(5, entry.getTable());
                        updStm.bindLong(6, entry.getId());
                        updStm.execute();
                    }
                } else {
                    if (entry.getTable() != lastTableID || lastID < 1) {
                        lastTableID = entry.getTable();
                        lastID = getHighestVocID(db, lastTableID);
                    }
                    lastID++;
                    insStm.clearBindings();
                    insStm.bindString(1, entry.getAWord());
                    insStm.bindString(2, entry.getBWord());
                    insStm.bindString(3, entry.getTip());
                    insStm.bindLong(4, entry.getDate());
                    insStm.bindLong(5, entry.getTable());
                    insStm.bindLong(6, lastID);
                    insStm.execute();
                    entry.setId(lastID);
                }
            }
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Returns the highest vocable ID for the specified table
     *
     * @param db
     * @param table
     * @return highest ID
     */
    private int getHighestVocID(final SQLiteDatabase db, final int table) throws Exception {
        if (db == null || table < 1)
            throw new IllegalArgumentException("invalid DB / table");

        try (Cursor cursor = db.rawQuery("SELECT `" + KEY_VOC + "` "
                + "FROM `" + TBL_VOCABLE + "` "
                + "WHERE " + KEY_TABLE + " = ? "
                + "ORDER BY `" + KEY_VOC + "` ASC"
                + "LIMIT 1", new String[]{String.valueOf(table)})) {
            if (cursor.moveToNext()) {
                return cursor.getInt(1);
            } else {
                throw new NoSuchElementException("No vocable ID found!");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Returns the highest table ID
     *
     * @param db
     * @return highest ID
     */
    private int getHighestTableID(final SQLiteDatabase db) throws Exception {
        if (db == null)
            throw new IllegalArgumentException("invalid DB");

        try (Cursor cursor = db.rawQuery("SELECT `" + KEY_TABLE + "` "
                + "FROM `" + TBL_TABLES + "` "
                + "ORDER BY `" + KEY_TABLE + "` ASC"
                + "LIMIT 1", new String[]{})) {
            if (cursor.moveToNext()) {
                return cursor.getInt(1);
            } else {
                throw new NoSuchElementException("No table ID found!");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Deletes the given table and all its vocables
     *
     * @param tbl Table to delete
     * @return true on success
     */
    public boolean deleteTable(final Table tbl) {
        try(
                SQLiteDatabase db = openHelper.getWritableDatabase();
        ) {
            db.beginTransaction();

            String[] arg = new String[]{String.valueOf(tbl.getId())};
            int i = db.delete(TBL_VOCABLE, KEY_TABLE + " = ?", arg);

            db.delete(TBL_TABLES, KEY_TABLE + " = ?", arg);
            db.delete(TBL_SESSION, KEY_TABLE + " = ?", arg);
            db.setTransactionSuccessful();
            db.endTransaction();
            return true;
        }catch (Exception e){
            Log.e(TAG,"error: "+e.getStackTrace().toString());
            return false;
        }
    }

    /**
     * Deletes the current session
     *
     * @return
     */
    public boolean deleteSession() {
        SQLiteDatabase db = openHelper.getWritableDatabase();

        if (db == null) {
            return false;
        }

        db.beginTransaction();
        try {
            db.delete(TBL_SESSION, "1", null);
            db.delete(TBL_SESSION_META, "1", null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG,e.getStackTrace().toString());
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    /**
     * Updates a transaction Entry
     *
     * @param entry Entry to update
     * @return true on success
     */
    public boolean updateTransaction(Entry entry) {
        SQLiteDatabase db = openHelper.getWritableDatabase();

        if (db == null)
            return false;


        try (
                SQLiteStatement updStm = db.compileStatement("INSERT OR REPLACE INTO `" + TBL_SESSION + "` ( `" + KEY_TABLE + "`,`" + KEY_VOC + "`,`" + KEY_POINTS + "` )"
                        + "(?,?,?)")
        ) {
            updStm.bindLong(1, entry.getTable());
            updStm.bindLong(2, entry.getId());
            updStm.bindLong(3, entry.getPoints());
            if (updStm.executeInsert() > 0) { // possible problem ( insert / update..)
                return true;
            } else {
                Log.e(TAG,"Inserted < 1 columns!");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG,e.getStackTrace().toString());
            return false;
        }
    }

    /**
     * Starts a new session based on the table entries<br>
     * Overriding any old session data!
     *
     * @param tables The Table to use for this sessions
     * @return true on success
     */
    public boolean createSession(Collection<Table> tables) {
        SQLiteDatabase db = openHelper.getWritableDatabase();

        if (db == null)
            return false;

        if (deleteSession()) {
            return false;
        }

        db.beginTransaction();

        try (SQLiteStatement insStm = db.compileStatement("INSERT INTO `" + TBL_SESSION_META + "`(`" + KEY_TABLE + "`) (?)")) {
            //TODO: update last_used
            for (Table tbl : tables) {
                insStm.clearBindings();
                insStm.bindLong(1, tbl.getId());
            }
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG,e.getStackTrace().toString());
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Set total and unfinished vocables for each table
     * @param tables
     * @param settings TrainerSettings, used for points treshold etc
     * @return true on success
     */
    public boolean setSessionTableData(List<Table> tables, TrainerSettings settings){
        SQLiteDatabase db = openHelper.getReadableDatabase();

        if(db == null)
            return false;

        for(Table table : tables){
            try(
                    Cursor curLeng = db.rawQuery("SELECT COUNT(*) FROM `"+TBL_VOCABLE+"` WHERE `"+KEY_TABLE+"`  = ?",new String[]{String.valueOf(table.getId())});
                    Cursor curUnfinished = db.rawQuery("SELECT COUNT(*) FROM `"+TBL_SESSION+"` WHERE `"+KEY_TABLE+"`  = ? AND `"+KEY_POINTS+"` < ?",new String[]{String.valueOf(table.getId()), String.valueOf(settings.timesToSolve)});
                    ){
                table.setTotalVocs(curLeng.getInt(1));
                table.setUnfinishedVocs(curUnfinished.getInt(1));
            } catch (Exception e){
                Log.e(TAG,e.getStackTrace().toString());
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a random entry from the specified table, which matche the trainer settings criteria<br>
     *     The Entry is guaranteed to be not the "lastEntry" provided here
     *     //TODO: case of last-entry-standing
     * @param tbl
     * @param ts
     * @return null on error
     */
    public Entry getRandomTrainerEntry(Table tbl, Entry lastEntry, TrainerSettings ts){
        SQLiteDatabase db = openHelper.getReadableDatabase();

        if(db == null){
            return null;
        }

        int lastID = -1;
        if(lastEntry.getTable() == tbl.getId())
            lastID = lastEntry.getId();

        try(
                Cursor cursor= db.rawQuery("SELECT tbl.`"+KEY_VOC+"`, tbl.`"+KEY_TABLE+"`,`"+KEY_WORD_A+"`, `"+KEY_WORD_B+"`, `"+KEY_TIP+"`. `"+KEY_POINTS+"` "
                        +"FROM `"+TBL_VOCABLE+"` tbl LEFT JOIN  `"+TBL_SESSION+"` ses"
                        +" ON tbl."+KEY_VOC+" = ses."+KEY_VOC+" AND tbl."+KEY_TABLE+" = ses."+KEY_TABLE
                        +" WHERE `"+KEY_TABLE+"` = ?"
                        +" ORDER BY RANDOM() LIMIT 1",new String[]{String.valueOf(tbl.getId())});
        ){
            if(cursor.moveToNext()){
                if (cursor.isNull(6)){
                    return new Entry(cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getInt(1),cursor.getInt(2), 0,cursor.getLong(7));
                }
                return new Entry(cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getInt(1),cursor.getInt(2), cursor.getInt(6),cursor.getLong(7));
            }else{
                //TODO: logs
                return null;
            }

        } catch (Exception e){
            Log.e(TAG,e.getStackTrace().toString());
            return null;
        }
    }
}
