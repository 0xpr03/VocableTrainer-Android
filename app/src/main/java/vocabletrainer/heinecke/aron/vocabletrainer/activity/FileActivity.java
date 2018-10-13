package vocabletrainer.heinecke.aron.vocabletrainer.activity;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.dialog.ProgressDialog;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Adapter.FileRecyclerAdapter;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Comparator.GenFileEntryComparator;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Comparator.GenericComparator;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.BasicFileEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.FileEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.ViewModel.FilePickerViewModel;

import static vocabletrainer.heinecke.aron.vocabletrainer.activity.MainActivity.PREFS_NAME;

/**
 * File activity for file requests<br>
 * <b>requires WRITE_EXTERNAL_STORAGE</b><br>
 * To be called as startActivityForResult
 * @author Aron Heinecke
 */
public class FileActivity extends AppCompatActivity implements FileRecyclerAdapter.ItemClickListener {
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
     * Param key for start file to be pre-selected<br>
     * Type: String
     */
    public static final String PARAM_START_FILE = "start_file";
    /**
     * Param key for write flag<br>
     * Pass as true to get a save-as activity, otherwise read file mode
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
    private RecyclerView recyclerView;
    private EditText tFileName;
    private TextView tCurrentDir;
    private Button bOk;

    private FileRecyclerAdapter adapter;
    private boolean write;
    private BasicFileEntry selectedEntry;
    private File selectedFile;
    private boolean sorting_name;
    private Comparator<BasicFileEntry> compName;
    private Comparator<BasicFileEntry> compSize;
    private FilePickerViewModel filePickerViewModel;
    private MenuItem menuItemUp;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        if(savedInstanceState != null){
            progressDialog = (ProgressDialog) getSupportFragmentManager().getFragment(savedInstanceState, ProgressDialog.TAG);
        }

        compName = new GenFileEntryComparator<>(new GenericComparator.ValueRetriever[] {
                GenFileEntryComparator.retType,GenFileEntryComparator.retName,
                GenFileEntryComparator.retSize
        });
        compSize = new GenFileEntryComparator<>(new GenericComparator.ValueRetriever[] {
                GenFileEntryComparator.retType,GenFileEntryComparator.retSize,
                GenFileEntryComparator.retName
        });

        filePickerViewModel = ViewModelProviders.of(this).get(FilePickerViewModel.class);

        filePickerViewModel.getErrorHandle().observe(this, error -> {
            if(error != null){
                Log.e(TAG,error);
                Snackbar.make(findViewById(R.id.contentView), error, Snackbar.LENGTH_LONG)
                        .show();
                filePickerViewModel.resetError();
                if(progressDialog != null && progressDialog.isAdded()){
                    progressDialog.dismiss();
                }
            }
        });

        TextView msg = findViewById(R.id.tFileMsg);
        tFileName = findViewById(R.id.tFileName);
        tCurrentDir = findViewById(R.id.tCurrentDir);
        bOk = findViewById(R.id.bFileOk);

        filePickerViewModel.getPathStringHandle().observe(this, path -> {
            if(path != null){
                tCurrentDir.setText(path);
            }
        });

        filePickerViewModel.getWritableDirHandle().observe(this, isWriteable -> {
            if(isWriteable != null){
                bOk.setEnabled(!write || isWriteable);
            }
        });

        Intent intent = getIntent();
        msg.setText(intent.getStringExtra(PARAM_MESSAGE));
        write = intent.getBooleanExtra(PARAM_WRITE_FLAG, false);
        filePickerViewModel.setWriteMode(write);

        tFileName.setVisibility(write ? View.VISIBLE : View.GONE);
        if(intent.hasExtra(PARAM_DEFAULT_FILENAME))
            tFileName.setText(intent.getStringExtra(PARAM_DEFAULT_FILENAME));

        initListView();
        filePickerViewModel.getViewListHandle().observe(this, list -> {
            if(list!= null){
                adapter.submitList(list,sorting_name ? compName : compSize);
                FileEntry preselectedElement = filePickerViewModel.getPreselectedElement();
                if(preselectedElement != null){
                    int position = adapter.getPositionOfElement(preselectedElement);
                    filePickerViewModel.resetPreselectedElement();
                    if(position > -1)
                        recyclerView.scrollToPosition(position);
                    setSelectedFile(preselectedElement);
                    adapter.setInitialSelectedElement(preselectedElement);
                }
                if(menuItemUp != null) {
                    menuItemUp.setEnabled(filePickerViewModel.isActionUpAllowed());
                    if(filePickerViewModel.isActionUpAllowed()) {
                        menuItemUp.getIcon().setColorFilter(null);
                    } else {
                        menuItemUp.getIcon().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
                    }
                }
                if(progressDialog != null && progressDialog.isAdded()){
                    progressDialog.dismiss();
                }
            }
        });
        if(savedInstanceState == null){
            String startPath = intent.getStringExtra(PARAM_START_FILE);
            if(startPath != null){
                File startFile = new File(startPath);
                tFileName.setText(startFile.getName());
                filePickerViewModel.goToFile(startFile.getParentFile(),startFile, this,true);
                return;
            }
        }
        loadStartDirectory(getSharedPreferences(PREFS_NAME,0));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file, menu);
        menuItemUp = menu.findItem(R.id.fMenu_Up);
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
            case R.id.fMenu_Up:
                if (!filePickerViewModel.isRunning()) {
                    showProgressDialog();
                    filePickerViewModel.goUp(this);
                }else{
                    displayLoadingToast();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Setup listview
     */
    private void initListView() {
        recyclerView = findViewById(R.id.listViewFiles);

        adapter = new FileRecyclerAdapter(new ArrayList<>(),this);
        adapter.setItemClickListener(this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);

        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        sorting_name = settings.getBoolean(P_KEY_FA_SORT, true);
    }

