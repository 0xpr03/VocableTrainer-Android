package vocabletrainer.heinecke.aron.vocabletrainer;

import android.content.Context;
import android.provider.ContactsContract;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Entry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Table;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer;

import static org.junit.Assert.*;

/**
 * Database test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class DBTest {

    private static final String TAG = "UNIT";

    private static final Lock _mutex = new ReentrantLock(true);

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("vocabletrainer.heinecke.aron.vocabletrainer", appContext.getPackageName());
    }

    private Table getTable(){
        return new Table("A","B","C");
    }

    private List<Entry> getEntries(Table tbl){
        List<Entry> entries = new ArrayList<>(100);
        for(int i = 0; i < 100; i++){
            entries.add(new Entry("A"+i,"B"+i,"C"+i,tbl,0));
        }
        return entries;
    }

    @Before
    public void init(){
        _mutex.lock();
        Context appContext = InstrumentationRegistry.getTargetContext();
        Log.d(TAG,"delete DB: "+appContext.deleteDatabase(Database.DB_NAME_DEV));
    }

    @After
    public void end(){
        _mutex.unlock();
    }

    @Test
    public void testDBInit(){
        //Database db = new Database(InstrumentationRegistry.getTargetContext());
    }

    @Test
    public void testDBInsertTable(){
        Database db = new Database(InstrumentationRegistry.getTargetContext(),true);

        Table tbl = getTable();
        assertTrue("UpsertTable",db.upsertTable(tbl));
        List<Table> tbls = db.getTables();
        assertEquals("Invalid amount of entries",1,tbls.size());
        assertTrue("Unequal table after insert",tbl.equals( tbls.get(0)));
    }

    @Test
    public void testDBInsertEntries(){
        Database db = new Database(InstrumentationRegistry.getTargetContext(),true);
        Table tbl = getTable();
        assertTrue("UpsertTable",db.upsertTable(tbl));

        List<Entry> entries = getEntries(tbl);

        assertTrue("UpsertEntries",db.upsertEntries(entries));
        List<Entry> result = db.getVocablesOfTable(tbl);
        assertEquals("invalid amount of entries",entries.size(),result.size());
    }

    @Test
    public void testDBEditEntries(){
        Database db = new Database(InstrumentationRegistry.getTargetContext(),true);
        Table tbl = getTable();
        assertTrue("UpsertTable",db.upsertTable(tbl));

        List<Entry> entries = getEntries(tbl);

        assertTrue("UpsertEntries",db.upsertEntries(entries));
        List<Entry> result = db.getVocablesOfTable(tbl);
        assertEquals("invalid amount of entries",entries.size(),result.size());

        result.get(20).setAWord("New Word");
        result.get(30).setDelete(true);
        assertTrue("UpsertEntries",db.upsertEntries(result));

        List<Entry> edited = db.getVocablesOfTable(tbl);
        assertEquals("invalid amount of entries",entries.size()-1,edited.size());
        assertEquals("invalid entry data", result.get(20).getAWord(),edited.get(20).getAWord());
        assertEquals("invalid entry data", result.get(20).getBWord(),edited.get(20).getBWord());
        assertEquals("invalid entry data", result.get(20).getTip(),edited.get(20).getTip());
    }

    @Test
    public void testDBDelete(){
        Database db = new Database(InstrumentationRegistry.getTargetContext(), true);
        Table tbl = getTable();
        assertTrue("upserttable",db.upsertTable(tbl));
        List<Entry> entries = getEntries(tbl);
        assertTrue("UpsertEntries", db.upsertEntries(entries));

        assertEquals("invalid amount entries", entries.size(), db.getVocablesOfTable(tbl).size());
        assertEquals("invalid amount tables", 1, db.getTables().size());
        assertTrue("delete table",db.deleteTable(tbl));
        assertEquals("invalid amount entries", 0, db.getVocablesOfTable(tbl).size());
        assertEquals("invalid amount tables", 0, db.getTables().size());
    }

    @Test
    public void testDBRandomSelect(){
        Database db = new Database(InstrumentationRegistry.getTargetContext(),true);
        Table tbl = getTable();
        assertTrue("UpsertTable",db.upsertTable(tbl));

        List<Entry> entries = getEntries(tbl);

        assertTrue("UpsertEntries",db.upsertEntries(entries));
        assertNotNull(db.getRandomTrainerEntry(tbl,null,new TrainerSettings(2, Trainer.TEST_MODE.RANDOM,true, true),true));
    }

    @Test
    public void testDBEntryPointsInsert(){
        Database db = new Database(InstrumentationRegistry.getTargetContext(),true);
        Table tbl = getTable();
        assertTrue("UpsertTable",db.upsertTable(tbl));

        List<Entry> entries = getEntries(tbl);

        assertTrue("UpsertEntries",db.upsertEntries(entries));
        Entry ent = entries.get(0);
        ent.setPoints(2);
        assertEquals(2,ent.getPoints());
        assertTrue(db.updateEntryProgress(ent));
        assertEquals("table points",ent.getPoints(),db.getEntryPoints(ent));

        ent.setPoints(3);
        assertTrue(db.updateEntryProgress(ent));
        assertEquals("upd table points",ent.getPoints(),db.getEntryPoints(ent));
    }

    /**
     * Test for db random select to be
     * a) not repetitive, given a previous element as param
     * b) not selecting entries for which the points are matching the finished criteria
     */
    @Test
    public void testDbEntryRandomSelect(){
        final int points = 1;
        Database db = new Database(InstrumentationRegistry.getTargetContext(),true);
        Table tbl = getTable();
        assertTrue("UpsertTable",db.upsertTable(tbl));

        List<Entry> entries = getEntries(tbl);
        entries = entries.subList(0,2);
        assertEquals(2,entries.size());

        assertTrue("UpsertEntries",db.upsertEntries(entries));

        TrainerSettings settings = new TrainerSettings(points, Trainer.TEST_MODE.RANDOM,true,true);

        Entry chosen = db.getRandomTrainerEntry(tbl,null,settings,false);
        assertNotNull(chosen);

        Entry secondChosen = db.getRandomTrainerEntry(tbl,chosen,settings,false);
        assertNotNull(secondChosen);
        assertNotEquals("selected same entry twice",chosen.getId(),secondChosen.getId());

        chosen.setPoints(points);
        assertEquals(points,chosen.getPoints());
        assertTrue(db.updateEntryProgress(chosen));
        assertEquals("table points",chosen.getPoints(),db.getEntryPoints(chosen));

        Entry thirdChosen = db.getRandomTrainerEntry(tbl,null,settings,false);
        assertNotNull(thirdChosen);
        assertNotEquals("selected entry with reached points",chosen.getId(),thirdChosen.getId());

        thirdChosen.setPoints(points);
        assertTrue(db.updateEntryProgress(thirdChosen));

        Entry fourthChosen = db.getRandomTrainerEntry(tbl,null,settings,false);
        assertNull(fourthChosen);
    }
}
