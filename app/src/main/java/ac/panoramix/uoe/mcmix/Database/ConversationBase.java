package ac.panoramix.uoe.mcmix.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Date;
import java.util.UUID;

import ac.panoramix.uoe.mcmix.Accounts.Buddy;
import ac.panoramix.uoe.mcmix.ConversationProtocol.ConversationMessage;

/**
 * Created by: Chris Campbell
 * on: 06/08/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

public class ConversationBase {

    private SQLiteDatabase mDatabase;
    private static ConversationBase mBase;

    private ConversationBase(Context context){
        mDatabase = new MCMixDbHelper(context).getWritableDatabase();
    }

    public static ConversationBase getOrCreateInstance(Context context){
        if(mBase == null){
            mBase = new ConversationBase(context);
        }
        return mBase;
    }


    public Cursor getMessageCursor(Buddy bob){
        Cursor cursor = mDatabase.query(
                MCMixDbContract.BuddyEntry.TABLE_NAME, // get a buddy id for this buddy
                new String[] {MCMixDbContract.BuddyEntry._ID},
                MCMixDbContract.BuddyEntry.USERNAME_COLUMN + " = ?" ,
                new String[] {bob.getUsername() },
                null,
                null,
                null
        );
        cursor.moveToFirst();
        int buddy_id = cursor.getInt(cursor.getColumnIndex(MCMixDbContract.BuddyEntry._ID));

        return mDatabase.query(
                MCMixDbContract.ConversationMessageEntry.TABLE_NAME,//table name
                null, //all columns
                MCMixDbContract.ConversationMessageEntry.BUDDY_ID_COLUMN + " = ?", //only rows for this buddy
                new String[] {Integer.toString(buddy_id)},
                null, //no group by
                null, //no having clause
                MCMixDbContract.ConversationMessageEntry.TIMESTAMP_COLUMN + " ASC", //order by time sent/received
                null); //no limit on numbers
    }
    public ConversationMessage getMessage(UUID message_uuid){
        Cursor cursor = mDatabase.query(
                MCMixDbContract.ConversationMessageEntry.TABLE_NAME,
                null,
                MCMixDbContract.ConversationMessageEntry.UUID_COLUMN + " = ?" ,
                new String[] {message_uuid.toString() },
                null,
                null,
                null
        );
        if(cursor == null || cursor.getCount() == 0){
            Log.d("ConvBase", "Searched for uuid of message. No such message.");
            return null;
        } else {
            cursor.moveToFirst();
            Log.d("ConvBase", "Found message on uuid search");
            return getMessageFromCursor(cursor);
        }
    }

    public ConversationMessage getMessageFromCursor(Cursor cursor){
        if(!cursor.isAfterLast()) {
            String message = cursor.getString(cursor.getColumnIndex(MCMixDbContract.ConversationMessageEntry.MESSAGE_COLUMN));
            boolean sent = cursor.getInt(cursor.getColumnIndex(MCMixDbContract.ConversationMessageEntry.SENT_COLUMN)) > 0;
            boolean from_alice = cursor.getInt(cursor.getColumnIndex(MCMixDbContract.ConversationMessageEntry.FROM_ALICE)) > 0;
            Date timestamp = new Date(cursor.getLong(cursor.getColumnIndex(MCMixDbContract.ConversationMessageEntry.TIMESTAMP_COLUMN)));
            UUID uuid = UUID.fromString(cursor.getString(cursor.getColumnIndex(MCMixDbContract.ConversationMessageEntry.UUID_COLUMN)));
            return new ConversationMessage(message, from_alice, sent, timestamp, uuid);
        } else {
            return null;
        }
    }

    public void setMessageSent(UUID uuid, Buddy bob){
        ConversationMessage msg = getMessage(uuid);
        msg.setSent(true);
        updateMessage(msg, bob);
    }

    public boolean deleteMessage(UUID uuid){
        return mDatabase.delete(MCMixDbContract.ConversationMessageEntry.TABLE_NAME,
                MCMixDbContract.ConversationMessageEntry.UUID_COLUMN + " = ?",
                new String[] {uuid.toString()}) > 0;
    }

    public void addMessage(ConversationMessage message, Buddy bob){
        updateMessage(message, bob);
    }
    public void updateMessage(ConversationMessage message, Buddy bob){
        ContentValues values = getContentValues(message, bob);
        if(getMessage(message.getUuid()) == null){
            mDatabase.insert(MCMixDbContract.ConversationMessageEntry.TABLE_NAME, null, values);
        } else {
            mDatabase.update(MCMixDbContract.ConversationMessageEntry.TABLE_NAME, values,
                    MCMixDbContract.ConversationMessageEntry.UUID_COLUMN + " = ?",
                    new String[] {message.getUuid().toString()} );
        }
    }


    public ContentValues getContentValues(ConversationMessage message, Buddy bob) {
        ContentValues values = new ContentValues();
        values.put(MCMixDbContract.ConversationMessageEntry.MESSAGE_COLUMN, message.getMessage());
        values.put(MCMixDbContract.ConversationMessageEntry.SENT_COLUMN, message.wasSent());
        values.put(MCMixDbContract.ConversationMessageEntry.TIMESTAMP_COLUMN, message.getTimestamp().getTime());
        values.put(MCMixDbContract.ConversationMessageEntry.FROM_ALICE, message.isFrom_alice());
        values.put(MCMixDbContract.ConversationMessageEntry.UUID_COLUMN, message.getUuid().toString());
        Cursor cursor = mDatabase.query(
                MCMixDbContract.BuddyEntry.TABLE_NAME, // get a buddy id for this buddy
                new String[] {MCMixDbContract.BuddyEntry._ID},
                MCMixDbContract.BuddyEntry.USERNAME_COLUMN + " = ?" ,
                new String[] {bob.getUsername() },
                null,
                null,
                null
        );
        cursor.moveToFirst();
        int buddy_id = cursor.getInt(cursor.getColumnIndex(MCMixDbContract.BuddyEntry._ID));
        values.put(MCMixDbContract.ConversationMessageEntry.BUDDY_ID_COLUMN, buddy_id);
        return values;
    }
}
