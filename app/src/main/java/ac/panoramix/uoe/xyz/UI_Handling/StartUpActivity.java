package ac.panoramix.uoe.xyz.UI_Handling;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;

import ac.panoramix.uoe.xyz.Accounts.Account;
import ac.panoramix.uoe.xyz.R;
import ac.panoramix.uoe.xyz.XYZApplication;
import ac.panoramix.uoe.xyz.XYZConstants;

public class StartUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);

        SharedPreferences prefs = getApplicationContext().getSharedPreferences(XYZConstants.SHARED_PREFS_FILE, MODE_PRIVATE);
        if(prefs.contains(XYZConstants.ACCOUNT_SHARED_PREF)) {
            Gson gson = new Gson();
            String json = prefs.getString(XYZConstants.ACCOUNT_SHARED_PREF, "");
            Account alice = gson.fromJson(json, Account.class);
            Log.d("StartUpAct", "Previous account found with name :" + alice.getUsername() );
            XYZApplication.setAccount(alice);
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getApplicationContext(), UserRegistrationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }


    }
}
