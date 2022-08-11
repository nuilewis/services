package org.opendatakit.services.resolve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.widget.ArrayAdapter;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendatakit.services.database.OdkConnectionFactoryInterface;
import org.opendatakit.services.database.OdkConnectionFactorySingleton;
import org.opendatakit.services.resolve.listener.ResolutionListener;
import org.opendatakit.services.resolve.task.CheckpointResolutionListTask;
import org.opendatakit.services.resolve.task.ConflictResolutionListTask;
import org.opendatakit.services.resolve.views.components.ResolveRowEntry;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowEnvironment;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1})
public class ConflictResolutionListTaskTest {

    private final String APP_NAME = "testAppName";
    private final String TABLE_ID = "11";
    private final String RESOLVING_ROWS = "Resolving row 1 of 0";
    private ConflictResolutionListTask conflictResolutionListTask;
    private ResolutionListener resolutionListener;
    private ArrayAdapter<ResolveRowEntry> adapter;
    private String mProgress;

    @Before
    public void setUp() {
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);

        OdkConnectionFactoryInterface odkConnectionFactoryInterface =mock(OdkConnectionFactoryInterface.class);
        OdkConnectionFactorySingleton.set(odkConnectionFactoryInterface);
        adapter = new ArrayAdapter<>(getContext(), 1);

        conflictResolutionListTask = new ConflictResolutionListTask(getContext(), true, APP_NAME);
        conflictResolutionListTask.setResolveRowEntryAdapter(adapter);
        conflictResolutionListTask.execute();

        resolutionListener =mock(ResolutionListener.class);
    }

    @Test
    public void checkIfAppName_isAvailable() {
        conflictResolutionListTask.setAppName(APP_NAME);
        assertEquals(APP_NAME, conflictResolutionListTask.getAppName());
    }

    @Test
    public void checkIfTableID_isVisible() {
        conflictResolutionListTask.setTableId(TABLE_ID);
        assertEquals(TABLE_ID, conflictResolutionListTask.getTableId());

    }

    @Test
    public void checkIfRowEntryAdapter_isResolved() {
        conflictResolutionListTask.setResolveRowEntryAdapter(adapter);
        assertEquals(adapter.getCount(), conflictResolutionListTask.getResolveRowEntryAdapter().getCount());
        mProgress = String.format(RESOLVING_ROWS, 1, adapter.getCount());
        assertEquals(mProgress, conflictResolutionListTask.getProgress());
    }

    @After
    public void tearDown() throws Exception {
        conflictResolutionListTask.cancel(false);

    }
    private Context getContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }
}