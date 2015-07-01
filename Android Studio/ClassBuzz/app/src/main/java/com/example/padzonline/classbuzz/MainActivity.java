package com.example.padzonline.classbuzz;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * This activity acts as the menu interface, which comprises the main menu and the Active Session
 * selection menu.
 *
 * Each of these menus is similar to a fragment, however considering their simplicity and the fact
 * that there are only two menus, they are not encapsulated in separate objects. Instead, the
 * relevant views for each menu are hidden/displayed as needed.
 *
 * Fade-in/out animations are used for transitioning between menus, and there is a simple background
 * animation of semi-transparent moving 'bubbles' playing throughout. This is done by assigning a
 * looping TranslateAnimation to an array of ImageViews.
 *
 * The two networking functions of this class are: 1) to retrieve the active class response sessions
 * for the user that is logged in (which will be displayed in the Active Sessions menu), and 2) to
 * retrieve the content for a selected question (i.e. the question title, text, image URL and
 * answers), which will be passed to the next QuestionActivity to be displayed. Each of these
 * networking functions are implemented in their own AsyncTask subclasses, so that they can be
 * performed concurrently with the UI thread.
 *
 * Created by Patrick Murphy - paddy@padzonline.com
 */

public class MainActivity extends Activity implements View.OnClickListener {

    Button theButton, infoButton, resultsButton, logoutButton; //Main menu buttons
    Button[] sessionButtons = new Button[10];   //Active Sessions buttons
    ImageView topBar, topLogo; // Logo and bar at top of the menu screen
    ImageView[] bubbles = new ImageView[10]; // Animated background 'bubbles'
    TextView    nameTextView, //User's first name shown in top right hand corner
                titleText, //Menu title
                infoBox; // Popup TextView displayed when the 'Info' button is pressed
    LinearLayout menuLayout; // Main menu
    ScrollView sv;

    float screenWidth =0;
    float screenHeight =0;

    String nameOfUser="", studentNo="", password=""; // User info
    String selectedQ;
    int currentMenu=0; //0==Main Menu; 1==Active Sessions menu

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get display width/height for scaling
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        //Assigning view components
        nameTextView = (TextView)findViewById(R.id.usernameb);
        titleText = (TextView)findViewById(R.id.menu_title);
        infoBox = (TextView)findViewById(R.id.infoBox);
        theButton = (Button)findViewById(R.id.button);
        infoButton = (Button)findViewById(R.id.button2);
        resultsButton = (Button)findViewById(R.id.button3);
        logoutButton = (Button)findViewById(R.id.button4);
        topBar = (ImageView)findViewById(R.id.imageView);
        topLogo = (ImageView)findViewById(R.id.menuLogo);

        sv = (ScrollView)findViewById(R.id.sessionsScrollView);
        menuLayout = (LinearLayout)findViewById(R.id.menulayout);

        bubbles[0] = (ImageView)findViewById(R.id.iview);
        bubbles[1] = (ImageView)findViewById(R.id.iview2);
        bubbles[2] = (ImageView)findViewById(R.id.iview3);
        bubbles[3] = (ImageView)findViewById(R.id.iview4);
        bubbles[4] = (ImageView)findViewById(R.id.iview5);
        bubbles[5] = (ImageView)findViewById(R.id.iview6);
        bubbles[6] = (ImageView)findViewById(R.id.iview7);
        bubbles[7] = (ImageView)findViewById(R.id.iview8);
        bubbles[8] = (ImageView)findViewById(R.id.iview9);
        bubbles[9] = (ImageView)findViewById(R.id.iview10);


        //Link to session buttons
        sessionButtons[0] = (Button)findViewById(R.id.sessionbutton1);
        sessionButtons[1] = (Button)findViewById(R.id.sessionbutton2);
        sessionButtons[2] = (Button)findViewById(R.id.sessionbutton3);
        sessionButtons[3] = (Button)findViewById(R.id.sessionbutton4);
        sessionButtons[4] = (Button)findViewById(R.id.sessionbutton5);
        sessionButtons[5] = (Button)findViewById(R.id.sessionbutton6);
        sessionButtons[6] = (Button)findViewById(R.id.sessionbutton7);
        sessionButtons[7] = (Button)findViewById(R.id.sessionbutton8);
        sessionButtons[8] = (Button)findViewById(R.id.sessionbutton9);
        sessionButtons[9] = (Button)findViewById(R.id.sessionbutton10);

        //Get user info from the Intent
        nameOfUser = getIntent().getStringExtra("username");
        studentNo = getIntent().getStringExtra("studentno");
        password = getIntent().getStringExtra("password");

        //display user's first name at top of screen
        nameTextView.setText(nameOfUser);

