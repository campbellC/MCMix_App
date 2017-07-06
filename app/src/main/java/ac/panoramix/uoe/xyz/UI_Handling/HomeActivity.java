package ac.panoramix.uoe.xyz.UI_Handling;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import ac.panoramix.uoe.xyz.Accounts.Account;
import ac.panoramix.uoe.xyz.Accounts.Buddy;
import ac.panoramix.uoe.xyz.R;
import ac.panoramix.uoe.xyz.Networking.XYZNetworkService;

public class HomeActivity extends AppCompatActivity {
    Button start_conversation;
    Button start_network_service;
    Button stop_network_service;

    Account Alice;
    //TODO: Bob should be chosen from a list
    Buddy bob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        start_conversation = (Button) findViewById(R.id.start_conversation_button);
        //TODO: remove the below code when accound handling is sorted
        Alice = new Account("Alice");
        Account Bob = new Account("bob");
        bob = new Buddy("bob", Bob.getKeyPair().getPublicKey());

        start_conversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadConversation(bob);
            }
        });

        start_network_service = (Button) findViewById(R.id.start_privacy_button);
        stop_network_service = (Button) findViewById(R.id.stop_privacy_button);

        start_network_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                begin_network_service();
            }
        });

        stop_network_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                end_network_service();
            }
        });


    }


    private void loadConversation(Buddy bob){

        Intent intent = new Intent(this, ConversationActivity.class);
        Log.d("debug_activity", "Loading Alice into intent");
        intent.putExtra("Alice", Alice);
        Log.d("debug_activity", "Loading Bob into intent");
        intent.putExtra("Bob", bob);
        Log.d("debug_activity", "Starting intent");
        startActivity(intent);
    }

    private void begin_network_service(){
        Intent intent = new Intent(getApplicationContext(), XYZNetworkService.class);
        intent.putExtra("Alice", Alice);
        getApplicationContext().startService(intent);
    }

    private void end_network_service() {
        Intent intent = new Intent(getApplicationContext(), XYZNetworkService.class);
        getApplicationContext().stopService(intent);
    }
}
