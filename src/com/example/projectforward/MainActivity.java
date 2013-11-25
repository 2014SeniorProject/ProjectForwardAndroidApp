/*----------------------------------------------------------------------------------
 * This is the main file of the Project Forward Android Application
 * File Name: MainActivity.java
 * Written by: Devin Moore, Team 3
 * This application will connect to an FPGA using an RN41 Bluetooth module from Sparkfun.com.
 * It will then be able to display certain data in real time to the user. This is meant to be a 
 * companion system to our Smart Electrically Assisted Bicycle.
 */



package com.example.projectforward;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	
	//This is just a unique tag for our messages, so that only our application will be using them.
	//Can be anything though.
	 public final static String EXTRA_MESSAGE = "com.example.projectforward.MESSAGE";
	
	// 
	//When the application opens, just set up the screen as defined in activity_main.xml 
	//------------------------------------------------------------------------------------------ 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    
 
    //
    //After the Speed button has be clicked, the Display Data activity should be started,
    //and the activity will be notified which button was hit by the intent.
    //-------------------------------------------------------------------------------------------
	public void beginSpeed(View view) {
	    Intent intent = new Intent(this, DataDisplay.class);	    
	    intent.putExtra(EXTRA_MESSAGE, "speed");
	    startActivity(intent);
	}
	
	//
    //After the Acceleration button has be clicked, the Display Data activity should be started,
    //and the activity will be notified which button was hit by the intent.
    //-------------------------------------------------------------------------------------------
	public void beginAccel(View view) {
	    Intent intent = new Intent(this, DataDisplay.class);	    
	    intent.putExtra(EXTRA_MESSAGE, "accel");
	    startActivity(intent);
	}
	
	//
    //After the Send button has be clicked, the Speed activity should be started,
    //and the activity will be notified which button was hit by the intent.
    //-------------------------------------------------------------------------------------------
	public void beginSend(View view) {
	    Intent intent = new Intent(this, DataDisplay.class);	    
	    intent.putExtra(EXTRA_MESSAGE, "send");
	    startActivity(intent);
	}
	
	//
    //After the Connect button has be clicked, the Connect activity should be started,
    //and the activity will be notified which button was hit by the intent.
    //-------------------------------------------------------------------------------------------
	public void beginConnect(View view) {
	    Intent intent = new Intent(this, Connect.class);	    
	    intent.putExtra(EXTRA_MESSAGE, "connect");
	    startActivity(intent);
	}
    
	//
	//This determines what to do when the "back" button is hit on the cell phone
	//--------------------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
