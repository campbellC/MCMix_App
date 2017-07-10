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
    Button end_conversation;

    Account Alice;
    //TODO: Bob should be chosen from a list
    Buddy bob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        start_conversation = (Button) findViewById(R.id.start_conversation_button);
        //TODO: remove the below code when accound handling is sorted

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

}
