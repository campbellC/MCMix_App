package ac.panoramix.uoe.mcmix.ConversationProtocol;

import android.util.Log;

import com.google.common.base.Splitter;

import org.libsodium.jni.SodiumConstants;
import org.libsodium.jni.crypto.Random;
import org.libsodium.jni.crypto.SecretBox;

import java.util.Date;

import ac.panoramix.uoe.mcmix.Accounts.Account;
import ac.panoramix.uoe.mcmix.Accounts.Buddy;
import ac.panoramix.uoe.mcmix.MCMixConstants;
import ac.panoramix.uoe.mcmix.Utility;

/**
 * Created by: Chris Campbell
 * on: 08/06/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

/* This class is the only class that knows and understands the format for
    conversation message payloads. It's primary function is to convert from the server
    side format of Sequences of uint's to a ConversationMessage. In order to do this
    it must also handle the symmetric encryption of the messages between Alice and Bob.
 */

public class ConversationPayloadMaker {
    Account Alice;
    Buddy Bob;
    /* SecretBox is the NaCl simple authenticated symmetric encryption class */
    SecretBox mBox;

    public ConversationPayloadMaker(Account alice, Buddy bob){
        Alice = alice;
        Bob = bob;
        mBox = new SecretBox(
                DiffieHellman.shared_secret(alice, bob).toBytes());
    }

    /* METHODS FOR HANDLING INCOMING MESSAGES */
    /*
        This function checks for the Dead-Drop UINTs being all 0. This is the
        modification to the protocol made by me (Chris Campbell).  This is not
        an authentication check, merely establishes that the message was acted upon by
        the protocol.
     */
    public boolean encryptedPayloadIsFromBob(String payload){
        int i = 0;
        for(String s: Splitter.on(' ').split(payload)){
            if(i >= MCMixConstants.DEAD_DROP_UINTS) break;
            i++;
            if(!s.equals("0")) return false;
        }
        return true;
    }


    public ConversationMessage encryptedPayloadToMessage(String payload){
        payload = payload.trim();
        ConversationMessage msg = encryptedBytesToMessage(Utility.bytes_from_uint_string(payload));
        return msg;
    }

    /* helper function for encryptedPayloadToMessage */
    private ConversationMessage encryptedBytesToMessage(byte[] payload) {
        assert payload.length == MCMixConstants.CONVERSATION_PAYLOAD_BYTES;
        //First we extract the nonce and ciphertexts from the message. See specs for message format
        byte[] nonce = new byte[SodiumConstants.NONCE_BYTES];
        byte[] ciphertext = new byte[MCMixConstants.C_CIPHERTEXT_BYTES];

        System.arraycopy(payload,
                MCMixConstants.DEAD_DROP_BYTES,
                nonce, 0, SodiumConstants.NONCE_BYTES);

        System.arraycopy(payload,
                 MCMixConstants.DEAD_DROP_BYTES + SodiumConstants.NONCE_BYTES,
                ciphertext, 0, MCMixConstants.C_CIPHERTEXT_BYTES);

        //Now we decrypt the ciphertext
        ConversationMessage msg = new ConversationMessage(mBox.decrypt(nonce, ciphertext), false);
        msg.setTimestamp(new Date());
        return msg;
    }


    /* METHODS FOR CONSTRUCTING OUTGOING MESSAGES */
    /* This method is for when the user wants to send a message in this next round of conversation */
    public String constructOutgoingPayload(ConversationMessage msg, long round_number){
        return Utility.uint_string_from_bytes(constructOutgoingPayloadBytes(msg, round_number));
    }

    /* Helper function for constructOutgoingPayload*/
    private byte[] constructOutgoingPayloadBytes(ConversationMessage msg, long round_number){
        byte[] dead_drop = DiffieHellman.dead_drop(Alice, Bob, round_number);

        //when we have a dead drop and a message, construct into an outgoing payload for the protocol
        byte[] payload = new byte[MCMixConstants.CONVERSATION_PAYLOAD_BYTES];

        // add dead_drop to start of payload
        System.arraycopy(dead_drop, 0, payload, 0, MCMixConstants.DEAD_DROP_BYTES);

        //Now pick a nonce and add the message
        byte[] nonce = new Random().randomBytes(SodiumConstants.NONCE_BYTES);
        System.arraycopy(nonce, 0, payload, MCMixConstants.DEAD_DROP_BYTES, SodiumConstants.NONCE_BYTES);

        //Now encrypt message, and add to the payload
        byte[] ciphertext = mBox.encrypt(nonce, msg.getBytes());
        assert ciphertext.length == MCMixConstants.C_CIPHERTEXT_BYTES;
        System.arraycopy(ciphertext, 0,
                payload, MCMixConstants.DEAD_DROP_BYTES + SodiumConstants.NONCE_BYTES,
                MCMixConstants.C_CIPHERTEXT_BYTES);
        return payload;
    }

    /* This method is used when the user has not submitted any messages to send this round but
        is still in conversation.
     */
    public String constructNullMessagePayload(long round_number){
        return constructOutgoingPayload(new ConversationMessage("", true), round_number);
    }

}
