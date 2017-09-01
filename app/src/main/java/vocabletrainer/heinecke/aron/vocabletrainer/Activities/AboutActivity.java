package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import vocabletrainer.heinecke.aron.vocabletrainer.BuildConfig;
import vocabletrainer.heinecke.aron.vocabletrainer.R;

/**
 * About activity
 */
public class AboutActivity extends AppCompatActivity {
    private final static String LINK = "https://github.com/0xpr03/VocableTrainer-Android/issues";
    private final static String LINK_HTML = "<u><a href=\"" + LINK + "\">github.com/0xpr03/VocableTrainer-Android/issues</a></u>";
    private static String MSG = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        if (MSG == null) {
            String versionName = BuildConfig.VERSION_NAME;
            MSG = getText(R.string.About_Msg).toString().replaceAll("\\n", "\n")
                    .replaceAll("\\n", "<br>")
                    .replaceAll("%v", versionName)
                    .replace("%l", LINK_HTML);
        }
        TextView msgTextbox = (TextView) findViewById(R.id.etAboutMsg);
        setTextViewHTML(msgTextbox, MSG);
    }

    /**
     * Make a link clickable
     * @param strBuilder
     * @param span
     */
    protected void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span) {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        ClickableSpan clickable = new ClickableSpan() {
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(LINK));
                if (browserIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(browserIntent);
                } else {
                    Log.e("ABOUT", "unable to open a browser!");
                }
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    /**
     * Set text view content to HTML
     * @param text
     * @param html HTML sourcecode
     */
    protected void setTextViewHTML(TextView text, String html) {
        CharSequence sequence = Html.fromHtml(html); //TODO: use API level 24 or above to correct this
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for (URLSpan span : urls) {
            makeLinkClickable(strBuilder, span);
        }
        text.setText(strBuilder);
        text.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * Called by ok button<br>
     * go back to main activity
     *
     * @param view
     */
    public void exitAbout(View view) {
        this.finish();
    }
}
