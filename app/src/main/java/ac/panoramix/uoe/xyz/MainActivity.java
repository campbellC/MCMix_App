package ac.panoramix.uoe.xyz;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ac.panoramix.uoe.xyz.Accounts.Account;


public class MainActivity extends AppCompatActivity {
    private Account mAccount;
    EditText mUsername, mPassword;
    Button mLogIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUsername = (EditText) findViewById(R.id.username_editText);
        mPassword = (EditText) findViewById(R.id.password_editText);

        mLogIn = (Button) findViewById(R.id.logIn_button);
        mLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),
                        mUsername.getText().toString() + mPassword.getText().toString(),
                        Toast.LENGTH_SHORT).show();

            }
        });
        mAccount = new Account("Ted", "password");
    }
}
