package ac.panoramix.uoe.xyz.MessageHandling;

import java.util.concurrent.ConcurrentLinkedDeque;

import ac.panoramix.uoe.xyz.Accounts.Account;
import ac.panoramix.uoe.xyz.Accounts.Buddy;

/**
 * Created by: Chris Campbell
 * on: 02/06/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

public class ConversationHistory extends ConcurrentLinkedDeque<ConversationMessage> {
    Account alice;
    Buddy bob;

    public ConversationHistory(Account alice, Buddy bob) {
        this.alice = alice;
        this.bob = bob;
    }
}
