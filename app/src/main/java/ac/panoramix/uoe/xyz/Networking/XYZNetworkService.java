package ac.panoramix.uoe.xyz.Networking;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import ac.panoramix.uoe.xyz.Accounts.Account;
import ac.panoramix.uoe.xyz.Accounts.Buddy;
import ac.panoramix.uoe.xyz.MessageHandling.ConversationHandler;
import ac.panoramix.uoe.xyz.XYZApplication;
import ac.panoramix.uoe.xyz.XYZConstants;


public class XYZNetworkService extends Service {

    private static Timer sTimer = new Timer();


    @Override
    public void onCreate() {
        super.onCreate();
        sTimer.scheduleAtFixedRate(new NetworkingTasks(),0,20000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID){


        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy()
    {
        ConversationHandler.getOrCreateInstance().endConversation();
        super.onDestroy();
    }



    private class NetworkingTasks extends TimerTask {
        private ServerHandler mServerHandler;
        private ConversationHandler mConversationHandler;

        public NetworkingTasks(){
            super();
            mConversationHandler = ConversationHandler.getOrCreateInstance();
            mServerHandler = ServerHandler.getOrCreateInstance();
        }



        @Override
        public void run() {
            Log.d("NetworkService", "Timed interaction beginning with server.");
            /**
             * Conversation portion of networking code. We check the server for a change of round number
             * if there is one we get incoming message and submit the next message.
             */
            if(mServerHandler.c_round_finished()){
                String incoming = mServerHandler.c_recv_message();
                if(incoming != null) {
                    mConversationHandler.handleMessageFromServer(incoming);
                }
                String outgoing = mConversationHandler.getNextMessageForServer(mServerHandler.getC_round_number());
                boolean sent_message = mServerHandler.c_send_message(outgoing);
                if (sent_message){
                    mConversationHandler.confirmMessageSent();
                }
                Intent intent = new Intent();
                intent.setAction(XYZConstants.MESSAGE_ADDED_BROADCAST_TAG);
                sendBroadcast(intent);
            }


        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
