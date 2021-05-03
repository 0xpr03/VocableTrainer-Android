package vocabletrainer.heinecke.aron.vocabletrainer.lib

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteStatement
import android.util.Log
import android.util.SparseArray
import androidx.lifecycle.LiveData
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VEntry
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList
import java.io.File
import java.sql.Date
import java.sql.SQLException
import java.util.*

/**
 * Database manager<br></br>
 * Doing all relevant DB stuff
 */
class Database {
    private var db: SQLiteDatabase? = null // pointer to DB used in this class
    private var helper: internalDB? = null

    /**
     * Database for export / import
     *
     * @param context
     * @param file    // file to use for this DB
     */
    constructor(context: Context?, file: File?) {
        helper = internalDB(context, file)
        db = helper!!.writableDatabase
    }
    /**
     * Database object, using internal storage for this App (default DB file)
     *
     * @param context
     * @param dev     set to true for unit tests<br></br>
     * no data will be saved
     */
    /**
     * Database object
     *
     * @param context
     */
    @JvmOverloads
    constructor(context: Context?, dev: Boolean = false) {
        if (dbIntern == null) {
            helper = internalDB(context, dev)
            dbIntern = helper!!.writableDatabase
        }
        db = dbIntern
    }

    /**
     * Retrieve vocable by ID & list ID
     * @param vocID
     * @param listID
     * @return VEntry with set List<br></br>
     * Null on failure
     */
    fun getVocable(vocID: Int, listID: Int): VEntry? {
        db!!.rawQuery(
                "SELECT $KEY_TIP,$KEY_ADDITION,$KEY_LAST_USED"
                        + ",tVoc.$KEY_CREATED,$KEY_CORRECT,$KEY_WRONG"
                        + ",tVoc.$KEY_TABLE,$KEY_NAME_A,$KEY_NAME_B,$KEY_NAME_TBL"
                        + ",tList.$KEY_CREATED;$KEY_POINTS"
                        + " FROM $TBL_VOCABLE tVoc"
                        + " JOIN $TBL_TABLES tList ON tVoc.$KEY_TABLE = tList.$KEY_TABLE"
                        + " LEFT JOIN $TBL_SESSION ses ON tVoc.$KEY_TABLE = ses.$KEY_TABLE"
                        + " AND tVoc.$KEY_VOC = ses.$KEY_VOC"
                        + " WHERE tVoc.$KEY_TABLE = ? AND tVoc.$KEY_VOC = ?", arrayOf(listID.toString(), vocID.toString())).use { cV ->
            return if (cV.moveToNext()) {
                val list = VList(listID, cV.getString(7), cV.getString(8),
                        cV.getString(9), Date(cV.getLong(10)))
                val meaningA: MutableList<String> = LinkedList()
                val meaningB: MutableList<String> = LinkedList()
                val vocable = VEntry(meaningA, meaningB, cV.getString(0),
                        cV.getString(1), vocID, list,
                        if (cV.isNull(11)) 0 else cV.getInt(11),
                        Date(cV.getLong(2)), Date(cV.getLong(3)),
                        cV.getInt(4), cV.getInt(5))
                getVocableMeanings(TBL_MEANING_A, vocable, meaningA)
                getVocableMeanings(TBL_MEANING_B, vocable, meaningB)
                vocable
            } else {
                Log.w(TAG, "vocable not found by ID!")
                null
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
    private fun getVocableMeanings(table: String, vocable: VEntry, list: MutableList<String>) {
        db!!.query(table, arrayOf(KEY_MEANING),
                "$KEY_TABLE = ? AND $KEY_VOC = ?", arrayOf(Integer.toString(vocable.list.id), Integer.toString(vocable.id)),
                null, null, null).use { cM ->
            while (cM.moveToNext()) {
                list.add(cM.getString(0))
            }
            if (list.size == 0) {
                Log.w(TAG, "No meanings in $table for $vocable")
            }
        }
    }

    /**
     * Wipe all session points
     *
     * @return
     */
    fun wipeSessionPoints(): Boolean {
        return try {
            db!!.delete(TBL_SESSION + "", null, null)
            true
        } catch (e: Exception) {
            Log.e(TAG, "", e)
            false
        }
    }

    /**
     * Retruns a List of Entries for the specified list<br></br>
     * <u>No point data is being loaded</u>
     *
     * @param list VList for which all entries should be retrieved
     * @return List<VEntry>
    </VEntry> */
    fun getVocablesOfTable(list: VList): List<VEntry> {
        val sqlMeaning = ("SELECT $KEY_MEANING,$KEY_VOC voc FROM %s WHERE "
                + "$KEY_TABLE = ? ORDER BY voc")
        db!!.rawQuery("SELECT $KEY_TIP,$KEY_ADDITION,$KEY_LAST_USED"
                + ",$KEY_CREATED,$KEY_CORRECT,$KEY_WRONG"
                + ",$KEY_VOC"
                + " FROM $TBL_VOCABLE WHERE $KEY_TABLE = ?", arrayOf(list.id.toString())).use { cV ->
            db!!.rawQuery(String.format(sqlMeaning, TBL_MEANING_A), arrayOf(Integer.toString(list.id))).use { cMA ->
                db!!.rawQuery(String.format(sqlMeaning, TBL_MEANING_B), arrayOf(Integer.toString(list.id))).use { cMB ->
                    val lst: MutableList<VEntry> = ArrayList()
                    val mapA = SparseArray<MutableList<String>>()
                    val mapB = SparseArray<MutableList<String>>()
                    while (cV.moveToNext()) {
                        val meaningA: MutableList<String> = LinkedList()
                        val meaningB: MutableList<String> = LinkedList()
                        val vocable = VEntry(meaningA, meaningB, cV.getString(0),
                                cV.getString(1), cV.getInt(6), list,
                                Date(cV.getLong(2)), Date(cV.getLong(3)),
                                cV.getInt(4), cV.getInt(5))
                        mapA.put(vocable.id, meaningA)
                        mapB.put(vocable.id, meaningB)
                        lst.add(vocable)
                    }
                    handleMeaningData(cMA, mapA)
                    handleMeaningData(cMB, mapB)
                    return lst
                }
            }
        }
    }

    /**
     * Sort meanings from cursor into correct List:String from map
     *
     * @param cursor expected as [0] = String meaning, [1] = int ID
     * @param map
     */
    private fun handleMeaningData(cursor: Cursor, map: SparseArray<MutableList<String>>) {
        var lst: MutableList<String>? = null
        var lastID = ID_RESERVED_SKIP
        while (cursor.moveToNext()) {
            val id = cursor.getInt(1)
            if (id == ID_RESERVED_SKIP) Log.wtf(TAG, "ID is -1!")
            if (id != lastID || lst == null) {
                lst = map[id]
                lastID = id
            }
            lst!!.add(cursor.getString(0))
        }
    }

    /**
     * Debug function to retrieve points of entry
     *
     * @return
     */
    @Deprecated("")
    fun getEntryPoints(ent: VEntry): Int {
        db!!.rawQuery("SELECT $KEY_POINTS "
                + "FROM $TBL_SESSION WHERE $KEY_TABLE = ? AND $KEY_VOC = ?", arrayOf(ent.list.id.toString(), ent.id.toString())).use { cursor -> return if (cursor.moveToNext()) cursor.getInt(0) else -1 }
    }

    /**
     * Get a list of all lists
     *
     * @return ArrayList<\VList>
     */
    val tables: List<VList>?
        get() = getTables(null)

    /**
     * Get a list of all lists
     * @param cancelHandle Handle for cancel, null for cancel disable, value has to be set
     * @return ArrayList<\VList>
     */
    fun getTables(cancelHandle: LiveData<Boolean>?): List<VList>? {
        val column = arrayOf(KEY_TABLE, KEY_NAME_A, KEY_NAME_B, KEY_NAME_TBL, KEY_CREATED)
        try {
            db!!.query(TBL_TABLES, column, null, null, null, null, null).use { cursor ->
                val list: MutableList<VList> = ArrayList()
                while (cursor.moveToNext()) {
                    if (cancelHandle != null && cancelHandle.value!!) {
                        break
                    }
                    val entry = VList(cursor.getInt(0), cursor.getString(1),
                            cursor.getString(2), cursor.getString(3),
                            Date(cursor.getLong(4)))
                    list.add(entry)
                }
                return list
            }
        } catch (e: Exception) {
            Log.e(TAG, "", e)
            return null
        }
    }

    /**
     * Update or insert the provided VList data
     *
     * @param tbl
     * @return true on succuess
     */
    fun upsertVList(tbl: VList): Boolean {
        Log.v(TAG, "upsertVList")
        return if (tbl.isExisting) {
            try {
                val args = arrayOf(tbl.id.toString())
                val values = ContentValues()
                values.put(KEY_NAME_TBL, tbl.name)
                values.put(KEY_NAME_A, tbl.nameA)
                values.put(KEY_NAME_B, tbl.nameB)
                val updated = db!!.update(TBL_TABLES, values, KEY_TABLE + " = ?",
                        args)
                if (updated != 1) {
                    throw SQLException("Update error, updated: $updated")
                }
                true
            } catch (e: Exception) {
                Log.e(TAG, "", e)
                false
            }
        } else {
            try {
                val tbl_id = getHighestTableID(db) + 1
                Log.d(TAG, "highest TBL ID: $tbl_id")
                val values = ContentValues()
                values.put(KEY_TABLE, tbl_id)
                values.put(KEY_NAME_A, tbl.nameA)
                values.put(KEY_NAME_B, tbl.nameB)
                values.put(KEY_NAME_TBL, tbl.name)
                values.put(KEY_CREATED, tbl.created.time)
                db!!.insertOrThrow(TBL_TABLES, null, values)
                tbl.id = tbl_id
                true
            } catch (e: Exception) {
                Log.wtf(TAG, "", e)
                false
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
    private fun testTableExists(db: SQLiteDatabase?, tbl: VList): Boolean {
        requireNotNull(db) { "illegal sql db" }
        if (tbl.isExisting) return false
        try {
            db.rawQuery("SELECT 1 FROM $TBL_TABLES WHERE $KEY_TABLE = ?", arrayOf(tbl.id.toString())).use { cursor -> return cursor.moveToNext() }
        } catch (e: Exception) {
            Log.e(TAG, "", e)
            return false
        }
    }

    /**
     * Update and/or insert all Entries<br></br>
     * This function uses delete and changed flags in entries<br></br>
     * <u>Does not update vocable metadata such as last used etc on changed flag.</u>
     *
     * @param lst
     * @return true on success
     */
    fun upsertEntries(lst: List<VEntry>): Boolean {
        try {
            db!!.compileStatement("DELETE FROM $TBL_VOCABLE WHERE $KEY_VOC = ? AND $KEY_TABLE = ?").use { delStm ->
                db!!.compileStatement("UPDATE $TBL_VOCABLE SET $KEY_ADDITION = ?,$KEY_TIP = ? WHERE $KEY_TABLE= ? AND $KEY_VOC = ?").use { updStm ->
                    db!!.compileStatement("INSERT INTO $TBL_VOCABLE ($KEY_TABLE,$KEY_VOC,$KEY_TIP,$KEY_ADDITION,$KEY_CREATED,$KEY_CORRECT,$KEY_WRONG) VALUES (?,?,?,?,?,?,?)").use { insStm ->
                        db!!.compileStatement("INSERT INTO $TBL_MEANING_A($KEY_TABLE,$KEY_VOC,$KEY_MEANING) VALUES (?,?,?)").use { insMeanA ->
                            db!!.compileStatement("INSERT INTO $TBL_MEANING_B($KEY_TABLE,$KEY_VOC,$KEY_MEANING) VALUES (?,?,?)").use { insMeanB ->
                                val whereDelMeaning = "$KEY_TABLE = ? AND $KEY_VOC = ?"
                                db!!.beginTransaction()
                                var lastTableID = -1
                                var lastID = -1
                                var insertMeanings: Boolean
                                for (entry in lst) {
                                    //Log.d(TAG, "processing " + entry + " of " + entry.getList());
                                    if (entry.id == ID_RESERVED_SKIP) // skip spacer
                                        continue
                                    insertMeanings = false
                                    if (entry.isExisting) {
                                        if (entry.isDelete || entry.isChanged) {
                                            // we need to clear meanings anyway
                                            val args = arrayOf(Integer.toString(entry.list.id), Integer.toString(entry.id))
                                            db!!.delete(TBL_MEANING_B, whereDelMeaning, args)
                                            db!!.delete(TBL_MEANING_A, whereDelMeaning, args)
                                            if (entry.isDelete) {
                                                delStm.clearBindings()
                                                delStm.bindLong(1, entry.id.toLong())
                                                delStm.bindLong(2, entry.list.id.toLong())
                                                delStm.execute()
                                            } else if (entry.isChanged) {
                                                updStm.clearBindings()
                                                updStm.bindString(1, entry.addition)
                                                updStm.bindString(2, entry.tip)
                                                updStm.bindLong(3, entry.list.id.toLong())
                                                updStm.bindLong(4, entry.id.toLong())
                                                updStm.execute()
                                                insertMeanings = true
                                            }
                                        }
                                    } else if (!entry.isDelete) { // vocable created & deleted in editor
                                        if (entry.list.id != lastTableID || lastID < MIN_ID_TRESHOLD) {
                                            lastTableID = entry.list.id
                                            Log.d(TAG, "lastTableID: $lastTableID lastID: $lastID")
                                            lastID = getHighestVocID(db, lastTableID)
                                        }
                                        lastID++ // make last ID to new ID
                                        insStm.clearBindings()
                                        insStm.bindLong(1, entry.list.id.toLong())
                                        insStm.bindLong(2, lastID.toLong())
                                        insStm.bindString(3, entry.tip)
                                        insStm.bindString(4, entry.addition)
                                        insStm.bindLong(5, entry.created.time)
                                        insStm.bindLong(6, entry.correct.toLong())
                                        insStm.bindLong(7, entry.wrong.toLong())
                                        insStm.execute()
                                        insertMeanings = true
                                        entry.id = lastID
                                    }
                                    if (insertMeanings) {
                                        insMeanA.bindLong(1, entry.list.id.toLong())
                                        insMeanA.bindLong(2, entry.id.toLong())
                                        for (meaning in entry.aMeanings) {
                                            insMeanA.bindString(3, meaning)
                                            if (insMeanA.executeInsert() == -1L) {
                                                throw Exception("unable to insert meaning")
                                            }
                                        }
                                        insMeanB.bindLong(1, entry.list.id.toLong())
                                        insMeanB.bindLong(2, entry.id.toLong())
                                        for (meaning in entry.bMeanings) {
                                            insMeanB.bindString(3, meaning)
                                            if (insMeanB.executeInsert() == -1L) {
                                                throw Exception("unable to insert meaning")
                                            }
                                        }
                                    }
                                }
                                db!!.setTransactionSuccessful()
                                return true
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "", e)
            return false
        } finally {
            if (db!!.inTransaction()) {
                Log.d(TAG, "in transaction")
                db!!.endTransaction()
            }
        }
    }

    /**
     * Returns the ID of a table with the exact same naming <br></br>
     * this also updates the VList element itself to contains the right ID
     *
     * @param tbl VList to be used a search source
     * @return ID or  -1 if not found, -2 if an error occurred
     */
    fun getSetTableID(tbl: VList): Int {
        if (tbl.id > -1) {
            return tbl.id
        }
        val args = arrayOf(tbl.name, tbl.nameA, tbl.nameB)
        try {
            db!!.rawQuery("SELECT $KEY_TABLE FROM $TBL_TABLES "
                    +"WHERE $KEY_NAME_TBL = ? AND $KEY_NAME_A = ? AND $KEY_NAME_B  = ? LIMIT 1", args).use { cursor ->
                var id = -1
                if (cursor.moveToNext()) {
                    id = cursor.getInt(0)
                }
                tbl.id = id
                return id
            }
        } catch (e: Exception) {
            Log.e(TAG, "", e)
            return -1
        }
    }

    /**
     * Returns the highest vocable ID for the specified table
     *
     * @param db
     * @param table table ID<br></br>
     * This is on purpose no VList object
     * @return highest ID **or -1 if none is found**
     */
    private fun getHighestVocID(db: SQLiteDatabase?, table: Int): Int {
        if (VList.isIDValid(table)) {
            db!!.rawQuery("SELECT MAX($KEY_VOC) FROM $TBL_VOCABLE WHERE $KEY_TABLE = ? ", arrayOf(table.toString())).use { cursor ->
                return if (cursor.moveToNext()) {
                    cursor.getInt(0)
                } else {
                    MIN_ID_TRESHOLD - 1
                }
            }
        } else {
            throw IllegalArgumentException("invalid table ID!")
        }
    }

    /**
     * Returns the highest table ID
     *
     * @param db
     * @return highest ID,  **-1 is none if found**
     */
    private fun getHighestTableID(db: SQLiteDatabase?): Int {
        requireNotNull(db) { "invalid DB" }
        db.rawQuery("SELECT MAX($KEY_TABLE) FROM $TBL_TABLES", arrayOf()).use { cursor ->
            return if (cursor.moveToNext()) {
                Log.d(TAG, Arrays.toString(cursor.columnNames))
                cursor.getInt(0)
            } else {
                MIN_ID_TRESHOLD - 1
            }
        }
    }

    /**
     * Deletes the given table and all its vocables
     *
     * @param tbl VList to delete
     * @return true on success
     */
    fun deleteTable(tbl: VList): Boolean {
        return try {
            db!!.beginTransaction()
            val arg = arrayOf(tbl.id.toString())
            emptyList_(arg)
            db!!.delete(TBL_TABLES, KEY_TABLE + " = ?", arg)
            db!!.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            Log.e(TAG, "", e)
            false
        } finally {
            if (db!!.inTransaction()) {
                db!!.endTransaction()
            }
        }
    }

    /**
     * directly calls table empty SQL<br></br>
     * <u>does not handle any transactions</u>
     *
     * @param arg String array containing the tbl ID at [0]
     */
    private fun emptyList_(arg: Array<String>) {
        db!!.delete(TBL_SESSION, "$KEY_TABLE = ?", arg)
        db!!.delete(TBL_SESSION_TABLES, "$KEY_TABLE = ?", arg)
        db!!.delete(TBL_SESSION_VOC, "$KEY_TABLE = ?", arg)
        db!!.delete(TBL_VOCABLE, "$KEY_TABLE = ?", arg)
        db!!.delete(TBL_MEANING_B, "$KEY_TABLE = ?", arg)
        db!!.delete(TBL_MEANING_A, "$KEY_TABLE = ?", arg)
    }

    /**
     * Clear vocable list from all entries
     *
     * @param tbl
     * @return
     */
    fun emptyList(tbl: VList): Boolean {
        return try {
            db!!.beginTransaction()
            emptyList_(arrayOf(tbl.id.toString()))
            db!!.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            Log.e(TAG, "", e)
            false
        } finally {
            if (db!!.inTransaction()) db!!.endTransaction()
        }
    }

    /**
     * Deletes the current session
     *
     * @return
     */
    fun deleteSession(): Boolean {
        Log.d(TAG, "entry deleteSession")
        db!!.beginTransaction()
        try {
            db!!.delete(TBL_SESSION, null, null)
            db!!.delete(TBL_SESSION_META, null, null)
            db!!.delete(TBL_SESSION_TABLES, null, null)
            db!!.delete(TBL_SESSION_VOC, null, null)
            db!!.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(TAG, "", e)
            return false
        } finally {
            if (db!!.inTransaction()) db!!.endTransaction()
        }
        Log.d(TAG, "exit deleteSession")
        return true
    }

    /**
     * Updates a transaction VEntry
     *
     * @param entry VEntry to update
     * @return true on success
     */
    fun updateEntryProgress(entry: VEntry): Boolean {
        try {
            db!!.compileStatement("INSERT OR REPLACE INTO $TBL_SESSION ( $KEY_TABLE,$KEY_VOC,$KEY_POINTS )VALUES (?,?,?)").use { updStm ->
                Log.d(TAG, entry.toString())
                //TODO: update date
                updStm.bindLong(1, entry.list.id.toLong())
                updStm.bindLong(2, entry.id.toLong())
                updStm.bindLong(3, entry.points.toLong())
                return if (updStm.executeInsert() > 0) { // possible problem ( insert / update..)
                    Log.d(TAG, "updated voc points")
                    true
                } else {
                    Log.e(TAG, "Inserted < 1 columns!")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "", e)
            return false
        }
    }

    /**
     * Starts a new session based on the table entries<br></br>
     * Overriding any old session data!
     *
     * @param lists The VList to use for this sessions
     * @return true on success
     */
    fun createSession(lists: Collection<VList>): Boolean {
        Log.d(TAG, "entry createSession")
        db!!.beginTransaction()
        try {
            db!!.compileStatement("INSERT INTO $TBL_SESSION_TABLES ($KEY_TABLE) VALUES (?)").use { insStm ->
                for (tbl in lists) {
                    insStm.clearBindings()
                    insStm.bindLong(1, tbl.id.toLong())
                    if (insStm.executeInsert() < 0) {
                        Log.wtf(TAG, "no new table inserted")
                    }
                }
                db!!.setTransactionSuccessful()
                Log.d(TAG, "exit createSession")
                return true
            }
        } catch (e: Exception) {
            Log.wtf(TAG, "", e)
            return false
        } finally {
            if (db!!.inTransaction()) db!!.endTransaction()
        }
    }

    /**
     * Returns the table selection from the stored session
     *
     * @return never null
     */
    val sessionTables: ArrayList<VList>
        get() {
            val lst = ArrayList<VList>(10)
            try {
                db!!.rawQuery("SELECT ses.$KEY_TABLE tbl,$KEY_NAME_A,$KEY_NAME_B,$KEY_NAME_TBL,$KEY_CREATED "
                        +"FROM $TBL_SESSION_TABLES ses JOIN $TBL_TABLES tbls ON tbls.$KEY_TABLE == ses.$KEY_TABLE", null).use { cursor ->
                    while (cursor.moveToNext()) {
                        lst.add(VList(cursor.getInt(0), cursor.getString(1),
                                cursor.getString(2), cursor.getString(3),
                                Date(cursor.getLong(4))))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "", e)
            }
            return lst
        }
    val isSessionStored: Boolean
        get() {
            Log.d(TAG, "entry isSessionStored")
            try {
                db!!.rawQuery("SELECT COUNT(*) FROM $TBL_SESSION_TABLES WHERE 1", null).use { cursor ->
                    cursor.moveToNext()
                    if (cursor.getInt(0) > 0) {
                        Log.d(TAG, "found session")
                        return true
                    }
                }
            } catch (e: Exception) {
                Log.wtf(TAG, "unable to get session lists row count", e)
            }
            return false
        }

    /**
     * Set total and unfinished vocables for each table, generate list of finished
     *
     * @param lists           list of VList to process
     * @param unfinishedLists List into which unfinished VList are added into
     * @param settings         TrainerSettings, used for points threshold etc
     * @return true on success
     */
    fun getSessionTableData(lists: List<VList>, unfinishedLists: MutableList<VList?>, settings: TrainerSettings): Boolean {
        require(!(lists == null || unfinishedLists == null || lists.size == 0))
        unfinishedLists.clear()
        for (list in lists) {
            try {
                db!!.rawQuery("SELECT COUNT(*) FROM $TBL_VOCABLE WHERE $KEY_TABLE  = ?", arrayOf(list.id.toString())).use { curLeng ->
                    db!!.rawQuery("SELECT COUNT(*) FROM $TBL_SESSION WHERE $KEY_TABLE  = ? AND $KEY_POINTS >= ?", arrayOf(list.id.toString(), settings.timesToSolve.toString())).use { curFinished ->
                        if (!curLeng.moveToNext()) return false
                        list.totalVocs = curLeng.getInt(0)
                        if (!curFinished.moveToNext()) return false
                        val unfinished = list.totalVocs - curFinished.getInt(0)
                        list.unfinishedVocs = unfinished
                        if (unfinished > 0) {
                            unfinishedLists.add(list)
                        }
                        Log.d(TAG, list.toString())
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "", e)
                return false
            }
        }
        return true
    }

    /**
     * Returns a random entry from the specified table, which matche the trainer settings criteria<br></br>
     * The VEntry is guaranteed to be not the "lastEntry" provided here
     *
     * @param list
     * @param ts
     * @param allowRepetition set to true to allow selecting the same vocable as lastEntry again
     * @return null on error
     */
    fun getRandomTrainerEntry(list: VList, lastEntry: VEntry?, ts: TrainerSettings, allowRepetition: Boolean): VEntry? {
        Log.d(TAG, "getRandomTrainerEntry")
        var lastID = MIN_ID_TRESHOLD - 1
        if (lastEntry != null && lastEntry.list.id == list.id && !allowRepetition) lastID = lastEntry.id
        val arg = arrayOf(list.id.toString(), lastID.toString(), ts.timesToSolve.toString())
        Log.v(TAG, Arrays.toString(arg))
        try {
            db!!.rawQuery(
                    "SELECT $KEY_TIP,$KEY_ADDITION,$KEY_LAST_USED,tbl.$KEY_CREATED,$KEY_CORRECT,$KEY_WRONG,tbl.$KEY_VOC,$KEY_POINTS "
                            +"FROM $TBL_VOCABLE tbl "
                            +"LEFT JOIN  $TBL_SESSION ses ON tbl.$KEY_VOC = ses.$KEY_VOC AND tbl.$KEY_TABLE = ses.$KEY_TABLE "
                            +"WHERE tbl.$KEY_TABLE = ? AND tbl.$KEY_VOC != ? AND ( $KEY_POINTS IS NULL OR $KEY_POINTS < ? ) "
                            +"ORDER BY RANDOM() LIMIT 1", arg).use { cV ->
                return if (cV.moveToNext()) {
                    val meaningA: MutableList<String> = LinkedList()
                    val meaningB: MutableList<String> = LinkedList()
                    val vocable = VEntry(meaningA, meaningB, cV.getString(0),
                            cV.getString(1), cV.getInt(6), list,
                            if (cV.isNull(7)) 0 else cV.getInt(7),
                            Date(cV.getLong(2)), Date(cV.getLong(3)),
                            cV.getInt(4), cV.getInt(5))
                    getVocableMeanings(TBL_MEANING_A, vocable, meaningA)
                    getVocableMeanings(TBL_MEANING_B, vocable, meaningB)
                    vocable
                } else {
                    Log.w(TAG, "no entries found!")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "", e)
            return null
        }
    }

    /**
     * Get session meta value for specified key
     *
     * @param key
     * @return null if no entry is found
     */
    fun getSessionMetaValue(key: String?): String? {
        requireNotNull(key)
        try {
            db!!.rawQuery("SELECT $KEY_MVALUE FROM $TBL_SESSION_META WHERE $KEY_MKEY = ?", arrayOf(key.toString())).use { cursor ->
                return if (cursor.moveToNext()) {
                    cursor.getString(1)
                } else {
                    Log.d(TAG, "No value for key $key")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "error on session meta retrieval", e)
            return null
        }
    }

    /**
     * Set session key-value pair
     *
     * @param key
     * @param value
     * @return true on success
     */
    fun setSessionMetaValue(key: String?, value: String?): Boolean {
        try {
            db!!.compileStatement("INSERT OR REPLACE INTO $TBL_SESSION_META ( $KEY_MKEY,$KEY_MVALUE ) VALUES (?,?)").use { updStm ->
                updStm.bindString(1, key)
                updStm.bindString(2, value)
                return if (updStm.executeInsert() > 0) { // possible problem ( insert / update..)
                    true
                } else {
                    Log.e(TAG, "Inserted < 1 columns!")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "", e)
            return false
        }
    }

    /**
     * Returns a statement to insert / replace session meta storage values
     *
     * @return
     */
    val sessionInsertStm: SQLiteStatement
        get() {
            db!!.beginTransaction()
            return db!!.compileStatement("INSERT OR REPLACE INTO $TBL_SESSION_META ($KEY_MKEY,$KEY_MVALUE) VALUES (?,?)")
        }

    /**
     * Start Transaction
     */
    fun startTransaction() {
        db!!.beginTransaction()
    }

    /**
     * Ends a transaction created by the getSessionInsert Statement
     */
    fun endTransaction(success: Boolean) {
        check(db!!.inTransaction()) { "No transaction ongoing!" }
        Log.d(TAG, "transaction success: $success")
        if (success) db!!.setTransactionSuccessful()
        db!!.endTransaction()
    }

    /**
     * Returns a cursor on the session data
     *
     * @return map of all key-value pairs or **null** on errors
     */
    val sessionData: HashMap<String, String>?
        get() {
            Log.d(TAG, "entry getSessionData")
            val map = HashMap<String, String>(10)
            try {
                db!!.rawQuery("SELECT $KEY_MKEY, $KEY_MVALUE FROM $TBL_SESSION_META WHERE 1", null).use { cursor ->
                    while (cursor.moveToNext()) {
                        map[cursor.getString(0)] = cursor.getString(1)
                    }
                    return map
                }
            } catch (e: Exception) {
                Log.wtf(TAG, "Session data retrieval failure", e)
                return null
            }
        }

    internal inner class internalDB : SQLiteOpenHelper {
        private val sql_a = ("CREATE TABLE " + TBL_TABLES + " ("
                + KEY_NAME_TBL + " TEXT NOT NULL,"
                + KEY_TABLE + " INTEGER PRIMARY KEY,"
                + KEY_NAME_A + " TEXT NOT NULL,"
                + KEY_NAME_B + " TEXT NOT NULL,"
                + KEY_CREATED + " INTEGER NOT NULL )")
        private val sql_b = ("CREATE TABLE " + TBL_VOCABLE + " ("
                + KEY_TABLE + " INTEGER NOT NULL, "
                + KEY_VOC + " INTEGER NOT NULL,"
                + KEY_TIP + " TEXT,"
                + KEY_ADDITION + "TEXT,"
                + KEY_LAST_USED + " INTEGER,"
                + KEY_CREATED + " INTEGER NOT NULL,"
                + KEY_CORRECT + " INTEGER NOT NULL,"
                + KEY_WRONG + " INTEGER NOT NULL,"
                + "PRIMARY KEY (" + KEY_TABLE + "," + KEY_VOC + ") )")
        private val sql_c = ("CREATE TABLE " + TBL_MEANING_A + " ("
                + KEY_TABLE + " INTEGER NOT NULL,"
                + KEY_VOC + " INTEGER NOT NULL,"
                + KEY_MEANING + " TEXT NOT NULL )")
        private val sql_d = ("CREATE TABLE " + TBL_MEANING_B + " ("
                + KEY_TABLE + " INTEGER NOT NULL,"
                + KEY_VOC + " INTEGER NOT NULL,"
                + KEY_MEANING + " TEXT NOT NULL )")
        private val sql_e = ("CREATE INDEX primA ON " + TBL_MEANING_A
                + "( " + KEY_TABLE + "," + KEY_VOC + " )")
        private val sql_f = ("CREATE INDEX primB ON " + TBL_MEANING_B
                + "( " + KEY_TABLE + "," + KEY_VOC + " )")
        private val sql_g = ("CREATE TABLE " + TBL_SESSION + " ("
                + KEY_TABLE + " INTEGER NOT NULL,"
                + KEY_VOC + " INTEGER NOT NULL,"
                + KEY_POINTS + " INTEGER NOT NULL,"
                + "PRIMARY KEY (" + KEY_TABLE + "," + KEY_VOC + "))")
        private val sql_h = ("CREATE TABLE " + TBL_SESSION_META + " ("
                + KEY_MKEY + " TEXT NOT NULL,"
                + KEY_MVALUE + " TEXT NOT NULL,"
                + "PRIMARY KEY (" + KEY_MKEY + "," + KEY_MVALUE + "))")
        private val sql_i = ("CREATE TABLE " + TBL_SESSION_TABLES + " ("
                + KEY_TABLE + " INTEGER PRIMARY KEY )")
        private val sql_j = ("CREATE TABLE " + TBL_SESSION_VOC + " ("
                + KEY_TABLE + " INTEGER NOT NULL,"
                + KEY_VOC + " INTEGER NOT NULL,"
                + "PRIMARY KEY (" + KEY_TABLE + "," + KEY_VOC + "))")

        constructor(context: Context?, databaseFile: File?) : super(DatabaseContext(context, databaseFile), "", null, Companion.DATABASE_VERSION) {}
        constructor(context: Context?) : this(context, false) {}
        constructor(context: Context?, dev: Boolean) : super(context, if (dev) DB_NAME_DEV else DB_NAME_PRODUCTION, null, Companion.DATABASE_VERSION) {}

        /**
         * Check for illegal ID entries below the threshold
         * @param key key to compare
         * @param table table to look into
         */
        private fun checkIllegalIDs(db: SQLiteDatabase, key: String, table: String) {
            db.rawQuery("SELECT COUNT(*) "
                    + "FROM " + table + " WHERE " + key + " < 0", null).use { cursor ->
                if (cursor.moveToNext()) {
                    val amount = cursor.getInt(0)
                    check(amount <= 0) { "Illegal entries of $key in $table: $amount" }
                    Log.v(TAG, "check passed for $key $table")
                }
            }
        }

        fun checkForIllegalIds(db: SQLiteDatabase) {
            Log.d(TAG, "checking for illegal IDs")
            checkIllegalIDs(db, KEY_VOC, TBL_VOCABLE)
            checkIllegalIDs(db, KEY_TABLE, TBL_VOCABLE)
            checkIllegalIDs(db, KEY_TABLE, TBL_TABLES)
            checkIllegalIDs(db, KEY_VOC, TBL_MEANING_A)
            checkIllegalIDs(db, KEY_TABLE, TBL_MEANING_A)
            checkIllegalIDs(db, KEY_VOC, TBL_MEANING_B)
            checkIllegalIDs(db, KEY_TABLE, TBL_MEANING_B)
            Log.d(TAG, "check passed")
        }

        override fun onCreate(db: SQLiteDatabase) {
            Log.d(TAG, "creating db")
            val tables = arrayOf(sql_a, sql_b, sql_c, sql_d, sql_e, sql_f, sql_g, sql_h, sql_i, sql_j)
            var i = 0
            db.beginTransaction()
            try {
                i = 0
                while (i < tables.size) {
                    db.compileStatement(tables[i])
                            .execute()
                    i++
                }
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                Log.wtf(TAG, "Database creation error", e)
                Log.w(TAG, "At Table " + tables[i])
                throw e
            } finally {
                db.endTransaction()
            }
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            Log.d(TAG, "db upgrade triggered old:$oldVersion new:$newVersion")
            if (oldVersion < 2) {
                val newTables = arrayOf(sql_a, sql_b, sql_c, sql_d, sql_e, sql_f, sql_j)
                for (sql in newTables) db.execSQL(sql)
                val time = java.lang.Long.toString(System.currentTimeMillis())
                run {
                    db.execSQL("ALTER TABLE $TBL_VOCABLE_V1 ADD COLUMN $KEY_ADDITION TEXT")
                    db.execSQL("ALTER TABLE $TBL_VOCABLE_V1 ADD COLUMN $KEY_CORRECT INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE $TBL_VOCABLE_V1 ADD COLUMN $KEY_WRONG INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE $TBL_VOCABLE_V1 ADD COLUMN $KEY_CREATED INTEGER NOT NULL DEFAULT $time")
                    val args = arrayOf(KEY_TABLE, KEY_VOC,KEY_TIP, KEY_ADDITION, KEY_LAST_USED,
                            KEY_CREATED, KEY_CORRECT,KEY_WRONG).joinToString(separator = ",")
                    val sqlCpy = "INSERT INTO $TBL_VOCABLE ($args) SELECT $args FROM $TBL_VOCABLE_V1"
                    db.execSQL(sqlCpy)
                    val colMA = arrayOf(KEY_TABLE, KEY_VOC, KEY_MEANING).joinToString(separator = ",")
                    val selMA = arrayOf(KEY_TABLE, KEY_VOC, KEY_WORD_A).joinToString(separator = ",")
                    val sqlMeaningA = "INSERT INTO $TBL_MEANING_A ($colMA) SELECT $selMA FROM $TBL_VOCABLE_V1"
                    db.execSQL(sqlMeaningA)
                    val sqlMeaningB = sqlMeaningA.replace(KEY_WORD_A.toRegex(), KEY_WORD_B)
                            .replace(TBL_MEANING_A.toRegex(), TBL_MEANING_B)
                    db.execSQL(sqlMeaningB)
                    db.execSQL("DROP TABLE $TBL_VOCABLE_V1")
                }
                run {
                    db.execSQL("ALTER TABLE $TBL_TABLES_V1 ADD COLUMN $KEY_CREATED INTEGER NOT NULL DEFAULT $time")
                    val args = arrayOf(KEY_NAME_TBL, KEY_TABLE,KEY_NAME_A, KEY_NAME_B, KEY_CREATED).joinToString(separator = ",")
                    val sqlCpy = "INSERT INTO $TBL_TABLES ($args) SELECT $args FROM $TBL_TABLES_V1"
                    db.execSQL(sqlCpy)
                    db.execSQL("DROP TABLE $TBL_TABLES_V1")
                }
                run { checkForIllegalIds(db) }
            }
            Log.v(TAG, "upgrade end")
        }
    }

    companion object {
        private const val TAG = "Database"
        const val DB_NAME_DEV = "test1.db"
        private const val DB_NAME_PRODUCTION = "voc.db"
        const val MIN_ID_TRESHOLD = 0
        const val ID_RESERVED_SKIP = -2
        private const val TBL_VOCABLE = "`vocables2`"
        private const val TBL_TABLES = "`voc_tables2`"
        private const val TBL_SESSION = "`session`"
        private const val TBL_SESSION_META = "`session_meta`"
        private const val TBL_SESSION_TABLES = "`session_tables`"
        private const val TBL_MEANING_A = "`meaning_a`"
        private const val TBL_MEANING_B = "`meaning_b`"
        private const val TBL_SESSION_VOC = "`session_voc`"
        private const val KEY_VOC = "`voc`"
        private const val KEY_NAME_A = "`name_a`"
        private const val KEY_NAME_B = "`name_b`"
        private const val KEY_TIP = "`tip`"
        private const val KEY_TABLE = "`table`"
        private const val KEY_LAST_USED = "`last_used`"
        private const val KEY_NAME_TBL = "`name`"
        private const val KEY_MEANING = "`meaning`"
        private const val KEY_CREATED = "`created`"
        private const val KEY_CORRECT = "`correct`"
        private const val KEY_WRONG = "`wrong`"
        private const val KEY_ADDITION = "`addition`"
        private const val KEY_POINTS = "`points`"
        private const val KEY_MKEY = "`key`"
        private const val KEY_MVALUE = "`value`"
        private var dbIntern: SQLiteDatabase? = null // DB to internal file, 99% of the time used

        private const val DATABASE_VERSION = 2

        @Deprecated("")
        private val TBL_VOCABLE_V1 = "`vocables`"

        @Deprecated("")
        private val TBL_TABLES_V1 = "`voc_tables`"

        @Deprecated("")
        private val KEY_WORD_A = "`word_a`"

        @Deprecated("")
        private val KEY_WORD_B = "`word_b`"
    }
}