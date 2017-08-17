package ac.panoramix.uoe.mcmix.DialingProtocol;

import ac.panoramix.uoe.mcmix.Accounts.Buddy;
import ac.panoramix.uoe.mcmix.MCMixApplication;
import ac.panoramix.uoe.mcmix.MCMixConstants;
import ac.panoramix.uoe.mcmix.Utility;

/**
 * Created by: Chris Campbell
 * on: 17/07/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

/* This class is the sole authority on the structure of Dialing payloads
    that the server can understand. It's role is to generate the dialing
    payloads when the networking thread requires it.
 */

public class DialPayloadMaker {
    private static String DIAL_CHECK = "1";


    /* getDialCheck constructs a dial check payload so that incoming dials can be received */
    public static String getDialCheck(){
        String username_string = Utility.UInt_String_From_String(MCMixApplication.getAccount().getUsername());
        return DIAL_CHECK + " " + username_string;
    }

    /* Generate a payload that dials bob */
    public static String dialBuddy(Buddy bob){
        String alice_name =  Utility.UInt_String_From_String(MCMixApplication.getAccount().getUsername());
        String bob_name = Utility.UInt_String_From_String(bob.getUsername());
        return alice_name + " " + bob_name;
    }

    /* is_username checks if an incoming payload from the dialing protocol is
        a username. If this is an incoming dial then the response will be a username
        rather than a zero.
     */
    public static boolean is_username(String response){
        String[] nums = response.split(" ");
        assert(nums.length == MCMixConstants.USERNAME_LENGTH_IN_UINTS);
        for(String num : nums){
            if(!num.equals("0"))
                return true;
        }
        return false;
    }

    /* This extracts the username from the incoming dialing payload */
    public static String get_username(String response){
        return Utility.String_From_UInt_String(response);
    }

    /* This function constructs a payload that does not dial check or dial anyone else. It merely
        blocks all incoming dials.
     */
    public static String dial_nobody(){
        String username_string = Utility.UInt_String_From_String(MCMixApplication.getAccount().getUsername());
        return username_string + " " + username_string;

    }
}
