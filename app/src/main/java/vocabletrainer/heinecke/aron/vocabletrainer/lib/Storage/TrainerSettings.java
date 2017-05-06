package vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage;

/**
 * Created by aron on 07.04.17.
 */

import java.io.Serializable;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer;

/**
 * Trainer settings obj
 */
public class TrainerSettings implements Serializable {
    private final static String KEY_SOLVE_TIMES = "t_solve";
    private final static String KEY_MODE = "mode";
    private final static String KEY_TIPS = "tips";
    private final static String KEY_GIVEN_TIPS = "t_tips_given";
    private final static String KEY_FAILED = "t_failed";

    public final int timesToSolve;
    public final Trainer.TEST_MODE mode;
    public final boolean allowTps;
    public final int tipsGiven;
    public final int timesFailed;

    /**
     * Create a new Trainer Settings storage with default starting values
     * @param timesToSolve
     * @param mode
     */
    public TrainerSettings(int timesToSolve, Trainer.TEST_MODE mode, boolean allowTips){
        this(timesToSolve, mode, allowTips,0,0);
    }

    /**
     * Create a new Trainer Settings storage
     * @param timesToSolve
     * @param mode
     * @param allowTips
     * @param tipsGiven
     * @param failedTimes
     */
    public TrainerSettings(int timesToSolve, Trainer.TEST_MODE mode, boolean allowTips, int tipsGiven, int failedTimes) {
        this.timesToSolve = timesToSolve;
        this.mode = mode;
        this.allowTps = allowTips;
        this.tipsGiven = tipsGiven;
        this.timesFailed = failedTimes;
    }

    public static boolean saveTrainerSettings(TrainerSettings settings, Database db){
        return false;
    }
}
