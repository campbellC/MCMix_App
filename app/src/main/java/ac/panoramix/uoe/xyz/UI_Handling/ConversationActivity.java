package ac.panoramix.uoe.xyz.UI_Handling;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import ac.panoramix.uoe.xyz.R;

public class ConversationActivity extends AppCompatActivity {
    Button send_button;
    EditText message_entry;

    //TODO: handle displaying history of messages

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        //TODO: Conversations should be loaded with a specific buddy in mind which is passed with the opening intent.

        send_button = (Button) findViewById(R.id.conversation_send_button);
        message_entry = (EditText) findViewById(R.id.conversation_message_entry);

        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String msg  = message_entry.getText().toString();
        //TODO: handle messages with non-ascii characters
        //TODO: handle messages of size that is not short enough.

    }

}
