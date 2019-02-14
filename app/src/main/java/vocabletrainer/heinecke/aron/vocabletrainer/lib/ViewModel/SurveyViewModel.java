package vocabletrainer.heinecke.aron.vocabletrainer.lib.ViewModel;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel for survey, doing survey submit
 */
public class SurveyViewModel extends ViewModel {
    private static final String TAG = "SurveyViewModel";
    private boolean wasRunning;
    private MutableLiveData<Boolean> loading;
    private MutableLiveData<Boolean> error;
    private RequestQueue queue;
    private Thread thread;

    public SurveyViewModel() {
        wasRunning = false;
        loading = new MutableLiveData<>();
        error = new MutableLiveData<>();
        loading.setValue(false);
        error.setValue(false);
    }

    public void submitSurvey(Context context) {
        if (wasRunning) {
            return;
        }
        loading.postValue(true);
        wasRunning = true;
        thread = new Thread(() -> {
            queue = Volley.newRequestQueue(context);
            String url = "https://vta.proctet.net/submit/?api="+ Build.VERSION.SDK_INT;
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    response -> {
                        Log.d(TAG, "Response is: " + response.substring(0, 500));
                        wasRunning = true;
                        loading.postValue(false);
                    }, e -> {
                Log.e(TAG, "error during request: " + e.getMessage());
                error.postValue(true);
            });

            queue.add(stringRequest);

            queue.start();
        });
        thread.start();
    }

    public boolean wasRunning() {
        return wasRunning;
    }

    public LiveData<Boolean> getErrorLiveData() {
        return this.error;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return this.loading;
    }
}
