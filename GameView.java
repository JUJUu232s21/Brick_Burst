import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    static {
        System.loadLibrary("brick_burst");
    }
    private SurfaceHolder holder;
    private Thread gameThread;
    private boolean playing;
    private final float[] gameState = new float[500];
    private long lastFrameTime;
    private Paint playerPaint;
    private Paint ballPaint;
    private Paint obstaclePaint;
    private Paint textPaint;
    private Paint hudPaint;
    private volatile boolean dialogShown = false;
    private volatile boolean needsRestart = false;
    private boolean isPaused = false;
    private boolean isInitialized = false;
    private native void initGameNative(float width, float height, int damage);
    private native void updateGameNative(float dt, float[] stateArray);
    private native void movePlayerNative(float x, float y);
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        holder.addCallback(this);
        playerPaint = new Paint();
        playerPaint.setColor(Color.parseColor("#F44336"));
        ballPaint = new Paint();
        ballPaint.setColor(Color.WHITE);
        ballPaint.setAntiAlias(true);
        obstaclePaint = new Paint();
        obstaclePaint.setColor(Color.parseColor("#8BC34A"));
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(60);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);
        hudPaint = new Paint();
        hudPaint.setColor(Color.WHITE);
        hudPaint.setTextSize(60);
        hudPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }
    public void pauseGame() {
        isPaused = true;
    }
    public void resumeGame() {
        isPaused = false;
        lastFrameTime = System.currentTimeMillis();
    }
    @Override
    public void run() {
        while (playing) {
            if (needsRestart) {
                SharedPreferences sp = getContext().getSharedPreferences("user_settings", Context.MODE_PRIVATE);
                String dmgStr = sp.getString("user_ball_", "1");
                int dmg = 1;
                try { dmg = Integer.parseInt(dmgStr); } catch (Exception e) { dmg = 1; }
                initGameNative(getWidth(), getHeight(), dmg);
                gameState[4] = 0.0f;
                dialogShown = false;
                needsRestart = false;
                lastFrameTime = System.currentTimeMillis();
            }
            long currentTime = System.currentTimeMillis();
            float dt = (currentTime - lastFrameTime) / 1000.0f;
            lastFrameTime = currentTime;
            if (!isPaused) {
                updateGameNative(dt, gameState);
            }
            drawGame();
            long frameTime = System.currentTimeMillis() - currentTime;
            if (frameTime < 16) {
                try {
                    Thread.sleep(16 - frameTime);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }
    private void drawGame() {
        if (!holder.getSurface().isValid()) return;
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) return;
        canvas.drawColor(Color.parseColor("#121212"));
        float playerX = gameState[0];
        float playerY = gameState[1];
        float playerSize = gameState[2];
        int score = (int) gameState[3];
        boolean isGameOver = gameState[4] > 0.5f;
        int wave = (int) gameState[5];
        int obsCount = (int) gameState[6];
        float halfSize = playerSize / 2.0f;
        canvas.drawRoundRect(playerX - halfSize, playerY - halfSize, playerX + halfSize, playerY + halfSize, 15.0f, 15.0f, playerPaint);
        int baseIdx = 7;
        for (int i = 0; i < obsCount; i++) {
            float ox = gameState[baseIdx];
            float oy = gameState[baseIdx + 1];
            float ow = gameState[baseIdx + 2];
            float oh = gameState[baseIdx + 3];
            int durability = (int) gameState[baseIdx + 4];
            if (durability <= 20) {
                obstaclePaint.setColor(Color.parseColor("#2196F3"));
            } else if (durability <= 50) {
                obstaclePaint.setColor(Color.parseColor("#8BC34A"));
            } else if (durability <= 70) {
                obstaclePaint.setColor(Color.parseColor("#FFEB3B"));
            } else if (durability <= 100) {
                obstaclePaint.setColor(Color.parseColor("#F44336"));
            } else if (durability <= 200) {
                obstaclePaint.setColor(Color.parseColor("#E91E63"));
            } else if (durability <= 300) {
                obstaclePaint.setColor(Color.parseColor("#9C27B0"));
            } else if (durability <= 400) {
                obstaclePaint.setColor(Color.parseColor("#673AB7"));
            } else if (durability <= 500) {
                obstaclePaint.setColor(Color.parseColor("#3F51B5"));
            } else if (durability <= 600) {
                obstaclePaint.setColor(Color.parseColor("#03A9F4"));
            } else if (durability <= 700) {
                obstaclePaint.setColor(Color.parseColor("#00BCD4"));
            } else if (durability <= 800) {
                obstaclePaint.setColor(Color.parseColor("#009688"));
            } else if (durability <= 900) {
                obstaclePaint.setColor(Color.parseColor("#FF9800"));
            } else {
                float hue = (durability * 0.5f) % 360;
                float[] hsv = new float[]{hue, 1.0f, 0.9f};
                obstaclePaint.setColor(Color.HSVToColor(hsv));
            }
            canvas.drawRect(ox, oy, ox + ow, oy + oh, obstaclePaint);
            float textY = oy + (oh / 2.0f) - ((textPaint.descent() + textPaint.ascent()) / 2.0f);
            canvas.drawText(String.valueOf(durability), ox + (ow / 2.0f), textY, textPaint);
            baseIdx += 5;
        }
        int ballCount = (int) gameState[baseIdx];
        baseIdx++;
        for (int i = 0; i < ballCount; i++) {
            float bx = gameState[baseIdx];
            float by = gameState[baseIdx + 1];
            float bradius = gameState[baseIdx + 2];
            canvas.drawCircle(bx, by, bradius, ballPaint);
            baseIdx += 3;
        }
        hudPaint.setTextAlign(Paint.Align.LEFT);
        hudPaint.setColor(Color.RED);
        canvas.drawText("Счёт: " + score, 50, 130, hudPaint);
        hudPaint.setColor(Color.WHITE);
        hudPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Уровень: " + wave, getWidth() - 50, 130, hudPaint);
        if (isGameOver) {
                if (!dialogShown) {
                    dialogShown = true;
                    if (getContext() instanceof MainActivity2) {
                        MainActivity2 activity = (MainActivity2) getContext();
                        activity.pauseMusic();
                        activity.showGameOverDialog(wave, score, () -> {
                            needsRestart = true;
                        });
                    }
                }
            Paint overlayPaint = new Paint();
            overlayPaint.setColor(Color.argb(180, 0, 0, 0));
            canvas.drawRect(0, 0, getWidth(), getHeight(), overlayPaint);
        }
        holder.unlockCanvasAndPost(canvas);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
            if (gameState[4] < 0.5f) {
                movePlayerNative(event.getX(), event.getY());
            }
        }
        return true;
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!isInitialized) {
            SharedPreferences sp = getContext().getSharedPreferences("user_settings", Context.MODE_PRIVATE);
            String dmgStr = sp.getString("user_ball_", "1");
            int dmg = 1;
            try { dmg = Integer.parseInt(dmgStr); } catch (Exception e) { dmg = 1; }
            initGameNative(getWidth(), getHeight(), dmg);
            isInitialized = true;
        }
        lastFrameTime = System.currentTimeMillis();
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
