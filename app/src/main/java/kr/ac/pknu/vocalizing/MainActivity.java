package kr.ac.pknu.vocalizing;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button buttonPitch,buttonVocal,buttonProfile;
    private final static int RECORD_AUDIO_RESULT = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_RESULT);
        }

        //initialize button main menu
        buttonPitch = (Button) findViewById(R.id.buttonPitch);
        buttonVocal = (Button) findViewById(R.id.buttonVocal);
        buttonProfile = (Button) findViewById(R.id.buttonProfile);

        //set listener
        buttonPitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,PitchTrainingActivity.class);
                startActivity(intent);
            }
        });

        buttonVocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,VocalTrainingActivity.class);
                startActivity(intent);
            }
        });

        buttonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ProfileActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == RECORD_AUDIO_RESULT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //start audio recording or whatever you planned to do
            }else if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.RECORD_AUDIO)) {
                    //Show an explanation to the user *asynchronously*
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("This permission is important to record audio.")
                            .setTitle("Important permission required");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_RESULT);
                        }
                    });
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_RESULT);
                }else{
                    //Never ask again and handle your app without permission.
                }
            }
        }
    }

}
