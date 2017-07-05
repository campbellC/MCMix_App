package ac.panoramix.uoe.xyz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.libsodium.jni.SodiumConstants;
import org.libsodium.jni.crypto.Random;
import org.libsodium.jni.crypto.SecretBox;

import java.net.CookieHandler;
import java.net.CookieManager;

import ac.panoramix.uoe.xyz.Accounts.Account;
import ac.panoramix.uoe.xyz.Accounts.Buddy;
import ac.panoramix.uoe.xyz.MessageHandling.ConversationHandler;
import ac.panoramix.uoe.xyz.MessageHandling.ConversationMessage;
import ac.panoramix.uoe.xyz.MessageHandling.Diffie_Hellman;
import ac.panoramix.uoe.xyz.UI_Handling.ConversationActivity;

public class debug extends AppCompatActivity {
    EditText alice, bob, secret, deaddrop;
    Button go;
    int i = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);


        final Account Alice = new Account("Alice", "password");
        final Account Bob = new Account("Bob", "password");
        final Buddy Alice_buddy = new Buddy("Alice", Alice.getKeyPair().getPublicKey()) ;
        final Buddy Bob_buddy = new Buddy("Bob", Bob.getKeyPair().getPublicKey());


        alice = (EditText) findViewById(R.id.alice_debug);
        bob = (EditText) findViewById(R.id.bob_debug);
        secret   = (EditText) findViewById(R.id.secret_debug);
        deaddrop   = (EditText) findViewById(R.id.deaddrop_debug);

        go = (Button) findViewById(R.id.debug_button);


        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String message = "This is a test string" + String.valueOf(i);
//                i = i * i;
//                ConversationMessage msg = new ConversationMessage(message);
//                secret.setText(String.valueOf(msg.getBytes().length));
//                alice.setText(message);
//                SecretBox Alices_SecretBox = new SecretBox(Diffie_Hellman.shared_secret(Alice, Bob_buddy).toBytes());
//                SecretBox Bobs_SecretBox = new SecretBox(Diffie_Hellman.shared_secret(Bob, Alice_buddy).toBytes());
//                byte[] nonce = new Random().randomBytes(SodiumConstants.NONCE_BYTES);
//
//                byte[] ciphertext = Alices_SecretBox.encrypt(nonce, msg.getBytes());
//                bob.setText(String.valueOf(ciphertext.length));
//
//                byte[] unDecrypted = Bobs_SecretBox.decrypt(nonce, ciphertext);
//                ConversationMessage bobs_msg = new ConversationMessage(unDecrypted);
//                deaddrop.setText(bobs_msg.getMessage());
//                String s_m1 = "test string";
//                ConversationMessage message1 = new ConversationMessage(s_m1);
//                byte[] m1_bytes = message1.getBytes();
//                if(i == 2){
//                    alice.setText(message1.getMessage());
//                    i++;
//                } else {
//                    ConversationMessage message2 = new ConversationMessage(m1_bytes);
//                    bob.setText(message2.getMessage());
//                    secret.setText(String.valueOf(message1.equals(message2)));
//                }
                loadConversation(go, Alice, Bob_buddy);


            }
        });

//        ConversationHandler handler = new ConversationHandler();
//        Thread handlerThread = new Thread(handler);
//        handlerThread.start();
    }


    public void loadConversation(View Pressed, Account Alice, Buddy Bob){
        Intent intent = new Intent(this, ConversationActivity.class);
        Log.d("debug_activity", "Loading Alice into intent");
        intent.putExtra("Alice", Alice);
        Log.d("debug_activity", "Loading Bob into intent");
        intent.putExtra("Bob", Bob);
        Log.d("debug_activity", "Starting intent");
        startActivity(intent);
    }
}
