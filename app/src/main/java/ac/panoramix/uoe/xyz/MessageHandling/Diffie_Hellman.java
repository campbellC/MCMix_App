package ac.panoramix.uoe.xyz.MessageHandling;

import org.libsodium.jni.SodiumConstants;
import org.libsodium.jni.crypto.Hash;
import org.libsodium.jni.crypto.Point;
import org.libsodium.jni.crypto.Random;
import org.libsodium.jni.keys.KeyPair;
import org.libsodium.jni.keys.PublicKey;

import ac.panoramix.uoe.xyz.Accounts.Account;
import ac.panoramix.uoe.xyz.Accounts.Buddy;
import ac.panoramix.uoe.xyz.Utility;
import ac.panoramix.uoe.xyz.XYZConstants;

/**
 * Created by: Chris Campbell
 * on: 31/05/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

public class Diffie_Hellman {

    //TODO: Make the input uniform. All inputs should be of the form Account, Buddy. Or byte[] byte[]
    // TODO: or something...


    public static Point shared_secret(KeyPair alice_keys, PublicKey bob_key){
        return new Point(bob_key.toBytes()).mult(alice_keys.getPrivateKey().toBytes());
    }



    // TODO: decide on format for round_number and dead_drop
    public static byte[] dead_drop(Account alice, Buddy bob, long round_number){

        Point shared_secret = shared_secret(alice.getKeyPair(), bob.getPublic_key());

        byte[] secret = shared_secret.toBytes();
        byte[] r = Utility.longToBytes(round_number);
        assert r.length ==8;

        byte[] concatenated = new byte[8 + SodiumConstants.SCALAR_BYTES];
        System.arraycopy(secret, 0, concatenated,0, SodiumConstants.SCALAR_BYTES);
        System.arraycopy(r, 0, concatenated, 0, 8);

        Hash hash = new Hash();
        byte[] dead_drop = hash.sha256(concatenated);
        assert dead_drop.length == XYZConstants.DEAD_DROP_LENGTH;

        return dead_drop;

    }

    public static byte[] dead_drop(Account alice){
        // In the case that alice does not want to communicate with anyone
        // the dead drop value is calculated with a random delivery address
        // TODO: possible overhead generating these classes, maybe best to change signature of dead_drop to take bytes instead of Buddy etc.
        Buddy bob = new Buddy( "",
                        new PublicKey(
                            new Random().randomBytes(
                                    SodiumConstants.PUBLICKEY_BYTES)));
        return dead_drop(alice, bob, 0);
    }
}
