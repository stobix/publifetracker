package stobix.view.containerview;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import stobix.view.containerview.AltAltContainerInstrumentedTestTests;

import static junit.framework.Assert.assertTrue;

/**
 * Created by stobix on 2018-03-07.
 */

@RunWith(AndroidJUnit4.class)
public class AltAltContainerInstrumentedTest {

    AltAltDatabase db;
    AltAltContainerDao dao;

    @Before
    public void createDB(){
        System.out.println("Initiating");
        Context ctx = InstrumentationRegistry.getTargetContext();
        db = Room.inMemoryDatabaseBuilder(ctx,AltAltDatabase.class).build();
        dao = db.containerDao();
    }

    @Test
    public void dbTest(){
        AltAltContainerInstrumentedTestTests tests = new AltAltContainerInstrumentedTestTests(db,dao);
        assertTrue(tests.setEntries());
    }

    @After
    public void closeDB(){
        System.out.println("cleaning up");
        db.close();
    }
}
