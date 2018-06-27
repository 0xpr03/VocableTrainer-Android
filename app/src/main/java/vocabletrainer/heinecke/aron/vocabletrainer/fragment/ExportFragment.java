package vocabletrainer.heinecke.aron.vocabletrainer.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.activity.FileActivity;
import vocabletrainer.heinecke.aron.vocabletrainer.activity.ListActivity;
import vocabletrainer.heinecke.aron.vocabletrainer.activity.lib.TableListAdapter;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.CSVCustomFormat;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Comparator.GenTableComparator;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Comparator.GenericComparator;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.MultiMeaningHandler;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.GenericSpinnerEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;

import static vocabletrainer.heinecke.aron.vocabletrainer.activity.ExImportActivity.populateFormatSpinnerAdapter;
import static vocabletrainer.heinecke.aron.vocabletrainer.activity.MainActivity.PREFS_NAME;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.CSVHeaders.CSV_METADATA_COMMENT;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.CSVHeaders.CSV_METADATA_START;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.ID_RESERVED_SKIP;

/**
 * Export fragment
 */
public class ExportFragment extends BaseFragment {

    private static final String P_KEY_B_EXP_TBL_META = "export_tbl_meta";
    private static final String P_KEY_B_EXP_TBL_MULTI = "export_tbl_multi";
    private static final String P_KEY_I_EXP_FORMAT = "export_format";
    private static final int REQUEST_FILE_RESULT_CODE = 10;
    private static final int REQUEST_TABLES_RESULT_CODE = 20;
    private static final String KEY_FILE_PATH = "filePath";
    private static final String KEY_LIST_EXP = "list";
    private static final String TAG = "ExportFragment";
    private static final int MAX_PROGRESS = 100;
    private EditText tExportFile;
    private Button btnExport;
    private File expFile;
    private ListView listView;
    private FloatingActionButton addButton;
    private ArrayList<VList> lists;
    private TableListAdapter adapter;
    private CheckBox chkExportTalbeInfo;
    private CheckBox chkExportMultiple;
    private ExportOperation exportTask;
    private Spinner spFormat;
    private ArrayAdapter<GenericSpinnerEntry<CSVCustomFormat>> spAdapterFormat;
    private TextView tMsg;
    private GenericComparator compTables;
    private View view;
    private boolean formatWarnDialog = false; // prevent dialog double trigger, due to spFormat logic

