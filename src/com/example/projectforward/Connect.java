/*----------------------------------------------------------------------------------------------------------
 * This is a class file of the Project Forward Android Application. 
 * File Name: Connect.java
 * Written by: Devin Moore, Team 3
 * This class extends Activity, and is in charge of connecting the phone to an external
 * Bluetooth module. In our case it will connect to the RN41 module and communicate with our
 * FPGA through UART.
 */

package com.example.projectforward;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

//||
//|| This class takes care of connecting to the Bluetooth device. This may have to become
//|| a service eventually.
//-----------------------------------------------------------------------------------------------------------
public class Connect extends Activity {
													//UUID of the RN41 that we are connecting to
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final String LOG_TAG = null;	
	private BluetoothAdapter myBluetoothAdapter;	//Internal Android Bluetooth transmitter/receiver.
    private BluetoothDevice[] myBondedDevices;		//An array of the different devices already bonded to the phone
    private BluetoothDevice myDevice;				//The specific device that we want to communicate with
    private int mDeviceIndex = 0;					//Index in the array of devices
    public BluetoothSocket mySocket;
    public InputStream myInput;
    public OutputStream myOutput;
    public double input= 0; 
    public int heartRateCap = 0;
    public int wheelSize = 30;
    public File data;
    File file;
    private Handler loopHandler = new Handler();
    OutputStream os = null;
    public int connectedFlag = 0;
    public Timestamp myTime;
    private Handler myHandler = new Handler();
    private ProgressBar mProgress;
    public int progressbar = 0;
 
    //| 
 	//| When the application opens, just set up the screen as defined in activity_connect.xml and set
    //| up button with an onClickListener to set up the Bluetooth connection
 	//| --------------------------------------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect);
		    
		//
		//Setup the onClick listener and attempt to connect to the device once the button has been pressed
		//----------------------------------------------------------------------------------------------------
		Button connectButton = (Button) findViewById(R.id.connect_but);
        connectButton.setOnClickListener(new View.OnClickListener() {
        	
        	//This functions connects to the RN-41 on the FPGA when the connect button is pressed.
        	//----------------------------------------------------------------------------------------------
            public void onClick(View v) {
            	myTime = new Timestamp(System.currentTimeMillis());
            	
            	
            	//Set up the Bluetooth adapter on the phone, and use it to get a list of bonded devices.
            	//Bonded devices are not connected yet(no communication socket).
            	//--------------------------------------------------------------------------------------------
            	myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                myBondedDevices =
                    (BluetoothDevice[]) myBluetoothAdapter.getBondedDevices().toArray(
                        new BluetoothDevice[0]);
                
                //If there are no devices bonded to the phone, notify the user.
                //--------------------------------------------------------------------------------------------
                if (myBondedDevices==null){
                	TextView t = (TextView)findViewById(R.id.top_connect);
            	    t.setText("bad dog");                	
                }
                
                //Populate the array of Bluetooth devices that are bonded to the phone
                //-------------------------------------------------------------------------------------------
                if (myBondedDevices.length > 0) {
                	int deviceCount = myBondedDevices.length;
                      if (mDeviceIndex < deviceCount) myDevice = myBondedDevices[mDeviceIndex];
                    else {
                        mDeviceIndex = 0;
                        myDevice = myBondedDevices[0];
                    }
                    String[] deviceNames = new String[deviceCount];
                    int i = 0;
                    for (BluetoothDevice device : myBondedDevices) {
                        deviceNames[i++] = device.getName();
                    }
                    
                    myDevice=myBondedDevices[0];
                    TextView t = (TextView)findViewById(R.id.top_connect);
            	    t.setText(deviceNames[0]); 
            	    TextView t3 = (TextView)findViewById(R.id.top_connect2);
            	    t3.setText(" ");
            	    TextView t8 = (TextView)findViewById(R.id.top_connect3);
            	    t8.setText(" ");
            	    TextView t9 = (TextView)findViewById(R.id.heart_ratecap_display);
            	    t9.setText("Connection Succesful!");
            	    TextView t10 = (TextView)findViewById(R.id.wheel_size_display);
            	    t10.setText("Connected to: ");
            	    
            	    //Create a Bluetooth socket and try to connect to the RN41. It has to be the 
            	    //first bonded device in the Bluetooth options on the phone at this point.
            	    //-------------------------------------------------------------------------------------------
            	    try {
						mySocket =myDevice.createRfcommSocketToServiceRecord(MY_UUID);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						
						
						e.printStackTrace();
					}
            	    try {
						mySocket.connect();
						connectedFlag = 1;
					} catch (IOException e) {
						e.printStackTrace();
						TextView t42 = (TextView)findViewById(R.id.heart_ratecap_display);
		            	t42.setText("Connection failed!!");
		            	connectedFlag = 0;
					}
            	    //
        	    	//Create input stream to get data from the FPGA
        	    	//-------------------------------------------------------
            	    try {
						myInput = mySocket.getInputStream();
					} catch (IOException e) {
						e.printStackTrace();
					}
            	    //
        	    	//Create output stream to send data to the FPGA
        	    	//-------------------------------------------------------
            	    try {
						myOutput = mySocket.getOutputStream();
					} catch (IOException e) {
						e.printStackTrace();
					}            	   
            	    //|
            	    //| Open File for writing data to.
            	    //| --------------------------------------------------------
            	    Environment.getExternalStorageState();
            	    myTime.setTime(System.currentTimeMillis());
            	    
            		data = getAlbumStorageDir("dat.csv");
            		file = new File(data,myTime.toString()+"data.csv");            		
            		
            		try {
						os = new FileOutputStream(file);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
            		
            		try {
            			os.write("Time_Stamp".getBytes());
            			os.write(',');
            			os.write("Heart_Rate Cap".getBytes());
            			os.write(',');
            			os.write("User Heart_Rate".getBytes());
            			os.write(',');
            			os.write("Inclination_Angle".getBytes());
            			os.write(',');
						os.write("Speed".getBytes());
						os.write(',');
						os.write("ADC_Data".getBytes());						
						os.write('\n');
					} catch (IOException e) {
						e.printStackTrace();
					}
            		 
                }
            }
        });
	}
	
