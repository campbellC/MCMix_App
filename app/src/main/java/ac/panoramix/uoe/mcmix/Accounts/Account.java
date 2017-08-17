package ac.panoramix.uoe.mcmix.Accounts;

/**
 * Created by: Chris Campbell
 * on: 31/05/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

import org.libsodium.jni.SodiumConstants;
import org.libsodium.jni.crypto.Random;
import org.libsodium.jni.keys.KeyPair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class Account implements Serializable{
    /* The Account class of the client application.
       This class is only used for the owner of the android device. For
       contacts use the Buddy class. */

    private String mUsername;
    private byte[] seed;
    private transient KeyPair mKeyPair;

    public Account(String username) {
        mUsername = username;
        mKeyPair = generateKeys();
    }

    private KeyPair generateKeys() {
        seed = new Random().randomBytes(SodiumConstants.SECRETKEY_BYTES);
        return new KeyPair(seed);
    }


    public KeyPair getKeyPair() {
        return mKeyPair;
    }

    public String getUsername() {
        return mUsername;
    }




    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        mKeyPair = new KeyPair(seed);
    }

}
