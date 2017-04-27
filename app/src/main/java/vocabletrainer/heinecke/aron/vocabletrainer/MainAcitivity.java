package vocabletrainer.heinecke.aron.vocabletrainer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainAcitivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Open new table intent
     * @param view
     */
    public void showNewTable(View view){
        Intent myIntent = new Intent(this, Editor_Activity.class);
        myIntent.putExtra(Editor_Activity.PARAM_NEW_TABLE, true);
        this.startActivity(myIntent);
    }
}
