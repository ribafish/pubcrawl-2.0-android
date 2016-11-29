package com.ws1617.iosl.pubcrawl20.ScanQR;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ws1617.iosl.pubcrawl20.R;

public class ScanQRActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);
        getSupportActionBar().setTitle(R.string.scan_qr);
    }
}
