package ac.panoramix.uoe.xyz;

/**
 * Created by: Chris Campbell
 * on: 31/05/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

/** This class is the "friend" class for XYZ. The only information
 * you need to know about a friend to call them is their public key and username.
 */
public class Buddy {
    private byte[] mPublic_key;
    private String mUsername;

    public Buddy(byte[] mPublic_key, String mUsername) {
        this.mPublic_key = mPublic_key;
        this.mUsername = mUsername;
    }
}
