package ac.panoramix.uoe.xyz.Networking;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.libsodium.jni.keys.PublicKey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import ac.panoramix.uoe.xyz.Utility;
import ac.panoramix.uoe.xyz.XYZApplication;

/**
 * Created by: Chris Campbell
 * on: 04/07/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

public class ServerHandler {


    public static final String SERVER_IP_ADDR = "129.215.25.108";
    public static final int PORT = 5013;
    public static final String GOOD_STATUS = "good";
    public static final String protocol = "http";
    public static final String CREATE_USER_URL = "/dial/create_user/";
    public static final String LOGIN_URL = "/accounts/login/";
    public static final String LOGOUT_URL = "/accounts/logout/";
    public static final String C_GET_MESSAGE_URL = "/conversation/get_message";
    public static final String C_SEND_MESSAGE_URL = "/conversation/send_message";
    public static final String C_GET_ROUND_NUMBER_URL = "/conversation/get_round_number";
    public static final String D_UPDATE_PUBLIC_KEY = "/dial/update_public_key";
    public static final String D_GET_PUBLIC_KEY= "/dial/get_public_key";

    public static URI sURI;
    static {
        try {
            sURI= new URI(protocol ,  null, SERVER_IP_ADDR, PORT, null, null, null);
        } catch (URISyntaxException e) {
            Log.d("ServerHandler", "URI initialisation issue", e);
        }
    }

    private static ServerHandler sServerHandler;
    private ServerHandler(){}
    public static ServerHandler getOrCreateInstance(){
        if (sServerHandler == null){
            sServerHandler = new ServerHandler();
        }
        return sServerHandler;
    }


    private URL mURL;
    private HttpURLConnection mConnection;
    private long c_round_number = 0;

    private boolean is_connected_to_network() {
        ConnectivityManager check = (ConnectivityManager) XYZApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks = check.getAllNetworks();
        NetworkInfo netInfo;
        for(Network mNetwork : networks){
            netInfo = check.getNetworkInfo(mNetwork);
            if(netInfo.getState().equals(NetworkInfo.State.CONNECTED)){
                return true;
            }
        }
        return false;
    }

    private String get_current_csrftoken(){
        CookieManager cm = (CookieManager) CookieHandler.getDefault();
        for(HttpCookie cookie_ret :cm.getCookieStore().get(sURI)) {
            if(cookie_ret.getName().equals("csrftoken")){
                return cookie_ret.getValue();
            }
        }
        return null;
    }

    public void log_cookies(){
        Log.d("ServerHandler"," ---------Logging cookies starts---------- ");
        CookieManager cm = (CookieManager) CookieHandler.getDefault();
        for(HttpCookie cookie :cm.getCookieStore().get(sURI)) {
            Log.d("ServerHandler", "cookie: " + cookie.getName() + " : " + cookie.getValue());
        }
        Log.d("ServerHandler"," ---------Logging cookies ends---------- ");
    }

    public synchronized boolean establish_connection( String resource){
        Log.d("ServerHandler", "Establishing connection with resource: " + resource);
        if(is_connected_to_network()){
            try {
                mURL= new URL("http", SERVER_IP_ADDR, PORT, resource);
                mConnection = (HttpURLConnection) mURL.openConnection();
                return true;
            } catch (MalformedURLException e) {
                Log.d("ServerHandler", "Malformed URL", e);
                return false;
            } catch (IOException e){
                Log.d("ServerHandler", "Bad open connection", e);
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean is_logged_in() {
        CookieManager cm = (CookieManager) CookieHandler.getDefault();
        for(HttpCookie cookie :cm.getCookieStore().get(sURI)) {
            if(cookie.getName().equals("sessionid") && !cookie.hasExpired()) {
                return true;
            }
        }
        return false;

    }

    public synchronized boolean log_in(String username, String password){
        if(is_connected_to_network() && establish_connection(LOGIN_URL)){
            try {
                mConnection.connect();
                mConnection.getInputStream();
                mConnection.disconnect();
                String csrftoken = get_current_csrftoken();

                //log_cookies();
                //Log.d("ServerHandler","csrf_token = " + csrftoken);

                establish_connection(LOGIN_URL);
                mConnection.setDoOutput(true);
                mConnection.setRequestMethod("POST");

                String formParameters = "csrfmiddlewaretoken=" + csrftoken
                        + "&username=" +username
                        + "&password=" + password;
                OutputStream out = mConnection.getOutputStream();
                out.write(formParameters.getBytes("UTF-8"));
                mConnection.connect();
                mConnection.getInputStream();
                mConnection.disconnect();
                return is_logged_in();
            } catch (IOException e ){
                Log.d("ServerHandler", "Bad connection to log in ", e);
                return false;
            } finally {
                if (mConnection != null)
                    mConnection.disconnect();
            }
        } else {
            Log.d("ServerHandler", "Not connected to network");
            return false;
        }
    }

    private synchronized JSONObject send_post_for_response(String resource, Map<String,String> parameters) {
        assert is_logged_in();
        if(is_connected_to_network() && establish_connection(resource)){
            try{
                mConnection.setDoInput(true);
                mConnection.setDoOutput(true);
                mConnection.setRequestMethod("POST");

                // create the form parameters and send to the server
                String formParameters = "csrfmiddlewaretoken=" + get_current_csrftoken();
                for(Map.Entry<String,String> entry : parameters.entrySet()){
                    formParameters += "&" + entry.getKey() + "=" + entry.getValue();
                }
                OutputStream out = mConnection.getOutputStream();
                out.write(formParameters.getBytes("UTF-8"));
                mConnection.connect();

                //retrieve the output JSON object
                InputStream in = mConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder response = new StringBuilder();
                String response_string;
                while((response_string = reader.readLine()) != null){
                    response.append(response_string + "\n");
                }
                JSONObject json_response = new JSONObject(response.toString());
                return json_response;

            } catch (IOException e) {
                Log.d("ServHandler", "Bad connection to resource: " + resource, e);
                return null;
            } catch (JSONException e) {
                Log.d("ServHandler", "Malformed JSON from resource: " + resource, e);
                return null;
            } finally {
                if(mConnection != null)
                    mConnection.disconnect();
            }
        } else {
            Log.d("ServHandler", "Can't connect to resource: " + resource);
            return null;
        }
    }

    public synchronized boolean c_round_finished(){
        JSONObject response = send_post_for_response(C_GET_ROUND_NUMBER_URL, new HashMap<String, String>());
        if(response == null) return false;
        try{
            String status = response.getString("status");
            switch(status){
                case GOOD_STATUS:
                    long new_round_number = response.getLong("round_number");
                    if(new_round_number != c_round_number) {
                        c_round_number = new_round_number;
                        return true;
                    } else {
                        return false;
                    }
                default:
                    Log.d("ServHandler", "On request for receiving c_message server returned status:" + status);
                    return false;
            }
        } catch (JSONException e) {
            Log.d("ServHandler", "json_response does not have a key", e);
            return false;
        }
    }
    public long getC_round_number(){
        return c_round_number;
    }
    public String c_recv_message(){
        JSONObject response = send_post_for_response(C_GET_MESSAGE_URL, new HashMap<String, String>());
        if (response == null) return null;
        try{
            String status = response.getString("status");
            switch(status){
                case GOOD_STATUS:
                    String message = response.getString("returned_message");
                    return message;
                default:
                    Log.d("ServHandler", "On request for receiving c_message server returned status:" + status);
                    return null;
            }
        } catch (JSONException e) {
            Log.d("ServHandler", "json_response does not have a key", e);
            return null;
        }
    }

    public boolean c_send_message(String message){
        Map<String,String> parameters = new HashMap<String,String>();
        parameters.put("message", message);
        JSONObject response = send_post_for_response(C_SEND_MESSAGE_URL, parameters);
        if(response == null) return false;
        try{
            String status = response.getString("status");
            switch(status){
                case GOOD_STATUS:
                    return true;
                default:
                    Log.d("ServHandler", "On request for receiving c_message server returned status:" + status);
                    return false;
            }
        } catch (JSONException e) {
            Log.d("ServHandler", "json_response does not have a key", e);
            return false;
        }

    }


    public static final String USERNAME_ALREADY_EXISTS = "user_already_exists";
    public static final String PASSWORD_DOES_NOT_CONFORM = "password_does_not_conform";
    public String create_user(String username, String password){
        Map<String,String> parameters = new HashMap<String,String>();
        parameters.put("username", username);
        parameters.put("password", password);
        JSONObject response = send_post_for_response(CREATE_USER_URL, parameters);
        if(response == null) return null;
        try{
            String status = response.getString("status");
            return status;
        } catch (JSONException e) {
            Log.d("ServHandler", "json_response does not have a key", e);
            return null;
        }
    }

    public PublicKey get_public_key_for_username(String username){
        Map<String,String> parameters = new HashMap<String,String>();
        parameters.put("username", username);
        JSONObject response = send_post_for_response(D_GET_PUBLIC_KEY, parameters);
        if(response == null) return null;
        try{
            switch(response.getString("status")){
                case GOOD_STATUS:
                    byte[] bytes_of_key = Utility.bytes_from_string(response.getString("public_key"));
                    return new PublicKey(bytes_of_key);
                default:
                    return null;
            }
        } catch (JSONException e) {
            Log.d("ServHandler", "json_response does not have a key", e);
            return null;
        }
    }


}
