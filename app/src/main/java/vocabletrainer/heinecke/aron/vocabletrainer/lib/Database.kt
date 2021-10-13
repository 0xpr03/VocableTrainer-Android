package vocabletrainer.heinecke.aron.vocabletrainer.lib

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteStatement
import android.text.TextUtils
import android.util.Log
import androidx.collection.LongSparseArray
import androidx.core.database.getStringOrNull
import androidx.lifecycle.LiveData
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VEntry
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList
import java.sql.Date
import java.sql.SQLException
import java.util.*
import android.R.id





/**
 * Database manager<br></br>
 * Doing all relevant DB stuff
 */
class Database {
    private var db: SQLiteDatabase? = null // pointer to DB used in this class
    private var helper: internalDB? = null
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
    constructor(context: Context?) {
        if (dbIntern == null) {
            helper = internalDB(context)
            dbIntern = helper!!.writableDatabase
        }
        db = dbIntern
    }

    /**
     * Close DB, all calls afterwards are UB!
     */
    fun close() {
        db!!.close();
    }

    /**
     * Retrieve vocable by ID
     * @param vocID
     * @param listID
     * @return VEntry with set List<br></br>
     * Null on failure
     */
    fun getVocable(vocID: Long): VEntry? {
        db!!.rawQuery(
                "SELECT $KEY_TIP, $KEY_ADDITION, $KEY_LAST_USED, tVoc.$KEY_CREATED, "
                +"tVoc.$KEY_LIST, $KEY_NAME_A, $KEY_NAME_B, $KEY_NAME_LIST,"
                +"tList.$KEY_CREATED, $KEY_POINTS, $KEY_LIST_UUID, tList.$KEY_CHANGED, tVoc.$KEY_CHANGED,"
                +"tVoc.$KEY_ENTRY_UUID"
                +"FROM $TBL_ENTRIES tVoc "
                +"JOIN $TBL_LISTS tList ON tVoc.$KEY_LIST = tList.$KEY_LIST "
                +"LEFT JOIN $TBL_SESSION ses ON tVoc.$KEY_ENTRY = ses.$KEY_ENTRY"
                +"LEFT JOIN $TBL_LIST_SYNC listSync ON tVoc.$KEY_LIST = listSync.$KEY_LIST"
                +"WHERE tVoc.$KEY_ENTRY = ?", arrayOf(vocID.toString())).use { cV ->
            return if (cV.moveToNext()) {
                val list = VList(
                    _id = cV.getLong(4), _name = cV.getString(7),
                    _nameA = cV.getString(5), _nameB = cV.getString(6),
                    created = Date(cV.getLong(8)), uuid = parseUUID(cV.getStringOrNull(10)),
                    changed = Date(cV.getLong(11))
                )
                val meaningA: MutableList<String> = ArrayList()
                val meaningB: MutableList<String> = ArrayList()
                val vocable = VEntry(
                    meaningA = meaningA, meaningB = meaningB, _tip = cV.getString(0),
                    _addition = cV.getString(1), id = vocID, list = list,
                    _points = if (cV.isNull(9)) 0 else cV.getInt(9),
                    last_used = Date(cV.getLong(2)), created = Date(cV.getLong(3)),
                    changed = Date(cV.getLong(12)),uuid = parseUUID(cV.getStringOrNull(13))
                )
                getVocableMeanings(TBL_WORDS_A, vocable, meaningA)
                getVocableMeanings(TBL_WORDS_B, vocable, meaningB)
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
                "$KEY_ENTRY = ?", arrayOf(vocable.id.toString()),
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
     * Returns a List of Entries for the specified list<br></br>
     * <u>No point data is being loaded</u>
     *
     * @param list VList for which all entries should be retrieved
     * @return List<VEntry>
    </VEntry> */
    fun getVocablesOfTable(list: VList): List<VEntry> {
        // we read all the meanings for this table, then sort them to their entry
        val sqlMeaning = ("SELECT $KEY_MEANING,m.$KEY_ENTRY voc FROM %s m "
                +"JOIN $TBL_ENTRIES e ON e.$KEY_ENTRY = m.$KEY_ENTRY "
                + "WHERE e.$KEY_LIST = ? ORDER BY voc")
        db!!.rawQuery(
            "SELECT $KEY_TIP, $KEY_ADDITION, $KEY_CREATED,"
                    + "e.$KEY_ENTRY,$KEY_ENTRY_UUID,$KEY_CHANGED "
                    + "FROM $TBL_ENTRIES e LEFT JOIN $TBL_ENTRY_SYNC s ON s.$KEY_ENTRY = e.$KEY_ENTRY "
                    + "WHERE $KEY_LIST = ?", arrayOf(list.id.toString())
        ).use { cV ->
            db!!.rawQuery(String.format(sqlMeaning, TBL_WORDS_A), arrayOf(list.id.toString()))
                .use { cMA ->
                    db!!.rawQuery(
                        String.format(sqlMeaning, TBL_WORDS_B),
                        arrayOf(list.id.toString())
                    ).use { cMB ->
                        val lst: MutableList<VEntry> = ArrayList()
                        val mapA = LongSparseArray<MutableList<String>>()
                        val mapB = LongSparseArray<MutableList<String>>()
                        while (cV.moveToNext()) {
                            val meaningA: MutableList<String> = ArrayList()
                            val meaningB: MutableList<String> = ArrayList()
                            val vocable = VEntry(
                                meaningA = meaningA, meaningB = meaningB,
                                _tip = cV.getString(0), _addition = cV.getString(1),
                                id = cV.getLong(3), list = list,
                                last_used = null,
                                created = Date(cV.getLong(2)),
                                changed = Date(cV.getLong(5)),
                                uuid = parseUUID(cV.getStringOrNull(4))
                            )
                            mapA.put(vocable.id, meaningA)
                            mapB.put(vocable.id, meaningB)
                            lst.add(vocable)
                        }
                        handleMeaningData(cMA, mapA)
                        handleMeaningData(cMB, mapB)
                        Log.d(TAG,"Loaded list: $lst")
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
    private fun handleMeaningData(cursor: Cursor, map: LongSparseArray<MutableList<String>>) {
        var lst: MutableList<String>? = null
        var lastID = ID_RESERVED_SKIP
        while (cursor.moveToNext()) {
            val id = cursor.getLong(1)
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
        db!!.rawQuery(
            "SELECT $KEY_POINTS "
                    + "FROM $TBL_SESSION WHERE $KEY_ENTRY = ?",
            arrayOf(ent.id.toString())
        ).use { cursor -> return if (cursor.moveToNext()) cursor.getInt(0) else -1 }
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
        val column = listOf(KEY_LIST, KEY_NAME_A, KEY_NAME_B, KEY_NAME_LIST, KEY_CREATED,
            KEY_CHANGED, KEY_LIST_UUID)
        try {
            db!!.rawQuery("SELECT l.${column.joinToString (separator = ",")} FROM $TBL_LISTS l LEFT JOIN $TBL_LIST_SYNC s " +
                    "ON s.$KEY_LIST = l.$KEY_LIST", null).use { cursor ->
                val list: MutableList<VList> = ArrayList()
                while (cursor.moveToNext()) {
                    if (cancelHandle != null && cancelHandle.value!!) {
                        break
                    }
                    val entry = VList(
                        _id = cursor.getLong(0),
                        _nameA = cursor.getString(1),
                        _nameB = cursor.getString(2),
                        _name = cursor.getString(3),
                        created = Date(cursor.getLong(4)),
                        changed = Date(cursor.getLong(5)),
                        uuid = parseUUID(cursor.getStringOrNull(6))
                    )
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
    fun upsertVList(tbl: VList) {
        Log.v(TAG, "upsertVList")
        if (tbl.isExisting) {
            val args = arrayOf(tbl.id.toString())
            val values = ContentValues()
            values.put(KEY_NAME_LIST, tbl.name)
            values.put(KEY_NAME_A, tbl.nameA)
            values.put(KEY_NAME_B, tbl.nameB)
            values.put(KEY_CHANGED, tbl.changed.time)
            val updated = db!!.update(TBL_LISTS, values, KEY_LIST + " = ?",
                args)
            if (updated != 1) {
                throw SQLException("Update error, updated: $updated expected 1")
            }
        } else {
            ContentValues().apply {
                put(KEY_NAME_A, tbl.nameA)
                put(KEY_NAME_B, tbl.nameB)
                put(KEY_NAME_LIST, tbl.name)
                put(KEY_CREATED, tbl.created.time)
                put(KEY_CHANGED, tbl.changed.time)
                tbl.id = db!!.insertOrThrow(TBL_LISTS, null, this)
            }
            tbl.uuid?.let {
                ContentValues().apply {
                    put(KEY_LIST, tbl.id)
                    put(KEY_LIST_UUID, uuidToString(it))
                    db!!.insertOrThrow(TBL_LIST_SYNC, null, this)
                }
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
            db.rawQuery("SELECT 1 FROM $TBL_LISTS WHERE $KEY_LIST = ?", arrayOf(tbl.id.toString())).use { cursor -> return cursor.moveToNext() }
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
    fun upsertEntries(lst: List<VEntry>) {
        try {
            db!!.compileStatement("DELETE FROM $TBL_ENTRIES WHERE $KEY_ENTRY = ?").use { delStm ->
                db!!.compileStatement("UPDATE $TBL_ENTRIES SET $KEY_ADDITION = ?,$KEY_TIP = ?, $KEY_CHANGED = ? WHERE $KEY_ENTRY = ?").use { updStm ->
                    db!!.compileStatement("INSERT INTO $TBL_ENTRIES ($KEY_LIST,$KEY_TIP,$KEY_ADDITION,$KEY_CREATED,$KEY_CHANGED) VALUES (?,?,?,?,?)").use { insStm ->
                        db!!.compileStatement("INSERT INTO $TBL_WORDS_A($KEY_ENTRY,$KEY_MEANING) VALUES (?,?)").use { insMeanA ->
                            db!!.compileStatement("INSERT INTO $TBL_WORDS_B($KEY_ENTRY,$KEY_MEANING) VALUES (?,?)").use { insMeanB ->
                                val whereDelMeaning = "$KEY_ENTRY = ?"
                                db!!.beginTransaction()
                                var insertMeanings: Boolean
                                for (entry in lst) {
                                    //Log.d(TAG, "processing " + entry + " of " + entry.getList());
                                    if (entry.id == ID_RESERVED_SKIP) {
                                        // skip spacer, but not "invalid" -> new entries
                                        continue
                                    }
                                    insertMeanings = false
                                    if (entry.isExisting) {
                                        if (entry.isDelete || entry.isChanged()) {
                                            // we can clear meanings anyway
                                            val args = arrayOf(entry.id.toString())
                                            db!!.delete(TBL_WORDS_B, whereDelMeaning, args)
                                            db!!.delete(TBL_WORDS_A, whereDelMeaning, args)
                                            if (entry.isDelete) {
                                                delStm.clearBindings()
                                                delStm.bindLong(1, entry.id)
                                                delStm.execute()
                                            } else if (entry.isChanged()) {
                                                updStm.clearBindings()
                                                updStm.bindString(1, entry.addition)
                                                updStm.bindString(2, entry.tip)
                                                updStm.bindLong(3, entry.changed.time)
                                                updStm.bindLong(4, entry.id)
                                                updStm.execute()
                                                insertMeanings = true
                                            }
                                        }
                                    } else if (!entry.isDelete) { // avoid vocable created & deleted in editor
                                        insStm.clearBindings()
                                        insStm.bindLong(1, entry.list!!.id)
                                        insStm.bindString(2, entry.tip)
                                        insStm.bindString(3, entry.addition)
                                        insStm.bindLong(4, entry.created.time)
                                        insStm.bindLong(5, entry.changed.time)
                                        entry.id = insStm.executeInsert()
                                        entry.uuid?.let {
                                            ContentValues().apply {
                                                put(KEY_ENTRY, entry.id)
                                                put(KEY_ENTRY_UUID, uuidToString(it))
                                                db!!.insertOrThrow(TBL_ENTRY_SYNC, null, this)
                                            }
                                        }
                                        insertMeanings = true
                                    }
                                    if (insertMeanings) {
                                        insMeanA.bindLong(1, entry.id)
                                        for (meaning in entry.aMeanings) {
                                            insMeanA.bindString(2, meaning)
                                            if (insMeanA.executeInsert() == -1L) {
                                                throw Exception("unable to insert meaning")
                                            }
                                        }
                                        insMeanB.bindLong(1, entry.id)
                                        for (meaning in entry.bMeanings) {
                                            insMeanB.bindString(2, meaning)
                                            if (insMeanB.executeInsert() == -1L) {
                                                throw Exception("unable to insert meaning")
                                            }
                                        }
                                    }
                                }
                                db!!.setTransactionSuccessful()
                            }
                        }
                    }
                }
            }
        } finally {
            if (db!!.inTransaction()) {
                db!!.endTransaction()
            }
        }
    }

    /**
     * Returns the ID of a table with the exact same naming <br></br>
     * this also updates the VList element itself to contain the right ID
     *
     * @param tbl VList to be used a search source
     * @return ID or  -1 if not found, -2 if an error occurred
     */
    fun getSetTableID(tbl: VList): Long {
        if (tbl.id > -1) {
            return tbl.id
        }
        val args = arrayOf(tbl.name, tbl.nameA, tbl.nameB)
        try {
            db!!.rawQuery("SELECT $KEY_LIST FROM $TBL_LISTS "
                    + "WHERE $KEY_NAME_LIST = ? AND $KEY_NAME_A = ? AND $KEY_NAME_B  = ? LIMIT 1", args).use { cursor ->
                var id = -1L
                if (cursor.moveToNext()) {
                    id = cursor.getLong(0)
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
     * Deletes the given table and all its vocables
     *
     * @param list VList to delete
     * @return true on success
     */
    fun deleteList(list: VList) {
        try {
            db!!.beginTransaction()
            db!!.delete(TBL_LISTS, "$KEY_LIST = ?", arrayOf(list.id.toString()))
            db!!.setTransactionSuccessful()
        } finally {
            if (db!!.inTransaction()) {
                db!!.endTransaction()
            }
        }
    }

    /**
     * Clear vocable list from all entries
     *
     * @param tbl
     * @return
     */
    fun truncateList(tbl: VList) {
        try {
            db!!.beginTransaction()
            db!!.delete(KEY_ENTRY,"$KEY_LIST = ?", arrayOf(tbl.id.toString()))
            db!!.setTransactionSuccessful()
        } finally {
            if (db!!.inTransaction()) db!!.endTransaction()
        }
    }

    /**
     * Deletes the current session
     *
     * @return
     */
    fun deleteSession() {
        db!!.beginTransaction()
        try {
            db!!.delete(TBL_SESSION, null, null)
            db!!.delete(TBL_SESSION_META, null, null)
            db!!.delete(TBL_SESSION_LISTS, null, null)
            db!!.setTransactionSuccessful()
        } finally {
            if (db!!.inTransaction()) db!!.endTransaction()
        }
    }

    /**
     * Updates a transaction VEntry
     *
     * @param entry VEntry to update
     * @return true on success
     */
    fun updateEntryProgress(entry: VEntry) {
        db!!.compileStatement("INSERT OR REPLACE INTO $TBL_SESSION ( $KEY_ENTRY,$KEY_POINTS )VALUES (?,?)").use { updStm ->
            Log.d(TAG, entry.toString())
            updStm.bindLong(1, entry.id)
            updStm.bindLong(2, entry.points!!.toLong())
            assert(updStm.executeInsert() > 0)
            val values = ContentValues()
            values.put(KEY_ENTRY, entry.id)
            values.put(KEY_LAST_USED, System.currentTimeMillis())
            assert(db!!.replace(TBL_ENTRIES_USED,null,values) > -1)
        }
    }

    /**
     * Starts a new session based on the table entries<br></br>
     * Overriding any old session data!
     *
     * @param lists The VList to use for this sessions
     * @return true on success
     */
    fun createSession(lists: Collection<VList>) {
        Log.d(TAG, "entry createSession")
        db!!.beginTransaction()
        try {
            db!!.compileStatement("INSERT INTO $TBL_SESSION_LISTS ($KEY_LIST) VALUES (?)").use { insStm ->
                for (tbl in lists) {
                    insStm.clearBindings()
                    insStm.bindLong(1, tbl.id.toLong())
                    if (insStm.executeInsert() < 0) {
                        Log.wtf(TAG, "no new table inserted")
                    }
                }
                db!!.setTransactionSuccessful()
                Log.d(TAG, "exit createSession")
            }
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
                db!!.rawQuery("SELECT ses.$KEY_LIST tbl,$KEY_NAME_A,$KEY_NAME_B,$KEY_NAME_LIST,$KEY_CREATED,$KEY_CHANGED "
                        + "FROM $TBL_SESSION_LISTS ses JOIN $TBL_LISTS tbls ON tbls.$KEY_LIST == ses.$KEY_LIST", null).use { cursor ->
                    while (cursor.moveToNext()) {
                        lst.add(
                            VList(
                                _id = cursor.getLong(0),
                                _nameA = cursor.getString(1),
                                _nameB = cursor.getString(2),
                                _name = cursor.getString(3),
                                created = Date(cursor.getLong(4)),
                                changed = Date(cursor.getLong(5)),
                                uuid = null,
                            )
                        )
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
                db!!.rawQuery("SELECT COUNT(*) FROM $TBL_SESSION_LISTS WHERE 1", null).use { cursor ->
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
        require(lists.isNotEmpty())
        unfinishedLists.clear()
        for (list in lists) {
            db!!.rawQuery("SELECT COUNT(*) FROM $TBL_ENTRIES WHERE $KEY_LIST  = ?", arrayOf(list.id.toString())).use { curLeng ->
                db!!.rawQuery("SELECT COUNT(*) FROM $TBL_SESSION s "
                    +"JOIN $TBL_ENTRIES e ON e.$KEY_ENTRY = s.$KEY_ENTRY "
                    +"WHERE $KEY_LIST  = ? AND $KEY_POINTS >= ?", arrayOf(list.id.toString(), settings.timesToSolve.toString())).use { curFinished ->
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
        }
        return true
    }

    /**
     * Returns a random entry from the specified table, which matches the trainer settings<br></br>
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
        if (lastEntry != null && lastEntry.list!!.id == list.id && !allowRepetition) lastID = lastEntry.id
        val arg = arrayOf(list.id.toString(), lastID.toString(), ts.timesToSolve.toString())
        Log.v(TAG, arg.contentToString())
        db!!.rawQuery(
                "SELECT $KEY_TIP, $KEY_ADDITION, tbl.$KEY_CREATED,"
                        + "tbl.$KEY_ENTRY, $KEY_POINTS, tbl.$KEY_CHANGED"
                        + "FROM $TBL_ENTRIES tbl "
                        + "LEFT JOIN  $TBL_SESSION ses ON tbl.$KEY_ENTRY = ses.$KEY_ENTRY "
                        + "WHERE tbl.$KEY_LIST = ? AND tbl.$KEY_ENTRY != ? AND ( $KEY_POINTS IS NULL OR $KEY_POINTS < ? ) "
                        + "ORDER BY RANDOM() LIMIT 1", arg).use { cV ->
            return if (cV.moveToNext()) {
                val meaningA: MutableList<String> = ArrayList()
                val meaningB: MutableList<String> = ArrayList()
                val points = if(cV.isNull(4)) { 0 } else cV.getInt(4)
                val vocable = VEntry(
                    meaningA = meaningA,
                    meaningB = meaningB,
                    _tip = cV.getString(0),
                    _addition = cV.getString(1),
                    id = cV.getLong(3),
                    list = list,
                    _points = points,
                    last_used = null,
                    created = Date(cV.getLong(2)),
                    uuid = null, // leave empty
                    changed = Date(cV.getLong(5))
                )
                getVocableMeanings(TBL_WORDS_A, vocable, meaningA)
                getVocableMeanings(TBL_WORDS_B, vocable, meaningB)
                vocable
            } else {
                Log.w(TAG, "no entries found!")
                null
            }
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

    internal inner class internalDB(context: Context?) : SQLiteOpenHelper(context, DB_NAME_PRODUCTION, null, Companion.DATABASE_VERSION) {
        private val sqlLists = ("CREATE TABLE " + TBL_LISTS + " ("
                + KEY_NAME_LIST + " TEXT NOT NULL,"
                + KEY_LIST + " INTEGER PRIMARY KEY,"
                + KEY_NAME_A + " TEXT NOT NULL,"
                + KEY_NAME_B + " TEXT NOT NULL,"
                + KEY_CREATED + " INTEGER NOT NULL,"
                + KEY_CHANGED + " INTEGER NOT NULL )")
        private val sqlListsIndex = ("CREATE INDEX listChangedI ON $TBL_LISTS ($KEY_CHANGED)")
        private val sqlListSync = ("CREATE TABLE "+ TBL_LIST_SYNC + " ("
                + KEY_LIST + " INTEGER PRIMARY KEY REFERENCES $TBL_LISTS($KEY_LIST) ON DELETE CASCADE,"
                + KEY_LIST_UUID + " STRING NOT NULL UNIQUE )")
        private val sqlEntries = ("CREATE TABLE " + TBL_ENTRIES + " ("
                + KEY_LIST + " INTEGER REFERENCES $TBL_LISTS($KEY_LIST) ON DELETE CASCADE, "
                + KEY_ENTRY + " INTEGER PRIMARY KEY,"
                + KEY_TIP + " TEXT,"
                + KEY_ADDITION + "TEXT,"
                + KEY_CREATED + " INTEGER NOT NULL,"
                + KEY_CHANGED + " INTEGER NOT NULL )")
        private val sqlEntryIndex = ("CREATE INDEX entryChangedI ON $TBL_ENTRIES ($KEY_CHANGED)")
        private val sqlEntrySync = ("CREATE TABLE "+ TBL_ENTRY_SYNC + " ("
                + KEY_ENTRY + " INTEGER PRIMARY KEY REFERENCES $TBL_ENTRIES($KEY_ENTRY) ON DELETE CASCADE, "
                + KEY_ENTRY_UUID + " STRING NOT NULL UNIQUE )")
        private val sqlEntryUsed = ("CREATE TABLE "+TBL_ENTRIES_USED + "("
                + KEY_ENTRY + " INTEGER PRIMARY KEY REFERENCES $TBL_ENTRIES($KEY_ENTRY) ON DELETE CASCADE, "
                + KEY_LAST_USED + " INTEGER NOT NULL )")
        private val sqlEntryUsedIndex = ("CREATE INDEX entryUsedI ON $TBL_ENTRIES_USED ($KEY_LAST_USED)")
        private val sqlWordsA = ("CREATE TABLE " + TBL_WORDS_A + " ("
                + KEY_ENTRY + " INTEGER NOT NULL REFERENCES $TBL_ENTRIES($KEY_ENTRY) ON DELETE CASCADE,"
                + KEY_MEANING + " TEXT NOT NULL )")
        private val sqlWordsB = ("CREATE TABLE " + TBL_WORDS_B + " ("
                + KEY_ENTRY + " INTEGER NOT NULL REFERENCES $TBL_ENTRIES($KEY_ENTRY) ON DELETE CASCADE,"
                + KEY_MEANING + " TEXT NOT NULL )")
        private val sqlWordsAIndex = ("CREATE INDEX wordsAI ON $TBL_WORDS_A ($KEY_ENTRY)")
        private val sqlWordsBIndex = ("CREATE INDEX wordsBI ON $TBL_WORDS_B ($KEY_ENTRY)")
        private val sqlSession = ("CREATE TABLE " + TBL_SESSION + " ("
                + KEY_ENTRY + " INTEGER PRIMARY KEY REFERENCES $TBL_ENTRIES($KEY_ENTRY) ON DELETE CASCADE,"
                + KEY_POINTS + " INTEGER NOT NULL )")
        private val sqlSessionMeta = ("CREATE TABLE " + TBL_SESSION_META + " ("
                + KEY_MKEY + " TEXT NOT NULL PRIMARY KEY,"
                + KEY_MVALUE + " TEXT NOT NULL )") // TODO: replace ?, previously combined primary ?!
        private val sqlSessionLists = ("CREATE TABLE " + TBL_SESSION_LISTS + " ("
                + KEY_LIST + " INTEGER PRIMARY KEY REFERENCES $TBL_LISTS($KEY_LIST) ON DELETE CASCADE )")
        private val sqlSessionEntries = ("CREATE TABLE " + TBL_SESSION_ENTRIES + " ("
                + KEY_ENTRY + " INTEGER PRIMARY KEY REFERENCES $TBL_ENTRIES($KEY_ENTRY) ON DELETE CASCADE )")
        private val sqlListsDeleted = ("CREATE TABLE " + TBL_LISTS_DELETED + " ("
                + KEY_LIST_UUID + " text NOT NULL PRIMARY KEY )")
        private val sqlEntriesDeleted = ("CREATE TABLE " + TBL_ENTRIES_DELETED + " ("
                + KEY_ENTRY_UUID + " text NOT NULL PRIMARY KEY )")
        private val sqlEntryStats = ("CREATE TABLE " + TBL_ENTRY_STATS + " ("
                + KEY_ENTRY + " INTEGER REFERENCES $TBL_ENTRIES($KEY_ENTRY) ON DELETE CASCADE,"
                + KEY_DATE + "INTEGER PRIMARY KEY,"
                + KEY_TIP_NEEDED + "boolean NOT NULL,"
                + KEY_IS_CORRECT + "boolean NOT NULL )")
        private val sqlListCategories = ("CREATE TABLE " + TBL_LIST_CATEGORIES + " ("
                + KEY_LIST + " INTEGER REFERENCES $TBL_LISTS($KEY_LIST) ON DELETE CASCADE,"
                + KEY_CATEGORY + " INTEGER REFERENCES $TBL_CATEGORY($KEY_CATEGORY) ON DELETE CASCADE,"
                + "PRIMARY KEY ($KEY_LIST ,$KEY_CATEGORY ))")
        private val sqlCategory = ("CREATE TABLE "+ TBL_CATEGORY + " ("
                + KEY_CATEGORY + " INTEGER PRIMARY KEY,"
                + KEY_CATEGORY_NAME + " STRING NOT NULL,"
                + KEY_CATEGORY_UUID + " STRING NOT NULL UNIQUE,"
                + KEY_CHANGED + " INTEGER NOT NULL )")
        private val sqlCategoryIndex = ("CREATE INDEX categoryChangedI ON $TBL_CATEGORY ($KEY_CHANGED)")
        private val sqlCategoriesDeleted = ("CREATE TABLE " + TBL_CATEGORIES_DELETED + " ("
                + KEY_CATEGORY_UUID + " text NOT NULL PRIMARY KEY )")

        override fun onOpen(db: SQLiteDatabase?) {
            super.onOpen(db)
            db?.run {
                if (!this.isReadOnly) {
                    this.setForeignKeyConstraintsEnabled(true)
                }
            }

        }

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
            checkIllegalIDs(db, KEY_ENTRY, TBL_ENTRIES)
            checkIllegalIDs(db, KEY_LIST, TBL_ENTRIES)
            checkIllegalIDs(db, KEY_LIST, TBL_LISTS)
            checkIllegalIDs(db, KEY_ENTRY, TBL_WORDS_A)
            checkIllegalIDs(db, KEY_LIST, TBL_WORDS_A)
            checkIllegalIDs(db, KEY_ENTRY, TBL_WORDS_B)
            checkIllegalIDs(db, KEY_LIST, TBL_WORDS_B)
            Log.d(TAG, "check passed")
        }

        override fun onCreate(db: SQLiteDatabase) {
            Log.d(TAG, "creating db")
            val tables = arrayOf(
                sqlLists,
                sqlListsIndex,
                sqlListSync,
                sqlEntries,
                sqlEntryIndex,
                sqlEntrySync,
                sqlWordsA,
                sqlWordsAIndex,
                sqlWordsB,
                sqlWordsBIndex,
                sqlSession,
                sqlSessionMeta,
                sqlSessionLists,
                sqlSessionEntries,
                sqlListsDeleted,
                sqlEntriesDeleted,
                sqlEntryStats,
                sqlListCategories,
                sqlCategory,
                sqlCategoryIndex,
                sqlCategoriesDeleted,
                sqlEntryUsed,
                sqlEntryUsedIndex
            )
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
            val start = System.currentTimeMillis()
            if (oldVersion < 2) {
                upgrade1to2(db)
            }
            if (oldVersion < 3) {
                upgrade2to3(db)
            }
            val duration = System.currentTimeMillis() - start
            Log.v(TAG, "upgrade end in $duration ms")
        }

        fun upgrade2to3(db: SQLiteDatabase) {
            val newTables = arrayOf(
                sqlLists,
                sqlListsIndex,
                sqlListSync,
                sqlEntries,
                sqlEntryIndex,
                sqlEntrySync,
                sqlWordsA,
                sqlWordsAIndex,
                sqlWordsB,
                sqlWordsBIndex,
                sqlSession,
                sqlSessionLists,
                sqlSessionEntries,
                sqlListsDeleted,
                sqlEntriesDeleted,
                sqlEntryStats,
                sqlListCategories,
                sqlCategory,
                sqlCategoryIndex,
                sqlCategoriesDeleted,
                sqlEntryUsed,
                sqlEntryUsedIndex
            )
            for (sql in newTables) db.execSQL(sql)
            val time = System.currentTimeMillis().toString()
            // transfer lists, keep old refs for new auto id mapping
            db.execSQL("ALTER TABLE $TBL_LISTS ADD COLUMN $KEY_TABLE INTEGER")
            run {
                val argsInto = arrayOf(
                    KEY_NAME_LIST,
                    KEY_TABLE,
                    KEY_NAME_A,
                    KEY_NAME_B,
                    KEY_CREATED,
                    KEY_CHANGED
                ).joinToString(separator = ",")
                val argsFrom = arrayOf(
                    KEY_NAME_TBL,
                    KEY_TABLE,
                    KEY_NAME_A,
                    KEY_NAME_B,
                    KEY_CREATED
                ).joinToString(separator = ",")
                val sqlCpy =
                    "INSERT INTO $TBL_LISTS ($argsInto) SELECT $argsFrom,$time FROM $TBL_TABLES_V2"
                db.execSQL(sqlCpy)
            }

            // add legacy mapping for entry foreign tables
            db.execSQL("ALTER TABLE $TBL_ENTRIES ADD COLUMN $KEY_TABLE INTEGER")
            db.execSQL("ALTER TABLE $TBL_ENTRIES ADD COLUMN $KEY_VOC INTEGER")
            run {
                // transfer entries using legacy ID mapping
                val argsInto = arrayOf(
                    KEY_LIST,
                    KEY_TABLE,
                    KEY_VOC,
                    KEY_TIP,
                    KEY_ADDITION,
                    KEY_LAST_USED,
                    KEY_CREATED,
                    KEY_CHANGED
                ).joinToString(separator = ",")
                val argsFrom = arrayOf(
                    "oldEntr.$KEY_TABLE",
                    KEY_VOC,
                    KEY_TIP,
                    KEY_ADDITION,
                    KEY_LAST_USED,
                    "oldEntr.$KEY_CREATED"
                ).joinToString(separator = ",")
                val sqlCpy =
                    "INSERT INTO $TBL_ENTRIES ($argsInto) SELECT lists.$KEY_LIST,$argsFrom,$time FROM $TBL_VOCABLE_V2 oldEntr JOIN $TBL_LISTS lists ON lists.$KEY_TABLE = oldEntr.$KEY_TABLE"
                db.execSQL(sqlCpy)
            }
            run {
                // now use the legacy mapping for entry based stuff
                val sqlMA = "INSERT INTO $TBL_WORDS_A ($KEY_ENTRY,$KEY_MEANING) SELECT entries.$KEY_ENTRY,$KEY_MEANING FROM $TBL_MEANING_A_V2 oldM JOIN $TBL_ENTRIES entries ON entries.$KEY_TABLE = oldM.$KEY_TABLE AND entries.$KEY_VOC = oldM.$KEY_VOC"
                Log.v(TAG, "sql: $sqlMA")
                db.execSQL(sqlMA)
                val sqlMB = sqlMA.replace(TBL_MEANING_A_V2, TBL_MEANING_B_V2).replace(TBL_WORDS_A,
                    TBL_WORDS_B)
                db.execSQL(sqlMB)
            }
            // transfer session entry points
            db.execSQL("INSERT INTO $TBL_SESSION ($KEY_ENTRY,$KEY_POINTS) SELECT entries.$KEY_ENTRY,$KEY_POINTS FROM $TBL_SESSION_V2 oldSes JOIN $TBL_ENTRIES entries ON entries.$KEY_TABLE = oldSes.$KEY_TABLE AND entries.$KEY_VOC = oldSes.$KEY_VOC")
            // transfer session tables used
            db.execSQL("INSERT INTO $TBL_SESSION_LISTS ($KEY_LIST) SELECT lists.$KEY_LIST FROM $TBL_SESSION_TABLES oldSes JOIN $TBL_LISTS lists ON lists.$KEY_TABLE = oldSes.$KEY_TABLE")
            // transfer single session entries used (shouldn't be in use at this point)
            db.execSQL("INSERT INTO $TBL_SESSION_ENTRIES ($KEY_ENTRY) SELECT entries.$KEY_ENTRY FROM $TBL_SESSION_VOC oldSesE JOIN $TBL_ENTRIES entries ON entries.$KEY_TABLE = oldSesE.$KEY_TABLE AND entries.$KEY_VOC = oldSesE.$KEY_VOC")

            // drop old ID system
            //db.execSQL("ALTER TABLE $TBL_ENTRIES DROP COLUMN $KEY_TABLE")
            //db.execSQL("ALTER TABLE $TBL_ENTRIES DROP COLUMN $KEY_VOC")
            dropColumns(db, TBL_ENTRIES.replace("`",""), listOf(KEY_TABLE.replace("`",""),KEY_VOC.replace("`","")))
            //db.execSQL("ALTER TABLE $TBL_LISTS DROP COLUMN $KEY_TABLE")
            dropColumns(db, TBL_LISTS.replace("`",""), listOf(KEY_TABLE.replace("`","")))

            // drop old tables
            db.execSQL("DROP TABLE $TBL_TABLES_V2")
            db.execSQL("DROP TABLE $TBL_VOCABLE_V2")
            db.execSQL("DROP TABLE $TBL_MEANING_A_V2")
            db.execSQL("DROP TABLE $TBL_MEANING_B_V2")
            db.execSQL("DROP TABLE $TBL_SESSION_V2")
            db.execSQL("DROP TABLE $TBL_SESSION_TABLES")
            db.execSQL("DROP TABLE $TBL_SESSION_VOC")
        }

        fun upgrade1to2(db: SQLiteDatabase) {
            val sqlOld_a = ("CREATE TABLE " + TBL_TABLES_V2 + " ("
                    + KEY_NAME_TBL + " TEXT NOT NULL,"
                    + KEY_TABLE + " INTEGER PRIMARY KEY,"
                    + KEY_NAME_A + " TEXT NOT NULL,"
                    + KEY_NAME_B + " TEXT NOT NULL,"
                    + KEY_CREATED + " INTEGER NOT NULL )")
            val sqlOld_b = ("CREATE TABLE " + TBL_VOCABLE_V2 + " ("
                    + KEY_TABLE + " INTEGER NOT NULL, "
                    + KEY_VOC + " INTEGER PRIMARY KEY,"
                    + KEY_TIP + " TEXT,"
                    + KEY_ADDITION + "TEXT,"
                    + KEY_LAST_USED + " INTEGER,"
                    + KEY_CREATED + " INTEGER NOT NULL,"
                    + KEY_CORRECT + " INTEGER NOT NULL,"
                    + KEY_WRONG + " INTEGER NOT NULL )")
            val sqlOld_c = ("CREATE TABLE " + TBL_MEANING_A_V2 + " ("
                    + KEY_TABLE + " INTEGER NOT NULL,"
                    + KEY_VOC + " INTEGER NOT NULL,"
                    + KEY_MEANING + " TEXT NOT NULL )")
            val sqlOld_d = ("CREATE TABLE " + TBL_MEANING_B_V2 + " ("
                    + KEY_TABLE + " INTEGER NOT NULL,"
                    + KEY_VOC + " INTEGER NOT NULL,"
                    + KEY_MEANING + " TEXT NOT NULL )")
            val sqlOld_e = ("CREATE INDEX primA ON " + TBL_MEANING_A_V2
                    + "( " + KEY_TABLE + "," + KEY_VOC + " )")
            val sqlOld_f = ("CREATE INDEX primB ON " + TBL_MEANING_B_V2
                    + "( " + KEY_TABLE + "," + KEY_VOC + " )")
            // already correct in v1
//            val sqlOld_g = ("CREATE TABLE " + TBL_SESSION_V2 + " ("
//                    + KEY_TABLE + " INTEGER NOT NULL,"
//                    + KEY_VOC + " INTEGER NOT NULL,"
//                    + KEY_POINTS + " INTEGER NOT NULL,"
//                    + "PRIMARY KEY (" + KEY_LIST + "," + KEY_ENTRY + "))")
//            val sqlOld_h = ("CREATE TABLE " + TBL_SESSION_META + " ("
//                    + KEY_MKEY + " TEXT NOT NULL,"
//                    + KEY_MVALUE + " TEXT NOT NULL,"
//                    + "PRIMARY KEY (" + KEY_MKEY + "," + KEY_MVALUE + "))")
//            val sqlOld_i = ("CREATE TABLE " + TBL_SESSION_TABLES + " ("
//                    + KEY_TABLE + " INTEGER PRIMARY KEY )")
            val sqlOld_j = ("CREATE TABLE " + TBL_SESSION_VOC + " ("
                    + KEY_TABLE + " INTEGER NOT NULL,"
                    + KEY_VOC + " INTEGER NOT NULL,"
                    + "PRIMARY KEY (" + KEY_TABLE + "," + KEY_VOC + "))")
            val newTables = arrayOf(sqlOld_a, sqlOld_b, sqlOld_c, sqlOld_d, sqlOld_e, sqlOld_f, sqlOld_j)
            for (sql in newTables) db.execSQL(sql)
            val time = System.currentTimeMillis().toString()
            run {
                db.execSQL("ALTER TABLE $TBL_VOCABLE_V1 ADD COLUMN $KEY_ADDITION TEXT")
                db.execSQL("ALTER TABLE $TBL_VOCABLE_V1 ADD COLUMN $KEY_CORRECT INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE $TBL_VOCABLE_V1 ADD COLUMN $KEY_WRONG INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE $TBL_VOCABLE_V1 ADD COLUMN $KEY_CREATED INTEGER NOT NULL DEFAULT $time")
                val args = arrayOf(KEY_TABLE, KEY_VOC, KEY_TIP, KEY_ADDITION, KEY_LAST_USED,
                    KEY_CREATED, KEY_CORRECT, KEY_WRONG).joinToString(separator = ",")
                val sqlCpy = "INSERT INTO $TBL_VOCABLE_V2 ($args) SELECT $args FROM $TBL_VOCABLE_V1"
                db.execSQL(sqlCpy)
                val colMA = arrayOf(KEY_TABLE, KEY_VOC, KEY_MEANING).joinToString(separator = ",")
                val selMA = arrayOf(KEY_TABLE, KEY_VOC, KEY_WORD_A).joinToString(separator = ",")
                val sqlMeaningA = "INSERT INTO $TBL_MEANING_A_V2 ($colMA) SELECT $selMA FROM $TBL_VOCABLE_V1"
                db.execSQL(sqlMeaningA)
                val sqlMeaningB = sqlMeaningA.replace(KEY_WORD_A.toRegex(), KEY_WORD_B)
                    .replace(TBL_MEANING_A_V2.toRegex(), TBL_MEANING_B_V2)
                db.execSQL(sqlMeaningB)
                db.execSQL("DROP TABLE $TBL_VOCABLE_V1")
            }
            run {
                db.execSQL("ALTER TABLE $TBL_TABLES_V1 ADD COLUMN $KEY_CREATED INTEGER NOT NULL DEFAULT $time")
                val args = arrayOf(KEY_NAME_TBL, KEY_TABLE, KEY_NAME_A, KEY_NAME_B, KEY_CREATED).joinToString(separator = ",")
                val sqlCpy = "INSERT INTO $TBL_TABLES_V2 ($args) SELECT $args FROM $TBL_TABLES_V1"
                db.execSQL(sqlCpy)
                db.execSQL("DROP TABLE $TBL_TABLES_V1")
            }
            run { checkForIllegalIds(db) }
        }

        // adopted from https://stackoverflow.com/a/51587449/3332686
        // we don't have DROP COLUMN support until sqlite 3.35.0 from 2021-03-12, so never in android terms
        // no transaction handling, left to caller
        fun dropColumns(
            database: SQLiteDatabase,
            tableName: String,
            columnsToRemove: Collection<String?>
        ) {
            val columnNames: MutableList<String?> = ArrayList()
            val columnNamesWithType: MutableList<String?> = ArrayList()
            val primaryKeys: MutableList<String?> = ArrayList()
            val query = "pragma table_info($tableName);"
            val cursor = database.rawQuery(query, null)
            while (cursor.moveToNext()) {
                val columnName = cursor.getString(cursor.getColumnIndex("name"))
                if (columnsToRemove.contains(columnName)) {
                    continue
                }
                val columnType = cursor.getString(cursor.getColumnIndex("type"))
                val isNotNull = cursor.getInt(cursor.getColumnIndex("notnull")) == 1
                val isPk = cursor.getInt(cursor.getColumnIndex("pk")) == 1
                columnNames.add(columnName)
                var tmp = "`$columnName` $columnType "
                if (isNotNull) {
                    tmp += " NOT NULL "
                }
                val defaultValueType = cursor.getType(cursor.getColumnIndex("dflt_value"))
                if (defaultValueType == Cursor.FIELD_TYPE_STRING) {
                    tmp += " DEFAULT " + "\"" + cursor.getString(cursor.getColumnIndex("dflt_value")) + "\" "
                } else if (defaultValueType == Cursor.FIELD_TYPE_INTEGER) {
                    tmp += " DEFAULT " + cursor.getInt(cursor.getColumnIndex("dflt_value")) + " "
                } else if (defaultValueType == Cursor.FIELD_TYPE_FLOAT) {
                    tmp += " DEFAULT " + cursor.getFloat(cursor.getColumnIndex("dflt_value")) + " "
                }
                columnNamesWithType.add(tmp)
                if (isPk) {
                    primaryKeys.add("`$columnName`")
                }
            }
            cursor.close()
            val columnNamesSeparated = TextUtils.join(", ", columnNames)
            if (primaryKeys.size > 0) {
                columnNamesWithType.add("PRIMARY KEY(" + TextUtils.join(", ", primaryKeys) + ")")
            }
            val columnNamesWithTypeSeparated = TextUtils.join(", ", columnNamesWithType)
            database.execSQL("PRAGMA foreign_keys=OFF")
            database.execSQL("ALTER TABLE " + tableName + " RENAME TO " + tableName + "_old;")
            database.execSQL("CREATE TABLE $tableName ($columnNamesWithTypeSeparated);")
            database.execSQL(
                "INSERT INTO " + tableName + " (" + columnNamesSeparated + ") SELECT "
                        + columnNamesSeparated + " FROM " + tableName + "_old;"
            )
            database.execSQL("DROP TABLE " + tableName + "_old;")
            database.execSQL("PRAGMA foreign_keys=ON")
        }
    }

    companion object {
        fun uuid(): UUID = UUID.randomUUID()
        fun parseUUID(input: String?): UUID? {
            return input?.let {
                UUID.fromString(it)
            }
        }
        fun uuidToString(input: UUID?): String? = input?.toString()

        private const val TAG = "Database"
        const val DB_NAME_PRODUCTION = "voc.db"
        const val MIN_ID_TRESHOLD = 0L
        const val ID_RESERVED_SKIP = -2L
        private const val TBL_LISTS = "`lists`"
        private const val TBL_LIST_SYNC = "`list_sync`"
        private const val TBL_ENTRIES = "`entries`"
        private const val TBL_ENTRY_SYNC = "`entry_sync`"
        private const val TBL_SESSION = "`session2`"
        private const val TBL_SESSION_META = "`session_meta`"
        private const val TBL_SESSION_LISTS = "`session_lists`"
        private const val TBL_WORDS_A = "`words_a`"
        private const val TBL_WORDS_B = "`words_b`"
        private const val TBL_SESSION_ENTRIES = "`session_entries`"
        private const val TBL_LISTS_DELETED = "`lists_deleted`"
        private const val TBL_ENTRIES_DELETED = "`entries_deleted`"
        private const val TBL_ENTRY_STATS = "`entry_stats`"
        private const val TBL_LIST_CATEGORIES = "`categories`"
        private const val TBL_CATEGORY = "`category_name`"
        private const val TBL_CATEGORIES_DELETED = "`categories_deleted`"
        private const val TBL_ENTRIES_USED = "`entries_used`"
        private const val KEY_ENTRY = "`entry`"
        private const val KEY_NAME_A = "`name_a`"
        private const val KEY_NAME_B = "`name_b`"
        private const val KEY_TIP = "`tip`"
        private const val KEY_TIP_NEEDED = "`tip_needed`"
        private const val KEY_LIST = "`list`"
        private const val KEY_LAST_USED = "`last_used`"
        private const val KEY_NAME_LIST = "`list_name`"
        private const val KEY_MEANING = "`meaning`"
        private const val KEY_CREATED = "`created`"
        private const val KEY_IS_CORRECT = "`is_correct`"
        @Deprecated("Deprecated DB version")
        private val KEY_CORRECT = "`correct`"
        @Deprecated("Deprecated DB version")
        private val KEY_WRONG = "`wrong`"
        private const val KEY_ADDITION = "`addition`"
        private const val KEY_POINTS = "`points`"
        private const val KEY_MKEY = "`key`"
        private const val KEY_MVALUE = "`value`"
        private const val KEY_LIST_UUID = "`uuid_list`"
        private const val KEY_ENTRY_UUID = "`uuid_voc`"
        private const val KEY_CHANGED = "`changed`"
        private const val KEY_DATE = "`date`"
        private const val KEY_CATEGORY = "`category`"
        private const val KEY_CATEGORY_NAME = "`name`"
        private const val KEY_CATEGORY_UUID = "`uuid_cat`"
        private var dbIntern: SQLiteDatabase? = null // DB to internal file, 99% of the time used

        private const val DATABASE_VERSION = 3

        @Deprecated("Deprecated DB version")
        private val TBL_VOCABLE_V1 = "`vocables`"

        @Deprecated("Deprecated DB version")
        private val TBL_TABLES_V1 = "`voc_tables`"

        @Deprecated("Deprecated DB version")
        private val KEY_WORD_A = "`word_a`"

        @Deprecated("Deprecated DB version")
        private val KEY_WORD_B = "`word_b`"

        @Deprecated("Deprecated DB version")
        private val TBL_VOCABLE_V2 = "`vocables2`"

        @Deprecated("Deprecated DB version")
        private val TBL_TABLES_V2 = "`voc_tables2`"

        @Deprecated("Deprecated DB version")
        private val TBL_MEANING_A_V2 = "`meaning_a`"
        @Deprecated("Deprecated DB version")
        private val TBL_MEANING_B_V2 = "`meaning_b`"
        @Deprecated("Deprecated DB version")
        private val TBL_SESSION_TABLES = "`session_tables`"
        @Deprecated("Deprecated DB version")
        private val TBL_SESSION_VOC = "`session_voc`"
        @Deprecated("Deprecated DB version")
        private val TBL_SESSION_V2 = "`session`"
        @Deprecated("Deprecated DB version")
        private val KEY_NAME_TBL = "`name`"
        @Deprecated("Deprecated DB version")
        private val KEY_TABLE = "`table`"
        @Deprecated("Deprecated DB version")
        private val KEY_VOC = "`voc`"
    }
}