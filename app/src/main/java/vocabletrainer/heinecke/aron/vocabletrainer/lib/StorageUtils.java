package vocabletrainer.heinecke.aron.vocabletrainer.lib;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static vocabletrainer.heinecke.aron.vocabletrainer.activity.MainActivity.PREFS_NAME;

/**
 * Storage retriever utilities
 * Copy-Pasta of different approaches, because android _really_ doesn't want you to export/import stuff
 * via files
 */
public class StorageUtils {
    private static final String INTERNAL = "internal";
    public static final String SD_CARD = "sdCard";
    private static final String EXTERNAL_SD_CARD = "externalSdCard";
    private static final String ENV_SECONDARY_STORAGE = "SECONDARY_STORAGE";
    private static final String TAG = "StorageUtils";
    private static final String SETTINGS_EXTENDED_VOLUMES_SEARCH = "search_volumes_extended";

    /**
     * Get all storage locations<br>
     * Uses @{SETTINGS_EXTENDED_VOLUMES_SEARCH} pref for extended mode
     * @param context
     * @return Locations Map of Label,File
     */
    @NonNull
    public static Map<String, File> getAllStorageLocations(@NonNull Context context) {
        Map<String, File> storageLocations = new HashMap<>(10);
        boolean extended = context.getSharedPreferences(PREFS_NAME,0).getBoolean(SETTINGS_EXTENDED_VOLUMES_SEARCH,false);
        if(extended) {
            File sdCard = Environment.getExternalStorageDirectory();
            addStorage(SD_CARD, sdCard,storageLocations);

            File internal = Environment.getDataDirectory();
            addStorage(INTERNAL, internal,storageLocations);

            File additional = context.getFilesDir();
            addStorage(INTERNAL, additional,storageLocations);

            getStorageEnv(storageLocations);
            retrieveStorageManager(context,storageLocations);
            retrieveStorageFilesystem(storageLocations);
        }
        getStorageExternalFilesDir(context, storageLocations);
        return storageLocations;
    }

    /**
     * unified test function to add storage if fitting
     * @param label
     * @param entry
     * @param storageLocations
     */
    private static void addStorage(String label, File entry, Map<String, File> storageLocations){
        if(entry != null && entry.listFiles() != null && !storageLocations.containsValue(entry)){
            storageLocations.put(label, entry);
        }else{
            Log.d(TAG,entry.getAbsolutePath());
        }
    }

    /**
     * Get storage from ENV, as recommended by 99%, doesn't detect external SD card, only internal ?!
     * @param storageLocations
     */
    private static void getStorageEnv(Map<String, File> storageLocations){
        final String rawSecondaryStorage = System.getenv(ENV_SECONDARY_STORAGE);
        if (!TextUtils.isEmpty(rawSecondaryStorage)) {
            String[] externalCards = rawSecondaryStorage.split(":");
            for (int i = 0; i < externalCards.length; i++) {
                String path = externalCards[i];
                storageLocations.put(EXTERNAL_SD_CARD + String.format(i == 0 ? "" : "_%d", i), new File(path));
            }
        }
    }

    /**
     * Get storage indirect, best solution so far
     * @param context
     * @param storageLocations
     */
    private static void getStorageExternalFilesDir(Context context, Map<String, File> storageLocations)
    {
        //Get primary & secondary external device storage (internal storage & micro SDCARD slot...)
        File[]  listExternalDirs = ContextCompat.getExternalFilesDirs(context, null);
        for (File listExternalDir : listExternalDirs) {
            if (listExternalDir != null) {
                Log.d(TAG,"filesDir:"+listExternalDir.getAbsolutePath());
                String path = listExternalDir.getAbsolutePath();
                int indexMountRoot = path.indexOf("/Android/data/");
                if (indexMountRoot >= 0 && indexMountRoot <= path.length()) {
                    //Get the root path for the external directory
                    File file = new File(path.substring(0, indexMountRoot));
                    addStorage(file.getName(), file, storageLocations);
                }
            }
        }
    }

    /**
     * Get storages via StorageManager & reflection hacks, probably never works
     * @param context
     * @param storageLocations
     */
    private static void retrieveStorageManager(Context context, Map<String, File> storageLocations) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StorageManager storage = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            try {
                for (StorageVolume volume : storage.getStorageVolumes()) {
                    // reflection hack to get volume paths
                    File dir = callReflectionFunction(volume,"getPathFile");
                    String label = callReflectionFunction(volume,"getUserLabel");
                    addStorage(label,dir,storageLocations);
                }
            } catch (Exception e){
                Log.w(TAG,"error during storage retrieval",e);
            }
        }
    }

    /**
     * Get storage via /proc/mounts, probably never works because of permissions to read raw mount
     * @param storageLocations
     */
    private static void retrieveStorageFilesystem(Map<String, File> storageLocations){
        try {
            File mountFile = new File("/proc/mounts");
            if(mountFile.exists()){
                Scanner scanner = new Scanner(mountFile);
                while (scanner.hasNext()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("/dev/block/vold/")) {
                        String[] lineElements = line.split(" ");
                        File element = new File(lineElements[1]);

                        addStorage(element.getName(),element,storageLocations);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reflection helper function, to invoke private functions
     * @param obj
     * @param function
     * @param <T>
     * @return
     * @throws ClassCastException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     */
    private static <T> T callReflectionFunction(Object obj, String function)
            throws ClassCastException,InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method method = obj.getClass().getDeclaredMethod(function);
        method.setAccessible(true);
        Object r = method.invoke(obj);
        //noinspection unchecked
        return (T) r;
    }
}
