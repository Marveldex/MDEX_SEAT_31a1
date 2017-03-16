
/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mdex.venusAlpha01a;


/*
*   TODO
*   send button
*   re-connection 안정화...
*   protocol 분석
*
*
* test git
*
* */

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.nordicsemi.venusAlpha01a.PacketParser;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.R.id.list;
import static android.os.SystemClock.elapsedRealtime;




public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;

    TextView mRemoteRssiVal;
    RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private com.mdex.venusAlpha01a.UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;
    private Button btnConnectDisconnect,btnSend;

    private boolean mSave_Flag = false;

    Button mSave_Start;
    Button mSave_Stop;
    private String positionCSV = null;
    Map<String, Object> hmap = null;
    ArrayList<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
    TextView mPointTxt;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        messageListView = (ListView) findViewById(R.id.listMessage);
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        messageListView.setAdapter(listAdapter);
        messageListView.setDivider(null);
        btnConnectDisconnect=(Button) findViewById(R.id.btn_select);
        btnSend=(Button) findViewById(R.id.sendButton);
        edtMessage = (EditText) findViewById(R.id.sendText);
        mSave_Start = (Button)findViewById(R.id.Save_Start);
        mSave_Stop = (Button)findViewById(R.id.Save_Stop);
        mPointTxt = (TextView)findViewById(R.id.point);



        onCreate_UI();

        service_init();
       
        // Handle Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else {
                	if (btnConnectDisconnect.getText().equals("Connect")){
                		
                		//Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices
                		
            			Intent newIntent = new Intent(MainActivity.this, com.mdex.venusAlpha01a.DeviceListActivity.class);
            			startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
        			} else {
        				//Disconnect button pressed
        				if (mDevice!=null)
        				{
        					mService.disconnect();
        					
        				}
        			}
                }
            }
        });

        // Handle Send button
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	EditText editText = (EditText) findViewById(R.id.sendText);
            	String message = editText.getText().toString();
            	byte[] value;
				try {
					//send data to service
					value = message.getBytes("UTF-8");
					mService.writeRXCharacteristic(value);
					//Update the log with time stamp
					String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
					listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
               	 	messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
               	 	edtMessage.setText("");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });
     
        // Set initial UI state


    }
    
    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((com.mdex.venusAlpha01a.UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            if(mService != null) {
                //mService.disconnect(mDevice);
                //mService.disconnect();
            }
            mService = null;
        }
    };

    public void onClick_Save(View v){
        switch (v.getId()) {
//            case R.id.Save_Start:
            case R.id.Save_Start:

                if(mSave_Flag==false){
                    mSave_Flag = true;
                    mSave_Start.setEnabled(true);
                    mSave_Start.setText("Save Stop");
                }else{
                    mSave_Flag = false;
                    //파일 생성
                    createToFilecsv();
                    mSave_Start.setEnabled(true);
                    mSave_Start.setText("Save Start");
                }
                break;

            case R.id.Save_Stop:

                mSave_Flag = false;
                //파일 생성
                createToFilecsv();
                mSave_Start.setEnabled(true);
                mSave_Stop.setEnabled(false);
                break;
        }

    }

    private void createToFilecsv(){

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyyMMdd");
        String formatDate = sdfNow.format(date);

        try{
            File file = new File("/mnt/sdcard/Download/" + formatDate + ".csv");
            if(!file.exists()){
                file = new File("/mnt/sdcard/Download/" + formatDate + ".csv");
                file.createNewFile();
            }

            PrintWriter csvWriter;
            csvWriter = new  PrintWriter(new FileWriter(file,true));

            csvWriter.print(positionCSV);
            //csvWriter.print("\r\n");
            csvWriter.close();

            mPointTxt.setText("파일의 저장 경로  : /mnt/sdcard/Download/오늘날짜.csv ");



        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }


    private Handler mHandler = new Handler() {
        @Override
        
        //Handler events that received from UART service 
        public void handleMessage(Message msg) {
  
        }
    };

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
           //*********************//
            if (action.equals(com.mdex.venusAlpha01a.UartService.ACTION_GATT_CONNECTED)) {
            	 runOnUiThread(new Runnable() {
                     public void run() {
                         String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                         Log.d(TAG, "UART_CONNECT_MSG");
                         btnConnectDisconnect.setText("Disconnect");
                         edtMessage.setEnabled(true);
                         btnSend.setEnabled(true);
                         ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connected");
                         listAdapter.add("["+currentDateTimeString+"] Connected to: "+ mDevice.getName());
                         messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                         mState = UART_PROFILE_CONNECTED;
                     }
            	 });
            }
           
          //*********************//
            if (action.equals(com.mdex.venusAlpha01a.UartService.ACTION_GATT_DISCONNECTED)) {
            	 runOnUiThread(new Runnable() {
                     public void run() {
                    	 	 String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             Log.d(TAG, "UART_DISCONNECT_MSG");
                             btnConnectDisconnect.setText("Connect");
                             edtMessage.setEnabled(false);
                             btnSend.setEnabled(false);
                             ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
                             listAdapter.add("["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName());
                             mState = UART_PROFILE_DISCONNECTED;
                             mService.close();
                            //setUiState();
                         
                     }
                 });
            }
            
          
          //*********************//
            if (action.equals(com.mdex.venusAlpha01a.UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
             	 mService.enableTXNotification();
            }
          //*********************//
            if (action.equals(com.mdex.venusAlpha01a.UartService.ACTION_DATA_AVAILABLE)) {
                final byte[] packetVenus2Phone = intent.getByteArrayExtra(com.mdex.venusAlpha01a.UartService.EXTRA_DATA);

                runOnUiThread(new Runnable() {
                     public void run() {
                         try {
                         	String text = new String(packetVenus2Phone, "UTF-8");
                         	String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             listAdapter.add("["+currentDateTimeString+"] RX: "+text);
                             messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);

                         } catch (Exception e) {
                             Log.e(TAG, e.toString());
                         }

                         //-------------------<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                         if(packetVenus2Phone.length < 20){
                             Log.e(TAG, packetVenus2Phone.toString());
                             return;
                         }

                         m_PacketParser.ParseOnePacket(packetVenus2Phone);
                         UI_showParsedData();
                         UI_updateComputedData();

                     }

                 });
             }
           //*********************//
            if (action.equals(com.mdex.venusAlpha01a.UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
            	showMessage("Device doesn't support UART. Disconnecting");
            	mService.disconnect();
            }
        }
    };

    private void service_init() {
        Intent bindIntent = new Intent(this, com.mdex.venusAlpha01a.UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
  
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(com.mdex.venusAlpha01a.UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(com.mdex.venusAlpha01a.UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(com.mdex.venusAlpha01a.UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(com.mdex.venusAlpha01a.UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(com.mdex.venusAlpha01a.UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
    	 super.onDestroy();
        Log.d(TAG, "onDestroy()");
        
        try {
        	LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        } 
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService= null;
       
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
 
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

        case REQUEST_SELECT_DEVICE:
        	//When the DeviceListActivity return, with the selected device address
            if (resultCode == Activity.RESULT_OK && data != null) {
                String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
               
                Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                btnConnectDisconnect.setText("Connecting...");

                mService.connect(deviceAddress);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        default:
            Log.e(TAG, "wrong request code");
            break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("nRFUART's running in background.\n             Disconnect to exit");
        }
        else {
            new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.popup_title)
            .setMessage(R.string.popup_message)
            .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
   	                finish();
                }
            })
            .setNegativeButton(R.string.popup_no, null)
            .show();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  USER VARIABLES
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private EditText edtMessage;
    private TextView edtBatteryLevel;
    private TextView edtLastPacketTime;
    private TextView tvADC_HexaMain;
    private TextView tvADC_HexaShield;
    private int mBoardSelect = 1; // 0 : Main board, 1 : Shield board

    private TextView [] atvChairCells_Row0 = new TextView[PacketParser.def_CELL_COUNT_ROW0];
    private TextView [] atvChairCells_Row1 = new TextView[PacketParser.def_CELL_COUNT_ROW1];
    private TextView [] atvChairCells_Row2 = new TextView[PacketParser.def_CELL_COUNT_ROW2];

    private TextView edtCOMLog;

    PacketParser m_PacketParser = new PacketParser();

    int def_UI_SERO_START_X = 92;
    int def_UI_CELL_WIDTH = 63;
    int def_UI_CELL_HEIGHT = 40;

    Space sp_COM_sero;
    Space sp_COC_left;
    Space sp_COC_right;

    enum POSTURE_tag{
        POSTURE_NO_LOG,
        POSTURE_LEFT_BAD,
        POSTURE_LEFT,
        POSTURE_CENTER,
        POSTURE_RIGHT,
        POSTURE_RIGHT_BAD
    }

    POSTURE_tag m_PostureState;
    long m_PostureKeepTimeMS = 0;

    MediaPlayer m_Player;



    protected void onRadioClicked(View view){
        switch (view.getId()){
            case R.id.RB_MAIN:
                mBoardSelect = 0; // Main board
                break;
            case R.id.RB_SHIELD:
                mBoardSelect = 1; // Shield board
                break;
        }
    }


    private void onCreate_UI(){
        edtBatteryLevel = (TextView) findViewById(R.id.TV_BatteryLevel);
        edtLastPacketTime = (TextView)findViewById(R.id.TV_CurTime);
        tvADC_HexaMain = (TextView) findViewById(R.id.PacketHexaMain);
        tvADC_HexaShield = (TextView) findViewById(R.id.PacketHexaShield);

        int cell_index = 0;
        atvChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_0);
        atvChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_1);
        atvChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_2);
        atvChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_3);
        atvChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_4);
        atvChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_5);

        cell_index = 0;
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_0);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_1);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_2);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_3);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_4);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_5);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_6);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_7);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_8);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_9);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_10);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_11);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_12);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_13);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_14);

        cell_index = 0;
        atvChairCells_Row2[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_2_0);
        atvChairCells_Row2[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_2_1);
        atvChairCells_Row2[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_2_2);
        atvChairCells_Row2[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_2_3);
        atvChairCells_Row2[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_2_4);
        atvChairCells_Row2[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_2_5);
        atvChairCells_Row2[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_2_6);
        atvChairCells_Row2[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_2_7);
        atvChairCells_Row2[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_2_8);
        atvChairCells_Row2[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_2_9);

        edtBatteryLevel.setText(String.format("Battery level : [%2d%%]", 0));

        edtCOMLog = (TextView)findViewById(R.id.TV_COM_LOG );

        sp_COM_sero = (Space)findViewById(R.id.space_COM);
        sp_COC_left = (Space)findViewById(R.id.space_COC_LEFT);
        sp_COC_right = (Space)findViewById(R.id.space_COC_RIGHT);


        m_PostureState = POSTURE_tag.POSTURE_NO_LOG;

        //creating media player
        m_Player = MediaPlayer.create(this, R.raw.alarm_flipover);
        m_Player.setLooping(false);
        //m_Player.start();

    }

    private void UI_showParsedData(){
        // last time packet received
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            String time_str = dateFormat.format(cal.getTime());
            edtLastPacketTime.setText("Last packet : " + time_str);
        }

        if(m_PacketParser.isPacketCompleted() == false)
            return;

        //  UI - battery level
        edtBatteryLevel.setText(String.format("Battery level : [%2d%%]", PacketParser.getBatteryLevel()));

        //  UI - cell data and color
        {
            int nSensorValue = 0;
            int cell_index = 0;
            int row_index = 0;

            hmap = new HashMap<String, Object>();

            for (cell_index = 0 ; cell_index < PacketParser.def_CELL_COUNT_ROW0 ; cell_index++){
                nSensorValue = m_PacketParser.getSensorDataByCoord(row_index, cell_index);
                atvChairCells_Row0[cell_index].setText(String.format("%d", nSensorValue));
                atvChairCells_Row0[cell_index].setBackgroundColor(0x00FF0000 | (nSensorValue << 24) );

                if (mSave_Flag==true){
                    //hmap.put("0_" + cell_index ,  atvChairCells_Row0[cell_index].getText());

                    String nPoint = atvChairCells_Row0[cell_index].getText().toString();

                    if(positionCSV==null){
                        positionCSV =  "0_" + cell_index + "," + nPoint + ",";

                    }else{
                        positionCSV = positionCSV + "0_" + cell_index + "," + nPoint + ",";
                    }

                    /*if(Integer.parseInt(nPoint) > 0){
                        mPointTxt.append("0_" + cell_index + "," + nPoint + "///");
                    }*/

                }

            }

            row_index = 1;
            for (cell_index = 0 ; cell_index < PacketParser.def_CELL_COUNT_ROW1 ; cell_index++){
                nSensorValue = m_PacketParser.getSensorDataByCoord(row_index, cell_index);
                atvChairCells_Row1[cell_index].setText(String.format("%d", nSensorValue));
                atvChairCells_Row1[cell_index].setBackgroundColor(0x00FF0000 | (nSensorValue << 24) );

                String nPoint1 = atvChairCells_Row1[cell_index].getText().toString();

                if (mSave_Flag==true){
                    //hmap.put("1_" + cell_index ,  atvChairCells_Row0[cell_index].getText());
                    positionCSV = positionCSV + "1_" + cell_index + "," + nPoint1 + ",";

                }
            }

            row_index = 2;
            for (cell_index = 0 ; cell_index < PacketParser.def_CELL_COUNT_ROW2 ; cell_index++){
                nSensorValue = m_PacketParser.getSensorDataByCoord(row_index, cell_index);
                atvChairCells_Row2[cell_index].setText(String.format("%d", nSensorValue));
                atvChairCells_Row2[cell_index].setBackgroundColor(0x00FF0000 | (nSensorValue << 24) );

                String nPoint2 = atvChairCells_Row2[cell_index].getText().toString();

                if (mSave_Flag==true){
                    //hmap.put("2_" + cell_index ,  atvChairCells_Row0[cell_index].getText());

                    if(cell_index < 9 ){
                        positionCSV = positionCSV + "2_" + cell_index + "," + nPoint2 + ",";

                    }else{
                        positionCSV = positionCSV + "2_" + cell_index + "," + nPoint2 + "\r\n";
                    }

                }
            }

            //list.add(hmap);
        }

        // UI LOG : adc data
        tvADC_HexaMain.setText(m_PacketParser.textHexaMain);
        tvADC_HexaShield.setText(m_PacketParser.textHexaShield);

    }

    boolean m_isAlarm_2000MS = false;
    long m_PostureOriginTimeMS = 0L;

    private void setPostureState(POSTURE_tag posture_state){
        if(m_PostureState != posture_state) {
            m_isAlarm_2000MS = false;
            m_PostureOriginTimeMS = elapsedRealtime();
        }

        //  it's not necessary
        m_PostureState = posture_state;
    }

    private POSTURE_tag getPostureState(){
        return m_PostureState;
    }

    private boolean makePostureAlarm_2000MS() {
        if(m_isAlarm_2000MS == true)
            return false;

        m_Player.start();
        m_isAlarm_2000MS = true;
        return true;
    }

    private long getPostureElapsedTime(){
        return (elapsedRealtime() - m_PostureOriginTimeMS);
    }

    private void UI_updateComputedData(){
        if(m_PacketParser.isPostureValid_Row1() == false){
            setPostureState(POSTURE_tag.POSTURE_NO_LOG);
            edtCOMLog.setText(String.format("EMPTY, %d sec", getPostureElapsedTime() / 1000));

            return;
        }

        //  UI draw COM - sero
        {
            float com_coord_x = m_PacketParser.getCOM_x_Row1();

            //  calculate COM - lateral
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)sp_COM_sero.getLayoutParams();
            params.setMargins(0, 0, (int)(def_UI_CELL_WIDTH * com_coord_x) + def_UI_SERO_START_X, 0); //substitute parameters for left, top, right, bottom
            sp_COM_sero.setLayoutParams(params);

            float com_coord_x_central = com_coord_x - 7.0F;
            if(com_coord_x < 6.3F){
                edtCOMLog.setText(String.format("LEFT - BAD (%1.3f), %d sec", com_coord_x_central, getPostureElapsedTime() / 1000));
                setPostureState(POSTURE_tag.POSTURE_LEFT_BAD);
            }
            else if(com_coord_x < 6.7F){
                edtCOMLog.setText(String.format("LEFT (%1.3f), %d sec", com_coord_x_central, getPostureElapsedTime() / 1000));
                setPostureState(POSTURE_tag.POSTURE_LEFT);
            }
            else if(com_coord_x < 7.3F){
                edtCOMLog.setText(String.format("CENTER (%1.3f), %d sec", com_coord_x_central, getPostureElapsedTime() / 1000));
                setPostureState(POSTURE_tag.POSTURE_CENTER);
            }
            else if(com_coord_x < 7.7F){
                edtCOMLog.setText(String.format("RIGHT (%1.3f), %d sec", com_coord_x_central, getPostureElapsedTime() / 1000));
                setPostureState(POSTURE_tag.POSTURE_RIGHT);
            }
            else {
                edtCOMLog.setText(String.format("RIGHT - BAD (%1.3f), %d sec", com_coord_x_central, getPostureElapsedTime() / 1000));
                setPostureState(POSTURE_tag.POSTURE_RIGHT_BAD);
            }
        }

        //  make alarm of bad posture
        if(2000 < getPostureElapsedTime()) {
            switch (getPostureState()){
                case POSTURE_LEFT_BAD:
                case POSTURE_LEFT:
                case POSTURE_RIGHT:
                case POSTURE_RIGHT_BAD:
                    makePostureAlarm_2000MS();
                    break;
            }
        }

        //  UI draw COC
        {
            float coc_coord_left = m_PacketParser.getCOC_left_Row1();
            //  calculate COC - left
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) sp_COC_left.getLayoutParams();
            params.setMargins(0, 0, (int) (def_UI_CELL_WIDTH * coc_coord_left) + def_UI_SERO_START_X, 0); //substitute parameters for left, top, right, bottom
            sp_COC_left.setLayoutParams(params);
        }

        {
            float coc_coord_right = m_PacketParser.getCOC_right_Row1();
            //  calculate COC - right
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)sp_COC_right.getLayoutParams();
            params.setMargins(0, 0, (int)(def_UI_CELL_WIDTH * coc_coord_right) + def_UI_SERO_START_X, 0); //substitute parameters for left, top, right, bottom
            sp_COC_right.setLayoutParams(params);
        }
    }

}
