package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

import vocabletrainer.heinecke.aron.vocabletrainer.Activities.lib.FileListAdapter;
import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Comparator.GenFileEntryComparator;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Comparator.GenericComparator;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Formatter;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.BasicFileEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.FileEntry;

import static vocabletrainer.heinecke.aron.vocabletrainer.Activities.MainActivity.PREFS_NAME;

/**
 * File activity for file requests<br>
 * <b>requires WRITE_EXTERNAL_STORAGE</b><br>
 * To be called as startActivityForResult
 */
public class FileActivity extends AppCompatActivity {
    /**
     * Param key under which the selected file is returned to the next activity<br>
     * File is passed as string containing the absolute path<br>
     * Type: File
     */
    public static final String RETURN_FILE = "file";
    /**
     * Param key for return of user friendly formated file path<br>
     * only containing the normal user-visible storage path<br>
     * Type: String
     */
    public static final String RETURN_FILE_USER_NAME = "user_file_path";
    /**
     * Param key for write flag<br>
     * Pass as true to get a save-as activity, otherwise read file "dialog"
     */
    public static final String PARAM_WRITE_FLAG = "write_flag";
    /**
     * Param key for short message to display
     */
    public static final String PARAM_MESSAGE = "message";
    /**
     * Optional param key for default file name, used upon write flag set true
     */
    public static final String PARAM_DEFAULT_FILENAME = "default_filename";
    private static final String P_KEY_FA_LAST_DIR = "last_directory";
    private static final String P_KEY_FA_LAST_FILENAME = "last_filename";
    private static final String P_KEY_FA_SORT = "FA_sorting_name";
    private static final String TAG = "FileActivity";
    private ListView listView;
    private EditText tFileName;
    private TextView tCurrentDir;
    private Button bOk;

    private ArrayList<BasicFileEntry> entries;
    private FileListAdapter adapter;
    private Formatter fmt;
    private boolean write;
    private File currentDir;
    private BasicFileEntry selectedEntry;
    private String basicDir; // user invisible part to remove
    private String defaultFileName;
    private File selectedFile;
    private boolean sorting_name;
    private Comparator<BasicFileEntry> compName;
    private Comparator<BasicFileEntry> compSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        compName = new GenFileEntryComparator<>(new GenericComparator.ValueRetriever[] {
                GenFileEntryComparator.retType,GenFileEntryComparator.retName,
                GenFileEntryComparator.retSize
        });
        compSize = new GenFileEntryComparator<>(new GenericComparator.ValueRetriever[] {
                GenFileEntryComparator.retType,GenFileEntryComparator.retSize,
                GenFileEntryComparator.retName
        });

        fmt = new Formatter();

        TextView msg = (TextView) findViewById(R.id.tFileMsg);
        tFileName = (EditText) findViewById(R.id.tFileName);
        tCurrentDir = (TextView) findViewById(R.id.tCurrentDir);
        bOk = (Button) findViewById(R.id.bFileOk);

        Intent intent = getIntent();
        msg.setText(intent.getStringExtra(PARAM_MESSAGE));
        write = intent.getBooleanExtra(PARAM_WRITE_FLAG, false);

        tFileName.setVisibility(write ? View.VISIBLE : View.GONE);

        String defaultName = intent.getStringExtra(PARAM_DEFAULT_FILENAME);
        defaultFileName = defaultName == null ? "file.xy" : defaultName;

        initListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fMenu_sort_name:
                sorting_name = true;
                applySorting();
                return true;
            case R.id.fMenu_sort_size:
                sorting_name = false;
                applySorting();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Setup listview
     */
    private void initListView() {
        listView = (ListView) findViewById(R.id.listViewFiles);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setLongClickable(false);

        entries = new ArrayList<>(20); // just a good guess
        adapter = new FileListAdapter(this, entries);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int pos, long id) {
                BasicFileEntry entry = entries.get(pos);
                if (entry.getTypeID() == BasicFileEntry.TYPE_FILE) {
                    Log.d(TAG, "selected: " + entry.getName() + " " + view.toString());
                    view.setSelected(true);
                    view.setActivated(true);
                    selectedEntry = entry;
                    bOk.setEnabled(true);
                    if (write) {
                        tFileName.setText(entry.getName());
                    }
                } else if (entry.getTypeID() == BasicFileEntry.TYPE_DIR) {
                    currentDir = ((FileEntry) entry).getFile();
                    changeDir();
                } else if (entry.getTypeID() == BasicFileEntry.TYPE_UP) {
                    goUp();
                }
            }

        });
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        sorting_name = settings.getBoolean(P_KEY_FA_SORT, true);
        setBasicDir(settings);
        changeDir();
    }

    /**
     * Does sorting<br>
     * Notifies data change
     */
    private void applySorting() {
        adapter.updateSorting(sorting_name ? compName : compSize);
    }

    /**
     * Go on directory up in navigation, if possible
     */
    private void goUp() {
        if (currentDir.getAbsolutePath().equals(basicDir)) {
            Log.d(TAG, "cancel go up");
        } else {
            currentDir = currentDir.getParentFile();
            changeDir();
        }
    }

    /**
     * Action for Cancel button press
     *
     * @param view
     */
    public void onCancelPressed(View view) {
        cancel();
    }

    @Override
    public boolean onSupportNavigateUp() {
        cancel();
        return true;
    }

    @Override
    public void onBackPressed() {
        cancel();
    }

    /**
     * Cancel file activity
     */
    private void cancel() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    /**
     * Action for OK button press
     *
     * @param view
     */
    public void onOkPressed(View view) {
        if (write || selectedEntry != null) {
            selectedFile = write ? new File(currentDir, tFileName.getText().toString()) : ((FileEntry) selectedEntry).getFile();
            Log.d(TAG, "file:" + selectedFile.getAbsolutePath());
            if (write) {
                if (selectedFile.isDirectory()) { // required !?
                    selectedFile = null;
                } else if (selectedFile.exists()) {
                    final AlertDialog.Builder alert = new AlertDialog.Builder(this);

                    alert.setTitle(R.string.File_Diag_exists_Title);
                    alert.setMessage(getString(R.string.File_Diag_MSG_part).replace("%f", selectedFile.getName()));

                    alert.setPositiveButton(R.string.File_Diag_btn_OK, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            useFile();
                        }
                    });

                    alert.setNegativeButton(R.string.File_Diag_btn_CANCEL, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            selectedFile = null;
                        }
                    });
                    alert.show();
                } else {
                    useFile();
                }
            }

            if (!write && selectedFile != null) {
                useFile();
            }
        }
    }

    /**
     * finishes & returns file if selected
     */
    private void useFile() {
        if (selectedFile != null) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(RETURN_FILE, selectedFile);
            returnIntent.putExtra(RETURN_FILE_USER_NAME, tCurrentDir.getText().toString() + File.separator + selectedFile.getName());
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }

    /**
     * Checks current media state
     *
     * @return true when media is ready
     */
    private boolean checkMediaState() {
        String extState = Environment.getExternalStorageState();
        if (!extState.equals(Environment.MEDIA_MOUNTED) || extState.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            Log.e(TAG, "media state: " + extState);
            Toast.makeText(FileActivity.this, R.string.File_Error_Mediastate, Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    /**
     * Load default or last path / file into dialog
     */
    private void setBasicDir(SharedPreferences settings) {
        currentDir = new File(settings.getString(P_KEY_FA_LAST_DIR, ""));
        if (!currentDir.exists()) { // old value not valid anymore
            Log.w(TAG, "old path is invalid");
            currentDir = Environment.getExternalStorageDirectory();
        }
        currentDir.mkdirs(); // mkdirs, we're sure to have a valid path
        this.basicDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        tFileName.setText(settings.getString(P_KEY_FA_LAST_FILENAME, defaultFileName));
    }

    /**
     * Change directory in view to the one specified in currentDir<br>
     * if currentDir is null, we're assuming that the overview is required
     */
    private void changeDir() {
        selectedEntry = null;
        if (!write) {
            bOk.setEnabled(false);
        }
        entries.clear();
        if (checkMediaState()) {
            if (currentDir != null) {
                File[] files = currentDir.listFiles();
                if (files == null) {
                    Log.e(TAG, "null file list!");
                    Toast.makeText(FileActivity.this, R.string.File_Error_Nullpointer, Toast.LENGTH_LONG).show();
                } else {
                    entries.add(new BasicFileEntry("..", "", 0, BasicFileEntry.TYPE_UP, true)); // go back entry
                    for (File file : files) {
                        entries.add(new FileEntry(file, fmt));
                    }
                }
            }
        }
        applySorting();

        String newDirLabel = currentDir.getAbsolutePath().replaceFirst(basicDir, "");
        if (newDirLabel.length() == 0)
            newDirLabel = "/";
        tCurrentDir.setText(newDirLabel);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Save values
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(P_KEY_FA_LAST_FILENAME, tFileName.getText().toString());
        editor.putString(P_KEY_FA_LAST_DIR, currentDir.getAbsolutePath());
        editor.putBoolean(P_KEY_FA_SORT, sorting_name);
        editor.apply();
    }
}
