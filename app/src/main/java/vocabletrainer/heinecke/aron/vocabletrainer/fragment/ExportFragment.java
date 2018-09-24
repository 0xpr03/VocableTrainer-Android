package vocabletrainer.heinecke.aron.vocabletrainer.fragment;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.activity.ExImportActivity;
import vocabletrainer.heinecke.aron.vocabletrainer.activity.FileActivity;
import vocabletrainer.heinecke.aron.vocabletrainer.dialog.ProgressDialog;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.CSVCustomFormat;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Comparator.GenTableComparator;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Comparator.GenericComparator;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.GenericSpinnerEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.ViewModel.ExportViewModel;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.ViewModel.FormatViewModel;

import static vocabletrainer.heinecke.aron.vocabletrainer.activity.MainActivity.PREFS_NAME;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.ID_RESERVED_SKIP;

/**
 * Exporter fragment
 */
public class ExportFragment extends PagerFragment {

    private static final String P_KEY_B_EXP_TBL_META = "export_tbl_meta";
    private static final String P_KEY_B_EXP_TBL_MULTI = "export_tbl_multi";
    private static final String P_KEY_I_EXP_FORMAT = "export_format";
    private static final int REQUEST_FILE_RESULT_CODE = 10;
    private static final String KEY_FILE_PATH = "filePath";
    public static final String TAG = "ExportFragment";
    private EditText tExportFile;
    private Button btnExport;
    private File expFile;
    private CheckBox chkExportTableInfo;
    private CheckBox chkExportMultiple;
    private Spinner spFormat;
    private ArrayAdapter<GenericSpinnerEntry<CSVCustomFormat>> spAdapterFormat;
    private TextView tMsg;
    private View view;
    private boolean formatWarnDialog = false; // prevent dialog double trigger, due to spFormat logic
    private static final ArrayList<VList> EMPTY_LISTS = new ArrayList<>(0);
    private ExImportActivity activity;
    private GenericSpinnerEntry<CSVCustomFormat> customFormatEntry;
    private FormatViewModel formatViewModel;
    private ProgressDialog progressDialog;
    private ExportViewModel exportViewModel;

    /**
     * Required interface for attachers of this class
     */
    public interface ExportListProvider {
        /**
         * Get list of VLists to export
         * @return
         */
        ArrayList<VList> getExportLists();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        formatViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(FormatViewModel.class);
        formatViewModel.getCustomFormatLD().observe(this, format -> {
            customFormatEntry.updateObject(format);
        });
        formatViewModel.getInFormatFragmentLD().observe(this, inFragment -> {
            //noinspection ConstantConditions
            if(!inFragment) {
                checkInputOk();
            }
        });

        if(savedInstanceState != null){
            progressDialog = (ProgressDialog) getACActivity().getSupportFragmentManager().getFragment(savedInstanceState, ProgressDialog.TAG);
        }

        exportViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(ExportViewModel.class);
        exportViewModel.getExportingHandles().observe(this, exporting -> {
            if(exporting!= null){
                if(exporting){
                    if(progressDialog == null){
                        progressDialog = ProgressDialog.newInstance();
                    }
                    progressDialog.setDisplayMode(false,exportViewModel.getExportSize(),R.string.Export_Exporting_Title);
                    progressDialog.setProgressHandle(exportViewModel.getProgressExportHandle());

                    if(!progressDialog.isAdded())
                        progressDialog.show(getACActivity().getSupportFragmentManager(),ProgressDialog.TAG);
                } else if(progressDialog != null && progressDialog.isAdded()) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_export, container, false);
        formatWarnDialog = false;
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.Export_Title);

        tExportFile = (EditText) view.findViewById(R.id.tExportFile);
        btnExport = (Button) view.findViewById(R.id.bExportStart);
        chkExportMultiple = (CheckBox) view.findViewById(R.id.chkExportMulti);
        chkExportTableInfo = (CheckBox) view.findViewById(R.id.chkExportMeta);
        spFormat = (Spinner) view.findViewById(R.id.spExpFormat);
        tMsg = (TextView) view.findViewById(R.id.tExportMsg);

        GenericComparator.ValueRetriever[] retrievers = new GenericComparator.ValueRetriever[]{
                GenTableComparator.retName, GenTableComparator.retA, GenTableComparator.retB
        };

        initView(savedInstanceState);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        view = null;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        ActionBar ab = getFragmentActivity().getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        } else {
            Log.w(TAG, "actionbar doesn't exist");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(getActivity() instanceof ExImportActivity) {
            activity = (ExImportActivity) getActivity();
        } else {
            throw new ClassCastException("parent Activity has to be ExImportActivity, is "+getActivity().getClass());
        }
    }

    /**
     * Init list view
     * @param savedInstanceState
     */
    private void initView(@Nullable Bundle savedInstanceState) {
        tMsg.setMovementMethod(LinkMovementMethod.getInstance());
        tExportFile.setKeyListener(null);
        btnExport.setEnabled(false);
        if(savedInstanceState != null) {
            String path = savedInstanceState.getString(KEY_FILE_PATH, null);
            if (path != null && !path.equals(""))
                expFile = new File(path);
            else
                expFile = null;
        }
        Button btnExport = (Button) view.findViewById(R.id.bExportStart);
        btnExport.setOnClickListener(v -> onExport());

        Button btnFileDialog = (Button) view.findViewById(R.id.bExportSelFile);
        btnFileDialog.setOnClickListener(v -> selectFile());

        spAdapterFormat = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        spAdapterFormat.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);

        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);

