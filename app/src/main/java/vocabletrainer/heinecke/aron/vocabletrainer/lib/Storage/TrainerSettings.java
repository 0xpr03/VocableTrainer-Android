package vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage;

import java.io.Serializable;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer;

/**
 * Trainer settings obj
 * TODO: evaluate build pattern
 */
public class TrainerSettings implements Serializable {

    public final int timesToSolve;
    public final Trainer.TEST_MODE mode;
    public final boolean allowTips;
    public final int tipsGiven;
    public final int timesFailed;
    public final boolean caseSensitive;
    public final boolean trimSpaces;


    /**
     * Last questioning, outstanding vocable<br>
     * <b>Can be null!</b>
     */
    public VEntry questioning;

    /**
     * Create a new Trainer Settings storage with default starting values
     *
     * @param timesToSolve number of times each vocable has to be solved correct
     * @param mode training mode
     * @param allowTips allow tips
     * @param caseSensitive check input case sensitive
     * @param trimSpaces ignore sourrounding spaces (input & output)
     */
    public TrainerSettings(final int timesToSolve, final Trainer.TEST_MODE mode, final boolean allowTips,
                           final boolean caseSensitive, final boolean trimSpaces) {
        this(timesToSolve, mode, allowTips, 0, 0,caseSensitive,null, trimSpaces);
    }

    /**
     * Create a new Trainer Settings storage
     *
     * @param timesToSolve number of times each vocable has to be solved correct
     * @param mode training mode
     * @param allowTips allow tips
     * @param tipsGiven number of tips given already (session continue)
     * @param failedTimes number of times a vocable was entered wrong (session continue)
     * @param caseSensitive check input case sensitive
     * @param questioning last entry displayed (unsolved,session continue)
     * @param trimSpaces trim surrounding spaces (input & solution)
     */
    public TrainerSettings(final int timesToSolve, final Trainer.TEST_MODE mode, final boolean allowTips,
                           final int tipsGiven, final int failedTimes, final boolean caseSensitive,
                           final VEntry questioning, final boolean trimSpaces) {
        this.timesToSolve = timesToSolve;
        this.mode = mode;
        this.allowTips = allowTips;
        this.tipsGiven = tipsGiven;
        this.timesFailed = failedTimes;
        this.caseSensitive = caseSensitive;
        this.questioning = questioning;
        this.trimSpaces = trimSpaces;
    }

}
