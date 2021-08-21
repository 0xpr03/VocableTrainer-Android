package vocabletrainer.heinecke.aron.vocabletrainer

import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.util.Log
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import org.junit.*
import org.junit.runner.RunWith
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VEntry
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer.Trainer
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

    private fun string2List(input: String): List<String> {
        val lst = ArrayList<String>(1)
        lst.add(input)
        return lst
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        Assert.assertEquals("vocabletrainer.heinecke.aron.vocabletrainer", appContext.packageName)
    }

    //https://medium.com/mobile-app-development-publication/android-sqlite-database-unit-testing-is-easy-a09994701162#.s44tity8x
    //https://github.com/elye/demo_simpledb_test/blob/master/app/src/test/java/com/elyeproj/simpledb/ExampleUnitTest.kt
    private fun table(): VList {
        val salt = Random.nextInt()
        return VList("Test Column A $salt", "Test Column B $salt", "Test List $salt")
    }

    private fun getEntries(tbl: VList): List<VEntry> {
        val entries: MutableList<VEntry> = ArrayList<VEntry>(100)
        for (i in 0..99) {
            entries.add(VEntry("A$i", "B$i", "C$i", "D$i", tbl))
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
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
        Assert.assertTrue(utc-time <2)
        Assert.assertEquals(utc.toString(),java.lang.Long.toString(utc))
    }

    @Test
    fun testDBInsertTable() {
        val db = Database(InstrumentationRegistry.getTargetContext())
        val tbl: VList = table()
        val currentTables: List<VList> = db.tables!!
        Assert.assertTrue("UpsertTable", db.upsertVList(tbl))
        val tblsNew: List<VList> = db.tables!!
        Assert.assertEquals("Invalid amount of entries", currentTables.size+1, tblsNew.size)
        Assert.assertTrue("Cannot find table after insert", tblsNew.contains(tbl))
    }

    @Test
    fun testDBInsertEntries() {
        val db = Database(InstrumentationRegistry.getTargetContext())
        val tbl: VList = table()
        Assert.assertTrue("UpsertTable", db.upsertVList(tbl))
        val entries: List<VEntry> = getEntries(tbl)
        Assert.assertTrue("UpsertEntries", db.upsertEntries(entries))
        val result: List<VEntry> = db.getVocablesOfTable(tbl)
        Assert.assertEquals("invalid amount of entries", entries.size.toLong(), result.size.toLong())
    }

    @Test
    fun testDBEditEntries() {
        val db = Database(InstrumentationRegistry.getTargetContext())
        val tbl: VList = table()
        Assert.assertTrue("UpsertTable", db.upsertVList(tbl))
        val entries: List<VEntry> = getEntries(tbl)
        Assert.assertTrue("UpsertEntries", db.upsertEntries(entries))
        val result: List<VEntry> = db.getVocablesOfTable(tbl)
        Assert.assertEquals("invalid amount of entries", entries.size.toLong(), result.size.toLong())
        result[20].setAMeanings(string2List("New Word"))
        result[30].setDelete(true)
        Assert.assertTrue("UpsertEntries", db.upsertEntries(result))
        val edited: List<VEntry> = db.getVocablesOfTable(tbl)
        Assert.assertEquals("invalid amount of entries", (entries.size - 1).toLong(), edited.size.toLong())
        Assert.assertEquals("invalid entry data", result[20].getAString(), edited[20].getAString())
        Assert.assertEquals("invalid entry data", result[20].getBString(), edited[20].getBString())
        Assert.assertEquals("invalid entry data", result[20].getTip(), edited[20].getTip())
    }

    @Test
    fun testDBDelete() {
        val db = Database(InstrumentationRegistry.getTargetContext())
        val tbl: VList = table()
        Assert.assertTrue("upsert table", db.upsertVList(tbl))
        val entries: List<VEntry> = getEntries(tbl)
        Assert.assertTrue("UpsertEntries", db.upsertEntries(entries))
        Assert.assertEquals("invalid amount entries", entries.size.toLong(), db.getVocablesOfTable(tbl).size.toLong())
        val lists = db.tables!!
        Assert.assertTrue("delete table", db.deleteTable(tbl))
        Assert.assertEquals("invalid amount entries", 0, db.getVocablesOfTable(tbl).size.toLong())
        Assert.assertEquals("invalid amount lists", lists.size-1, db.tables!!.size)
    }

    @Test
    fun testDBRandomSelect() {
        val db = Database(InstrumentationRegistry.getTargetContext())
        val tbl: VList = table()
        Assert.assertTrue("UpsertTable", db.upsertVList(tbl))
        val entries: List<VEntry> = getEntries(tbl)
        Assert.assertTrue("UpsertEntries", db.upsertEntries(entries))
        Assert.assertNotNull(db.getRandomTrainerEntry(tbl, null, TrainerSettings(2, Trainer.TEST_MODE.RANDOM, true, true, true, false), true))
    }

    @Test
    fun testDBEntryPointsInsert() {
        val db = Database(InstrumentationRegistry.getTargetContext())
        val tbl: VList = table()
        Assert.assertTrue("UpsertTable", db.upsertVList(tbl))
        val entries: List<VEntry> = getEntries(tbl)
        Assert.assertTrue("UpsertEntries", db.upsertEntries(entries))
        val ent: VEntry = entries[0]
        ent.setPoints(2)
        Assert.assertEquals(2, ent.getPoints().toLong())
        Assert.assertTrue(db.updateEntryProgress(ent))
        Assert.assertEquals("table points", ent.getPoints().toLong(), db.getEntryPoints(ent).toLong())
        ent.setPoints(3)
        Assert.assertTrue(db.updateEntryProgress(ent))
        Assert.assertEquals("upd table points", ent.getPoints().toLong(), db.getEntryPoints(ent).toLong())
    }

    /**
     * Test for db random select to be
     * a) not repetitive, given a previous element as param
     * b) not selecting entries for which the points are matching the finished criteria
     */
    @Test
    fun testDbEntryRandomSelect() {
        val db = Database(InstrumentationRegistry.getTargetContext())
        val points = 1
        val tbl: VList = table()
        Assert.assertTrue("UpsertTable", db.upsertVList(tbl))
        var entries: List<VEntry> = getEntries(tbl)
        entries = entries.subList(0, 2)
        Assert.assertEquals(2, entries.size.toLong())
        Assert.assertTrue("UpsertEntries", db.upsertEntries(entries))
        val settings = TrainerSettings(points, Trainer.TEST_MODE.RANDOM, true, true, true, false)
        val chosen: VEntry? = db.getRandomTrainerEntry(tbl, null, settings, false)
        Assert.assertNotNull(chosen)
        val secondChosen: VEntry? = db.getRandomTrainerEntry(tbl, chosen, settings, false)
        Assert.assertNotNull(secondChosen)
        Assert.assertNotEquals("selected same entry twice", chosen!!.id.toLong(), secondChosen!!.getId().toLong())
        chosen.points = points
        Assert.assertEquals(points.toLong(), chosen.points.toLong())
        Assert.assertTrue(db.updateEntryProgress(chosen))
        Assert.assertEquals("table points", chosen.points.toLong(), db.getEntryPoints(chosen).toLong())
        val thirdChosen: VEntry? = db.getRandomTrainerEntry(tbl, null, settings, false)
        Assert.assertNotNull(thirdChosen)
        Assert.assertNotEquals("selected entry with reached points", chosen.getId().toLong(), thirdChosen!!.getId().toLong())
        thirdChosen.points = points
        Assert.assertTrue(db.updateEntryProgress(thirdChosen))
        val fourthChosen: VEntry? = db.getRandomTrainerEntry(tbl, null, settings, false)
        Assert.assertNull(fourthChosen)
    }
}