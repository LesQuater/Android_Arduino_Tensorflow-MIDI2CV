package com.example.alan.midiusbkeras;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {


    //UsbDeviceConnection usbConnection;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    UsbDevice device;
    UsbDeviceConnection connection;
    UsbManager usbManager;
    UsbSerialDevice serialPort;
    PendingIntent pendingIntent;


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) {
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERMISSION NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                onClickStart(startButton);
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                //can add something to close the connection

            }
        };
    };

    int octave = 0;
    int channel = 1;

    private static TextView displayView;
    private static EditText inputView;
    private static SeekBar velocity;
    private static TextView PrintVelocity;
    private static SeekBar ondeSeek;
    private static TextView PrintOnde;
    private static SeekBar attack;
    private static TextView PrintAttack;
    private static SeekBar decay;
    private static TextView PrintDecay;
    private static Button startButton;
    private static Button sendButton;
    private static Button toggleButton;
    int progressValueDecay = 0;
    int progressValueOnde = 0;
    int progressValueVelocity = 0;
    int progressValueAttack = 0;
    boolean isLedON = false;
    boolean isSerialStarted = false;

    private TextView mBluetoothStatus;
    private TextView mReadBuffer;
    private Button mScanBtn;
    private Button mOffBtn;
    private Button mListPairedDevicesBtn;
    private Button mDiscoverBtn;
    private BluetoothAdapter mBTAdapter;
    private BluetoothSocket mBTSocket = null;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private ListView mDevicesListView;
    private CheckBox mLED1;
    private Handler mHandler; // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data

    byte[] ControlChanges = new byte[]{(byte) 0xB0};

    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //inputView = (EditText) findViewById(R.id.inputView);
        displayView = findViewById(R.id.textView);
        startButton =findViewById(R.id.start);
        //sendButton =findViewById(R.id.buttonSend);
        //toggleButton =findViewById(R.id.toggle);
        final Button Do = (Button) findViewById(R.id.DO);
        final Button DoD = (Button) findViewById(R.id.DOD);
        final Button Re = (Button) findViewById(R.id.RE);
        final Button ReD = (Button) findViewById(R.id.RED);
        final Button Mi = (Button) findViewById(R.id.MI);
        final Button Fa = (Button) findViewById(R.id.FA);
        final Button FaD = (Button) findViewById(R.id.FAD);
        final Button Sol = (Button) findViewById(R.id.SOL);
        final Button SolD = (Button) findViewById(R.id.SOLD);
        final Button La = (Button) findViewById(R.id.LA);
        final Button LaD = (Button) findViewById(R.id.LAD);
        final Button Si = (Button) findViewById(R.id.SI);

        mBluetoothStatus = (TextView) findViewById(R.id.bluetoothStatus);
        //mReadBuffer = (TextView) findViewById(R.id.readBuffer);
        mScanBtn = (Button)findViewById(R.id.scan);
        mOffBtn = (Button)findViewById(R.id.off);
        mDiscoverBtn = (Button)findViewById(R.id.discover);
        mListPairedDevicesBtn = (Button)findViewById(R.id.PairedBtn);
        mLED1 = (CheckBox)findViewById(R.id.checkboxLED1);
        mBTArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio

        mDevicesListView = (ListView)findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);

        mHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == MESSAGE_READ){
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    //mReadBuffer.setText(readMessage);
                }

                if(msg.what == CONNECTING_STATUS){
                    if(msg.arg1 == 1)
                        mBluetoothStatus.setText("Connected to Device: " + (String)(msg.obj));
                    else
                        mBluetoothStatus.setText("Connection Failed");
                }
            }
        };

        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            mBluetoothStatus.setText("Status: Bluetooth not found");
            Toast.makeText(getApplicationContext(),"Bluetooth device not found!",Toast.LENGTH_SHORT).show();
        }
        else {


            mScanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOn(v);
                }
            });

            mOffBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOff(v);
                }
            });

            mListPairedDevicesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listPairedDevices(v);
                }
            });

            mDiscoverBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    discover(v);
                }
            });
        }



        // Choix de l'octave
        Spinner ListOctave = (Spinner) findViewById(R.id.octave);
        ArrayAdapter<CharSequence> adapterOctave = ArrayAdapter.createFromResource(this,
                R.array.octave_array, android.R.layout.simple_spinner_item);
        adapterOctave.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ListOctave.setAdapter(adapterOctave);
        ListOctave.setOnItemSelectedListener(new ListOctaveListener());

        // Choix de la channel
        Spinner ListChannel = (Spinner) findViewById(R.id.channel);
        ArrayAdapter<CharSequence> adapterChannel = ArrayAdapter.createFromResource(this,
                R.array.channel_array, android.R.layout.simple_spinner_item);
        adapterChannel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ListChannel.setAdapter(adapterChannel);
        ListChannel.setOnItemSelectedListener(new ListChannelListener());

        pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(broadcastReceiver, filter);


        final Button Activity2 = findViewById(R.id.activity2);
        Activity2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                serialPort.write("generate".getBytes());
                Intent intent = new Intent(MainActivity.this, MusicGenerator.class);
                startActivity(intent);
            }
        });

        //A l'écoute de chaque notes

        Do.setOnTouchListener(this);
        DoD.setOnTouchListener(this);
        Re.setOnTouchListener(this); // Gros probleme de Build >>> debugger
        ReD.setOnTouchListener(this);
        Mi.setOnTouchListener(this);
        Fa.setOnTouchListener(this);
        FaD.setOnTouchListener(this);
        Sol.setOnTouchListener(this);
        SolD.setOnTouchListener(this);
        La.setOnTouchListener(this);
        LaD.setOnTouchListener(this);
        Si.setOnTouchListener(this);
        //sendButton.setOnTouchListener(this);


        // Si on modifie la vélocité

        velocity = (SeekBar) findViewById(R.id.progress);
        PrintVelocity = (TextView) findViewById(R.id.velocity);
        velocity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValueVelocity = progress;
                PrintVelocity.setText(" " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        attack = (SeekBar) findViewById(R.id.progressAttack);
        PrintAttack = (TextView) findViewById(R.id.attack);
        attack.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String progressValueAttack = String.valueOf(progress);
                String chan = Integer.toString(channel);
                PrintAttack.setText(" " + progress);
                byte[] Attack = new byte[]{(byte) 0x49};

                serialPort.write(ControlChanges);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                serialPort.write(chan.getBytes());
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                serialPort.write(Attack);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                serialPort.write(progressValueAttack.getBytes());
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                serialPort.write(".".getBytes());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        decay = (SeekBar) findViewById(R.id.progressDecay);
        PrintDecay = (TextView) findViewById(R.id.decay);
        decay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String chan = Integer.toString(channel);
                String progressValueDecay = String.valueOf(progress);
                PrintDecay.setText(" " + progress);
                byte[] Decay = new byte[]{(byte) 0x4B};

                serialPort.write(ControlChanges);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                serialPort.write(chan.getBytes());
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                serialPort.write(Decay);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                serialPort.write(progressValueDecay.getBytes());
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                serialPort.write(".".getBytes());


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ondeSeek = (SeekBar) findViewById(R.id.ondeSeek);
        PrintOnde = (TextView) findViewById(R.id.formOnde);
        ondeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValueOnde = progress;
                switch (progress) {
                    case 0:
                        PrintOnde.setText("Dent de scie");
                        byte[] DS = new byte[]{(byte) 0xAA};

                        serialPort.write(DS);
                        serialPort.write(".".getBytes());


                        break;
                    case 1:
                        PrintOnde.setText("Carré");
                        byte[] C = new byte[]{(byte) 0xBB};

                        serialPort.write(C);
                        serialPort.write(".".getBytes());
                        break;
                    case 2:
                        PrintOnde.setText("Sinus");
                        byte[] S = new byte[]{(byte) 0xCC};

                        serialPort.write(S);
                        serialPort.write(".".getBytes());
                        break;
                    case 3:
                        PrintOnde.setText("Triangle");
                        byte[] T = new byte[]{(byte) 0xDD};

                        serialPort.write(T);
                        serialPort.write(".".getBytes());
                        break;
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

    private void bluetoothOn(View view){
        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            mBluetoothStatus.setText("Bluetooth enabled");
            Toast.makeText(getApplicationContext(),"Bluetooth turned on",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"Bluetooth is already on", Toast.LENGTH_SHORT).show();
        }
    }

    // Enter here after user selects "yes" or "no" to enabling radio
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data){
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                mBluetoothStatus.setText("Enabled");
            }
            else
                mBluetoothStatus.setText("Disabled");
        }
    }

    private void bluetoothOff(View view){
        mBTAdapter.disable(); // turn off
        mBluetoothStatus.setText("Bluetooth disabled");
        Toast.makeText(getApplicationContext(),"Bluetooth turned Off", Toast.LENGTH_SHORT).show();
    }

    private void discover(View view){
        // Check if the device is already discovering
        if(mBTAdapter.isDiscovering()){
            mBTAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(),"Discovery stopped",Toast.LENGTH_SHORT).show();
        }
        else{
            if(mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
            else{
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    private void listPairedDevices(View view){
        mPairedDevices = mBTAdapter.getBondedDevices();
        if(mBTAdapter.isEnabled()) {
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            if(!mBTAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }

            mBluetoothStatus.setText("Connecting...");
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0,info.length() - 17);

            // Spawn a new thread to avoid blocking the GUI one
            new Thread()
            {
                public void run() {
                    boolean fail = false;

                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                    try {
                        mBTSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        mBTSocket.connect();
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBTSocket.close();
                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(fail == false) {
                        mConnectedThread = new ConnectedThread(mBTSocket);
                        mConnectedThread.start();

                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();
                    }
                }
            }.start();
        }
    };
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if(bytes != 0) {
                        SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
    


        private void NoteOn(byte channel, byte b, byte velocity) {
            int noteOn = 0x90;
            //byte[] BOn = {(byte)noteOn, channel, b, velocity};
            byte[] TestOn = {1,0,0,1};
            byte[] BOn = {channel, b, velocity};
            //serialPort.write(noteOn.getBytes());

            serialPort.write(TestOn); // PROBLEME CONCATENATION
            serialPort.write(BOn);
        }

        private void NoteOff(byte channel, byte b, byte velocity) {
            byte[] TestOff = {1,0,0,0,0,0,1,1,0,1,0,0,0,1,0,1,0,1,0,0,1,1,1,1};
            byte[] noteOff = {1,0,0,0};
            byte[] BOn = {channel, b, velocity};
            //String test = Integer.toString(noteOff);
            //byte[] BOff = {(byte)noteOff, channel, b, velocity};
            serialPort.write(noteOff); // PROBLEME CONCATENATION
            serialPort.write(BOn);
            //serial.write(B); // PROBLEME CONCATENATION
            //Toast.makeText(getApplicationContext(),"1000 + channel + b +velocity  STOP", Toast.LENGTH_SHORT).show();
        }
    public boolean onTouch(View v, MotionEvent event) {
        int DoInit = 36;
        int note = 0;
        switch (v.getId()) {
            case R.id.DO:
                note = 1;
                break;
            case R.id.DOD:
                note = 2;
                break;
            case R.id.RE:
                note = 3;
                break;
            case R.id.RED:
                note = 4;
                break;
            case R.id.MI:
                note = 5;
                break;
            case R.id.FA:
                note = 6;
                break;
            case R.id.FAD:
                note = 7;
                break;
            case R.id.SOL:
                note = 8;
                break;
            case R.id.SOLD:
                note = 9;
                break;
            case R.id.LA:
                note = 10;
                break;
            case R.id.LAD:
                note = 11;
                break;
            case R.id.SI:
                note = 12;
                break;

        }
        if(serialPort.open()) {
            String chan = Integer.toString(channel);
            String noteCalculed = Integer.toString((DoInit * (octave + note)));
            String Velocity = Integer.toString(progressValueVelocity);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    byte[] noteOn = new byte[]{(byte) 0x90};
                    //byte[] noteOn = new byte[]{1,0,0,1};
                    //serialPort.write((""+channel+note+progressValueVelocity).getBytes());
                    //NoteOn((byte)channel,(byte)(DoInit*(octave+note)),(byte)(progressValueVelocity));
                    //Toast.makeText(getApplicationContext(),(""+channel+(DoInit*(octave+note))+progressValueVelocity), Toast.LENGTH_SHORT).show();
                    serialPort.write(noteOn);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    serialPort.write(chan.getBytes());
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    serialPort.write(noteCalculed.getBytes());
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    serialPort.write(Velocity.getBytes());
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    serialPort.write(".".getBytes());

                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    byte[] noteOff = new byte[]{(byte) 0x80};
                    serialPort.write(noteOff);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    serialPort.write(chan.getBytes());
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    serialPort.write(noteCalculed.getBytes());
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    serialPort.write(Velocity.getBytes());
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    serialPort.write(".".getBytes());

                    //NoteOff((byte)channel,(byte)(DoInit*(octave+note)),(byte)progressValueVelocity);
                    break;
            }
        }


        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

// Si on modifie la liste de Chennel

    public class ListChannelListener implements AdapterView.OnItemSelectedListener

    {
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            channel = pos;

        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }

    // Si on modifie la liste de Octave
    public class ListOctaveListener implements AdapterView.OnItemSelectedListener

    {
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            switch (pos) {
                case 0:
                    octave = 0;
                    break;
                case 1:
                    octave = 1;
                    break;
                case 2:
                    octave = 2;
                    break;
                case 3:
                    octave = 3;
                    break;
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }


    }


    public void onClickStart(View v) {

        if (!isSerialStarted) {
            usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

            HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
            if (!usbDevices.isEmpty()) {
                boolean keep = true;
                for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                    device = entry.getValue();
                    int deviceVID = device.getVendorId();

                    if (deviceVID == 1027 || deviceVID == 9025 ||deviceVID == 6790) { //Arduino Vendor ID
                        usbManager.requestPermission(device, pendingIntent);
                        keep = false;
                    } else {
                        connection = null;
                        device = null;
                    }
                    if (!keep)
                        break;
                }
            }
        }
    }
    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            try {
                data = new String(arg0, "UTF-8");
                data.concat("/n");
                //tvAppend(displayView, "");
                //tvAppend(displayView, data);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };

    private void tvAppend(final TextView tv, final CharSequence text) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                if (text != null) {
                    tv.append(text);
                }
            }
        });
    }
    public void onClickSend(View view) {
        String textInput = inputView.getText().toString();
        serialPort.write(textInput.getBytes());
    }
    public void onClickToggle(View view) {
        if (isLedON == false) {
            isLedON = true;
            //serialPort.write("TONLED".getBytes());
            byte[] TestOn = {1,0,0,1,0,0,1,1,0,1,0,0,0,1,0,1,0,1,0,0,1,1,1,1};
            serialPort.write(TestOn);
        } else {
            isLedON = false;
            //serialPort.write("TOFFLED".getBytes());
            byte[] TestOff = {1,0,0,0,0,0,1,1,0,1,0,0,0,1,0,1,0,1,0,0,1,1,1,1};
            serialPort.write(TestOff);
        }
    }

}