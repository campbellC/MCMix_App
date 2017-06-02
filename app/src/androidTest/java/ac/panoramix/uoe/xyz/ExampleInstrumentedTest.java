package ac.panoramix.uoe.xyz;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libsodium.jni.crypto.Point;

import java.util.Arrays;

import ac.panoramix.uoe.xyz.Accounts.Account;
import ac.panoramix.uoe.xyz.Accounts.Buddy;
import ac.panoramix.uoe.xyz.MessageHandling.Diffie_Hellman;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("ac.panoramix.uoe.xyz", appContext.getPackageName());
    }

    @Test
    public void dead_drops_are_equal() throws Exception{
        Account Alice = new Account("Alice", "password");
        Account Bob = new Account("Bob", "password");
        Buddy b_buddy = new Buddy("Bob", Bob.getKeyPair().getPublicKey());
        Buddy a_buddy = new Buddy("Alice", Alice.getKeyPair().getPublicKey());

        for (long r = 0; r < 100; ++r) {
            byte[] Alice_dd = Diffie_Hellman.dead_drop(Alice, b_buddy, r);
            byte[] Bob_dd = Diffie_Hellman.dead_drop(Bob, a_buddy, r);
            assertEquals(Alice_dd.length, Bob_dd.length);
            assertEquals(Alice_dd.length,XYZConstants.DEAD_DROP_LENGTH);
            assertTrue(Arrays.equals(Alice_dd, Bob_dd));
        }
    }
    @Test
    public void dh_shared_secrets_are_equal() throws Exception{
        Account Alice = new Account("Alice", "password");
        Account Bob = new Account("Bob", "password");
        Buddy b_buddy = new Buddy("Bob", Bob.getKeyPair().getPublicKey());
        Buddy a_buddy = new Buddy("Alice", Alice.getKeyPair().getPublicKey());

        Point Alice_secret = Diffie_Hellman.shared_secret(Alice.getKeyPair(), b_buddy.getPublic_key());
        Point Bob_secret =  Diffie_Hellman.shared_secret(Bob.getKeyPair(), a_buddy.getPublic_key());
        assertEquals(Alice_secret.toString(), Bob_secret.toString());
        Log.d("testing", Alice_secret.toString());
    }
}
