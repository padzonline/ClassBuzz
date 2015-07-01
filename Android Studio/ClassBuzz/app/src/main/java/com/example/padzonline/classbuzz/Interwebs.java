package com.example.padzonline.classbuzz;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Class for customised network tools. Contains a static function to check for network connectivity,
 * (isConnectedToInternet) which is used by all activities before an attempt to send/receive data,
 * and an abstract class which contains generic implementation for executing a HTTP GET request
 * asynchronously (without inferring on the UI thread).
 *
 * Created by Patrick Murphy - paddy@padzonline.com
 */

public class Interwebs {

    public static boolean isConnectedToInternet(Context context){
        ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null){
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED){
                        return true;
                    }

        }
        return false;
    }


    /* AsyncTask with generic implementation for HTTP GET requests. Accepts the http url string as
     * an argument and contains an abstract method onPostExecute which specifies what must be done
     * with the resultant received info.
     */
    public static abstract class GETTask extends AsyncTask<String, Void, String> {
        HttpURLConnection httpConnection;

        @Override
        protected String doInBackground(String... urls) {
            String output = null;
            for (String url : urls){ output = getOutputFromUrl(url);}
            return output;
        }

        // Gets the HTTP response in raw string form
        private String getOutputFromUrl(String url) {
            StringBuffer output = new StringBuffer("");
            try {
                InputStream stream = getHttpConnection(url);
                BufferedReader buffer = new BufferedReader(new InputStreamReader(stream));
                String s = "";
                while ((s = buffer.readLine()) != null) output.append(s);
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally{
                if(httpConnection!=null)
                 httpConnection.disconnect();
            }

            return output.toString();
        }

        // Makes HttpURLConnection and returns InputStream
        private InputStream getHttpConnection(String urlString) throws IOException {
            InputStream stream = null;
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();

            try {
                httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();

                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream(); }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }

            return stream;
        }

        @Override
        protected abstract void onPostExecute(String output);
    }
}
