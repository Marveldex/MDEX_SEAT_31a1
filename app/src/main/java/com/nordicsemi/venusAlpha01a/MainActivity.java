
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
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
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

import static android.R.id.button3;
import static android.R.id.icon1;
import static android.R.id.list;
import static android.os.SystemClock.elapsedRealtime;

/**
 *
 * @mainpage SCMS( Sitting Cushion Management System) - Marveldex
 * @brief 스마트 방석을 이용한 Application :
 * @details 방석의 각 압력 센서의 값을 VENUS 보드를 통해 전달 받아 데이터를 처리하여 Application을 통해 여러 형태의 값으로 보여주는 기능을 하는 Source
 *
 * @brief 2번째 설명 :
 * @details 방석의 각 압력 센서의 값을 VENUS 보드를 통해 전달 받아 데이터를 처리하여 Application을 통해 여러 형태의 값으로 보여주는 기능을 하는 Source
 *
 */

/**
 *
 * @file MainActivity.java
 * @brief 메인 기능을 수행하는 파일
 *
 */

/**
 *
 * @brief this is main function for run this app
 * @details show values of sence and save to CSV File
 * @author Marveldex
 * @date 2017-03-17
 * @version 0.0.1
 * @li list1
 * @li list2
 *
 */

public class   MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    //private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;

    private int mState = UART_PROFILE_DISCONNECTED;
    private com.mdex.venusAlpha01a.UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    //private ListView messageListView;
    //private ArrayAdapter<String> listAdapter;
    private Button btnConnectDisconnect,btnSend;

    ArrayList<String> mlist;
    ArrayAdapter<String> mAdapter;
    ListView mStateList;
    private float vector;
    private String m_Mode_Info;
    private TextView m_Mode_TxtView;
    private int toast_flag = 0;
    RelativeLayout ui_coc_com_layout;

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
/*
        messageListView = (ListView) findViewById(R.id.listMessage);
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        messageListView.setAdapter(listAdapter);
        messageListView.setDivider(null);
*/
        btnConnectDisconnect=(Button) findViewById(R.id.btn_select);
        btnSend=(Button) findViewById(R.id.sendButton);
        edtMessage = (EditText) findViewById(R.id.sendText);
        ui_coc_com_layout = (RelativeLayout)findViewById(R.id.RelativeLayout_COM);

        UI_onCreate();

        service_init();

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

                toast_flag = 0;

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
/*
					//Update the log with time stamp
					String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
					listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
               	 	messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
*/
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

    private Handler mHandler = new Handler() {
        @Override
        
        //Handler events that received from UART service 
        public void handleMessage(Message msg) {
  
        }
    };

    /**
     *
     * @brief Receive Broadcast and Check and Action each Function
     * @details  It receives data from the UartService.java file as a broadcast and performs its function through its value. Manage Bluetooth and device connection status.
     * @param
     * @return
     * @throws
     */
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
/*
                         listAdapter.add("["+currentDateTimeString+"] Connected to: "+ mDevice.getName());
                         messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
*/
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
/*
                             listAdapter.add("["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName());
*/
                             mState = UART_PROFILE_DISCONNECTED;
                             mService.close();
                     }
                 });
            }
          
          //*********************//
            if (action.equals(com.mdex.venusAlpha01a.UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
             	 mService.enableTXNotification();
            }

            //-----------------------------------------------------
            // HERE RECEIVE RAW BLE DATA AND PARSE
            //-----------------------------------------------------

            if (action.equals(com.mdex.venusAlpha01a.UartService.ACTION_DATA_AVAILABLE)) {
                final byte[] packetVenus2Phone = intent.getByteArrayExtra(com.mdex.venusAlpha01a.UartService.EXTRA_DATA);

                runOnUiThread(new Runnable() {
                     public void run() {
                         try {
/*
                         	String text = new String(packetVenus2Phone, "UTF-8");
                         	String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             listAdapter.add("["+currentDateTimeString+"] RX: "+text);
                             messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
*/

                         } catch (Exception e) {
                             Log.e(TAG, e.toString());
                         }

                         if(packetVenus2Phone.length < m_PacketParser.def_PACKET_LENGTH){
                             Log.e(TAG, packetVenus2Phone.toString());
                         }
                         else {
                             // receive data and parse
                             m_PacketParser.onReceiveRawPacket(packetVenus2Phone);

                             // update sensor data to TextView
                             UI_updateTextView();

                             // draw center of mass image
                             UI_drawImage();

                             // save CSV file
                             if (mSave_Flag==true) {
                                 UI_CSV_makeCSVdata();
                             }
                         }

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
    //  USER VARIABLES, FUNCTIONS
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //  Variables - UI Vies
    private EditText edtMessage;
    private TextView edtBatteryLevel;
    private TextView edtLastPacketTime;
    private TextView tv_PostureState;
    Space sp_COM_sero;
    Space sp_COC_left;
    Space sp_COC_right;
    Space sp_COC;
    ImageView iv_COM_bar;
    ImageView iv_COC;
    int nImageCOM_offset = 0;
    int nImageCOC_offset = 0;
    TextView tvFilePathName;


    //  Variables - Raw data
    private TextView [] tvaChairCells_Row0 = new TextView[PacketParser.def_CELL_COUNT_ROW0];
    private TextView [] tvaChairCells_Row1 = new TextView[PacketParser.def_CELL_COUNT_ROW1];
    private TextView [] tvaChairCells_Row2 = new TextView[PacketParser.def_CELL_COUNT_ROW2];
    PacketParser m_PacketParser = new PacketParser();


    //  Variables - Posture tag and time
    enum POSTURE_tag{
        POSTURE_NO_LOG,
        POSTURE_LEFT_BAD,
        POSTURE_LEFT,
        POSTURE_CENTER,
        POSTURE_RIGHT,
        POSTURE_RIGHT_BAD
    }
    POSTURE_tag m_PostureState;
    long m_PostureOriginTimeMS = elapsedRealtime();


    //  Variables - Save CSV
    Button mSave_Start;
    private boolean mSave_Flag = false;
    private String mPosition_Csv = null;
    Map<String, Object> hmap = null;

    /**
     *
     * @brief App의 화면의 컨트롤들 선언
     * @details App의 화면을 구성하는 컨트롤들을 불러와 각 변수에 선언하여 사용할수 있게 셌팅한다.
     * @param void
     * @return void
     * @throws
     */
    private void UI_onCreate(){
        edtBatteryLevel = (TextView) findViewById(R.id.TV_BatteryLevel);
        edtLastPacketTime = (TextView)findViewById(R.id.TV_CurTime);

        int cell_index = 0;
        tvaChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_0);
        tvaChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_1);
        tvaChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_2);
        tvaChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_3);
        tvaChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_4);
        tvaChairCells_Row0[cell_index] =(TextView)findViewById(R.id.TV_CHAIR_0_5);

        cell_index = 0;
        tvaChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_0);
        tvaChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_1);
        tvaChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_2);
        tvaChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_3);
        tvaChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_4);
        tvaChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_5);
        tvaChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_6);
        tvaChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_7);
        tvaChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_8);
        tvaChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_9);
        tvaChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_10);
        tvaChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_11);
        tvaChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_12);
        tvaChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_13);
        tvaChairCells_Row1[cell_index] =(TextView)findViewById(R.id.TV_CHAIR_1_14);

        cell_index = 0;
        tvaChairCells_Row2[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_2_0);
        tvaChairCells_Row2[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_2_1);
        tvaChairCells_Row2[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_2_2);
        tvaChairCells_Row2[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_2_3);
        tvaChairCells_Row2[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_2_4);
        tvaChairCells_Row2[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_2_5);
        tvaChairCells_Row2[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_2_6);
        tvaChairCells_Row2[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_2_7);
        tvaChairCells_Row2[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_2_8);
        tvaChairCells_Row2[cell_index] =(TextView)findViewById(R.id.TV_CHAIR_2_9);

        edtBatteryLevel.setText(String.format(getString(R.string.fmt_battery_level), 0));

        tv_PostureState = (TextView)findViewById(R.id.TV_POSTURE_LATERAL );
        sp_COM_sero = (Space)findViewById(R.id.space_COM);
        sp_COC_left = (Space)findViewById(R.id.space_COC_LEFT);
        sp_COC_right = (Space)findViewById(R.id.space_COC_RIGHT);
        iv_COM_bar = (ImageView)findViewById(R.id.iv_COM_SERO);
        iv_COC = (ImageView)findViewById(R.id.iv_coc);
        sp_COC = (Space)findViewById(R.id.space_COC);

        m_PostureState = POSTURE_tag.POSTURE_NO_LOG;

        mSave_Start = (Button)findViewById(R.id.Save_Start);
        tvFilePathName = (TextView)findViewById(R.id.TV_FILEPATH);
        m_Mode_TxtView = (TextView)findViewById(R.id.MODE_INFO);

    }

    /**
     *
     * @brief 변수에 value setting
     * @details 각 변수에 전달 받은 압력값을 Setting 하고 CSV FIle저장 유무에 따라 데이터를 따라 저장한다.
     * @param void
     * @return void
     * @throws
     */
    private void UI_updateTextView(){
        if(m_PacketParser.isPacketCompleted() == false)
            return;

        //mode check  M,S --> Venus   L,R --> Seat
        m_Mode_Info = PacketParser.Mode_Info;
        Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.ui_dip_switch_toast), Toast.LENGTH_SHORT);

        if(m_Mode_Info == "M" || m_Mode_Info == "S"){

            m_Mode_TxtView.setText(getString(R.string.ui_dip_switch_txt));
            //toast 보여주기

            if(toast_flag == 0 ){
                toast.show();
                toast_flag = toast_flag + 1;
            }


        }else if(m_Mode_Info == "L" || m_Mode_Info == "R"){

            m_Mode_TxtView.setText("");
            toast.cancel();
        }

        // last time packet received
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            String time_str = dateFormat.format(cal.getTime());
            edtLastPacketTime.setText(getString(R.string.fmt_time_last_packet) + time_str);
        }

        //  UI - battery level
        edtBatteryLevel.setText(String.format(getString(R.string.fmt_battery_level), PacketParser.getBatteryLevel()));

        //  UI - cell data and color
        {
            int nSensorValue = 0;
            int cell_index = 0;
            int row_index = 0;

            for (cell_index = 0 ; cell_index < PacketParser.def_CELL_COUNT_ROW0 ; cell_index++){
                nSensorValue = m_PacketParser.getSensorDataByCoord(row_index, cell_index);
                tvaChairCells_Row0[cell_index].setText(String.format("%d", nSensorValue));
                tvaChairCells_Row0[cell_index].setBackgroundColor(0x00FF0000 | (nSensorValue << 24) );
            }

            row_index = 1;
            for (cell_index = 0 ; cell_index < PacketParser.def_CELL_COUNT_ROW1 ; cell_index++){
                nSensorValue = m_PacketParser.getSensorDataByCoord(row_index, cell_index);
                tvaChairCells_Row1[cell_index].setText(String.format("%d", nSensorValue));
                tvaChairCells_Row1[cell_index].setBackgroundColor(0x00FF0000 | (nSensorValue << 24) );
            }

            row_index = 2;
            for (cell_index = 0 ; cell_index < PacketParser.def_CELL_COUNT_ROW2 ; cell_index++){
                nSensorValue = m_PacketParser.getSensorDataByCoord(row_index, cell_index);
                tvaChairCells_Row2[cell_index].setText(String.format("%d", nSensorValue));
                tvaChairCells_Row2[cell_index].setBackgroundColor(0x00FF0000 | (nSensorValue << 24) );
            }
        }
    }

    /**
     * @brief   현재의 자세를 태그로 저장한다.
     * @detail  추가로 현재의 자세를 취한 시점의 시간을 저장한다. 이 값은 현재의 자세를 유지할 경우, 몇초간 유지하고 있는 지를 UI에서 보여줄 때 사용된다.
     * @param   void
     * @return  void
     */
    private void setPostureState(POSTURE_tag posture_state) {
        if (m_PostureState != posture_state) {
            m_PostureOriginTimeMS = elapsedRealtime();
        }
        m_PostureState = posture_state;
    }

    /**
     * @brief   현재의 자세 태그를 반환한다.
     * @param   void
     * @return  자세 태그 (POSTURE_tag enum)
     */
    private POSTURE_tag getPostureState(){
        return m_PostureState;
    }

    /**
     * @brief   현재의 자세를 취한 시간 길이를 측정하여 반환한다.
     * @param   void
     * @return  시간 (초 단위)
     */
    private long getPostureElapsedSecond(){
        return (elapsedRealtime() - m_PostureOriginTimeMS) / 1000;
    }

    /**
     * @brief UI 화면에 무게 중심을 표시해주는 함수(COM : Center of Mess)
     * @details 센서에서 측정한 압력 값으로 COM 값을 계산한다. 이를 이미지로 화면에 뿌려준다.
     * @param
     * @return
     * @throws
     */
    private void UI_drawImage(){
        if(m_PacketParser.isSeatOccupied() == false){
            setPostureState(POSTURE_tag.POSTURE_NO_LOG);
            tv_PostureState.setText( "POSTURE : "+ String.format("EMPTY, %d sec", getPostureElapsedSecond()));

            UI_LEDWORK(0, 1);

            //contour, left edge : -7, right edge : 7\n center of contour : 0 \n center of mass : 0 \n lateral vector : 0.0"/>
            mlist = new ArrayList<String>();
            mlist.add(getString(R.string.ui_state_leftside)+ " = 0  // " + getString(R.string.ui_state_rightside) +" =  0");
            mlist.add(getString(R.string.ui_state_coc) + " =  0" );
            mlist.add(getString(R.string.ui_state_com)+ " =  0");
            //mlist.add("lateral Vector = " + (coord_com - ((coord_coc_left + coord_coc_right)/2)));
            mlist.add(getString(R.string.ui_state_vector) + "=  0");


            mAdapter = new CustomAdapter(this, 0, mlist);


            mStateList = (ListView)findViewById(R.id.TV_SEAT_LOG);

            mStateList.setAdapter(mAdapter);

            return;
        }

        //--------------------------------------------------------------------
        //  Finding coordinate back data to draw line
        //--------------------------------------------------------------------

        //  UI - calculate summation of Row1 textview array
        int ui_cell_width =  tvaChairCells_Row1[0].getMeasuredWidth();
        int ui_coc_area_size = ui_cell_width * PacketParser.def_CELL_COUNT_ROW1;



        //  UI - finding left margin of Row1 textview array
        int[] locations = new int[2];
        tvaChairCells_Row1[0].getLocationOnScreen(locations);
        int ui_left_of_most_left_cell = locations[0];
        int ui_left_margin = ui_coc_area_size / 2 + ui_left_of_most_left_cell;

        //  UI - half of imageview "COM, coc"
        nImageCOM_offset = iv_COM_bar.getMeasuredWidth() / 2;
        nImageCOC_offset = iv_COC.getMeasuredWidth()/2;

        //ui_coc_com_layout.setLayoutParams(new RelativeLayout(986, 80));

        //  find COM coordinate
        float proportion_com_x  = m_PacketParser.getLateralCOM_Row1();
        int ui_com_x            = ui_left_margin + (int)(ui_cell_width * proportion_com_x) - nImageCOM_offset;

        //ui_com_x = 100;

        //  find COC left coordinate
        float proportion_coc_left   = m_PacketParser.getLateralCOC_left_Row1();
        int ui_coc_left             = ui_left_margin + (int)(ui_cell_width * proportion_coc_left) - 20;

        //  find COC right coordinate
        float proportion_coc_right  = m_PacketParser.getLateralCOC_right_Row1();
        int ui_coc_right            = ui_left_margin + (int)(ui_cell_width * proportion_coc_right) - 10;

        int ui_coc_x = ((ui_coc_left + ui_coc_right) / 2) - nImageCOC_offset + 14;

        //ui_coc_x = 100;

        UI_list(proportion_com_x, proportion_coc_left,proportion_coc_right );


        //착석 방향에 따른 led 기능
        UI_LEDWORK(proportion_com_x,0);


        //--------------------------------------------------------------------
        //  Draw Images - COM, Left COC, Right COC
        //--------------------------------------------------------------------

        //  UI draw COM image
        {

            //  calculate COM - lateral
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)sp_COM_sero.getLayoutParams();
            params.setMargins(0, 0, ui_com_x, 0); //substitute parameters for left, top, right, bottom
            sp_COM_sero.setLayoutParams(params);


            if(proportion_com_x < -1.0F){
                tv_PostureState.setText(String.format("LEFT - BAD (%1.3f), %d sec", proportion_com_x, getPostureElapsedSecond()));
                setPostureState(POSTURE_tag.POSTURE_LEFT_BAD);

            }
            else if(proportion_com_x < -0.4F){
                tv_PostureState.setText(String.format("LEFT (%1.3f), %d sec", proportion_com_x, getPostureElapsedSecond()));
                setPostureState(POSTURE_tag.POSTURE_LEFT);
            }
            else if(proportion_com_x < 0.4F){
                tv_PostureState.setText(String.format("CENTER (%1.3f), %d sec", proportion_com_x, getPostureElapsedSecond()));
                setPostureState(POSTURE_tag.POSTURE_CENTER);
            }
            else if(proportion_com_x < 1.0F){
                tv_PostureState.setText(String.format("RIGHT (%1.3f), %d sec", proportion_com_x, getPostureElapsedSecond()));
                setPostureState(POSTURE_tag.POSTURE_RIGHT);
            }
            else {
                tv_PostureState.setText(String.format("RIGHT - BAD (%1.3f), %d sec", proportion_com_x, getPostureElapsedSecond()));
                setPostureState(POSTURE_tag.POSTURE_RIGHT_BAD);
            }
        }

        //  UI draw left line of COC
        {
            //  calculate COC - left
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) sp_COC_left.getLayoutParams();
            params.setMargins(0, 0, ui_coc_left, 0); //substitute parameters for left, top, right, bottom
            sp_COC_left.setLayoutParams(params);
        }

        //  UI draw right line of COC
        {
            //  calculate COC - right
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)sp_COC_right.getLayoutParams();
            params.setMargins(0, 0, ui_coc_right, 0); //substitute parameters for left, top, right, bottom
            sp_COC_right.setLayoutParams(params);
        }
        {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)sp_COC.getLayoutParams();
            params.setMargins(0, 0, ui_coc_x, 0); //subst.itute parameters for left, top, right, bottom
            sp_COC.setLayoutParams(params);
        }
    }

    private void UI_CSV_makeCSVdata() {
        if(m_PacketParser.isPacketCompleted() == false)
            return;

        //  UI - cell data and color
        {
            int cell_index = 0;

            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat sdfNow = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String formatDate = sdfNow.format(date);

            hmap = new HashMap<String, Object>();

            //  Row 0
            for (cell_index = 0 ; cell_index < PacketParser.def_CELL_COUNT_ROW0 ; cell_index++){
                String nPoint = tvaChairCells_Row0[cell_index].getText().toString();

                if(mPosition_Csv==null){
                    mPosition_Csv = formatDate +"," +  "0_" + cell_index + "," + nPoint + ",";

                }else{

                    if(cell_index==0){
                        mPosition_Csv =  mPosition_Csv + formatDate +"," +  "0_" + cell_index + "," + nPoint + ",";

                    }else{
                        mPosition_Csv =  mPosition_Csv  + "0_" + cell_index + "," + nPoint + ",";
                    }
                }
            }

            //  Row 1
            for (cell_index = 0 ; cell_index < PacketParser.def_CELL_COUNT_ROW1 ; cell_index++){
                String nPoint1 = tvaChairCells_Row1[cell_index].getText().toString();

                mPosition_Csv = mPosition_Csv + "1_" + cell_index + "," + nPoint1 + ",";
            }

            //  Row 2
            for (cell_index = 0 ; cell_index < PacketParser.def_CELL_COUNT_ROW2 ; cell_index++){
                String nPoint2 = tvaChairCells_Row2[cell_index].getText().toString();

                if(cell_index < 9 ){
                    mPosition_Csv = mPosition_Csv + "2_" + cell_index + "," + nPoint2 + ",";

                }else{
                    mPosition_Csv = mPosition_Csv + "2_" + cell_index + "," + nPoint2 + "\r\n";
                }
            }

        }
    }

    /**
     *
     * @brief To Save Sitting Data, Make CSV File
     * @details IF you Click the Button, Check the Button String. if the String is'Start Save' then this App Start saving Date and when you click the Button again, Make CSV File
     * @param
     * @return
     * @throws
     */

    public void UI_onClickSaveCSV(View v) {
        //  if : toggle - start save
        if (mSave_Flag == false) {
            mSave_Flag = true;
            mSave_Start.setEnabled(true);
            mSave_Start.setText(getString(R.string.fmt_stop_save));
        }
        //  else : toggle - stop save
        else {
            mSave_Flag = false;
            //파일 생성
            UI_saveCSVFile();
            mSave_Start.setEnabled(true);
            mSave_Start.setText(getString(R.string.fmt_start_save));
            Toast.makeText(this, getString(R.string.fmt_save_msg), Toast.LENGTH_SHORT).show();
        }
    }

    private void UI_LEDWORK(float com_led, int led_flag){

        String msg_led[] = new String[2];

        if(led_flag == 0){

            if(com_led < - 0.4){
                //왼쪽기울림 -- left led on

                msg_led[0] = "turn on left";
                msg_led[1] = "turn off right";

            }else if(com_led >= -0.4 && com_led <= 0.4){
                //정상착석  - left/ right led on

                msg_led[0] = "turn on left";
                msg_led[1] = "turn on right";

            }else if(com_led > 0.4){
                //오른쪽 기울림 - right led on
                msg_led[0] = "turn off left";
                msg_led[1] = "turn on right";

            }
        }else if(led_flag == 1){

            msg_led[0] = "turn off left";
            msg_led[1] = "turn off right";
        }

        int msg_cnt;
        String send_msg = null;

        for(msg_cnt = 0;  msg_cnt < 2; msg_cnt++){

            send_msg = msg_led[msg_cnt];

            byte[] value;
            try {
                //send data to service
                value = send_msg.getBytes("UTF-8");
                mService.writeRXCharacteristic(value);
                edtMessage.setText("");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    private void UI_list(float coord_com, float coord_coc_left, float coord_coc_right){

        //contour, left edge : -7, right edge : 7\n center of contour : 0 \n center of mass : 0 \n lateral vector : 0.0"/>
        mlist = new ArrayList<String>();
        mlist.add(getString(R.string.ui_state_leftside)+ " = " + String.format("%1.1f",coord_coc_left) +"//" + getString(R.string.ui_state_rightside)+ " = " + coord_coc_right);
        mlist.add(getString(R.string.ui_state_coc) + " = " + String.format("%1.3f",((coord_coc_left + coord_coc_right)/2)));
        mlist.add(getString(R.string.ui_state_com) +" = " + String.format("%1.3f",coord_com));
        //mlist.add("lateral Vector = " + (coord_com - ((coord_coc_left + coord_coc_right)/2)));
        vector = (coord_com - ((coord_coc_left + coord_coc_right)/2));
        mlist.add(getString(R.string.ui_state_vector) + " = " + String.format("%1.3f", vector));

        //mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mlist);

        mAdapter = new CustomAdapter(this, 0, mlist);


        mStateList = (ListView)findViewById(R.id.TV_SEAT_LOG);

        mStateList.setAdapter(mAdapter);
    }

    private class CustomAdapter extends ArrayAdapter<String>{

        public CustomAdapter(Context context, int textViewResourceId, ArrayList<String> objects) {
            super(context, textViewResourceId, objects);
            //this.mlist = objects;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.state_list, null);
            }

            // ImageView 인스턴스
            ImageView imageView = (ImageView)v.findViewById(R.id.imageView);

            // 리스트뷰의 아이템에 이미지를 변경한다.
            if(mlist.get(position).substring(0, 7).equals("Contour"))
                imageView.setImageResource(R.drawable.total);
            else if(mlist.get(position).substring(0, 17).equals("Center of Contour"))
                imageView.setImageResource(R.drawable.coc);
            else if(mlist.get(position).substring(0, 14).equals("Center of Mass"))
                imageView.setImageResource(R.drawable.com);
            else if(mlist.get(position).substring(0, 7).equals("Lateral")){
                //imageView.setImageResource(R.drawable.vector_center);
                if(  - 0.7< vector && vector < -0.3){

                    imageView.setImageResource(R.drawable.vector_left);

                }else if(vector <= - 0.7){

                    imageView.setImageResource(R.drawable.vector_big_left);

                }else if( 0.3 < vector && vector < 0.7 ){

                    imageView.setImageResource(R.drawable.vector_right);

                }else if(vector >= 0.7){

                    imageView.setImageResource(R.drawable.vector_big_right);
                }else{

                    imageView.setImageResource(R.drawable.vector_center);
                }
            }
           /* else if(mlist.get(position).substring(0, 7).equals("Lateral")){

            }*/

            TextView textView = (TextView)v.findViewById(R.id.textView);
            textView.setText(mlist.get(position));

            final String text = mlist.get(position);

            //tvFilePathName.setText(text);

            return v;
        }
    }

    /**
     *
     * @brief save CSV File
     * @details When you Click 'Save Stop' Button, this Function count the now date and create CSV File. the File name is TODAY.csv
     * @param none
     * @return none
     * @throws check the File existed and Direct, Create CSV File
     */
    private void UI_saveCSVFile(){

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String formatDate = sdfNow.format(date);

        try{
            File file = new File("/mnt/sdcard/Download/" + formatDate + ".csv");
            if(!file.exists()){
                file = new File("/mnt/sdcard/Download/" + formatDate + ".csv");
                file.createNewFile();
            }

            PrintWriter csv_writer;
            csv_writer = new  PrintWriter(new FileWriter(file,true));

            csv_writer.print(mPosition_Csv);
            //csv_writer.print("\r\n");
            csv_writer.close();

            tvFilePathName.setText("File path : /mnt/sdcard/Download/" + formatDate + ".csv");
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

}