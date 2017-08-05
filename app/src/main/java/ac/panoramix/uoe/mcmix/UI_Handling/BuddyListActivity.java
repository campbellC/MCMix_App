package ac.panoramix.uoe.mcmix.UI_Handling;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import ac.panoramix.uoe.mcmix.Database.BuddyBase;
import ac.panoramix.uoe.mcmix.Database.MCMixDbContract;
import ac.panoramix.uoe.mcmix.R;

public class BuddyListActivity extends AppCompatActivity {

    private BuddyBase mBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buddy_list);
        mBase = BuddyBase.getOrCreateInstance(this);
        ListView buddy_listview = (ListView) findViewById(R.id.buddy_list_list_view);
        buddy_listview.setAdapter(new BuddyCursorAdapter(this, mBase.getBuddies()));


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
