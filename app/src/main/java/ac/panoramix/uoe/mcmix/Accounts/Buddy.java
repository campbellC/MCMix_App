package ac.panoramix.uoe.mcmix.Accounts;

/**
 * Created by: Chris Campbell
 * on: 31/05/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

import org.libsodium.jni.SodiumConstants;
import org.libsodium.jni.keys.PublicKey;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

/** This class is the "friend" class for mcmix. The only information
 * you need to know about a friend to call them is their public key and username.
 */
public class Buddy implements Serializable{
    private transient PublicKey mPublic_key;
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

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(!(obj instanceof Buddy)) return false;
        Buddy b = (Buddy) obj;
        return b.getPublic_key().equals(mPublic_key)
                && mUsername.equals(b.getUsername());
    }

    @Override
    public int hashCode() {
        return Objects.hash(mUsername, mPublic_key);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.write(mPublic_key.toBytes());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        byte[] buf = new byte[SodiumConstants.PUBLICKEY_BYTES];
        in.read(buf);
        mPublic_key = new PublicKey(buf);
    }
}
