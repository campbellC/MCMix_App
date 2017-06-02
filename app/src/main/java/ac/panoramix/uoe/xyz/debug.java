package ac.panoramix.uoe.xyz;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import ac.panoramix.uoe.xyz.Accounts.Account;
import ac.panoramix.uoe.xyz.Accounts.Buddy;
import ac.panoramix.uoe.xyz.MessageHandling.ConversationHandler;
import ac.panoramix.uoe.xyz.MessageHandling.Diffie_Hellman;

public class debug extends AppCompatActivity {
    EditText alice, bob, secret, deaddrop;
    Button go;
    int i = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        final Account Alice = new Account("Alice", "password");
        final Account Bob = new Account("Bob", "password");
        final Buddy Alice_buddy = new Buddy("Alice", Alice.getKeyPair().getPublicKey()) ;
        final Buddy Bob_buddy = new Buddy("Bob", Bob.getKeyPair().getPublicKey());


        alice = (EditText) findViewById(R.id.alice_debug);
        alice.setText(Alice.getKeyPair().getPublicKey().toString());
        bob = (EditText) findViewById(R.id.bob_debug);
        bob.setText(Bob.getKeyPair().getPublicKey().toString());
        secret   = (EditText) findViewById(R.id.secret_debug);
        deaddrop   = (EditText) findViewById(R.id.deaddrop_debug);

        go = (Button) findViewById(R.id.debug_button);
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Diffie_Hellman.shared_secret(Alice.getKeyPair(), Bob.getKeyPair().getPublicKey()).equals(
                        Diffie_Hellman.shared_secret(Bob.getKeyPair(), Alice.getKeyPair().getPublicKey()))){
                    deaddrop.setText(Diffie_Hellman.dead_drop(Bob, Alice_buddy, i).toString());
                }
                if( Diffie_Hellman.dead_drop(Alice, Bob_buddy, i).equals(  Diffie_Hellman.dead_drop(Bob, Alice_buddy, i))){
                    secret.setText(Diffie_Hellman.dead_drop(Alice, Bob_buddy, i).toString());
                }
                i++;
                secret.setText(Diffie_Hellman.dead_drop(Alice, Bob_buddy, i).toString());
                deaddrop.setText(Diffie_Hellman.dead_drop(Bob, Alice_buddy, i).toString());

            }
        });

//        ConversationHandler handler = new ConversationHandler();
//        Thread handlerThread = new Thread(handler);
//        handlerThread.start();
    }
}
