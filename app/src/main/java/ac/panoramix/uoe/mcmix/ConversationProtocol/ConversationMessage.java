package ac.panoramix.uoe.mcmix.ConversationProtocol;

import com.google.common.base.CharMatcher;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import ac.panoramix.uoe.mcmix.MCMixConstants;

/**
 * Created by: Chris Campbell
 * on: 02/06/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

/** This class encapsulates a single message for the protocol. It is the fundamental
 * unit of the ConversationProtocol package.
 * The upper limit for the length of a message is MCMixConstants.C_MESSAGE_BYTES.
 * We assume throughout this app that ASCII is the encoding used. Therefore this must be
 * protected with asserts as much as possible.
 */

public class ConversationMessage implements Serializable {

    private String message;
    final public static Charset sEncoding = StandardCharsets.US_ASCII;
    private boolean from_alice;
    private boolean sent;
    private Date timestamp;
    private UUID uuid;

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }


    public ConversationMessage(String message, boolean from_alice, boolean sent, Date timestamp, UUID uuid) {
        assert message.length() <= MCMixConstants.C_MESSAGE_BYTES;
        this.message = message;
        this.from_alice = from_alice;
        this.sent = sent;
        this.timestamp = timestamp;
        this.uuid = uuid;
    }

    public boolean wasSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    /* Constructors */
    public ConversationMessage(String message, boolean from_alice) {
        assert message.length() <= MCMixConstants.C_MESSAGE_BYTES;
        assert CharMatcher.ascii().matchesAllOf(message);
        this.message = message;
        this.from_alice = from_alice;
        uuid = UUID.randomUUID();
        timestamp = new Date();
        this.sent = false;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {

        return uuid;
    }

    public ConversationMessage(byte[] message, boolean from_alice) {
        assert message.length <= MCMixConstants.C_MESSAGE_BYTES;
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
        uuid = UUID.randomUUID();
    }

    /* Utility Methods */
    /*
        getBytes returns an array of bytes which is the sEncoding-encoded form. Note that
        throughout this is currently ASCII encoding.
     */
    public byte[] getBytes(){
        byte[] initial_message = message.getBytes(sEncoding);
        if(initial_message.length == MCMixConstants.C_MESSAGE_BYTES){
            return initial_message;
        } else {
            byte[] padded_message = new byte[MCMixConstants.C_MESSAGE_BYTES];
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

    public boolean isEmpty() {return message.equals(""); }

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
