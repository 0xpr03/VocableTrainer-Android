package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import vocabletrainer.heinecke.aron.vocabletrainer.BuildConfig;
import vocabletrainer.heinecke.aron.vocabletrainer.R;

public class AboutActivity extends AppCompatActivity {

    private static String MSG = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        if(MSG == null){
            String versionName = BuildConfig.VERSION_NAME;
            MSG = getText(R.string.About_Msg).toString().replaceAll("\\n","\n").replaceAll("%v",versionName);
        }
        TextView msgTextbox = (TextView) findViewById(R.id.etAboutMsg);
        msgTextbox.setText(MSG);
    }

    /**
     * Called by ok button<br>
     *     go back to main activity
     * @param view
     */
    public void exitAbout(View view){
        this.finish();
    }
}
