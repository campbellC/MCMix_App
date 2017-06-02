package ac.panoramix.uoe.xyz.Accounts;

/**
 * Created by: Chris Campbell
 * on: 31/05/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

import org.libsodium.jni.keys.PublicKey;

/** This class is the "friend" class for XYZ. The only information
 * you need to know about a friend to call them is their public key and username.
 */
public class Buddy {
    private PublicKey mPublic_key;
    private String mUsername;

    public Buddy(String mUsername, PublicKey mPublic_key) {
        this.mPublic_key = mPublic_key;
        this.mUsername = mUsername;
    }

    public PublicKey getPublic_key() {
        return mPublic_key;
    }

    public String getUsername() {
        return mUsername;
    }
}
