package vocabletrainer.heinecke.aron.vocabletrainer.lib.Import;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ProgressBar;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Function;

import static vocabletrainer.heinecke.aron.vocabletrainer.lib.CSVHeaders.CSV_METADATA_START;

/**
 * ImportFetcher class<br>
 * Does the basic parsing work, passes data to specified ImportHandler
 */
public class ImportFetcher extends AsyncTask<Integer, Integer, String> {
    private final static String TAG = "ImportFetcher";
    private final static int MAX_RECORD_SIZE = 3;
    private final static int MIN_RECORD_SIZE = MAX_RECORD_SIZE - 1;
    private final static int REC_V1 = 0;
    private final static int REC_V2 = 1;
    private final static int REC_V3 = 2;
    private final File source;
    private final CSVFormat format;
    private final ImportHandler handler;
    private final int maxEntries;
    private final AlertDialog dialog;
    private final ProgressBar progressBar;
    private final MessageProvider messageProvider;
    private final Function<Void,String> importCallback;
    private final boolean logErrors;
    private long lastUpdate = 0;
    private StringBuilder log;

    /**
     * Creates a new importer<br>
     *     It takes a dialog with a progressbar as input to display progress.
     *     Alternative you can use
     *
     * @param format      Format to use
     * @param source      Source for parsing
     * @param handler     Data handler
     * @param maxEntries  max amount of entries in this file<br>
     *                    set to > 0 to gain a "X/maxEntries" progress update
     * @param progressBar Progressbar updated when maxEntries is set, other the AlertDialog's Message is used<br>
     *                    Please not that the progressBar requires a style matching determinate / indeterminate mode
     * @param dialog      AlertDialog to show the progress on
     * @param messageProvider Message provider for logs
     * @param importCallback callback after successful import, given import log as param, return ignored
     * @param logErrors   disable error logging on false
     */
    ImportFetcher(final CSVFormat format, final File source, final ImportHandler handler,
                  final int maxEntries, final AlertDialog dialog, final ProgressBar progressBar,
                  final MessageProvider messageProvider, final Function<Void,String> importCallback,
                  final boolean logErrors) {
        this.source = source;
        this.format = format;
        this.handler = handler;
        this.maxEntries = maxEntries;
        this.progressBar = progressBar;
        this.dialog = dialog;
        this.log = new StringBuilder();
        this.messageProvider = messageProvider;
        this.importCallback = importCallback;
        this.logErrors = logErrors;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        try { // catch this, as display view change could lead to re-initializations
            if (maxEntries > 0) {
                progressBar.setProgress(values[0]);
            } else {
                dialog.setMessage(String.valueOf(values[0]));
            }
        } catch (Exception e) {
            Log.w(TAG, "unable to update progress");
        }
    }

    @Override
    protected void onPostExecute(String s) {
        dialog.dismiss();
        try {
            importCallback.function(s);
        } catch (Exception e){
            Log.e(TAG,"importCallback crash",e);
        }
    }

    @Override
    protected void onPreExecute() {
        progressBar.setIndeterminate(maxEntries <= 0);
        progressBar.setMax(maxEntries);
    }

    @Override
    protected void onCancelled() {
        dialog.cancel();
    }

    @Override
    protected String doInBackground(Integer... params) {
        try (
                FileReader reader = new FileReader(source);
                BufferedReader bufferedReader = new BufferedReader(reader);
                CSVParser parser = new CSVParser(bufferedReader, format)
        ) {
            handler.start();
            boolean tbl_start = false;
            final String empty_v3 = "";
            int i = 1;
            for (CSVRecord record : parser) {
                if (System.currentTimeMillis() - lastUpdate > 250) { // don't spam the UI thread
                    publishProgress(i);
                    lastUpdate = System.currentTimeMillis();
                }

                if (record.size() < MIN_RECORD_SIZE) { // ignore, not enough values
                    if(logErrors) {
                        Log.w(TAG, "ignoring entry, missing values: " + record.toString());
                        log.append(messageProvider.E_NOT_ENOUGH_VALUES);
                        log.append(record.toString());
                        log.append('\n');
                    }
                    continue;
                } else if (record.size() > MAX_RECORD_SIZE && logErrors) { // warn, too many values
                    Log.w(TAG, "entry longer then necessary: " + record.toString());
                    log.append(messageProvider.W_TOO_MANY_VALUES);
                    log.append(record.toString());
                    log.append('\n');
                }
                String v1 = record.get(REC_V1);
                String v2 = record.get(REC_V2);
                String v3 = record.size() < MAX_RECORD_SIZE ? empty_v3 : record.get(REC_V3);
                if (tbl_start) {
                    handler.newTable(v1, v2, v3);
                    tbl_start = false;
                } else if (tbl_start = (v1.equals(CSV_METADATA_START[0]) && v2.equals(CSV_METADATA_START[1]) && v3.equals(CSV_METADATA_START[2]))) {
                    //do nothing
                } else {
                    handler.newEntry(v1, v2, v3);
                }
                i++;
            }
            //prepend to start
            log.insert(0,messageProvider.formatIMPORTED_AMOUNT(i));
            parser.close();
            bufferedReader.close();
            reader.close();
        } catch (Exception e) {
            Log.e(TAG, "", e);
        } finally {
            handler.finish();
        }
        return log.toString();
    }

    /**
     * Message provider for the import log
     */
    public static class MessageProvider {
        public final String E_NOT_ENOUGH_VALUES;
        public final String W_TOO_MANY_VALUES;
        public final String I_IMPORTED_AMOUNT;

        /**
         * Creates a new MessageProvider
         * @param fragment Fragment for context resource retrieval
         */
        public MessageProvider(final Fragment fragment){
            E_NOT_ENOUGH_VALUES = fragment.getString(R.string.Import_Error_MIN_RECORD_SIZE);
            W_TOO_MANY_VALUES = fragment.getString(R.string.Import_Warn_MAX_RECORD_SIZE);
            I_IMPORTED_AMOUNT = fragment.getString(R.string.Import_Info_Import);
        }

        /**
         * Returns formated IMPORT_AMOUNT with param
         * @param amount
         * @return formated String
         */
        public String formatIMPORTED_AMOUNT(final int amount){
            return I_IMPORTED_AMOUNT.replace("%d",String.valueOf(amount)) + '\n';
        }
    }
}
