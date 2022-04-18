package vocabletrainer.heinecke.aron.vocabletrainer

import android.content.Context
import android.icu.util.Calendar
import android.icu.util.TimeZone
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.runner.RunWith
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.*
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer.SessionStorageManager
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer.Trainer
import java.sql.Date
import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayList
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
    private fun genList(withUUID: Boolean = false): VList {
        val id = Random.nextInt()
        val lst = VList.blank("Test Column A $id", "Test Column B $id","Test List $id")
        if (withUUID) {
            lst.uuid = Database.uuid()
        }
        return lst
    }

    private fun genSetting(): Pair<String,String> {
        return Pair(UUID.randomUUID().toString(),UUID.randomUUID().toString())
    }

    private fun generateEntries(tbl: VList): List<VEntry> {
        val entries: MutableList<VEntry> = ArrayList<VEntry>(100)
        for (i in 0..99) {
            entries.add(VEntry.importer("A$i", "B$i", "C$i", "D$i",tbl))
        }
        return entries
    }

    private fun genCategory(): Category {
        return Category.new("Category ${Random.nextInt()}")
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
    fun testGetVocable() {
        val db = Database(context)
        val lst: VList = genList(false)
        db.upsertVList(lst)
        val entries: List<VEntry> = generateEntries(lst)
        Assert.assertTrue(entries.size > 1)
        db.upsertEntries(entries)

        for (i in 0..10) {
            val expected = entries.get(Random.nextInt(0,entries.size-1))
            val got = db.getEntry(expected.id)!!
            Assert.assertEquals(expected.id,got.id)
            Assert.assertEquals(expected.aMeanings,got.aMeanings)
        }
    }

    @Test
    fun testDBInsertList() {
        _testDBInsertList(false)
        _testDBInsertList(true)
    }

    fun _testDBInsertList(withUUID: Boolean) {
        val db = Database(context)
        val list: VList = genList(withUUID)
        list.shared = Random.nextInt(0,2)
        val curLists: List<VList> = db.lists
        db.upsertVList(list)
        Assert.assertTrue(list.isExisting)
        val listsNew: List<VList> = db.lists
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
    fun testTruncateList() {
        val db = Database(context)
        val lst: VList = genList(false)
        db.upsertVList(lst)
        val delTime = System.currentTimeMillis()
        val entries: List<VEntry> = generateEntries(lst)
        Assert.assertTrue(entries.size > 1)
        db.upsertEntries(entries)
        Assert.assertEquals(entries.size,db.getEntriesOfList(lst).size)
        db.truncateList(lst)
        Assert.assertEquals(0,db.getEntriesOfList(lst).size)
        val deleted = db.deletedEntries(delTime)
        for (e in entries) {
            Assert.assertNotNull("Can't find tombstone for $e",deleted.find { v -> v.uuid == e.uuid })
        }
    }

    @Test
    fun testDBInsertEntries() {
        val db = Database(context)
        val tbl: VList = genList(false)
        db.upsertVList(tbl)
        val entries: List<VEntry> = generateEntries(tbl)
        Assert.assertTrue(entries.size > 1)
        db.upsertEntries(entries)
        val result: List<VEntry> = db.getEntriesOfList(tbl)
        Assert.assertEquals("invalid amount of entries for $tbl", entries.size.toLong(), result.size.toLong())
        for (entry in entries) {
            val ie = result.find { v -> v.id == entry.id }!!
            Assert.assertEquals("Inserted element not equal to original",entry,ie)
            Assert.assertNotNull(ie.uuid)
        }
    }

    @Test
    fun testDBEditEntries() {
        val db = Database(context)
        val tbl: VList = genList()
        db.upsertVList(tbl)
        val entries: List<VEntry> = generateEntries(tbl)
        db.upsertEntries(entries)
        val result: List<VEntry> = db.getEntriesOfList(tbl)
        Assert.assertEquals("invalid amount of entries", entries.size.toLong(), result.size.toLong())
        result[20].aMeanings = string2List("New Word")
        result[30].isDelete = true
        db.upsertEntries(result)
        val edited: List<VEntry> = db.getEntriesOfList(tbl)
        Assert.assertEquals("invalid amount of entries", (entries.size - 1).toLong(), edited.size.toLong())
        Assert.assertEquals("invalid entry data", result[20].aString, edited[20].aString)
        Assert.assertEquals("invalid entry data", result[20].bString, edited[20].bString)
        Assert.assertEquals("invalid entry data", result[20].tip, edited[20].tip)
    }

    @Test
    fun testListDelete() {
        _testListDelete(false)
        _testListDelete(true)
    }

    @Suppress("TestFunctionName")
    private fun _testListDelete(uuid: Boolean) {
        val db = Database(context)
        val list: VList = genList(uuid)
        if (uuid)
            Assert.assertNotNull("missing UUID for list",list.uuid)
        db.upsertVList(list)
        val entries: List<VEntry> = generateEntries(list)
        db.upsertEntries(entries)
        if (uuid)
            for (e in entries)
                Assert.assertNotNull("missing UUID for entry",e.uuid)
        Assert.assertEquals("invalid amount entries", entries.size, db.getEntriesOfList(list).size)
        val listsPre = db.lists
        val deletionTime = System.currentTimeMillis()
        db.deleteList(list)
        val listsPost = db.lists
        Assert.assertEquals("invalid amount entries", 0, db.getEntriesOfList(list).size)
        Assert.assertEquals("invalid amount lists", listsPre.size-1, listsPost.size)
        Assert.assertNull("found deleted list",listsPost.find { v -> v.id == list.id })
        if (uuid) {
            val deletedLists = db.deletedLists(deletionTime)
            val found = deletedLists.find { v -> v.uuid == list.uuid }
            Assert.assertNotNull("deleted list has no tombstone",found)
            val diff = deletionTime - found!!.created.time
            Assert.assertTrue("deletion time diff is $diff", diff < 5)
            val deletedEntries = db.deletedEntries(deletionTime)
            for (entry in entries) {
                Assert.assertNull("unnecessary entry deletion for deleted list",deletedEntries.find { v -> v.uuid == entry.uuid })
            }
        }
    }

    @Test
    fun testEntryDelete() {
        val db = Database(context)
        val list: VList = genList(false)
        db.upsertVList(list)
        val entries = generateEntries(list).toMutableList()
        db.upsertEntries(entries)
        val deletedEntries: MutableList<VEntry> = mutableListOf()
        for (i in 0..5) {
            val entry = entries[Random.nextInt(0, entries.size-1)]
            entry.isDelete = true
            deletedEntries.add(entry)
        }
        db.upsertEntries(entries)
        // ensure deletion of entries
        val newEntries = db.getEntriesOfList(list)
        entries.removeAll(deletedEntries)
        Assert.assertEquals(entries,newEntries)
        val deleted = db.deletedEntries()
        for (e in deletedEntries) {
            Assert.assertNotNull("can't find UUID of deleted entry ${e.uuid}",deleted.find { v -> v.uuid == e.uuid })
        }
        // now delete the list, this should remove all entry tombstone
        db.deleteList(list)
        val newDeleted = db.deletedEntries()
        for (e in deletedEntries) {
            Assert.assertNull(newDeleted.find { v -> v.uuid == e.uuid })
        }
    }

    @Test
    fun testDBRandomSelect() {
        val db = Database(context)
        val tbl: VList = genList()
        db.upsertVList(tbl)
        val entries: List<VEntry> = generateEntries(tbl)
        db.upsertEntries(entries)
        Assert.assertNotNull(db.getRandomTrainerEntry( null, TrainerSettings(2, Trainer.TEST_MODE.RANDOM, true, true, true, false), true))
    }

    /**
     * Test session start
     */
    @Test
    fun testDbSessionStart() {
        val db = Database(context)
        val tbl: VList = genList()
        db.upsertVList(tbl)
        var entries: List<VEntry> = generateEntries(tbl)
        entries = entries.subList(0, 2)
        Assert.assertEquals(2, entries.size.toLong())
        db.upsertEntries(entries)

        db.deleteSession()
        Assert.assertFalse(db.isSessionStored)
        db.createSession(listOf(tbl))
        Assert.assertTrue(db.isSessionStored)
        Assert.assertEquals(db.getSessionUnfinishedEntries(1),entries.size.toLong())
        Assert.assertEquals(db.getSessionTotalEntries(),entries.size.toLong())
    }

    @Test
    fun testDBEntryPointsInsert() {
        val db = Database(context)
        db.deleteSession()
        val tbl: VList = genList()
        db.upsertVList(tbl)
        val entries: List<VEntry> = generateEntries(tbl)
        db.upsertEntries(entries)

        db.createSession(listOf(tbl))
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
        val tbl: VList = genList()
        db.upsertVList(tbl)
        var entries: List<VEntry> = generateEntries(tbl)
        entries = entries.subList(0, 2)
        Assert.assertEquals(2, entries.size.toLong())
        db.upsertEntries(entries)
        // clear previous
        db.deleteSession()
        val settings = TrainerSettings(points, Trainer.TEST_MODE.RANDOM, true, true, true, false)
        // init session with db.createSession underneath
        SessionStorageManager.CreateSession(db,settings, arrayListOf(tbl))

        val chosen: VEntry? = db.getRandomTrainerEntry(null, settings, false)
        Assert.assertNotNull(chosen)
        val secondChosen: VEntry? = db.getRandomTrainerEntry(chosen, settings, false)
        Assert.assertNotNull(secondChosen)
        Assert.assertNotEquals("selected same entry twice", chosen!!.id.toLong(), secondChosen!!.id.toLong())
        chosen.points = points
        Assert.assertEquals(points.toLong(), chosen.points!!.toLong())
        db.updateEntryProgress(chosen)
        Assert.assertEquals("table points", chosen.points!!.toLong(), db.getEntryPoints(chosen).toLong())
        val thirdChosen: VEntry? = db.getRandomTrainerEntry(null, settings, false)
        Assert.assertNotNull(thirdChosen)
        Assert.assertNotEquals("selected entry with reached points", chosen.id.toLong(), thirdChosen!!.id.toLong())
        thirdChosen.points = points
        db.updateEntryProgress(thirdChosen)
        val fourthChosen: VEntry? = db.getRandomTrainerEntry(null, settings, false)
        Assert.assertNull(fourthChosen)
    }

    @Test
    fun testCategoryInsert() {
        val db = Database(context)
        val sizePrev = db.categories().size()
        val cat = genCategory()
        db.upsertCategory(cat)
        val categories = db.categories()
        Assert.assertEquals(sizePrev+1,categories.size())
        val found = categories.get(cat.id)
        Assert.assertNotNull("inserted category not found",found)
        Assert.assertEquals(cat,found)
    }

    @Test
    fun testCategoryDelete() {
        val db = Database(context)
        val cat = genCategory()
        db.upsertCategory(cat)
        val delTime = System.currentTimeMillis()
        db.deleteCategory(cat)
        Assert.assertNull("found deleted category",db.categories().get(cat.id))
        val deleted = db.deletedCategories(delTime)
        val tombstone = deleted.find { v -> v.uuid == cat.uuid }
        Assert.assertNotNull("no tombstone for deleted category",tombstone)
        val diff = delTime - tombstone!!.created.time
        Assert.assertTrue("deletion time diff is $diff", diff < 5)
    }

    @Test
    fun testCategoryEdit() {
        val db = Database(context)
        val origin = genCategory()
        db.upsertCategory(origin)
        val cat = db.categories().get(origin.id)
        Assert.assertEquals(origin,cat)
        cat!!.name = "asdasdasd"
        db.upsertCategory(cat)
        val found = db.categories().get(origin.id)
        Assert.assertEquals(cat,found)
        Assert.assertNotEquals(origin.changed,found!!.changed)
    }

    @Test
    fun testCategoryRetrieval() {
        val db = Database(context)
        val cat = genCategory()
        db.upsertCategory(cat)
        val list = genList(true)
        list.categories = mutableListOf(cat)
        val entries = generateEntries(list)
        db.upsertVList(list)
        db.upsertEntries(entries)

        val listT = db.lists.find { v -> v.id == list.id }
        Assert.assertNotNull(listT)
        Assert.assertNotNull(listT!!.categories)
        val catT = listT.categories!![0]
        Assert.assertEquals(cat,catT)
    }

    @Test
    fun testTrainingDataInsert() {
        val db = Database(context)
        val list = genList(true)
        val entries = generateEntries(list)
        val insTime = System.currentTimeMillis()
        db.upsertVList(list)
        db.upsertEntries(entries)

        db.insertEntryStat(insTime,entries[0],true,false)
        val stats = db.entryStats(insTime)
        val found = stats.find { v -> v.date == insTime }
        Assert.assertNotNull(found)
        Assert.assertEquals(EntryStat(entryUUID = entries[0].uuid!!,tipNeeded = true,isCorrect = false,date = insTime),found)
    }

    @Test
    fun testSettings() {
        val db = Database(context)
        val setting = genSetting()
        Assert.assertNull(db.getSetting(setting.first))
        db.setSetting(setting.first,setting.second)
        Assert.assertEquals(setting.second,db.getSetting(setting.first))
    }
}