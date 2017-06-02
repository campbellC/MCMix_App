package ac.panoramix.uoe.xyz;

/**
 * Created by: Chris Campbell
 * on: 31/05/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

import org.libsodium.jni.SodiumConstants;
import org.libsodium.jni.crypto.Point;
import org.libsodium.jni.crypto.Random;
import org.libsodium.jni.keys.KeyPair;


import java.util.ArrayList;
import java.util.List;

public class Account {

    private String mUsername;
    private String mPassword; // TODO: make password a hash etc.
    private KeyPair mKeyPair; // TODO: Not certain that this is the correct type for DH KeyPairs.
    private List<Buddy> mBuddies;

    public Account(String mUsername, String mPassword) {
        mUsername = mUsername;
        mPassword = mPassword;
        mKeyPair = generateKeys();
        mBuddies = new ArrayList<>();

    }

    private static KeyPair generateKeys() {
        byte[] seed = new Random().randomBytes(SodiumConstants.SECRETKEY_BYTES);
        return new KeyPair(seed);
    }

    public void addBuddy(Buddy inBuddy){
        mBuddies.add(inBuddy);
    }

    public KeyPair getKeyPair() {
        return mKeyPair;
    }
}
