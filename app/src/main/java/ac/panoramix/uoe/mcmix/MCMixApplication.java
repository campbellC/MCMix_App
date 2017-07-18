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
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
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
    private static SSLSocketFactory sSocketFactory;
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


    public static SSLSocketFactory getSocketFactory() {
        return sSocketFactory;
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
            InputStream cert_input = getResources().openRawResource(R.raw.mcmix);
            X509Certificate cert ;
            try{
                cert = (X509Certificate) cf.generateCertificate(cert_input);

            } finally {
                cert_input.close();
            }
            Log.d("MCMixApp", "cert: " + cert.toString());
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("mcmix", cert);


            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            SSLContext context;
            context= SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);
            sSocketFactory = context.getSocketFactory();

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
