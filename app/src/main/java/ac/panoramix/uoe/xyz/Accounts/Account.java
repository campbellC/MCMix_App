package ac.panoramix.uoe.xyz.Accounts;

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
import java.util.ArrayList;
import java.util.List;

public class Account implements Serializable{

    private String mUsername;
    private byte[] seed;
    private transient KeyPair mKeyPair;
    private List<Buddy> mBuddies;

    public Account(String username) {
        mUsername = username;
        mKeyPair = generateKeys();
        mBuddies = new ArrayList<>();
    }

    private KeyPair generateKeys() {
        seed = new Random().randomBytes(SodiumConstants.SECRETKEY_BYTES);
        return new KeyPair(seed);
    }

    public void addBuddy(Buddy inBuddy){
        mBuddies.add(inBuddy);
    }

    public KeyPair getKeyPair() {
        return mKeyPair;
    }

    public String getUsername() {
        return mUsername;
    }


    public List<Buddy> getBuddies() {
        return mBuddies;
    }


    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        mKeyPair = new KeyPair(seed);
    }

}