        //Set scaled parameters of top bar/logo
        ViewGroup.LayoutParams topBarParams = new RelativeLayout.LayoutParams((int)screenWidth, (int)(screenWidth*0.2));
        topBar.setLayoutParams(topBarParams);
        ViewGroup.LayoutParams topLogoParams = new RelativeLayout.LayoutParams((int)(screenWidth*0.3), (int)(screenWidth*0.3));
        topLogo.setLayoutParams(topLogoParams);

        //Set scaled parameters of textview displaying user's first name
        ViewGroup.LayoutParams nameParams = nameTextView.getLayoutParams();//new RelativeLayout.LayoutParams((int)(screenWidth*0.5), (int)(screenWidth*0.2));
        nameParams.width=(int)(screenWidth*0.5);
        nameParams.height=(int)(screenWidth*0.2);
        nameTextView.setLayoutParams(nameParams);

        //Set scaled button sizes (height = 0.08 of the screen height)
        theButton.setHeight((int) (screenHeight*0.08));
        infoButton.setHeight((int) (screenHeight * 0.08));
        resultsButton.setHeight((int) (screenHeight * 0.08));
        logoutButton.setHeight((int) (screenHeight*0.08));
        for(int i=0; i<10; i++){ sessionButtons[i].setHeight((int) (screenHeight*0.08)); }

        //Setting the OnClickListener to the various view components
        theButton.setOnClickListener(this);
        infoButton.setOnClickListener(this);
        resultsButton.setOnClickListener(this);
        logoutButton.setOnClickListener(this);
        for(int i=0; i<10; i++)sessionButtons[i].setOnClickListener(this);
        infoBox.setOnClickListener(this);

        //Initialize main menu
        mainMenuInit();

