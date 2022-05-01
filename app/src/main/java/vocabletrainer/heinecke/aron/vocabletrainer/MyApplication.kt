package vocabletrainer.heinecke.aron.vocabletrainer

import android.app.Application
import android.content.Context
import org.acra.config.dialog
import org.acra.config.httpSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.acra.security.TLS
import org.acra.sender.HttpSender

class MyApplication: Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        initAcra {
            //core configuration:
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON
            applicationLogFileLines = 500
            //each plugin you chose above can be configured in a block like this:
            dialog {
                //required
                text = getString(R.string.crash_dialog_text)
                //optional, enables the dialog title
                title = getString(R.string.crash_dialog_title)
                //defaults to android.R.string.ok
                positiveButtonText = getString(R.string.crash_dialog_positive)
                //defaults to android.R.string.cancel
                negativeButtonText = getString(R.string.crash_dialog_negative)
                //optional, enables the comment input
                commentPrompt = getString(R.string.crash_dialog_comment)
                //optional, enables the email input
                emailPrompt = getString(R.string.crash_dialog_email)
                //defaults to android.R.drawable.ic_dialog_alert
                //resIcon = R.drawable.crash_dialog_icon
                //optional, defaults to @android:style/Theme.Dialog
                //resTheme = R.style.
                //allows other customization
                //reportDialogClass = MyCustomDialog::class.java
            }
            httpSender {
                //required. Https recommended
                uri = "https://acra.vocabletrainer.de/report"
                //optional. Enables http basic auth
                basicAuthLogin = "uNigx2xiOtOM6sQo"
                //required if above set
                basicAuthPassword = "eE71l5LE0bDO84jc"
                // defaults to POST
                httpMethod = HttpSender.Method.POST
                //defaults to 5000ms
                connectionTimeout = 5000
                //defaults to 20000ms
                socketTimeout = 20000
                // defaults to false
                dropReportsOnTimeout = false
                //the following options allow you to configure a self signed certificate
                //keyStoreFactoryClass = MyKeyStoreFactory::class.java
                //certificatePath = "asset://mycert.cer"
                //resCertificate = R.raw.mycert
                //certificateType = "X.509"
                //defaults to false. Recommended if your backend supports it
                compress = true
                //defaults to all
                tlsProtocols = arrayOf(TLS.V1_3, TLS.V1_2, TLS.V1_1, TLS.V1)
            }
        }
    }
}