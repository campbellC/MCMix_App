package ac.panoramix.uoe.xyz;

import org.libsodium.jni.SodiumConstants;

/**
 * Created by: Chris Campbell
 * on: 02/06/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

public final class XYZConstants {
    // Constants for Conversation Protocol
    public static final int INCOMING_CONVERSATION_TAG_OFFSET = 1; // The number of bytes set aside for tagging incoming messages in the conversation protocol
    public static final byte CONVERSATION_ROUND_END_TAG = 1;
    public static final byte CONVERSATION_MESSAGE_TAG = 0;

    public static final int MESSAGE_LENGTH = 160; // length in bytes of a conversation message
    public static final int DEAD_DROP_LENGTH = 32; // sha256 has 256 bits = 32 bytes
    public static final int CIPHERTEXT_LENGTH = MESSAGE_LENGTH + 16; // length in bytes of encrypted messages. //TODO: find proof that this is correct fomula
    public static final int OUTGOING_CONVERSATION_PAYLOAD_LENGTH = DEAD_DROP_LENGTH + SodiumConstants.NONCE_BYTES + CIPHERTEXT_LENGTH;
    public static final int INCOMING_CONVERSATION_PAYLOAD_LENGTH = OUTGOING_CONVERSATION_PAYLOAD_LENGTH + INCOMING_CONVERSATION_TAG_OFFSET;

    public static final int MAX_MESSAGES_IN_QUEUE = 50;

}
