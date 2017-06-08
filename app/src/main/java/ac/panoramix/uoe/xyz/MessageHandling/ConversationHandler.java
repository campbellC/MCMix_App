package ac.panoramix.uoe.xyz.MessageHandling;



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
    ConversationMessagePayloadConverter mConverter;

    public ConversationHandler(Account a){
        bob = null;
        alice = a;
        mConversationQueue = ConversationQueue.getOrCreateConversationQueue();
        mConverter = null;
    }

    synchronized public void endConversation(){
        bob = null;
        mConversationQueue.clear();
        mConverter = null;
    }

   synchronized public void startConversation(Buddy bob){
        this.bob = bob;
        mConversationQueue.clear();
        mConverter = new ConversationMessagePayloadConverter(alice, bob);
    }
    synchronized public boolean inConversation(){
        return (bob != null);
    }
    synchronized public void incomingConversationMessage(byte[] incoming_payload){
        if(inConversation()){
            // if not in conversation then this message is random noise we sent out so drop it, otherwise
            // add to the conversation history for bob
            ConversationMessage msg = mConverter.payload_to_message(incoming_payload);
            //TODO: add msg to conversation history for this conversation.
        }
    }

    synchronized public byte[] incomingRoundEndMessage(byte[] incoming_payload){
        byte[] outgoing_payload;
        if(!inConversation()){
            // In this case we send random noise out to the entry server
            outgoing_payload = generateRandomMessage();
        } else {
            // In conversation we must check if are any messages waiting to be sent. Otherwise,
            // create an empty message and send that.
            long round_number = Utility.bytesToLongWithOffset(
                            incoming_payload,
                            XYZConstants.INCOMING_CONVERSATION_TAG_OFFSET);
            if(mConversationQueue.isEmpty()) {
                outgoing_payload = mConverter.construct_null_message_payload(round_number);
            } else {
                outgoing_payload = mConverter.construct_outgoing_payload(mConversationQueue.poll(), round_number);
                //TODO: add message to conversation history for this conversation or is this handled by UI thread?
            }
        }
        return outgoing_payload;
    }
    private byte[] generateRandomMessage(){
        byte[] random_payload = new Random().randomBytes(XYZConstants.INCOMING_CONVERSATION_PAYLOAD_LENGTH);
        return random_payload;
    }



}
