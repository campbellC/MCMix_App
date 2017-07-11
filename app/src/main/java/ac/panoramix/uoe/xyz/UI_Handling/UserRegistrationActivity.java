package ac.panoramix.uoe.xyz.UI_Handling;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ac.panoramix.uoe.xyz.Accounts.Account;
import ac.panoramix.uoe.xyz.Networking.ServerHandler;
import ac.panoramix.uoe.xyz.R;
import ac.panoramix.uoe.xyz.XYZApplication;

public class UserRegistrationActivity extends AppCompatActivity {

    private Button user_create_button;
    private EditText username_input;
    private EditText password_input;
    private EditText password_repeat_input;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        username_input = (EditText) findViewById(R.id.user_creation_username);
        password_input = (EditText) findViewById(R.id.user_creation_password);
        password_repeat_input= (EditText) findViewById(R.id.user_creation_password_repeat);

        user_create_button = (Button) findViewById(R.id.user_creation_button);
        user_create_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(username_input.getText().length() == 0 || password_input.getText().length() == 0){

                } else if (!password_input.getText().toString().equals(password_repeat_input.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Passwords do not match.", Toast.LENGTH_LONG).show();
                } else {
                    new CreateUserTask().execute(username_input.getText().toString(), password_input.getText().toString());
                }

            }
        });
    }

    private class CreateUserTask extends AsyncTask<String, Integer, String> {
        ServerHandler mServerHandler;
        String response;
        String username;
        public CreateUserTask(){
            super();
            mServerHandler = new ServerHandler();

        }

        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            this.username = username;
            String password = params[1];
            response = mServerHandler.create_user(username, password);
            return "Finished creating user attempt.";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mServerHandler.log_cookies();
            Log.d("LoginTask", "user is logged in: " + Boolean.toString(mServerHandler.is_logged_in()));
            if (response == null) {
                Toast.makeText(getApplicationContext(), "Failed to connect to server", Toast.LENGTH_LONG).show();
            } else {
                switch (response) {
                    case ServerHandler.GOOD_STATUS:
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        // These flags make it so that pressing Back will not return the user to the login screen

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        break;
                    case ServerHandler.USERNAME_ALREADY_EXISTS:
                        Toast.makeText(getApplicationContext(), "Username already exists", Toast.LENGTH_LONG).show();
                        break;
                    case ServerHandler.PASSWORD_DOES_NOT_CONFORM:
                        Toast.makeText(getApplicationContext(), "Password does not conform to guidelines", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "Something went wrong " + response, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }
    }
}
