package vocabletrainer.heinecke.aron.vocabletrainer.eximport.CSV;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Multi meaning handler
 */
public class MultiMeaningHandler {
    private final CSVCustomFormat cFormat;
    private final String escapedEscapeChar;
    private final String escapedMChar;
    public final static String ERROR_MSG_FORMAT = "Can't format multi meaning without supporting format!";

    /**
     * New multi meaning handler
     * @param cFormat Format to use
     */
    public MultiMeaningHandler(@NonNull CSVCustomFormat cFormat){
        this.cFormat = cFormat;

        //setup for performance
        if(cFormat.getEscapeMVChar() != null) {
            escapedEscapeChar = String.format("%s%s", cFormat.getEscapeMVChar(), cFormat.getEscapeMVChar());
            escapedMChar = String.format("%s%s", cFormat.getEscapeMVChar(), cFormat.getMultiValueChar());
        } else {
            escapedEscapeChar = null;
            escapedMChar = null;
        }
    }

    /**
     * Convert multi-meaning formatted string to a list of meanings<br>
     *     Does nothing if no escape / multi-value char is set
     * @param input Input string
     * @return List of meanings
     */
    public List<String> parseMultiMeaning(@NonNull String input) {
        List<String> meanings = new LinkedList<>();
        boolean escaped = false;
        final boolean trim = cFormat.getFormat().getIgnoreSurroundingSpaces();
        boolean handleEscape = cFormat.getMultiValueChar() != null & cFormat.getEscapeMVChar() != null;
        boolean handleMultiValue = cFormat.getMultiValueChar() != null;
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < input.length(); i++){
            char c = input.charAt(i);
            if(escaped){
                builder.append(c);
                escaped = false;
            } else if(handleEscape && c == cFormat.getEscapeMVChar()){
                escaped = true;
            } else if(handleMultiValue && c == cFormat.getMultiValueChar()) {
                meanings.add(trim ? builder.toString().trim() : builder.toString());
                builder.setLength(0); // reset builder
            } else {
                builder.append(c);
            }
        }
        if(builder.length() > 0){
            meanings.add(trim ? builder.toString().trim() : builder.toString());
        }

        return meanings;
    }

    /**
     * Convert multi meanings to multi-meaning formatted string
     * @param meanings List of meanings to format
     * @return multi-meaning formatted String
     */
    public String formatMultiMeaning(@NonNull Collection<String> meanings) {
        if(cFormat.getMultiValueChar() == null){
            throw new IllegalArgumentException(ERROR_MSG_FORMAT);
        }
        StringBuilder builder = new StringBuilder();

        boolean first = true;
        for(String meaning : meanings) { // do not use streams for builder performance?
            if(first){
                first = false;
            } else {
                builder.append(cFormat.getMultiValueChar());
            }
            if(escapedEscapeChar != null){
                builder.append(
                    meaning.replace(cFormat.getEscapeMVCharString(), escapedEscapeChar)
                        .replace(cFormat.getMultiValueCharString(),escapedMChar)
                );
            } else {
                builder.append(meaning);
            }
        }
        return builder.toString();
    }
}
