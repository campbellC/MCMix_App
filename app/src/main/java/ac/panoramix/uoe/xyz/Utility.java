package ac.panoramix.uoe.xyz;

import android.util.Log;

import com.google.common.primitives.UnsignedInts;
import com.google.common.primitives.UnsignedLongs;

import java.util.Arrays;
import java.util.regex.Pattern;

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


    public static String string_from_bytes(byte[] payload){
        assert(payload.length % 8 == 0);
        String ret_str = "";
        for(int offset = 0; offset < payload.length; offset += 8){
           long next = bytesToLongWithOffset(payload, offset);
           ret_str += " " +  UnsignedLongs.toString(next);
        }
        return ret_str.trim();
    }

    public static byte[] bytes_from_string(String long_str){
        assert( Pattern.compile("([0-9]|\\s)+").matcher(long_str).matches());
        String trimmed = long_str.trim();
        String[] nums_strs = trimmed.split("\\s+");
        byte[] nums_bytes = new byte[nums_strs.length * 8];
        for(int i = 0; i < nums_strs.length; i ++ ){
            long nextLong = UnsignedLongs.parseUnsignedLong(nums_strs[i]);
            System.arraycopy(longToBytes(nextLong), 0, nums_bytes, i * 8, 8);
        }
        return nums_bytes;
    }
}
