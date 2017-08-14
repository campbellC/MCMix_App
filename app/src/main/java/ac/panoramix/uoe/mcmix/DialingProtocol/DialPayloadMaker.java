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

public class DialPayloadMaker {
    private static String DIAL_CHECK = "1";


    public static String getDialCheck(){
        String username_string = Utility.UInt_String_From_String(MCMixApplication.getAccount().getUsername());
        return DIAL_CHECK + " " + username_string;
    }

    public static String dialBuddy(Buddy bob){
        String alice_name =  Utility.UInt_String_From_String(MCMixApplication.getAccount().getUsername());
        String bob_name = Utility.UInt_String_From_String(bob.getUsername());
        return alice_name + " " + bob_name;
    }

    public static boolean is_username(String response){
        String[] nums = response.split(" ");
        assert(nums.length == MCMixConstants.USERNAME_LENGTH_IN_UINTS);
        for(String num : nums){
            if(!num.equals("0"))
                return true;
        }
        return false;
    }

    public static String get_username(String response){
        return Utility.String_From_UInt_String(response);
    }

    public static String dial_nobody(){
        String username_string = Utility.UInt_String_From_String(MCMixApplication.getAccount().getUsername());
        return username_string + " " + username_string;

    }
}
