package kr.ac.pknu.vocalizing;


import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import org.billthefarmer.mididriver.MidiDriver;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class PitchTrainingActivity extends AppCompatActivity implements MidiDriver.OnMidiStartListener,
        View.OnTouchListener, AdapterView.OnItemSelectedListener {
    private final static int RECORD_AUDIO_RESULT = 200;
    int rowFreq,colFreq,midiColTable;
    TextView labelPitch;
    //note lock
    String noteSpinner,octaveSpinner;
    Spinner spinnerNote,spinnerInstruments,spinnerOctave;
    double noteFrequency;
    //initialize tarsosDSP
    double a0 = 440.0/4;
    int fs=22050;
    int n=1024;
    //public TextView textFreq,noteFreq;
    //initialize midiDriver
    private MidiDriver midiDriver;
    private byte[] event;
    private int[] config;

    private ImageButton btnPlayMidi,buttonSustain;

    protected boolean hasMicrophone() {
        PackageManager pmanager = this.getPackageManager();
        return pmanager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pitch_training);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        if (ContextCompat.checkSelfPermission(PitchTrainingActivity.this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PitchTrainingActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_RESULT);
        }

        if (!hasMicrophone())
        {
            Log.d("MICROPHONE", "Failed");
        } else {
            Log.d("MICROPHONE", "Succes");
        }

        labelPitch =(TextView) findViewById(R.id.textLabelPitchTraining);
        //set up tarsosDSP
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(fs, n, 0);
        dispatcher.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, fs, n, new PitchDetectionHandler() {
            TextView textFreq =(TextView)findViewById(R.id.textFreq);
            TextView noteFreq =(TextView) findViewById(R.id.noteFreq);
            TextView noteThres =(TextView) findViewById(R.id.textThreshold);
            ImageView imgCent = (ImageView) findViewById(R.id.imgCent);
            @Override
            public void handlePitch(PitchDetectionResult pitchDetectionResult,
                                    AudioEvent audioEvent) {
                final float pitchInHz = pitchDetectionResult.getPitch();
                final FrequencyTable f = new FrequencyTable();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        NadaDasar note = f.getNadaDasar((double)pitchInHz);
                        double noteFrequency = FrequencyTable.getNoteFreq(rowFreq,colFreq);
                        double f2 = pitchInHz;
                        double f1 =noteFrequency;
                        double freqDiv = f2/f1;
                        //Log.d("Cent Freq div", String.valueOf(freqDiv));
                        double lb=Math.log(freqDiv)/Math.log(2.0);
                        //Log.d("Cent LN", String.valueOf(lb));
                        double cent = 1200*lb;
                        //Log.d("Cent Note", String.valueOf(noteFrequency));
                        //String freqThresh = "Threshold "+String.valueOf(noteFrequency-pitchInHz);
                        //String freqThresh = "Threshold "+String.valueOf(noteFrequency);
                        String strFreq="Frequency : " + pitchInHz + " || Note : " + note.getNama() + note.getOktaf() ;
                        if (pitchInHz>=a0 && !Double.isNaN(lb)) {
                            //Log.d("RUNNING METHOD", "masuk cek freq");
                            //Log.d("RUNNING Freq", String.valueOf(pitchInHz));
                            //String setfq=String.valueOf(pitchInHz);
                            noteFreq.setText(note.getNama());
                            //Log.d("RUNNING SET TEXT", setfq);
                            textFreq.setText(strFreq);
                            noteThres.setText(String.valueOf(cent));
                            Log.d(" CENT", String.valueOf(cent));
                            if(cent >= 100){
                                imgCent.setColorFilter(Color.RED);
                                Log.d(" Threshold", "RED");
                            }
                            else if(cent<=-100) {
                                //imgCent.getDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
                                imgCent.setColorFilter(Color.RED);
                                Log.d(" Threshold", "RED");
                            }
                            else if(cent>=60) {
                                imgCent.setColorFilter(Color.YELLOW);
                                Log.d(" Threshold", "YELLOW");
                            }

                            else if(cent<=-60) {
                                //imgCent.getDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
                                imgCent.setColorFilter(Color.YELLOW);
                                Log.d(" Threshold", "YELLOW");
                            }
                            else {
                                //imgCent.getDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
                                imgCent.setColorFilter(Color.GREEN);
                                Log.d(" Threshold", "GREEN");
                            }



                        }

                        //noteThres.setText(String.valueOf(cent));@android:color/holo_red_dark
                    }
                });

            }
        }));
        new Thread(dispatcher,"Audio Dispatcher").start();

        //Set Up Button
        btnPlayMidi = (ImageButton) findViewById(R.id.btnPlayMidi);
        btnPlayMidi.setOnTouchListener(this);
        buttonSustain = (ImageButton)findViewById(R.id.buttonSustain);
        buttonSustain.setOnTouchListener(this);

        //set up spinner note
        spinnerNote = (Spinner) findViewById(R.id.spinnerNote);
        spinnerNote.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterNote = ArrayAdapter.createFromResource(this,
                        R.array.spinnerNoteArray, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterNote.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerNote.setAdapter(adapterNote);

        //spinner Octave
        spinnerOctave = (Spinner) findViewById(R.id.spinnerOctave);
        spinnerOctave.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterOctave = ArrayAdapter.createFromResource(this,
                R.array.spinnerOctaveArray, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterOctave.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinnerOctave.setAdapter(adapterOctave);
        spinnerOctave.setSelection(adapterOctave.getPosition("3"));


        //spinner Instruments
        spinnerInstruments = (Spinner) findViewById(R.id.spinnerInstruments);
        spinnerInstruments.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterInstruments = ArrayAdapter.createFromResource(this,
                R.array.spinnerInstruments, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterInstruments.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerInstruments.setAdapter(adapterInstruments);

        //choose note using spinner(note and octave)
        noteSpinner = spinnerNote.getSelectedItem().toString();
        octaveSpinner = spinnerOctave.getSelectedItem().toString();

        // Instantiate the driver.
        midiDriver = new MidiDriver();
        // Set the listener.
        midiDriver.setOnMidiStartListener(this);

        targetFreq();
    }

        @Override
    protected void onResume() {
        super.onResume();
        midiDriver.start();

        // Get the configuration.
        config = midiDriver.config();

        // Print out the details.
        Log.d(this.getClass().getName(), "maxVoices: " + config[0]);
        Log.d(this.getClass().getName(), "numChannels: " + config[1]);
        Log.d(this.getClass().getName(), "sampleRate: " + config[2]);
        Log.d(this.getClass().getName(), "mixBufferSize: " + config[3]);


    }

    @Override
    protected void onPause() {
        super.onPause();
        midiDriver.stop();
    }

    @Override
    public void onMidiStart() {
        Log.d(this.getClass().getName(), "onMidiStart()");
    }

    private void playNote(int noteNumber) {

        // Construct a note ON message for the note at maximum velocity on channel 1:
        event = new byte[3];
        event[0] = (byte) (0x90 | 0x00);  // 0x90 = note On, 0x00 = channel 1
        event[1] = (byte) noteNumber;
        event[2] = (byte) 0x7F;  // 0x7F = the maximum velocity (127)

        // Send the MIDI event to the synthesizer.
        midiDriver.write(event);

    }

    private void stopNote(int noteNumber, boolean sustainUpEvent) {

        // Stop the note unless the sustain button is currently pressed. Or stop the note if the
        // sustain button was depressed and the note's button is not pressed.
        if (!buttonSustain.isPressed() || sustainUpEvent) {
            // Construct a note OFF message for the note at minimum velocity on channel 1:
            event = new byte[3];
            event[0] = (byte) (0x80 | 0x00);  // 0x80 = note Off, 0x00 = channel 1
            event[1] = (byte) noteNumber;
            event[2] = (byte) 0x00;  // 0x00 = the minimum velocity (0)

            // Send the MIDI event to the synthesizer.
            midiDriver.write(event);
        }
    }

    private void selectInstrument(int instrument) {

        // Construct a program change to select the instrument on channel 1:
        event = new byte[2];
        event[0] = (byte)(0xC0 | 0x00); // 0xC0 = program change, 0x00 = channel 1
        event[1] = (byte)instrument;

        // Send the MIDI event to the synthesizer.
        midiDriver.write(event);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        Spinner spinner = (Spinner) parent;
        if(spinner.getId() == R.id.spinnerInstruments){
            selectInstrument(position);
            Log.d("SELECTED", "spinnerInstruments");
        }
        else if(spinner.getId() == R.id.spinnerOctave){
            octaveSpinner = spinnerOctave.getSelectedItem().toString();
            targetFreq();
            Log.d("SELECTED", "spinnerOctave");
        }
        else if(spinner.getId() == R.id.spinnerNote){
            noteSpinner = spinnerNote.getSelectedItem().toString();
            targetFreq();
            Log.d("SELECTED", "spinnerNote");
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void targetFreq(){
        checkFrequency(noteSpinner,octaveSpinner);
        noteFrequency = FrequencyTable.getNoteFreq(rowFreq,colFreq);
        String labelFreq="Pitch Training "+noteFrequency+" Note "+noteSpinner;
        labelPitch.setText(labelFreq);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int noteNumber,draftNoteNumber;
        Log.d(this.getClass().getName(), "Motion event: " + event);

        noteSpinner = spinnerNote.getSelectedItem().toString();
        octaveSpinner = spinnerOctave.getSelectedItem().toString();

        draftNoteNumber = MidiNoteTable.getNoteNumber(colFreq,midiColTable);

        //set label pitch
        //String labelFreq="Pitch Training "+noteFrequency+" Note "+noteSpinner;
        //labelPitch.setText(labelFreq);
        targetFreq();
        Log.d("Testing", "onTouch: note "+noteSpinner+" |octave: "+octaveSpinner);
        Log.d("MIDI TABLE", "onTouch: "+draftNoteNumber);
        Log.d("Frequency TABLE", "onTouch: "+noteFrequency);
        if (v.getId() == R.id.buttonSustain && event.getAction() == MotionEvent.ACTION_UP) {
            // Stop any notes whose buttons are not held down.
            if (!btnPlayMidi.isPressed()) {
                stopNote(draftNoteNumber, true);
            }

            }


        switch (v.getId()) {
            case R.id.btnPlayMidi:
                noteNumber = draftNoteNumber;
                break;

            default:
                noteNumber = -1;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Log.d(this.getClass().getName(), "MotionEvent.ACTION_DOWN");
            playNote(noteNumber);
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Log.d(this.getClass().getName(), "MotionEvent.ACTION_UP");
            stopNote(noteNumber, false);
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == RECORD_AUDIO_RESULT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //start audio recording or whatever you planned to do
            }else if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(PitchTrainingActivity.this, Manifest.permission.RECORD_AUDIO)) {
                    //Show an explanation to the user *asynchronously*
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("This permission is important to record audio.")
                            .setTitle("Important permission required");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ActivityCompat.requestPermissions(PitchTrainingActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_RESULT);
                        }
                    });
                    ActivityCompat.requestPermissions(PitchTrainingActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_RESULT);
                }else{
                    //Never ask again and handle your app without permission.
                }
            }
        }
    }

    public void checkFrequency(String noteFromSpinner, String octaveFromSpinner){

        String sNote;
        sNote=noteFromSpinner;
        colFreq=Integer.parseInt(octaveFromSpinner);
        rowFreq=0;
        if(sNote.equals("C")){
            rowFreq=1;
            midiColTable=1;
        }else if(sNote.equals("C#")){
            rowFreq=3;
            midiColTable=2;
        }else if(sNote.equals("D")){
            rowFreq=5;
            midiColTable=3;
        }else if(sNote.equals("D#")){
            rowFreq=7;
            midiColTable=4;
        }else if(sNote.equals("E")){
            rowFreq=9;
            midiColTable=5;
        }else if(sNote.equals("F")){
            rowFreq=11;
            midiColTable=6;
        }else if(sNote.equals("F#")){
            rowFreq=13;
            midiColTable=7;
        }else if(sNote.equals("G")){
           rowFreq=15;
            midiColTable=8;
        }else if(sNote.equals("G#")){
            rowFreq=17;
            midiColTable=9;
        }else if(sNote.equals("A")){
            rowFreq=19;
            midiColTable=10;
        }else if(sNote.equals("A#")){
            rowFreq=21;
            midiColTable=11;
        }else if(sNote.equals("B")){
            rowFreq=23;
            midiColTable=12;
        }
        Log.d("TEST FREQ",sNote+" "+rowFreq+" "+colFreq);
    }


}
