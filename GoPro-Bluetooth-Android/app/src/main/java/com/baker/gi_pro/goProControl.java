package com.baker.gi_pro;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;


public class goProControl extends ActionBarActivity implements View.OnTouchListener, View.OnClickListener{

    //la position en tilt par défaut
    private long posTilt = 0;
    //la position en pan par défaut
    private long posPan = 0;
    //le nombre à six chiffres à envoyer en bluetooth
    private long finalnb = 0;

    TextView tilt = null;
    TextView pan = null;

    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;

    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_pro_control);

        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); //receive the address of the bluetooth device

        //récupération des vues
        Button raz = (Button) findViewById(R.id.raz);
        Button tiltDown = (Button)findViewById(R.id.tiltDown);
        Button tiltUp = (Button)findViewById(R.id.tiltUp);
        Button panLeft = (Button)findViewById(R.id.panLeft);
        Button panRight = (Button)findViewById(R.id.panRight);
        Button dis = (Button)findViewById(R.id.button2);
        Button bpan = (Button)findViewById(R.id.bpan);
        Button btilt = (Button)findViewById(R.id.btilt);
        SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar);
        SeekBar seekBar1 = (SeekBar)findViewById(R.id.seekBar2);

        tilt = (TextView)findViewById(R.id.tilt);
        pan = (TextView)findViewById(R.id.pan);

        new ConnectBT().execute(); //Call the class to connect

        //mis en plance des listeners
        tiltUp.setOnClickListener((android.view.View.OnClickListener) this);
        tiltDown.setOnClickListener((android.view.View.OnClickListener) this);
        panLeft.setOnClickListener((android.view.View.OnClickListener) this);
        panRight.setOnClickListener((android.view.View.OnClickListener) this);
        raz.setOnClickListener((android.view.View.OnClickListener) this);
        dis.setOnClickListener((android.view.View.OnClickListener) this);
        bpan.setOnClickListener((android.view.View.OnClickListener) this);
        btilt.setOnClickListener((android.view.View.OnClickListener) this);
        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                posPan =5*progresValue;
                tilt.setText("Pan : " + posPan);
                pan.setText("Tilt : " + posTilt);
                finalnb = progress;
                if (btSocket!=null)
                {
                    try
                    {
                        btSocket.getOutputStream().write((int) (finalnb));
                    }
                    catch (IOException e)
                    {
                        msg("Error");
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                posTilt = 5*progresValue;
                tilt.setText("Pan : " + posPan);
                pan.setText("Tilt : " + posTilt);
                finalnb = progress +100;
                if (btSocket!=null)
                {
                    try
                    {
                        btSocket.getOutputStream().write((int) (finalnb));
                    }
                    catch (IOException e)
                    {
                        msg("Error");
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public boolean onTouch(View view, MotionEvent event){
        return true;
    }

    @Override
    public void onClick(View view){
        Button bouton = (Button)view;
        switch (bouton.getId()){
            case R.id.tiltUp:
                if(posTilt<180){
                    posTilt += 5;
                    finalnb = posTilt/5 + 100;
                }
                break;
            case R.id.tiltDown:
                if(posTilt>0){
                    posTilt -= 5;
                    finalnb = posTilt/5 + 100;
                }
                break;
            case R.id.panLeft:
                if(posPan>0){
                    posPan -= 5;
                    finalnb = posPan/5;
                }
                break;
            case R.id.panRight:
                if(posPan<180){
                    posPan += 5;
                    finalnb = posPan/5;
                }
                break;
            case R.id.raz:
                posPan = 90;
                posTilt = 90;
                finalnb = 255;
                break;
            case R.id.bpan:
                posTilt = 90;
                posPan = 180;
                finalnb = 253;
                break;
            case R.id.btilt:
                posPan = 90;
                posTilt = 180;
                finalnb = 254;
                break;
            case R.id.button2:
                if (btSocket!=null) //If the btSocket is busy
                {
                    try
                    {
                        btSocket.close(); //close connection
                    }
                    catch (IOException e)
                    { msg("Error");}
                }
                finish(); //return to the first layout
        }

        tilt.setText("Pan : " + posPan);
        pan.setText("Tilt : " + posTilt);
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write((int) (finalnb));
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }

    }

    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout

    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(goProControl.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_go_pro_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
