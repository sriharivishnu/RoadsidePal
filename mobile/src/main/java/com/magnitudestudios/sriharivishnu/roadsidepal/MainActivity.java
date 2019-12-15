package com.magnitudestudios.sriharivishnu.roadsidepal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.AlarmClock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.JsonElement;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.RequestExtras;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

public class MainActivity extends AppCompatActivity implements AIListener {
    private static final int REQUEST_MICROPHONE = 550;
    private static final int REQUEST_LOCATION = 200;
    private static final String CLIENT_ID = "0d220e1e8b4f49a89b827355c614766d";
    private static final String REDIRECT_URI = "com.srihari.spotify.appremote://callback";

    private TextView mTextView;
    private SeekBar mSeekBar;
    private Button button;
    private static int HEART_RATE_SLEEPY = 65;
    private AIService aiService;
    private TextToSpeech textToSpeech;
    private int counter = 0;

    private SpotifyAppRemote spotifyAppRemote;

    private Boolean running = false;
    private Boolean listening = false;

    private Boolean isFirst = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.number);
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        button = (Button) findViewById(R.id.button);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE);

        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        }

        initTTS();

        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.background);


        final AIConfiguration config = new AIConfiguration("f0105f860a904ef9b48c4c9a2ce38bc2",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);

//        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        double longitude = location.getLongitude();
//        double latitude = location.getLatitude();


        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTextView.setText(Integer.toString(progress));
                if (progress < HEART_RATE_SLEEPY) {
                    //Sleepy
                    linearLayout.setBackgroundColor(getColor(R.color.colorAccent));
                    if (!isFirst && !running) {
                        ping(config, "Srihari");
                        isFirst = true;
                    }
                }
                else {
                    linearLayout.setBackgroundColor(getColor(R.color.colorPrimaryDark));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!listening) {
                    listening = true;
                    ping(config, "Hello");
                }
                else {
                    aiService.stopListening();
                    listening = false;
                }
            }
        });

        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                counter++;
                Log.d("Listening: ", listening.toString() +" "+counter);

                if (listening && counter > 5) {
                    listening = false;
                    counter = 0;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            aiService.stopListening();
                        }
                    });
                } else if (!running && !listening && counter > 60){
                    counter = 0;
                    ping(config, "Srihari");
                }
                else if (counter > 600) {
                    counter = 0;
                    running = false;
                }
            }
        }, 1000, 1000);

    }

    @Override
    public void onResult(final AIResponse result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Result response = result.getResult();

                // Get parameters
                String parameterString = "";
                if (response.getParameters() != null && !response.getParameters().isEmpty()) {
                    for (final Map.Entry<String, JsonElement> entry : response.getParameters().entrySet()) {
                        parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
                    }
                }
                if (!listening) {
                    command(response.getFulfillment().getSpeech(), response);
                }
                Log.d("GOSRIGO", response.getFulfillment().getSpeech());
                // Show results in TextView.
                Log.d("SRIHARI","Query:" + response.getResolvedQuery() + "\nAction: " + response.getAction() + "\nParameters: " + parameterString);
            }
        });
    }
    public void command(String command, Result result) {
        if (result.getResolvedQuery().toLowerCase().contains("thank you") || result.getResolvedQuery().toLowerCase().contains("thanks") || result.getResolvedQuery().toLowerCase().contains("cancel")) {
            running = true;
        }
        if (command.equals("play_music")) {
            Log.d("SEHER", result.getStringParameter("music"));
            speak("Playing "+ result.getStringParameter("music"));
            playMusic();
            running = true;
        }
        else if (command.equals("call_someone")) {
            speak("Calling " + result.getStringParameter("name"));
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:0123456789"));
            startActivity(intent);
            running = true;
        }
        else if (command.equals("find_food")) {
            speak("Here are a couple restaurants nearby");
            running = true;
            Uri gmmIntentUri = Uri.parse("google.navigation:q=restaurants");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);

        }
        else if (command.equals("find_hotel")) {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=hotels");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
            running = true;
        }
        else if (command.equals("alarm_set")) {
            speak("Ok I will set the alarm. Please pull over.");
            Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
            intent.putExtra(AlarmClock.EXTRA_HOUR, Calendar.getInstance().get(Calendar.HOUR));
            intent.putExtra(AlarmClock.EXTRA_MINUTES, Calendar.getInstance().get(Calendar.MINUTE)+ (Integer) result.getIntParameter("number"));
            startActivity(intent);
            running = true;
        }
        else if (command.equals("rant_go")) {
            speak("You are so right, I feel so bad for you.");
        }
        else if (command.equals("end_conversation_ok")) {
            speak("okay");
            running = true;
        }
        else if (command.equals("end_conversation_gj")) {
            speak("good job, I'll check back in 10 minutes");
            running = true;
        }
