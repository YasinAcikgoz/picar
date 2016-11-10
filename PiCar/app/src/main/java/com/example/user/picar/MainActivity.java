package com.example.user.picar;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ExpandedMenuView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private TextView y_axis;
    Socket socket;
    PrintWriter printWriter_socket;
    volatile boolean isConnected = false;
    volatile boolean sendData = true;
    volatile char direction = 'w';
    String ip;
    int port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.input_dialog, null);
        final EditText editText_ip = (EditText) layout.findViewById(R.id.ip);
        final EditText editText_port = (EditText) layout.findViewById(R.id.port);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layout);
        builder.setPositiveButton("CONNECT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setMessage("Initilizing...");
                progressDialog.show();

                ip = editText_ip.getText().toString();
                port = Integer.parseInt(editText_port.getText().toString());
                SocketCreator socketCreator = new SocketCreator();
                socketCreator.execute((Void) null);

                while (!isConnected) {
                    try {
                        Thread.sleep(100); //ms
                    } catch (Exception e) {
                        break;
                    }
                }
                progressDialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();

        y_axis = (TextView) findViewById(R.id.y_axis);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);

        try {

            ((Button) findViewById(R.id.button_back)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(!sendData)

                        sendData = true;

                    direction = 's';
                }
            });
            ((Button) findViewById(R.id.button_forward)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(!sendData)

                        sendData = true;

                    direction = 'w';
                }
            });
            ((Button) findViewById(R.id.button_stop)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(sendData)

                        sendData = false;

                    printWriter_socket.print("x");
                    printWriter_socket.flush();

                }
            });

            ((Button) findViewById(R.id.button_exit)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(printWriter_socket != null) {

                        printWriter_socket.print("e");
                        printWriter_socket.flush();
                        printWriter_socket.close();
                        printWriter_socket = null;
                    }

                    System.exit(0);
                }
            });



        } catch (Exception e) {

            Log.e("Button init", e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (sensorManager != null)
            sensorManager.unregisterListener(this);

        if (printWriter_socket != null) {

            printWriter_socket.print("e");
            printWriter_socket.flush();
            printWriter_socket.close();
        }

        try {
            socket.close();
        } catch (IOException e) {
            Log.e("Closing socket", e.getMessage());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && socket != null && socket.isConnected()) {

            int data = (int) (event.values[1] / 2);

            if (data < 0)

                data = 5 - data;

            if(data == 5 || data == 10)

                data--;

            if(sendData) {

                y_axis.setText(String.format("%c_%d", direction, data));
                printWriter_socket.print(String.format("%c_%d", direction, data));
                printWriter_socket.flush();
            }
        }
    }

    public class SocketCreator extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() { super.onPreExecute(); }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                socket = new Socket(ip, port);
                printWriter_socket = new PrintWriter(socket.getOutputStream());
                isConnected = true;
            } catch (Exception e) {

                Log.e("Thread", e.getMessage());
                System.exit(0);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled(Void result) {
            super.onCancelled(result);
        }
    }
}
