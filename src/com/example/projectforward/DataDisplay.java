/*----------------------------------------------------------------------------------------------------------
 * This is a class file of the Project Forward Android Application. 
 * File Name: DataDisplay.java
 * Written by: Devin Moore, Team 3
 * This class extends Activity, and is in charge of displaying specific data that we receive from
 * the FPGA over the Bluetooth connection. Each different type of data such as speed, accelerometer 
 * data will be using different instances of this activity. 
 */


package com.example.projectforward;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DataDisplay extends Activity {
	public static int value1 =0;				//Integer used for testing. Maybe used to store incoming 
												//Bluetooth data
	public static String message;				//Incomming message that will determine what type of data
												//will be displayed
	public static String speed = "speed";		//String used for testing

	//
	//When the activity starts, just set up the screen as defined in activity_data_display.xml
	//and set the text to the displayed data type from the message.
	//-----------------------------------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		    // Get the message from the intent
		  Intent intent = getIntent();
		  message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

		    // Create the text view
		  TextView textView = new TextView(this);
		  textView.setTextSize(40);
		  textView.setText(message);

		    // Set the text view as the activity layout
		  setContentView(textView);

		setContentView(R.layout.activity_data_display);
	    TextView t = (TextView)findViewById(R.id.top_string);
	    t.setText(message);
	}
	

	
	//
	//When the activity starts, set up the screen as defined in data_display.xml
	//-------------------------------------------------------------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.data_display, menu);
		return true;
	}
	
	//
	//Depending on the button pressed to get to this activity, different data will be 
	//required to display to the user.
	//-------------------------------------------------------------------------------------------------------
	public void incValue(View view){
		
		//Get the intent from the main activity to determine what data the user wants
		//---------------------------------------------------------------------------------------------------
		Intent intent = getIntent();
		String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
		
		//If the user is looking for speed data
		//---------------------------------------------------------------------------------------------------
		if(message.equals(speed)){
			value1++;
		    TextView t = (TextView)findViewById(R.id.top_string);
		    t.setText(message+" is: "+value1);	
		    
		//If the user is looking for accelerometer data
		//---------------------------------------------------------------------------------------------------
		}else if (message.intern() == "accel"){
			value1++;
		    TextView t = (TextView)findViewById(R.id.top_string);
		    t.setText(message+" is: "+value1);
		    
		//Connect button has been re-routed, so this is a place holder for other data type
		//---------------------------------------------------------------------------------------------------		    
		}else if (message.intern() == "connect"){			
			value1++;
		    TextView t = (TextView)findViewById(R.id.top_string);
		    t.setText(message+" is: "+value1);
		    
		//Connect button has been re-routed, so this is a place holder for other data type
		//---------------------------------------------------------------------------------------------------
		}else if (message.intern() == "send"){
			value1++;
		    TextView t = (TextView)findViewById(R.id.top_string);
		    t.setText(message+" is: "+value1);	
		    
		//If the user selection was not correct, then notify the use that an error has occured.
		//---------------------------------------------------------------------------------------------------    
		}else{		
			value1++;
			TextView t = (TextView)findViewById(R.id.top_string);
			t.setText("bad dog");
		}	    	
	}
}
