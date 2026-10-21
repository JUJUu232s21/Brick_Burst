import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
public class MainActivity extends AppCompatActivity {
    private SoundPool soundPool;
    private int soundClick;
    private boolean isSoundLoaded = false;
    private TextView record_yroven;
    private TextView balance;
    private TextView ball_Attack;
    private TextView yroven;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Base_Theme_Brick_Burst);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        record_yroven = findViewById(R.id.record_yroven);
        balance = findViewById(R.id.balance);
        ball_Attack = findViewById(R.id.ball_Attack);
        yroven = findViewById(R.id.yroven);
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
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if(status == 0){
                isSoundLoaded = true;
            }
        });
        soundClick = soundPool.load(this, R.raw.button_click, 1);
        getUserInfo();
    }
    private void getUserInfo(){
        SharedPreferences sharedPref = getSharedPreferences("user_settings", Context.MODE_PRIVATE);
        String intyroven = sharedPref.getString("user_max_yroven", "0");
        record_yroven.setText(intyroven);
        String balance1 = sharedPref.getString("user_balance", "0");
        balance.setText(balance1);
        String ball_speed_val = sharedPref.getString("user_ball_", "1");
        ball_Attack.setText(ball_speed_val);
        int recordVal = Integer.parseInt(intyroven);
        int cost = ((recordVal / 10) + 1) * 100;
        yroven.setText("+1 к урону за: " + cost);
    }
    public void upgradeDamage(View view) {
        SharedPreferences sharedPref = getSharedPreferences("user_settings", Context.MODE_PRIVATE);
        int currentBalance = Integer.parseInt(sharedPref.getString("user_balance", "0"));
        int currentDmg = Integer.parseInt(sharedPref.getString("user_ball_", "1"));
        int recordVal = Integer.parseInt(sharedPref.getString("user_max_yroven", "0"));
        int cost = ((recordVal / 10) + 1) * 100;
        if (currentBalance >= cost) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("user_balance", String.valueOf(currentBalance - cost));
            editor.putString("user_ball_", String.valueOf(currentDmg + 1));
            editor.apply();
            getUserInfo();
        } else {
            Toast.makeText(this, "Недостаточно средств!", Toast.LENGTH_SHORT).show();
        }
    }
    public void game(View view){
        if(isSoundLoaded){
            soundPool.play(soundClick, 1.0f, 1.0f, 0, 0, 1.0f);
        }
        Intent intent = new Intent(this, MainActivity2.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        getUserInfo();
    }
}
