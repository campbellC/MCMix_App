package ac.panoramix.uoe.mcmix.Networking;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import ac.panoramix.uoe.mcmix.ConversationProtocol.ConversationHandler;
import ac.panoramix.uoe.mcmix.DialingProtocol.DialHandler;
import ac.panoramix.uoe.mcmix.MCMixConstants;


public class MCMixNetworkService extends Service {

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
        private DialHandler mDialHandler;
        public NetworkingTasks(){
            super();
            mConversationHandler = ConversationHandler.getOrCreateInstance();
            mServerHandler = ServerHandler.getOrCreateInstance();
            mDialHandler = DialHandler.getOrCreateInstance();
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
            }

            if(mServerHandler.d_round_finished()){
                String incoming_dial = mServerHandler.d_recv_dial();
                if(incoming_dial != null){
                    mDialHandler.handle_dial_from_server(incoming_dial);
                }
                String outgoing_dial = mDialHandler.get_dial_for_server();
                mServerHandler.d_send_dial(outgoing_dial);
            }


        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
