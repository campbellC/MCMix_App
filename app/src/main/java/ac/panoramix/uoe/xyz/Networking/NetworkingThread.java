package ac.panoramix.uoe.xyz.Networking;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
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
    HttpURLConnection mConnection;
    public static String SERVER_IP_ADDR = "129.215.25.108";
    public static int PORT = 5013;


    public void setKill_flag(boolean kill_flag) {
        this.kill_flag = kill_flag;
    }

    public NetworkingThread(Context ctx, Account Alice){
        Log.d("NetworkThread", "Creating no-bob network thread");
        context = ctx;
        kill_flag = false;
        mConversationHandler = new ConversationHandler(Alice, context);
        mConversationQueue = ConversationQueue.getOrCreateConversationQueue();
    }

    public NetworkingThread(Context ctx, Account Alice, Buddy bob){
        Log.d("NetworkThread", "Creating network thread with a bob");
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

            ServerHandler serverHandler = new ServerHandler();
            Log.d("networkThread","logging in as testuser1");
            Log.d("networkThread","logged in: " + Boolean.toString(serverHandler.is_logged_in()));
            serverHandler.log_cookies();
            serverHandler.log_in("testuser1","horse_battery_staple");
            serverHandler.log_cookies();
            Log.d("networkThread","logged in: " + Boolean.toString(serverHandler.is_logged_in()));

            String in_message = serverHandler.c_recv_message();
            Log.d("networkThread","received message: " + in_message);


            break;
            //TODO: poll conversation connection
            //TODO: poll dialing connection

            //TODO: remove this debugging code
//            byte[] incoming_payload = new byte[XYZConstants.CONVERSATION_ROUND_END_MESSAGE_LENGTH];
//            incoming_payload[0] = XYZConstants.CONVERSATION_ROUND_END_TAG;
//
//            //TODO: add cases for dialling messages.
//            switch (incoming_payload[0]){
//                case XYZConstants.CONVERSATION_MESSAGE_TAG:
//                    mConversationHandler.incomingConversationMessage(incoming_payload);
//                    broadcast_conversation_message_arrived();
//                    break;
//                case XYZConstants.CONVERSATION_ROUND_END_TAG:
//                    byte[] outgoing_message = mConversationHandler.incomingRoundEndMessage(incoming_payload);
//                    send_message(outgoing_message);
//                    broadcast_conversation_message_arrived();
//                    break;
//                default:
//                    Log.d("networkThread", "Unknown Incoming Message Type");
//            }
        }
    }
    private void broadcast_conversation_message_arrived(){
        Intent intent = new Intent(XYZConstants.MESSAGE_ADDED_BROADCAST_TAG);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
    private void send_message(byte[] outgoing_payload){
        try{
            send_bytes(outgoing_payload);
            Log.d("networkThread", "Sent message on network");
        }  catch (IOException e){
            Log.d("networkThread", "Failed to send message on network", e);
        }
    }


    private void establish_connection(){
        try {
            sock = new Socket(InetAddress.getByName(XYZConstants.SERVER_IP_ADDRESS), XYZConstants.SERVER_PORT);
        } catch (IOException e) {
            Log.d("NetworkThread", "IOException on creating socket", e);
        }
    }

    private void send_bytes(byte[] payload) throws IOException{
        if(sock == null){
            establish_connection();
        }

        if(sock == null){
            throw new IOException("Failed to establish connection.");
        }
        //TODO: handle if no connection is made by throwing an exception?

        OutputStream os = sock.getOutputStream();
        os.write(payload);

    }

    private byte[] recv_bytes() throws IOException {
        if(sock == null){
            establish_connection();
        }
        if(sock == null){
            throw new IOException("Failed to establish connection.");
        }
        //TODO: this should change behaviour based on first byte in stream which determines whether this is a conversation or a dial (or error etc).
        InputStream is = sock.getInputStream();
        byte[] incoming_payload = new byte[XYZConstants.INCOMING_CONVERSATION_PAYLOAD_LENGTH];
        is.read(incoming_payload);
        return incoming_payload;
    }


}
