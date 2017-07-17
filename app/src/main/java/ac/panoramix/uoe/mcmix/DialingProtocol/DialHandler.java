package ac.panoramix.uoe.mcmix.DialingProtocol;

import org.libsodium.jni.keys.PublicKey;

import ac.panoramix.uoe.mcmix.Accounts.Buddy;
import ac.panoramix.uoe.mcmix.Networking.GetPublicKeyTask;
import ac.panoramix.uoe.mcmix.Networking.ServerHandler;
import ac.panoramix.uoe.mcmix.Utility;

/**
 * Created by: Chris Campbell
 * on: 13/07/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

public class DialHandler {

    private static DialHandler mDialHandler;
    private DialHandler(){

    }
    public static DialHandler getOrCreateInstance(){
        if(mDialHandler == null){
            mDialHandler = new DialHandler();
        }
        return mDialHandler;
    }

    private Buddy bob = null;
    private boolean user_wants_to_dialcheck = true;
    private Buddy last_incoming_dial = null;
    private boolean last_dial_was_null = true;

    private boolean next_dial_is_dialcheck(){
        return bob == null && user_wants_to_dialcheck;
    }
    private boolean user_wants_to_dial(){
        return bob != null;
    }
    private boolean user_doesnt_want_to_dial_or_dialcheck(){
        return bob == null && !user_wants_to_dialcheck;
    }
    public synchronized void handle_user_request_to_dial(Buddy bob){
        this.bob = bob;
        user_wants_to_dialcheck = false;
    }

    public synchronized void setUser_wants_to_dialcheck(boolean user_wants_to_dialcheck) {
        this.user_wants_to_dialcheck = user_wants_to_dialcheck;
    }

    public synchronized void handle_dial_from_server(String dial){
        //TODO: currently this method makes calls to the server for the public key. Should this be wrapped in a PKS class?
        if(DialMessagePayloadConverter.is_username(dial)){
            last_dial_was_null = false;
            String bob_username = DialMessagePayloadConverter.get_username(dial);
            PublicKey bob_pk = ServerHandler.getOrCreateInstance().get_public_key_for_username(bob_username);
            //TODO: if this user is a known buddy then no need to create a new one right?
            last_incoming_dial = new Buddy(bob_username, bob_pk);
        } else {
            last_dial_was_null = true;
        }
    }

    public synchronized String get_dial_for_server(){
        if(next_dial_is_dialcheck()) {
            return DialMessagePayloadConverter.getDialCheck();
        } else if (user_wants_to_dial()){
            return DialMessagePayloadConverter.dialBuddy(bob);
        } else {
            return DialMessagePayloadConverter.dial_nobody();
        }
    }

    public synchronized Buddy get_last_dial_for_user(){
        return last_incoming_dial;
    }
    public synchronized boolean was_last_dial_null(){
        return last_dial_was_null;
    }
}
