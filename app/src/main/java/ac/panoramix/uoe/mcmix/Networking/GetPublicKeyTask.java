package ac.panoramix.uoe.mcmix.Networking;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.libsodium.jni.keys.PublicKey;

import ac.panoramix.uoe.mcmix.Accounts.Account;
import ac.panoramix.uoe.mcmix.Accounts.Buddy;
import ac.panoramix.uoe.mcmix.Utility;
import ac.panoramix.uoe.mcmix.MCMixApplication;

/**
 * Created by: Chris Campbell
 * on: 11/07/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

/**
 * This class allows the user to fetch
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
            Account Alice = MCMixApplication.getAccount();
            for(int i = 0; i < Alice.getBuddies().size(); ++i){
                if (bob.getUsername().equals(Alice.getBuddies().get(i).getUsername())){
                    Alice.getBuddies().set(i, bob);
                    Toast.makeText(MCMixApplication.getContext(), "Updated key for " + bob.getUsername(), Toast.LENGTH_SHORT).show();
                    Utility.saveAccountToDisk();
                    return;
                }
            }
            Toast.makeText(MCMixApplication.getContext(), "Added key for " + bob.getUsername(), Toast.LENGTH_SHORT).show();
            Log.d("GetKey", "Added key for  " + bob.getUsername() +": "+ Utility.string_from_bytes(bob.getPublic_key().toBytes()));
            Alice.getBuddies().add(bob);
            Utility.saveAccountToDisk();
        }
    }

}
