package ac.panoramix.uoe.xyz.MessageHandling;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;

import ac.panoramix.uoe.xyz.Accounts.Account;
import ac.panoramix.uoe.xyz.Accounts.Buddy;

/**
 * Created by: Chris Campbell
 * on: 02/06/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

public class ConversationHistory extends CopyOnWriteArrayList<ConversationMessage> {
    Account alice;
    Buddy bob;

    public ConversationHistory(Account alice, Buddy bob) {
        this.alice = alice;
        this.bob = bob;
    }
}
