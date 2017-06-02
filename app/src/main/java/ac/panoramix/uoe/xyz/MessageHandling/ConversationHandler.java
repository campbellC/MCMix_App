package ac.panoramix.uoe.xyz.MessageHandling;

import android.util.Log;

import org.libsodium.jni.crypto.Random;

import ac.panoramix.uoe.xyz.XYZConstants;

/**
 * Created by: Chris Campbell
 * on: 02/06/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */


public class ConversationHandler implements Runnable {

    ConversationQueue mConversationQueue;
    /**
     * Poll the channel with the entry server for conversation round beginning token
     * and then get the next message off the queue and send it.
     */
    @Override
    public void run() {
        Log.d("ConvHandler", "Conversation Handler began");
        //TODO: establish and poll connection with entry server

        while(true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.d("ConvHandler", "Interrupted sleep in Conversation Handler");
            }

            //TODO: if incoming message is a round-end token then line up next outgoing message
            //TODO: if incoming message is an incoming conversationmessage then add this to the relevant message queue.
            byte[] next_payload = getNextOutgoingMessage();
            //TODO: split into pieces and encrypt as per back end requirements

            Log.d("ConvHandler", "msg: " + next_payload.toString());

        }
    }
    public ConversationHandler(){
        mConversationQueue = ConversationQueue.getOrCreateQueue();
    }


    private byte[] getNextOutgoingMessage(){
        if(mConversationQueue.isEmpty()){
           return generateRandomMessage();
        } else {
            return mConversationQueue.poll().getPayload();
        }
    }

    private byte[] generateRandomMessage(){
        byte[] random_payload = new byte[XYZConstants.MESSAGE_LENGTH + XYZConstants.DEAD_DROP_LENGTH];
        byte[] random_dead_drop = new Random().randomBytes(XYZConstants.DEAD_DROP_LENGTH);
        System.arraycopy(random_dead_drop, 0, random_payload, 0, XYZConstants.DEAD_DROP_LENGTH);
        return random_payload;
    }
}
