package vocabletrainer.heinecke.aron.vocabletrainer.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.dialog.SurveyDialog;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Widget.VectorImageHelper;

/**
 * Main activity
 */
public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    public static final String PREFS_NAME = "voc_prefs";
    private static final String P_KEY_ALPHA_DIALOG = "showedAlphaDialog";
    @SuppressWarnings("unused")
    @Deprecated
    private static final String P_KEY_DB_CHANGE_N_N = "showedDBDialogN_N";
    private static boolean showedDialog = false;
    Button btnContinue;
    SurveyDialog surveyDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        showedDialog = settings.getBoolean(P_KEY_ALPHA_DIALOG, false);
        if (!showedDialog) {
            final AlertDialog.Builder betaWarnDiag = new AlertDialog.Builder(this,R.style.CustomDialog);

            betaWarnDiag.setTitle(R.string.Beta_Warning_Diag_Title);
            betaWarnDiag.setMessage(R.string.Beta_Warning_Diag_Msg);

            betaWarnDiag.setPositiveButton(R.string.Beta_Warning_Btn_Accept, (dialog, whichButton) -> showedDialog = true);

            betaWarnDiag.setNegativeButton(R.string.Beta_Warning_Btn_Exit, (dialog, whichButton) -> System.exit(0));

            betaWarnDiag.show();
        } else { // don't show both dialogs, show beta warning at first

            if (savedInstanceState != null) {
                surveyDialog = (SurveyDialog) getSupportFragmentManager().getFragment(savedInstanceState, SurveyDialog.TAG);
            }
            if (surveyDialog == null && SurveyDialog.shouldDisplaySurvey(this)) {
                surveyDialog = SurveyDialog.newInstance();
                surveyDialog.show(getSupportFragmentManager(), SurveyDialog.TAG);
            }
        }

        btnContinue = findViewById(R.id.bLastSession);
        VectorImageHelper helper = new VectorImageHelper(this, findViewById(android.R.id.content));
        helper.initImageLeft(R.id.bLastSession, R.drawable.ic_play_arrow_white_24dp);
        helper.initImageLeft(R.id.bTrainerEnter, R.drawable.ic_send_white_24dp);
        helper.initImageLeft(R.id.bEditTable, R.drawable.ic_edit_white_24dp);
        helper.initImageLeft(R.id.bAbout, R.drawable.ic_info_outline_white_24dp);
        helper.initImageLeft(R.id.bExport, R.drawable.ic_file_upload_white_24dp);
        helper.initImageLeft(R.id.bImport, R.drawable.ic_file_download_white_24dp);
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(P_KEY_ALPHA_DIALOG, showedDialog);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnContinue.setEnabled(false);

        new Thread(() -> {
            final boolean openSession = new Database(getBaseContext()).isSessionStored();

            runOnUiThread(() -> btnContinue.setEnabled(openSession));

        }).start();
    }

    /**
     * Open trainer to continue the last session
     *
     * @param view
     */
    public void continueSession(View view) {
        Intent myIntent = new Intent(this, TrainerActivity.class);
        this.startActivity(myIntent);
    }

    /**
     * Crash test
     * @param view
     */
    public void crashTest(View view) {
        throw new RuntimeException("Crash test");
    }

    /**
     * Open edit table intent
     *
     * @param view
     */
    public void showEditTable(View view) {
        Intent myIntent = new Intent(this, ListActivity.class);
        myIntent.putExtra(ListActivity.PARAM_FULL_FEATURESET, true);
        this.startActivity(myIntent);
    }

    /**
     * Open trainer intent
     *
     * @param view
     */
    public void showTrainer(View view) {
        Intent myIntent = new Intent(this, TrainerSettingsActivity.class);
        this.startActivity(myIntent);
    }

    /**
     * Open about activity
     *
     * @param view
     */
    public void showAbout(View view) {
        Intent myIntent = new Intent(this, AboutActivity.class);
        this.startActivity(myIntent);
    }

    /**
     * Open export activity
     *
     * @param view
     */
    public void showExport(View view) {
        Intent myIntent = new Intent(this, ExImportActivity.class);
        myIntent.putExtra(ExImportActivity.PARAM_IMPORT, false);
        this.startActivity(myIntent);
    }

    /**
     * Open import activity
     *
     * @param view
     */
    public void showImport(View view) {
        Intent myIntent = new Intent(this, ExImportActivity.class);
        myIntent.putExtra(ExImportActivity.PARAM_IMPORT, true);
        this.startActivity(myIntent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (surveyDialog != null && surveyDialog.isAdded())
            getSupportFragmentManager().putFragment(outState, SurveyDialog.TAG, surveyDialog);
    }
}
