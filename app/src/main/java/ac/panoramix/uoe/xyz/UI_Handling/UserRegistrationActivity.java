package ac.panoramix.uoe.xyz.UI_Handling;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import ac.panoramix.uoe.xyz.Accounts.Account;
import ac.panoramix.uoe.xyz.Networking.ServerHandler;
import ac.panoramix.uoe.xyz.R;
import ac.panoramix.uoe.xyz.XYZApplication;
import ac.panoramix.uoe.xyz.XYZConstants;

public class UserRegistrationActivity extends AppCompatActivity {

    private Button user_create_button;
    private EditText username_input;
    private EditText password_input;
    private EditText password_repeat_input;

    public Account getNew_account() {
        return new_account;
    }

    public void setNew_account(Account new_account) {
        this.new_account = new_account;
    }

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

                } else if (!password_input.getText().toString().equals(password_repeat_input.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Passwords do not match.", Toast.LENGTH_LONG).show();
                } else {
                    new_account = new Account(username_input.getText().toString());
                    new CreateUserTask().execute(username_input.getText().toString(), password_input.getText().toString());
                }

            }
        });
    }

    private void StoreAccount(){
        try {
            FileOutputStream fos = openFileOutput(XYZConstants.ACCOUNT_STORAGE_FILE, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(XYZApplication.getAccount());
            oos.close();
            fos.close();
        }catch (FileNotFoundException e){
            Log.d("UserRegAct", "Cannot open the file", e);
        } catch (IOException e) {
            Log.d("UserRegAct", "Cannot write object to file", e);
        }
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
                XYZApplication.setAccount(new_account);
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
}
