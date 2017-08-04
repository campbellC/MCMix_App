package ac.panoramix.uoe.mcmix.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by: Chris Campbell
 * on: 04/08/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

public class MCMixDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "MCMix.db";

    public MCMixDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db){
        db.execSQL(MCMixDbContract.SQL_CREATE_BUDDY_ENTRIES);
        db.execSQL(MCMixDbContract.SQL_CREATE_MESSAGES_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        // For now we simply delete all tables
        db.execSQL(MCMixDbContract.SQL_DELETE_BUDDY_ENTRIES);
        db.execSQL(MCMixDbContract.SQL_DELETE_MESSAGES_ENTRIES);
        onCreate(db);
    }


}
