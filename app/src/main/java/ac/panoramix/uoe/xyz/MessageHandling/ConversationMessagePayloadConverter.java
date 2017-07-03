package ac.panoramix.uoe.xyz.MessageHandling;

import android.util.Log;

import org.libsodium.jni.SodiumConstants;
import org.libsodium.jni.crypto.Random;
import org.libsodium.jni.crypto.SecretBox;

import ac.panoramix.uoe.xyz.Accounts.Account;
import ac.panoramix.uoe.xyz.Accounts.Buddy;
import ac.panoramix.uoe.xyz.Utility;
import ac.panoramix.uoe.xyz.XYZConstants;

/**
 * Created by: Chris Campbell
 * on: 08/06/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

/* This class is the only class that knows and understands the format for
conversation message payloads.
 */

public class ConversationMessagePayloadConverter {
    Account Alice;
    Buddy Bob;
    SecretBox mBox;

    public ConversationMessagePayloadConverter(Account alice, Buddy bob){
        Alice = alice;
        Bob = bob;
        mBox = new SecretBox(
                Diffie_Hellman.shared_secret(alice, bob).toBytes());
    }


    public int tagged_payload_to_tag(String payload){
        payload = payload.trim();
        return Integer.parseInt(payload.substring(0, payload.indexOf(" ")));
    }

    public String tagged_payload_to_payload(String tagged_payload){
        tagged_payload = tagged_payload.trim();
        return tagged_payload.substring(tagged_payload.indexOf(" ")).trim();
    }

    public ConversationMessage tagged_payload_to_message(String tagged_payload){
        tagged_payload = tagged_payload.trim();
        Log.d("ConvMsgConverter","converting payload: " + tagged_payload);
        // First extract the tag
        // Then we extract the rest of the message

        String payload = tagged_payload_to_payload(tagged_payload);

        return payload_to_message(Utility.bytes_from_string(payload));
    }
    private ConversationMessage payload_to_message(byte[] incoming_payload) {
        assert incoming_payload.length == XYZConstants.INCOMING_CONVERSATION_PAYLOAD_LENGTH - 1;
        //First we extract the nonce and ciphertexts from the message. See specs for message format
        byte[] nonce = new byte[SodiumConstants.NONCE_BYTES];
        byte[] ciphertext = new byte[XYZConstants.CIPHERTEXT_LENGTH];

        System.arraycopy(incoming_payload,
                XYZConstants.DEAD_DROP_LENGTH,
                nonce, 0, SodiumConstants.NONCE_BYTES);

        System.arraycopy(incoming_payload,
                 XYZConstants.DEAD_DROP_LENGTH + SodiumConstants.NONCE_BYTES,
                ciphertext, 0, XYZConstants.CIPHERTEXT_LENGTH);

        //Now we decrypt the ciphertext
        return new ConversationMessage(mBox.decrypt(nonce, ciphertext), false);
    }

    public String construct_null_message_payload(long round_number){
        return construct_outgoing_payload(new ConversationMessage("", true), round_number);
    }

    private byte[] construct_outgoing_payload_bytes(ConversationMessage msg, long round_number){
        byte[] dead_drop = Diffie_Hellman.dead_drop(Alice, Bob, round_number);

        //when we have a dead drop and a message, construct into an outgoing payload for the protocol
        byte[] payload = new byte[XYZConstants.OUTGOING_CONVERSATION_PAYLOAD_LENGTH];

        // add dead_drop to start of payload
        System.arraycopy(dead_drop, 0, payload, 0, XYZConstants.DEAD_DROP_LENGTH);

        //Now pick a nonce and add the message
        byte[] nonce = new Random().randomBytes(SodiumConstants.NONCE_BYTES);
        System.arraycopy(nonce, 0, payload, XYZConstants.DEAD_DROP_LENGTH, SodiumConstants.NONCE_BYTES);

        //Now encrypt message, and add to the payload
        byte[] ciphertext = mBox.encrypt(nonce, msg.getBytes());
        assert ciphertext.length == XYZConstants.CIPHERTEXT_LENGTH;
        System.arraycopy(ciphertext, 0,
                payload, XYZConstants.DEAD_DROP_LENGTH + SodiumConstants.NONCE_BYTES,
                XYZConstants.CIPHERTEXT_LENGTH);
        return payload;
    }

    public String construct_outgoing_payload(ConversationMessage msg, long round_number){
        return Utility.string_from_bytes(construct_outgoing_payload_bytes(msg, round_number));
    }

}