//        else if (command.equals("riddle_answer")) {
//            if (result.getStringParameter("Answers").contains("penny")) {
//                Log.d("AYAYYAAYA","It works");
//            }
//        }
        else {
            speak(result.getFulfillment().getSpeech());

        }
    }
    public void speak(String thing) {
        if (textToSpeech.getDefaultEngine() == null) initTTS();
        if (!listening) {
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "stringId");
            textToSpeech.speak(thing, TextToSpeech.QUEUE_FLUSH, params, "GOSRIGO");
        }
    }

    public void playMusic() {
        spotifyAppRemote.getPlayerApi().play("spotify:user:11158272501:playlist:4Rj0zQ0Ux47upeqVSIuBx9");
    }

    public void ping(AIConfiguration config, String message) {
        final AIDataService aiDataService = new AIDataService(this, config);

        final AIRequest aiRequest = new AIRequest(message);

        new AsyncTask<AIRequest,Void,AIResponse>(){

            @Override
            protected AIResponse doInBackground(AIRequest... aiRequests) {
                final AIRequest request = aiRequests[0];
                try {
                    final AIResponse response = aiDataService.request(aiRequest);
                    return response;
                } catch (AIServiceException e) {
                }
                return null;
            }
            @Override
            protected void onPostExecute(AIResponse response) {
                if (response != null) {

                    Result result = response.getResult();
                    final String reply = result.getFulfillment().getSpeech();
                    if (!listening) {
                        speak(reply);
                        Log.d("YEETT. ", reply);
                    }

                }
            }
        }.execute(aiRequest);
    }

    public void initTTS() {
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {

                        }

                        @Override
                        public void onDone(String utteranceId) {
                            Log.d("SRIHARI", "SFSDF");
                            if (!running) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!listening) {
                                            counter = 0;
                                            try {
                                                aiService.startListening();
                                            }
                                            catch (Error e) {
                                                Log.e("TAG", e.getMessage());
                                            }
                                            listening = true;
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onError(String utteranceId) {

                        }
                    });
                }
            }
        });
    }

    @Override
    public void onError(AIError error) {
        Log.e("Error", error.toString());
    }

    @Override
    public void onAudioLevel(float level) {
    }

    @Override
    public void onListeningStarted() {
        listening = true;
    }

    @Override
    public void onListeningCanceled() {
        listening = false;
    }

    @Override
    public void onListeningFinished() {
        counter = 0;
        listening = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .showAuthView(true)
                        .setRedirectUri(REDIRECT_URI)
                        .build();

        SpotifyAppRemote.CONNECTOR.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote s) {
                        spotifyAppRemote = s;
                        Log.d("MainActivity", "Connected! Yay!");
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("MainActivity", throwable.getMessage(), throwable);
                    }
                });
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (spotifyAppRemote != null) {
            SpotifyAppRemote.CONNECTOR.disconnect(spotifyAppRemote);
        }
        textToSpeech.shutdown();
    }
}
