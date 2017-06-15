package ac.panoramix.uoe.xyz;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libsodium.jni.SodiumConstants;
import org.libsodium.jni.crypto.Point;
import org.libsodium.jni.crypto.Random;
import org.libsodium.jni.keys.KeyPair;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import ac.panoramix.uoe.xyz.Accounts.Account;
import ac.panoramix.uoe.xyz.Accounts.Buddy;
import ac.panoramix.uoe.xyz.MessageHandling.ConversationMessage;
import ac.panoramix.uoe.xyz.MessageHandling.ConversationMessagePayloadConverter;
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

        Point Alice_secret = Diffie_Hellman.shared_secret(Alice, b_buddy);
        Point Bob_secret =  Diffie_Hellman.shared_secret(Bob, a_buddy);
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
        assertEquals(bytes.length, XYZConstants.MESSAGE_LENGTH);

        ConversationMessage msg2 = new ConversationMessage(bytes, false);
        assertEquals(msg, msg2);
        assertEquals(msg2.getMessage(), "");

    }
    @Test
    public void conv_payload_to_msg_test() throws Exception {
        Account Alice = new Account("Alice", "password");
        Account Bob = new Account("Bob", "password");
        Buddy b_buddy = new Buddy("Bob", Bob.getKeyPair().getPublicKey());
        Buddy a_buddy = new Buddy("Alice", Alice.getKeyPair().getPublicKey());

        ConversationMessagePayloadConverter alice_converter = new ConversationMessagePayloadConverter(Alice, b_buddy);
        ConversationMessagePayloadConverter bob_converter = new ConversationMessagePayloadConverter(Bob, a_buddy);
        // generate a message from alice to bob. create payload

        ConversationMessage alice_msg = new ConversationMessage("This is a random string 09432802938470928374", true);
        byte[] payload = alice_converter.construct_outgoing_payload(alice_msg, 203948l);

        //mimic tagging by the server by prepending a 1 tag
        byte[] tagged_payload =new byte[XYZConstants.INCOMING_CONVERSATION_PAYLOAD_LENGTH];
        tagged_payload[0] = XYZConstants.CONVERSATION_MESSAGE_TAG;
        System.arraycopy(payload, 0,
                tagged_payload, XYZConstants.INCOMING_CONVERSATION_TAG_OFFSET,
                XYZConstants.OUTGOING_CONVERSATION_PAYLOAD_LENGTH);


        // get bob to decrypt and check messages are equal
        ConversationMessage bob_msg = bob_converter.payload_to_message(tagged_payload);
        assertEquals(bob_msg, alice_msg);



    }

    @Test
    public void buddy_serialization() throws Exception {
        Account Bob = new Account("Bob", "password");
        Buddy b_buddy = new Buddy("Bob", Bob.getKeyPair().getPublicKey());
        new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(b_buddy);
        File file;
        try{
            String filename = "buddy_test.ser";
            file = File.createTempFile(filename, null, InstrumentationRegistry.getContext().getCacheDir());
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(b_buddy);
            oos.close();

            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis) ;
            Buddy b_copy = (Buddy) ois.readObject();
            ois.close();

            assertEquals(b_copy.getPublic_key(), b_buddy.getPublic_key());
            assertEquals(b_buddy.getUsername(), b_copy.getUsername());
        } catch (IOException e){
            Log.d("Serialisation","Error creating file");
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
    public void account_serialization() throws Exception {
        Account Alice = new Account("Alice", "password");

        new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(Alice);
        File file;
        try{
            String filename = "account_test.ser";
            file = File.createTempFile(filename, null, InstrumentationRegistry.getContext().getCacheDir());
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(Alice);
            oos.close();

            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis) ;
            Account a_copy = (Account) ois.readObject();
            ois.close();

            assertEquals(a_copy.getKeyPair().getPrivateKey(), Alice.getKeyPair().getPrivateKey());
            assertEquals(a_copy.getKeyPair().getPublicKey(), Alice.getKeyPair().getPublicKey());
            assertEquals(a_copy.getHash(), Alice.getHash());
            assertEquals(a_copy.getUsername(), Alice.getUsername());
            assertEquals(a_copy.getSalt(), Alice.getSalt());
        } catch (IOException e){
           Log.d("Serialisation","Error creating file");
        }


    }


}
