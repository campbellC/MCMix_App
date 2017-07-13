package ac.panoramix.uoe.mcmix.UI_Handling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.CharMatcher;

import java.util.List;

import ac.panoramix.uoe.mcmix.ConversationProtocol.ConversationHandler;
import ac.panoramix.uoe.mcmix.ConversationProtocol.ConversationMessage;
import ac.panoramix.uoe.mcmix.MCMixConstants;
import ac.panoramix.uoe.mcmix.ConversationProtocol.ConversationHistory;
import ac.panoramix.uoe.mcmix.R;

public class ConversationActivity extends AppCompatActivity {
    Button send_button;
    EditText message_entry;
    ListView conversation_view;
    ConversationAdapter mAdapter;
    MessageSentReceiver mMessageSentReceiver;

    private ConversationHistory mHistory;
    private ConversationHandler mConversationHandler = ConversationHandler.getOrCreateInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        message_entry = (EditText) findViewById(R.id.conversation_message_input);

        send_button = (Button) findViewById(R.id.conversation_send_button);
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        mAdapter = new ConversationAdapter(this, mConversationHandler.getCurrentConversationHistory());

        conversation_view = (ListView) findViewById(R.id.conversation_history_view);
        conversation_view.setAdapter(mAdapter);


    }

    @Override
    protected void onStart() {
        super.onStart();
        mMessageSentReceiver = new MessageSentReceiver();
        IntentFilter intentFilter = new IntentFilter(MCMixConstants.MESSAGE_ADDED_BROADCAST_TAG);
        getApplicationContext().registerReceiver(mMessageSentReceiver, intentFilter);

    }

    @Override
    protected void onStop() {
        if(mMessageSentReceiver != null){
            getApplicationContext().unregisterReceiver(mMessageSentReceiver);
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    /**
     * sendMessage takes a users message and asks the ConversationHandler to handle the message. Before
     * doing so it checks that it is ascii and also warns the user if it is too long and so will take longer
     * to send.
     */
    private void sendMessage() {
        String msg  = message_entry.getText().toString();
        if(msg.length() == 0) {
            return;
        }else if(!CharMatcher.ascii().matchesAllOf(msg)){
            Toast.makeText(getApplicationContext(),
                R.string.Bad_Character_Toast,
                Toast.LENGTH_SHORT).show();
            return;

        } else if(msg.length() > MCMixConstants.C_MESSAGE_BYTES) {
            Toast.makeText(getApplicationContext(),
                    R.string.Long_Message_Toast,
                    Toast.LENGTH_SHORT).show();
        }
        mConversationHandler.handleMessageFromUser(msg);
        message_entry.setText(null);
    }

    private void updateView(){
        mHistory = mConversationHandler.getCurrentConversationHistory();

    }

    private class ConversationAdapter extends ArrayAdapter<ConversationMessage>{
        private static final int FROM_ALICE = 0;
        private static final int FROM_BOB = 1;

        public ConversationAdapter(Context context, List<ConversationMessage> messages) {
            super(context, -1, messages);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            ConversationMessage msg = getItem(position);
            int type = getItemViewType(position);
            // in either case of message we check if convertView can be used to recycle views, if not
            // we inflate a new view of the correct type and then insert the data from the message.
            switch (type){
                case FROM_ALICE:
                    if(convertView == null){
                        convertView = getLayoutInflater().inflate(R.layout.conversation_message_from_alice, parent, false);
                    }
                    ((TextView) convertView.findViewById(R.id.conversation_message_entry)).setText(msg.getMessage());
                    break;
                case FROM_BOB:
                    if(convertView == null) {
                        convertView = getLayoutInflater().inflate(R.layout.conversation_message_from_bob, parent, false);
                    }
                    ((TextView) convertView.findViewById(R.id.conversation_message_entry)).setText(msg.getMessage());
                    break;
            }
            return convertView;
        }

        @Override
        public int getItemViewType(int position) {
            ConversationMessage msg = getItem(position);
            return msg.isFrom_alice() ? FROM_ALICE : FROM_BOB;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }
    }


    private class MessageSentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mAdapter.notifyDataSetChanged();
        }
    }

}