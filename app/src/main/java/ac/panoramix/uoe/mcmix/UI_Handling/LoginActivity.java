package ac.panoramix.uoe.mcmix.UI_Handling;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ac.panoramix.uoe.mcmix.Networking.ServerHandler;
import ac.panoramix.uoe.mcmix.Networking.UpdatePublicKeyTask;
import ac.panoramix.uoe.mcmix.Networking.MCMixNetworkService;
import ac.panoramix.uoe.mcmix.MCMixApplication;
import ac.panoramix.uoe.mcmix.R;

public class LoginActivity extends AppCompatActivity {
    TextView mUsername_text;
    EditText mPassword_text;
    Button mLogin_button;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUsername_text = (TextView) findViewById(R.id.login_username);
        mUsername_text.setText(MCMixApplication.getAccount().getUsername());


        mPassword_text = (EditText) findViewById(R.id.login_password);
        //TODO: remove debugging password
        mPassword_text.setText("horse_battery");

        mLogin_button = (Button) findViewById(R.id.login_button);
        mLogin_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LoginTask().execute(mUsername_text.getText().toString(), mPassword_text.getText().toString());
            }
        });
    }

    /**
     * Created by: Chris Campbell
     * on: 06/07/2017
     * for: University Of Edinburgh
     * contact: c.j.campbell@ed.ac.uk
     */

     private class LoginTask extends AsyncTask<String, Integer, String> {
        ServerHandler mServerHandler;
        public LoginTask(){
            super();
            mServerHandler = ServerHandler.getOrCreateInstance();

        }

        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String password = params[1];
            boolean success = mServerHandler.log_in(username, password);
            return "Finished logging in.";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mServerHandler.log_cookies();
            Log.d("LoginTask", Boolean.toString(mServerHandler.is_logged_in()));
            if(mServerHandler.is_logged_in()){
                Intent serviceIntent = new Intent(getApplicationContext(), MCMixNetworkService.class);
                startService(serviceIntent);
                //TODO: fix which activity is launched by this
                Intent intent = new Intent(getApplicationContext(), BuddyListActivity.class);
                // These flags make it so that pressing Back will not return the user to the login screen
                new UpdatePublicKeyTask().execute();

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "Failed to log in - check password and network", Toast.LENGTH_LONG).show();
            }
        }
    }
}
