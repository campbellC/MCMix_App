package ac.panoramix.uoe.xyz.MessageHandling;



import android.content.Context;
import android.util.Log;

import org.libsodium.jni.crypto.Random;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import ac.panoramix.uoe.xyz.Accounts.Account;
import ac.panoramix.uoe.xyz.Accounts.Buddy;
import ac.panoramix.uoe.xyz.Utility;
import ac.panoramix.uoe.xyz.XYZApplication;
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
 *
 * This class is a singleton that is the sole responsibility for manipulating the conversation histories
 * and outgoing conversation queue. All requests for conversation message handling must go through an
 * instance of this class.
 */


public class ConversationHandler {
    private static ConversationHandler sConversationHandler;

    Buddy bob;
    ConversationQueue mConversationQueue;
    ConversationMessagePayloadConverter mConverter;
    ConversationHistory mCurrentConversationHistory;

    /**
     * The following two fields buddyLastMessageWasSentTo and lastMessage allow delivery reciepts.
     * The networking thread confirms delivery and the conversation handler only adds a message
     * to the history if still in conversation with the same person *and* the last message sent
     * was not a null message.
     */
    Buddy buddyLastMessageWasSentTo;
    ConversationMessage lastMessage;

    private ConversationHandler(){
        mConversationQueue = new ConversationQueue();
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
        return mConversationQueue.isEmpty();
    }

    /***
     * End the current conversation, deleting any messages waiting to be
     * sent at this time.
     */
    synchronized public void endConversation(){
        saveConversationToDisk();
        bob = null;
        mConversationQueue.clear();
        mConverter = null;
    }

    /***
     * Start a new conversation with bob. All outgoing messages will
     * be tagged with the relevant dead drop.
     * @param bob
     */
   synchronized public void startConversation(Buddy bob){
        if(bob != null) {
            this.bob = bob;
            mConversationQueue.clear();
            mConverter = new ConversationMessagePayloadConverter(XYZApplication.getAccount(), bob);
            retrieveConversationFromDisk();
        }
    }
    synchronized public boolean inConversation(){
        return (bob != null);
    }

    synchronized public void handleMessageFromServer(String incoming_payload){
        Log.d("ConvHandler", "handling message from server: " + incoming_payload);
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
        mCurrentConversationHistory.add(message);
    }

    synchronized public void handleMessageFromUser(String payload){
        // This one liner splits the payload into strings of the correct length for sending over the wire
        for(String s : payload.split("(?<=\\G.{"+ Integer.toString(XYZConstants.C_MESSAGE_BYTES) + "})")){
            mConversationQueue.add(new ConversationMessage(s, true));
        }
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
            lastMessage = null;
        } else {
            // In conversation we must check if are any messages waiting to be sent. Otherwise,
            // create an empty message and send that.
            if(buddyLastMessageWasSentTo != null && buddyLastMessageWasSentTo.equals(bob) && lastMessage != null && !lastMessage.wasSent()){
                Log.d("ConvHandler", "Sending Message: " + lastMessage.toString());
                outgoing_payload = mConverter.constructOutgoingPayload(lastMessage, round_number) ;
            } else {
                buddyLastMessageWasSentTo = bob;
                if (mConversationQueue.isEmpty()) {
                    lastMessage = null;
                    outgoing_payload = mConverter.constructNullMessagePayload(round_number);
                    Log.d("ConvHandler", "Sending null message");
                } else {
                    ConversationMessage msg_to_send = mConversationQueue.peek();
                    lastMessage = msg_to_send;
                    Log.d("ConvHandler", "Sending Message: " + msg_to_send.toString());
                    outgoing_payload = mConverter.constructOutgoingPayload(msg_to_send, round_number);
                }
            }
        }
        Log.d("ConvHandler", "Actual payload: " + outgoing_payload);
        return outgoing_payload;
    }

    public ConversationHistory getCurrentConversationHistory() {
        return mCurrentConversationHistory;
    }

    synchronized  public void confirmMessageSent(){

        if(inConversation() && buddyLastMessageWasSentTo!= null && buddyLastMessageWasSentTo.equals(bob) && lastMessage != null) {
            lastMessage.setSent(true);
            addMessageToHistory(lastMessage);
            mConversationQueue.poll();
            lastMessage = null;
        }
    }

    private String generateRandomMessage(){
        byte[] random_bytes = new Random().randomBytes(XYZConstants.CONVERSATION_PAYLOAD_BYTES);
        return Utility.string_from_bytes(random_bytes);
    }

    public void log_status(){
        Log.d("ConvHandler", "in conversation: " + Boolean.toString(inConversation()));
        Log.d("ConvHandler", "last message sent " + lastMessage);
        Log.d("ConvHandler", "last buddy sent to " + buddyLastMessageWasSentTo);

    }

    /**
     * This message takes the  CurrentConversationHistory and saves it to disk for later retrieval.
     */
    private void saveConversationToDisk(){
        String history_filename = Utility.filename_for_conversation(XYZApplication.getAccount(),bob);
        if(bob == null || mCurrentConversationHistory == null) {
            Log.d("ConvHandler", "Tried to save null conversation to disk");
            return;
        }
        try {
            FileOutputStream fos = XYZApplication.getContext().openFileOutput(history_filename, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(mCurrentConversationHistory);
            oos.close();
            fos.close();
        } catch (IOException ioe) {
            Log.d("ConvHandler", "Issues writing ConvHistory to disk", ioe);
        }
    }

    /**
     * This message is the inverse of the saveConversationToDisk function.
     *
     */
    private void retrieveConversationFromDisk() {
        if(bob == null){
            Log.d("ConvHandler", "Tried to retrieve null conversation from disk");
            return;
        }
        String history_filename = Utility.filename_for_conversation(XYZApplication.getAccount(),bob);
        File f = XYZApplication.getContext().getFileStreamPath(history_filename);

        if(f.exists()){
            // if this file exists then we have previously saved a conversation history for this user.
            try (
                    FileInputStream fis = XYZApplication.getContext().openFileInput(history_filename);
                    ObjectInputStream ois = new ObjectInputStream(fis);
            ) {
                mCurrentConversationHistory = (ConversationHistory) ois.readObject();
                return;
            } catch (FileNotFoundException e){
                Log.d("ConvHandler", "Could not find ConversationHistory file", e);
            } catch (IOException ioe) {
                Log.d("ConvHandler", "Error reading ConvHistory file" , ioe);
            } catch (ClassNotFoundException cnfe){
                Log.d("ConvHandler", "Corrupted Conversation History file", cnfe);
            }
            // In the case we reach this point the conversation history is lost and we need to create a new one.
            mCurrentConversationHistory = new ConversationHistory(bob);

        } else {
            // If no such file exists then we can safely create a new ConversationHistory without losing anything
            mCurrentConversationHistory = new ConversationHistory(bob);
        }


    }

}
