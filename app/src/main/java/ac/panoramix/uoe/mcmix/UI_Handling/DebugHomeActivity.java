package ac.panoramix.uoe.mcmix.UI_Handling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ac.panoramix.uoe.mcmix.Accounts.Account;
import ac.panoramix.uoe.mcmix.Accounts.Buddy;
import ac.panoramix.uoe.mcmix.Database.BuddyBase;
import ac.panoramix.uoe.mcmix.DialingProtocol.DialHandler;
import ac.panoramix.uoe.mcmix.MCMixApplication;
import ac.panoramix.uoe.mcmix.MCMixConstants;
import ac.panoramix.uoe.mcmix.R;

public class DebugHomeActivity extends AppCompatActivity {
    BroadcastReceiver mDialRecievedReceiver;
    private Button start_conversation_button;
    private Button buddy_list_button;
    private BuddyBase mBase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_home);


        mBase = BuddyBase.getOrCreateInstance(MCMixApplication.getContext());
        for(int i = 0; i <= 5; ++i){
            String name = "Bob" + Integer.toString(i);
            Account a = new Account(name);
            Buddy bob = new Buddy(name, a.getKeyPair().getPublicKey());
            mBase.addBuddy(bob);
            Log.d("debuggingAct", "Added buddy: " + name);
        }

        start_conversation_button = (Button) findViewById(R.id.start_conversation_button);
        start_conversation_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Buddy bob = get_buddy_for_conversation();
                if(bob == null){
                    Toast.makeText(DebugHomeActivity.this, "Buddy not known.", Toast.LENGTH_SHORT).show();
                } else {
                    launch_conversation(bob);
                }
            }
        });





        buddy_list_button = (Button) findViewById(R.id.BuddyListButton);
        buddy_list_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                launch_buddy_list();
            }
        });



    }

    private void launch_conversation(Buddy bob){
        Intent intent = new Intent(DebugHomeActivity.this, ConversationActivity.class);
        intent.putExtra(MCMixConstants.BUDDY_EXTRA, bob);
        startActivity(intent);
    }
    private Buddy get_buddy_for_conversation(){
        EditText user_text = (EditText) findViewById(R.id.buddy_for_conversation_text);

        if(user_text.getText().length() == 0){
            return null;
        } else{
            String username = user_text.getText().toString();
            for(Buddy bob : mBase.getBuddies()){
                if(bob.getUsername().equals(username)){
                    return bob;
                }
            }
            return null;
        }
    }

    private void launch_buddy_list(){
        Intent intent = new Intent(DebugHomeActivity.this, BuddyListActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDialRecievedReceiver = new DialRecievedReceiver();
        IntentFilter intentFilter = new IntentFilter(MCMixConstants.DIAL_ADDED_BROADCAST_TAG);
        getApplicationContext().registerReceiver(mDialRecievedReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        if(mDialRecievedReceiver != null){
            getApplicationContext().unregisterReceiver(mDialRecievedReceiver);
        }
        super.onStop();
    }

    private class DialRecievedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!DialHandler.getOrCreateInstance().was_last_dial_null()){
                Toast.makeText(DebugHomeActivity.this,
                        DialHandler.getOrCreateInstance().get_last_incoming_dial_for_user().getUsername() + " has dialed you",
                        Toast.LENGTH_LONG).show();
            }
        }
    }



}
