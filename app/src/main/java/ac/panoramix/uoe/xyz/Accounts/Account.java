package ac.panoramix.uoe.xyz.Accounts;

/**
 * Created by: Chris Campbell
 * on: 31/05/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

import android.os.Parcelable;

import org.libsodium.jni.SodiumConstants;
import org.libsodium.jni.crypto.Hash;
import org.libsodium.jni.crypto.Random;
import org.libsodium.jni.keys.KeyPair;
import org.libsodium.jni.keys.PublicKey;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ac.panoramix.uoe.xyz.XYZConstants;

public class Account implements Serializable{

    private String mUsername;
    private byte[] mHash;
    private byte[] salt;
    private byte[] seed;
    private transient KeyPair mKeyPair;
    private List<Buddy> mBuddies;

    public Account(String mUsername, String mPassword) {
        mUsername = mUsername;
        salt = new Random().randomBytes(XYZConstants.SALT_LENGTH);

        //TODO: HANDLE non-ascii passwords?
        byte[] pwd = mPassword.getBytes(StandardCharsets.US_ASCII);
        byte[] salted_pwd = new byte[XYZConstants.SALT_LENGTH + pwd.length];
        System.arraycopy(salt,0, salted_pwd,0,XYZConstants.SALT_LENGTH);
        System.arraycopy(pwd, 0, salted_pwd, XYZConstants.SALT_LENGTH, pwd.length);

        mHash = new Hash().sha256(salted_pwd);
        mKeyPair = generateKeys();
        mBuddies = new ArrayList<>();

    }

    private boolean testPassword(String password){

        byte[] pwd = password.getBytes(StandardCharsets.US_ASCII);
        byte[] salted_pwd = new byte[XYZConstants.SALT_LENGTH + pwd.length];
        System.arraycopy(salt,0, salted_pwd,0,XYZConstants.SALT_LENGTH);
        System.arraycopy(pwd, 0, salted_pwd, XYZConstants.SALT_LENGTH, pwd.length);

        return Arrays.equals(mHash, new Hash().sha256(salted_pwd));
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

    public byte[] getHash() {
        return mHash;
    }

    public byte[] getSalt() {
        return salt;
    }

    public List<Buddy> getBuddies() {
        return mBuddies;
    }


    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        mKeyPair = new KeyPair(seed);
    }

}
