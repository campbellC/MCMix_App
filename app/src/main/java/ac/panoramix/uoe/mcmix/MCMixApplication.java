package ac.panoramix.uoe.mcmix;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import ac.panoramix.uoe.mcmix.Accounts.Account;
import ac.panoramix.uoe.mcmix.Accounts.Buddy;

/**
 * Created by: Chris Campbell
 * on: 06/07/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

public class MCMixApplication extends Application {
    private static Application sApplication;


    private static Account sAccount;
    private static SSLContext sSSLContext;

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

    public static SSLContext getSSLContext() {
        return sSSLContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        // This initialises cookie handling to allow persistent sessions with the server.
        CookieHandler.setDefault(new CookieManager());
        // Initialise certificate handling for ssl connection
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = getResources().openRawResource(R.raw.mcmix);
            Certificate ca;
            try{
                ca = cf.generateCertificate(caInput);

            } finally {
                caInput.close();
            }
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            sSSLContext= SSLContext.getInstance("TLS");
            sSSLContext.init(null, tmf.getTrustManagers(), null);


        }   catch (CertificateException e){
            Log.d("MCMixApplication", "Bad certificate", e);
        } catch (IOException e ) {
            Log.d("MCMixApplication", "Can't find certificate resource", e);
        } catch (KeyStoreException e){
            Log.d("MCMixApplication", "Bad keystore", e);
        } catch (NoSuchAlgorithmException e) {
            Log.d("MCMixApplication", "Bad keystore", e);
        } catch (KeyManagementException e){
            Log.d("MCMixApplication", "Bad SSLContext", e);

        }
    }

    public static Buddy getBuddy(String username){
        for(Buddy b : getAccount().getBuddies()){
            if(b.getUsername().equals(username)){
                return b;
            }
        }
        return null;
    }
}
