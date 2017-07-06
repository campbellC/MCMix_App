package ac.panoramix.uoe.xyz.MessageHandling;



import android.content.Context;
import android.util.Log;

import org.libsodium.jni.crypto.Random;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
 */


public class ConversationHandler {
    private static ConversationHandler sConversationHandler;

    Buddy bob;
    Account alice;
    ConversationQueue mConversationQueue;
    ConversationMessagePayloadConverter mConverter;

    Buddy buddyLastMessageWasSentTo;

    private ConversationHandler(){
        mConversationQueue = new ConversationQueue();
        mConverter = null;
    }

    public ConversationHandler getOrCreateInstance(){
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
        this.bob = bob;
        mConversationQueue.clear();
        mConverter = new ConversationMessagePayloadConverter(alice, bob);
    }
    synchronized public boolean inConversation(){
        return (bob != null);
    }

    synchronized public void handleMessageFromServer(String incoming_payload){
        if(inConversation() && buddyLastMessageWasSentTo.equals(bob)){
            // if not in conversation then this message is random noise we sent out so drop it, otherwise
            // add to the conversation history for bob
            ConversationMessage msg = mConverter.encryptedPayloadToMessage(incoming_payload);
            addMessageToHistory(msg);
        }
    }

    synchronized private void addMessageToHistory(ConversationMessage message){
        // This method takes a conversation message and saves a file containing the previous history of this
        // conversation. It does this by opening the old save file, appending this message and then resaving that file.
        String history_filename = Utility.filename_for_conversation(alice,bob);
        ConversationHistory history = new ConversationHistory(alice,bob);
        try {
            FileInputStream fis = XYZApplication.getContext().openFileInput(history_filename);
            ObjectInputStream ois = new ObjectInputStream(fis);
            history = (ConversationHistory) ois.readObject();
        } catch (FileNotFoundException e){
            Log.d("ConvHandler", "FileNotFoundException" + e.getStackTrace());
        } catch (IOException ioe) {
            Log.d("ConvHandler", "IOException" + ioe.getStackTrace());
        } catch (ClassNotFoundException cnfe){
            Log.d("ConvHandler", "ClassNotFoundException" + cnfe.getStackTrace());
        }

        history.add(message);
        try {
            FileOutputStream fos = XYZApplication.getContext().openFileOutput(history_filename, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(history);
        } catch (IOException ioe) {
            Log.d("ConvHandler", "IOException" + ioe.getStackTrace());
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
            outgoing_payload = generateRandomMessage();
            buddyLastMessageWasSentTo = null;
        } else {
            // In conversation we must check if are any messages waiting to be sent. Otherwise,
            // create an empty message and send that.
            buddyLastMessageWasSentTo = bob;
            if(mConversationQueue.isEmpty()) {
                outgoing_payload = mConverter.constructNullMessagePayload(round_number);
                Log.d("ConvHandler", "Sending null message");
            } else {
                ConversationMessage msg_to_send = mConversationQueue.peek();
                Log.d("ConvHandler", "Sending Message: " + msg_to_send.toString());
                outgoing_payload = mConverter.constructOutgoingPayload(msg_to_send, round_number);
            }
        }
        return outgoing_payload;
    }

    synchronized  public void confirmMessageSent(){
        //TODO: serious testing needed of this behaviour with stopping conversations and starting with same person etc.
        if(inConversation() && buddyLastMessageWasSentTo.equals(bob)) {
            assert !mConversationQueue.isEmpty();
            ConversationMessage msg = mConversationQueue.poll();
            addMessageToHistory(msg);
        }
    }

    private String generateRandomMessage(){
        byte[] random_bytes = new Random().randomBytes(XYZConstants.CONVERSATION_PAYLOAD_BYTES);
        return Utility.string_from_bytes(random_bytes);
    }



}
