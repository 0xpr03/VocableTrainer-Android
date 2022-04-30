package vocabletrainer.heinecke.aron.vocabletrainer.lib.ViewModel

import org.acra.ACRA.errorReporter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import kotlin.jvm.Synchronized
import org.json.JSONObject
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import org.acra.ktx.sendWithAcra
import vocabletrainer.heinecke.aron.vocabletrainer.BuildConfig
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * ViewModel for survey, doing survey submit
 */
class SurveyViewModel : ViewModel() {
    private val loading: MutableLiveData<Boolean>
    private val error: MutableLiveData<Boolean>
    private var runner: Thread? = null
    @Synchronized
    fun submitSurvey() {
        if (wasRunning()) {
            return
        }
        loading.postValue(true)
        System.setProperty("http.keepAlive", "false")
        val url: String = if (BuildConfig.BUILD_TYPE == "debug") {
            //"http://192.168.178.32:3219/data/add/api"
            "http://localhost:3219/data/add/api"
        } else {
            // allow tls 1.0
            // can't be tested fully due to different device libraries
            "https://stats.vocabletrainer.de/data/add/api"
        }
        runner = Thread {
            val jsonBody: JSONObject
            try {
                errorReporter.putCustomData("url", url)
                val con = URL(url).openConnection() as HttpURLConnection
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                con.setRequestProperty("Accept", "application/json")
                con.doOutput = true
                con.doInput = true
                jsonBody = JSONObject("{\"api\":" + Build.VERSION.SDK_INT + "}")
                val wr = con.outputStream
                wr.write(jsonBody.toString().toByteArray(StandardCharsets.UTF_8))
                wr.flush()
                wr.close()
                val `in`: InputStream = BufferedInputStream(con.inputStream)
                val result = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var length: Int
                while (`in`.read(buffer).also { length = it } != -1) {
                    result.write(buffer, 0, length)
                }

                val resultString = result.toString("UTF-8")
                errorReporter.putCustomData("server response", resultString)
                val jsonObject = JSONObject(resultString)
                `in`.close()
                con.disconnect()
                Log.d(TAG, "received json: $jsonObject")
                loading.postValue(false)
            } catch (e: Exception) {
                Log.wtf(TAG, e)
                e.sendWithAcra()
                error.postValue(true)
            }
        }
        runner!!.start()
    }

    fun wasRunning(): Boolean {
        return runner != null
    }

    val errorLiveData: LiveData<Boolean>
        get() = error
    val loadingLiveData: LiveData<Boolean>
        get() = loading

    companion object {
        private const val TAG = "SurveyViewModel"
    }

    init {
        loading = MutableLiveData()
        error = MutableLiveData()
        loading.value = false
        error.value = false
    }
}