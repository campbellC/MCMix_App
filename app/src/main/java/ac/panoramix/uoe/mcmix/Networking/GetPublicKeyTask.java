package ac.panoramix.uoe.mcmix.Networking;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.libsodium.jni.keys.PublicKey;

import ac.panoramix.uoe.mcmix.Accounts.Buddy;
import ac.panoramix.uoe.mcmix.Database.BuddyBase;
import ac.panoramix.uoe.mcmix.MCMixApplication;
import ac.panoramix.uoe.mcmix.MCMixConstants;
import ac.panoramix.uoe.mcmix.Utility;

/**
 * Created by: Chris Campbell
 * on: 11/07/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

/**
 * This class allows the user to fetch the public key of a buddy from the server
 * knowing only the username.
 */
public class GetPublicKeyTask extends AsyncTask<String,Integer,String> {
    String username;
    ServerHandler mServerHandler;
    Buddy bob;
    public GetPublicKeyTask() {
        super();
        mServerHandler = ServerHandler.getOrCreateInstance();
        bob = null;
    }

    @Override
    protected String doInBackground(String... params) {
        username = params[0];
        PublicKey pk = mServerHandler.get_public_key_for_username(username);
        if(pk != null) {
            bob = new Buddy(username, pk);
        }
        return "Finished creating user attempt.";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (bob == null) {
            Toast.makeText(MCMixApplication.getContext(), "Username does not exists, connection is down or user has no public key.", Toast.LENGTH_LONG).show();
        } else {
            BuddyBase.getOrCreateInstance(MCMixApplication.getContext()).updateBuddy(bob);
            Log.d("GetKey", "Added or updated key for  " + bob.getUsername());
            // Broadcast that a buddy has been added or updated
            Intent intent = new Intent();
            intent.setAction(MCMixConstants.BUDDY_ADDED_BROADCAST_TAG);
            MCMixApplication.getContext().sendBroadcast(intent);
        }
    }

}
