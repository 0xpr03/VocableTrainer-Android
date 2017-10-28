package vocabletrainer.heinecke.aron.vocabletrainer.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import vocabletrainer.heinecke.aron.vocabletrainer.R;

/**
 * Activity to request permissions<br>
 *     Returns success when permission was granted
 */
public class PermActivity extends AppCompatActivity {
    public static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 10000;
    public static final String PARAM_PERMISSION = "permissions";
    public static final String PARAM_MESSAGE = "message";
    private static final String TAG = "PermActivity";

    private String permission;
    private String message;
    private Button bRetry;

    /**
     * Check whether we have this permission or not<br>
     * should be called before this activity to check whether this is necessary
     *
     * @param context
     * @param perm
     * @return true when context has specified permission
     */
    public static boolean hasPermission(Context context, String perm) {
        return ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request permissions for generic context & activity
     *
     * @param context
     * @param activity
     * @param perm
     */
    private static void requestPerm(Context context, Activity activity, String perm) {
        if (!hasPermission(context, perm)) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    perm)) {
                Log.d(TAG, "not requesting..");
            }

            // No explanation needed, we can request the permission.
            Log.d(TAG, "requesting..");
            ActivityCompat.requestPermissions(activity,
                    new String[]{perm},
                    MY_PERMISSIONS_REQUEST_READ_STORAGE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perm);

        Intent intent = getIntent();
        // handle passed params
        permission = intent.getStringExtra(PARAM_PERMISSION);
        message = intent.getStringExtra(PARAM_MESSAGE);

        if (permission == null || message == null) {
            Log.wtf(TAG, "missing parameters");
        }

        TextView tMsg = (TextView) findViewById(R.id.tPermMsg);
        tMsg.setText(message);
        bRetry = (Button) findViewById(R.id.bPermReqAgain);
        bRetry.setVisibility(View.INVISIBLE);

        //TODO: allow resource IDs as message

        requestPerm();
    }

    /**
     * Called upon retry click
     *
     * @param view
     */
    public void onRetry(View view) {
        requestPerm();
    }

    /**
     * Wrapper around requestPerm for unique calls
     */
    private void requestPerm() {
        requestPerm(getApplicationContext(), this, permission);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_STORAGE: {
                //permission to read storage
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent returnIntent = new Intent();
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                } else { // allow for retry
                    bRetry.setVisibility(View.VISIBLE);
                }
                return;
            }
        }
    }

    /**
     * Called upon cancel pressed
     *
     * @param view
     */
    public void onCancel(View view) {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

}
