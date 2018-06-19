package vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage;

import android.os.Parcel;
import android.os.Parcelable;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer.Trainer;

import static vocabletrainer.heinecke.aron.vocabletrainer.lib.ParcableTools.readParcableBool;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.ParcableTools.writeParcableBool;

/**
 * Trainer settings obj
 * TODO: evaluate build pattern
 */
public class TrainerSettings implements Parcelable {
    public final int timesToSolve;
    public final Trainer.TEST_MODE mode;
    public final boolean allowTips;
    public final int tipsGiven;
    public final int timesFailed;
    public final boolean caseSensitive;
    public final boolean trimSpaces;
    public final boolean additionAuto;


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
     * @param additionAuto Automatically show next after addition value
     */
    public TrainerSettings(final int timesToSolve, final Trainer.TEST_MODE mode, final boolean allowTips,
                           final boolean caseSensitive, final boolean trimSpaces, final boolean additionAuto) {
        this(timesToSolve, mode, allowTips, 0, 0,caseSensitive,null, trimSpaces, additionAuto);
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
                           final VEntry questioning, final boolean trimSpaces, final boolean additionAuto) {
        this.timesToSolve = timesToSolve;
        this.mode = mode;
        this.allowTips = allowTips;
        this.tipsGiven = tipsGiven;
        this.timesFailed = failedTimes;
        this.caseSensitive = caseSensitive;
        this.questioning = questioning;
        this.trimSpaces = trimSpaces;
        this.additionAuto = additionAuto;
    }

    /**
     * Parcel constructor
     * @param in
     */
    protected TrainerSettings(Parcel in){
        this.timesToSolve = in.readInt();
        this.mode = Trainer.TEST_MODE.fromInt(in.readInt());
        this.allowTips = readParcableBool(in);
        this.tipsGiven = in.readInt();
        this.timesFailed = in.readInt();
        this.caseSensitive = readParcableBool(in);
        this.questioning = in.readParcelable(VEntry.class.getClassLoader());
        this.trimSpaces = readParcableBool(in);
        this.additionAuto = readParcableBool(in);
    }

    public static final Creator<TrainerSettings> CREATOR = new Creator<TrainerSettings>() {
        @Override
        public TrainerSettings createFromParcel(Parcel in) {
            return new TrainerSettings(in);
        }

        @Override
        public TrainerSettings[] newArray(int size) {
            return new TrainerSettings[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(timesToSolve);
        parcel.writeInt(mode.getValue());
        writeParcableBool(parcel,allowTips);
        parcel.writeInt(tipsGiven);
        parcel.writeInt(timesFailed);
        writeParcableBool(parcel,caseSensitive);
        parcel.writeParcelable(questioning,0);
        writeParcableBool(parcel,trimSpaces);
        writeParcableBool(parcel,additionAuto);
    }
}
