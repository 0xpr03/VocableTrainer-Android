package vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.sql.Date
import java.util.*

/**
 * Tombstone of UUID + created date. Does not specify for which kind of Data.
 */
@Parcelize
data class Tombstone(val uuid: UUID, val created: Date): Parcelable