        customFormatEntry = activity.populateFormatSpinnerAdapter(spAdapterFormat);

        spFormat.setAdapter(spAdapterFormat);

        chkExportTableInfo.setChecked(settings.getBoolean(P_KEY_B_EXP_TBL_META, true));
        chkExportTableInfo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            chkExportMultiple.setEnabled(isChecked);
            checkInputOk();
        });
        chkExportMultiple.setChecked(settings.getBoolean(P_KEY_B_EXP_TBL_MULTI, true));
        chkExportMultiple.setOnCheckedChangeListener((buttonView, isChecked) -> checkInputOk());
        spFormat.setSelection(settings.getInt(P_KEY_I_EXP_FORMAT, 0));
        chkExportMultiple.setEnabled(chkExportTableInfo.isChecked());
        spFormat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG,"selected something");
                checkInputOk();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG,"nothing selected");
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(P_KEY_B_EXP_TBL_MULTI, chkExportMultiple.isChecked());
        editor.putBoolean(P_KEY_B_EXP_TBL_META, chkExportTableInfo.isChecked());
        editor.putInt(P_KEY_I_EXP_FORMAT, spFormat.getSelectedItemPosition());
        editor.apply();
    }

    /**
     * Called on file select click
     */
    public void selectFile() {
        Intent myIntent = new Intent(getActivity(), FileActivity.class);
        myIntent.putExtra(FileActivity.PARAM_WRITE_FLAG, true);
        myIntent.putExtra(FileActivity.PARAM_MESSAGE, getString(R.string.Export_File_select_Info));
        myIntent.putExtra(FileActivity.PARAM_DEFAULT_FILENAME, "list.csv");
        startActivityForResult(myIntent, REQUEST_FILE_RESULT_CODE);
    }

    private ArrayList<VList> getLists(){
        ArrayList<VList> list = EMPTY_LISTS;
        if(activity != null) {
            return activity.getExportLists();
        } else {
            Log.wtf(TAG,"No ListPickerFragment found!");
        }
        Log.d(TAG,"List size: "+list.size());
        return list;
    }

    /**
     * Called upon ok press
     */
    public void onExport() {
        CSVCustomFormat format = getCFormat();
        ExportStorage es = new ExportStorage(format, getLists(), chkExportTableInfo.isChecked(), chkExportMultiple.isChecked(), expFile);
        exportViewModel.runImport(getContext(),es);
    }

    /**
     * Helper to return the currently selected CSVCustomFormat
     * @return selected CSVCustomFormat
     */
    private CSVCustomFormat getCFormat() {
        return spAdapterFormat.getItem(spFormat.getSelectedItemPosition()).getObject();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_FILE_PATH,expFile != null ? expFile.getAbsolutePath() : "");
        if(progressDialog != null && progressDialog.isAdded())
            getACActivity().getSupportFragmentManager().putFragment(outState, ProgressDialog.TAG, progressDialog);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_FILE_RESULT_CODE:
                    Log.d(TAG, "got file:" + data.getStringExtra(FileActivity.RETURN_FILE_USER_NAME));
                    expFile = (File) data.getSerializableExtra(FileActivity.RETURN_FILE);
                    tExportFile.setText(data.getStringExtra(FileActivity.RETURN_FILE_USER_NAME));
                    checkInputOk();
                    break;
            }
        }
    }

    /**
     * Validate input & set export button accordingly
     */
    private void checkInputOk() {
        Log.d(TAG,"checking input");
        boolean exportFormatOk = getCFormat().isMultiValueEnabled();
        int listSize = getLists().size();
        Log.d(TAG,"list size:"+listSize);
        // disabled = (!export tableinfo || !export multilist) && listsize > 1
        // enabled = !disabled <==> !(!export table info || !exprtmultilist) || list size > 1)
        // <==> (export table info && exportmultulst) || list size == 1
        btnExport.setEnabled(exportFormatOk && listSize > 0 && expFile != null && ((chkExportTableInfo.isChecked() && chkExportMultiple.isChecked()) || listSize == 1));
        if(!exportFormatOk && !formatWarnDialog) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setCancelable(true);
            alert.setTitle(R.string.Export_Error_Format_Multivalue_Title);
            alert.setMessage(R.string.Export_Error_Format_Multivalue_Text);
            alert.setNeutralButton(R.string.GEN_Ok, (dialogInterface, i) -> {
                formatWarnDialog = false; // clear state
            });
            alert.show();
            formatWarnDialog = true;
        }
    }

    @Override
    protected void onFragmentVisible() {
        super.onFragmentVisible();
        onResume();
        checkInputOk();
    }
    /**
     * Exporter storage class
     */
    public class ExportStorage {
        public final ArrayList<VList> lists;
        public final boolean exportTableInfo;
        public final boolean exportMultiple;
        public final File file;
        public final CSVCustomFormat cFormat;

        /**
         * New export storage
         *
         * @param cFormat          CSV cFormat to use
         * @param lists          table to export
         * @param exportTableInfo setting
         * @param exportMultiple  setting
         * @param file            file to read from
         */
        ExportStorage(CSVCustomFormat cFormat, ArrayList<VList> lists, boolean exportTableInfo,
                      boolean exportMultiple, File file) {
            this.cFormat = cFormat;
            this.lists = lists;
            this.exportTableInfo = exportTableInfo;
            this.exportMultiple = exportMultiple;
            this.file = file;
        }
    }
}
