package ac.panoramix.uoe.mcmix.Networking;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import ac.panoramix.uoe.mcmix.MCMixApplication;

/**
 * Created by: Chris Campbell
 * on: 11/07/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

public class UpdatePublicKeyTask extends AsyncTask<String, Integer, String> {
    ServerHandler mServerHandler;
    String response;
    public UpdatePublicKeyTask() {
        super();
        mServerHandler = ServerHandler.getOrCreateInstance();
    }

    @Override
    protected String doInBackground(String... params) {
        response = mServerHandler.update_key(MCMixApplication.getAccount().getKeyPair().getPublicKey());
        return "Finished creating user attempt.";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (response == null) {
            Toast.makeText(MCMixApplication.getContext(), "Connection down", Toast.LENGTH_LONG).show();
        } else {
            switch (response){
                case ServerHandler.GOOD_STATUS:
                    Log.d("UpdateKey", "Update users public key");
                    break;
                case ServerHandler.PUBLIC_KEY_DOES_NOT_CONFORM:
                    Log.d("UpdateKey", "Users key does not conform to standards");
                    break;
                default:
                    Log.d("UpdateKey", "Issue updating users key: " + response);

            }
        }
    }
}
