package ac.panoramix.uoe.xyz.ConversationProtocol;

import java.util.concurrent.ConcurrentLinkedQueue;

import ac.panoramix.uoe.xyz.XYZConstants;

/**
 * Created by: Chris Campbell
 * on: 02/06/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

/**
 * This class is a thread safe queue that stores conversation messages for
 * future sending. It only allows MAX_MESSAGES_IN_QUEUE to be in the queue
 * at any one time.
 */

public class ConversationQueue extends ConcurrentLinkedQueue<ConversationMessage> {

    private static ConversationQueue sConversationQueue;

    public static ConversationQueue getOrCreateConversationQueue(){
       if (sConversationQueue == null){
           sConversationQueue = new ConversationQueue();
       }
       return sConversationQueue;
    }

    public ConversationQueue(){
        super();
    }

    @Override
    public boolean add(ConversationMessage m)
    {
        if(this.size() > XYZConstants.MAX_MESSAGES_IN_QUEUE ) {
            return false;
        } else {
            return super.add(m);
        }
    }
}
