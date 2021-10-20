package vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database
import java.util.*

/**
 * List category
 *
 * Always has a UUID, as you can't partially sync categories
 */
@Parcelize
data class Category(private var _name: String, var id: Long, val uuid: UUID, var changed: Long): Parcelable {
    /**
     * Check whether this entry is existing, according to it's ID<br></br>
     * **Note:** this is not a check whether this entity exists in the Database or a "special" placeholder
     * @return true if the ID is valid
     */
    val isExisting: Boolean
        get() = VList.isIDValid(id)

    var name: String
        get() = _name
        set(value) {
            _name = value
            changed = System.currentTimeMillis()
        }

    companion object {
        fun new(name: String): Category {
            return Category(
                _name = name,
                id = Database.MIN_ID_TRESHOLD -1,
                uuid = Database.uuid(),
                changed = System.currentTimeMillis()
            )
        }
    }
}
