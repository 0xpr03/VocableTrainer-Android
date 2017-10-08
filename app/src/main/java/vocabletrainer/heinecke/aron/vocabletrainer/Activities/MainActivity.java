package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database;

/**
 * Main activity
 */
public class MainActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "voc_prefs";
    private final static int REQUEST_EDITOR_LIST = 10;
    private final static int REQUEST_TRAINER_LIST = 20;
    private final static int REQUEST_PERM_EXPORT = 30;
    private final static int REQUEST_PERM_IMPORT = 35;
    private static final String P_KEY_ALPHA_DIALOG = "showedAlphaDialog";
    private static boolean showedDialog = false;
    Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        showedDialog = settings.getBoolean(P_KEY_ALPHA_DIALOG, false);
        if (!showedDialog) {
            final AlertDialog.Builder finishedDiag = new AlertDialog.Builder(this);

            finishedDiag.setTitle("Info");
            finishedDiag.setMessage("This software is an beta state. This includes, but not limited to, data loss, destroying your phone, eating your children and burning your dog! You have been warned.");

            finishedDiag.setPositiveButton("TLDR", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    showedDialog = true;
                }
            });

            finishedDiag.setNegativeButton("Get me outta here", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    System.exit(0);
                }
            });

            finishedDiag.show();
        }
        btnContinue = (Button) findViewById(R.id.bLastSession);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                //Todo: rewrite both activities to use the returning list selector directly
                case REQUEST_EDITOR_LIST: {
                    Intent myIntent = new Intent(this, EditorActivity.class);
                    myIntent.putExtra(EditorActivity.PARAM_NEW_TABLE, false);
                    myIntent.putExtra(EditorActivity.PARAM_TABLE, data.getSerializableExtra(ListActivity.RETURN_LISTS));
                    this.startActivity(myIntent);
                }
                break;
                case REQUEST_TRAINER_LIST: {
                    Intent myIntent = new Intent(this, TrainerSettingsActivity.class);
                    myIntent.putExtra(ListActivity.RETURN_LISTS, data.getSerializableExtra(ListActivity.RETURN_LISTS));
                    this.startActivity(myIntent);
                }
                break;
                case REQUEST_PERM_EXPORT: {
                    startExportActivityUnchecked();
                }
                break;
                case REQUEST_PERM_IMPORT: {
                    startImportActivityUnchecked();
                }
                break;
            }
        }
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
        btnContinue.setEnabled(new Database(getBaseContext()).isSessionStored());
    }

    /**
     * Open trainer to continue the last session
     *
     * @param view
     */
    public void continueSession(View view) {
        Intent myIntent = new Intent(this, TrainerActivity.class);
        myIntent.putExtra(TrainerActivity.PARAM_RESUME_SESSION_FLAG, true);
        this.startActivity(myIntent);
    }

    /**
     * Open new table intent
     *
     * @param view
     */
    public void showNewTable(View view) {
        Intent myIntent = new Intent(this, EditorActivity.class);
        myIntent.putExtra(EditorActivity.PARAM_NEW_TABLE, true);
        this.startActivity(myIntent);
    }

    /**
     * Open edit table intent
     *
     * @param view
     */
    public void showEditTable(View view) {
        Intent myIntent = new Intent(this, ListActivity.class);
        myIntent.putExtra(ListActivity.PARAM_MULTI_SELECT, false);
        this.startActivityForResult(myIntent, REQUEST_EDITOR_LIST);
    }

    /**
     * Open trainer intent
     *
     * @param view
     */
    public void showTrainer(View view) {
        Intent myIntent = new Intent(this, ListActivity.class);
        myIntent.putExtra(ListActivity.PARAM_MULTI_SELECT, true);
        this.startActivityForResult(myIntent, REQUEST_TRAINER_LIST);
    }

    /**
     * Open list delete
     *
     * @param view
     */
    public void showDeleteTable(View view) {
        Intent myIntent = new Intent(this, ListActivity.class);
        myIntent.putExtra(ListActivity.PARAM_DELETE_FLAG, true);
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
        if (PermActivity.hasPermission(getApplicationContext(), ExportActivity.REQUIRED_PERMISSION)) {
            startExportActivityUnchecked();
        } else {
            Intent myIntent = new Intent(this, PermActivity.class);
            myIntent.putExtra(PermActivity.PARAM_PERMISSION, ExportActivity.REQUIRED_PERMISSION);
            myIntent.putExtra(PermActivity.PARAM_MESSAGE, getString(R.string.Perm_CSV));
            this.startActivityForResult(myIntent,REQUEST_PERM_EXPORT);
        }
    }

    /**
     * Open import activity
     *
     * @param view
     */
    public void showImport(View view) {
        if (PermActivity.hasPermission(getApplicationContext(), ImportActivity.REQUIRED_PERMISSION)) {
            startImportActivityUnchecked();
        } else {
            Intent myIntent = new Intent(this, PermActivity.class);
            myIntent.putExtra(PermActivity.PARAM_PERMISSION, ImportActivity.REQUIRED_PERMISSION);
            myIntent.putExtra(PermActivity.PARAM_MESSAGE, getString(R.string.Perm_CSV));
            this.startActivityForResult(myIntent,REQUEST_PERM_IMPORT);
        }
    }

    /**
     * Start import activity, does not check for permissions
     */
    private void startImportActivityUnchecked(){
        Intent myIntent = new Intent(this, ImportActivity.class);
        this.startActivity(myIntent);
    }

    /**
     * Start export activity, does not check for permissions
     */
    private void startExportActivityUnchecked(){
        Intent myIntent = new Intent(this, ExportActivity.class);// dialog.dismiss();
        this.startActivity(myIntent);
    }

}
