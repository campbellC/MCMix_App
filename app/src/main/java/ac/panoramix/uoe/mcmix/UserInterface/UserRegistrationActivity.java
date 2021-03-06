package ac.panoramix.uoe.mcmix.UserInterface;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Pattern;

import ac.panoramix.uoe.mcmix.Accounts.Account;
import ac.panoramix.uoe.mcmix.MCMixApplication;
import ac.panoramix.uoe.mcmix.MCMixConstants;
import ac.panoramix.uoe.mcmix.Networking.ServerHandler;
import ac.panoramix.uoe.mcmix.R;
import ac.panoramix.uoe.mcmix.Utility;

/* This class allows the user to create an account.
Note that this does not log in the user and does not submit the user's public key. The user
must pass through the log in screen and only upon logging in does this happen.

Some client side validation is done on the username and password but most of the validation occurs
on the server side.
 */
public class UserRegistrationActivity extends AppCompatActivity {




    private Button user_create_button;
    private EditText username_input;
    private EditText password_input;
    private EditText password_repeat_input;

    private Account new_account;

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

                } else if (!validate_username(username_input.getText().toString())) {
                    Toast.makeText(UserRegistrationActivity.this, "Username does not meet requirements", Toast.LENGTH_SHORT).show();
                } else if (!password_input.getText().toString().equals(password_repeat_input.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Passwords do not match.", Toast.LENGTH_LONG).show();
                } else if (password_input.getText().toString().length() < MCMixConstants.PASSWORD_MIN_LENGTH){
                    Toast.makeText(getApplicationContext(), "Passwords must be 8 characters or longer.", Toast.LENGTH_LONG).show();

                } else {
                    new_account = new Account(username_input.getText().toString());
                    new CreateUserTask().execute(username_input.getText().toString(), password_input.getText().toString());
                }

            }
        });
    }

    private void StoreAccount(){
        Utility.saveAccountToDisk();
    }


    private class CreateUserTask extends AsyncTask<String, Integer, String> {
        ServerHandler mServerHandler;
        String response;
        public CreateUserTask(){
            super();
            mServerHandler = ServerHandler.getOrCreateInstance();

        }

        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String password = params[1];
            // Firstly we attempt to create the user on the server. If this is succesful we then
            // set the application account to the newly created one and send the public key to the server
            response = mServerHandler.create_user(username, password);
            if (response != null && response.equals(ServerHandler.GOOD_STATUS)){
                MCMixApplication.setAccount(new_account);
                UserRegistrationActivity.this.StoreAccount();
            }
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

    private boolean validate_username(String name){
         return Pattern.compile("(\\w|\\d)+").matcher(name).matches() && name.length() <= MCMixConstants.USERNAME_LENGTH_IN_CHARS;
    }
}
