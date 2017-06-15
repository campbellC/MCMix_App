package ac.panoramix.uoe.xyz.UI_Handling;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.common.base.CharMatcher;

import java.util.List;

import ac.panoramix.uoe.xyz.Accounts.Account;
import ac.panoramix.uoe.xyz.Accounts.Buddy;
import ac.panoramix.uoe.xyz.MessageHandling.ConversationHistory;
import ac.panoramix.uoe.xyz.MessageHandling.ConversationMessage;
import ac.panoramix.uoe.xyz.MessageHandling.ConversationQueue;
import ac.panoramix.uoe.xyz.R;
import ac.panoramix.uoe.xyz.XYZConstants;

public class ConversationActivity extends AppCompatActivity {
    Button send_button;
    EditText message_entry;
    ListView conversation_view;
    ArrayAdapter<ConversationMessage> mAdapter;
    ConversationQueue mConversationQueue;
    ConversationHistory mConversationHistory;

    //TODO: handle displaying history of messages

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        //Set up conversation handling
        mConversationQueue = ConversationQueue.getOrCreateConversationQueue();

        //TODO: should load conversation history from stored state
        Log.d("ConvActivity","Retrieving intent");
        Intent intent = getIntent();
        Log.d("ConvActivity","Retrieving Alice from intent");
        Account Alice = (Account) intent.getSerializableExtra("Alice");
        Log.d("ConvActivity","Retrieving Bob from intent");
        Buddy Bob = (Buddy) intent.getSerializableExtra("Bob");
        Log.d("ConvActivity", "Retrieving/Creating conversation history");
        mConversationHistory = new ConversationHistory(Alice, Bob);

        //Set up UI interaction
        message_entry = (EditText) findViewById(R.id.conversation_message_entry);

        send_button = (Button) findViewById(R.id.conversation_send_button);
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO: this call to add should actually only be done once message has been checked and transmitted
                mConversationHistory.add(new ConversationMessage(message_entry.getText().toString(), true));
                mAdapter.notifyDataSetChanged();
                sendMessage();
            }
        });


        conversation_view = (ListView) findViewById(R.id.conversation_history_view);
        mAdapter = new ArrayAdapter<ConversationMessage>(this.getApplicationContext(),
                android.R.layout.simple_list_item_1,
                mConversationHistory);
        conversation_view.setAdapter(mAdapter);

        //TODO: delete the below debugging code
        mAdapter.add(new ConversationMessage("from alice", true));
        mAdapter.add(new ConversationMessage("from bob", false));
        mConversationHistory.add(new ConversationMessage("direct addition", true));
    }

    private void sendMessage() {
        String msg  = message_entry.getText().toString();

        if(!CharMatcher.ascii().matchesAllOf(msg)){
            Toast.makeText(getApplicationContext(),
                R.string.Bad_Character_Toast,
                Toast.LENGTH_SHORT).show();
            return;
        }

        if(msg.length() == 0){
            return;
        } else if(msg.length() > XYZConstants.MESSAGE_LENGTH){
            Toast.makeText(getApplicationContext(),
                    R.string.Long_Message_Toast,
                    Toast.LENGTH_SHORT).show();
            //TODO: add testing for this functionality
            String re = "(?<=\\\\G.{"+String.valueOf(XYZConstants.MESSAGE_LENGTH) +"})";
            String[] msgs = msg.split(re);
            for(String s: msgs){
                transmit(new ConversationMessage(s, true));
            }
        } else {
            transmit(new ConversationMessage(msg, true));
        }
        message_entry.setText(null);

    }

    private void transmit(ConversationMessage msg){

    }

    private void addToHistory(ConversationMessage msg){

    }

}
