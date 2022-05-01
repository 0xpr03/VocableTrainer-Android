package vocabletrainer.heinecke.aron.vocabletrainer.eximport;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;
import android.util.Log;

import vocabletrainer.heinecke.aron.vocabletrainer.eximport.CSV.CSVCustomFormat;

import static vocabletrainer.heinecke.aron.vocabletrainer.eximport.ExImportActivity.loadCustomFormat;
import static vocabletrainer.heinecke.aron.vocabletrainer.activity.MainActivity.PREFS_NAME;

/**
 * ViewModel for format customization
 * @author Aron Heinecke
 */
public class FormatViewModel extends AndroidViewModel {
    private static final String TAG = "FormatViewModel";
    private MutableLiveData<CSVCustomFormat> csvCustomFormat;
    private MutableLiveData<Boolean> inFormatFragment;

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
