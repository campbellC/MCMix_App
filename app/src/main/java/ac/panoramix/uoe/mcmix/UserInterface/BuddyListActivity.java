package ac.panoramix.uoe.mcmix.UserInterface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import ac.panoramix.uoe.mcmix.Accounts.Buddy;
import ac.panoramix.uoe.mcmix.ConversationProtocol.ConversationHandler;
import ac.panoramix.uoe.mcmix.Database.BuddyBase;
import ac.panoramix.uoe.mcmix.MCMixApplication;
import ac.panoramix.uoe.mcmix.MCMixConstants;
import ac.panoramix.uoe.mcmix.Networking.GetPublicKeyTask;
import ac.panoramix.uoe.mcmix.R;

/* This class is the 'home' screen of the app. It is simply a list
    of the buddies. Each buddy is clickable and clicking on it loads
    the conversation corresponding to that buddy.

    If the buddy is in active conversation then this is displayed in the list.
 */
public class BuddyListActivity extends DialResponderBaseActivity {

    private BuddyBase mBase = BuddyBase.getOrCreateInstance(MCMixApplication.getContext());
    private ListView mBuddyListview;
    private BuddyCursorAdapter mAdapter;
    private ImageButton mAddBuddyButton;


    /* Since this screen displays the List Buddy, it requires a broadcast receiver
        to detect when a buddy is added or updated in the list.
     */
    MessageSentReceiver mMessageSentReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buddy_list);

        // We populate the list view from the database of buddies
        mBuddyListview = (ListView) findViewById(R.id.buddy_list_list_view);
        mAdapter = new BuddyCursorAdapter(this, mBase.getBuddiesCursor());
        mBuddyListview.setAdapter(mAdapter);

        //The list view is clickable. On click it should launch the conversation activity for that buddy
        mBuddyListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) mBuddyListview.getItemAtPosition(position);
                Buddy bob = mBase.buddyFromCursor(cursor);
                launchConversation(bob);
            }
        });

        mAddBuddyButton = (ImageButton) findViewById(R.id.add_buddy_button);
        mAddBuddyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(BuddyListActivity.this);
                final EditText edittext = new EditText(getApplicationContext());
                alert.setMessage("Enter Buddy's Name");
                alert.setTitle("Add Buddy");

                alert.setView(edittext);

                alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String username = edittext.getText().toString();
                        if(username.length() > 0){
                            new GetPublicKeyTask().execute(username);
                            dialog.dismiss();
                        }
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });

                alert.show();
            }
        });

    }

    /* A CursorAdapter allows the ListView to display the entries in the database and
        recycle item views to save memory and provide proper scrolling speed.
     */
    private class BuddyCursorAdapter extends CursorAdapter {
        public BuddyCursorAdapter(Context context, Cursor cursor){
            super(context, cursor, 0);
        }
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.buddy_list_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Buddy bob = mBase.buddyFromCursor(cursor);

            TextView buddy_name_view = (TextView) view.findViewById(R.id.buddylist_item_username);
            buddy_name_view.setText(bob.getUsername());

            TextView active_conversation_view = (TextView) view.findViewById(R.id.buddylist_item_conversation_active);
            if(ConversationHandler.getOrCreateInstance().inConversationWith(bob)){
                active_conversation_view.setText(getResources().getString(R.string.active_conversation_hint));
            } else {
                active_conversation_view.setText("");
            }
        }
    }
    private void updateUI(){
        mAdapter.changeCursor(mBase.getBuddiesCursor());
    }

    /* this class responds to the addition/update of buddies in the list */
    private class MessageSentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BuddyList", "Received broadcast of added buddy");
            updateUI();
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        mMessageSentReceiver = new MessageSentReceiver();
        IntentFilter intentFilter = new IntentFilter(MCMixConstants.BUDDY_ADDED_BROADCAST_TAG);
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

}
