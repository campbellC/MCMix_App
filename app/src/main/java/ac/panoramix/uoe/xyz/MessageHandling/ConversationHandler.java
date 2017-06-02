package ac.panoramix.uoe.xyz.MessageHandling;

import android.util.Log;

import org.libsodium.jni.crypto.Random;

import ac.panoramix.uoe.xyz.Accounts.Account;
import ac.panoramix.uoe.xyz.Accounts.Buddy;
import ac.panoramix.uoe.xyz.Utility;
import ac.panoramix.uoe.xyz.XYZConstants;

/**
 * Created by: Chris Campbell
 * on: 02/06/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 *
 * This class is the outer layer of conversation handling. It's main responsibility is to act as a
 * mediator between the network handling layer that deals with the specific SHAREMIND sharing/TLS
 * and the UI thread that is adding messages to a queue and starting and stopping conversations.
 */


public class ConversationHandler {
    Buddy bob;
    Account alice;
    ConversationQueue mConversationQueue;

    public ConversationHandler(Account a){
        bob = null;
        alice = a;
        mConversationQueue = ConversationQueue.getOrCreateConversationQueue();
    }

    synchronized public void endConversation(){
        bob = null;
        mConversationQueue.clear();
    }

   synchronized public void startConversation(Buddy bob){
        this.bob = bob;
        mConversationQueue.clear();
    }

    synchronized public void incomingConversationMessage(byte[] incoming_payload){
        if(bob != null){
            // if bob is null then this message is random noise we sent out so drop it, otherwise
            // add to the conversation history for bob
            byte[] incoming_message = new byte[XYZConstants.MESSAGE_LENGTH];

            System.arraycopy(incoming_payload,
                    XYZConstants.INCOMING_CONVERSATION_TAG_OFFSET + XYZConstants.DEAD_DROP_LENGTH,
                    incoming_message, 0, XYZConstants.MESSAGE_LENGTH);

            ConversationMessage msg = new ConversationMessage(incoming_message.toString());
            //TODO: add msg to conversation history for this conversation.
        }
    }

    synchronized public byte[] incomingRoundEndMessage(byte[] incoming_payload){
        byte[] outgoing_payload;
        if(bob == null){
            //if bob is null then we are not in a conversation. In this case we send random noise
            // out to the entry server
            outgoing_payload = generateRandomMessage();
        } else {
            //if bob is not null then we are in a conversation. Therefore we check whether there
            // are any messages waiting to be sent. If so, add the dead_drop and send. Otherwise,
            // create an empty message and send that.
            long round_number = Utility.bytesToLongWithOffset(
                            incoming_payload,
                            XYZConstants.INCOMING_CONVERSATION_TAG_OFFSET);
            byte [] dead_drop = Diffie_Hellman.dead_drop(alice, bob, round_number);
            if(mConversationQueue.isEmpty()) {
                outgoing_payload = generateNullMessage(dead_drop);
            } else {
                outgoing_payload = construct_payload(mConversationQueue.poll(), dead_drop);
            }
            encrypt_payload(outgoing_payload);
        }
        return outgoing_payload;
    }
    private byte[] generateRandomMessage(){
        byte[] random_payload = new Random().randomBytes(XYZConstants.MESSAGE_LENGTH + XYZConstants.DEAD_DROP_LENGTH);
        return random_payload;
    }

    private byte[] generateNullMessage(byte[] dead_drop){
        byte[] payload = new byte[XYZConstants.MESSAGE_LENGTH + XYZConstants.DEAD_DROP_LENGTH];
        System.arraycopy(dead_drop, 0, payload, 0, XYZConstants.DEAD_DROP_LENGTH);
        return payload;
    }

    private byte[] construct_payload(ConversationMessage msg, byte[] dead_drop) {
        //when we have a dead drop and a message, construct into an outgoing payload for the protocol
        byte[] payload = new byte[XYZConstants.MESSAGE_LENGTH + XYZConstants.DEAD_DROP_LENGTH];
        System.arraycopy(dead_drop, 0, payload, 0, XYZConstants.DEAD_DROP_LENGTH);
        System.arraycopy(msg.getBytes(), 0,
                payload, XYZConstants.DEAD_DROP_LENGTH,
                XYZConstants.MESSAGE_LENGTH + XYZConstants.DEAD_DROP_LENGTH);
        return payload;
    }

    private byte[] encrypt_payload(byte[] payload){
        //TODO: make this encrypt. Note need to worry about length of message changing during encryption
        //TODO: therefore may require more than one MESSAGE_LENGTH and use in different situations
        return payload;
    }
}
