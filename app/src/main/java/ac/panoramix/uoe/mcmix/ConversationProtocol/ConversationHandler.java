package ac.panoramix.uoe.mcmix.ConversationProtocol;



import android.content.Intent;
import android.util.Log;

import com.google.common.base.Splitter;

import org.libsodium.jni.crypto.Random;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;

import ac.panoramix.uoe.mcmix.Accounts.Buddy;
import ac.panoramix.uoe.mcmix.Database.ConversationBase;
import ac.panoramix.uoe.mcmix.Utility;
import ac.panoramix.uoe.mcmix.MCMixApplication;
import ac.panoramix.uoe.mcmix.MCMixConstants;

/**
 * Created by: Chris Campbell
 * on: 02/06/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 *
 * This class is the outer layer of conversation handling. It's main responsibility is to act as a
 * mediator between the network handling layer that deals with the specific SHAREMIND sharing/TLS
 * and the UI thread that is adding messages to a queue and starting and stopping conversations.
 *
 * This class is a singleton that is the sole responsibility for manipulating the conversation histories
 * and outgoing conversation queue. All requests for conversation message handling must go through an
 * instance of this class.
 */


public class ConversationHandler {
    private static ConversationHandler sConversationHandler;

    private Buddy bob;
    private LinkedList<UUID> outgoingMessages;
    private ConversationPayloadMaker mConverter;
    private ConversationBase mBase = ConversationBase.getOrCreateInstance(MCMixApplication.getContext());

    /**
     * The following two fields buddyLastMessageWasSentTo and lastMessageUUID allow delivery receipts.
     * The networking thread confirms delivery and the conversation handler only adds a message
     * to the history if still in conversation with the same person *and* the last message sent
     * was not a null message.
     */
    Buddy buddyLastMessageWasSentTo;
    UUID lastMessageUUID;

    private ConversationHandler(){
        outgoingMessages = new LinkedList<>();
        mConverter = null;
    }

    public static ConversationHandler getOrCreateInstance(){
        if(sConversationHandler == null){
            sConversationHandler = new ConversationHandler();
        }
        return sConversationHandler;
    }

    /***
     * messagesToSend lets a user check if closing this conversation
     * will lose messages and so decide to wait before closing the conversation.
     * @return
     */
    synchronized public boolean outgoingQueueIsEmpty(){
        return outgoingMessages.isEmpty();
    }

    /***
     * End the current conversation, deleting any messages waiting to be
     * sent at this time.
     */
    synchronized public void endConversation(){
        if(bob != null) {
            Log.d("ConvHandler", "Ending conversation with " + bob.getUsername());
        } else {
            Log.d("ConvHandler", "Ending conversation with null bob ");
        }
        bob = null;
        outgoingMessages.clear();
        mConverter = null;
    }

    /***
     * Start a new conversation with bob. All outgoing messages will
     * be tagged with the relevant dead drop.
     * @param new_bob
     */
   synchronized public void startConversation(Buddy new_bob){
        if(new_bob == null){
            return;
        } else if (new_bob.equals( bob)) {
            Log.d("ConvHandler","Tried Starting conversation with " + new_bob.getUsername() + " but already in one");
            return;
        } else if(bob != null) {
            endConversation();
        }
        Log.d("ConvHandler","Starting conversation with " + new_bob.getUsername());
        bob = new_bob;
        outgoingMessages.clear();
        mConverter = new ConversationPayloadMaker(MCMixApplication.getAccount(), bob);
    }
    synchronized public boolean inConversation(){
        return (bob != null);
    }

    synchronized public void handleMessageFromServer(String incoming_payload){
        //Log.d("ConvHandler", "handling message from server: " + incoming_payload);
        if(inConversation() && buddyLastMessageWasSentTo != null && buddyLastMessageWasSentTo.equals(bob)){
            // if not in conversation then this message is random noise we sent out so drop it, otherwise
            // add to the conversation history for bob
            ConversationMessage msg = mConverter.encryptedPayloadToMessage(incoming_payload);
            Log.d("ConvHandler", "Message contents" + msg.toString());
            if(!msg.isEmpty()) {
                addMessageToHistory(msg);
            }
        }
    }

