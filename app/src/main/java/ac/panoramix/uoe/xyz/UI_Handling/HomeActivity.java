package ac.panoramix.uoe.xyz.UI_Handling;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ac.panoramix.uoe.xyz.Accounts.Account;
import ac.panoramix.uoe.xyz.Accounts.Buddy;
import ac.panoramix.uoe.xyz.ConversationProtocol.ConversationHandler;
import ac.panoramix.uoe.xyz.R;
import ac.panoramix.uoe.xyz.XYZApplication;

public class HomeActivity extends AppCompatActivity {
    private Button add_buddy_button;

    private Button stop_conversation_button;

    private Button start_conversation_button;

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
                Intent intent = new Intent(getApplicationContext(), UpdateKeysActivity.class);
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
                    ConversationHandler.getOrCreateInstance().startConversation(bob);
                    Intent intent = new Intent(HomeActivity.this, ConversationActivity.class);
                    startActivity(intent);
                }
            }
        });

    }

    private Buddy get_buddy_for_conversation(){
        EditText user_text = (EditText) findViewById(R.id.buddy_for_conversation_text);
        if(user_text.getText().length() == 0){
            return null;
        } else{
            String username = user_text.getText().toString();
            Account Alice = XYZApplication.getAccount();
            for(Buddy bob : Alice.getBuddies()){
                if(bob.getUsername().equals(username)){
                    return bob;
                }
            }
            return null;
        }
    }



}