    /**
     * Does sorting<br>
     * Notifies data change
     */
    private void applySorting() {
        if(filePickerViewModel.isRunning()){
            Toast.makeText(this,R.string.File_Loading,Toast.LENGTH_SHORT).show();
            return;
        }
        adapter.updateSorting(sorting_name ? compName : compSize);
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
            selectedFile = write ? new File(filePickerViewModel.getCurrentFolder(), tFileName.getText().toString()) : ((FileEntry) selectedEntry).getFile();
            Log.d(TAG, "file:" + selectedFile.getAbsolutePath());
            if (write) {
                if (selectedFile.isDirectory()) { // required !?
                    selectedFile = null;
                    Snackbar.make(findViewById(R.id.contentView), R.string.File_Error_IsDir, Snackbar.LENGTH_LONG)
                            .show();
                    Log.d(TAG,"selection is directory");
                } else if (selectedFile.exists()) {
                    final AlertDialog.Builder alert = new AlertDialog.Builder(this);

                    alert.setTitle(R.string.File_Diag_exists_Title);
                    alert.setMessage(getString(R.string.File_Diag_MSG_part).replace("%f", selectedFile.getName()));

                    alert.setPositiveButton(R.string.File_Diag_btn_OK, (dialog, whichButton) -> useFile());

                    alert.setNegativeButton(R.string.File_Diag_btn_CANCEL, (dialog, whichButton) -> selectedFile = null);
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

    private void loadStartDirectory(SharedPreferences settings){
        File folder = new File(settings.getString(P_KEY_FA_LAST_DIR, ""));
        if(settings.contains(P_KEY_FA_LAST_FILENAME)){
            tFileName.setText(settings.getString(P_KEY_FA_LAST_FILENAME,""));
        }
        filePickerViewModel.goToFile(folder, null, this,true);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(progressDialog != null && progressDialog.isAdded())
            getSupportFragmentManager().putFragment(outState, ProgressDialog.TAG, progressDialog);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Save values
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(P_KEY_FA_LAST_FILENAME, tFileName.getText().toString());
        String folderPath = null;
        if(filePickerViewModel.getCurrentFolder() != null)
            folderPath = filePickerViewModel.getCurrentFolder().getAbsolutePath();
        editor.putString(P_KEY_FA_LAST_DIR, folderPath);
        editor.putBoolean(P_KEY_FA_SORT, sorting_name);
        editor.apply();
    }

    /**
     * Update UI to reflect file selection<br>
     *     Does not update recyclerview
     * @param entry
     */
    private void setSelectedFile(FileEntry entry){
        selectedEntry = entry;
        if (write) {
            tFileName.setText(entry.getName());
        }
        Boolean isWritable = filePickerViewModel.getWritableDirHandle().getValue();
        bOk.setEnabled(!write || (isWritable != null && isWritable) );
    }

    @Override
    public void onItemClick(View view, int position) {
        if (filePickerViewModel.isRunning()) {
            displayLoadingToast();
            return;
        }
        BasicFileEntry entry = adapter.getItem(position);
        if (entry.getTypeID() == BasicFileEntry.TYPE_FILE) {
            adapter.selectSingleEntry(position);
            setSelectedFile((FileEntry) entry);
        } else if (entry.getTypeID() == BasicFileEntry.TYPE_UP) {
            showProgressDialog();
            filePickerViewModel.goUp(this);
        } else {
            showProgressDialog();
            filePickerViewModel.goToFile(((FileEntry) entry).getFile(), null, this, false);
        }
    }

    /**
     * Display toast stating a currently loading task
     */
    private void displayLoadingToast() {
        Toast.makeText(this, R.string.File_Loading, Toast.LENGTH_SHORT).show();
    }

    /**
     * Display indeterminate progress dialog
     */
    private void showProgressDialog(){
        progressDialog = ProgressDialog.newInstance();
        progressDialog.setDisplayMode(true,0, R.string.File_Loading,null);
        if(!progressDialog.isAdded())
            progressDialog.show(getSupportFragmentManager(),ProgressDialog.TAG);
    }
}
