package com.yinheli.sxtcm.activity;

import com.yinheli.sxtcm.R;
import com.yinheli.sxtcm.R.layout;

import android.app.Activity;
import android.os.Bundle;

/**
 * Launcher Activity
 * 
 * @author yinheli <yinheli@gmail.com>
 *
 */
public class LauncherActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}