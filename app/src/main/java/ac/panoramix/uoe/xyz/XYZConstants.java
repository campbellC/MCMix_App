package ac.panoramix.uoe.xyz;

/**
 * Created by: Chris Campbell
 * on: 02/06/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

public final class XYZConstants {
    public static final int MESSAGE_LENGTH = 140; // length in bytes of a conversation message
    public static final int DEAD_DROP_LENGTH = 32; // sha256 has 256 bits = 32 bytes

    public static final int MAX_MESSAGES_IN_QUEUE = 50;
}
