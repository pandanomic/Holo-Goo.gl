package com.pandanomic.hologoogl;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class PostTask extends AsyncTask<String, Void, JSONObject> {

    private Activity parentActivity;
//    private ProgressDialog progressDialog;

    public PostTask(Activity activity) {
        parentActivity = activity;
//        progressDialog = new ProgressDialog(parentActivity);
    }

    @Override
    protected void onPreExecute() {
//        this.progressDialog.setMessage("Retrieving Data");
//        this.progressDialog.show();
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        String sharedURL = params[0];

        JSONObject results;

        /**
         * Check network connection
         * TODO: timeout as well
         */
        if (!networkAvailable()) {
            Toast.makeText(parentActivity, "Not connected to the internet", Toast.LENGTH_LONG).show();
            return null;
        }

        Log.d("googl", "Fetching data");
        try {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
            HttpConnectionParams.setSoTimeout(httpParams, 5000);

            DefaultHttpClient client = new DefaultHttpClient(httpParams);
            HttpPost post = new HttpPost("https://www.googleapis.com/urlshortener/v1/url");
            post.setEntity(new StringEntity("{\"longUrl\": \"" + sharedURL + "\"}"));
            post.setHeader("Content-Type", "application/json");

            HttpResponse response = client.execute(post);

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            StringBuilder builder = new StringBuilder();
            for (String line; (line = reader.readLine()) != null;) {
                builder.append(line).append("\n");
            }

            results = new JSONObject(new JSONTokener(builder.toString()));
        }
        catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "Error: ";
            if (e instanceof UnsupportedEncodingException) {
                errorMessage += "Encoding exception";
            }
            else if (e instanceof ClientProtocolException) {
                errorMessage += "POST exception";
            }
            else if (e instanceof IOException) {
                errorMessage += "IO Exception in parsing response";
            }
            else {
                errorMessage += "JSON parsing exception";
            }

            Log.e("googl:retrieveURLTask", errorMessage);
            return null;
        }
        return results;
    }

    /**
     * Post-execution stuff
     * @param result JSONObject result received from Goo.gl
     */
    protected void onPostExecute(JSONObject result) {
//        if (progressDialog.isShowing()) {
//            progressDialog.dismiss();
//        }
    }

    private boolean networkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) parentActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}