package ac.panoramix.uoe.mcmix.UI_Handling;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import ac.panoramix.uoe.mcmix.Database.BuddyBase;
import ac.panoramix.uoe.mcmix.Database.MCMixDbContract;
import ac.panoramix.uoe.mcmix.R;

public class BuddyListActivity extends AppCompatActivity {

    private BuddyBase mBase;
    private ListView mBuddyListview;
    private BuddyCursorAdapter mAdapter;
    private ImageButton mAddBuddyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buddy_list);

        // We populate the list view from the database of buddies
        mBase = BuddyBase.getOrCreateInstance(this);
        mBuddyListview = (ListView) findViewById(R.id.buddy_list_list_view);
        mAdapter = new BuddyCursorAdapter(this, mBase.getBuddies());
        mBuddyListview.setAdapter(mAdapter);

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
                        Toast.makeText(BuddyListActivity.this, "Adding " + edittext.getText().toString(), Toast.LENGTH_SHORT).show();
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
            TextView buddy_name_view = (TextView) view.findViewById(R.id.list_item_buddy_name);
            String buddy_name = cursor.getString(cursor.getColumnIndex(MCMixDbContract.BuddyEntry.USERNAME_COLUMN));
            buddy_name_view.setText(buddy_name);
        }
    }
}