    synchronized private void addMessageToHistory(ConversationMessage message){
        Log.d("ConvHandler", "Adding message to history: " + message.toString());
        mBase.addMessage(message, bob);
        broadcastMessagesUpdated();
    }

    synchronized private void broadcastMessagesUpdated(){
        Intent intent = new Intent();
        intent.setAction(MCMixConstants.MESSAGES_UPDATED_BROADCAST_TAG);
        MCMixApplication.getContext().sendBroadcast(intent);
    }
    synchronized public void handleMessageFromUser(String payload){
        // This one liner splits the payload into strings of the correct length for sending over the wire
        for(String s : Splitter.fixedLength(MCMixConstants.C_MESSAGE_BYTES).split(payload) ){
            ConversationMessage msg = new ConversationMessage(s, true);
            addMessageToHistory(msg);
            outgoingMessages.add(msg.getUuid());
            broadcastMessagesUpdated();
        }
    }

    synchronized public void handleDeleteRequestFromUser(ConversationMessage msg){
        UUID uuid = msg.getUuid();
        // Firstly we check if this msg is waiting to be sent, in which case we delete this
        for(Iterator<UUID> it = outgoingMessages.iterator(); it.hasNext();){
            if(uuid.equals(it.next())){
                it.remove();
                break;
            }
        }
        //Then we ask the database to delete the message as well
        mBase.deleteMessage(uuid);
        // Finally we let any viewing activities know that a change has been made
        broadcastMessagesUpdated();
    }
    /***
     *
     * @param round_number
     * @return
     */
    synchronized public String getNextMessageForServer(long round_number){
        String outgoing_payload;
        if(!inConversation()){
            // In this case we send random noise out to the entry server
            Log.d("ConvHandler", "Sending random noise to server");
            outgoing_payload = generateRandomMessage();
            buddyLastMessageWasSentTo = null;
            lastMessageUUID = null;
        } else {
            // In conversation we must check if are any messages waiting to be sent. Otherwise,
            // create an empty message and send that.
            buddyLastMessageWasSentTo = bob;
            if (outgoingMessages.isEmpty()) {
                outgoing_payload = mConverter.constructNullMessagePayload(round_number);
                lastMessageUUID = null;
                Log.d("ConvHandler", "Sending null message");
            } else {
                UUID uuid_of_next_message = outgoingMessages.peek();
                lastMessageUUID = uuid_of_next_message;
                Log.d("ConvHandler", "UUID of message to send: " + uuid_of_next_message.toString());
                ConversationMessage msg_to_send = mBase.getMessage(uuid_of_next_message);
                Log.d("ConvHandler", "Sending Message: " + msg_to_send.toString());
                outgoing_payload = mConverter.constructOutgoingPayload(msg_to_send, round_number);
            }
        }
        //Log.d("ConvHandler", "Actual payload: " + outgoing_payload);
        return outgoing_payload;
    }

    synchronized public void confirmMessageSent(){
        if(inConversation() && buddyLastMessageWasSentTo!= null && buddyLastMessageWasSentTo.equals(bob) && lastMessageUUID != null) {
            Log.d("ConvHandler", "Message confirmed sent: " + lastMessageUUID.toString());
            mBase.setMessageSent(lastMessageUUID, bob);
            outgoingMessages.poll();
            lastMessageUUID = null;
            broadcastMessagesUpdated();
        }
    }

    private String generateRandomMessage(){
        byte[] random_bytes = new Random().randomBytes(MCMixConstants.CONVERSATION_PAYLOAD_BYTES);
        return Utility.uint_string_from_bytes(random_bytes);
    }

    public void log_status(){
        Log.d("ConvHandler", "in conversation: " + Boolean.toString(inConversation()));
        Log.d("ConvHandler", "last message sent " + lastMessageUUID);
        Log.d("ConvHandler", "last buddy sent to " + buddyLastMessageWasSentTo);

    }


    public boolean inConversationWith(Buddy b){
        return bob != null && b != null && b.equals(bob);
    }

}
