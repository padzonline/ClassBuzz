package com.example.padzonline.classbuzz;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;

/**
 * This activity allows the user to monitor the live results of an example class response session,
 * 'ST900-01a'.
 * The question/results information is contained within a LinearLayout, which is initially invisible
 * and is made visible once this information has been retrieved from the server.
 *
 * Created by Patrick Murphy - paddy@padzonline.com
 */

public class ExampleResultsActivity extends Activity implements View.OnClickListener{
    LinearLayout ll;
    ProgressBar pb;
    TextView qTitle, qText, ans1, ans2, ans3, ans4;
    ImageView iv;
    Button OK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example_results);

        // Pointing objects to views in layout
        ll= (LinearLayout)findViewById(R.id.exampleLayout);
        pb= (ProgressBar)findViewById(R.id.progressBar);

        ans1= (TextView)findViewById(R.id.text_a);
        ans2= (TextView)findViewById(R.id.text_b);
        ans3= (TextView)findViewById(R.id.text_c);
        ans4= (TextView)findViewById(R.id.text_d);

        iv = (ImageView)findViewById(R.id.resultQImage);
        qTitle= (TextView)findViewById(R.id.resultQTitle);
        qText= (TextView)findViewById(R.id.resultQText);

        OK = (Button)findViewById(R.id.button5);

        OK.setOnClickListener(this);

        //Downloads question/results info and displays them in the above TextViews/ImageViews
        new getInfoTask().execute("http://padzonline.com/classbuzz/l_getcontent.php?question=ST900-01a");
    }

    @Override
    public void onClick(View v) {
        finish();
    }

/* HTTP GET task to retrieve information to be displayed on page*/
    private class getInfoTask extends Interwebs.GETTask{
        @Override
        protected void onPostExecute(String output) {
            JSONObject jObject;
            try {
                jObject = new JSONObject(output);

                qTitle.setText(jObject.getString("titletext"));
                qText.setText(jObject.getString("questiontext"));
                ans1.setText(jObject.getString("a"));
                ans2.setText(jObject.getString("b"));
                ans3.setText(jObject.getString("c"));
                ans4.setText(jObject.getString("d"));

                new DownloadImageTask().execute(jObject.getString("image"));

                pb.setVisibility(View.GONE); //Hide progress bar
                ll.setVisibility(View.VISIBLE); //Show content
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /* AsyncTask which downloads and sets the question's image */
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

}