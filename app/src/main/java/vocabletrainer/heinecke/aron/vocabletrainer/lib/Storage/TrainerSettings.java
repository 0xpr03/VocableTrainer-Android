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
    public final boolean caseSensitive;

    /**
     * Create a new Trainer Settings storage with default starting values
     *
     * @param timesToSolve
     * @param mode
     */
    public TrainerSettings(final int timesToSolve, final Trainer.TEST_MODE mode, final boolean allowTips,
                           final boolean caseSensitive) {
        this(timesToSolve, mode, allowTips, 0, 0,caseSensitive);
    }

    /**
     * Create a new Trainer Settings storage
     *
     * @param timesToSolve
     * @param mode
     * @param allowTips
     * @param tipsGiven
     * @param failedTimes
     * @param caseSensitive
     */
    public TrainerSettings(final int timesToSolve, final Trainer.TEST_MODE mode, final boolean allowTips,
                           final int tipsGiven, final int failedTimes, final boolean caseSensitive) {
        this.timesToSolve = timesToSolve;
        this.mode = mode;
        this.allowTips = allowTips;
        this.tipsGiven = tipsGiven;
        this.timesFailed = failedTimes;
        this.caseSensitive = caseSensitive;
    }

}
