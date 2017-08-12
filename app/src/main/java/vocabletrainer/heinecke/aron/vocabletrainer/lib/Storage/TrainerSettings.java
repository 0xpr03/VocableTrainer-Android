package vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage;

import java.io.Serializable;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer;

/**
 * Trainer settings obj
 */
public class TrainerSettings implements Serializable {

    public final int timesToSolve;
    public final Trainer.TEST_MODE mode;
    public final boolean allowTips;
    public final int tipsGiven;
    public final int timesFailed;

    /**
     * Create a new Trainer Settings storage with default starting values
     *
     * @param timesToSolve
     * @param mode
     */
    public TrainerSettings(int timesToSolve, Trainer.TEST_MODE mode, boolean allowTips) {
        this(timesToSolve, mode, allowTips, 0, 0);
    }

    /**
     * Create a new Trainer Settings storage
     *
     * @param timesToSolve
     * @param mode
     * @param allowTips
     * @param tipsGiven
     * @param failedTimes
     */
    public TrainerSettings(int timesToSolve, Trainer.TEST_MODE mode, boolean allowTips, int tipsGiven, int failedTimes) {
        this.timesToSolve = timesToSolve;
        this.mode = mode;
        this.allowTips = allowTips;
        this.tipsGiven = tipsGiven;
        this.timesFailed = failedTimes;
    }

}
