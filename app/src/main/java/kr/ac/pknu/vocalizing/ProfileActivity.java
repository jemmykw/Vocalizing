package kr.ac.pknu.vocalizing;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class ProfileActivity extends AppCompatActivity {
    double a0 = 440.0/4;
    int fs=44100;
    int n=1024;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);


        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(fs, n, 0);
        dispatcher.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, fs, n, new PitchDetectionHandler() {

            TextView text = (TextView) findViewById(R.id.textFreq);
            TextView noteText = (TextView) findViewById(R.id.noteFreq);
            @Override
            public void handlePitch(PitchDetectionResult pitchDetectionResult,
                                    AudioEvent audioEvent) {
                final float pitchInHz = pitchDetectionResult.getPitch();
                final FrequencyTable f = new FrequencyTable();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        NadaDasar note = f.getNadaDasar((double)pitchInHz);

                        if (pitchInHz>=a0) {
                            text.setText("Frequency : " + pitchInHz + " || Note : " + note.getNama() + note.getOktaf());
                            noteText.setText(note.getNama());
                        }

                    }
                });

            }
        }));
        new Thread(dispatcher,"Audio Dispatcher").start();
    }


}
