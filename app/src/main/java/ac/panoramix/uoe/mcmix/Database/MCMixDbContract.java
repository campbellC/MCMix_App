package ac.panoramix.uoe.mcmix.Database;

import android.provider.BaseColumns;

/**
 * Created by: Chris Campbell
 * on: 04/08/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

public final class MCMixDbContract {
    private MCMixDbContract() {}

    // THe database has a table of all of the buddies with their public keys
    public static class BuddyEntry implements BaseColumns {
        public static final String TABLE_NAME = "Buddies";
        public static final String USERNAME_COLUMN = "username";
        public static final String PUBLIC_KEY_COLUMN = "public_key";
    }


    public static final String SQL_CREATE_BUDDY_ENTRIES =
            "CREATE TABLE " + BuddyEntry.TABLE_NAME + " (" +
                    BuddyEntry._ID + " INTEGER PRIMARY KEY," +
                    BuddyEntry.USERNAME_COLUMN + " TEXT UNIQUE," +
                    BuddyEntry.PUBLIC_KEY_COLUMN + " TEXT" + ")";

    public static final String SQL_DELETE_BUDDY_ENTRIES =
            "DROP TABLE IF EXISTS " + BuddyEntry.TABLE_NAME;


    //A second table stores the conversation messages that Alice and Bob have shared
    public static class ConversationMessageEntry implements BaseColumns {
        public static final String TABLE_NAME = "ConversationMessages";
        public static final String MESSAGE_COLUMN = "messages";
        public static final String SENT_COLUMN = "sent";
        public static final String TIMESTAMP_COLUMN = "timestamp";
        public static final String BUDDY_ID_COLUMN = "buddy";
        public static final String FROM_ALICE = "from_alice";
        public static final String UUID_COLUMN = "message_uuid";
    }

    public static final String SQL_CREATE_MESSAGES_ENTRIES =
            "CREATE TABLE " + ConversationMessageEntry.TABLE_NAME + " (" +
                    ConversationMessageEntry._ID + " INTEGER PRIMARY KEY," +
                    ConversationMessageEntry.BUDDY_ID_COLUMN + " INTEGER," +
                    ConversationMessageEntry.UUID_COLUMN + " TEXT," +
                    ConversationMessageEntry.MESSAGE_COLUMN + " TEXT," +
                    ConversationMessageEntry.FROM_ALICE + " BOOLEAN," +
                    ConversationMessageEntry.SENT_COLUMN + " BOOLEAN," +
                    ConversationMessageEntry.TIMESTAMP_COLUMN + " INTEGER," +
                    "FOREIGN KEY(" + ConversationMessageEntry.BUDDY_ID_COLUMN + ") REFERENCES "
                        + BuddyEntry.TABLE_NAME+"(" + BuddyEntry._ID + ") ON DELETE CASCADE" + ")";

    public static final String SQL_DELETE_MESSAGES_ENTRIES =
            "DROP TABLE IF EXISTS " + ConversationMessageEntry.TABLE_NAME;

}
