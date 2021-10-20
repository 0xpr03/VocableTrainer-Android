package vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage

import android.os.Parcelable
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database
import android.text.TextUtils
import kotlinx.android.parcel.Parcelize
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.Companion.MIN_ID_TRESHOLD
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

/**
 * DB Vocable Entry
 */
@Parcelize
data class VEntry(
    /**
     * Can be null if spacer etc
     */
    val list: VList?,
    private var _tip: String?,

    /**
     * Get addition
     *
     * @return
     */
    private var _addition: String?,
    var id: Long,
    private var _points: Int? = null,
    /**
     * Can be null if never used in training or light data retrieval
     */
    var last_used: Long?,
    var created: Long,

    /**
     * Returns whether the Data of this VEntry was changed
     *
     * @return
     */
    var changed: Long,
    val uuid: UUID?,
    private var meaningA: MutableList<String>,
    private var meaningB: MutableList<String>,
    var isDelete: Boolean = false,
    private var isChanged: Boolean = false
) : Serializable, Parcelable {
    fun isChanged(): Boolean = isChanged

    /**
     * Returns meanings for A Column
     *
     * @return
     */
    var aMeanings: MutableList<String>
        get() = meaningA
        set(AMeanings) {
            meaningA = AMeanings
            isChanged = true
            changed = System.currentTimeMillis()
        }

    /**
     * Returns meanings for B Column
     *
     * @return
     */
    var bMeanings: MutableList<String>
        get() = meaningB
        set(BMeanings) {
            meaningB = BMeanings
            isChanged = true
            changed = System.currentTimeMillis()
        }

    var tip: String?
        get() = _tip
        set(value) {
            _tip = value
            isChanged = true
            changed = System.currentTimeMillis()
        }

    var addition: String?
        get() = _addition
        set(value) {
            _addition = value
            isChanged = true
            changed = System.currentTimeMillis()
        }

    var points: Int?
        get() = _points
        set(value) {
            _points = value
            last_used = System.currentTimeMillis()
        }

    override fun toString(): String {
        return if (list != null)  {
            aString + " " + bString + " ID:" + id + " List:" + list.id + " P:" + points
        } else {
            "$aString $bString ID:$id List:null P:$points"
        }
    }

    /**
     * Test for equality based on entry & list ID
     * If both have no List, it is ignored.
     * If one has no list, they are not seen as equal.
     * @param entry
     * @return
     */
    fun equalsId(entry: VEntry): Boolean {
        if (this === entry) return true
        return if (list == null) {
            if (entry.list == null) id == entry.id else false
        } else id == entry.id && list.equalsId(entry.list)
    }

    fun equalsId(obj: Any?): Boolean {
        return if (obj is VEntry) equalsId(obj) else super.equals(obj)
    }

    /**
     * Check whether this entry is existing, according to it's ID<br></br>
     * **Note:** this is not a check whether this entity exists in the Database or a "special" placeholder
     * @return true if the ID is valid
     */
    val isExisting: Boolean
        get() = VList.isIDValid(id)

    val aString: String
        get() = TextUtils.join(CONCAT, meaningA!!)
    val bString: String
        get() = TextUtils.join(CONCAT, meaningB!!)

    override fun describeContents(): Int {
        return 0
    }


    companion object {
        private const val CONCAT = "/"
        /**
         * Creates a new VEntry with invalid ID & empty fields
         */
        fun blankFromList(list: VList): VEntry {
            return importer("","","", "",list)
        }

        /**
         * Creates a 1:1 entry for spacers etc
         *  throw an IllegalArgumentException if ID should be valid
         * @param A meaning A entry
         * @param B meaning B entry
         * @param fID fake ID, has to be invalid
         * @param tip
         */
        fun spacer(A: String, B: String, tip: String, fID: Long): VEntry {
            if(VList.isIDValid(fID)){
                throw IllegalArgumentException("no valid ID allowed!");
            }
            val time = System.currentTimeMillis()
            return VEntry(
                _tip = tip,
                created = time,
                changed = time,
                id = fID,
                list = null,
                meaningA = ArrayList(1),
                meaningB = ArrayList(1),
                uuid = null,
                _addition = "",
                last_used = null
            ).apply {
                meaningA.add(A)
                meaningB.add(B)
            }
        }

        /**
         * Creates a 1:1 VEntry for Importing with an invalid ID
         */
        fun importer(A: String, B: String, tip: String, addition: String, list: VList): VEntry {
            return predefined(ArrayList(),ArrayList(),tip,addition,list).apply {
                meaningA.add(A)
                meaningB.add(B)
            }
        }

        /**
         * Creates a new VEntry with 0 points, ID < MIN_ID_TRESHOLD & current Date & 0 correct, wrong
         */
        fun predefined(meaningA: MutableList<String>,meaningB: MutableList<String>,tip: String,addition: String,list: VList?): VEntry {
            val time = System.currentTimeMillis()
            val uuid = Database.uuid()
            return VEntry(
                _tip = tip,
                created = time,
                changed = time,
                id = MIN_ID_TRESHOLD-1,
                list = list,
                meaningA = meaningA,
                meaningB = meaningB,
                uuid = uuid,
                _addition = addition,
                last_used = null
            )
        }
    }
}