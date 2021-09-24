package vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage

import android.os.Parcelable
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VEntry
import android.text.TextUtils
import android.os.Parcel
import vocabletrainer.heinecke.aron.vocabletrainer.lib.ParcableTools
import android.os.Parcelable.Creator
import kotlinx.android.parcel.Parcelize
import java.sql.Date
import java.util.*
import kotlin.jvm.JvmOverloads

/**
 * DB Vocable List entry
 */
@Parcelize
data class VList(
    var nameA: String,
    var nameB: String,
    var name: String,
    var created: Date,
    var changed: Date,

    /**
     * Returns the amount of vocables this table has<br></br>
     * The value can be -1 when not set!
     *
     * @return
     */
    var totalVocs: Int = -1,

    /**
     * Returns the amount of unfinished vocables<br></br>
     * The value can be -1 when not set!
     *
     * @return
     */
    var unfinishedVocs: Int = -1,
    private var _id: Long,
    var uuid: UUID?,
    var isSelected: Boolean = false
) : Parcelable {

    /**
     * Check whether this entry is existing, according to it's ID<br></br>
     * **Note:** this is not a check whether this entity exists in the Database
     * @return true if the ID is valid
     */
    val isExisting: Boolean
        get() = isIDValid(id)

    /**
     * Tests for equality based on list ID
     *
     * @param list
     * @return true when list IDs are equal
     */
    fun equals(list: VList?): Boolean {
        return if (this === list) {
            true
        } else id == list!!.id
    }

    override fun equals(obj: Any?): Boolean {
        return if (obj is VList) {
            equals(obj as VList?)
        } else super.equals(obj)
    }

    var id: Long
        get() = _id
        /**
         * Set a new ID
         *
         * @param id new ID
         * @throws IllegalAccessError if a valid ID is already set
         */
        set(value) {
            if (isIDValid(_id)) throw IllegalAccessError("Can't override existing VList ID") else _id =  value
        }

    companion object {
        /**
         * Checks whether a given ID is valid, according to MIN_ID_TRESHOLD
         * @param id ID to check
         * @return true if ID is valid
         */
        @JvmStatic
        fun isIDValid(id: Long): Boolean {
            return id >= Database.MIN_ID_TRESHOLD
        }
        @JvmStatic
        fun isIDValid(id: Int): Boolean {
            return id >= Database.MIN_ID_TRESHOLD
        }

        /**
         * Create a blank VList missing a valid ID and just naming
         */
        fun blank(nameA: String, nameB: String,name: String): VList {
            val time = Date(System.currentTimeMillis())
            return VList(
                name = name, nameB = nameB, nameA = nameA, _id = Database.MIN_ID_TRESHOLD - 1,
                changed = time,
                uuid = null, created = time
            )
        }

        /**
         * Creates a new VList data object
         *
         * @param id    ID
         * @param nameA Name for A Column
         * @param nameB Name for B Column
         */
        fun withId(id: Long, name: String, nameA: String, nameB: String): VList {
            val time = Date(System.currentTimeMillis())
            return VList(
                name = name, nameB = nameB, nameA = nameA, _id = id,
                changed = time,
                uuid = null, created = time
            )
        }
    }
}