package com.example.bleposprinter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSIONS = 123;
    private static final int REQUEST_ENABLE_BLUETOOTH = 456;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDeviceAdapter deviceAdapter;

    private OutputStream outputStream;
    private Button btnImprimir;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnImprimir = findViewById(R.id.btn_imprimir);
        btnImprimir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printScreen();
            }
        });

        checkPermissions();

        // Initialize BluetoothAdapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Initialize RecyclerView and its adapter
        RecyclerView recyclerView = findViewById(R.id.deviceRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        deviceAdapter = new BluetoothDeviceAdapter(getApplicationContext(), new ArrayList<>(), new BluetoothDeviceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BluetoothDeviceModel device) {
                // Handle item click, e.g., connect to the selected Bluetooth device
                connectToDevice(device);
            }
        });
        recyclerView.setAdapter(deviceAdapter);

        // Check if Bluetooth is supported on the device
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // Bluetooth is supported and permissions are granted
            startBluetoothDiscovery();
        }
    }

    private void checkPermissions() {
        String[] permissions = {
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
        };

        // add bluetooth_scan permission for android 12
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions = new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.BLUETOOTH_ADVERTISE,
            };
        }

        // Check if all permissions are granted
        if (!PermissionUtil.checkPermissions(this, permissions)) {
            // Request permissions
            requestPermissions(permissions, REQUEST_CODE_PERMISSIONS);

//            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 2);
        }


    }

    private void startBluetoothDiscovery() {
        // Check if Bluetooth is enabled, and enable it if not
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Bluetooth scan permission not granted", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
        } else {
            // Start Bluetooth discovery
            discoverBluetoothDevices();
        }
    }

    private void discoverBluetoothDevices() {
        // Start discovery and set a BroadcastReceiver to handle discovered devices
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothReceiver, filter);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Bluetooth scan permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        bluetoothAdapter.startDiscovery();
    }

    private void connectToDevice(BluetoothDeviceModel device) {
        Toast.makeText(this, "Connecting to " + device.getName(), Toast.LENGTH_SHORT).show();

        BluetoothDevice blDevice = bluetoothAdapter.getRemoteDevice(device.getAddress());
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Bluetooth scan permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            ParcelUuid[] uuids = blDevice.getUuids();
            UUID uuid = uuids[1].getUuid();
            BluetoothSocket socket = blDevice.createRfcommSocketToServiceRecord(uuid);
            socket.connect();

            outputStream = socket.getOutputStream();
            Toast.makeText(this, "Connected to " + device.getName(), Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Toast.makeText(this, "Failed to connect to " + device.getName() + " | " + ex.getMessage(), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    private void sendTest() throws IOException {
        String textToPrint = "Hello, World!\n";
        byte[] bytes = textToPrint.getBytes("UTF-8");
        outputStream.write(bytes);

        byte[] cutCommand = {0x1D, 0x56, 0x42, 0x00};
        outputStream.write(cutCommand);
    }

    private void printScreen() {
        Bitmap viewImage = new PrintHelper(this).printReciboTest();
        printPhoto(viewImage);
    }

    private List<byte[]> convertBitmapArrayToBinary(Bitmap[] bitmapArray) {
        List<byte[]> binaryArray = new ArrayList<>();

        for (int i = 0; i < bitmapArray.length; i++) {
            Bitmap bitmap = bitmapArray[i];

            // Convert Bitmap to byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();


            binaryArray.add(byteArray);
        }

        return binaryArray;
    }

    public void printPhoto(Bitmap bmp) {
        try {
            if (bmp != null) {
//                if (bmp.getHeight() > 400) {
//                    Bitmap[] bitmaps = BitmapUtils.splitBitmap(bmp);
//                    List<byte[]> bytes = Utils.decodeBitmaps(bitmaps);
//
//                    for (byte[] b : bytes) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                try {
//                                    Thread.sleep(1000);
//                                    printText(b);
//                                } catch (Exception ex) {
//                                    ex.printStackTrace();
//                                }
//                            }
//                        });
//                    }
//                } else {
//                    byte[] command = Utils.decodeBitmap(bmp);
//                    printText(command);
//                }

                byte[] command = Utils.decodeBitmap(bmp);
                printText(command);
                printNewLine();
            } else {
                Log.e("Print Photo error", "the file isn't exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void printNewLine() {
        try {
            outputStream.write(PrinterCommands.FEED_LINE);
            outputStream.write(PrinterCommands.CUT_COMMAND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printText(byte[] msg) {
        try {
            if (outputStream == null) {
                Toast.makeText(this, "outputStream is null", Toast.LENGTH_SHORT).show();
                return;
            }

            if (msg == null) {
                Toast.makeText(this, "msg is null", Toast.LENGTH_SHORT).show();
                return;
            }

            // Print normal text
            outputStream.write(msg);
            printNewLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // A new device has been discovered
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    // Add the device to the list
                    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(context, "Bluetooth scan permission not granted", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    BluetoothDeviceModel deviceModel = new BluetoothDeviceModel(device.getName(), device.getAddress());
                    deviceAdapter.addDevice(deviceModel);
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the Bluetooth receiver when the activity is destroyed
        unregisterReceiver(bluetoothReceiver);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            // Check if all permissions are granted
            if (!PermissionUtil.checkPermissions(this, permissions)) {
                // Some permissions were denied, handle accordingly
                // show error message
                Toast.makeText(this, "Permissions denied.", Toast.LENGTH_LONG).show();
            }
        }
    }
}