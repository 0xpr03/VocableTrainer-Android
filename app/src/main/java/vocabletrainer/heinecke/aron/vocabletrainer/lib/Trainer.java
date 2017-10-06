package vocabletrainer.heinecke.aron.vocabletrainer.lib;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Entry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Table;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings;

/**
 * Trainer class
 */
public class Trainer {
    private final static String TAG = "Trainer";
    private Random rng;
    private AB_MODE order;
    private List<Table> tables;
    private List<Table> unsolvedTables;
    private Entry cVocable = null;
    private int tips;
    private int total;
    private int unsolved;
    private int failed;
    private int timesToSolve;
    private boolean showedSolution;
    private Database db;
    private TrainerSettings settings;
    private SessionStorageManager ssm;
    private boolean firstTimeVocLoad;

    /**
     * Creates a new Trainer
     *
     * @param tables     Vocable tables to use
     * @param settings   Trainer settings storage
     * @param newSession whether this is a new or a continued session
     */
    public Trainer(final ArrayList<Table> tables, final TrainerSettings settings, final Context context, final boolean newSession,
                   final SessionStorageManager ssm) {
        if (tables == null || tables.size() == 0 || settings == null)
            throw new IllegalArgumentException();
        this.settings = settings;
        this.tips = settings.tipsGiven;
        this.failed = settings.timesFailed;
        this.tables = tables;
        this.timesToSolve = settings.timesToSolve;
        this.unsolvedTables = new ArrayList<>();
        this.ssm = ssm;
        db = new Database(context);
        rng = new Random();

        if (newSession) {
            wipeSession();
        }

        firstTimeVocLoad = settings.questioning != null; // load voc from settings.questioning
        getTableData();
        prepareData();
        this.getNext();
    }

    /**
     * Save vocable state<br>
     *  saves last vocable in question for later continue
     */
    public void saveVocState(){
        if(!ssm.saveLastVoc(cVocable)){
            Log.w(TAG,"unable to save vocable");
        }
    }

    /**
     * Wipe DB from (previous) session
     *
     * @return true on success
     */
    private boolean wipeSession() {
        return db.wipeSessionPoints();
    }

    /**
     * Calculate totals etc
     */
    private void prepareData() {
        total = 0;
        unsolved = 0;
        for (Table tbl : tables) {
            total += tbl.getTotalVocs();
            unsolved += tbl.getUnfinishedVocs();
        }
    }

    /**
     * Retreive information from DB
     */
    private void getTableData() {
        db.getSessionTableData(tables, unsolvedTables, settings);
    }

    /**
     * Returns the solution of the current vocable
     *
     * @return
     */
    public String getSolution() {
        if (this.cVocable == null) {
            Log.e(TAG, "Null vocable!");
            return "";
        }
        this.failed++;
        showedSolution = true;
        return getSolutionUnchecked();
    }

    /**
     * Returns the solution<br>
     * No null checks are done or failed counter changes are made
     *
     * @return Solution
     */
    private String getSolutionUnchecked() {
        if (order == AB_MODE.A)
            return cVocable.getAWord();
        else
            return cVocable.getBWord();
    }

    /**
     * Check two string for equality, taking case sensitive settings into account
     * @param a
     * @param b
     * @return true if they are equals according to this trainings settings
     */
    private boolean equals(final String a, final String b){
        if(this.settings.caseSensitive){
            return a.equals(b);
        }else{
            return a.equalsIgnoreCase(b);
        }
    }

    /**
     * Checks for correct solution
     *
     * @param tSolution
     * @return true on success
     */
    public boolean checkSolution(final String tSolution) {
        if (this.cVocable == null) {
            Log.e(TAG, "Null vocable!");
            return false;
        }
        boolean bSolved;
        if (bSolved = equals(getSolutionUnchecked(),tSolution)) {
            if (!showedSolution)
                this.cVocable.setPoints(this.cVocable.getPoints() + 1);
            if (cVocable.getPoints() >= timesToSolve) {
                Table tbl = cVocable.getTable();
                tbl.setUnfinishedVocs(tbl.getUnfinishedVocs() - 1);
                if (tbl.getUnfinishedVocs() <= 0) {
                    unsolvedTables.remove(tbl);

                    if (unsolvedTables.size() == 0) {
                        db.deleteSession();
                        Log.d(TAG, "finished");
                    }
                }
            }
            getNext();
        } else {
            this.failed++;
        }
        return bSolved;
    }

