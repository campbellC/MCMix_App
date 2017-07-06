package ac.panoramix.uoe.xyz.MessageHandling;

import org.libsodium.jni.SodiumConstants;
import org.libsodium.jni.crypto.Hash;
import org.libsodium.jni.crypto.Point;

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

    public static Point shared_secret(Account alice, Buddy bob){
        return new Point(bob.getPublic_key().toBytes()).mult(alice.getKeyPair().getPrivateKey().toBytes());
    }



    // TODO: decide on format for round_number and dead_drop
    public static byte[] dead_drop(Account alice, Buddy bob, long round_number){

        Point shared_secret = shared_secret(alice, bob);

        byte[] secret = shared_secret.toBytes();
        byte[] r = Utility.longToBytes(round_number);
        assert r.length ==8;

        byte[] concatenated = new byte[8 + SodiumConstants.SCALAR_BYTES];
        System.arraycopy(secret, 0, concatenated,0, SodiumConstants.SCALAR_BYTES);
        System.arraycopy(r, 0, concatenated, 0, 8);

        Hash hash = new Hash();
        byte[] dead_drop = hash.sha256(concatenated);
        assert dead_drop.length == XYZConstants.DEAD_DROP_BYTES;

        return dead_drop;

    }


}