	//|
    //| This method controls the protocol that updates the variables and displays them on the screen.
	//| This will happen as often as the timer is set.
	//|	---------------------------------------------------------------------------------------------
    private Runnable sendByte3 = new Runnable(){
    @Override
    	public void run(){
			int signByte = 0;
			int ADCL = 0;
			int ADCH = 0;
			double ADCfull = 0;
			int heartCount = 0;
			int angleCount = 0;
			int speedCount = 0;
			int adcCount = 0;
			double previousHR = 0;
			double previousAngle = 0;
			double previousSpeed = 0;
			double previousADC = 0;
			//final int progressbar = 0;
			
			mProgress = (ProgressBar) findViewById(R.id.progressBar1);
			
			myTime.setTime(System.currentTimeMillis());
			//| 
			//| Log Time Stamp data to file	
			try {
				os.write(myTime.toString().getBytes());
				//os.write("Time".getBytes());
				os.write(',');
				os.write(new DecimalFormat("###.##").format(heartRateCap).getBytes());
				os.write(',');
				//os.write("Time".getBytes());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			
			//| ----------------------------------------------------------
			//| This will call for the Heart Rate 
			//| ----------------------------------------------------------
			try {
				do	{
					myOutput.write(01);
					Thread.sleep(100);
				}
				while(myInput.available()==0);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			//| Read Byte 
			try {
				 //if (myInput.available()>0)		//*********************************
				 input = myInput.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if((input == 0) && (previousHR != 0)){
				heartCount++;
				if(heartCount < 3){
					input = previousHR;
				}				
			}else {
				heartCount = 0;	
				previousHR = input;
			}
			
			
			//| ---------------------------------------------------------
		 	//| Display heart rate.
		 	//| ---------------------------------------------------------
			TextView t4 = (TextView)findViewById(R.id.top_connect);
	 	    t4.setText("Heart Rate = "+input);
	 	    
	 	    //| 
			//| Log Heart Rate data to file
			try {
				os.write(Double.toString(input).getBytes());
				os.write(',');
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			
	 	    //| ----------------------------------------------------------------
	 	    //| This will call for the inclination angle of the system
	 	    //| ----------------------------------------------------------------
			try {
				do	{
					myOutput.write(02);
					Thread.sleep(100);
				}
				while(myInput.available()==0);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
	 	   	// Read the top byte of angle to determine sign
			try {
				//if (myInput.available()>0)		//*********************************
				signByte = myInput.read();
			} catch (IOException e) {
				e.printStackTrace();
			}

		 	//Request lower byte of the angle
			try {
				do	{
					myOutput.write(03);
					Thread.sleep(100);
				}
				while(myInput.available()==0);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	 

		 	try {
		 		//if (myInput.available()>0)		//*********************************
				input = myInput.read();
		 	} catch (IOException e) {
				e.printStackTrace();
		 	}
		 	
		 	if((input == 0) && (previousAngle != 0)){
				angleCount++;
				if(angleCount < 3){
					input = previousAngle;
				}				
			}else {
				angleCount = 0;	
				previousAngle = input;
			}
		 	
		 	//| ---------------------------------------------------------
		 	//| Calculate and display the angle of inclination.
		 	//| ---------------------------------------------------------
			TextView t5 = (TextView)findViewById(R.id.top_connect2);
	 	    if(signByte < 2){
	 	    		
	 	    	 input = input/2.8;
	 	    	 t5.setText("Angle of inclination = "+new DecimalFormat("###.##").format(input)+" degrees");
	 	    	
	 	    	//| Write positive inclination to file
	 			try {
					os.write(new DecimalFormat("###.##").format(input).getBytes());
					os.write(',');
				} catch (IOException e1) {
					e1.printStackTrace();
				}
	 	   	 
	 	    }
	 	    else {
	 	    	input = 255-input;
	 	    	input = input/2.8;
	 	    	t5.setText("Angle of inclination = -"+new DecimalFormat("###.##").format(input)+" degrees");
	 	    	 
	 	    	//| Write negative inclination to file
	 			try {
	 				os.write('-');
					os.write(new DecimalFormat("###.##").format(input).getBytes());
					os.write(',');
				} catch (IOException e1) {
					e1.printStackTrace();
				}				
	 	    }
	 	
		 
	 	    //| -------------------------------------------------------------
	 	    //| This will call for the speed of the system
	 	    //| -------------------------------------------------------------    
	 	   try {
				do	{
					myOutput.write(04);
					Thread.sleep(100);
				}
				while(myInput.available()==0);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

    		try {
    			//if (myInput.available()>0)		//*********************************
    			input = myInput.read();
    		} catch (IOException e) {
				e.printStackTrace();
			}
    		
    		
    		if((input == 0) && (previousSpeed != 0)){
				speedCount++;
				if(speedCount < 3){
					input = previousSpeed;
				}				
			}else {
				speedCount = 0;	
				previousSpeed = input;
			}
    		
    		//| ---------------------------------------------------------
		 	//| Calculate and display the wheel size.
		 	//| ---------------------------------------------------------
			TextView t6 = (TextView)findViewById(R.id.top_connect3);
			input = input*4*wheelSize*60/63360;
			String speedVar = new DecimalFormat("###.##").format(input);
	 	    t6.setText("Speed = "+speedVar);
	 	    
	 	    //| 
			//| Log Speed data to file
			try {
				os.write(speedVar.getBytes());
				os.write(',');				
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
	 	    //|
	 	    //| Receive 12 bit ADC data
	 	    //| --------------------------------------------------------------------
	 	    //| Low byte
			try {
				do	{
					myOutput.write(07);
					Thread.sleep(100);
				}
				while(myInput.available()==0);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			try {
				//if (myInput.available()>0)		//*********************************
				ADCL = myInput.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//|High byte			
			try {
				do	{
					myOutput.write(8);
					Thread.sleep(100);
				}
				while(myInput.available()==0);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			

	 	   	// Read the top byte of angle to determine sign
			try {
				//if (myInput.available()>0)		//*********************************
				ADCH = myInput.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
			ADCH = ADCH*256;
			ADCfull = ADCH + ADCL;
			
			if((ADCfull == 0) && (previousADC != 0)){
				adcCount++;
				if(adcCount < 3){
					ADCfull = previousADC;
				}				
			}else {
				angleCount = 0;	
				previousADC = ADCfull;
			}
			
			try {
				os.write(new DecimalFormat("###.##").format(ADCfull).getBytes());
				os.write(',');
			} catch (IOException e1) {
				e1.printStackTrace();
			}			
			
			
			//ADCfull = ADCfull/902;
			String ADCfinal = new DecimalFormat("###.##").format(ADCfull);
			progressbar = (int)ADCfull; 
			myHandler.post(new Runnable() { public void run() {
                    mProgress.setProgress(progressbar);
                }
			});
			
			//| Display ADC to screen and log to file
			TextView t12 = (TextView)findViewById(R.id.ADC_display);
	 	    t12.setText("ADC data = "+ADCfinal);
	 	    

 	    
	 	    //| 
			//| End line in the CSV
			try {
				os.write('\n');
			} catch (IOException e1) {
				e1.printStackTrace();
			}
    	
	 	    loopHandler.postDelayed(sendByte3, 200);	
    	}
    };
	
    
    //|	
    //|	This function is ran once the user has hit the "Set" button next to the heart rate
    //| input field. It will send a byte to notified the FPGA and then send the heart rate cap.
    //| -----------------------------------------------------------------------------------------
	public void setHeart(View view){
		int attempt1 = 0;
		int attempt2 = 0;
		
		if(connectedFlag == 0) return;
		
		while(attempt1 == 0){
		try {
			myOutput.write(05);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
 	       Thread.sleep(100);
 	    } catch (InterruptedException e) {
 	       e.printStackTrace();
 	    }
		try {
			attempt1 = myInput.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
		
		EditText editText = (EditText) findViewById(R.id.edit_heartrate);
		heartRateCap = Integer.parseInt(editText.getText().toString());
		TextView t6 = (TextView)findViewById(R.id.heart_ratecap_display);
 	    t6.setText("Heart Rate Cap = "+heartRateCap);
		
 	     
 	    //| 
 	    //| This while loop keeps trying to make sure the FPGA receives the heartrate cap. 
 	    //| Should not be necessary.
 	    //| --------------------------------------------------------------------------------
 		while(attempt2 == 0){
 			try {
 				myOutput.write(heartRateCap);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			 try {
 	 	       Thread.sleep(100);
 	 	    } catch (InterruptedException e) {
 	 	       e.printStackTrace();
 	 	    }
 			try {
 				attempt2 = myInput.read();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}	
 		}
		
	}
	
	
	public void setWheel(View view){
		int attempt1 = 0;
		int attempt2 = 0;
		if(connectedFlag == 0) return;
		
		while(attempt1 == 0){
			try {
				myOutput.write(06);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 try {
	 	       Thread.sleep(100);
	 	    } catch (InterruptedException e) {
	 	       e.printStackTrace();
	 	    }
			try {
				attempt1 = myInput.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		EditText editText = (EditText) findViewById(R.id.edit_wheelSize);
		wheelSize = Integer.parseInt(editText.getText().toString());
		 TextView t6 = (TextView)findViewById(R.id.wheel_size_display);
 	     t6.setText("Wheel Size = "+wheelSize);
		
 		while(attempt2 == 0){
 			try {
 				myOutput.write(wheelSize);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			 try {
 	 	       Thread.sleep(100);
 	 	    } catch (InterruptedException e) {
 	 	       e.printStackTrace();
 	 	    }
 			try {
 				attempt2 = myInput.read();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}	
 		}
			
		
	}
	
	
	public File getAlbumStorageDir(String albumName) {
	    // Get the directory for the user's public pictures directory.
	    File file = new File(Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_PICTURES), albumName);
	    if (!file.mkdirs()) {
	        Log.e(LOG_TAG, "Directory not created");
	    }
	    return file;
	}
	
	public boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	};
	public void writeTest(View view) throws IOException{
		// Checks if external storage is available for read and write 
		Environment.getExternalStorageState();
		data = getAlbumStorageDir("dat.csv");
		File file = new File(data, "data.csv");
		OutputStream out = null;
		int stringy = 184;  
		    
		try {
			OutputStream os = new FileOutputStream(file);
			os.write('a');
			os.write(',');
			os.write(Integer.toString(stringy).getBytes());
			os.write('\n');
			os.write('a');
			os.write(Integer.toString(stringy).getBytes());
			os.write('\n');
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		
		}	
	};
	
	public void sendByte2(View view){
		if(connectedFlag ==1){
			loopHandler.postDelayed(sendByte3, 10);
		}		
	}	
	
	//
	//This determines what to do when the "back" button is hit on the cell phone
	//-------------------------------------------------------------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.connect, menu);
		return true;
	}
}
