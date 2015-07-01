package com.example.padzonline.classbuzz;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Activity for the class response session, which displays the question, an image (if included with
 * the question), a radio group containing the various answer options and a submit button, which
 * will post the user's response to the server.
 *
 * The unique content for the question has been passed with the Intent from the previous
 * MainActivity as extras. This activity also contains two nested AsyncTask subclasses; one to load
 * the questions image from the provided URL, and the other to send the user's response to the web
 * server via a POST request.
 *
 * Created by Patrick Murphy - paddy@padzonline.com
 */

public class QuestionActivity extends Activity implements View.OnClickListener {

    String  studentNo = "", // User's student number
            password = "",  // User's password
            question ="";   // Question ID
    TextView qTextView,     // Question text
             qTitleTextView;// Question title
    ImageView iv;
    Button submit;
    RadioButton[] answers = new RadioButton[4];
    RadioGroup answersGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question); // Set xml layout

        // Set layout references
        qTitleTextView = (TextView)findViewById(R.id.textView4);
        qTextView = (TextView)findViewById(R.id.textView3);
        iv = (ImageView)findViewById(R.id.imageView2);
        submit = (Button)findViewById(R.id.submitbutton);

        answersGroup = (RadioGroup)findViewById(R.id.radioGroup);
        answers[0]= (RadioButton)findViewById(R.id.a1);
        answers[1]= (RadioButton)findViewById(R.id.a2);
        answers[2]= (RadioButton)findViewById(R.id.a3);
        answers[3]= (RadioButton)findViewById(R.id.a4);

        // Create local reference to the received intent from the previous activity instead of
        // calling getIntent() repeatedly.
        Intent receivedIntent = getIntent();

        // Set radio button's visibility to GONE if it's string is blank
        if( !receivedIntent.getStringExtra("ans1").equals("") )
            answers[0].setText(receivedIntent.getStringExtra("ans1"));
            else answers[0].setVisibility(View.GONE);
        if( !receivedIntent.getStringExtra("ans2").equals("") )
            answers[1].setText(receivedIntent.getStringExtra("ans2"));
            else answers[1].setVisibility(View.GONE);
        if( !receivedIntent.getStringExtra("ans3").equals("") )
            answers[2].setText(receivedIntent.getStringExtra("ans3"));
            else answers[2].setVisibility(View.GONE);
        if( !receivedIntent.getStringExtra("ans4").equals("") )
            answers[3].setText(receivedIntent.getStringExtra("ans4"));
            else answers[3].setVisibility(View.GONE);

        submit.setOnClickListener(this); // Listen for the click button

        // Get student/question info from the previous activity's Intent
        studentNo = receivedIntent.getStringExtra("studentno");
        password = receivedIntent.getStringExtra("password");
        question = receivedIntent.getStringExtra("question");
        qTitleTextView.setText(receivedIntent.getStringExtra("questiontitle"));
        qTextView.setText(receivedIntent.getStringExtra("questiontext"));

        //Get the image from the intent's image URL (if there is one) and put it in the ImageView
        String imageURL = receivedIntent.getStringExtra("image");
        if(!imageURL.equals("")) new DownloadImageTask().execute(imageURL);
    }

    @Override
    public void onClick(View v) {
        //onClickListener only set to the SUBMIT button
        if(Interwebs.isConnectedToInternet(getApplicationContext())){ // If internet access...
            //Submit response
            if(answers[0].isChecked() || answers[1].isChecked() || answers[2].isChecked() || answers[3].isChecked()){
                new POSTTask().execute("http://padzonline.com/classbuzz/receiveresponse.php");
            }
            else{ //If no RadioButton has been checked...
                Toast toast = Toast.makeText(getBaseContext(), "Please select an answer!", Toast.LENGTH_SHORT);
                toast.show();
            }
              }
        else{
            Toast toast = Toast.makeText(getBaseContext(), "No internet connection", Toast.LENGTH_SHORT);
            toast.show();
            finish(); // Finish this activity to return to the Menu
        }
    }

    /* AsyncTask which downloads and sets the question's image in the ImageView */
    private class DownloadImageTask extends AsyncTask<String, Integer, Bitmap>{
        Bitmap bitmap = null;

        @Override
        protected Bitmap doInBackground(String... params) {
            try{
                InputStream in = new URL(params[0]).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            }catch (Exception e){
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result){
            iv.setImageBitmap(bitmap);
        }
    }

    /* AsyncTask which POSTs the user's selected answer to the server */
    private class POSTTask extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... urls){
            URL url;
            HttpURLConnection connection = null;
            StringBuilder response = new StringBuilder();
            try {
                //Create connection and set parameters
                url = new URL("http://padzonline.com/classbuzz/receiveresponse.php");
                connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");

                connection.setConnectTimeout(5000); //set timeout to 5 seconds
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                //Create JSONObject here
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("studentno", studentNo);
                jsonParam.put("password", password);
                jsonParam.put("question", question);
                RadioButton rb = (RadioButton)findViewById(answersGroup.getCheckedRadioButtonId());
                jsonParam.put("response", rb.getText());

                //Send request
                DataOutputStream wr = new DataOutputStream (connection.getOutputStream ());
                wr.writeBytes(jsonParam.toString());
                wr.flush();

                wr.close();

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;

                while((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();

            }catch (java.net.SocketTimeoutException e) {
                Toast toast = Toast.makeText(getBaseContext(), "Connection timed out!", Toast.LENGTH_SHORT);
                toast.show();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if(connection != null) {
                    connection.disconnect();
                }
            }

            return response.toString();
        }

        @Override
        protected void onPostExecute(String result){
            //result is not currently being used
            finish();
        }
    }

}
