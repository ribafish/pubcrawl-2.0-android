package com.ws1617.iosl.pubcrawl20.ScanQR;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.vision.text.Line;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.ws1617.iosl.pubcrawl20.R;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

/**
 * Created by Haneen on 12/7/2016.
 */

public class QRScannerDialog extends DialogFragment {


    //Views
    View rootView;
    Button closeBtn;

    LinearLayout inProcessView;
    LinearLayout notFoundLayout;


    boolean showNotFound = false;

    public QRScannerDialog() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        rootView = LayoutInflater.from(getActivity()).inflate(R.layout.view_scan_event
                , null);
        closeBtn = (Button) rootView.findViewById(R.id.invite_dialog_pub_done);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        alertDialog.setView(rootView);

        inProcessView = (LinearLayout) rootView.findViewById(R.id.spinner_placeholder);
        notFoundLayout = (LinearLayout) rootView.findViewById(R.id.notfound_placeholder);

        initNotFoundLayout(false);
        return alertDialog.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initLayout();

    }

    public void initLayout() {
        if (inProcessView == null || notFoundLayout == null) {
            if (!showNotFound) {
                inProcessView.setVisibility(View.GONE);
                notFoundLayout.setVisibility(View.VISIBLE);
            } else {
                inProcessView.setVisibility(View.VISIBLE);
                notFoundLayout.setVisibility(View.GONE);
            }
        }
    }

    public void initNotFoundLayout(boolean show) {
        showNotFound = show;
    }


}

