package ac.panoramix.uoe.xyz.Networking;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.net.Socket;

import ac.panoramix.uoe.xyz.Accounts.Account;
import ac.panoramix.uoe.xyz.Accounts.Buddy;
import ac.panoramix.uoe.xyz.MessageHandling.ConversationHandler;
import ac.panoramix.uoe.xyz.MessageHandling.ConversationQueue;
import ac.panoramix.uoe.xyz.XYZConstants;

/**
 * Created by: Chris Campbell
 * on: 15/06/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

//TODO: this thread should have a socket which it manages which connects to the server.
public class NetworkingThread extends Thread{
    public Context context;
    public volatile boolean kill_flag;
    private ConversationHandler mConversationHandler;
    private ConversationQueue mConversationQueue;
    private Socket sock;



    public void setKill_flag(boolean kill_flag) {
        this.kill_flag = kill_flag;
    }

    public NetworkingThread(Context ctx, Account Alice){
        context = ctx;
        kill_flag = false;
        mConversationHandler = new ConversationHandler(Alice, context);
        mConversationQueue = ConversationQueue.getOrCreateConversationQueue();
    }

    public NetworkingThread(Context ctx, Account Alice, Buddy bob){
        context = ctx;
        kill_flag = false;
        mConversationHandler = new ConversationHandler(Alice, context);
        mConversationHandler.startConversation(bob);
        mConversationQueue = ConversationQueue.getOrCreateConversationQueue();
    }

    public void run(){
        while(!kill_flag) {
            try{
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.d("networkThread","interrupted thread");
            }
            //TODO: poll conversation connection
            //TODO: poll dialing connection

            //TODO: remove this debugging code
            byte[] incoming_payload = new byte[XYZConstants.CONVERSATION_ROUND_END_MESSAGE_LENGTH];
            incoming_payload[0] = XYZConstants.CONVERSATION_ROUND_END_TAG;

            //TODO: add cases for dialling messages.
            switch (incoming_payload[0]){
                case XYZConstants.CONVERSATION_MESSAGE_TAG:
                    mConversationHandler.incomingConversationMessage(incoming_payload);
                    broadcast_conversation_message_arrived();
                    break;
                case XYZConstants.CONVERSATION_ROUND_END_TAG:
                    byte[] outgoing_message = mConversationHandler.incomingRoundEndMessage(incoming_payload);
                    send_message(outgoing_message);
                    broadcast_conversation_message_arrived();
                    break;
                default:
                    Log.d("networkThread", "Unknown Incoming Message Type");
            }
        }
    }
    private void broadcast_conversation_message_arrived(){
        Intent intent = new Intent(XYZConstants.MESSAGE_ADDED_BROADCAST_TAG);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
    private void send_message(byte[] outgoing_payload){
        //TODO: the below is debugging code dealing with encryption to check messages are sent correctly
        Log.d("networkThread", "Sent message");
    }



    private void establish_connection(){}

    private void send_bytes(byte[] payload){}



}
