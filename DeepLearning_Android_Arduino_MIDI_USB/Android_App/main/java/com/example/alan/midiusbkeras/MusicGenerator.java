package com.example.alan.midiusbkeras;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class MusicGenerator extends AppCompatActivity implements View.OnClickListener {
    int progressValueBPM = 0;
    SeekBar SeekBPM;
    TextView BPM;
    String from="";
    TextView fromDisplay;
    String generate ="";
    TextView generateDisplay;
    int NBnote = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_generator);

        SeekBPM = (SeekBar) findViewById(R.id.progress);
        BPM = (TextView) findViewById(R.id.bpm);
        SeekBPM.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValueBPM = progress;
                BPM.setText(" " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        fromDisplay = findViewById(R.id.piano);
        generateDisplay = findViewById(R.id.gen);
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


        final Button main = findViewById(R.id.main);
        main.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        final Button clear = findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                NBnote =0 ;
                from = "";
                generate = "";
                fromDisplay.setText(from);
                generateDisplay.setText(generate);
            }
        });



        Do.setOnClickListener(this);
        DoD.setOnClickListener(this);
        Re.setOnClickListener(this); // Gros probleme de Build >>> debugger
        ReD.setOnClickListener(this);
        Mi.setOnClickListener(this);
        Fa.setOnClickListener(this);
        FaD.setOnClickListener(this);
        Sol.setOnClickListener(this);
        SolD.setOnClickListener(this);
        La.setOnClickListener(this);
        LaD.setOnClickListener(this);
        Si.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        String note="";
        switch (v.getId()){
            case R.id.DO:
                note="c";
                break;
            case R.id.DOD:
                //note=;
                break;
            case R.id.RE:
                note="d";
                break;
            case R.id.RED:
                //note=4;
                break;
            case R.id.MI:
                note="e";
                break;
            case R.id.FA:
                note="f";
                break;
            case R.id.FAD:
                //note=7;
                break;
            case R.id.SOL:
                note="g";
                break;
            case R.id.SOLD:
                //note=9;
                break;
            case R.id.LA:
                note="a";
                break;
            case R.id.LAD:
                //note=11;
                break;
            case R.id.SI:
                note="b";
                break;
        }

        from = from + note;
        NBnote = NBnote+1;
        if(NBnote<5) {
            fromDisplay.setText(from);
        }
    }
}
