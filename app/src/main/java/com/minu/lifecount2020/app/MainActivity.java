package com.minu.lifecount2020.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends SensorActivity {

    private LinearLayout mLifeLinearLayoutOne;
    private LinearLayout mLifeLinearLayoutTwo;
    private LinearLayout mPoisonLinearLayoutOne;
    private LinearLayout mPoisonLinearLayoutTwo;
    private LinearLayout mEnergyLinerLayoutOne;
    private LinearLayout mEnergyLinerLayoutTwo;
    private TextView mLifePickerOne;
    private TextView mLifePickerTwo;
    private TextView mPoisonPickerOne;
    private TextView mPoisonPickerTwo;
    private TextView mEnergyPickerOne;
    private TextView mEnergyPickerTwo;

    private ImageButton mSettingsButton;
    private ImageButton mHistoryButton;

    private LinearLayout mWrapper;

    private TextView mLeftUpdateTextView;
    private TextView mRighyUpdateTextView;

    private Settings mSettings;

    private GameState mGameState;

    private ArrayList<String> mOptions;

    private DrawerLayout mSettingsDrawerLayout;
    private ListView mSettingsDrawerList;
    private RelativeLayout mSettingsDrawer;

    private ListView mHistoryDrawerList;

    private int mScreenHeight;
    private int mScreenWidth;

    private boolean mSpun;
    private boolean mSideSwipe;

    private float mPickerY;
    private float mPickerX;
    private float mPickerLastX;
    private boolean mUpdating;

    private String mShowPoison;
    private String mHidePoison;

    private String mPullToRefresh;
    private String mReleaseToRefresh;

    private String mPoisonOption = mShowPoison;
    private int mPoisonOptionIndex;

    private float mCurrentRotation;

    private CountDownTimer mRoundTimer;
    private TextView mRoundTimerTextView;
    private boolean mTimerRunning;
    private String mEnergyOption;
    private String mHapticOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isTaskRoot()) {
            // Android launch bug
            finish();
            return;
        }

        hideSystemUI();

        bindElements();

        initElements();

        if (savedInstanceState != null) {
            mSettings = Settings.Companion.fromBundle(savedInstanceState);

            mGameState = GameState.Companion.fromBundle(savedInstanceState);

            setLifeTotals();

        } else {
            SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);

            mGameState = GameState.Companion.fromPreferences(settings);

            mSettings = Settings.Companion.fromPreferences(settings);

            setLifeTotals();

            resetTimer(true);

            restoreSettings();
        }
    }

    private void restoreSettings() {
        mPoisonOption = mShowPoison;

        displayPoison();

        displayEnergy();

        toggleTimer();

        BackgroundColor backgroundColor = mSettings.getBackgroundColor();

        mSettingsDrawerLayout.setBackgroundColor(backgroundColor.getColor());

        ((SettingsListAdapter) mSettingsDrawerList.getAdapter()).setSettings(mSettings);
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();

        mGameState.saveTo(editor);

        mSettings.saveTo(editor);

        editor.apply();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        mGameState.saveTo(savedInstanceState);

        mSettings.saveTo(savedInstanceState);

        super.onSaveInstanceState(savedInstanceState);
    }

    private void bindElements() {
        mLifeLinearLayoutOne = findViewById(R.id.first_life_picker_layout);
        mLifeLinearLayoutTwo = findViewById(R.id.second_life_picker_layout);

        mPoisonLinearLayoutOne = findViewById(R.id.first_poison_picker_layout);
        mPoisonLinearLayoutTwo = findViewById(R.id.second_poison_picker_layout);

        mEnergyLinerLayoutOne = findViewById(R.id.first_energy_picker_layout);
        mEnergyLinerLayoutTwo = findViewById(R.id.second_energy_picker_layout);

        mLifePickerOne = findViewById(R.id.life_picker_1);
        mLifePickerTwo = findViewById(R.id.life_picker_2);

        mPoisonPickerOne = findViewById(R.id.poison_picker_1);
        mPoisonPickerTwo = findViewById(R.id.poison_picker_2);

        mEnergyPickerOne = findViewById(R.id.energy_picker_1);
        mEnergyPickerTwo = findViewById(R.id.energy_picker_2);

        setInitialColors();

        mWrapper = findViewById(R.id.wrapper);

        mLeftUpdateTextView = findViewById(R.id.update);
        mRighyUpdateTextView = findViewById(R.id.update_2);

        mSettingsButton = findViewById(R.id.settings_button);
        mHistoryButton = findViewById(R.id.history_button);


        mSettingsDrawerLayout = findViewById(R.id.drawer_layout);

        mSettingsDrawer = findViewById(R.id.settings_drawer);

        mSettingsDrawerList = findViewById(R.id.left_drawer);
        mHistoryDrawerList = findViewById(R.id.right_drawer);
    }

    private void instantiateArrayLists() {
        if (mOptions == null) {
            mOptions = new ArrayList<>();
        } else {
            mOptions.clear();
        }

        mOptions.add(getString(R.string.new_duel));
        mOptions.add(getString(R.string.starting_life_total));
        mOptions.add(mPoisonOption);
        mOptions.add(getString(R.string.energy));
        mOptions.add(getString(R.string.color_scheme));
        mOptions.add(getString(R.string.throw_dice));
        mOptions.add(getString(R.string.round_timer));
        mOptions.add(getString(R.string.haptic_feedback));
    }

    private void setInitialColors() {
        int red = Color.parseColor(getString(R.string.color_red));
        int blue = Color.parseColor(getString(R.string.color_blue));
        mLifePickerOne.setTextColor(red);
        mPoisonPickerOne.setTextColor(red);
        mEnergyPickerOne.setTextColor(red);
        mLifePickerTwo.setTextColor(blue);
        mPoisonPickerTwo.setTextColor(blue);
        mEnergyPickerTwo.setTextColor(blue);
        Drawable arrowLeft = getResources().getDrawable(R.drawable.left_arrow);
        Drawable arrowRight = getResources().getDrawable(R.drawable.right_arrow);
        Drawable energyIconLeft = getResources().getDrawable(R.drawable.energy_icon_left);
        Drawable energyIconRight = getResources().getDrawable(R.drawable.energy_icon_right);
        if (arrowLeft != null)
            arrowLeft.setColorFilter(Color.parseColor(getString(R.string.color_text)),
                    PorterDuff.Mode.SRC_ATOP);
        if (arrowRight != null)
            arrowRight.setColorFilter(Color.parseColor(getString(R.string.color_text)),
                    PorterDuff.Mode.SRC_ATOP);
        if (energyIconLeft != null)
            energyIconLeft.setColorFilter(red,
                    PorterDuff.Mode.SRC_ATOP);
        if (energyIconRight != null)
            energyIconRight.setColorFilter(blue,
                    PorterDuff.Mode.SRC_ATOP);

        ((ImageView) findViewById(R.id.update_arrow_left)).setImageDrawable(arrowLeft);
        ((ImageView) findViewById(R.id.update_arrow_right)).setImageDrawable(arrowRight);

        ((ImageView) findViewById(R.id.energy_icon_one)).setImageDrawable(energyIconLeft);
        ((ImageView) findViewById(R.id.energy_icon_two)).setImageDrawable(energyIconRight);
    }

    private void collapseHistory() {
        long currentTime;
        long nextTime;
        ArrayList<String> history = mGameState.getHistory();
        for (int i = 0; i + 1 < history.size(); i++) {
            if (!isHistoryEntryRead(history.get(i))) {
                currentTime = parseTimeStamp(history.get(i));
                nextTime = parseTimeStamp(history.get(i + 1));
                if (nextTime - currentTime < 2000) {
                    history.remove(i);
                    i--;
                }
            }
        }
        for (int i = 0; i < history.size(); i++) {
            history.set(i, markedHistoryEntryRead(history.get(i)));
        }
        mGameState.setHistory(history);
        ((HistoryListAdapter) mHistoryDrawerList.getAdapter()).clear();
        ((HistoryListAdapter) mHistoryDrawerList.getAdapter()).addAll(history);
    }

    private void showHistory() {
        ((HistoryListAdapter) mHistoryDrawerList.getAdapter()).notifyDataSetChanged();
    }

    private long parseTimeStamp(String historyEntry) {
        String timeString = historyEntry.split(" ")[6];
        return Long.parseLong(timeString);
    }

    private boolean isHistoryEntryRead(String historyEntry) {
        //System.out.println(mHistory);
        String read = historyEntry.split(" ")[6];
        return read.compareTo(Constants.READ) == 0;
    }

    private String markedHistoryEntryRead(String historyEntry) {
        String[] split = historyEntry.split(" ");
        split[6] = Constants.READ;
        return split[0] + " " + split[1] + " " + split[2] + " " + split[3] + " " + split[4] + " " + split[5] + " " + split[6];
    }

    public void setmStartingLife(int startingLife) {
        mSettings.setStartingLife(startingLife);
    }

    private void initElements() {
        mSettings = Settings.Companion.getDefault();

        mGameState = mSettings.buildNewGame();

        instantiateArrayLists();
        mSettingsDrawerList.setAdapter(new SettingsListAdapter(this, mOptions));

        mHistoryDrawerList.setAdapter(new HistoryListAdapter(this, mGameState.getHistory()));

        mSettingsDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        setLayoutTouchListener(mLifeLinearLayoutOne, mLifePickerOne, Player.ONE, PlayerField.Life);
        setLayoutTouchListener(mLifeLinearLayoutTwo, mLifePickerTwo, Player.TWO, PlayerField.Life);
        setLayoutTouchListener(mPoisonLinearLayoutOne, mPoisonPickerOne, Player.ONE, PlayerField.Poison);
        setLayoutTouchListener(mPoisonLinearLayoutTwo, mPoisonPickerTwo, Player.TWO, PlayerField.Poison);
        setLayoutTouchListener(mEnergyLinerLayoutOne, mEnergyPickerOne, Player.ONE, PlayerField.Energy);
        setLayoutTouchListener(mEnergyLinerLayoutTwo, mEnergyPickerTwo, Player.TWO, PlayerField.Energy);
        setTextViewOnTouchListener(mLifePickerOne, Player.ONE, PlayerField.Life);
        setTextViewOnTouchListener(mLifePickerTwo, Player.TWO, PlayerField.Life);
        setTextViewOnTouchListener(mPoisonPickerOne, Player.ONE, PlayerField.Poison);
        setTextViewOnTouchListener(mPoisonPickerTwo, Player.TWO, PlayerField.Poison);
        setTextViewOnTouchListener(mEnergyPickerOne, Player.ONE, PlayerField.Energy);
        setTextViewOnTouchListener(mEnergyPickerTwo, Player.TWO, PlayerField.Energy);

        mPoisonLinearLayoutOne.setVisibility(View.GONE);
        mPoisonLinearLayoutTwo.setVisibility(View.GONE);

        mEnergyLinerLayoutOne.setVisibility(View.GONE);
        mEnergyLinerLayoutTwo.setVisibility(View.GONE);


        mSettings = Settings.Companion.getDefault();

        mPoisonOptionIndex = 2;

        mCurrentRotation = 0.0f;

        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettingsDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        mHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettingsDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });

        mSettingsDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                if (drawerView.equals(mHistoryDrawerList)) {
                    //System.out.println("Should show history");
                    //System.out.println(mHistory);
                    collapseHistory();
                    showHistory();
                    mHistoryDrawerList.post(new Runnable() {
                        @Override
                        public void run() {
                            mHistoryDrawerList.
                                    setSelection(mHistoryDrawerList.getAdapter().getCount());
                        }
                    });
                    mSettingsDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.RIGHT);
                }
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                mSettingsDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        mShowPoison = getString(R.string.poison);
        mHidePoison = getString(R.string.poison);
        mEnergyOption = getString(R.string.energy);
        mHapticOption = getString(R.string.haptic_feedback);

        mPullToRefresh = getString(R.string.pull_to_restart);
        mReleaseToRefresh = getString(R.string.pull_to_cancel);

        mPoisonOption = mShowPoison;

        Display display = getWindowManager().getDefaultDisplay();
        mScreenHeight = display.getWidth();
        mScreenWidth = display.getHeight();

        ((TextView) findViewById(R.id.twitter_link))
                .setMovementMethod(LinkMovementMethod.getInstance());

        mSettingsDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
        mSettingsDrawerLayout.setKeepScreenOn(true);

        instantiateRoundTimer();

    }

    private void instantiateRoundTimer() {
        mGameState.setRemainingMillis(Constants.BASE_ROUND_TIME_IN_MS);
        mRoundTimer = getNewTimer(Constants.BASE_ROUND_TIME_IN_MS);

        mRoundTimerTextView = findViewById(R.id.round_timer);

        mRoundTimerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTimerRunning)
                    mRoundTimer.cancel();
                else {
                    mRoundTimer = getNewTimer(mGameState.getRemainingMillis());
                    mRoundTimer.start();
                }
                mTimerRunning = !mTimerRunning;
            }
        });

        setTimerAnimations(true);

        mRoundTimerTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                resetTimer(false);
                return true;
            }
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
        return minutes * 60 * 1000;
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
            mPoisonLinearLayoutOne.setVisibility(View.GONE);
            mPoisonLinearLayoutTwo.setVisibility(View.GONE);
            mPoisonOption = mShowPoison;
        } else {
            mPoisonLinearLayoutOne.setVisibility(View.VISIBLE);
            mPoisonLinearLayoutTwo.setVisibility(View.VISIBLE);
            mPoisonOption = mHidePoison;
        }
        mOptions.set(mPoisonOptionIndex, mPoisonOption);

        ((SettingsListAdapter) mSettingsDrawerList.getAdapter()).notifyDataSetChanged();
    }

    private void displayEnergy() {
        if (!mSettings.getEnergyShowing()) {
            mEnergyLinerLayoutOne.setVisibility(View.GONE);
            mEnergyLinerLayoutTwo.setVisibility(View.GONE);
        } else {
            mEnergyLinerLayoutOne.setVisibility(View.VISIBLE);
            mEnergyLinerLayoutTwo.setVisibility(View.VISIBLE);
        }

        ((SettingsListAdapter) mSettingsDrawerList.getAdapter()).notifyDataSetChanged();
    }

    protected void checkShake(float x, float y, float z) {
        float acceleration = (float) Math.sqrt((double) x * x + y * y + z * z);
        if (Math.abs(acceleration - mGravity) > Constants.THROW_ACCELERATION)
            startDiceThrowActivity(mSettings.getBackgroundColor());
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
                    mSettingsDrawerLayout.closeDrawer(mSettingsDrawer);
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
                    mSettingsDrawerLayout.closeDrawer(mSettingsDrawer);
                    startDiceThrowActivity(mSettings.getBackgroundColor());
                    break;
                case 6:
                    break;
                case 7:
                    break;
                default:
                    mSettingsDrawerLayout.closeDrawer(mSettingsDrawer);
                    break;
            }
        }
    }

    private void startDiceThrowActivity(BackgroundColor backgroundColor) {
        Intent i = new Intent(this, DiceActivity.class);
        i.putExtra(Constants.BACKGROUND_WHITE, backgroundColor);
        startActivity(i);
        overridePendingTransition(R.anim.activity_slide_in_bottom, R.anim.activity_slide_out_top);
    }

    public void toggleBackground(BackgroundColor targetBackground) {
        mSettingsDrawerLayout.setBackgroundColor(targetBackground.getColor());
        mSettings.setBackgroundColor(targetBackground);
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
        picker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                float y = motionEvent.getY();
                float x = motionEvent.getX();
                handlePickerTouchEvent(x, y, action, motionEvent, picker, player, field);
                return true;
            }
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
        mWrapper.scrollTo(0, 0);
        mUpdating = false;
        mSideSwipe = false;
    }

    private void setUpdateTextViewTexts(String s) {
        if (mLeftUpdateTextView.getText().toString().compareTo(s) != 0) {
            mRighyUpdateTextView.setText(s);
            mLeftUpdateTextView.setText(s);
            spinResetArrows();
        }
    }

    private void setLayoutTouchListener(final LinearLayout layout, final TextView picker, final Player player, final PlayerField field) {
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                float y = motionEvent.getY();
                float x = motionEvent.getX();
                handleLayoutTouchEvent(x, y, action, motionEvent, picker, layout, player, field);
                return true;
            }
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
        mWrapper.scrollTo(0, 0);
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
        if (!mSpun && Math.abs(x - mPickerLastX) > mScreenWidth / 40)
            mSideSwipe = true;
        if (mSideSwipe) {
            //System.out.println((int) (x - mPickerLastX));
            if (!mUpdating)
                mWrapper.scrollBy((int) -(x - mPickerLastX) / 2, 0);
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
        if (!mSideSwipe && Math.abs(y - mPickerY) > mScreenHeight / 35) {
            mSpun = true;
            //System.out.println("Changing picker value");
            if (y > mPickerY)
                changePickerValue(picker, false, player, field);
            else
                changePickerValue(picker, true, player, field);
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
            if (y > (coordinates[1] + layout.getHeight()) / 2)
                changePickerValue(picker, false, player, field);
            else
                changePickerValue(picker, true, player, field);
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
        picker.setText(Integer.toString(lifeTotal));

        mGameState.update(player, field, Integer.toString(lifeTotal));

        // Move into update
        addToHistory(getTotals());
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
        findViewById(R.id.left_update).startAnimation(animations);
    }

    private int getPickerValue(TextView picker) {
        return Integer.parseInt(picker.getText().toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        //System.out.println("RESUMING");
        mSettingsDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
        mOptions.set(mPoisonOptionIndex, mPoisonOption);
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

        ((HistoryListAdapter) mHistoryDrawerList.getAdapter()).clear();
        instantiateArrayLists();
        addToHistory(getTotals());
        ((SettingsListAdapter) mSettingsDrawerList.getAdapter()).notifyDataSetChanged();
        ((HistoryListAdapter) mHistoryDrawerList.getAdapter()).notifyDataSetChanged();
    }

    public void addToHistory(String[] totals) {
        String timeStamp = Long.toString(System.currentTimeMillis());
        //System.out.println("Adding to history " + totals[0] + " " + totals[1] + " "
        //        + totals[2] + " " + totals[3] + " " + timeStamp);
        mGameState.getHistory().add(totals[0] + " " + totals[1] + " "
                + totals[2] + " " + totals[3] + " " + totals[4] + " " + totals[5] + " " + timeStamp);
    }

    public String[] getTotals() {
        return new String[]{mLifePickerOne.getText().toString(), mLifePickerTwo.getText().toString(),
                mPoisonPickerOne.getText().toString(), mPoisonPickerTwo.getText().toString(),
                mEnergyPickerOne.getText().toString(), mEnergyPickerTwo.getText().toString()};
    }


    private void setLifeTotals() {
        PlayerState playerOne = mGameState.getPlayerOne();
        PlayerState playerTwo = mGameState.getPlayerTwo();

        mLifePickerOne.setText(playerOne.getCurrentLife());
        mLifePickerTwo.setText(playerTwo.getCurrentLife());
        mPoisonPickerOne.setText(playerOne.getCurrentPoison());
        mPoisonPickerTwo.setText(playerTwo.getCurrentPoison());
        mEnergyPickerOne.setText(playerOne.getCurrentEnergy());
        mEnergyPickerTwo.setText(playerTwo.getCurrentEnergy());
    }

    private void spinResetArrows() {
        ImageView leftArrow = findViewById(R.id.update_arrow_left);
        ImageView rightArrow = findViewById(R.id.update_arrow_right);
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
        Animation scaleAnimation;
        if (scaleUp)
            scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.text_scale_up);
        else
            scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.text_scale_down);
        view.startAnimation(scaleAnimation);
    }
}
