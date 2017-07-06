package ac.panoramix.uoe.xyz.MessageHandling;



import android.content.Context;
import android.util.Log;

import com.google.common.primitives.UnsignedLongs;

import org.libsodium.jni.crypto.Random;

import java.io.File;
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
    Buddy bob;
    Account alice;
    ConversationQueue mConversationQueue;
    ConversationMessagePayloadConverter mConverter;
    Context context;

    public ConversationHandler(Account a, Context context){
        bob = null;
        alice = a;
        mConversationQueue = ConversationQueue.getOrCreateConversationQueue();
        mConverter = null;
        this.context = context;
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

    synchronized public void incomingConversationMessage(String incoming_payload) {
        if(inConversation()){
            // if not in conversation then this message is random noise we sent out so drop it, otherwise
            // add to the conversation history for bob
            ConversationMessage msg = mConverter.tagged_payload_to_message(incoming_payload);
            add_message_to_history(msg);
        }
    }

    synchronized void add_message_to_history(ConversationMessage message){
        // This method takes a conversation message and saves a file containing the previous history of this
        // conversation. It does this by opening the old save file, appending this message and then resaving that file.
        String history_filename = Utility.filename_for_conversation(alice,bob);
        ConversationHistory history = new ConversationHistory(alice,bob);
        try {
            FileInputStream fis = context.openFileInput(history_filename);
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
            FileOutputStream fos = context.openFileOutput(history_filename, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(history);
        } catch (IOException ioe) {
            Log.d("ConvHandler", "IOException" + ioe.getStackTrace());
        }



    }


    synchronized public String incomingRoundEndMessage(String incoming_payload){
        incoming_payload = incoming_payload.trim();
        String outgoing_payload;
        if(!inConversation()){
            // In this case we send random noise out to the entry server
            outgoing_payload = generateRandomMessage();
        } else {
            // In conversation we must check if are any messages waiting to be sent. Otherwise,
            // create an empty message and send that.
            String[] nums = incoming_payload.split("\\s+");
            //TODO: check which Constant is correct here
            long round_number = UnsignedLongs.parseUnsignedLong(nums[XYZConstants.INCOMING_CONVERSATION_TAG_OFFSET]);
            if(mConversationQueue.isEmpty()) {
                outgoing_payload = mConverter.construct_null_message_payload(round_number);
                Log.d("ConvHandler", "Sending null message");
            } else {
                ConversationMessage msg_to_send = mConversationQueue.poll();
                add_message_to_history(msg_to_send);
                Log.d("ConvHandler", "Sending Message: " + msg_to_send.toString());
                outgoing_payload = mConverter.construct_outgoing_payload(msg_to_send, round_number);
            }
        }
        return outgoing_payload;
    }

    private String generateRandomMessage(){
        //TODO: establish what Constant is required here.
        byte[] random_bytes = new Random().randomBytes(XYZConstants.INCOMING_CONVERSATION_PAYLOAD_LENGTH);
        return Utility.string_from_bytes(random_bytes);
    }



}
