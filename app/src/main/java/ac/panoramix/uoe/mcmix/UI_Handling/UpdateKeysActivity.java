package ac.panoramix.uoe.mcmix.UI_Handling;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import ac.panoramix.uoe.mcmix.Accounts.Buddy;
import ac.panoramix.uoe.mcmix.Database.MCMixDbContract;
import ac.panoramix.uoe.mcmix.Networking.GetPublicKeyTask;
import ac.panoramix.uoe.mcmix.R;
import ac.panoramix.uoe.mcmix.MCMixApplication;

public class UpdateKeysActivity extends AppCompatActivity {
    Button update_all_keys_button;
    Button update_one_key_button;
    EditText username_input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_keys);

        username_input = (EditText) findViewById(R.id.update_key_username_input);

        update_one_key_button = (Button) findViewById(R.id.update_key_one_user_button);
        update_one_key_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(username_input.getText().length() != 0){
                    new GetPublicKeyTask().execute(username_input.getText().toString());
                }
            }
        });

        update_all_keys_button = (Button) findViewById(R.id.update_key_all_users_button);
        update_all_keys_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(Buddy bob : MCMixApplication.getAccount().getBuddies()){
                    new GetPublicKeyTask().execute(bob.getUsername());
                }
            }
        });

    }
}
