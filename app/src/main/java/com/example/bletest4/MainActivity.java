package com.example.bletest4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;

import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;

public class MainActivity extends AppCompatActivity {
    private BluetoothClient mClient;
    private BluetoothDevice bleDevice;
    private String mac;
    private final BleConnectStatusListener mBleConnectStatusListener = new BleConnectStatusListener() {

        @Override
        public void onConnectStatusChanged(String mac, int status) {
            Log.v("brad", "debug");
            if (status == STATUS_CONNECTED) {
                Log.v("brad", "connect OK");
            } else if (status == STATUS_DISCONNECTED) {
                Log.v("brad", "disconnect");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    12);
        }else{
            init();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        init();
    }

    private void init(){
        mClient = new BluetoothClient(this);
    }

    public void scanDevices(View view){
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(3000, 3)   // 先扫BLE设备3次，每次3s
                .searchBluetoothClassicDevice(5000) // 再扫经典蓝牙5s
                .searchBluetoothLeDevice(2000)      // 再扫BLE设备2s
                .build();
        mClient.search(request, new SearchResponse() {
            @Override
            public void onSearchStarted() {
                Log.v("brad", "start...");
            }

            @Override
            public void onDeviceFounded(SearchResult device) {
                BluetoothDevice btdevice = device.device;
                //String mac = device.getAddress();


                if (device.getName() != null && device.getName().contains("Brad")){
                    mClient.stopSearch();
                    bleDevice = btdevice;
                    mac = device.getAddress();
                    Log.v("brad", "got it:" + mac);
                }


            }

            @Override
            public void onSearchStopped() {
                Log.v("brad", "stop");
            }

            @Override
            public void onSearchCanceled() {
                Log.v("brad", "cancel");
            }
        });


    }

    public void stopScanDevices(View view) {
        mClient.stopSearch();
    }

    public void connectDevices(View view) {
        BleConnectOptions options = new BleConnectOptions.Builder()
                .setConnectRetry(3)   // 连接如果失败重试3次
                .setConnectTimeout(30000)   // 连接超时30s
                .setServiceDiscoverRetry(3)  // 发现服务如果失败重试3次
                .setServiceDiscoverTimeout(20000)  // 发现服务超时20s
                .build();
        Log.v("brad", "debug2:" + mac);
        mClient.registerConnectStatusListener(mac, mBleConnectStatusListener);
        mClient.connect(mac, new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile data) {
                Log.v("brad", "code = " + code);
            }
        });
    }
    public void disconnectDevices(View view) {
        mClient.disconnect(mac);
    }
}
