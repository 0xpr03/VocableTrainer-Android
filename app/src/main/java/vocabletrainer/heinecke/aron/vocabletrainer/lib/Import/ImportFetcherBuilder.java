package vocabletrainer.heinecke.aron.vocabletrainer.lib.Import;

import android.support.v7.app.AlertDialog;
import android.widget.ProgressBar;

import java.io.File;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.CSVCustomFormat;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Function;

/**
 * ImportFetcher builder<br>
 *     because ImportFetcher requires too many parameters
 */
public class ImportFetcherBuilder {
    private CSVCustomFormat format;
    private File source;
    private ImportHandler handler;
    private int maxEntries;
    private AlertDialog dialog;
    private ProgressBar progressBar;
    private ImportFetcher.MessageProvider messageProvider;
    private Function<Void,String> importCallback;
    private boolean logErrors = true;

    public ImportFetcherBuilder setFormat(CSVCustomFormat format) {
        this.format = format;
        return this;
    }

    public ImportFetcherBuilder setSource(File source) {
        this.source = source;
        return this;
    }

    public ImportFetcherBuilder setHandler(ImportHandler handler) {
        this.handler = handler;
        return this;
    }

    public ImportFetcherBuilder setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
        return this;
    }

    public ImportFetcherBuilder setDialog(AlertDialog dialog) {
        this.dialog = dialog;
        return this;
    }

    public ImportFetcherBuilder setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
        return this;
    }

    public ImportFetcherBuilder setMessageProvider(ImportFetcher.MessageProvider messageProvider) {
        this.messageProvider = messageProvider;
        return this;
    }

    public ImportFetcherBuilder setImportCallback(Function<Void,String> importCallback) {
        this.importCallback = importCallback;
        return this;
    }

    public ImportFetcherBuilder setLogErrors(boolean logErrors) {
        this.logErrors = logErrors;
        return this;
    }

    /**
     * Finalize builder, create ImportFetcher
     * @return ImportFetcher
     */
    public ImportFetcher createImportFetcher() {
        if(format == null || source == null || handler == null || dialog == null
                || progressBar == null || messageProvider == null || importCallback == null)
            throw new IllegalArgumentException();
        return new ImportFetcher(format, source, handler, maxEntries, dialog, progressBar,
                messageProvider,importCallback,logErrors);
    }
}