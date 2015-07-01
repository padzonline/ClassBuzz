package com.example.padzonline.classbuzz;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * Login Screen; here the user enters their student number and password, and presses the submit button.
 * If the authentication is successful, a brief animation will play to transition to the MainActivity.
 * The user's credentials are passed to the next activity as intent extras.
 *
 * Created by Patrick Murphy - paddy@padzonline.com
 */
public class LoginActivity extends Activity implements View.OnClickListener {

    Button submit;
    EditText studentNoField, passwordField;
    TextView tv1, tv2; // "Student number:" , "Password:"
    ImageView bubbles, logo;

    // Animations which will execute on a successful login
    ScaleAnimation zoom;
    AlphaAnimation alpha;
    AnimationSet as;
    AlphaAnimation fadeAll;

    // Intent for next activity
    Intent intent= new Intent("com.example.padzonline.classbuzz.MAINACTIVITY");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Assigning objects to view components
        studentNoField = (EditText)findViewById(R.id.editText);
        passwordField = (EditText)findViewById(R.id.editText2);
        submit = (Button)findViewById(R.id.button4);
        logo = (ImageView)findViewById(R.id.imageView3);
        bubbles = (ImageView)findViewById(R.id.bubblesView);
        tv1 = (TextView)findViewById(R.id.textView);
        tv2 = (TextView)findViewById(R.id.textView2);

        submit.setOnClickListener(this);

        //Get display width/height for scaling
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        //Get width for scale animation pivoting
        float screenWidth = metrics.widthPixels;

        //Set up login animation properties
        zoom = new ScaleAnimation(1f, 3f, 1f, 3f, screenWidth*0.5f, bubbles.getLayoutParams().height*0.5f);
        alpha = new AlphaAnimation(1f, 0f);
        alpha.setFillAfter(true);
        zoom.setFillAfter(true);

        //Used to perform simultaneous alpha and zoom animations on one ImageView
        as = new AnimationSet(true);
        as.setFillEnabled(true);
        as.setDuration(1000);
        as.setFillAfter(true);
        as.addAnimation(alpha);
        as.addAnimation(zoom);

        fadeAll = new AlphaAnimation(1f, 0f);
        fadeAll.setDuration(500);
        fadeAll.setStartOffset(500);
        fadeAll.setFillAfter(true);

        //Set AnimationListener on the final fade out animation to initiate next activity
        fadeAll.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                studentNoField.setVisibility(View.INVISIBLE);
                overridePendingTransition(0, 0); //Disable activity transition
                startActivity(intent); //Start MainActivity
                finish(); //remove login activity from the back stack
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
    }

    // Initiates asynchronous login task when the Submit button is pressed
    @Override
    public void onClick(View v) {
        //Perform login if connected to the internet, otherwise give Toast alert
        if(Interwebs.isConnectedToInternet(getApplicationContext())){
            new ValidateUserTask().execute("http://padzonline.com/classbuzz/login.php?studentno="
                    +studentNoField.getText().toString()
                    +"&password="+passwordField.getText().toString());
            }
        else{ Toast toast = Toast.makeText(getBaseContext(), "Could not connect to internet", Toast.LENGTH_SHORT);
        toast.show();}
    }

    /* Validates user credentials and starts the next Activity if the login is successful
    */
    private class ValidateUserTask extends Interwebs.GETTask{

        //Parses JSON response string and either logs in or displays "incorrect username/password"
        @Override
        protected void onPostExecute(String output) {
            JSONObject jObject;
            String username ="";
            try {
                jObject = new JSONObject(output);
                username = jObject.getString("name");
            }
                catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }
            String result = " ";
            try {
                result = jObject.getString("status_message");
            }
                catch (JSONException e) {
                    e.printStackTrace();
            }
                finally{
                if(result.equals("success")){
                    //Put the following info into the intent to pass to next activity
                    intent.putExtra("username", username);
                    intent.putExtra("studentno", studentNoField.getText().toString());
                    intent.putExtra("password", passwordField.getText().toString());

                    //Start Login animations *NOTE fadeAll has an AnimationListener which will start
                    //the next activity when it is finished.
                    bubbles.startAnimation(as);
                    logo.startAnimation(fadeAll);
                    studentNoField.startAnimation(fadeAll);
                    passwordField.startAnimation(fadeAll);
                    tv1.startAnimation(fadeAll);
                    tv2.startAnimation(fadeAll);
                    submit.startAnimation(fadeAll);
                }
                else{
                    Toast toast = Toast.makeText(getBaseContext(), "Incorrect username/password", Toast.LENGTH_SHORT);
                    toast.show();}
            }
        }
    }
}