package ac.panoramix.uoe.mcmix;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.common.primitives.UnsignedLongs;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by: Chris Campbell
 * on: 02/06/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

/*
This class provides utility functions used in other classes.
 */
public class Utility {


    /* METHODS FOR BYTE MANIPULATION */

    /** Takes next 8 bytes and returns corresponding long
     * requires array to be at least 8 bytes past offset
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




    /* METHODS FOR UINT STRING MANIPULATION */



    /* This function takes a string of characters and encodes it
    as 64 bit UINTS that the server can parse
     */
    public static String UInt_String_From_String(String username){

        if(username.length() == 0){
            return "";
        } else if(username.length() <= 8){
            byte[] as_bytes = username.getBytes(StandardCharsets.US_ASCII);
            if(as_bytes.length == 8){
                return uint_string_from_bytes(as_bytes);
            } else {
                byte[] padded_bytes = new byte[8];
                int padding_length = 8 - (as_bytes.length % 8);
                System.arraycopy(as_bytes, 0, padded_bytes, padding_length, as_bytes.length);
                return uint_string_from_bytes(padded_bytes);
            }

        } else {
            String answer = "";
            String[] split_up = username.split("(?<=\\G.{8})");
            for(String piece : split_up){
                answer += UInt_String_From_String(piece) + " ";
            }
            return answer;
        }
    }

    private static byte null_byte;
    static{
        try {
            null_byte = "\0".getBytes("US-ASCII")[0];
        } catch (UnsupportedEncodingException e){
            Log.d("Utility", "Null byte not supported in ascii", e);
        }
    }

    /* This is the inverse function to UInt_String_From_String */
    public static String String_From_UInt_String(String uints){
        byte[] as_bytes = bytes_from_uint_string(uints);
        int num_null_bytes = 0;
        for(byte b : as_bytes){
            if(b == null_byte){
                ++num_null_bytes;
            }
        }
        byte[] non_null_bytes = new byte[as_bytes.length - num_null_bytes];
        int j = 0;
        for(int i = 0; i < as_bytes.length; ++i){
            if(as_bytes[i] != null_byte){
                non_null_bytes[j] = as_bytes[i];
                ++j;
            }
        }
        try {
            return new String(non_null_bytes, "US-ASCII");
        } catch (UnsupportedEncodingException e){
            Log.d("Utility", "Bad encoding in uint string", e);
            return "";
        }
    }

    public static String uint_string_from_bytes(byte[] payload){
        assert(payload.length % 8 == 0);
        String ret_str = "";
        for(int offset = 0; offset < payload.length; offset += 8){
            long next = bytesToLongWithOffset(payload, offset);
            ret_str += " " +  UnsignedLongs.toString(next);
        }
        return ret_str.trim();
    }

    public static byte[] bytes_from_uint_string(String long_str){
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


    /* OTHER METHODS */
    /* This message formats the date for the display in conversation activity.
        If it is on this day then it gives the actual time in 24 hour format. Otherwise
        it gives a date instead
     */
    public static String format_date_for_display(Date date){
        String ret;
        if(DateUtils.isToday(date.getTime())){
           ret = new SimpleDateFormat("HH:mm").format(date);
        } else {
            ret = new SimpleDateFormat("MMM dd").format(date);
        }
        return ret;
    }

    /* This method allows other objects to persist the account information to disk */
    public static void saveAccountToDisk(){
        try {
            FileOutputStream fos = MCMixApplication.getContext().openFileOutput(MCMixConstants.ACCOUNT_STORAGE_FILE, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(MCMixApplication.getAccount());
            oos.close();
            fos.close();
        }catch (FileNotFoundException e){
            Log.d("Utility", "Cannot open to save account", e);
        } catch (IOException e) {
            Log.d("UserRegAct", "Cannot save account to disk", e);
        }
    }
}