    private boolean showedCustomFormatFragment = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_export, container, false);
        formatWarnDialog = false;
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.Export_Title);

        tExportFile = (EditText) view.findViewById(R.id.tExportFile);
        btnExport = (Button) view.findViewById(R.id.bExportStart);
        listView = (ListView) view.findViewById(R.id.lExportListView);
        addButton = (FloatingActionButton) view.findViewById(R.id.bExportAddTables);
        chkExportMultiple = (CheckBox) view.findViewById(R.id.chkExportMulti);
        chkExportTalbeInfo = (CheckBox) view.findViewById(R.id.chkExportMeta);
        spFormat = (Spinner) view.findViewById(R.id.spExpFormat);
        tMsg = (TextView) view.findViewById(R.id.tExportMsg);

        GenericComparator.ValueRetriever[] retrievers = new GenericComparator.ValueRetriever[]{
                GenTableComparator.retName, GenTableComparator.retA, GenTableComparator.retB
        };

        compTables = new GenTableComparator(retrievers, ID_RESERVED_SKIP);

        initView(savedInstanceState);
        return view;
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "destroying view");
        super.onDestroyView();
        view = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, "inflating the menu");
        menu.clear();
        inflater.inflate(R.menu.exp_import, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
            case R.id.tCustomFormat:
                showedCustomFormatFragment = true;
                FormatFragment formatFragment = new FormatFragment();
                getFragmentActivity().addFragment(this,formatFragment);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
            lists = savedInstanceState.getParcelableArrayList(KEY_LIST_EXP);
            String path = savedInstanceState.getString(KEY_FILE_PATH, null);
            if (path != null && !path.equals(""))
                expFile = new File(path);
            else
                expFile = null;
        } else {
            lists = new ArrayList<>();
        }
        addButton.setOnClickListener(v -> runSelectTables());
        chkExportMultiple.setOnClickListener(v -> checkInputOk());

        Button btnOk = (Button) view.findViewById(R.id.bExportStart);
        btnOk.setOnClickListener(v -> onOk());

        Button btnFileDialog = (Button) view.findViewById(R.id.bExportSelFile);
        btnFileDialog.setOnClickListener(v -> selectFile());

        adapter = new TableListAdapter(getActivity(), R.layout.table_list_view, lists, false);
        listView.setAdapter(adapter);
        listView.setLongClickable(false);
        listView.setOnItemClickListener((parent, view, position, id) -> runSelectTables());

        spAdapterFormat = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);

        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);

        populateFormatSpinnerAdapter(spAdapterFormat, getActivity(), settings);

        spFormat.setAdapter(spAdapterFormat);

        chkExportTalbeInfo.setChecked(settings.getBoolean(P_KEY_B_EXP_TBL_META, true));
        chkExportMultiple.setChecked(settings.getBoolean(P_KEY_B_EXP_TBL_MULTI, true));
        spFormat.setSelection(settings.getInt(P_KEY_I_EXP_FORMAT, 0));
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
        editor.putBoolean(P_KEY_B_EXP_TBL_META, chkExportTalbeInfo.isChecked());
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

    /**
     * Calls select lists activity
     */
    private void runSelectTables() {
        Intent myIntent = new Intent(getActivity(), ListActivity.class);
        myIntent.putExtra(ListActivity.PARAM_SELECTED, lists);
        myIntent.putExtra(ListActivity.PARAM_MULTI_SELECT, true);
        startActivityForResult(myIntent, REQUEST_TABLES_RESULT_CODE);
    }

    /**
     * Called upon ok press
     */
    public void onOk() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setCancelable(false);
        alert.setTitle(R.string.Export_Exporting_Title);
        final ProgressBar pg = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleHorizontal);
        pg.setIndeterminate(false);
        LinearLayout rl = new TableLayout(getActivity());
        rl.addView(pg);
        alert.setView(rl);
        /*alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //TODO: add cancel option
            }
        });*/
        final AlertDialog dialog = alert.show();
        CSVCustomFormat format = getCFormat();
        ExportStorage es = new ExportStorage(format, lists, chkExportTalbeInfo.isChecked(), chkExportMultiple.isChecked(), expFile, dialog, pg);
        exportTask = new ExportOperation(es);
        exportTask.execute();
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
        outState.putParcelableArrayList(KEY_LIST_EXP,lists);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (showedCustomFormatFragment) {
            showedCustomFormatFragment = false;
            SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
            populateFormatSpinnerAdapter(spAdapterFormat, getActivity(), settings);
        }
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
                case REQUEST_TABLES_RESULT_CODE:
                    adapter.setAllUpdated(data.getParcelableArrayListExtra(ListActivity.RETURN_LISTS), compTables);
                    checkInputOk();
                    break;
            }
        }
    }

    /**
     * Validate input & set export button accordingly
     */
    private void checkInputOk() {
        boolean exportFormatOk = getCFormat().isMultiValueEnabled();
        btnExport.setEnabled(exportFormatOk && lists.size() > 1 && expFile != null && (chkExportMultiple.isChecked() || (!chkExportMultiple.isChecked() && lists.size() == 2)));
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

    /**
     * Export async task class
     */
    private class ExportOperation extends AsyncTask<Integer, Integer, String> {
        private final ExportStorage es;
        private final Database db;
        String multiMeaningDelimiter;
        String escapedChar;

        /**
         * Creates a new ExportOperation
         *
         * @param es
         */
        ExportOperation(ExportStorage es) {
            this.es = es;
            db = new Database(getActivity().getApplicationContext());
            multiMeaningDelimiter = String.valueOf(es.cFormat.getMultiValueChar());
            escapedChar = multiMeaningDelimiter + multiMeaningDelimiter;
        }

        @Override
        protected String doInBackground(Integer... params) {
            Log.d(TAG, "Starting background task");
            try (FileWriter fw = new FileWriter(es.file);
                 BufferedWriter writer = new BufferedWriter(fw);
                 CSVPrinter printer = new CSVPrinter(writer, es.cFormat.getFormat())
            ) {
                MultiMeaningHandler handler = new MultiMeaningHandler(es.cFormat);
                int i = 0;
                for (VList tbl : es.lists) {
                    if (tbl.getId() == ID_RESERVED_SKIP) {
                        continue;
                    }
                    Log.d(TAG, "exporting tbl " + tbl.toString());
                    if (es.exportTableInfo) {
                        printer.printRecord((Object[]) CSV_METADATA_START);
                        printer.printComment(CSV_METADATA_COMMENT);
                        printer.print(tbl.getName());
                        printer.print(tbl.getNameA());
                        printer.print(tbl.getNameB());
                        printer.println();
                    }
                    List<VEntry> vocables = db.getVocablesOfTable(tbl);

                    for (VEntry ent : vocables) {
                        List<String> mA = ent.getAMeanings();
                        List<String> mB = ent.getBMeanings();
                        printer.print(handler.formatMultiMeaning(mA));
                        printer.print(handler.formatMultiMeaning(mB));
                        printer.print(ent.getTip());
                        printer.print(ent.getAddition());
                        printer.println();
                    }
                    i++;
                    publishProgress((es.lists.size() / MAX_PROGRESS) * i);
                }
                Log.d(TAG, "closing all");
                printer.close();
                writer.close();
                fw.close();
            } catch (Exception e) {
                Log.wtf(TAG, e);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Log.d(TAG, "updating progress");
            es.progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPreExecute() {
            es.progressBar.setMax(lists.size());
        }

        @Override
        protected void onPostExecute(String result) {
            es.dialog.dismiss();
            getActivity().finish();
        }
    }

    /**
     * Export storage class
     */
    private class ExportStorage {
        final ArrayList<VList> lists;
        final boolean exportTableInfo;
        final boolean exportMultiple;
        final File file;
        final AlertDialog dialog;
        final CSVCustomFormat cFormat;
        final ProgressBar progressBar;

        /**
         * New export storage
         *
         * @param cFormat          CSV cFormat to use
         * @param lists          table to export
         * @param exportTableInfo setting
         * @param exportMultiple  setting
         * @param file            file to read from
         * @param dialog          dialog for progress, closed on end
         * @param progressBar     progress bar that is updated
         */
        ExportStorage(CSVCustomFormat cFormat, ArrayList<VList> lists, boolean exportTableInfo,
                      boolean exportMultiple, File file, AlertDialog dialog, ProgressBar progressBar) {
            this.cFormat = cFormat;
            this.lists = lists;
            this.exportTableInfo = exportTableInfo;
            this.exportMultiple = exportMultiple;
            this.file = file;
            this.dialog = dialog;
            this.progressBar = progressBar;
        }
    }
}
