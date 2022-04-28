package vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;

import static vocabletrainer.heinecke.aron.vocabletrainer.lib.ParcableTools.readParcableBool;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.ParcableTools.writeParcableBool;

/**
 * Trainer class
 */
public class Trainer implements Parcelable {
    private final static String TAG = "Trainer";
    private Random rng;
    private AB_MODE order;
    private VEntry cVocable = null;
    private int tips;
    private long total;
    private long unsolved;
    private int failed;
    private final int timesToSolve;
    private int timesShowedSolution;
    private boolean showedTip;
    private boolean showedSolution;
    private Database db;
    private final TrainerSettings settings;
    private SessionStorageManager ssm;
    private boolean firstTimeVocLoad;
    private String lastAddition;

    /**
     * Creates a new Trainer
     *
     * @param settings   Trainer settings storage
     */
    public Trainer(final TrainerSettings settings, final Context context,
                   final SessionStorageManager ssm) {
        if (settings == null)
            throw new IllegalArgumentException();
        this.settings = settings;
        this.tips = settings.tipsGiven;
        this.failed = settings.timesFailed;
        this.lastAddition = "";
        this.timesToSolve = settings.timesToSolve;
        this.ssm = ssm;
        this.timesShowedSolution = 0;//todo: load from params
        db = new Database(context);
        rng = new Random();

        firstTimeVocLoad = settings.questioning != null; // load voc from settings.questioning
        prepareData();
        this.getNext();
    }

    public static final Creator<Trainer> CREATOR = new Creator<Trainer>() {
        @Override
        public Trainer createFromParcel(Parcel in) {
            return new Trainer(in);
        }

        @Override
        public Trainer[] newArray(int size) {
            return new Trainer[size];
        }
    };

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
     * Calculate totals etc
     */
    private void prepareData() {
        total = db.getSessionTotalEntries();
        unsolved = db.getSessionUnfinishedEntries(timesToSolve);
    }

    /**
     * Returns the solution of the current vocable
     *
     * @return
     */
    public String getSolution() {
        Log.d(TAG,"getSolution");
        timesShowedSolution++;
        showedSolution = true;
        return getSolutionUnchecked();
    }

    public void getSolutions(TrainerInput input){
        timesShowedSolution++;
        showedSolution = true;
        List<String> solutions = getSolutionsUnchecked();
        for(int i = 0; i < solutions.size(); i++){
            input.setInputValue(i, solutions.get(i));
        }
    }

    /**
     * Get addition for current vocable
     * @return
     */
    public String getCurrentAddition() {
        return cVocable.getAddition() == null ? "" : cVocable.getAddition();
    }

    public String getLastAddition() {
        return lastAddition;
    }

    /**
     * Get whether the vocable has an addition or not
     * @return true if addition != ''
     */
    public boolean hasLastAddition() {
        return getLastAddition() != null && !getLastAddition().equals("");
    }

    /**
     * Returns the solution to the current vocable<br>
     *     Does not count it as failed.
     *
     *     <br><br>
     *         not to be confused with getSolutionUnchecked
     * @return Solution
     */
    public String getSolutionUncounted(){
        Log.d(TAG,"getSolutionUncounted");
        if (this.cVocable == null) {
            Log.e(TAG, "Null vocable!");
            return "";
        }
        return getSolutionUnchecked();
    }

    /**
     * Returns all possible solutions<br>
     * No null checks are done or failed counter changes are made
     *
     * @return Solution
     */
    private String getSolutionUnchecked() {
        if (order == AB_MODE.A)
            return cVocable.getAString();
        else
            return cVocable.getBString();
    }

    /**
     * Returns possible solutions
     * @return
     */
    private List<String> getSolutionsUnchecked() {
        if (order == AB_MODE.A)
            return cVocable.getAMeanings();
        else
            return cVocable.getBMeanings();
    }

