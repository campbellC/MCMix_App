package ac.panoramix.uoe.mcmix.UI_Handling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ac.panoramix.uoe.mcmix.Accounts.Account;
import ac.panoramix.uoe.mcmix.Accounts.Buddy;
import ac.panoramix.uoe.mcmix.ConversationProtocol.ConversationHandler;
import ac.panoramix.uoe.mcmix.DialingProtocol.DialHandler;
import ac.panoramix.uoe.mcmix.MCMixConstants;
import ac.panoramix.uoe.mcmix.R;
import ac.panoramix.uoe.mcmix.MCMixApplication;

public class HomeActivity extends AppCompatActivity {
    BroadcastReceiver mDialRecievedReceiver;
    private Button add_buddy_button;

    private Button stop_conversation_button;

    private Button start_conversation_button;

    private Button dial_buddy_button;
    private Button start_conversation_with_incoming_dial;

    private Button start_conversation_with_outgoing_dial;

    private Button block_incoming_dials;
    private Button allow_incoming_dials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        stop_conversation_button = (Button) findViewById(R.id.stop_conversation_button);
        stop_conversation_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConversationHandler.getOrCreateInstance().endConversation();
            }
        });

        add_buddy_button = (Button) findViewById(R.id.add_buddy_button);
        add_buddy_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NewBuddyActivity.class);
                startActivity(intent);
            }
        });


        start_conversation_button = (Button) findViewById(R.id.start_conversation_button);
        start_conversation_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Buddy bob = get_buddy_for_conversation();
                if(bob == null){
                    Toast.makeText(HomeActivity.this, "Buddy not known.", Toast.LENGTH_SHORT).show();
                } else {
                    launch_conversation(bob);
                }
            }
        });

        dial_buddy_button = (Button) findViewById(R.id.send_dial_button);
        dial_buddy_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Buddy bob = get_buddy_for_dial();
                if(bob != null){
                    DialHandler.getOrCreateInstance().handle_user_request_to_dial(bob);
                } else {
                    Toast.makeText(HomeActivity.this, "Buddy not known. Try adding them to your buddies.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        start_conversation_with_incoming_dial = (Button) findViewById(R.id.start_conversation_with_last_dial_button);
        start_conversation_with_incoming_dial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Buddy bob = DialHandler.getOrCreateInstance().get_last_incoming_dial_for_user();
                if(bob != null){
                    launch_conversation(bob);
                } else {
                    Toast.makeText(HomeActivity.this, "Nobody has dialed you", Toast.LENGTH_SHORT).show();
                }
            }
        });

        start_conversation_with_outgoing_dial = (Button) findViewById(R.id.start_conversation_with_outgoing_dial);
        start_conversation_with_outgoing_dial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Buddy bob = DialHandler.getOrCreateInstance().getLast_outgoing_dial();
                if(bob != null){
                    launch_conversation(bob);
                } else {
                    Toast.makeText(HomeActivity.this, "You haven't dialed anyone yet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        block_incoming_dials = (Button) findViewById(R.id.block_incoming_dials_button);
        block_incoming_dials.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialHandler.getOrCreateInstance().handle_user_request_to_block_dials();
                Toast.makeText(HomeActivity.this, "Blocking all incoming dials", Toast.LENGTH_SHORT).show();
            }
        });

        allow_incoming_dials = (Button) findViewById(R.id.allow_incoming_dials_button);
        allow_incoming_dials.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialHandler.getOrCreateInstance().handle_user_request_to_dialcheck();
                Toast.makeText(HomeActivity.this, "Allowing all incoming dials", Toast.LENGTH_SHORT).show();
            }
        });




    }

    private void launch_conversation(Buddy bob){
        ConversationHandler.getOrCreateInstance().startConversation(bob);
        Intent intent = new Intent(HomeActivity.this, ConversationActivity.class);
        intent.putExtra(MCMixConstants.BUDDY_EXTRA, bob);
        startActivity(intent);
    }
    private Buddy get_buddy_for_conversation(){
        EditText user_text = (EditText) findViewById(R.id.buddy_for_conversation_text);
        if(user_text.getText().length() == 0){
            return null;
        } else{
            String username = user_text.getText().toString();
            Account Alice = MCMixApplication.getAccount();
            for(Buddy bob : Alice.getBuddies()){
                if(bob.getUsername().equals(username)){
                    return bob;
                }
            }
            return null;
        }
    }
    private Buddy get_buddy_for_dial(){
        EditText user_text = (EditText) findViewById(R.id.buddy_for_dialing_input);
        if(user_text.getText().length() == 0){
            return null;
        } else{
            String username = user_text.getText().toString();
            Account Alice = MCMixApplication.getAccount();
            for(Buddy bob : Alice.getBuddies()){
                if(bob.getUsername().equals(username)){
                    return bob;
                }
            }
            return null;
        }
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
                Toast.makeText(HomeActivity.this,
                        DialHandler.getOrCreateInstance().get_last_incoming_dial_for_user().getUsername() + " has dialed you",
                        Toast.LENGTH_LONG).show();
            }
        }
    }



}
