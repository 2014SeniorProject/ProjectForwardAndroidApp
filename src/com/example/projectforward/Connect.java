/*----------------------------------------------------------------------------------------------------------
 * This is a class file of the Project Forward Android Application. 
 * File Name: Connect.java
 * Written by: Devin Moore, Team 3
 * This class extends Activity, and is in charge of connecting the phone to an external
 * Bluetooth module. In our case it will connect to the RN41 module and communicate with our
 * FPGA through UART.
 */

package com.example.projectforward;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

//
//This class takes care of connecting to the Bluetooth device. This may have to become
//a service eventually.
//-----------------------------------------------------------------------------------------------------------
public class Connect extends Activity {
													//UUID of the RN41 that we are connecting to
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");	
	private BluetoothAdapter myBluetoothAdapter;	//Internal Android Bluetooth transmitter/receiver.
    private BluetoothDevice[] myBondedDevices;		//An array of the different devices already bonded to the phone
    private BluetoothDevice myDevice;				//The specific device that we want to communicate with
    private int mDeviceIndex = 0;					//Index in the array of devices
    public BluetoothSocket mySocket;
    public InputStream myInput;
    public OutputStream myOutput;
    public double input= 0; 
    public int heartRateCap = 0;
    public int wheelSize = 0;
    
    private Handler loopHandler = new Handler();
 
    // 
 	//When the application opens, just set up the screen as defined in activity_connect.xml and set
    //up button with an onClickListener to set up the Bluetooth connection
 	//--------------------------------------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect);
		    
		
		//Setup the onClick listener and attempt to connect to the device once the button has been pressed
		//----------------------------------------------------------------------------------------------------
		Button connectButton = (Button) findViewById(R.id.connect_but);
        connectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
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
            	    TextView t3 = (TextView)findViewById(R.id.top_connect);
            	    t3.setText(deviceNames[1]);
            	    
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
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	    try {
						myInput = mySocket.getInputStream();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	    
            	    try {
						myOutput = mySocket.getOutputStream();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}            	   
                }
            }
        });
	}
	
	  //|
    //| This method controls the protocol that updates the variables and displays them on the screen
    private Runnable sendByte3 = new Runnable()
    {
    @Override
    public void run(){
		int signByte = 0;
	
		//
		//This will call for the Heart Rate 
		//------------------------------------------------------------
		try {
			myOutput.write(01);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
 	       Thread.sleep(50);
 	    } catch (InterruptedException e) {
 	       e.printStackTrace();
 	    }
		try {
			input = myInput.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 TextView t4 = (TextView)findViewById(R.id.top_connect);
 	     t4.setText("Heart Rate = "+input);	
 	     
 	    try {
 	       Thread.sleep(50);
 	    } catch (InterruptedException e) {
 	       e.printStackTrace();
 	    }
 	    //
 	    //This will call for the inclination angle of the system
 	    //-------------------------------------------------------------
 	    try {
			myOutput.write(02);
		 } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		 }
 	    try {
 	       Thread.sleep(50);
 	    } catch (InterruptedException e) {
 	       e.printStackTrace();
 	    }
 	   
 	   	// Read the top byte of angle to determine sign
		 try {
				signByte = myInput.read();
		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}//-----------------------------------------------------------
		 
		 //Sleep
		try {
			Thread.sleep(50);
	 	} catch (InterruptedException e) {
	 	    e.printStackTrace();
	 	}//---------------------------------------------------------
		 
		 //Request lower byte of the angle
		 try {
				myOutput.write(03);
			 } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		 }	 
		 try {
	 	       Thread.sleep(50);
	 	   } catch (InterruptedException e) {
	 	       e.printStackTrace();
	 	   }
		 
		 try {
				input = myInput.read();
		 } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		 }
		 	 
			 TextView t5 = (TextView)findViewById(R.id.top_connect2);
	 	     if(signByte == 0){
	 	    	 input = input/2.8;
	 	    	 t5.setText("Angle of inclination = "+new DecimalFormat("###.##").format(input)+" degrees");	
	 	     }
	 	     else {
	 	    	 input = 255-input;
	 	    	 input = input/2.8;
	 	    	 t5.setText("Angle of inclination = -"+new DecimalFormat("###.##").format(input)+" degrees");
	 	     }
	 	    
	 	    try {
	 	       Thread.sleep(50);
	 	   } catch (InterruptedException e) {
	 	       e.printStackTrace();
	 	   }
	 	  //
	 	  //This will call for the inclination speed of the system
	 	  //-------------------------------------------------------------    
	 	    try {
    			myOutput.write(04);
    		} catch (IOException e) {
				// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
	 	   try {
	 	       Thread.sleep(50);
	 	   } catch (InterruptedException e) {
	 	       e.printStackTrace();
	 	   }
    		try {
    			input = myInput.read();
    		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 TextView t6 = (TextView)findViewById(R.id.top_connect3);
			 input = input*4*wheelSize*60/63360;
	 	     t6.setText("Speed = "+new DecimalFormat("###.##").format(input));
	 	     
	 	    try {
	 	       Thread.sleep(50);
	 	   } catch (InterruptedException e) {
	 	       e.printStackTrace();
	 	   }     
    	
    loopHandler.postDelayed(sendByte3, 1000);	
    }
    };
	
	public void setHeart(View view){
		int attempt1 = 0;
		int attempt2 = 0;
		while(attempt1 == 0){
		try {
			myOutput.write(05);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
 	       Thread.sleep(50);
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
		
 		while(attempt2 == 0){
 			try {
 				myOutput.write(heartRateCap);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			 try {
 	 	       Thread.sleep(50);
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
		while(attempt1 == 0){
		try {
			myOutput.write(06);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
 	       Thread.sleep(50);
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
 	 	       Thread.sleep(50);
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

	public void sendByte2(View view){		
		loopHandler.postDelayed(sendByte3, 500);
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
