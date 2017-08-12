package vocabletrainer.heinecke.aron.vocabletrainer.lib;

import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Table;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings;

/**
 * Manager for sessions, loading saving of settings
 */
public class SessionStorageManager {
    private final static String KEY_SOLVE_TIMES_TO = "t_solve";
    private final static String KEY_MODE = "mode";
    private final static String KEY_TIPS_ALLOWED = "tips";
    private final static String KEY_GIVEN_TIPS = "t_tips_given";
    private final static String KEY_FAILED = "t_failed";

    private final static int BIND_KEY = 1;
    private final static int BIND_VAL = 2;

    private Database db;

    /**
     * Create a new session storage manager
     *
     * @param db Database to use
     */
    public SessionStorageManager(Database db) {
        this.db = db;
    }

    /**
     * Save session settings
     *
     * @param settings
     * @return
     */
    public boolean saveSession(TrainerSettings settings) {
        if(settings == null){
            return false;
        }
        Writer w = new Writer(settings, db);
        return w.write();
    }

    /**
     * Load TrainerSettings
     * @return null on failure
     */
    public TrainerSettings loadSession(){
        Loader l = new Loader(db);
        return l.load();
    }

    /**
     * Save table selection for session
     * @param tables
     * @return true on success
     */
    public boolean saveSessionTbls(Collection<Table> tables){
        return db.createSession(tables);
    }

    /**
     * Load table selection of session
     * @return
     */
    public ArrayList<Table> loadSessionTbls(){
        return db.getSessionTables();
    }

    /**
     * Loader class, handling session loading from db
     */
    private class Loader {
        private Database db;
        private SQLiteStatement stm;
        HashMap<String,String> map;

        /**
         * Creates a new loader<br>
         *     call load() to actually load a session
         * @param db
         */
        public Loader(Database db) {
            this.db = db;
        }

        /**
         * Does the actual loading
         * @return null on errors
         */
        public TrainerSettings load() {
            map = db.getSessionData();
            if(map == null){
                return null;
            }
            int failed = getInt(KEY_FAILED);
            Trainer.TEST_MODE mode = Trainer.TEST_MODE.fromInt(getInt(KEY_MODE));
            int tips = getInt(KEY_GIVEN_TIPS);
            int timesToSolve = getInt(KEY_SOLVE_TIMES_TO);
            boolean allowTips = getBoolean(KEY_TIPS_ALLOWED);

            return new TrainerSettings(timesToSolve, mode, allowTips,tips,failed);
        }

        /**
         * Get int from map
         * @param key
         * @return
         */
        private int getInt(String key){
            return Integer.parseInt(map.get(key));
        }

        /**
         * Get boolean from map
         * @param key
         * @return
         */
        private boolean getBoolean(String key){
            return Boolean.valueOf(map.get(key));
        }
    }

    /**
     * Writer class, handling session writing to DB
     */
    private class Writer {
        TrainerSettings settings;
        SQLiteStatement stm;

        /**
         * Creates a new writer<br>
         * call write() to actually insert the settings
         *
         * @param settings
         * @param db
         */
        public Writer(TrainerSettings settings, Database db) {
            this.settings = settings;
        }

        /**
         * Writes data to DB
         *
         * @return true on success
         */
        public boolean write() {
            Log.d("write","entry");
            stm = db.getSessionInsertStm();
            boolean success = write_();
            db.endSessionTransaction(success);
            return success;
        }

        /**
         * Inner writing function, not caring about transactions, stm etc
         * @return true on success
         */
        private boolean write_() {
            if (exec(KEY_MODE, settings.mode.getValue())) return false;
            if (exec(KEY_GIVEN_TIPS, settings.tipsGiven)) return false;
            if (exec(KEY_FAILED, settings.timesFailed)) return false;
            if (exec(KEY_SOLVE_TIMES_TO, settings.timesToSolve)) return false;
            if (exec(KEY_TIPS_ALLOWED,settings.allowTips)) return false;
            stm.close();
            return true;
        }

        /**
         * @see {exec}
         * @param key
         * @param value
         * @return
         */
        private boolean exec(final String key, final boolean value){
            return exec(key, String.valueOf(value));
        }

        /**
         * @see {exec}
         * @param key
         * @param value
         * @return
         */
        private boolean exec(final String key, final int value) {
            return exec(key, String.valueOf(value));
        }

        /**
         * Execute write for key, value
         *
         * @param key
         * @param value
         * @return false if <b>no errors</b> occoured
         */
        private boolean exec(final String key, final String value) {
            Log.d("TrainerSettings","writing"+key+" "+value);
            stm.bindString(BIND_KEY, key);
            stm.bindString(BIND_VAL, value);
            if (stm.executeInsert() < 0) {
                Log.e("TrainerSettings", "unable to insert key" + key);
                return true;
            } else {
                return false;
            }
        }
    }
}
