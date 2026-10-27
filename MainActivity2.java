import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
public class MainActivity2 extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private SoundPool soundPool;
    private int soundClick;
    private int soundButton;
    private boolean isSoundLoaded = false;
    private volatile boolean isDialogShowing = false;
    private volatile boolean isGameOver = false;
    private boolean isFirstResume = true;
    private GameView gameView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        mediaPlayer = MediaPlayer.create(this, R.raw.ogni);
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(0.5f, 0.5f);
        }
        gameView = findViewById(R.id.gameView);
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build();
        soundClick = soundPool.load(this, R.raw.weak, 1);
        soundButton = soundPool.load(this, R.raw.button_click, 1);
        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> isSoundLoaded = true);
        WindowInsetsControllerCompat windowInsetsController = ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
            windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.transparent));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.transparent));
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
            controller.setAppearanceLightNavigationBars(false);
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            return insets;
        });
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (gameView != null) {
                    gameView.pauseGame();
                }
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                showPauseDialog();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
        }
    }
    public void startMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }
    public void showGameOverDialog(int wave, int score, final Runnable onRestart) {
        isGameOver = true;
        if (isSoundLoaded) {
            soundPool.play(soundClick, 1.0f, 1.0f, 0, 0, 1.0f);
        }
        SharedPreferences sharedPref = getSharedPreferences("user_settings", Context.MODE_PRIVATE);
        String currentMaxStr = sharedPref.getString("user_max_yroven", "0");
        int currentMax = 0;
        try {
            currentMax = Integer.parseInt(currentMaxStr);
        } catch (NumberFormatException e) {
            currentMax = 0;
        }
        if (wave > currentMax) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("user_max_yroven", String.valueOf(wave));
            editor.apply();
        }
        String currentBalanceStr = sharedPref.getString("user_balance", "0");
        int currentBalance = 0;
        try {
            currentBalance = Integer.parseInt(currentBalanceStr);
        } catch (NumberFormatException e) {
            currentBalance = 0;
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("user_balance", String.valueOf(currentBalance + score));
        editor.apply();
        new Handler(Looper.getMainLooper()).post(() -> {
            if (isDialogShowing) return;
            isDialogShowing = true;
            AlertDialog dialog = new AlertDialog.Builder(MainActivity2.this, R.style.CustomAlertDialog)
                    .setTitle("ИГРА ОКОНЧЕНА!")
                    .setMessage("Счёт: " + score + "\nУровень: " + wave + "\nХотите сыграть еще раз?")
                    .setCancelable(false)
                    .setPositiveButton("Играть ещё", (d, which) -> {
                        isGameOver = false;
                        isDialogShowing = false;
                        if (isSoundLoaded) {
                            soundPool.play(soundButton, 1.0f, 1.0f, 0, 0, 1.0f);
                        }
                        startMusic();
                        onRestart.run();
                    })
                    .setNegativeButton("Выйти", (d, which)->{
                        isDialogShowing = false;
                        if (isSoundLoaded) {
                            soundPool.play(soundButton, 1.0f, 1.0f, 0, 0, 1.0f);
                        }
                        finish();
                        overridePendingTransition(0, 0);
                    })
                    .create();
            dialog.setOnShowListener(dialogInterface -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.sini));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.sini));
            });
            dialog.show();
        });
    }
    public void showPauseDialog() {
        if (isDialogShowing || isGameOver) return;
        isDialogShowing = true;
        new Handler(Looper.getMainLooper()).post(() -> {
            AlertDialog dialog = new AlertDialog.Builder(MainActivity2.this, R.style.CustomAlertDialog)
                    .setTitle("ПАУЗА")
                    .setMessage("Игра приостановлена! " + "\nПри выходе текущий прогресс не будет сохранён!")
                    .setCancelable(false)
                    .setPositiveButton("Продолжить", (d, which) -> {
                        isDialogShowing = false;
                        if (isSoundLoaded) {
                            soundPool.play(soundButton, 1.0f, 1.0f, 0, 0, 1.0f);
                        }
                        if (gameView != null) {
                            gameView.resumeGame();
                        }
                        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                            mediaPlayer.start();
                        }
                    })
                    .setNegativeButton("Выйти", (d, which) -> {
                        if (isSoundLoaded) {
                            soundPool.play(soundButton, 1.0f, 1.0f, 0, 0, 1.0f);
                        }
                        isDialogShowing = false;
                        finish();
                        overridePendingTransition(0, 0);
                    })
                    .create();
            dialog.setOnShowListener(dialogInterface -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.sini));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.sini));
            });
            dialog.show();
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstResume) {
            isFirstResume = false;
            if (gameView != null) {
                gameView.resumeGame();
            }
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        } else {
            showPauseDialog();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) {
            gameView.pauseGame();
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    public void pause_bnte(View view){
        if (isSoundLoaded) {
            soundPool.play(soundButton, 1.0f, 1.0f, 0, 0, 1.0f);
        }
        if (gameView != null) {
            gameView.pauseGame();
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        showPauseDialog();
    }
}
