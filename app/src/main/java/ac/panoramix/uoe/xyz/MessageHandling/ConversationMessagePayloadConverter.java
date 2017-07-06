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

    public ConversationMessage encryptedPayloadToMessage(String payload){
        payload = payload.trim();
        Log.d("ConvMsgConverter","decrypting payload: " + payload);
        return encryptedBytesToMessage(Utility.bytes_from_string(payload));
    }
    private ConversationMessage encryptedBytesToMessage(byte[] payload) {
        assert payload.length == XYZConstants.CONVERSATION_PAYLOAD_BYTES;
        //First we extract the nonce and ciphertexts from the message. See specs for message format
        byte[] nonce = new byte[SodiumConstants.NONCE_BYTES];
        byte[] ciphertext = new byte[XYZConstants.C_CIPHERTEXT_BYTES];

        System.arraycopy(payload,
                XYZConstants.DEAD_DROP_BYTES,
                nonce, 0, SodiumConstants.NONCE_BYTES);

        System.arraycopy(payload,
                 XYZConstants.DEAD_DROP_BYTES + SodiumConstants.NONCE_BYTES,
                ciphertext, 0, XYZConstants.C_CIPHERTEXT_BYTES);

        //Now we decrypt the ciphertext
        return new ConversationMessage(mBox.decrypt(nonce, ciphertext), false);
    }

    public String constructNullMessagePayload(long round_number){
        return constructOutgoingPayload(new ConversationMessage("", true), round_number);
    }

    private byte[] constructOutgoingPayloadBytes(ConversationMessage msg, long round_number){
        byte[] dead_drop = Diffie_Hellman.dead_drop(Alice, Bob, round_number);

        //when we have a dead drop and a message, construct into an outgoing payload for the protocol
        byte[] payload = new byte[XYZConstants.CONVERSATION_PAYLOAD_BYTES];

        // add dead_drop to start of payload
        System.arraycopy(dead_drop, 0, payload, 0, XYZConstants.DEAD_DROP_BYTES);

        //Now pick a nonce and add the message
        byte[] nonce = new Random().randomBytes(SodiumConstants.NONCE_BYTES);
        System.arraycopy(nonce, 0, payload, XYZConstants.DEAD_DROP_BYTES, SodiumConstants.NONCE_BYTES);

        //Now encrypt message, and add to the payload
        byte[] ciphertext = mBox.encrypt(nonce, msg.getBytes());
        assert ciphertext.length == XYZConstants.C_CIPHERTEXT_BYTES;
        System.arraycopy(ciphertext, 0,
                payload, XYZConstants.DEAD_DROP_BYTES + SodiumConstants.NONCE_BYTES,
                XYZConstants.C_CIPHERTEXT_BYTES);
        return payload;
    }

    public String constructOutgoingPayload(ConversationMessage msg, long round_number){
        return Utility.string_from_bytes(constructOutgoingPayloadBytes(msg, round_number));
    }

}
