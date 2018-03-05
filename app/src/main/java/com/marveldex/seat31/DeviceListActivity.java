
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
package com.marveldex.seat31;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @details 연결 할  블루투스 장치를 관리하는 class
 * @author Marveldex
 * @date 2017-03-17
 * @version 0.0.1
 * @li list1
 * @li list2
 * @li list3
 * @li list4 asddxfsdfsdf
 *
 */

public class DeviceListActivity extends Activity {
    private BluetoothAdapter m_BluetoothAdapter;

   // private BluetoothAdapter mBtAdapter;
    private TextView m_EmptyList;
    public static final String TAG = "DeviceListActivity";
    
    List<BluetoothDevice> m_DeviceList;
    private DeviceAdapter m_DeviceAdapter;
    private ServiceConnection onService = null;
    Map<String, Integer> m_DevRssiValues;
    private static final long SCAN_PERIOD = 10000; //scanning for 10 seconds
    private Handler m_Handler;
    private boolean m_Scanning;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);
        setContentView(R.layout.device_list);
        android.view.WindowManager.LayoutParams layoutParams = this.getWindow().getAttributes();
        layoutParams.gravity=Gravity.TOP;
        layoutParams.y = 200;
        m_Handler = new Handler();
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        m_BluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (m_BluetoothAdapter == null) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        populateList();
        m_EmptyList = (TextView) findViewById(R.id.empty);
        Button cancelButton = (Button) findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            	if (m_Scanning==false) scanLeDevice(true); 
            	else finish();
            }
        });

    }

    /**
     *
     * @brief 연결할 장비 리스트를 관리하는 함수
     * @details 연결 할 장비의 ArrayList, adapter, hashmap를 생성하고  연결 가능한 블루투스 디바이스의 정보를 가져와 ListView에 Setting한다.
     * @param
     * @return
     * @throws
     */
    private void populateList() {
        /* Initialize device list container */
        Log.d(TAG, "populateList");
        m_DeviceList = new ArrayList<BluetoothDevice>();
        m_DeviceAdapter = new DeviceAdapter(this, m_DeviceList);
        m_DevRssiValues = new HashMap<String, Integer>();

        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(m_DeviceAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

           scanLeDevice(true);

    }

    /**
     *
     * @brief 블루투스 디바이스를 스켄한다.
     * @details 구분자를 통해 블루투스의 장치 scan의 여부를 판단하고 블루투스 장치를 스캔하거나 스켄을 중단한다. 해당 구분자를 통해 장치 List화면에서 Button의 Text값을 변경해준다.
     * @param
     * @return
     * @throws
     */
    private void scanLeDevice(final boolean enable) {
        final Button cancelButton = (Button) findViewById(R.id.btn_cancel);
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            m_Handler.postDelayed(new Runnable() {
                @Override
                public void run() {
					m_Scanning = false;
                    m_BluetoothAdapter.stopLeScan(mLeScanCallback);
                        
                    cancelButton.setText(R.string.scan);

                }
            }, SCAN_PERIOD);

            m_Scanning = true;
            m_BluetoothAdapter.startLeScan(mLeScanCallback);
            cancelButton.setText(R.string.cancel);
        } else {
            m_Scanning = false;
            m_BluetoothAdapter.stopLeScan(mLeScanCallback);
            cancelButton.setText(R.string.scan);
        }

    }

    /**
     *
     * @brief 디바이스 정보를 Setting 하는 Adapter
     * @details
     * @param
     * @return
     * @throws
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	
                              addDevice(device,rssi);
                }
            });
        }
    };

    /**
     *
     * @brief BluetoothAdapter.LeScanCallback 에서 호출되는 함수
     * @details List의 블루투스 장치들의 Mac? 주소를 통해 장치를 찾고 해당 디바이스의 정보를 함수에 저장
     * @param
     * @return
     * @throws
     */
    private void addDevice(BluetoothDevice device, int rssi) {
        boolean deviceFound = false;

        for (BluetoothDevice listDev : m_DeviceList) {
            if (listDev.getAddress().equals(device.getAddress())) {
                deviceFound = true;
                break;
            }
        }
        
        
        m_DevRssiValues.put(device.getAddress(), rssi);
        if (!deviceFound) {
        	m_DeviceList.add(device);
            m_EmptyList.setVisibility(View.GONE);
                 	
        	

            
            m_DeviceAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
       
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    @Override
    public void onStop() {
        super.onStop();
        m_BluetoothAdapter.stopLeScan(mLeScanCallback);
    
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        m_BluetoothAdapter.stopLeScan(mLeScanCallback);
        
    }


    /**
     *
     * @brief    블루투스 리스트에서 장비를 선택했을 시 동작하는 함수
     * @details 선택된 디바이스 정보를 Setting
     * @param
     * @return
     * @throws
     */
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
    	
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice device = m_DeviceList.get(position);
            m_BluetoothAdapter.stopLeScan(mLeScanCallback);
  
            Bundle b = new Bundle();
            b.putString(BluetoothDevice.EXTRA_DEVICE, m_DeviceList.get(position).getAddress());

            Intent result = new Intent();
            result.putExtras(b);
            setResult(Activity.RESULT_OK, result);
            finish();
        	
        }
    };


    
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }


    /**
     *
     * @details
     * @author Marveldex
     * @date 2017-03-17
     * @version 0.0.1
     * @li list1
     * @li list2
     *
     */

    class DeviceAdapter extends BaseAdapter {
        Context context;
        List<BluetoothDevice> devices;
        LayoutInflater inflater;

        public DeviceAdapter(Context context, List<BluetoothDevice> devices) {
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.devices = devices;
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup vg;

            if (convertView != null) {
                vg = (ViewGroup) convertView;
            } else {
                vg = (ViewGroup) inflater.inflate(R.layout.device_element, null);
            }

            BluetoothDevice device = devices.get(position);
            final TextView tvadd = ((TextView) vg.findViewById(R.id.address));
            final TextView tvname = ((TextView) vg.findViewById(R.id.name));
            final TextView tvpaired = (TextView) vg.findViewById(R.id.paired);
            final TextView tvrssi = (TextView) vg.findViewById(R.id.rssi);
            final TextView tvlastdevice = (TextView) vg.findViewById(R.id.lastdevice);

            tvrssi.setVisibility(View.VISIBLE);
            byte rssival = (byte) m_DevRssiValues.get(device.getAddress()).intValue();
            if (rssival != 0) {
                tvrssi.setText("Rssi = " + String.valueOf(rssival));
            }

            tvname.setText(device.getName());
            tvadd.setText(device.getAddress());
            //최근 연결 디바이스와 비교하여 표시
            SharedPreferences pref = getSharedPreferences("MacAddr", Activity.MODE_PRIVATE);
            String lastAddr  = pref.getString("MacAddr", "00");

            if(lastAddr.equals(device.getAddress())){
                tvlastdevice.setTextColor(Color.BLUE);
                tvlastdevice.setText("Last Device");

            }else{
                tvlastdevice.setText("");
            }


            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                Log.i(TAG, "device::"+device.getName());
                tvname.setTextColor(Color.WHITE);
                tvadd.setTextColor(Color.WHITE);
                tvpaired.setTextColor(Color.GRAY);
                tvpaired.setVisibility(View.VISIBLE);
                tvpaired.setText(R.string.paired);
                tvrssi.setVisibility(View.VISIBLE);
                tvrssi.setTextColor(Color.WHITE);
                
            } else {
                tvname.setTextColor(Color.WHITE);
                tvadd.setTextColor(Color.WHITE);
                tvpaired.setVisibility(View.GONE);
                tvrssi.setVisibility(View.VISIBLE);
                tvrssi.setTextColor(Color.WHITE);
            }
            return vg;
        }
    }
    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
