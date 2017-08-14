package ac.panoramix.uoe.mcmix.DialingProtocol;

import android.content.Intent;

import ac.panoramix.uoe.mcmix.Accounts.Buddy;
import ac.panoramix.uoe.mcmix.MCMixApplication;
import ac.panoramix.uoe.mcmix.MCMixConstants;

/**
 * Created by: Chris Campbell
 * on: 13/07/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

public class DialHandler {
    /* The primary purpose of this class is to be an interface between the user and
        the network for dialing purposes. The user can request to make dials to a buddy
        and the network can deliver and request dial payloads from/for the server.
     */

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
            /*  Firstly record that we dialed bob before setting the handler to
                dialcheck for now forwards. We don't want to keep dialling the same
                person repeatedly.
             */
            last_outgoing_dial = bob;
            String dial = DialMessagePayloadConverter.dialBuddy(bob);
            handle_user_request_to_dialcheck();
            return dial;
        } else {
            return DialMessagePayloadConverter.dial_nobody();
        }
    }

    public synchronized void handle_dial_from_server(String dial){
        if(DialMessagePayloadConverter.is_username(dial)){
            /* On recieving a dial the dialhandler must alert the user. This is done via
                a broadcast to which user facing activities can respond as required.
             */
            last_incoming_dial_was_null = false;
            String bob_username = DialMessagePayloadConverter.get_username(dial);
            Intent intent = new Intent();
            intent.setAction(MCMixConstants.INCOMING_DIAL_RECEIVED_BROADCAST_TAG);
            intent.putExtra(MCMixConstants.BUDDY_EXTRA, bob_username);
            MCMixApplication.getContext().sendBroadcast(intent);
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
