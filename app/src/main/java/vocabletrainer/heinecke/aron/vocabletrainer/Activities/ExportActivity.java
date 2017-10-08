package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.Activities.lib.TableListAdapter;
import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Comparator.GenTableComparator;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Comparator.GenericComparator;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Entry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.GenericSpinnerEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Table;

import static vocabletrainer.heinecke.aron.vocabletrainer.Activities.MainActivity.PREFS_NAME;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.CSVHeaders.CSV_METADATA_COMMENT;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.CSVHeaders.CSV_METADATA_START;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.ID_RESERVED_SKIP;

/**
 * Export activity
 */
public class ExportActivity extends AppCompatActivity {

    /**
     * This permission is required for this activity to work
     */
    public static final String REQUIRED_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String P_KEY_B_EXP_TBL_META = "export_tbl_meta";
    private static final String P_KEY_B_EXP_TBL_MULTI = "export_tbl_multi";
    private static final String P_KEY_I_EXP_FORMAT = "export_format";
    private static final int REQUEST_FILE_RESULT_CODE = 10;
    private static final int REQUEST_TABLES_RESULT_CODE = 20;
    private static final String TAG = "ExportActivity";
    private static final int MAX_PROGRESS = 100;
    private EditText tExportFile;
    private Button btnExport;
    private File expFile;
    private ListView listView;
    private FloatingActionButton addButton;
    private ArrayList<Table> tables;
    private TableListAdapter adapter;
    private CheckBox chkExportTalbeInfo;
    private CheckBox chkExportMultiple;
    private ExportOperation exportTask;
    private Spinner spFormat;
    private ArrayAdapter<GenericSpinnerEntry<CSVFormat>> spAdapterFormat;
    private TextView tMsg;
    private GenericComparator compTables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);
        setTitle(R.string.Export_Title);

        tExportFile = (EditText) findViewById(R.id.tExportFile);
        btnExport = (Button) findViewById(R.id.bExportStart);
        listView = (ListView) findViewById(R.id.lExportListView);
        addButton = (FloatingActionButton) findViewById(R.id.bExportAddTables);
        chkExportMultiple = (CheckBox) findViewById(R.id.chkExportMulti);
        chkExportTalbeInfo = (CheckBox) findViewById(R.id.chkExportMeta);
        spFormat = (Spinner) findViewById(R.id.spExpFormat);
        tMsg = (TextView) findViewById(R.id.tExportMsg);

        GenericComparator.ValueRetriever[] retrievers = new GenericComparator.ValueRetriever[]{
                GenTableComparator.retName,GenTableComparator.retA,GenTableComparator.retB
        };

        compTables = new GenTableComparator(retrievers,ID_RESERVED_SKIP);

        initView();
    }

    /**
     * Init list view
     */
    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        tMsg.setMovementMethod(LinkMovementMethod.getInstance());
        tExportFile.setKeyListener(null);
        btnExport.setEnabled(false);
        tables = new ArrayList<>();
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runSelectTables();
            }
        });
        chkExportMultiple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInputOk();
            }
        });

        adapter = new TableListAdapter(this, R.layout.table_list_view, tables, false);
        listView.setAdapter(adapter);
        listView.setLongClickable(false);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                runSelectTables();
            }
        });

        spAdapterFormat = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);

        spAdapterFormat.add(new GenericSpinnerEntry<>(CSVFormat.DEFAULT, getString(R.string.CSV_Format_Default)));
        spAdapterFormat.add(new GenericSpinnerEntry<>(CSVFormat.EXCEL, getString(R.string.CSV_Format_EXCEL)));
        spAdapterFormat.add(new GenericSpinnerEntry<>(CSVFormat.RFC4180, getString(R.string.CSV_Format_RFC4180)));
        spAdapterFormat.add(new GenericSpinnerEntry<>(CSVFormat.TDF, getString(R.string.CSV_Format_Tabs)));

        spFormat.setAdapter(spAdapterFormat);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        chkExportTalbeInfo.setChecked(settings.getBoolean(P_KEY_B_EXP_TBL_META, true));
        chkExportMultiple.setChecked(settings.getBoolean(P_KEY_B_EXP_TBL_MULTI, true));
        spFormat.setSelection(settings.getInt(P_KEY_I_EXP_FORMAT, 0));
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(P_KEY_B_EXP_TBL_MULTI, chkExportMultiple.isChecked());
        editor.putBoolean(P_KEY_B_EXP_TBL_META, chkExportTalbeInfo.isChecked());
        editor.putInt(P_KEY_I_EXP_FORMAT, spFormat.getSelectedItemPosition());
        editor.apply();
    }

    /**
     * Called on cancel button click
     *
     * @param view
     */
    public void onCancel(View view) {
        finish();
    }

    /**
     * Called on file select click
     *
     * @param view
     */
    public void selectFile(View view) {
        Intent myIntent = new Intent(this, FileActivity.class);
        myIntent.putExtra(FileActivity.PARAM_WRITE_FLAG, true);
        myIntent.putExtra(FileActivity.PARAM_MESSAGE, getString(R.string.Export_File_select_Info));
        myIntent.putExtra(FileActivity.PARAM_DEFAULT_FILENAME, "list.csv");
        startActivityForResult(myIntent, REQUEST_FILE_RESULT_CODE);
    }

    /**
     * Called on table select click
     *
     * @param view
     */
    public void onSelectTables(View view) {
        runSelectTables();
    }

    /**
     * Calls select tables activity
     */
    private void runSelectTables() {
        Intent myIntent = new Intent(this, ListActivity.class);
        myIntent.putExtra(ListActivity.PARAM_SELECTED, tables);
        myIntent.putExtra(ListActivity.PARAM_MULTI_SELECT, true);
        startActivityForResult(myIntent, REQUEST_TABLES_RESULT_CODE);
    }

    /**
     * Called upon ok press
     *
     * @param view
     */
    public void onOk(View view) {

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setCancelable(false);
        alert.setTitle(R.string.Export_Exporting_Title);
        final ProgressBar pg = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pg.setIndeterminate(false);
        LinearLayout rl = new TableLayout(this);
        rl.addView(pg);
        alert.setView(rl);
        /*alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //TODO: add cancel option
            }
        });*/
        final AlertDialog dialog = alert.show();
        CSVFormat format = spAdapterFormat.getItem(spFormat.getSelectedItemPosition()).getObject();
        ExportStorage es = new ExportStorage(format, tables, chkExportTalbeInfo.isChecked(), chkExportMultiple.isChecked(), expFile, dialog, pg);
        exportTask = new ExportOperation(es);
        exportTask.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_FILE_RESULT_CODE) {
                Log.d(TAG, "got file:" + data.getStringExtra(FileActivity.RETURN_FILE_USER_NAME));
                expFile = (File) data.getSerializableExtra(FileActivity.RETURN_FILE);
                tExportFile.setText(data.getStringExtra(FileActivity.RETURN_FILE_USER_NAME));
                checkInputOk();
            } else if (requestCode == REQUEST_TABLES_RESULT_CODE) {
                adapter.setAllUpdated((ArrayList<Table>) data.getSerializableExtra(ListActivity.RETURN_LISTS),compTables);
                checkInputOk();
            }
        }
    }

    /**
     * Validate input & set export button accordingly
     */
    private void checkInputOk() {
        btnExport.setEnabled(tables.size() > 1 && expFile != null && (chkExportMultiple.isChecked() || (!chkExportMultiple.isChecked() && tables.size() == 2)));
    }

    /**
     * Export async task class
     */
    private class ExportOperation extends AsyncTask<Integer, Integer, String> {
        private final ExportStorage es;
        private final Database db;

        /**
         * Creates a new ExportOperation
         *
         * @param es
         */
        public ExportOperation(ExportStorage es) {
            this.es = es;
            db = new Database(getApplicationContext());
        }

        @Override
        protected String doInBackground(Integer... params) {
            Log.d(TAG, "Starting background task");
            try (FileWriter fw = new FileWriter(es.file);
                 //TODO: enforce UTF-8
                 BufferedWriter writer = new BufferedWriter(fw);
                 CSVPrinter printer = new CSVPrinter(writer, es.format)
            ) {
                int i = 0;
                for (Table tbl : es.tables) {
                    if (tbl.getId() == ID_RESERVED_SKIP) {
                        continue;
                    }
                    Log.d(TAG, "exporting tbl " + tbl.toString());
                    if (es.exportTableInfo) {
                        printer.printRecord(CSV_METADATA_START);
                        printer.printComment(CSV_METADATA_COMMENT);
                        printer.print(tbl.getName());
                        printer.print(tbl.getNameA());
                        printer.print(tbl.getNameB());
                        printer.println();
                    }
                    List<Entry> vocables = db.getVocablesOfTable(tbl);

                    for (Entry ent : vocables) {
                        printer.print(ent.getAWord());
                        printer.print(ent.getBWord());
                        printer.print(ent.getTip());
                        printer.println();
                    }
                    i++;
                    publishProgress((es.tables.size() / MAX_PROGRESS) * i);
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
            es.progressBar.setMax(tables.size());
        }

        @Override
        protected void onPostExecute(String result) {
            es.dialog.dismiss();
            finish();
        }
    }

    /**
     * Export storage class
     */
    private class ExportStorage {
        public final ArrayList<Table> tables;
        public final boolean exportTableInfo;
        public final boolean exportMultiple;
        public final File file;
        public final AlertDialog dialog;
        public final CSVFormat format;
        public final ProgressBar progressBar;

        /**
         * New export storage
         *
         * @param format          CSV format to use
         * @param tables          table to export
         * @param exportTableInfo setting
         * @param exportMultiple  setting
         * @param file            file to read from
         * @param dialog          dialog for progress, closed on end
         * @param progressBar     progress bar that is updated
         */
        public ExportStorage(CSVFormat format, ArrayList<Table> tables, boolean exportTableInfo,
                             boolean exportMultiple, File file, AlertDialog dialog, ProgressBar progressBar) {
            this.format = format;
            this.tables = tables;
            this.exportTableInfo = exportTableInfo;
            this.exportMultiple = exportMultiple;
            this.file = file;
            this.dialog = dialog;
            this.progressBar = progressBar;
        }
    }
}