        //Assigning attributes and animations to each 'bubble'
        startBubble(0, 20, 40, 0, 28000);
        startBubble(1, 25, -10, 10000, 26000);
        startBubble(2, 30, 0, 700, 25000);
        startBubble(3, 25, 30, 17000, 20000);
        startBubble(4, 40, 60, 4000, 35000);
        startBubble(5, 45, 80, 15000, 25000);
        startBubble(6, 30, 100, 1400, 29000);
        startBubble(7, 55, 120, 200, 21000);
    }

    //Assigns size/positional parameters and a new translate animation to each bubble ImageView.
    public void startBubble(int whichBubble, int bubbleSize, int bubbleX, int bubbleStartOffset, int duration){
        if(whichBubble<10){
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)(bubbleSize*screenWidth/100), (int)(bubbleSize*screenWidth/100));
            bubbles[whichBubble].setLayoutParams(params);

            TranslateAnimation trans = new TranslateAnimation((bubbleX-bubbleSize/2)*screenWidth/100, (screenWidth-bubbleSize*screenWidth/100)/2, -(bubbleSize*screenWidth/100), screenHeight);
            trans.setDuration(duration);
            trans.setStartOffset(bubbleStartOffset);
            trans.setRepeatCount(-1); // Repeat forever
            bubbles[whichBubble].startAnimation(trans);
        }
    }

    //Options Menu not used in this version
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //Options Menu not used in this version
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Switches to the main menu layout and performs fade-in animation
    protected void mainMenuInit(){
        sv.setVisibility(View.INVISIBLE);
        menuLayout.setVisibility(View.VISIBLE);
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setFillAfter(true);
        anim.setDuration(600);
        menuLayout.setAnimation(anim);
        currentMenu=0;
        titleText.setText("MAIN MENU");
    }

   //If resuming to the Active Sessions Menu, call goToActiveSessionMenu() to check for newly added sessions
   public void onResume(){
       super.onResume();
       if(currentMenu==1)goToActiveSessionsMenu();
   }

    /*Retrieves a list of the user's active sessions and displays them in the Active Sessions menu.
    */
   protected void goToActiveSessionsMenu(){
       if(Interwebs.isConnectedToInternet(getApplicationContext())){ // if internet access...
              new getSessionsTask().execute("http://padzonline.com/classbuzz/getsessions.php?studentno=" + studentNo);
       }
       else{
           Toast toast = Toast.makeText(getBaseContext(), "No internet connection", Toast.LENGTH_SHORT);
           toast.show();
           if(currentMenu==1)mainMenuInit();}
   }

    //Executes a 'getQuestionTask', which in turn will initiate the session (QuestionActivity) is successful.
    private void goToSession(int session){
        if(Interwebs.isConnectedToInternet(getApplicationContext())) { // if internet access
            try {
                selectedQ = sessionButtons[session].getText().toString();
                new getQuestionTask().execute("http://padzonline.com/classbuzz/getcontent.php?question="
                        + URLEncoder.encode(sessionButtons[session].getText().toString(), "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Toast toast = Toast.makeText(getBaseContext(), "Could not read session info!", Toast.LENGTH_SHORT);
                toast.show();
            }
        } else{
            Toast toast = Toast.makeText(getBaseContext(), "No internet connection", Toast.LENGTH_SHORT);
            toast.show();}
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button:
                goToActiveSessionsMenu();
                break;
            case R.id.button2:
                //Display info box
                infoBox.setVisibility(View.VISIBLE);
                AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
                anim.setDuration(600);
                infoBox.setAnimation(anim);
                break;
            case R.id.button3:
                Intent resultsIntent = new Intent("com.example.padzonline.classbuzz.RESULTSACTIVITY");
                startActivity(resultsIntent);
                break;
            case R.id.button4:
                Intent loginIntent = new Intent("com.example.padzonline.classbuzz.LOGINACTIVITY");
                startActivity(loginIntent);
                finish();
                break;
            case R.id.infoBox:
                //Tap info box to dismiss
                infoBox.setVisibility(View.INVISIBLE);
                break;
            case R.id.sessionbutton1:
                goToSession(0);
                break;
            case R.id.sessionbutton2:
                goToSession(1);
                break;
            case R.id.sessionbutton3:
                goToSession(2);
                break;
            case R.id.sessionbutton4:
                goToSession(3);
                break;
            case R.id.sessionbutton5:
                goToSession(4);
                break;
            case R.id.sessionbutton6:
                goToSession(5);
                break;
            case R.id.sessionbutton7:
                goToSession(6);
                break;
            case R.id.sessionbutton8:
                goToSession(7);
                break;
            case R.id.sessionbutton9:
                goToSession(8);
                break;
            case R.id.sessionbutton10:
                goToSession(9);
                break;
        }
    }

    @Override
    public void onBackPressed(){
        if(currentMenu==1){
        mainMenuInit();
        }
        else infoBox.setVisibility(View.INVISIBLE); // in case the user is attempting to back out of the info box
    }

    //Async task which performs HTTP GET request for the list of active sessions assigned to the user
    private class getSessionsTask extends Interwebs.GETTask {
        @Override
        protected void onPostExecute(String output) {

            JSONArray arr = null;
            try {
                arr = new JSONArray(output);

                //Assign any active session info to each session button and hide those that are not needed
                for(int i=0; i<10; i++){
                    //Assign  each retrieved session to a button
                    if (i<arr.length()){sessionButtons[i].setText(arr.getJSONObject(i).getString("question"));
                        // If the current session has already been answered by the user, make the
                        // button and its text green...
                            if(!arr.getJSONObject(i).getString("response").equals("0")) {
                                sessionButtons[i].setBackgroundResource(R.drawable.menubtn);
                                sessionButtons[i].setTextColor(Color.parseColor("#00cc91"));
                            }
                    }
                    else sessionButtons[i].setVisibility(View.GONE); //hide any unused buttons
                }

                // Fade out the main menu while active sessions menu is fading in
                if(currentMenu==0){AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
                    fadeOut.setDuration(400);
                    fadeOut.setFillAfter(true);
                    menuLayout.setAnimation(fadeOut);

                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) { }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            menuLayout.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) { }
                    });
                }

                //Fade in button for each active session
                for(int i=0; i<arr.length(); i++){
                AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
                anim.setDuration(600);
                anim.setStartOffset(i*200);
                sessionButtons[i].setAnimation(anim);
                }

                // Show list of active session buttons
                sv.setVisibility(View.VISIBLE);
                currentMenu=1;
                titleText.setText("ACTIVE SESSIONS");

            } catch (JSONException e) {
                e.printStackTrace();

            }

            return;
        }
    }

    /*
    HTTP GET task to retrieve data for the question selected by user which is to be displayed.
    (Question title, question text, image URL, response options).
    Task will start the question activity in the onPostExecute(), and pass this data
    with the Intent object.
     */
    private class getQuestionTask extends Interwebs.GETTask {
        @Override
        protected void onPostExecute(String output) {
            Intent intent = new Intent("com.example.padzonline.classbuzz.QUESTIONACTIVITY");
            JSONObject jObject;
            try {
                jObject = new JSONObject(output);

                intent.putExtra("question", selectedQ);
                intent.putExtra("questiontitle", jObject.getString("titletext"));
                intent.putExtra("questiontext", jObject.getString("questiontext"));
                intent.putExtra("image", jObject.getString("image"));

                intent.putExtra("ans1", jObject.getString("a_one"));
                intent.putExtra("ans2", jObject.getString("a_two"));
                intent.putExtra("ans3", jObject.getString("a_three"));
                intent.putExtra("ans4", jObject.getString("a_four"));

                intent.putExtra("studentno", studentNo);
                intent.putExtra("password", password);

                startActivity(intent);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}