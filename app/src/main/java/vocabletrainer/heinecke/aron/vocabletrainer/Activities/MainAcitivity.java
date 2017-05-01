package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import vocabletrainer.heinecke.aron.vocabletrainer.R;

import static vocabletrainer.heinecke.aron.vocabletrainer.Activities.ListSelector.PARAM_NEW_ACTIVITY;

/**
 * Main activity
 */
public class MainAcitivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        myIntent.putExtra(PARAM_NEW_ACTIVITY,EditorActivity.class);
        myIntent.putExtra(ListSelector.PARAM_MULTI_SELECT, true);
        this.startActivity(myIntent);
    }

}
