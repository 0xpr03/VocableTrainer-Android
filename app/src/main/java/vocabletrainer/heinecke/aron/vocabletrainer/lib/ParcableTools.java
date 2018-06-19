package vocabletrainer.heinecke.aron.vocabletrainer.lib;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.sql.Date;

/**
 * Tools to help making stuff parable
 */
public class ParcableTools {
    /**
     * Read boolean from parcel
     * @param in
     * @return
     */
    public static boolean readParcableBool(@NonNull Parcel in){
        return in.readInt() == 1;
    }

    /**
     * Write boolean to pacel
     * @param in
     * @param data
     */
    public static void writeParcableBool(@NonNull Parcel in, boolean data){
        in.writeInt( data ? 1 : 0);
    }

    /**
     * Read date from parcel
     * @param in
     * @return null if -1 as value
     */
    @Nullable
    public static Date readParcableDate(@NonNull Parcel in) {
        long data = in.readLong();
        if(data == -1){
            return null;
        } else {
            return new Date(data);
        }
    }

    /**
     * Write date to parcel
     * @param in
     * @param data Date, can be null
     */
    public static void writeParcableDate(@NonNull Parcel in, @Nullable Date data) {
        if(data == null){
            in.writeLong(-1);
        } else {
            in.writeLong(data.getTime());
        }
    }
}
