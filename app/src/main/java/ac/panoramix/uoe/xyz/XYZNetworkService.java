package ac.panoramix.uoe.xyz;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class XYZNetworkService extends Service {
    public XYZNetworkService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID){
        //TODO: set up network connection with server
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
