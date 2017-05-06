package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import vocabletrainer.heinecke.aron.vocabletrainer.R;

import static vocabletrainer.heinecke.aron.vocabletrainer.Activities.ListSelector.PARAM_NEW_ACTIVITY;

/**
 * Main activity
 */
public class MainAcitivity extends AppCompatActivity {
    private static boolean showedDialog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("VocableTrainer Alpha");
        if(!showedDialog) {
            final AlertDialog.Builder finishedDiag = new AlertDialog.Builder(this);

            finishedDiag.setTitle("Warning");
            finishedDiag.setMessage("This software is an alpha state. This includes, but not limited to, data loss, destroying your phone, eating your children and burning your dog! You have been warned.");

            finishedDiag.setPositiveButton("Show me", new DialogInterface.OnClickListener() {
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
        Intent myIntent = new Intent(this, ListSelector.class);
        myIntent.putExtra(PARAM_NEW_ACTIVITY,EditorActivity.class);
        myIntent.putExtra(ListSelector.PARAM_MULTI_SELECT, false);
        this.startActivity(myIntent);
    }

    /**
     * Open trainer intent
     * @param view
     */
    public void showTrainer(View view){
        Intent myIntent = new Intent(this, ListSelector.class);
        myIntent.putExtra(PARAM_NEW_ACTIVITY,TrainerSettingsActivity.class);
        myIntent.putExtra(ListSelector.PARAM_MULTI_SELECT, true);
        this.startActivity(myIntent);
    }

}
