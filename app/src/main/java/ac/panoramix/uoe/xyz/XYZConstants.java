package ac.panoramix.uoe.xyz;

import org.libsodium.jni.SodiumConstants;

/**
 * Created by: Chris Campbell
 * on: 02/06/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

public final class XYZConstants {

    public static final int INCOMING_CONVERSATION_TAG_COLUMN = 0; //The position in terms of integers that the tag will take in incoming messages

    public static final int C_MESSAGE_BYTES = 160; // length in bytes of a conversation message
    public static final int DEAD_DROP_BYTES = 32; // sha256 has 256 bits = 32 bytes
    public static final int C_CIPHERTEXT_BYTES = C_MESSAGE_BYTES + 16; // length in bytes of encrypted messages. //TODO: find proof that this is correct fomula
    public static final int CONVERSATION_PAYLOAD_BYTES = DEAD_DROP_BYTES + SodiumConstants.NONCE_BYTES + C_CIPHERTEXT_BYTES;
    public static final int CONVERSATION_PAYLOAD_LENGTH = CONVERSATION_PAYLOAD_BYTES / 8;
    public static final int MAX_MESSAGES_IN_QUEUE = 50;


    public static final int SALT_LENGTH = 32;




    public static final String MESSAGE_ADDED_BROADCAST_TAG = "ac.panoramix.uoe.xyz.MESSAGE_ADDED_TO_CONVERSATION";

    //TODO: this should be a domain name presumably
    public static final String SERVER_IP_ADDRESS = "129.215.164.45";
    public static final int SERVER_PORT = 5013;

}
