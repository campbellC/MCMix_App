package ac.panoramix.uoe.xyz;

import android.app.Application;
import android.content.Context;

import java.net.CookieHandler;
import java.net.CookieManager;

import ac.panoramix.uoe.xyz.Accounts.Account;
import ac.panoramix.uoe.xyz.Accounts.Buddy;

/**
 * Created by: Chris Campbell
 * on: 06/07/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

public class XYZApplication extends Application {
    private static Application sApplication;


    private static Account sAccount;

    public static Application getApplication(){
        return sApplication;
    }

    public static Context getContext() {
        return getApplication().getApplicationContext();
    }

    public static Account getAccount() {
        return sAccount;
    }

    public static void setAccount(Account account) {
        sAccount = account;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        // This initialises cookie handling to allow persistent sessions with the server.
        CookieHandler.setDefault(new CookieManager());

    }
}
