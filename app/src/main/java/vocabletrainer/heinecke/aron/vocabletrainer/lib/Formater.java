package vocabletrainer.heinecke.aron.vocabletrainer.lib;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Formater
 */
public class Formater {
    private final DecimalFormat formatBytes = new DecimalFormat("#,##0.#");
    private final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
    private final String[] unit_si = new String[] {"B", "KiB", "MiB", "GiB", "TiB"};
    private boolean USE_SI = true;

    /**
     * Format bytes to human readable string
     * @param i bytes amount
     * @return String, for ex. 5,4MiB
     */
    public String formatBytes(long i){
        if (i <= 0)
            return "0";
        int digitGroups = (int) (Math.log10(i) / Math.log10(USE_SI ? 1024 : 1000));
        return formatBytes.format(i / Math.pow(1024, digitGroups)) + " " + (USE_SI ? unit_si[digitGroups] : units[digitGroups]);
    }

    /**
     * Format file length
     * @param file
     * @return (empty) String with the file length
     */
    public String formatFileLength(final File file){
        if(file.exists() && file.isFile()){
            return formatBytes(file.length());
        }else{
            return "";
        }
    }

    /**
     * Change SI notation usage
     * @param use_si
     */
    public void changeSI(boolean use_si){
        USE_SI = use_si;
    }
}
