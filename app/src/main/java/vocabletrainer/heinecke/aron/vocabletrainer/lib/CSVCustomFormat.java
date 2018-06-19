package vocabletrainer.heinecke.aron.vocabletrainer.lib;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.csv.CSVFormat;

import java.io.Serializable;

/**
 * CSV Custom Format
 * Ads multi-value per cell to the format
 */
public class CSVCustomFormat implements Serializable {
    private final CSVFormat format;
    private final Character multiValueChar;
    private final Character escapeMVChar;

    private final String multiValueCharString; // store for performance
    private final String escapeMVCharString; // store for performance

    /*
     * DEFAULT custom format using CSVFormat.DEFAULT and '/' for multi value (enabled)
     */
    public static CSVCustomFormat DEFAULT = new CSVCustomFormat(CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true));

    /**
     * Custom Format with default multi value settings<br>
     *     Multi value is enabled and set to '/' char
     * @param format
     */
    public CSVCustomFormat (@NonNull CSVFormat format) {
        this(format,'/','\\');
    }

    /**
     * New Custom Format
     * @param format CSVFormat
     * @param multiValueChar null disable multi value support
     * @param escapeMVChar null disable escaping support
     */
    public CSVCustomFormat(@NonNull CSVFormat format, @Nullable Character multiValueChar,
                           @Nullable Character escapeMVChar) {
        this.format = format;
        this.multiValueChar = multiValueChar;
        this.escapeMVChar = escapeMVChar;

        this.multiValueCharString = multiValueChar == null ? null : String.valueOf(multiValueChar);
        this.escapeMVCharString = escapeMVChar == null ? null : String.valueOf(escapeMVChar);
    }

    public CSVFormat getFormat() {
        return format;
    }

    @Nullable
    public Character getMultiValueChar() {
        return multiValueChar;
    }

    @Nullable
    public Character getEscapeMVChar() {
        return escapeMVChar;
    }

    public boolean isMultiValueEnabled() {
        return multiValueChar != null;
    }

    public boolean isMVEscapeEnabled() {
        return escapeMVChar != null;
    }

    /**
     * Returns a string representation of the multi-value char
     * @return
     */
    public String getMultiValueCharString() {
        return multiValueCharString;
    }

    /**
     * Returns a string representation of the escape multi-value char
     * @return
     */
    public String getEscapeMVCharString() {
        return escapeMVCharString;
    }

    /**
     * Returns IgnoreSurroundingSpaces state for trimming<br>
     *     Can be set in CSVFormat with ignoreSurroundingSpaces
     * @return state
     */
    public boolean trim() { return format.getIgnoreSurroundingSpaces(); }
}
