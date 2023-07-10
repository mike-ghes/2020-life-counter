package com.minu.lifecount2020.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.method.LinkMovementMethod;
import android.view.Display;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;

import com.minu.lifecount2020.app.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends SensorActivity implements SettingsListAdapter.Delegate {

    private ActivityMainBinding binding;

    private Settings mSettings;

    private GameState mGameState;

    private int mScreenHeight;
    private int mScreenWidth;

    private boolean mSpun;
    private boolean mSideSwipe;

    private float mPickerY;
    private float mPickerX;
    private float mPickerLastX;
    private boolean mUpdating;

    private String mPullToRefresh;
    private String mReleaseToRefresh;

    private float mCurrentRotation;

    private CountDownTimer mRoundTimer;
    private TextView mRoundTimerTextView;
    private boolean mTimerRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        if (!isTaskRoot()) {
            // Android launch bug
            finish();
            return;
        }

        hideSystemUI();

        if (savedInstanceState != null) {
            mSettings = Settings.Companion.fromBundle(savedInstanceState);

            mGameState = GameState.Companion.fromBundle(savedInstanceState);
        } else {
            SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);

            mGameState = GameState.Companion.fromPreferences(preferences);

            mSettings = Settings.Companion.fromPreferences(preferences);
        }

        applyTheme(mSettings.getTheme());

        initElements();

        setLifeTotals();

        resetTimer(true);

        restoreSettings();
    }

    private void restoreSettings() {
        displayPoison();

        displayEnergy();

        toggleTimer();

        ((SettingsListAdapter) binding.leftDrawer.getAdapter()).setSettings(mSettings);
    }

    @Override
    protected void onStop() {
        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();

        mGameState.saveTo(editor);

        mSettings.saveTo(editor);

        editor.apply();

        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        mGameState.saveTo(savedInstanceState);

        mSettings.saveTo(savedInstanceState);

        super.onSaveInstanceState(savedInstanceState);
    }

    private void instantiateArrayLists() {

    }

    private void applyTheme(Theme theme) {
        binding.drawerLayout.setBackgroundResource(theme.getBackground());

        int playerOne = ContextCompat.getColor(this, theme.getPlayerOne());
        int playerTwo = ContextCompat.getColor(this, theme.getPlayerTwo());
        int textColor = ContextCompat.getColor(this, theme.getTextColor());
        binding.lifePicker1.setTextColor(playerOne);
        binding.poisonPicker1.setTextColor(playerOne);
        binding.energyPicker1.setTextColor(playerOne);
        binding.lifePicker2.setTextColor(playerTwo);
        binding.poisonPicker2.setTextColor(playerTwo);
        binding.energyPicker2.setTextColor(playerTwo);

        Drawable arrowLeft = ContextCompat.getDrawable(this, R.drawable.left_arrow);
        Drawable arrowRight = ContextCompat.getDrawable(this, R.drawable.right_arrow);
        Drawable energyIconLeft = ContextCompat.getDrawable(this, R.drawable.energy_icon_left);
        Drawable energyIconRight = ContextCompat.getDrawable(this, R.drawable.energy_icon_right);
        if (arrowLeft != null)
            arrowLeft.setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);
        if (arrowRight != null)
            arrowRight.setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);
        if (energyIconLeft != null)
            energyIconLeft.setColorFilter(playerOne, PorterDuff.Mode.SRC_ATOP);
        if (energyIconRight != null)
            energyIconRight.setColorFilter(playerTwo, PorterDuff.Mode.SRC_ATOP);

        ((ImageView) findViewById(R.id.update_arrow_left)).setImageDrawable(arrowLeft);
        ((ImageView) findViewById(R.id.update_arrow_right)).setImageDrawable(arrowRight);

        ((ImageView) findViewById(R.id.energy_icon_one)).setImageDrawable(energyIconLeft);
        ((ImageView) findViewById(R.id.energy_icon_two)).setImageDrawable(energyIconRight);
    }

    private void collapseHistory() {
        mGameState.collapseHistory();
        ((HistoryListAdapter) binding.rightDrawer.getAdapter()).setHistory(new ArrayList<>(mGameState.getHistory()));
    }

    public void setStartingLife(int startingLife) {
        mSettings.setStartingLife(startingLife);
    }

    private void initElements() {
        binding.leftDrawer.setAdapter(new SettingsListAdapter(this, this));

        binding.rightDrawer.setAdapter(new HistoryListAdapter(this, new ArrayList<>(mGameState.getHistory())));

        binding.leftDrawer.setOnItemClickListener(new DrawerItemClickListener());

        setLayoutTouchListener(binding.firstLifePickerLayout, binding.lifePicker1, Player.ONE, PlayerField.Life);
        setLayoutTouchListener(binding.secondLifePickerLayout, binding.lifePicker2, Player.TWO, PlayerField.Life);
        setLayoutTouchListener(binding.firstPoisonPickerLayout, binding.poisonPicker1, Player.ONE, PlayerField.Poison);
        setLayoutTouchListener(binding.secondPoisonPickerLayout, binding.poisonPicker2, Player.TWO, PlayerField.Poison);
        setLayoutTouchListener(binding.firstEnergyPickerLayout, binding.energyPicker1, Player.ONE, PlayerField.Energy);
        setLayoutTouchListener(binding.secondEnergyPickerLayout, binding.energyPicker2, Player.TWO, PlayerField.Energy);
        setTextViewOnTouchListener(binding.lifePicker1, Player.ONE, PlayerField.Life);
        setTextViewOnTouchListener(binding.lifePicker2, Player.TWO, PlayerField.Life);
        setTextViewOnTouchListener(binding.poisonPicker1, Player.ONE, PlayerField.Poison);
        setTextViewOnTouchListener(binding.poisonPicker2, Player.TWO, PlayerField.Poison);
        setTextViewOnTouchListener(binding.energyPicker1, Player.ONE, PlayerField.Energy);
        setTextViewOnTouchListener(binding.energyPicker2, Player.TWO, PlayerField.Energy);

        binding.firstPoisonPickerLayout.setVisibility(View.GONE);
        binding.secondPoisonPickerLayout.setVisibility(View.GONE);

        binding.firstEnergyPickerLayout.setVisibility(View.GONE);
        binding.secondEnergyPickerLayout.setVisibility(View.GONE);

        mCurrentRotation = 0.0f;

        binding.settingsButton.setOnClickListener(v -> binding.drawerLayout.openDrawer(Gravity.LEFT));

        binding.historyButton.setOnClickListener(v -> binding.drawerLayout.openDrawer(Gravity.RIGHT));

        binding.drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                if (drawerView.equals(binding.rightDrawer)) {
                    //System.out.println("Should show history");
                    //System.out.println(mHistory);
                    collapseHistory();
                    binding.rightDrawer.post(() -> binding.rightDrawer.
                            setSelection(binding.rightDrawer.getAdapter().getCount()));
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.RIGHT);
                }
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        mPullToRefresh = getString(R.string.pull_to_restart);
        mReleaseToRefresh = getString(R.string.pull_to_cancel);

        Display display = getWindowManager().getDefaultDisplay();
        mScreenHeight = display.getWidth();
        mScreenWidth = display.getHeight();

        ((TextView) findViewById(R.id.twitter_link))
                .setMovementMethod(LinkMovementMethod.getInstance());

        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
        binding.drawerLayout.setKeepScreenOn(true);

        instantiateRoundTimer();

    }

    private void instantiateRoundTimer() {
        mGameState.setRemainingMillis(Constants.BASE_ROUND_TIME_IN_MS);
        mRoundTimer = getNewTimer(Constants.BASE_ROUND_TIME_IN_MS);

        mRoundTimerTextView = binding.roundTimer;

        mRoundTimerTextView.setOnClickListener(v -> {
            if (mTimerRunning)
                mRoundTimer.cancel();
            else {
                mRoundTimer = getNewTimer(mGameState.getRemainingMillis());
                mRoundTimer.start();
            }
            mTimerRunning = !mTimerRunning;
        });

        setTimerAnimations(true);

        mRoundTimerTextView.setOnLongClickListener(v -> {
            resetTimer(false);
            return true;
        });

        //mRoundTimer.start();
        //mTimerRunning = true;
    }

    private void setTimerAnimations(boolean on) {
        if (on) {
            mRoundTimerTextView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            scaleTextView((TextView) view, true);
                            break;
                        case MotionEvent.ACTION_UP:
                            scaleTextView((TextView) view, false);
                            break;
                        default:
                            break;
                    }
                    return false;
                }
            });
        } else {
            mRoundTimerTextView.clearAnimation();
        }
    }

    private void resetTimer(boolean restart) {
        mRoundTimer.cancel();
        mRoundTimer = getNewTimer(Constants.BASE_ROUND_TIME_IN_MS);
        if (!restart)
            mGameState.setRemainingMillis(minutesToMilliseconds(mSettings.getRoundTimeInMinutes()));
        mTimerRunning = false;
        mRoundTimerTextView.setText(getMinutes(mGameState.getRemainingMillis()));
    }

    private long minutesToMilliseconds(int minutes) {
        return minutes * 60 * 1000L;
    }

    private CountDownTimer getNewTimer(long startingTime) {
        return new CountDownTimer(startingTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mRoundTimerTextView.setText(getMinutes(millisUntilFinished));
                mGameState.setRemainingMillis(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                mRoundTimerTextView.setText("TIME");
            }
        };
    }

    private String getMinutes(long millisUntilFinished) {
        long remainingSeconds = millisUntilFinished / 1000;
        long minutes = remainingSeconds / 60;
        long seconds = remainingSeconds % 60;
        String result;
        if (seconds < 10)
            result = minutes + ":0" + seconds;
        else
            result = minutes + ":" + seconds;
        return result;
    }

    private void displayPoison() {
        if (!mSettings.getPoisonShowing()) {
            binding.firstPoisonPickerLayout.setVisibility(View.GONE);
            binding.secondPoisonPickerLayout.setVisibility(View.GONE);
        } else {
            binding.firstPoisonPickerLayout.setVisibility(View.VISIBLE);
            binding.secondPoisonPickerLayout.setVisibility(View.VISIBLE);
        }

        ((SettingsListAdapter) binding.leftDrawer.getAdapter()).notifyDataSetChanged();
    }

    private void displayEnergy() {
        if (!mSettings.getEnergyShowing()) {
            binding.firstEnergyPickerLayout.setVisibility(View.GONE);
            binding.secondEnergyPickerLayout.setVisibility(View.GONE);
        } else {
            binding.firstEnergyPickerLayout.setVisibility(View.VISIBLE);
            binding.secondEnergyPickerLayout.setVisibility(View.VISIBLE);
        }

        ((SettingsListAdapter) binding.leftDrawer.getAdapter()).notifyDataSetChanged();
    }

    protected void checkShake(float x, float y, float z) {
        float acceleration = (float) Math.sqrt((double) x * x + y * y + z * z);
        if (Math.abs(acceleration - mGravity) > Constants.THROW_ACCELERATION)
            startDiceThrowActivity(mSettings.getTheme());
    }

    public void setTime(int i) {
        mSettings.setRoundTimeInMinutes(i);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            switch (position) {
                case 0:
                    resetDuel();
                    binding.drawerLayout.closeDrawer(binding.settingsDrawer);
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    binding.drawerLayout.closeDrawer(binding.settingsDrawer);
                    startDiceThrowActivity(mSettings.getTheme());
                    break;
                case 6:
                    break;
                case 7:
                    break;
                default:
                    binding.drawerLayout.closeDrawer(binding.settingsDrawer);
                    break;
            }
        }
    }

    private void startDiceThrowActivity(Theme theme) {
        Intent i = new Intent(this, DiceActivity.class);
        i.putExtra(Constants.BACKGROUND_WHITE, theme);
        startActivity(i);
        overridePendingTransition(R.anim.activity_slide_in_bottom, R.anim.activity_slide_out_top);
    }

    public void toggleBackground(Theme targetBackground) {
        mSettings.setTheme(targetBackground);
        applyTheme(targetBackground);
    }

    public void togglePoison(boolean showPoison) {
        mSettings.setPoisonShowing(showPoison);
        displayPoison();
    }

    public void toggleEnergy(boolean showEnergy) {
        mSettings.setEnergyShowing(showEnergy);
        displayEnergy();
    }

    public void toggleHaptic(boolean hapticEnabled) {
        mSettings.setHapticFeedbackEnabled(hapticEnabled);
    }

    public void toggleTimer() {
        boolean timerShowing = mSettings.getTimerShowing();
        if (timerShowing) {
            setTimerAnimations(false);
            mRoundTimerTextView.setVisibility(View.GONE);
            mRoundTimer.cancel();
        } else {
            setTimerAnimations(true);
            mRoundTimerTextView.setVisibility(View.VISIBLE);
            if (mRoundTimer == null)
                mRoundTimer = getNewTimer(mGameState.getRemainingMillis());
            mRoundTimerTextView.setText(getMinutes(mGameState.getRemainingMillis()));
        }
    }

    private void setTextViewOnTouchListener(final TextView picker, final Player player, final PlayerField field) {
        picker.setOnTouchListener((view, motionEvent) -> {
            int action = motionEvent.getAction();
            float y = motionEvent.getY();
            float x = motionEvent.getX();
            handlePickerTouchEvent(x, y, action, motionEvent, picker, player, field);
            return true;
        });
    }

    private void handlePickerTouchEvent(float x, float y, int action,
                                        MotionEvent motionEvent, TextView picker, final Player player, final PlayerField field) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                recordTouchStart(motionEvent, picker);
                break;
            case MotionEvent.ACTION_MOVE:
                verticalSwipe(y, picker, player, field);
                sideSwipe(x);
                break;
            case MotionEvent.ACTION_UP:
                handlePickerTouchRelease(picker, player, field);
                break;
            default:
                //System.out.println("Default picker touchevent");
                break;
        }
    }

    private void handlePickerTouchRelease(TextView picker, final Player player, final PlayerField field) {
        if (!mSpun && !mSideSwipe) {
            changePickerValue(picker, field == PlayerField.Poison, player, field);
            scaleTextView(picker, Constants.SCALE_DOWN);
        }
        //System.out.println("Action up");
        if (mSpun || mSideSwipe)
            scaleTextView(picker, Constants.SCALE_DOWN);
        mSpun = false;
        setUpdateTextViewTexts(mPullToRefresh);
        if (mUpdating)
            resetDuel();
        binding.wrapper.scrollTo(0, 0);
        mUpdating = false;
        mSideSwipe = false;
    }

    private void setUpdateTextViewTexts(String s) {
        if (binding.update.getText().toString().compareTo(s) != 0) {
            binding.update2.setText(s);
            binding.update.setText(s);
            spinResetArrows();
        }
    }

    private void setLayoutTouchListener(final LinearLayout layout, final TextView picker, final Player player, final PlayerField field) {
        layout.setOnTouchListener((view, motionEvent) -> {
            int action = motionEvent.getAction();
            float y = motionEvent.getY();
            float x = motionEvent.getX();
            handleLayoutTouchEvent(x, y, action, motionEvent, picker, layout, player, field);
            return true;
        });
    }

    private void handleLayoutTouchEvent(float x, float y, int action, MotionEvent motionEvent,
                                        TextView picker, LinearLayout layout, Player player, PlayerField field) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                recordTouchStart(motionEvent, picker);
                break;
            case MotionEvent.ACTION_MOVE:
                verticalSwipe(y, picker, player, field);
                sideSwipe(x);
                break;
            case MotionEvent.ACTION_UP:
                handleLayoutTouchRelease(y, picker, layout, player, field);
                break;
            default:
                //System.out.println("Default layout touchevent");
                break;
        }
    }

    private void handleLayoutTouchRelease(float y, TextView picker, LinearLayout layout, final Player player, final PlayerField field) {
        setUpdateTextViewTexts(mPullToRefresh);
        if (mUpdating)
            resetDuel();
        else if (!mSideSwipe)
            peripheralTouch(y, picker, layout, player, field);
        else
            scaleTextView(picker, Constants.SCALE_DOWN);
        binding.wrapper.scrollTo(0, 0);
        mUpdating = false;
        mSideSwipe = false;
    }

    private void recordTouchStart(MotionEvent motionEvent, TextView picker) {
        mPickerY = motionEvent.getY();
        mPickerX = motionEvent.getX();
        mPickerLastX = motionEvent.getX();
        scaleTextView(picker, Constants.SCALE_UP);
    }

    private void sideSwipe(float x) {
        if (!mSpun && Math.abs(x - mPickerLastX) > mScreenWidth / 40f)
            mSideSwipe = true;
        if (mSideSwipe) {
            //System.out.println((int) (x - mPickerLastX));
            if (!mUpdating)
                binding.wrapper.scrollBy((int) -(x - mPickerLastX) / 2, 0);
            //System.out.println("Side swiping");
            if (Math.abs(x - mPickerX) > mScreenWidth / 3.5f) {
                setUpdateTextViewTexts(mReleaseToRefresh);
                mUpdating = true;
            } else {
                setUpdateTextViewTexts(mPullToRefresh);
                mUpdating = false;
            }
            mPickerLastX = x;
        }
    }

    private void verticalSwipe(float y, TextView picker, Player player, PlayerField field) {
        if (!mSideSwipe && Math.abs(y - mPickerY) > mScreenHeight / 35f) {
            mSpun = true;
            //System.out.println("Changing picker value");
            changePickerValue(picker, y < mPickerY, player, field);
            mPickerY = y;
        }
    }

    private void peripheralTouch(float y, TextView picker, LinearLayout layout, Player player, PlayerField field) {
        if (mSpun) {
            mSpun = false;
            scaleTextView(picker, Constants.SCALE_DOWN);
        } else {
            int[] coordinates = {0, 0};
            //System.out.println("Layout touch, coordinates and y: " + coordinates + " " + y);
            scaleTextView(picker, Constants.SCALE_DOWN);
            changePickerValue(picker, y < (coordinates[1] + layout.getHeight()) / 2f, player, field);

        }
    }

    private void changePickerValue(TextView picker, boolean add, Player player, PlayerField field) {
        if (mSettings.getHapticFeedbackEnabled()) {
            picker.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        }
        int lifeTotal = getPickerValue(picker);
        if (add)
            lifeTotal++;
        else
            lifeTotal--;
        picker.setText(String.format(Locale.getDefault(), "%d", lifeTotal));

        mGameState.update(player, field, Integer.toString(lifeTotal));

        // Move into update
        mGameState.getHistory().add(getTotals());
        if (mGameState.isLethal()) {
            shakeLayout();
        }
    }

    private void shakeLayout() {
        AnimationSet animations = new AnimationSet(false);
        Animation shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake_main_layout);
        Animation unShakeAnimation = AnimationUtils.loadAnimation(this, R.anim.unshake_main_layout);
        animations.addAnimation(shakeAnimation);
        animations.addAnimation(unShakeAnimation);
        binding.leftUpdate.startAnimation(animations);
    }

    private int getPickerValue(TextView picker) {
        return Integer.parseInt(picker.getText().toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        //System.out.println("RESUMING");
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
        restoreSettings();
        hideSystemUI();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.reset) {
            resetDuel();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void resetDuel() {
        mGameState = mSettings.buildNewGame();

        setLifeTotals();

        ((HistoryListAdapter) binding.rightDrawer.getAdapter()).clear();
        instantiateArrayLists();
        mGameState.getHistory().add(getTotals());
        ((SettingsListAdapter) binding.leftDrawer.getAdapter()).notifyDataSetChanged();
        ((HistoryListAdapter) binding.rightDrawer.getAdapter()).notifyDataSetChanged();
    }

    public GameSnapshot getTotals() {
        return new GameSnapshot(
                binding.lifePicker1.getText().toString(),
                binding.lifePicker2.getText().toString(),
                binding.poisonPicker1.getText().toString(),
                binding.poisonPicker2.getText().toString(),
                binding.energyPicker1.getText().toString(),
                binding.energyPicker2.getText().toString(),
                System.currentTimeMillis()
        );
    }


    private void setLifeTotals() {
        PlayerState playerOne = mGameState.getPlayerOne();
        PlayerState playerTwo = mGameState.getPlayerTwo();

        binding.lifePicker1.setText(playerOne.getCurrentLife());
        binding.lifePicker2.setText(playerTwo.getCurrentLife());
        binding.poisonPicker1.setText(playerOne.getCurrentPoison());
        binding.poisonPicker2.setText(playerTwo.getCurrentPoison());
        binding.energyPicker1.setText(playerOne.getCurrentEnergy());
        binding.energyPicker2.setText(playerTwo.getCurrentEnergy());
    }

    private void spinResetArrows() {
        ImageView leftArrow = binding.updateArrowLeft;
        ImageView rightArrow = binding.updateArrowRight;
        RotateAnimation r = new RotateAnimation(mCurrentRotation, mCurrentRotation + 180.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        r.setDuration((long) 300);
        r.setRepeatCount(0);
        r.setFillAfter(true);
        mCurrentRotation += 180.0f;
        leftArrow.startAnimation(r);
        rightArrow.startAnimation(r);
    }

    private void scaleTextView(TextView view, boolean scaleUp) {
        int anim = scaleUp ? R.anim.text_scale_up : R.anim.text_scale_down;
        view.startAnimation(AnimationUtils.loadAnimation(this, anim));
    }
}
