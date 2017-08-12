package vocabletrainer.heinecke.aron.vocabletrainer.lib.Import;

import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ProgressBar;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.Callable;

import static vocabletrainer.heinecke.aron.vocabletrainer.lib.CSVHeaders.CSV_METADATA_START;

/**
 * ImportFetcher class<br>
 * Does the basic parsing work, passes data to specified ImportHandler
 */
public class ImportFetcher extends AsyncTask<Integer, Integer, Integer> {
    private final static String TAG = "ImportFetcher";
    private final static int RECORD_SIZE = 3;
    private final static int MIN_RECORD_SIZE = RECORD_SIZE - 1;
    private final static int REC_V1 = 0;
    private final static int REC_V2 = 1;
    private final static int REC_V3 = 2;
    private final Callable<Void> callable;
    private final File source;
    private final CSVFormat format;
    private final ImportHandler handler;
    private final int maxEntries;
    private final AlertDialog dialog;
    private final ProgressBar progressBar;
    private long lastUpdate = 0;

    /**
     * Creates a new importer
     *
     * @param format      Format to use
     * @param source      Source for parsing
     * @param handler     Data handler
     * @param maxEntries  max amount of entries in this file<br>
     *                    set to > 0 to gain a "X/maxEntries" progress update
     * @param progressBar Progressbar updated when maxEntries is set, other the AlertDialog's Message is used<br>
     *                    Please not that the progressBar requires a style matching determinate / indeterminate mode
     * @param dialog      AlertDialog to show the progress on
     * @param callable    Function to call after task has finished
     */
    public ImportFetcher(final CSVFormat format, final File source, final ImportHandler handler,
                         final int maxEntries, final AlertDialog dialog, final ProgressBar progressBar, final Callable<Void> callable) {
        this.source = source;
        this.format = format;
        this.handler = handler;
        this.maxEntries = maxEntries;
        this.progressBar = progressBar;
        this.dialog = dialog;
        this.callable = callable;
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
    protected void onPostExecute(Integer s) {
        runCallable();
        dialog.dismiss();
    }

    @Override
    protected void onPreExecute() {
        progressBar.setIndeterminate(maxEntries <= 0);
        progressBar.setMax(maxEntries);
    }

    /**
     * Run callable
     */
    private void runCallable() {
        try {
            callable.call();
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    @Override
    protected void onCancelled(Integer s) {
        dialog.cancel();
    }

    @Override
    protected Integer doInBackground(Integer... params) {
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

                if (record.size() < MIN_RECORD_SIZE) {
                    Log.w(TAG, "ignoring entry, missing values: " + record.toString());
                    continue;
                } else if (record.size() > RECORD_SIZE) {
                    Log.w(TAG, "entry longer then necessary: " + record.toString());
                }
                String v1 = record.get(REC_V1);
                String v2 = record.get(REC_V2);
                String v3 = record.size() < RECORD_SIZE ? empty_v3 : record.get(REC_V3);
                if (tbl_start) {
                    handler.newTable(v1, v2, v3);
                    tbl_start = false;
                } else if (tbl_start = v1.equals(CSV_METADATA_START[0]) && v2.equals(CSV_METADATA_START[1]) && v3.equals(CSV_METADATA_START[2])) {
                    //do nothing
                } else {
                    handler.newEntry(v1, v2, v3);
                }
                i++;
            }
            parser.close();
            bufferedReader.close();
            reader.close();
        } catch (Exception e) {
            Log.e(TAG, "", e);
        } finally {
            handler.finish();
        }
        return null;
    }
}
