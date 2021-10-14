package vocabletrainer.heinecke.aron.vocabletrainer

import android.content.Context
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.runner.RunWith
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VEntry
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer.Trainer
import java.sql.Date
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.random.Random

/**
 * Database test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 *
 * Using roboelectric system from https://github.com/elye/demo_simpledb_test for non-emulator DB tests.
 */

@RunWith(AndroidJUnit4::class)
class DBTest {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun string2List(input: String): MutableList<String> {
        val lst = ArrayList<String>(1)
        lst.add(input)
        return lst
    }

    //https://medium.com/mobile-app-development-publication/android-sqlite-database-unit-testing-is-easy-a09994701162#.s44tity8x
    //https://github.com/elye/demo_simpledb_test/blob/master/app/src/test/java/com/elyeproj/simpledb/ExampleUnitTest.kt
    private fun table(withUUID: Boolean = false): VList {
        val id = Random.nextInt()
        val lst = VList.blank("Test Column A $id", "Test Column B $id","Test List $id")
        if (withUUID) {
            lst.uuid = Database.uuid()
        }
        return lst
    }

    private fun generateEntries(tbl: VList): List<VEntry> {
        val entries: MutableList<VEntry> = ArrayList<VEntry>(100)
        for (i in 0..99) {
            entries.add(VEntry.importer("A$i", "B$i", "C$i", "D$i",tbl))
        }
        return entries
    }

    @Before
    fun init() {
        _mutex.lock()
    }

    @After
    fun after() {
        _mutex.unlock()
    }

    companion object {
        private const val TAG = "UNIT"
        private val _mutex: Lock = ReentrantLock(true)
    }

    @Test
    fun testUTC() {
        val time = System.currentTimeMillis()
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis;
        val diff = utc-time
        Assert.assertTrue("time vs Calendar-UTC has a difference of $diff",diff < 5)
        Assert.assertEquals(utc.toString(),java.lang.Long.toString(utc))
        val date = Date(time)
        Assert.assertEquals("System.currentTimeMillis isn't equal Date.time created from it",time,date.time)
    }

    @Test
    fun testDBInsertList() {
        _testDBInsertList(false)
        _testDBInsertList(true)
    }

    fun _testDBInsertList(withUUID: Boolean) {
        val db = Database(context)
        val list: VList = table(withUUID)
        val curLists: List<VList> = db.tables
        db.upsertVList(list)
        Assert.assertTrue(list.isExisting)
        val listsNew: List<VList> = db.tables
        Assert.assertEquals("Invalid amount of entries", curLists.size+1, listsNew.size)
        //Log.d(this::class.simpleName,"list id: $list.id")
        Assert.assertFalse("list existed pre-insertion",curLists.map { v -> v.id }.contains(list.id))
        Assert.assertTrue("list missing after insertion",listsNew.map { v -> v.id }.contains(list.id))
        listsNew.find { v -> v.id == list.id }!!.apply {
            Assert.assertEquals("retrieved list != original list", list,this)
            Assert.assertEquals(withUUID,this.uuid != null)
        }
        Assert.assertTrue("Cannot find table after insert", listsNew.contains(list))
    }

    @Test
    fun testDBInsertEntries() {
        _testDBInsertEntries(false)
        _testDBInsertEntries(true)
    }

    @Suppress("TestFunctionName")
    private fun _testDBInsertEntries(withUUID: Boolean) {
        val db = Database(context)
        val tbl: VList = table(withUUID)
        db.upsertVList(tbl)
        val entries: List<VEntry> = generateEntries(tbl)
        Assert.assertTrue(entries.size > 1)
        db.upsertEntries(entries)
        val result: List<VEntry> = db.getVocablesOfTable(tbl)
        Assert.assertEquals("invalid amount of entries", entries.size.toLong(), result.size.toLong())
        for (entry in entries) {
            val ie = result.find { v -> v.id == entry.id }!!
            Assert.assertEquals("Inserted element not equal to original",entry,ie)
            Assert.assertEquals(withUUID,ie.uuid != null)
        }
    }

    @Test
    fun testDBEditEntries() {
        val db = Database(context)
        val tbl: VList = table()
        db.upsertVList(tbl)
        val entries: List<VEntry> = generateEntries(tbl)
        db.upsertEntries(entries)
        val result: List<VEntry> = db.getVocablesOfTable(tbl)
        Assert.assertEquals("invalid amount of entries", entries.size.toLong(), result.size.toLong())
        result[20].aMeanings = string2List("New Word")
        result[30].isDelete = true
        db.upsertEntries(result)
        val edited: List<VEntry> = db.getVocablesOfTable(tbl)
        Assert.assertEquals("invalid amount of entries", (entries.size - 1).toLong(), edited.size.toLong())
        Assert.assertEquals("invalid entry data", result[20].aString, edited[20].aString)
        Assert.assertEquals("invalid entry data", result[20].bString, edited[20].bString)
        Assert.assertEquals("invalid entry data", result[20].tip, edited[20].tip)
    }

