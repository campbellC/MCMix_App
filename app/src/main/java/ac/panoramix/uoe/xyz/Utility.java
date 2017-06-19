package ac.panoramix.uoe.xyz;

import ac.panoramix.uoe.xyz.Accounts.Account;
import ac.panoramix.uoe.xyz.Accounts.Buddy;

/**
 * Created by: Chris Campbell
 * on: 02/06/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

public class Utility {
    /** Takes next 8 bytes and returns corresponding long
     * requires array to be at least 8 bytes past offset
     *
     **/
    public static long bytesToLongWithOffset(byte[] b, int offset){
        byte[] shorter = new byte[8];
        System.arraycopy(b, offset, shorter, 0, 8);
        return bytesToLong(shorter);
    }
    public static byte[] longToBytes(long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= 8;
        }
        return result;
    }

    public static long bytesToLong(byte[] b) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }


    public static String filename_for_conversation(Account alice, Buddy bob){
        String name = alice.getUsername() + "_" + bob.getUsername() + ".conv";
        return name;
    }
}
