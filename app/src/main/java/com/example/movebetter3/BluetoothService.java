//import android.app.Service;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothSocket;
//import android.content.Intent;
//import android.os.Binder;
//import android.os.IBinder;
//import android.util.Log;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.UUID;
//
//public class BluetoothService extends Service {
//    private static final String TAG = "BluetoothService";
//    private final IBinder binder = new LocalBinder();
//    private BluetoothAdapter bluetoothAdapter;
//    private BluetoothSocket bluetoothSocket;
//    private InputStream inputStream;
//    private boolean isConnected = false;
//
//    public class LocalBinder extends Binder {
//        BluetoothService getService() {
//            return BluetoothService.this;
//        }
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return binder;
//    }
//
//    public void connectToDevice(String address) {
//        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
//        try {
//            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
//            bluetoothSocket.connect();
//            inputStream = bluetoothSocket.getInputStream();
//            isConnected = true;
//            new ReadThread().start();
//        } catch (IOException e) {
//            Log.e(TAG, "Unable to connect to device", e);
//            isConnected = false;
//        }
//    }
//
//    public boolean isConnected() {
//        return isConnected;
//    }
//
//    public void disconnect() {
//        try {
//            if (inputStream != null) inputStream.close();
//            if (bluetoothSocket != null) bluetoothSocket.close();
//            isConnected = false;
//        } catch (IOException e) {
//            Log.e(TAG, "Unable to disconnect", e);
//        }
//    }
//
//    private class ReadThread extends Thread {
//        @Override
//        public void run() {
//            byte[] buffer = new byte[1024];
//            int bytes;
//            while (isConnected) {
//                try {
//                    bytes = inputStream.read(buffer);
//                    String data = new String(buffer, 0, bytes);
//                    Intent intent = new Intent("BluetoothData");
//                    intent.putExtra("data", data);
//                    sendBroadcast(intent);
//                } catch (IOException e) {
//                    Log.e(TAG, "Error reading from Bluetooth device", e);
//                    break;
//                }
//            }
//        }
//    }
//}
