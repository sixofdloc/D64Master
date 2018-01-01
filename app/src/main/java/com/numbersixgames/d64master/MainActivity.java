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

    TextView tvDiskTitle;
    TextView tvDiskId;
    TextView tvBlocksFree;
    ListView lvDirectory;
    TextView tvLoadAddress;

    D64File _d64File;
    ArrayList<FileTableEntry>  _directory;
    DirectoryListAdapter _directoryAdapter;
    Intent _intent;
    byte[] fileBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvDiskTitle = (TextView) findViewById(R.id.tvDiskTitle);
        tvDiskId = (TextView) findViewById(R.id.tvDiskId);
        tvBlocksFree = (TextView) findViewById(R.id.tvBlocksFree);
        tvLoadAddress = (TextView) findViewById(R.id.tvLoadAddress);
        lvDirectory = (ListView) findViewById(R.id.lvDir);
        Typeface c64Font = Typeface.createFromAsset(getAssets(),"fonts/C64_Pro_Mono-STYLE.ttf");
        tvDiskTitle.setTypeface(c64Font);
        tvDiskId.setTypeface(c64Font);
        tvBlocksFree.setTypeface(c64Font);
        tvLoadAddress.setTypeface(c64Font);
        _intent = getIntent();
        if (_intent.getDataString() != null) {
            int permissionGranted =ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionGranted!=PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    tvDiskTitle.setText("READ EXTERNAL STORAGE PERMISSION MISSING");
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},69);
                }
            } else {
                ReadD64File(0);
                DisplayD64File();
            }
        } else {
           ReadD64File(1);
           DisplayD64File();
        }
    }

    private void ReadD64File(int ReadType) {
        if (ReadType == 0) {
            fileBytes = FileTools.ReadFileAsByteArray(this, _intent.getData());
        } else {
            fileBytes = FileTools.ReadAssetAsByteArray(this, "six_test.d64");
        }
    }

    private void DisplayD64File() {
        _d64File = new D64File(fileBytes);
        _directory = _d64File.directory(false);
        _directoryAdapter = new DirectoryListAdapter(this, _directory);
        _directoryAdapter.setOnFileStatusUpdateListener(this);
        lvDirectory.setAdapter(_directoryAdapter);
        lvDirectory.setOnItemClickListener(_directoryAdapter);
        tvDiskTitle.setText("\"" + _d64File.diskTitle() + "\"");
        tvDiskId.setText(_d64File.diskId());
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 69: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ReadD64File(0);
                    DisplayD64File();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    tvDiskTitle.setText("INSUFFICIENT PERMISSION");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void OnFileStatusUpdated(FileTableEntry fileTableEntry) {
        tvLoadAddress.setText("Load Address: " + Integer.toString(_d64File.LoadAddressOfFile(fileTableEntry)));
    }
}
