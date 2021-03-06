package ac.panoramix.uoe.mcmix.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;

import org.libsodium.jni.keys.PublicKey;

import java.util.ArrayList;
import java.util.List;

import ac.panoramix.uoe.mcmix.Accounts.Buddy;

/**
 * Created by: Chris Campbell
 * on: 04/08/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

/*
    This class is a wrapper for the database to allow easy manipulation of the Buddy table by
    other classes.
 */
public class BuddyBase {
    private SQLiteDatabase mDatabase;
    private static BuddyBase mBase;

    private BuddyBase(Context context){
        mDatabase = new MCMixDbHelper(context).getWritableDatabase();
    }

    public static BuddyBase getOrCreateInstance(Context context){
        if(mBase == null){
            mBase = new BuddyBase(context);
        }
        return mBase;
    }

    public Buddy buddyFromCursor(Cursor cursor){
        if(!cursor.isAfterLast()) {
            String username = cursor.getString(cursor.getColumnIndex(MCMixDbContract.BuddyEntry.USERNAME_COLUMN));
            String pk_str = cursor.getString(cursor.getColumnIndex(MCMixDbContract.BuddyEntry.PUBLIC_KEY_COLUMN));
            PublicKey pk = new PublicKey(Base64.decode(pk_str, Base64.DEFAULT));
            return new Buddy(username, pk);
        } else {
            return null;
        }
    }

    /*
        getBuddiesCursor retrieves the complete list of Buddies (e.g. for displaying on the Buddylist page)
     */
    public Cursor getBuddiesCursor(){
        return mDatabase.query(
                MCMixDbContract.BuddyEntry.TABLE_NAME,//table name
                null, //all columns
                null, //all rows
                null, //no arguments for WHERE
                null, //no group by
                null, //no having clause
                MCMixDbContract.BuddyEntry.USERNAME_COLUMN, //order alphabetically by username
                null); //no limit on numbers
    }

    /* This function allows retrieval of a Buddy just by its username.
        If the Buddy is not currently known then null is returned.
     */
    public Buddy getBuddy(String username){
        Cursor cursor = mDatabase.query(
                MCMixDbContract.BuddyEntry.TABLE_NAME,
                null,
                MCMixDbContract.BuddyEntry.USERNAME_COLUMN + " = ?" ,
                new String[] {username },
                null,
                null,
                null
        );
        if(cursor == null || cursor.getCount() == 0){
            return null;
        } else {
            try{
                while (cursor.moveToFirst()){
                    String pk_str = cursor.getString(cursor.getColumnIndex(MCMixDbContract.BuddyEntry.PUBLIC_KEY_COLUMN));
                    PublicKey pk = new PublicKey(Base64.decode(pk_str, Base64.DEFAULT));
                    return new Buddy(username, pk);
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    public void updateBuddy(Buddy bob){
        ContentValues values = getContentValues(bob);
        if(getBuddy(bob.getUsername()) == null){
            mDatabase.insert(MCMixDbContract.BuddyEntry.TABLE_NAME, null, values);
        } else {
            mDatabase.update(MCMixDbContract.BuddyEntry.TABLE_NAME, values,
                    MCMixDbContract.BuddyEntry.USERNAME_COLUMN + " = ?",
                    new String[] {bob.getUsername()} );
        }
    }

    public void addBuddy(Buddy bob){
        updateBuddy(bob);
    }

    /* ContentValues are objects that allow insertion into a SQLite database */
    public ContentValues getContentValues(Buddy bob) {
        ContentValues values = new ContentValues();
        values.put(MCMixDbContract.BuddyEntry.USERNAME_COLUMN, bob.getUsername());
        values.put(MCMixDbContract.BuddyEntry.PUBLIC_KEY_COLUMN, Base64.encodeToString(bob.getPublic_key().toBytes(), Base64.DEFAULT));
        return values;
    }

    public void deleteBuddy(String name){
        mDatabase.delete(MCMixDbContract.BuddyEntry.TABLE_NAME,
                MCMixDbContract.BuddyEntry.USERNAME_COLUMN + " = ?",
                new String[] {name});
    }
}