    /**
     * Check two strings for equality, taking case sensitive & space ignore settings into account
     * @param a
     * @param b
     * @return true if they are equals according to this trainings settings
     */
    private boolean equals(String a, String b){
        if(settings.trimSpaces){
            a = a.trim();
            b = b.trim();
        }
        if(this.settings.caseSensitive){
            return a.equals(b);
        }else{
            return a.equalsIgnoreCase(b);
        }
    }

    /**
     * Check is candidate is one of the possible solutions
     *
     * @param candidate
     * @return solution if candidate is a correct, null otherwise
     * TODO: use an java.util.Optional when api min version is >= 24
     */
    @Nullable
    private String isSolution(String candidate) {
        List<String> solutions = getSolutionsUnchecked();
        if(settings.trimSpaces)
            candidate = candidate.trim();

        for(String solution : solutions){
            if(settings.trimSpaces)
                solution = solution.trim();
            if(this.settings.caseSensitive && solution.equalsIgnoreCase(candidate))
                return solution;
            else if(solution.equals(candidate))
                return solution;
        }
        return null;
    }

    /**
     * Check a multi-meaning input for correctness
     * @param tInput
     * @return true if all is valid
     */
    public boolean checkSolutions(final TrainerInput tInput) {
        Log.d(TAG,"checkSolutions");
        boolean bSolved = true;

        List<String> solutions = tInput.getData();
        HashSet<String> correct = new HashSet<>();
        String solution;
        for(int i = 0; i < solutions.size(); i++) {
            solution = solutions.get(i);
            String result = isSolution(solution);
            if(result != null){
                if(correct.contains(result)){
                    tInput.setInputState(i, TrainerInput.INPUT_STATE.DUPLICATE);
                } else {
                    correct.add(result);
                    tInput.setInputState(i, TrainerInput.INPUT_STATE.VALID);
                }
            } else {
                bSolved = false;
                tInput.setInputState(i, TrainerInput.INPUT_STATE.INVALID);
            }
        }

        if(bSolved){
            bSolved = correct.size() == getSolutionsUnchecked().size();
        }

        if(bSolved) {
            accountVocable(bSolved && !showedSolution);
        } else {
            this.failed++;
        }
        return bSolved;
    }

    /**
     * Returns the amount of solution meanings
     * @return
     */
    public int getAmountSolutionMeanings(){
        return getSolutionsUnchecked().size();
    }

    /**
     * Checks for correct solution <br>
     *     Retrieves next vocable if correct
     *
     * @param tSolution input to be checked against the solution
     * @return true on tSolution is correct
     */
    public boolean checkSolution(final String tSolution) {
        Log.d(TAG,"checkSolution");
        boolean bSolved = isSolution(tSolution) != null;
        if(bSolved) {
            accountVocable(bSolved && !showedSolution);
        }else {
            this.failed++;
        }
        return bSolved;
    }

    /** Accounts vocable as passed based on the parameter.<br>
     *     This function is called from external when checkSolution does not apply.
     * @param correct true when the vocable was answered correct
     */
    private void accountVocable(final boolean correct){
        Log.d(TAG,"accountVocable(correct:"+correct+")");
        if (this.cVocable == null) {
            Log.e(TAG, "Null vocable!");
            return;
        }
        // track every input, even de-duplication entries displayed
        db.insertEntryStat(System.currentTimeMillis(),this.cVocable,showedTip,correct);
        // ignore de-duplication entries, which are already finished
        if (cVocable.getPoints() < timesToSolve) {
            if (correct) {
                this.cVocable.setPoints(this.cVocable.getPoints() + 1);
                db.updateEntryProgress(cVocable); // this is only required if correct for now
                if (cVocable.getPoints() >= timesToSolve) {
                    Log.d(TAG, "finished training entry");
                    this.unsolved = db.getSessionUnfinishedEntries(timesToSolve);
                }
            }
        }
        Log.d(TAG,"getting next");
        getNext();
    }

