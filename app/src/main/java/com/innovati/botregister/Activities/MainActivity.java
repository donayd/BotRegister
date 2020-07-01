package com.innovati.botregister.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.innovati.botregister.Adapters.CameraAdapter;
import com.innovati.botregister.R;
import com.innovati.botregister.TTSManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Activity thisActivity = MainActivity.this;

    private MediaRecorder record;
    private String srcRecord = null;
    private TTSManager ttsManager = null;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private String keeper = "";
    private AudioManager manager;
    private Integer maxVolume;

    Uri imageUri;
    final int TAKE_PICTURE = 1;
    final int PICK_PICTURE = 2;
    final String FOLDER = Environment.getExternalStorageDirectory() + "/BotRegister";
    private CameraAdapter adapter;

    private Button btn_recorder;
    private Button btn_play;
    private Button btn_camera;
    private TextView tv_photo;
    private TextView tv_finish;
    private LinearLayout lyFoto;
    private RecyclerView rvFoto;
    private ConstraintLayout ly_container;
    private LinearLayoutManager layoutManager;

    List<String> listFotos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File carpetaApp = new File(FOLDER);
        if (!carpetaApp.exists()) {
            carpetaApp.mkdirs();
        }

        bindViews();
        checkPermissions();
        introductoryVoice();
        hearComand();
    }

    private void bindViews() {
        ly_container = findViewById(R.id.ly_container);
        btn_recorder = findViewById(R.id.btn_recorder);
        btn_play = findViewById(R.id.btn_play);
        btn_camera = findViewById(R.id.btn_camera);
        tv_photo = findViewById(R.id.textViewPhoto);
        tv_finish = findViewById(R.id.textViewFinish);
        lyFoto = findViewById(R.id.lyFoto);
        rvFoto = findViewById(R.id.rvFoto);

        layoutManager = new LinearLayoutManager(thisActivity, LinearLayoutManager.HORIZONTAL, false);
        rvFoto.setLayoutManager(layoutManager);
        adapter = new CameraAdapter(listFotos, thisActivity, lyFoto);
        rvFoto.setAdapter(adapter);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(thisActivity,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.CAMERA}, 1000);
            checkPermissions();
        }
    }

    private void introductoryVoice() {
        ttsManager = new TTSManager();
        ttsManager.init(this);
        manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        configureVolume(maxVolume * 3 / 4);
        ttsManager.isLoaded.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                ttsManager.initQueue("Bienvenido a BotRegister");
                ttsManager.addQueue("Para grabar un texto por favor presione el micrófono o diga la palabra Grabar");
            }
        });
    }

    private void hearComand() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(thisActivity);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int error) {
            }

            @Override
            public void onResults(Bundle results) {
                if (record == null) {
                    ArrayList<String> matchesFound = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matchesFound != null) {
                        keeper = matchesFound.get(0);
                        if (keeper.toLowerCase().indexOf("grabar") != -1 || keeper.toLowerCase().indexOf("graba") != -1) {
                            btn_recorder.performClick();
                        } else {
                            hearVoice();
                        }
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });

        new android.os.Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        hearVoice();
                    }
                },
                7000);
    }

    private void hearVoice() {

        configureVolume(0);
        speechRecognizer.startListening(speechRecognizerIntent);
        keeper = "";

        new android.os.Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        configureVolume(maxVolume * 3 / 4);
                        speechRecognizer.stopListening();
                    }
                },
                4000);
    }

    public void Recorder(View view) {
        if (record == null) {
            configureVolume(maxVolume * 3 / 4);
            srcRecord = FOLDER + "/robotoRecord.mp3";
            record = new MediaRecorder();
            speechRecognizer.stopListening();
            record.setAudioSource(MediaRecorder.AudioSource.MIC);
            record.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            record.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            record.setOutputFile(srcRecord);
            try {
                record.prepare();
                record.start();
            } catch (IOException e) {
                Log.e("error", e.toString());
            }
            btn_recorder.setBackgroundResource(R.drawable.micon);
            Toast.makeText(getApplicationContext(), "Grabando...", Toast.LENGTH_LONG).show();
        } else {
            record.stop();
            record.release();
            record = null;
            TransitionManager.beginDelayedTransition(ly_container);
            btn_play.setVisibility(View.VISIBLE);
            btn_recorder.setBackgroundResource(R.drawable.micoff);
            Toast.makeText(getApplicationContext(), "¡Grabación finalizada!", Toast.LENGTH_LONG).show();
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            recorderSuccessful();
                        }
                    },
                    2000);
        }

    }

    private void recorderSuccessful() {
        ttsManager.initQueue("Muy bien");
        ttsManager.addQueue("Ahora por favor toma al menos 3 fotos");
        TransitionManager.beginDelayedTransition(ly_container);
        tv_photo.setVisibility(View.VISIBLE);
        btn_camera.setVisibility(View.VISIBLE);
    }

    public void play(View view) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(srcRecord);
            mediaPlayer.prepare();
        } catch (IOException e) {
            Log.e("error", e.toString());
        }

        mediaPlayer.start();
        Toast.makeText(getApplicationContext(), "Reproduciendo audio", Toast.LENGTH_LONG).show();
    }

    public void takePhoto(View view) {
        final CharSequence[] options = {"TOMAR FOTO", "OBTENER DE GALERIA", "CANCELAR"};
        AlertDialog.Builder builder = new AlertDialog.Builder(thisActivity);
        builder.setTitle("Opciones de Cámara");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("TOMAR FOTO")) {
                    StrictMode.VmPolicy.Builder builder1 = new StrictMode.VmPolicy.Builder();
                    StrictMode.setVmPolicy(builder1.build());
                    Date dNow = new Date();
                    SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddhhmmss");
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File photo = new File(FOLDER, "IMG_BOT_" + ft.format(dNow) + ".jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
                    imageUri = Uri.fromFile(photo);
                    MainActivity.this.startActivityForResult(intent, TAKE_PICTURE);
                } else if (options[item].equals("OBTENER DE GALERIA")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    MainActivity.this.startActivityForResult(pickPhoto, PICK_PICTURE);
                } else if (options[item].equals("CANCELAR")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        listFotos.add(getRealPathFromURI(imageUri, thisActivity));
                        adapter.notifyDataSetChanged();
                        lyFoto.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        Log.e("Camera", e.toString());
                    }
                }
                break;
            case PICK_PICTURE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    if (selectedImage != null) {
                        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn,
                                null, null, null);
                        if (cursor != null) {
                            cursor.moveToFirst();
                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            String picturePath = cursor.getString(columnIndex);
                            listFotos.add(picturePath);
                            adapter.notifyDataSetChanged();
                            cursor.close();
                        }
                    }
                    lyFoto.setVisibility(View.VISIBLE);
                }
                break;
        }
        if (listFotos.size() > 2) {
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            ttsManager.initQueue("Registro finalizado");
                            ttsManager.addQueue("Gracias por tu colaboración");
                            TransitionManager.beginDelayedTransition(ly_container);
                            tv_finish.setVisibility(View.VISIBLE);
                        }
                    },
                    2000);
        }
    }

    public String getRealPathFromURI(Uri contentUri, Context context) {
        Cursor cursor = context.getContentResolver().query(contentUri, null, null,
                null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

    private void configureVolume(int level) {
        manager.setStreamVolume(AudioManager.STREAM_MUSIC, level, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ttsManager.shutDown();
    }
}