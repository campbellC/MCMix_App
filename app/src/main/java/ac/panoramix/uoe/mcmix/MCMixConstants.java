package ac.panoramix.uoe.mcmix;

import org.libsodium.jni.SodiumConstants;

/**
 * Created by: Chris Campbell
 * on: 02/06/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

public final class MCMixConstants {

    public static final String ACCOUNT_STORAGE_FILE = "ACCOUNT_STORAGE.ser";

    public static int USERNAME_LENGTH_IN_UINTS = 1;
    public static int USERNAME_LENGTH_IN_CHARS = USERNAME_LENGTH_IN_UINTS * 8;


    public static final int C_MESSAGE_BYTES = 160; // length in bytes of a conversation message
    public static final int DEAD_DROP_BYTES = 32; // sha256 has 256 bits = 32 bytes
    public static final int C_CIPHERTEXT_BYTES = C_MESSAGE_BYTES + 16; // length in bytes of encrypted messages. //TODO: find proof that this is correct fomula
    public static final int CONVERSATION_PAYLOAD_BYTES = DEAD_DROP_BYTES + SodiumConstants.NONCE_BYTES + C_CIPHERTEXT_BYTES;
    public static final int CONVERSATION_PAYLOAD_LENGTH = CONVERSATION_PAYLOAD_BYTES / 8;
    public static final int MAX_MESSAGES_IN_QUEUE = 50;





    public static final String BUDDY_EXTRA = "ac.panoramix.uoe.mcmix.BUDDY_EXTRA";

    public static final String BUDDY_ADDED_BROADCAST_TAG = "ac.panoramix.uoe.mcmix.BUDDY_ADDED";
    public static final String MESSAGES_UPDATED_BROADCAST_TAG = "ac.panoramix.uoe.mcmix.MESSAGES_UPDATED";
    public static final String DIAL_ADDED_BROADCAST_TAG = "ac.panoramix.uoe.mcmix.INCOMING_DIAL_RECEIVED";


}