    /**
     * function for modes where checkSolution doesn't apply<br>
     *     reads next vocable & accounts passed=false as solution showed & failed
     * @param passed vocable answered correctly
     */
    public void updateVocable(final boolean passed){
        if(!passed){
            this.failed++;
            timesShowedSolution++;
        } else {
        }
        accountVocable(passed);
    }

    /**
     * Returns true when all vocables are solved as many times as expected
     *
     * @return
     */
    public boolean isFinished() {
        return db.getSessionUnfinishedEntries(settings.timesToSolve) == 0;
    }

    /**
     * Get next vocable
     */
    private void getNext() {
        if (cVocable != null) {
            lastAddition = cVocable.getAddition();
        }
        showedSolution = false;
        showedTip = false;
        Log.d(TAG,"unsolved:"+unsolved);
        if (unsolved == 0) {
            Log.d(TAG, "No unsolved entries remaining");
        } else {
            boolean allowRepetition = unsolved == 1;
            Log.d(TAG,"allowRepetition:"+allowRepetition);
            if(firstTimeVocLoad){
                firstTimeVocLoad = false;
                cVocable = settings.questioning;
            }else {
                cVocable = db.getRandomTrainerEntry(cVocable, settings, allowRepetition);
            }
            if (cVocable == null) {
                Log.wtf(TAG, "New vocable is null!");
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

        return order == AB_MODE.A ? cVocable.getBString() : cVocable.getAString();
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
        this.showedTip = true;
        this.tips++;
        return cVocable.getTip();
    }

    /**
     * Returns whether the current vocable has a tip
     * @return
     */
    public boolean hasTip() {
        return cVocable != null && cVocable.getTip() != null;
    }

    /**
     * Returns the column name of the question<br>
     * returns an empty string when there is no current vocable
     *
     * @return
     */
    public String getColumnNameExercise() {
        if (this.cVocable == null)
            return "ERROR: No Column Name!";

        return order == AB_MODE.A ? cVocable.getList().getNameB() : cVocable.getList().getNameA();
    }

    /**
     * Returns the column name of the solution<br>
     * returns an empty string when there is no current vocable
     *
     * @return
     */
    public String getColumnNameSolution() {
        if (this.cVocable == null)
            return "ERROR: No Column Name!";

        return order == AB_MODE.B ? cVocable.getList().getNameB() : cVocable.getList().getNameA();
    }

    /**
     * @return remaining vocables
     */
    public long remaining() {
        return unsolved;
    }

    /**
     * @return total vocables, including finished
     */
    public long total(){ return total; }

    /**
     * @return solved vocables
     */
    public long solved() {
        return total - unsolved;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Parcel constructor
     * @param in
     */
    protected Trainer(Parcel in) {
        order = in.readInt() == 1 ? AB_MODE.A : AB_MODE.B;
        cVocable = in.readParcelable(VEntry.class.getClassLoader());
        tips = in.readInt();
        total = in.readInt();
        timesToSolve = in.readInt();
        timesShowedSolution = in.readInt();
        showedSolution = readParcableBool(in);
        showedTip = readParcableBool(in);
        settings = in.readParcelable(TrainerSettings.class.getClassLoader());
        firstTimeVocLoad = readParcableBool(in);
        lastAddition = in.readString();
    }

    public TrainerSettings getSettings() {
        return settings;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(order == AB_MODE.A ? 1 : 0);
        parcel.writeParcelable(cVocable,0);
        parcel.writeInt(tips);
        parcel.writeInt(failed);
        parcel.writeInt(timesToSolve);
        parcel.writeInt(timesShowedSolution);
        writeParcableBool(parcel,showedSolution);
        writeParcableBool(parcel,showedTip);
        parcel.writeParcelable(settings,0);
        // ssm
        // db
        writeParcableBool(parcel, firstTimeVocLoad);
        parcel.writeString(lastAddition);
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
