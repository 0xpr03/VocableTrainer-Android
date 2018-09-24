package vocabletrainer.heinecke.aron.vocabletrainer.lib.ViewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.util.Log;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.CSVCustomFormat;

import static vocabletrainer.heinecke.aron.vocabletrainer.activity.ExImportActivity.loadCustomFormat;
import static vocabletrainer.heinecke.aron.vocabletrainer.activity.MainActivity.PREFS_NAME;

/**
 * ViewModel for format customization
 * @author Aron Heinecke
 */
public class FormatViewModel extends AndroidViewModel {
    private static final String TAG = "FormatViewModel";
    private MutableLiveData<CSVCustomFormat> csvCustomFormat;
    private MutableLiveData<Boolean> inFormatFragment;

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.w("FormatViewModel","onCleared");
    }

    /**
     * Create new FormatViewModel, loading last CustomFormat
     * @param application
     */
    public FormatViewModel(@NonNull Application application) {
        super(application);
        csvCustomFormat = new MutableLiveData<>();
        inFormatFragment = new MutableLiveData<>();
        csvCustomFormat.setValue(loadCustomFormat(getApplication().getSharedPreferences(PREFS_NAME, 0)));
        inFormatFragment.setValue(false);
    }

    /**
     * Check whether UI currently in CustomFormatFragment
     * @return true when in FormatFragment
     */
    public boolean isInFormatDialog() {
        if(inFormatFragment.getValue() != null)
            return inFormatFragment.getValue();
        else return false;
    }

    /**
     * Returns the InFormatFragment LiveData
     * @return
     */
    public LiveData<Boolean> getInFormatFragmentLD() {
        return inFormatFragment;
    }

    /**
     * Update InFormatFragment
     * @param inFormatFragment new Value
     */
    public void setInFormatFragment(boolean inFormatFragment) {
        this.inFormatFragment.setValue(inFormatFragment);
    }

    /**
     * Returns CustomFormat
     * @return {@code LiveData<CSVCustomFormat>}
     */
    public LiveData<CSVCustomFormat> getCustomFormatLD() {
        return csvCustomFormat;
    }

    /**
     * Returns the actual CustomFormat
     * @return CSVCustomFormat
     */
    public CSVCustomFormat getCustomFormatData() {
        return csvCustomFormat.getValue();
    }

    /**
     * Set CSVCustomFormat data, main thread
     * @param format new Value
     */
    public void setCustomFormat(CSVCustomFormat format) {
        Log.d(TAG,"setCustomFormat "+format.getFormat().hashCode() + " "+getCustomFormatData().getFormat().hashCode());
        if(!format.equals(getCustomFormatData())) {
            Log.d(TAG,"New Format");
            csvCustomFormat.setValue(format);
        }
    }
}
