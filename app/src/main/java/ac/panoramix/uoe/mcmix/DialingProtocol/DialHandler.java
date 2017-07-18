package ac.panoramix.uoe.mcmix.DialingProtocol;

import org.libsodium.jni.keys.PublicKey;

import ac.panoramix.uoe.mcmix.Accounts.Buddy;
import ac.panoramix.uoe.mcmix.MCMixApplication;
import ac.panoramix.uoe.mcmix.Networking.ServerHandler;

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
    private Buddy last_outgoing_dial = null;
    private boolean last_incoming_dial_was_null = true;

    public synchronized void handle_user_request_to_dial(Buddy bob){
        this.bob = bob;
        user_wants_to_dialcheck = false;
    }

    public synchronized void handle_user_request_to_dialcheck(){
        this.user_wants_to_dialcheck = true;
        bob = null;
    }

    public synchronized void handle_user_request_to_block_dials(){
        this.user_wants_to_dialcheck = false;
        bob = null;
    }

    public synchronized String get_dial_for_server(){
        if(user_wants_to_dialcheck) {
            return DialMessagePayloadConverter.getDialCheck();
        } else if (bob != null){
            last_outgoing_dial = bob;
            return DialMessagePayloadConverter.dialBuddy(bob);
        } else {
            return DialMessagePayloadConverter.dial_nobody();
        }
    }

    public synchronized void handle_dial_from_server(String dial){
        //TODO: currently this method makes calls to the server for the public key. Should this be wrapped in a PKS class?
        if(DialMessagePayloadConverter.is_username(dial)){
            last_incoming_dial_was_null = false;
            String bob_username = DialMessagePayloadConverter.get_username(dial);
            last_incoming_dial = MCMixApplication.getBuddy(bob_username);
            if(last_incoming_dial == null) {
                PublicKey bob_pk = ServerHandler.getOrCreateInstance().get_public_key_for_username(bob_username);
                last_incoming_dial = new Buddy(bob_username, bob_pk);
            }
        } else {
            last_incoming_dial_was_null = true;
        }
    }


    public Buddy getLast_outgoing_dial() {
        return last_outgoing_dial;
    }

    public synchronized Buddy get_last_incoming_dial_for_user(){
        return last_incoming_dial;
    }
    public synchronized boolean was_last_dial_null(){
        return last_incoming_dial_was_null;
    }
}
