package vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage

import android.os.Parcelable
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database
import android.text.TextUtils
import kotlinx.android.parcel.Parcelize
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.Companion.MIN_ID_TRESHOLD
import java.io.Serializable
import java.sql.Date
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
    var addition: String?,
    var id: Long,
    var points: Int? = null,
    /**
     * Can be null if never used in training
     */
    var last_used: Date?,
    var created: Date,

    /**
     * Returns whether the Data of this VEntry was changed
     *
     * @return
     */
    var changed: Date,
    private var uuid: UUID?,
    var correct: Int,
    var wrong: Int,
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
        }

    var tip: String?
        get() = _tip
        set(value) {
            _tip = value
            isChanged = true
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
    fun equals(entry: VEntry): Boolean {
        if (this === entry) return true
        return if (list == null) {
            if (entry.list == null) id == entry.id else false
        } else id == entry.id && list.equals(entry.list)
    }

    override fun equals(obj: Any?): Boolean {
        return if (obj is VEntry) equals(obj) else super.equals(obj)
    }

    /**
     * Check whether this entry is existing, according to it's ID<br></br>
     * **Note:** this is not a check whether this entity exists in the Database or a "special" placeholder
     * @return true if the ID is valid
     */
    val isExisting: Boolean
        get() = VList.Companion.isIDValid(id)

    fun incrCorrect() {
        correct++
    }

    fun incrWrong() {
        wrong++
    }

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
            val time = Date(System.currentTimeMillis())
            return VEntry(
                _tip = tip,
                created = time,
                changed = time,
                wrong = 0,
                correct = 0,
                id = fID,
                list = null,
                meaningA = ArrayList(1),
                meaningB = ArrayList(1),
                uuid = null,
                addition = "",
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
            val time = Date(System.currentTimeMillis())
            val uuid = if (list?.uuid != null) {
                Database.uuid()
            } else {
                null
            }
            return VEntry(
                _tip = tip,
                created = time,
                changed = time,
                wrong = 0,
                correct = 0,
                id = MIN_ID_TRESHOLD-1,
                list = list,
                meaningA = meaningA,
                meaningB = meaningB,
                uuid = uuid,
                addition = addition,
                last_used = null
            )
        }
    }
}