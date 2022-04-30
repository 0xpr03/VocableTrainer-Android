package vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer

import android.database.sqlite.SQLiteStatement
import android.util.Log
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VEntry
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer.Trainer.TEST_MODE

/**
 * Manager for training sessions, loading/saving of settings
 */
class SessionStorageManager
/**
 * Create a new session storage manager
 *
 * @param db Database to use
 */(private val db: Database) {

    /**
     * Delete session when finished, retains history
     */
    fun finishSession() {
        db.deleteSession(false)
    }

    /**
     * Save session settings
     *
     * @param settings
     * @return
     */
    fun saveSession(settings: TrainerSettings?): Boolean {
        if (settings == null) {
            return false
        }
        val w: Writer = Writer(settings, db)
        return w.write()
    }

    /**
     * Load TrainerSettings
     * @return null on failure
     */
    fun loadSession(): TrainerSettings? {
        val l: Loader = Loader(
            db
        )
        return l.load()
    }

    /**
     * Write vocable
     * @param entry
     * @return true on success
     */
    fun saveLastVoc(entry: VEntry?): Boolean {
        val w: Writer = Writer(null, db)
        return w.writeVocable(entry)
    }
    /**
     * Save table selection for session
     * @param lists
     * @return true on success
     */
    //public void saveSessionTbls(Collection<VList> lists){
    //    db.createSession(lists);
    //}
    /**
     * Load table selection of session
     * @return
     */
    /*public ArrayList<VList> loadSessionTbls(){
        return db.getSessionLists();
    }*/
    /**
     * Loader class, handling session loading from db
     */
    private inner class Loader
    /**
     * Creates a new loader<br></br>
     * call load() to actually load a session
     * @param db
     */(private val db: Database) {
        private val stm: SQLiteStatement? = null
        var map: HashMap<String, String>? = null

        /**
         * Does the actual loading
         * @return null on errors
         */
        fun load(): TrainerSettings? {
            map = db.sessionData
            if (map == null) {
                return null
            }
            val failed = getInt(KEY_FAILED)
            val mode = TEST_MODE.fromInt(getInt(KEY_MODE))
            val tips = getInt(KEY_GIVEN_TIPS)
            val timesToSolve = getInt(KEY_SOLVE_TIMES_TO)
            val allowTips = getBoolean(KEY_TIPS_ALLOWED)
            val caseSensitive = getBoolean(KEY_CASE_SENSITIVE)
            val trimSpaces = getBoolean(KEY_TRIM_SPACES)
            val additionAuto = getBoolean(KEY_ADDITION_AUTO)
            var entry: VEntry? = null
            if (map!!.containsKey(KEY_VOCABLE_ID)) {
                val vocID = getLong(KEY_VOCABLE_ID)
                entry = db.getEntry(vocID)
            }
            return TrainerSettings(
                timesToSolve,
                mode,
                allowTips,
                tips,
                failed,
                caseSensitive,
                entry,
                trimSpaces,
                additionAuto
            )
        }

        /**
         * Get int from map
         * @param key
         * @return
         */
        private fun getInt(key: String): Int {
            return map!![key]!!.toInt()
        }

        /**
         * Get long from map
         * @param key
         * @return
         */
        private fun getLong(key: String): Long {
            return map!![key]!!.toLong()
        }

        /**
         * Get boolean from map
         * @param key
         * @return
         */
        private fun getBoolean(key: String): Boolean {
            return java.lang.Boolean.valueOf(map!![key])
        }
    }

    /**
     * Writer class, handling session writing to DB
     */
    private inner class Writer
    /**
     * Creates a new writer<br></br>
     * call write() to actually insert the settings
     *
     * @param settings
     * @param db
     */(var settings: TrainerSettings?, db: Database) {
        var stm: SQLiteStatement? = null

        /**
         * Prepares writing, init statement
         */
        private fun prepareWrite() {
            stm = db.sessionInsertStm
        }

        /**
         * End write
         * @param success commits changes on true
         */
        private fun endWrite(success: Boolean) {
            db.endTransaction(success)
        }

        /**
         * Writes data to DB
         *
         * @return true on success
         */
        fun write(): Boolean {
            Log.d("write", "entry")
            prepareWrite()
            val success = write_()
            endWrite(success)
            return success
        }

        /**
         * Inner writing function, not caring about transactions, stm etc
         * @return true on success
         */
        private fun write_(): Boolean {
            settings?.run {
                if (exec(KEY_MODE, this.mode.value)) return false
                if (exec(KEY_GIVEN_TIPS, this.tipsGiven)) return false
                if (exec(KEY_FAILED, this.timesFailed)) return false
                if (exec(KEY_SOLVE_TIMES_TO, this.timesToSolve)) return false
                if (exec(KEY_TIPS_ALLOWED, this.allowTips)) return false
                if (exec(KEY_CASE_SENSITIVE, this.caseSensitive)) return false
                if (exec(KEY_TRIM_SPACES, this.trimSpaces)) return false
                if (exec(KEY_ADDITION_AUTO, this.additionAuto)) return false
                if (!writeVocable_(this.questioning)) return false
            }
            stm!!.close()
            return true
        }

        /**
         * Write vocable<br></br>
         * public method doing prepare & commit
         * @param entry
         * @return true on success
         */
        fun writeVocable(entry: VEntry?): Boolean {
            prepareWrite()
            val success = writeVocable_(entry)
            endWrite(success)
            return success
        }

        /**
         * Write vocable to session<br></br>
         * internal, does not preapre & commit changes
         * @param entry
         * @return true on success
         */
        private fun writeVocable_(entry: VEntry?): Boolean {
            return if (entry != null) {
                !exec(KEY_VOCABLE_ID, entry.id)
            } else true
        }

        /**
         * @see {exec}
         *
         * @param key
         * @param value
         * @return
         */
        private fun exec(key: String, value: Boolean): Boolean {
            return exec(key, value.toString())
        }

        /**
         * @see {exec}
         *
         * @param key
         * @param value
         * @return
         */
        private fun exec(key: String, value: Int): Boolean {
            return exec(key, value.toString())
        }

        /**
         * @see {exec}
         *
         * @param key
         * @param value
         * @return
         */
        private fun exec(key: String, value: Long): Boolean {
            return exec(key, value.toString())
        }

        /**
         * Execute write for key, value
         *
         * @param key
         * @param value
         * @return false if **no errors** occoured
         */
        private fun exec(key: String, value: String): Boolean {
            Log.d("TrainerSettings", "writing$key $value")
            stm!!.bindString(BIND_KEY, key)
            stm!!.bindString(BIND_VAL, value)
            return if (stm!!.executeInsert() < 0) {
                Log.e("TrainerSettings", "unable to insert key$key")
                true
            } else {
                false
            }
        }
    }

    companion object {
        private const val KEY_SOLVE_TIMES_TO = "t_solve"
        private const val KEY_MODE = "mode"
        private const val KEY_TIPS_ALLOWED = "tips"
        private const val KEY_GIVEN_TIPS = "t_tips_given"
        private const val KEY_FAILED = "t_failed"
        private const val KEY_CASE_SENSITIVE = "t_case_sensitive"
        private const val KEY_VOCABLE_ID = "t_last_voc_id"
        private const val KEY_TRIM_SPACES = "t_trim_spaces"
        private const val KEY_ADDITION_AUTO = "t_addition_auto"
        private const val BIND_KEY = 1
        private const val BIND_VAL = 2

        /**
         * Create new training session, storing the settings and VEntries of the VLists specified
         */
        fun CreateSession(db: Database, settings: TrainerSettings?, lists: Collection<VList>) {
            db.deleteSession()
            val ssm = SessionStorageManager(db)
            ssm.saveSession(settings)
            db.createSession(lists)
        }
    }
}