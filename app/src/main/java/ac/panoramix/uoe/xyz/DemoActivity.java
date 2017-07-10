package ac.panoramix.uoe.xyz;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import ac.panoramix.uoe.xyz.Accounts.Account;
import ac.panoramix.uoe.xyz.Accounts.Buddy;
import ac.panoramix.uoe.xyz.MessageHandling.ConversationHandler;

public class DemoActivity extends AppCompatActivity {
    Button send_message;
    EditText message_body;
    ConversationHandler mConversationHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        Account bob_account = new Account("Bob");
        Buddy bob = new Buddy("Bob", bob_account.getKeyPair().getPublicKey());
        mConversationHandler = ConversationHandler.getOrCreateInstance();
        mConversationHandler.startConversation(bob);

        send_message = (Button)  findViewById(R.id.demo_send_message_button);
        send_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!message_body.getText().equals("")){
                    String text = message_body.getText().toString();
                    mConversationHandler.handleMessageFromUser(text);
                    message_body.getText().clear();
                }
                mConversationHandler.log_status();
            }
        });

        message_body = (EditText) findViewById(R.id.demo_message_body);

    }
}
