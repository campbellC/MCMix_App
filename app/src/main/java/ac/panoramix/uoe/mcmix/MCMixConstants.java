package ac.panoramix.uoe.mcmix;

import org.libsodium.jni.SodiumConstants;

/**
 * Created by: Chris Campbell
 * on: 02/06/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

public final class MCMixConstants {
    /* A store for all of the application wide parameters and settings */

    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final String ACCOUNT_STORAGE_FILE = "ACCOUNT_STORAGE.ser";


    public static int USERNAME_LENGTH_IN_UINTS = 1;
    public static int USERNAME_LENGTH_IN_CHARS = USERNAME_LENGTH_IN_UINTS * 8;


    // Conversation protocol parameters
    public static final int C_MESSAGE_BYTES = 160; // length in bytes of a conversation message
    public static final int DEAD_DROP_BYTES = 32; // sha256 has 256 bits = 32 bytes
    public static final int DEAD_DROP_UINTS = DEAD_DROP_BYTES / 8; // sha256 has 256 bits = 32 bytes
    public static final int C_CIPHERTEXT_BYTES = C_MESSAGE_BYTES + 16; // length in bytes of encrypted messages.
    public static final int CONVERSATION_PAYLOAD_BYTES = DEAD_DROP_BYTES + SodiumConstants.NONCE_BYTES + C_CIPHERTEXT_BYTES;
    public static final int CONVERSATION_PAYLOAD_LENGTH = CONVERSATION_PAYLOAD_BYTES / 8;






    // Tags and Extra names.
    public static final String BUDDY_EXTRA = "ac.panoramix.uoe.mcmix.BUDDY_EXTRA";

    public static final String BUDDY_ADDED_BROADCAST_TAG = "ac.panoramix.uoe.mcmix.BUDDY_ADDED";
    public static final String MESSAGES_UPDATED_BROADCAST_TAG = "ac.panoramix.uoe.mcmix.MESSAGES_UPDATED";
    public static final String INCOMING_DIAL_RECEIVED_BROADCAST_TAG = "ac.panoramix.uoe.mcmix.INCOMING_DIAL_RECEIVED";


}
