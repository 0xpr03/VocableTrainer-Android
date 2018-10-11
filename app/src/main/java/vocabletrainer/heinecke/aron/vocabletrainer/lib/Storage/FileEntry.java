package vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage;

import java.io.File;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Formatter;

/**
 * File VEntry holding also a real file
 */
public class FileEntry extends BasicFileEntry {
    private final File file;

    /**
     * New FileEntry
     *
     * @param file File
     * @param fmt  Formatter for length
     */
    public FileEntry(final File file, final Formatter fmt) {
        super(file.getName(), fmt.formatFileLength(file),file.length(), (file.isFile() ? TYPE_FILE : TYPE_DIR), file.isDirectory());
        this.file = file;
    }

    /**
     * File Entry for media virtual folders
     * @param file
     * @param type
     * @param name
     */
    public FileEntry(final File file,final int type, final String name){
        super(file.getName(), "",file.length(), type, true);
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public boolean isFile() {
        return this.file.isFile();
    }
}
