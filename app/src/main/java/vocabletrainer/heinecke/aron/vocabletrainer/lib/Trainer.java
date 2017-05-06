package vocabletrainer.heinecke.aron.vocabletrainer.lib;

/**
 * Created by aron on 07.04.17.
 */

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Table;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Entry;

/**
 * Trainer class
 */
public class Trainer {
    private final static String TAG = "Trainer";

    public enum TEST_MODE {A, B, RANDOM}

    ;
    private Random rng;

    private enum AB_MODE {A, B}

    ;


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

    /**
     * Creates a new Trainer
     *
     * @param tables   Vocable tables to use
     * @param settings Trainer settings storage
     * @param newSession whether this is a new or a continued session
     */
    public Trainer(final ArrayList<Table> tables, final TrainerSettings settings, final Context context, final boolean newSession) {
        if (tables == null || tables.size() == 0 || settings == null)
            throw new IllegalArgumentException();
        this.settings = settings;
        this.tips = settings.tipsGiven;
        this.failed = settings.timesFailed;
        this.tables = tables;
        this.timesToSolve = settings.timesToSolve;
        this.unsolvedTables = new ArrayList<>();
        db = new Database(context);
        rng = new Random();

        if(newSession){
            wipeSession();
        }

        getTableData();
        prepareData();
        this.getNext();
    }

    /**
     * Wipe DB from (previous) session
     * @return true on success
     */
    private boolean wipeSession(){
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
        if (bSolved = getSolutionUnchecked().equals(tSolution)) {
            if(!showedSolution)
                this.cVocable.setPoints(this.cVocable.getPoints() + 1);
            if (cVocable.getPoints() >= timesToSolve) {
                Table tbl = cVocable.getTable();
                tbl.setUnfinishedVocs(tbl.getUnfinishedVocs() - 1);
                if (tbl.getUnfinishedVocs() <= 0) {
                    unsolvedTables.remove(tbl);

                    if (unsolvedTables.size() == 0) {
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
     * @return
     */
    public boolean isFinished(){
        return unsolvedTables.size() == 0;
    }

    /**
     * Update cVocable points
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
            if(!updateVocable())
                return;
        }
        showedSolution = false;

        if (unsolvedTables.size() == 0){
            Log.w(TAG,"no unsolved tables remaining!");
        } else {
            Table tbl = unsolvedTables.get(rng.nextInt(unsolvedTables.size()));

            if(unsolvedTables.size() == 1 && unsolvedTables.get(0).getUnfinishedVocs() == 1) {
                Log.d(TAG,"one left");
            }

            cVocable = db.getRandomTrainerEntry(tbl, cVocable, settings);
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
     * Returns the non-solution column of the vocable
     *
     * @return
     */
    public String getQuestion() {
        if (cVocable == null)
            return "";

        return order == AB_MODE.A ? cVocable.getBWord() : cVocable.getAWord();
    }

    /**
     * Returns the tip, increasing the counter
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
}
