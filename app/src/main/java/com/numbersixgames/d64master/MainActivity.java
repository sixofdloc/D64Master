package com.numbersixgames.d64master;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.numbersixgames.d64master.d64lib.D64File;
import com.numbersixgames.d64master.d64lib.FileTableEntry;

import java.util.ArrayList;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity implements FileStatusUpdateListener {


    private static final int INTENT_LAUNCH_WITH_PERMISSION = 0;
    private static final int  INTENT_LAUNCH_DENY_PERMISSION = 1;
    private static final int  TEST_LAUNCH =2;


    private int launchType = TEST_LAUNCH;

    TextView tvDiskTitle;
    TextView tvDiskId;
    TextView tvBlocksFree;
    ListView lvDirectory;
    TextView tvLoadAddress;
    TextView tvTitlePadding;

    D64File _d64File;
    ArrayList<FileTableEntry>  _directory;
    DirectoryListAdapter _directoryAdapter;
    Intent _intent;
    byte[] fileBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitTextFields();
        launchType = CheckInvocationAndPermissions();
        if (launchType != INTENT_LAUNCH_DENY_PERMISSION){
            ProcessD64();
        }
    }

    private void ProcessD64(){
        if (ReadD64File()) {
            DisplayD64File();
        }
    }

    private void InitTextFields() {
        Typeface c64Font = Typeface.createFromAsset(getAssets(), "fonts/C64_Pro_Mono-STYLE.ttf");
        tvDiskTitle = findViewById(R.id.tvDiskTitle);
        tvDiskId = findViewById(R.id.tvDiskId);
        tvBlocksFree = findViewById(R.id.tvBlocksFree);
        tvLoadAddress = findViewById(R.id.tvLoadAddress);
        lvDirectory = findViewById(R.id.lvDir);
        tvTitlePadding = findViewById(R.id.tvTitlePadding);
        tvDiskTitle.setTypeface(c64Font);
        tvDiskId.setTypeface(c64Font);
        tvBlocksFree.setTypeface(c64Font);
        tvLoadAddress.setTypeface(c64Font);
        tvTitlePadding.setTypeface(c64Font);
    }

    private int CheckInvocationAndPermissions(){
        _intent = getIntent();
        if (_intent.getDataString() != null) {
            int permissionGranted =ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionGranted!=PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    return INTENT_LAUNCH_DENY_PERMISSION;
                } else {
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},69);
                    return INTENT_LAUNCH_DENY_PERMISSION;
                }
            } else {
                return INTENT_LAUNCH_WITH_PERMISSION;
            }
        } else {
            return TEST_LAUNCH;
        }
    }

    private boolean ReadD64File() {
        boolean result = false;
        try {
            if (launchType == INTENT_LAUNCH_WITH_PERMISSION) {
                fileBytes = FileTools.ReadFileAsByteArray(this, _intent.getData());
            } else if (launchType == TEST_LAUNCH) {
                fileBytes = FileTools.ReadAssetAsByteArray(this, "six_test.d64");
            }
            if (fileBytes.length > 170000) { //170K or so...
                _d64File = new D64File(fileBytes);
                _directory = _d64File.directory(false);
                result = true;
            }
        } catch (Exception e){
            result = false;
        }
        return result;
    }

    private void DisplayD64File() {
        _directoryAdapter = new DirectoryListAdapter(this, _directory);
        _directoryAdapter.setOnFileStatusUpdateListener(this);
        lvDirectory.setAdapter(_directoryAdapter);
        lvDirectory.setOnItemClickListener(_directoryAdapter);
        tvDiskTitle.setText("\"" + _d64File.diskTitle() + "\"");
        tvDiskId.setText(_d64File.diskId());
        tvBlocksFree.setText(Integer.toString(_d64File.BlocksFree()) +" BLOCKS FREE.");
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 69: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchType = INTENT_LAUNCH_WITH_PERMISSION;
                    ProcessD64();
                } else {
                    tvDiskTitle.setText("INSUFFICIENT PERMISSION");
                }
                return;
            }
        }
    }

    @Override
    public void OnFileStatusUpdated(FileTableEntry fileTableEntry) {
        tvLoadAddress.setText("Load Address: " + Integer.toString(_d64File.LoadAddressOfFile(fileTableEntry)));
    }
}