    @Test
    fun testDBDelete() {
        _testDBDelete(false)
        _testDBDelete(true)
    }

    @Suppress("TestFunctionName")
    private fun _testDBDelete(uuid: Boolean) {
        val db = Database(context)
        val list: VList = table(uuid)
        Assert.assertNotNull("missing UUID for list",list.uuid)
        db.upsertVList(list)
        val entries: List<VEntry> = generateEntries(list)
        db.upsertEntries(entries)
        for (e in entries)
            Assert.assertNotNull("missing UUID for entry",e.uuid)
        Assert.assertEquals("invalid amount entries", entries.size, db.getVocablesOfTable(list).size)
        val listsPre = db.tables
        val deletionTime = Date(System.currentTimeMillis())
        db.deleteList(list)
        val listsPost = db.tables
        Assert.assertEquals("invalid amount entries", 0, db.getVocablesOfTable(list).size)
        Assert.assertEquals("invalid amount lists", listsPre.size-1, listsPost.size)
        Assert.assertNull(listsPost.find { v -> v.id == list.id })
        if (uuid) {
            val deletedLists = db.deletedLists(deletionTime)
            val found = deletedLists.find { v -> v.uuid == list.uuid }
            Assert.assertNotNull("deleted list has no tombstone",found)
            val diff = deletionTime.time - found!!.created.time
            Assert.assertTrue("deletion time diff is $diff", diff < 5)
            val deletedEntries = db.deletedEntries(deletionTime)
            for (entry in entries) {
                Assert.assertNull("unnecessary entry deletion for deleted list",deletedEntries.find { v -> v.uuid == entry.uuid })
            }
        }
    }

    @Test
    fun testDBRandomSelect() {
        val db = Database(context)
        val tbl: VList = table()
        db.upsertVList(tbl)
        val entries: List<VEntry> = generateEntries(tbl)
        db.upsertEntries(entries)
        Assert.assertNotNull(db.getRandomTrainerEntry(tbl, null, TrainerSettings(2, Trainer.TEST_MODE.RANDOM, true, true, true, false), true))
    }

    @Test
    fun testDBEntryPointsInsert() {
        val db = Database(context)
        val tbl: VList = table()
        db.upsertVList(tbl)
        val entries: List<VEntry> = generateEntries(tbl)
        db.upsertEntries(entries)
        val ent: VEntry = entries[0]
        ent.points = 2
        Assert.assertEquals(2, ent.points!!.toLong())
        db.updateEntryProgress(ent)
        Assert.assertEquals("table points", ent.points!!.toLong(), db.getEntryPoints(ent).toLong())
        ent.points = 3
        db.updateEntryProgress(ent)
        Assert.assertEquals("upd table points", ent.points!!.toLong(), db.getEntryPoints(ent).toLong())
    }

    /**
     * Test for db random select to be
     * a) not repetitive, given a previous element as param
     * b) not selecting entries for which the points are matching the finished criteria
     */
    @Test
    fun testDbEntryRandomSelect() {
        val db = Database(context)
        val points = 1
        val tbl: VList = table()
        db.upsertVList(tbl)
        var entries: List<VEntry> = generateEntries(tbl)
        entries = entries.subList(0, 2)
        Assert.assertEquals(2, entries.size.toLong())
        db.upsertEntries(entries)
        val settings = TrainerSettings(points, Trainer.TEST_MODE.RANDOM, true, true, true, false)
        val chosen: VEntry? = db.getRandomTrainerEntry(tbl, null, settings, false)
        Assert.assertNotNull(chosen)
        val secondChosen: VEntry? = db.getRandomTrainerEntry(tbl, chosen, settings, false)
        Assert.assertNotNull(secondChosen)
        Assert.assertNotEquals("selected same entry twice", chosen!!.id.toLong(), secondChosen!!.id.toLong())
        chosen.points = points
        Assert.assertEquals(points.toLong(), chosen.points!!.toLong())
        db.updateEntryProgress(chosen)
        Assert.assertEquals("table points", chosen.points!!.toLong(), db.getEntryPoints(chosen).toLong())
        val thirdChosen: VEntry? = db.getRandomTrainerEntry(tbl, null, settings, false)
        Assert.assertNotNull(thirdChosen)
        Assert.assertNotEquals("selected entry with reached points", chosen.id.toLong(), thirdChosen!!.id.toLong())
        thirdChosen.points = points
        db.updateEntryProgress(thirdChosen)
        val fourthChosen: VEntry? = db.getRandomTrainerEntry(tbl, null, settings, false)
        Assert.assertNull(fourthChosen)
    }
}