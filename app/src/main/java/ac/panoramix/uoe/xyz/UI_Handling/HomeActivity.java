package ac.panoramix.uoe.xyz.UI_Handling;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ac.panoramix.uoe.xyz.Accounts.Account;
import ac.panoramix.uoe.xyz.Accounts.Buddy;
import ac.panoramix.uoe.xyz.R;

public class HomeActivity extends AppCompatActivity {
    Button add_buddy_button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        add_buddy_button = (Button) findViewById(R.id.add_buddy_button);
        add_buddy_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UpdateKeysActivity.class);
                startActivity(intent);
            }
        });

    }



}
