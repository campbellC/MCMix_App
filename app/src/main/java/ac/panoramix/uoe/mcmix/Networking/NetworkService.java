package ac.panoramix.uoe.mcmix.Networking;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import ac.panoramix.uoe.mcmix.ConversationProtocol.ConversationHandler;
import ac.panoramix.uoe.mcmix.DialingProtocol.DialHandler;


/*
    This class is a Service subclass. This means that the Android OS is less likely to kill it
    than a normal activity. However, it still runs in the UI thread and so needs to launch separate
    threads for networking. We use a Timer for this.

    The service simply polls the server for a change in round number and when it is
    detected requests an incoming payload and asks the handlers for the next outgoing payloads.
 */
public class NetworkService extends Service {

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
            }

            /**
             * Dialing portion of networking code. We check the server for a change of round number
             * if there is one we get incoming message and submit the next message.
             */
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
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
