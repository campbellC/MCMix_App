package ac.panoramix.uoe.xyz.UI_Handling;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

import ac.panoramix.uoe.xyz.Accounts.Account;
import ac.panoramix.uoe.xyz.R;
import ac.panoramix.uoe.xyz.Utility;
import ac.panoramix.uoe.xyz.XYZApplication;
import ac.panoramix.uoe.xyz.XYZConstants;

public class StartUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);

        File f = getBaseContext().getFileStreamPath(XYZConstants.ACCOUNT_STORAGE_FILE);
        if(f.exists()) {
            try {
                FileInputStream fis = openFileInput(XYZConstants.ACCOUNT_STORAGE_FILE);
                ObjectInputStream ois = new ObjectInputStream(fis);
                Account alice = (Account) ois.readObject();
                XYZApplication.setAccount(alice);
                ois.close();
                fis.close();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return;
            } catch (FileNotFoundException e){
                Log.d("StartUpAct", "Account File can't open", e);
            } catch (IOException e){
                Log.d("StartUpAct", "Account File can't be read", e);
            } catch (ClassNotFoundException e) {
                Log.d("StartUpAct", "Account File corrupt", e);
            }
            Toast.makeText(this, "Account storage damaged, you must make a new account.", Toast.LENGTH_SHORT).show();
            launchUserRegistration();
        } else {
            launchUserRegistration();
        }


    }

    private void launchUserRegistration() {
        Intent intent = new Intent(getApplicationContext(), UserRegistrationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}