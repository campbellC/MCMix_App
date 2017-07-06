package ac.panoramix.uoe.xyz.UI_Handling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

import ac.panoramix.uoe.xyz.Accounts.Account;
import ac.panoramix.uoe.xyz.Accounts.Buddy;
import ac.panoramix.uoe.xyz.MessageHandling.ConversationHistory;
import ac.panoramix.uoe.xyz.MessageHandling.ConversationMessage;
import ac.panoramix.uoe.xyz.MessageHandling.ConversationQueue;
import ac.panoramix.uoe.xyz.Networking.XYZNetworkService;
import ac.panoramix.uoe.xyz.R;
import ac.panoramix.uoe.xyz.Utility;
import ac.panoramix.uoe.xyz.XYZConstants;

public class ConversationActivity extends AppCompatActivity {
    Button send_button;
    EditText message_entry;
    ListView conversation_view;
    ArrayAdapter<ConversationMessage> mAdapter;
    ConversationQueue mConversationQueue;
    ConversationHistory mConversationHistory;
    private BroadcastReceiver mReceiver;
    Account Alice;
    Buddy Bob;
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
        Alice = (Account) intent.getSerializableExtra("Alice");
        Log.d("ConvActivity","Retrieving Bob from intent");
        Bob = (Buddy) intent.getSerializableExtra("Bob");


        Intent service_intent = new Intent(getApplicationContext(), XYZNetworkService.class);
        service_intent.putExtra("Alice", Alice);
        service_intent.putExtra("Bob", Bob);
        getApplicationContext().startService(service_intent);
        //Get conversation history from file
        mConversationHistory = retrieveConversationHistory(Alice, Bob);
        //Register receiver to stay alert for messages sent or received from the line.
        IntentFilter filter = new IntentFilter();
        filter.addAction(XYZConstants.MESSAGE_ADDED_BROADCAST_TAG);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("ConvActivity", "Recieved Broadcast");
                mConversationHistory = retrieveConversationHistory(Alice, Bob);
                mAdapter = new ArrayAdapter<ConversationMessage>(getApplicationContext(),
                        android.R.layout.simple_list_item_1,
                        mConversationHistory);
                conversation_view.setAdapter(mAdapter);

            }
        };
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mReceiver, filter);

        //Set up UI interaction
        message_entry = (EditText) findViewById(R.id.conversation_message_entry);

        send_button = (Button) findViewById(R.id.conversation_send_button);
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });


        conversation_view = (ListView) findViewById(R.id.conversation_history_view);
        mAdapter = new ArrayAdapter<ConversationMessage>(this.getApplicationContext(),
                android.R.layout.simple_list_item_1,
                mConversationHistory);
        conversation_view.setAdapter(mAdapter);

    }
    //TODO: check whether receiver registration/deregistration should take place in onStart/Stop rather than create/destroy

    @Override
    protected void onDestroy(){
        if(mReceiver != null){
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        super.onDestroy();
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
        } else if(msg.length() > XYZConstants.C_MESSAGE_BYTES){
            Toast.makeText(getApplicationContext(),
                    R.string.Long_Message_Toast,
                    Toast.LENGTH_SHORT).show();
            //TODO: add testing for this functionality
            String re = "(?<=\\\\G.{"+String.valueOf(XYZConstants.C_MESSAGE_BYTES) +"})";
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
        mConversationQueue.add(msg);
    }

    private ConversationHistory retrieveConversationHistory(Account alice, Buddy bob){
        String history_filename = Utility.filename_for_conversation(alice,bob);
        ConversationHistory history = new ConversationHistory(alice,bob);
        try {
            FileInputStream fis = getApplicationContext().openFileInput(history_filename);
            ObjectInputStream ois = new ObjectInputStream(fis);
            history = (ConversationHistory) ois.readObject();
        } catch (FileNotFoundException e){

        } catch (IOException ioe) {
            Log.d("ConvHandler", "IOException" , ioe);
        } catch (ClassNotFoundException cnfe){
            Log.d("ConvHandler", "ClassNotFoundException" + cnfe.getStackTrace());
        }
        return history;
    }

}
