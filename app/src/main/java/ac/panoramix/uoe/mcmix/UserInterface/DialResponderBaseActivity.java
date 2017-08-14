package ac.panoramix.uoe.mcmix.UserInterface;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.libsodium.jni.keys.PublicKey;

import ac.panoramix.uoe.mcmix.Accounts.Buddy;
import ac.panoramix.uoe.mcmix.ConversationProtocol.ConversationHandler;
import ac.panoramix.uoe.mcmix.Database.BuddyBase;
import ac.panoramix.uoe.mcmix.MCMixApplication;
import ac.panoramix.uoe.mcmix.MCMixConstants;
import ac.panoramix.uoe.mcmix.Networking.ServerHandler;
import ac.panoramix.uoe.mcmix.Utility;

/**
 * Created by: Chris Campbell
 * on: 09/08/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

public abstract class DialResponderBaseActivity extends AppCompatActivity {
    private DialReceiver mDialReceiver;

    @Override
    protected void onStart() {
        super.onStart();
        mDialReceiver = new DialReceiver();
        IntentFilter intentFilter = new IntentFilter(MCMixConstants.INCOMING_DIAL_RECEIVED_BROADCAST_TAG);
        getApplicationContext().registerReceiver(mDialReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        if(mDialReceiver != null){
            getApplicationContext().unregisterReceiver(mDialReceiver);
        }
        super.onStop();
    }

    protected class DialReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            /* On receiving an incoming dial the behaviour depends on
                whether the user knows this person. If this is an already known
                buddy then we simply provide them the option to finish whatever conversation
                is currently happening and start the new conversation or ignore the dial.

                If they don't know the person then we should provide them the opportunity to
                add them as a buddy before starting the conversation.
             */
            /* Firstly we build an alert dialog for handling the launching of a conversation
                with a known buddy.
            */
            String username = intent.getStringExtra(MCMixConstants.BUDDY_EXTRA);
            Buddy b = BuddyBase.getOrCreateInstance(context).getBuddy(username);

            if(b != null){
                /* In this case we know the person and so offer to start a conversation with them if
                 we are not already in conversation with them.
                 */
                if(!ConversationHandler.getOrCreateInstance().inConversationWith(b)) {
                    AlertDialog.Builder alert = knownBuddyDialog(b);
                    alert.show();
                }
            } else {
                // Otherwise this is an unknown person and we must first offer to add them as a contact
                AlertDialog.Builder alert = unknownBuddyDialog(username);
                alert.show();
            }
        }

        protected AlertDialog.Builder knownBuddyDialog(final Buddy bob){
            AlertDialog.Builder alert = new AlertDialog.Builder(DialResponderBaseActivity.this);
            alert.setTitle("Incoming Dial From Buddy");
            alert.setMessage(bob.getUsername() + " has dialed you. Would you like " +
                    "to end any current conversation and start a new one with them?");


            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    ConversationHandler.getOrCreateInstance().startConversation(bob);
                    launchConversation(bob);
                }
            });

            alert.setNegativeButton("Ignore Dial", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.cancel();
                }
            });

            return alert;
        }
        protected AlertDialog.Builder unknownBuddyDialog(final String name){
            AlertDialog.Builder alert = new AlertDialog.Builder(DialResponderBaseActivity.this);
            alert.setTitle("Incoming Dial From Unknown Person");
            alert.setMessage(name + " has dialed you. Would you like to add them as a buddy, " +
                            "end any current conversation and start a new one with them?");


            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    new StartConversationWithNewBuddyTask().execute(name);
                }
            });

            alert.setNegativeButton("Ignore Dial", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.cancel();
                }
            });

            return alert;
        }


    }
    protected void launchConversation(Buddy bob){
        Intent intent = new Intent(DialResponderBaseActivity.this, ConversationActivity.class);
        intent.putExtra(MCMixConstants.BUDDY_EXTRA, bob);
        startActivity(intent);
    }

    public class StartConversationWithNewBuddyTask extends AsyncTask<String,Integer,String> {
        String username;
        ServerHandler mServerHandler;
        Buddy bob;

        public StartConversationWithNewBuddyTask() {
            super();
            mServerHandler = ServerHandler.getOrCreateInstance();
            bob = null;
        }

        @Override
        protected String doInBackground(String... params) {
            username = params[0];
            PublicKey pk = mServerHandler.get_public_key_for_username(username);
            if (pk != null) {
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
                Log.d("DialResponse", "Added or updated key for  " + bob.getUsername() + ": " + Utility.uint_string_from_bytes(bob.getPublic_key().toBytes()));
                Intent intent = new Intent();
                intent.setAction(MCMixConstants.BUDDY_ADDED_BROADCAST_TAG);
                MCMixApplication.getContext().sendBroadcast(intent);
                Log.d("DialResponse", "Sent Broadcast");
                ConversationHandler.getOrCreateInstance().startConversation(bob);
                launchConversation(bob);

            }
        }
    }
}
