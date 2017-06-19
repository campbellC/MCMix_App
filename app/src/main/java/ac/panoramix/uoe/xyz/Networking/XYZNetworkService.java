package ac.panoramix.uoe.xyz.Networking;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import ac.panoramix.uoe.xyz.Accounts.Account;
import ac.panoramix.uoe.xyz.Accounts.Buddy;


public class XYZNetworkService extends Service {

    private int i;
    public XYZNetworkService() {
        i = 0;
    }
    NetworkingThread thread;

    @Override
    public int onStartCommand(Intent intent, int flags, int startID){
        Account Alice = (Account) intent.getSerializableExtra("Alice");
        //TODO: decide if passing through intent is the best way to handle this.
        Buddy bob = (Buddy) intent.getSerializableExtra("Bob");

        //TODO: this is clearly not a good way of handling this. I have a headache but we should be sending
        //TODO: bob to the thread rather than stopping and starting the thread.
        if(bob == null) {
            Log.d("NetworkService", "Network service started with no bob");
            if (thread == null || thread.getState() == Thread.State.TERMINATED) {
                //If bob is null then we just want to provide cover traffic
                thread = new NetworkingThread(getApplicationContext(), Alice);
                thread.start();
            }
        } else {
            Log.d("NetworkService", "Network service started with bob");
            if(thread != null){
                thread.setKill_flag(true);
                thread = null;
            }
            thread = new NetworkingThread(getApplicationContext(), Alice, bob);
            thread.start();
        }
        //TODO: set up network connection with server
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
        if(thread != null) {
            thread.setKill_flag(true);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
