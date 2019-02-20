package vocabletrainer.heinecke.aron.vocabletrainer.lib.ViewModel;

import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import info.guardianproject.netcipher.NetCipher;

/**
 * ViewModel for survey, doing survey submit
 */
public class SurveyViewModel extends ViewModel {
    private static final String TAG = "SurveyViewModel";
    private MutableLiveData<Boolean> loading;
    private MutableLiveData<Boolean> error;
    private Thread runner;

    public SurveyViewModel() {
        loading = new MutableLiveData<>();
        error = new MutableLiveData<>();
        loading.setValue(false);
        error.setValue(false);
    }

    public synchronized void submitSurvey() {
        if (wasRunning()) {
            return;
        }
        loading.postValue(true);
        System.setProperty("http.keepAlive", "false");

        final String url;
        // allow tls 1.0
        // can't be tested fully due to different device libraries
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            url = "https://vct.badssl.proctet.net/data/add/api";
        } else {
            url = "https://vct.proctet.net/data/add/api";
        }

        runner = new Thread(() -> {

            final JSONObject jsonBody;
            try {
                HttpsURLConnection con = NetCipher.getHttpsURLConnection(new URL(url));
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                con.setRequestProperty("Accept", "application/json");
                con.setDoOutput(true);
                con.setDoInput(true);

                jsonBody = new JSONObject("{\"api\":" + Build.VERSION.SDK_INT + "}");

                OutputStream wr = con.getOutputStream();
                wr.write(jsonBody.toString().getBytes("UTF-8"));
                wr.flush();
                wr.close();

                InputStream in = new BufferedInputStream(con.getInputStream());
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }

                JSONObject jsonObject = new JSONObject(result.toString("UTF-8"));
                in.close();

                con.disconnect();
                Log.d(TAG,"received json: "+jsonObject.toString());
                loading.postValue(false);
            } catch (
                    JSONException e) {
                Log.wtf(TAG, e);
                error.postValue(true);
            } catch (MalformedURLException e) {
                Log.wtf(TAG, e);
                error.postValue(true);
            } catch (IOException e) {
                Log.e(TAG, "IOException:",e);
                error.postValue(true);
            }
        });
        runner.start();
    }

    public boolean wasRunning() {
        return runner != null;
    }

    public LiveData<Boolean> getErrorLiveData() {
        return this.error;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return this.loading;
    }
}
