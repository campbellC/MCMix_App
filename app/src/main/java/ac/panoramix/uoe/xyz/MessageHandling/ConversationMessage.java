package ac.panoramix.uoe.xyz.MessageHandling;

import android.util.Log;

import com.google.common.base.CharMatcher;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import ac.panoramix.uoe.xyz.XYZConstants;

/**
 * Created by: Chris Campbell
 * on: 02/06/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

/** This class encapsulates a single message for the protocol.
 * The upper limit for the length of a message is XYZConstants.MESSAGE_LENGTH.
 */

public class ConversationMessage {


    private String message;
    final public static Charset sEncoding = StandardCharsets.US_ASCII;
    private boolean from_alice;



    /* Constructors */
    public ConversationMessage(String message, boolean from_alice) {
        assert message.length() < XYZConstants.MESSAGE_LENGTH;
        assert CharMatcher.ascii().matchesAllOf(message);
        this.message = message;
        this.from_alice = from_alice;
    }
    public ConversationMessage(byte[] message, boolean from_alice) {
        // need to handle 0-padded arrays.
        if(message.length == 0 || message[0] ==  0){
            this.message = "";
        } else {
            // Firstly we find the last index of a non-zero byte
            int i = message.length-1;
            while (i >= 0 && message[i] ==  0) {
                --i;
            }
            // now we construct a string up to that last index. i+1 since this is the length not the index
            this.message = new String(message, 0, i+1, sEncoding);
        }
        this.from_alice = from_alice;
    }

    /* Utility Methods */
    public byte[] getBytes(){
        byte[] initial_message = message.getBytes(sEncoding);
        if(initial_message.length == 160){
            return initial_message;
        } else {
            byte[] padded_message = new byte[160];
            System.arraycopy(initial_message, 0, padded_message, 0, initial_message.length);
            return padded_message;
        }


    }

    public boolean isFrom_alice() {
        return from_alice;
    }
    public String getMessage() {
        return message;
    }

    public int length() {
        return message.length();
    }

    @Override
    public String toString(){
        return message;
    }

    /* Object methods */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ConversationMessage))
            return false;
        if (obj == this)
            return true;

        ConversationMessage rhs = (ConversationMessage) obj;
        return rhs.message.equals(message);
    }


    @Override
    public int hashCode() {
        return message.hashCode();
    }
}
