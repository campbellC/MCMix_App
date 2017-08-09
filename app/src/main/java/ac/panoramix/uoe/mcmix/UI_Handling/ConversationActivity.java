package ac.panoramix.uoe.mcmix.UI_Handling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.common.base.CharMatcher;

import ac.panoramix.uoe.mcmix.Accounts.Buddy;
import ac.panoramix.uoe.mcmix.ConversationProtocol.ConversationHandler;
import ac.panoramix.uoe.mcmix.ConversationProtocol.ConversationMessage;
import ac.panoramix.uoe.mcmix.Database.ConversationBase;
import ac.panoramix.uoe.mcmix.DialingProtocol.DialHandler;
import ac.panoramix.uoe.mcmix.MCMixApplication;
import ac.panoramix.uoe.mcmix.MCMixConstants;
import ac.panoramix.uoe.mcmix.R;
import ac.panoramix.uoe.mcmix.Utility;

public class ConversationActivity extends AppCompatActivity {

    /* A ConversationActivity does not exist without a Buddy who the
     *   conversation is with.
     */
    Buddy bob;

    /* The top portion of the screen is a view on the history
        of this conversation.
     */
    ListView conversation_view;
    ConversationAdapter mAdapter;

    /* The bottom portion of the screen is either a send message
        area or a button for dialing bob.
     */
    Button send_button;
    EditText message_entry;
    Button dial_bob;
    ViewSwitcher send_message_switcher;
    View send_message_view;
    View dial_bob_view;
    ImageButton back_button;

    /* The message sent receiver is used so that when a message is added to the conversation the
        activity will update to show it.
     */
    MessageSentReceiver mMessageSentReceiver;

    private ConversationHandler mConversationHandler = ConversationHandler.getOrCreateInstance();
    private ConversationBase mBase = ConversationBase.getOrCreateInstance(MCMixApplication.getContext());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        back_button = (ImageButton) findViewById(R.id.previous);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



        /* The launching activity must attach the Buddy this conversation is with.
         */
        bob = (Buddy) getIntent().getSerializableExtra(MCMixConstants.BUDDY_EXTRA);
        ((TextView) findViewById(R.id.toolbar_buddy_name)).setText(bob.getUsername());



        send_message_switcher = (ViewSwitcher) findViewById(R.id.conversation_dial_or_type_switcher);
        dial_bob_view = findViewById(R.id.dial_bob_view);
        send_message_view = findViewById(R.id.send_message_view);


        dial_bob = (Button) findViewById(R.id.start_conversation_button);
        dial_bob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: if in conversation with someone else, check whether there are unsent outgoing messages
                // and give the user a chance to decide whether they want to delete thhose before doing anything

                /* On clicking this button the user wishes to dial bob and therefore cease any other
                conversations. If they have outgoing messages waiting to be sent then this may be
                unintended. Therefore we check if they meant to by offering them a chance to wait
                first instead.
                 */
                if(mConversationHandler.inConversation() && !mConversationHandler.outgoingQueueIsEmpty()){
                    AlertDialog.Builder alert = new AlertDialog.Builder(ConversationActivity.this);
                    alert.setTitle("Unsent Message Warning");
                    alert.setMessage("You have unsent messages in your currently active conversation. Do you want " +
                            "to not send these and start a new conversation?");


                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            mConversationHandler.startConversation(bob);
                            DialHandler.getOrCreateInstance().handle_user_request_to_dial(bob);
                            changeDialView();
                            Toast.makeText(ConversationActivity.this, getResources().getString(R.string.dial_buddy_toast), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });

                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    });

                    alert.show();

                } else {
                    mConversationHandler.startConversation(bob);
                    DialHandler.getOrCreateInstance().handle_user_request_to_dial(bob);
                    changeDialView();
                    Toast.makeText(ConversationActivity.this, getResources().getString(R.string.dial_buddy_toast), Toast.LENGTH_SHORT).show();
                }
            }
        });


        message_entry = (EditText) findViewById(R.id.conversation_message_input);

        send_button = (Button) findViewById(R.id.conversation_send_button);
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        mAdapter = new ConversationAdapter(this, mBase.getMessageCursor(bob));

        conversation_view = (ListView) findViewById(R.id.conversation_history_view);
        conversation_view.setAdapter(mAdapter);

        changeDialView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMessageSentReceiver = new MessageSentReceiver();
        IntentFilter intentFilter = new IntentFilter(MCMixConstants.MESSAGES_UPDATED_BROADCAST_TAG);
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
        updateUI();
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


    private boolean conversationIsActive(){
        return mConversationHandler.inConversationWith(bob);
    }

    private void updateUI(){
        mAdapter.changeCursor(mBase.getMessageCursor(bob));
    }
    private void changeDialView(){
        if(conversationIsActive()) {
            while(send_message_switcher.getCurrentView() != send_message_view){
                send_message_switcher.showNext();
            }
            ((TextView) findViewById(R.id.toolbar_active_conversation)).setText(getResources().getString(R.string.active_conversation_hint));
        } else {
            while(send_message_switcher.getCurrentView() != dial_bob_view){
                send_message_switcher.showNext();
            }
            dial_bob.setTransformationMethod(null);
            dial_bob.setText(getResources().getString(R.string.start_conversation_button_text) + " with " + bob.getUsername());
            ((TextView) findViewById(R.id.toolbar_active_conversation)).setText(getResources().getString(R.string.inactive_conversation_hint));
        }

    }

    private class ConversationAdapter extends CursorAdapter{
        private static final int FROM_ALICE = 0;
        private static final int FROM_BOB = 1;

        public ConversationAdapter(Context context, Cursor cursor){
            super(context, cursor, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            switch (getItemViewType(cursor)){
                case FROM_ALICE:
                    return LayoutInflater.from(context).inflate(R.layout.conversation_message_from_alice, parent, false);
                case FROM_BOB:
                    return LayoutInflater.from(context).inflate(R.layout.conversation_message_from_bob, parent, false);
                default:
                    return null;
            }
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            ConversationMessage msg = mBase.getMessageFromCursor(cursor);
            int type = getItemViewType(cursor);
            // we inflate a new view of the correct type and then insert the data from the message.
            // cursor adapters already implement view recycling so we don't need to be explicit about it here.
            switch (type){
                case FROM_ALICE:
                    ((TextView) view.findViewById(R.id.conversation_message_entry)).setText(msg.getMessage());
                    String formatted_date = Utility.format_date_for_display(msg.getTimestamp());
                    ((TextView) view.findViewById(R.id.conversation_message_timestamp)).setText(formatted_date);
                    if(msg.wasSent()){
                        //\u2714 = unicode check mark
                        ((TextView) view.findViewById(R.id.sent_confirmation_tick)).setText("\u2714");
                    } else {
                        // \u2022 = unicode bullet mark
                        ((TextView) view.findViewById(R.id.sent_confirmation_tick)).setText("\u2022");
                    }
                    break;
                case FROM_BOB:
                    ((TextView) view.findViewById(R.id.conversation_message_entry)).setText(msg.getMessage());
                    formatted_date = Utility.format_date_for_display(msg.getTimestamp());
                    ((TextView) view.findViewById(R.id.conversation_message_timestamp)).setText(formatted_date);
                    break;
            }
        }

        private int getItemViewType(Cursor cursor){
            ConversationMessage msg = mBase.getMessageFromCursor(cursor);
            return msg.isFrom_alice() ? FROM_ALICE : FROM_BOB;
        }
        @Override
        public int getItemViewType(int position) {
            Cursor cursor = (Cursor) getItem(position);
            return getItemViewType(cursor);
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }
    }


    private class MessageSentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    }

}
