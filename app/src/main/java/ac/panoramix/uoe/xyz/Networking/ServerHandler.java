package ac.panoramix.uoe.xyz.Networking;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by: Chris Campbell
 * on: 04/07/2017
 * for: University Of Edinburgh
 * contact: c.j.campbell@ed.ac.uk
 */

class ServerHandler {

    public static String SERVER_IP_ADDR = "129.215.25.108";
    public static int PORT = 5013;
    public static String protocol = "http";
    public static String LOGIN_URL = "/accounts/login/";
    public static String LOGOUT_URL = "/accounts/logout/";
    public static String C_GET_MESSAGE_URL = "/conversation/get_message";
    public static String C_SEND_MESSAGE_URL = "/conversation/send_message";
    public static URI sURI;
    static {
        try {
            sURI= new URI("http",  null, SERVER_IP_ADDR, PORT, null, null, null);
        } catch (URISyntaxException e) {
            Log.d("ServerHandler", "URI initialisation issue", e);
        }
    }

    private URL mURL;




    public ServerHandler(Context context) {
        mContext = context;
    }

    private HttpURLConnection mConnection;

    private Context mContext;
    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        mContext = context;
    }


    public boolean is_connected_to_network() {
        ConnectivityManager check = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
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


    public boolean establish_connection( String resource){
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


    public void log_cookies(){
        Log.d("ServerHandler"," ---------Logging cookies starts---------- ");
        CookieManager cm = (CookieManager) CookieHandler.getDefault();
        for(HttpCookie cookie :cm.getCookieStore().get(sURI)) {
            Log.d("ServerHandler", "cookie: " + cookie.getName() + " : " + cookie.getValue());
        }
        Log.d("ServerHandler"," ---------Logging cookies ends---------- ");
    }

    public void log_out() {
        CookieManager cm = (CookieManager) CookieHandler.getDefault();
        CookieStore cs = cm.getCookieStore();
        cs.removeAll();
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
    private String get_current_csrftoken(){

        CookieManager cm = (CookieManager) CookieHandler.getDefault();
        for(HttpCookie cookie_ret :cm.getCookieStore().get(sURI)) {
            if(cookie_ret.getName().equals("csrftoken")){
                return cookie_ret.getValue();
            }
        }
        return null;
    }
    public boolean log_in(String username, String password){
        if(is_connected_to_network()){
            try {
                establish_connection(LOGIN_URL);
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
        return true;
    }

    public boolean c_send_message(String message){
        //TODO: handle multiple sends in same round
        if(is_connected_to_network() && is_logged_in()){
            try {
                String csrftoken = get_current_csrftoken();

                //log_cookies();
                //Log.d("ServerHandler","csrf_token = " + csrftoken);

                establish_connection(C_SEND_MESSAGE_URL);
                mConnection.setDoOutput(true);
                mConnection.setRequestMethod("POST");

                String formParameters = "csrfmiddlewaretoken=" + csrftoken
                        + "&message="  + message;
                OutputStream out = mConnection.getOutputStream();
                out.write(formParameters.getBytes("UTF-8"));
                mConnection.connect();
                mConnection.getInputStream();
                mConnection.disconnect();



            } catch (IOException e ){
                Log.d("ServerHandler", "Bad connection to log in ", e);
                return false;
            } finally {
                if (mConnection != null)
                    mConnection.disconnect();
            }
        } else {
            Log.d("ServerHandler", "Not connected to network or not logged in.");
            return false;
        }
        return true;
    }

    public String c_recv_message() {
        //TODO: add establish connection branching in case no connection established
        if (is_connected_to_network() && is_logged_in()) {
            try {
                establish_connection(C_GET_MESSAGE_URL);

                mConnection.connect();
                InputStream in = mConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder response = new StringBuilder();
                String inputstr;
                while ((inputstr = reader.readLine()) != null) {
                    response.append(inputstr + "\n");
                }
                try {
                    JSONObject json = new JSONObject(response.toString());
                    String message = json.getString("returned_message");
                    return message;
                } catch (JSONException e) {
                    Log.d("ServerHandler", "Malformed JSON from server: ", e);
                    return null;
                }

            } catch (IOException e) {
                Log.d("ServerHandler", "Bad connection to site", e);
            } finally {
                if (mConnection != null)
                    mConnection.disconnect();
            }
        } else {
            Log.d("ServerHandler", "Not connected to network or not logged in.");
        }
        return null;
    }
    //submit dial
    //receive dial message


}