    /**
     * Returns true when all vocables are solved as many times as expected
     *
     * @return
     */
    public boolean isFinished() {
        return unsolvedTables.size() == 0;
    }

    /**
     * Update cVocable points
     *
     * @return true on success
     */
    private boolean updateVocable() {
        return db.updateEntryProgress(cVocable);
    }

    /**
     * Get next vocable
     */
    private void getNext() {
        if (cVocable != null) {
            if (!updateVocable()) {
                Log.e(TAG, "unable to update vocable!");
                return;
            }
        }
        showedSolution = false;

        if (unsolvedTables.size() == 0) {
            Log.d(TAG, "no unsolved tables remaining!");
        } else {
            int selected = rng.nextInt(unsolvedTables.size());
            Table tbl = unsolvedTables.get(selected);

            boolean allowRepetition = false;
            if (cVocable != null) {
                if (allowRepetition = unsolvedTables.size() == 1 && unsolvedTables.get(0).getUnfinishedVocs() == 1) {
                    Log.d(TAG, "one left");
                } else if (tbl.getUnfinishedVocs() == 1 && tbl.getId() == cVocable.getTable().getId()) {
                    // handle table has only one entry left
                    // prevent repeating last vocable of table
                    if (selected + 1 >= unsolvedTables.size())
                        selected--;
                    else
                        selected++;
                    Log.d(TAG, "selectionID:" + selected + " max(-1):" + unsolvedTables.size());
                    tbl = unsolvedTables.get(selected);
                }
            }

            if(firstTimeVocLoad){
                firstTimeVocLoad = false;
                cVocable = settings.questioning;
            }else {
                cVocable = db.getRandomTrainerEntry(tbl, cVocable, settings, allowRepetition);
            }
            if (cVocable == null) {
                Log.e(TAG, "New vocable is null!");
            }
            boolean mode = settings.mode == TEST_MODE.A;
            if (settings.mode == TEST_MODE.RANDOM) {
                mode = rng.nextBoolean();
            }
            if (mode) {
                order = AB_MODE.A;
            } else {
                order = AB_MODE.B;
            }
        }
    }

    /**
     * Returns the non-solution column of the vocable<br>
     * returns an empty string when there is no current vocable
     *
     * @return
     */
    public String getQuestion() {
        if (cVocable == null)
            return "";

        return order == AB_MODE.A ? cVocable.getBWord() : cVocable.getAWord();
    }

    /**
     * Returns the tip, increasing the counter<br>
     * returns an empty string when there is no current vocable
     *
     * @return
     */
    public String getTip() {
        if (this.cVocable == null)
            return "";

        this.tips++;
        return cVocable.getTip();
    }

    /**
     * Returns the column name of the question<br>
     * returns an empty string when there is no current vocable
     *
     * @return
     */
    public String getColumnNameExercise() {
        if (this.cVocable == null)
            return "";

        return order == AB_MODE.A ? cVocable.getTable().getNameB() : cVocable.getTable().getNameA();
    }

    /**
     * Returns the column name of the solution<br>
     * returns an empty string when there is no current vocable
     *
     * @return
     */
    public String getColumnNameSolution() {
        if (this.cVocable == null)
            return "";

        return order == AB_MODE.B ? cVocable.getTable().getNameB() : cVocable.getTable().getNameA();
    }

    /**
     * @return remaining vocables
     */
    public int remaining() {
        return unsolved;
    }

    /**
     * @return solved vocables
     */
    public int solved() {
        return total - unsolved;
    }

    /**
     * Testing mode for trainer<br>
     *     Ask column a/b/a&b random
     */
    public enum TEST_MODE {
        A(0), B(1), RANDOM(2);
        private final int id;

        TEST_MODE(int id) {
            this.id = id;
        }

        public static TEST_MODE fromInt(int code) {
            for (TEST_MODE typе : TEST_MODE.values()) {
                if (typе.getValue() == code) {
                    return typе;
                }
            }
            return null;
        }

        public int getValue() {
            return id;
        }
    }

    /**
     * Enum to store the current column that is displayed
     */
    private enum AB_MODE {A, B}
}
