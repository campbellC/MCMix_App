package ac.panoramix.uoe.mcmix;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.google.common.base.Splitter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libsodium.jni.SodiumConstants;
import org.libsodium.jni.crypto.Point;
import org.libsodium.jni.crypto.Random;
import org.libsodium.jni.keys.KeyPair;
import org.libsodium.jni.keys.PublicKey;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import ac.panoramix.uoe.mcmix.Accounts.Account;
import ac.panoramix.uoe.mcmix.Accounts.Buddy;
import ac.panoramix.uoe.mcmix.ConversationProtocol.ConversationMessage;
import ac.panoramix.uoe.mcmix.ConversationProtocol.ConversationMessagePayloadConverter;
import ac.panoramix.uoe.mcmix.ConversationProtocol.DiffieHellman;
import ac.panoramix.uoe.mcmix.Database.BuddyBase;
import ac.panoramix.uoe.mcmix.Database.ConversationBase;

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

        assertEquals("ac.panoramix.uoe.mcmix", appContext.getPackageName());
    }

    @Test
    public void dead_drops_are_equal() throws Exception{
        Account Alice = new Account("Alice");
        Account Bob = new Account("Bob");
        Buddy b_buddy = new Buddy("Bob", Bob.getKeyPair().getPublicKey());
        Buddy a_buddy = new Buddy("Alice", Alice.getKeyPair().getPublicKey());

        for (long r = 0; r < 100; ++r) {
            byte[] Alice_dd = DiffieHellman.dead_drop(Alice, b_buddy, r);
            byte[] Bob_dd = DiffieHellman.dead_drop(Bob, a_buddy, r);
            assertEquals(Alice_dd.length, Bob_dd.length);
            assertEquals(Alice_dd.length, MCMixConstants.DEAD_DROP_BYTES);
            assertTrue(Arrays.equals(Alice_dd, Bob_dd));
        }
    }
    @Test
    public void dh_shared_secrets_are_equal() throws Exception{
        Account Alice = new Account("Alice");
        Account Bob = new Account("Bob");
        Buddy b_buddy = new Buddy("Bob", Bob.getKeyPair().getPublicKey());
        Buddy a_buddy = new Buddy("Alice", Alice.getKeyPair().getPublicKey());

        Point Alice_secret = DiffieHellman.shared_secret(Alice, b_buddy);
        Point Bob_secret =  DiffieHellman.shared_secret(Bob, a_buddy);
        assertEquals(Alice_secret.toString(), Bob_secret.toString());
        Log.d("testing", Alice_secret.toString());
    }


    @Test
    public void message_to_bytes_and_back_again() throws Exception {
        String s_m1 = "Hi there my name is chris. This, is a ! test Message";
        byte[] s_m1_bytes = s_m1.getBytes(ConversationMessage.sEncoding);
        String s_m2 = new String(s_m1_bytes,ConversationMessage.sEncoding);
        assertEquals(s_m1, s_m2);

        ConversationMessage message1 = new ConversationMessage(s_m1, true);
        byte[] m1_bytes = message1.getBytes();
        ConversationMessage message2 = new ConversationMessage(m1_bytes, false);
        assertEquals(message1, message2);
    }


    @Test
    public void empty_string_message_handling() throws Exception {
        ConversationMessage msg = new ConversationMessage("", true);
        byte[] bytes = msg.getBytes();
        assertEquals(bytes.length, MCMixConstants.C_MESSAGE_BYTES);

        ConversationMessage msg2 = new ConversationMessage(bytes, false);
        assertEquals(msg, msg2);
        assertEquals(msg2.getMessage(), "");

    }
    @Test
    public void conv_payload_to_msg_test() throws Exception {
        Account Alice = new Account("Alice");
        Account Bob = new Account("Bob");
        Buddy b_buddy = new Buddy("Bob", Bob.getKeyPair().getPublicKey());
        Buddy a_buddy = new Buddy("Alice", Alice.getKeyPair().getPublicKey());

        ConversationMessagePayloadConverter alice_converter = new ConversationMessagePayloadConverter(Alice, b_buddy);
        ConversationMessagePayloadConverter bob_converter = new ConversationMessagePayloadConverter(Bob, a_buddy);
        // generate a message from alice to bob. create payload

        ConversationMessage alice_msg = new ConversationMessage("This is a random string 09432802938470928374", true);
        String payload = alice_converter.constructOutgoingPayload(alice_msg, 203948l);

        assertEquals(payload.split(" ").length, MCMixConstants.CONVERSATION_PAYLOAD_LENGTH);

        // get bob to decrypt and check messages are equal
        ConversationMessage bob_msg = bob_converter.encryptedPayloadToMessage(payload);
        assertEquals(bob_msg, alice_msg);

    }



    @Test
    public void public_key_to_string_and_back_test() throws Exception {
        for(int i = 0; i < 100; ++i) {
            byte[] seed = new Random().randomBytes(SodiumConstants.SECRETKEY_BYTES);
            KeyPair kp = new KeyPair(seed);
            PublicKey pk1 = kp.getPublicKey();
            byte[] bytes1 = pk1.toBytes();
            String server_side_pk = Utility.uint_string_from_bytes(bytes1);
            byte[] bytes2 = Utility.bytes_from_uint_string(server_side_pk);
            assertArrayEquals(bytes1, bytes2);
            PublicKey pk2 = new PublicKey(bytes2);
            assertArrayEquals(pk1.toBytes(), pk2.toBytes());


        }
    }
    @Test
    public void keyPair_seed_test() throws Exception {
        for(int i = 0; i < 100; ++i) {
            byte[] seed = new Random().randomBytes(SodiumConstants.SECRETKEY_BYTES);
            KeyPair kp = new KeyPair(seed);
            KeyPair kp2 = new KeyPair(seed);
            assertArrayEquals(kp.getPrivateKey().toBytes(), kp2.getPrivateKey().toBytes());
            assertArrayEquals(kp.getPublicKey().toBytes(), kp2.getPublicKey().toBytes());
        }
    }

    @Test
    public void buddy_serialization() throws Exception {
        Account Bob = new Account("Bob");
        Buddy b_buddy = new Buddy("Bob", Bob.getKeyPair().getPublicKey());
        new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(b_buddy);
        File file;
        String filename = "buddy_test.ser";
        file = File.createTempFile(filename, null, InstrumentationRegistry.getTargetContext().getCacheDir());
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(b_buddy);
        oos.close();

        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis) ;
        Buddy b_copy = (Buddy) ois.readObject();
        ois.close();

        assertArrayEquals(b_copy.getPublic_key().toBytes(), b_buddy.getPublic_key().toBytes());
        assertEquals(b_buddy.getUsername(), b_copy.getUsername());


    }
    @Test
    public void message_serialization() throws Exception {
        Account Alice = new Account("Alice");
        ConversationMessage msg_from_alice = new ConversationMessage("test message", true);
        ConversationMessage msg_from_bob = new ConversationMessage("test message 2", false);

        new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(msg_from_alice);
        File file;
        String filename = "msg_from_alice.ser";
        file = File.createTempFile(filename, null, InstrumentationRegistry.getTargetContext().getFilesDir());
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(msg_from_alice);
        oos.close();

        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis) ;
        ConversationMessage a_copy = (ConversationMessage) ois.readObject();
        ois.close();

        assertEquals(a_copy,msg_from_alice);
        assertTrue(a_copy.isFrom_alice());

        filename = "msg_from_bob.ser";
        file = File.createTempFile(filename, null, InstrumentationRegistry.getTargetContext().getCacheDir());
        fos = new FileOutputStream(file);
        oos = new ObjectOutputStream(fos);
        oos.writeObject(msg_from_bob);
        oos.close();

        fis = new FileInputStream(file);
        ois = new ObjectInputStream(fis) ;
        ConversationMessage b_copy = (ConversationMessage) ois.readObject();
        ois.close();

        assertEquals(b_copy,msg_from_bob);
        assertTrue(!b_copy.isFrom_alice());


    }
    @Test
    public void account_serialization() throws Exception {
        Account Alice = new Account("Alice");

        new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(Alice);
        File file;
        String filename = "account_test.ser";
        file = File.createTempFile(filename, null, InstrumentationRegistry.getTargetContext().getCacheDir());
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(Alice);
        oos.close();

        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis) ;
        Account a_copy = (Account) ois.readObject();
        ois.close();

        assertArrayEquals(a_copy.getKeyPair().getPrivateKey().toBytes(), Alice.getKeyPair().getPrivateKey().toBytes());
        assertArrayEquals(a_copy.getKeyPair().getPublicKey().toBytes(), Alice.getKeyPair().getPublicKey().toBytes());


    }




    @Test
    public void string_to_bytes_test() throws Exception {
        String test_str = "0 0 0 1 9 8 7 6 5 4 6 8 6 636732648 276 23876 28476384761284 761253 7165";
        byte[] bytes = Utility.bytes_from_uint_string(test_str);
        String reconstructed = Utility.uint_string_from_bytes(bytes);
        assertEquals(test_str, reconstructed);

    }
    @Test
    public void bytes_to_string_test() throws Exception {
        byte[] bytes = {0,0,0,0,1,1,1,1,0,0,0,0,3,3,3,3,0,1,0,1,0,0,3,4};
        String test_str = Utility.uint_string_from_bytes(bytes);
        byte[] reconstructed = Utility.bytes_from_uint_string(test_str);
        assertArrayEquals(bytes, reconstructed);
    }
    @Test
    public void usernames_to_uints_and_back_test() throws Exception {
        String[] usernames = {"bob", "alice", "alicebobandalice", "frenchalice!@#$!%#%$%!$!@$@#%$%$#%!@$!"};
        Log.d("testing", "starting conversions");
        for(String name : usernames){
            Log.d("testing", "converting: " + name);
            String uints = Utility.UInt_String_From_String(name);
            Log.d("testing", "uints is : " + uints);
            String new_user = Utility.String_From_UInt_String(uints);
            Log.d("testing", "converted name is : " + new_user);
            assertEquals(name, new_user);
        }
    }

    @Test
    public void buddy_database_test() throws Exception {
        Account bobA = new Account("test_Bob");
        Buddy buddy = new Buddy(bobA.getUsername(), bobA.getKeyPair().getPublicKey());
        BuddyBase base = BuddyBase.getOrCreateInstance(InstrumentationRegistry.getTargetContext());
        base.addBuddy(buddy);
        Buddy new_bob = base.getBuddy(bobA.getUsername());
        assertEquals(new_bob.getUsername(), buddy.getUsername());
        assertArrayEquals(new_bob.getPublic_key().toBytes(), buddy.getPublic_key().toBytes());
    }

    @Test
    public void conversation_database_test() throws Exception {
        Account bobA = new Account("test_Bob2");
        Buddy buddy = new Buddy(bobA.getUsername(), bobA.getKeyPair().getPublicKey());
        BuddyBase base = BuddyBase.getOrCreateInstance(InstrumentationRegistry.getTargetContext());
        base.addBuddy(buddy);
        ConversationMessage in_msg = new ConversationMessage("hi alice", true);
        assertFalse(in_msg.wasSent());
        ConversationBase conversationBase = ConversationBase.getOrCreateInstance(InstrumentationRegistry.getTargetContext());
        conversationBase.addMessage(in_msg, buddy);
        ConversationMessage out_msg = conversationBase.getMessage(in_msg.getUuid());
        assertEquals(in_msg.getMessage(), out_msg.getMessage());
        assertEquals(in_msg.isFrom_alice(), out_msg.isFrom_alice());
        assertEquals(in_msg.getTimestamp(), out_msg.getTimestamp());
        assertEquals(in_msg.getUuid(), out_msg.getUuid());
        assertEquals(in_msg.wasSent(), out_msg.wasSent());
        conversationBase.setMessageSent(in_msg.getUuid(), buddy);
        out_msg = conversationBase.getMessage(in_msg.getUuid());
        assertTrue(out_msg.wasSent());


        in_msg = new ConversationMessage("hi alice", false);
        conversationBase.addMessage(in_msg, buddy);
        out_msg = conversationBase.getMessage(in_msg.getUuid());
        assertEquals(in_msg.getMessage(), out_msg.getMessage());
        assertEquals(in_msg.isFrom_alice(), out_msg.isFrom_alice());
        assertEquals(in_msg.getTimestamp(), out_msg.getTimestamp());
        assertEquals(in_msg.getUuid(), out_msg.getUuid());
        assertEquals(in_msg.wasSent(), out_msg.wasSent());

    }


    @Test
    public void splitting_messages_test() throws Exception {
        String payload = "this is a test string that is over 320 characters long.this is a test string that is over 320 characters long.this is a test string that is over 320 characters long.this is a test string that is over 320 characters long.this is a test string that is over 320 characters long.this is a test string that is over 320 characters long.this is a test string that is over 320 characters long.this is a test string that is over 320 characters long.this is a test string that is over 320 characters long.this is a test string that is over 320 characters long.this is a test string that is over 320 characters long.this is a test string that is over 320 characters long.this is a test string that is over 320 characters long.this is a test string that is over 320 characters long.this is a test string that is over 320 characters long.this is a test string that is over 320 characters long.this is a test string that is over 320 characters long.this is a test string that is over 320 characters long.this is a test string that is over 320 characters long.                   this is a test string that is over 320 characters long. ";
        for(String s : Splitter.fixedLength(MCMixConstants.C_MESSAGE_BYTES).split(payload) ){
            assertTrue(s.length() <= MCMixConstants.C_MESSAGE_BYTES);
        }
    }
}
