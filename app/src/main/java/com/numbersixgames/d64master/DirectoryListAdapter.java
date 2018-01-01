package com.numbersixgames.d64master;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.numbersixgames.d64master.d64lib.FileTableEntry;

import java.util.ArrayList;

/**
 * Created by Six on 12/29/2017.
 */

public class DirectoryListAdapter  extends ArrayAdapter implements AdapterView.OnItemClickListener {

    private ArrayList<FileTableEntry> _directoryFiles;
    private final Context _context;
    private int _highlightedItem = -1;

    private FileStatusUpdateListener _fileStatusUpdateListener = null;

    public DirectoryListAdapter(Context context, ArrayList<FileTableEntry> directoryFiles) {
        super(context, -1, directoryFiles);
        _context = context;
        _directoryFiles = directoryFiles;


    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.dirlistitem_template, parent, false);
        Typeface c64Font = Typeface.createFromAsset(_context.getAssets(),"fonts/C64_Pro_Mono-STYLE.ttf");
        TextView tvFilename = (TextView) rowView.findViewById(R.id.tvFilename);
        TextView tvFilesize = (TextView) rowView.findViewById(R.id.tvFileSize);
        TextView tvFiletype = (TextView) rowView.findViewById(R.id.tvFileType);
        TextView tvSplat = (TextView) rowView.findViewById(R.id.tvSplat);
        tvFilename.setTypeface(c64Font);
        tvFilesize.setTypeface(c64Font);
        tvFiletype.setTypeface(c64Font);
        tvSplat.setTypeface(c64Font);
        tvFilename.setText("\"" + _directoryFiles.get(position).FileName+"\"");
        tvFilesize.setText(Integer.toString(_directoryFiles.get(position).FileSize));
        tvFiletype.setText(_directoryFiles.get(position).FileType.toString());
        tvSplat.setText(_directoryFiles.get(position).Locked?">":" ");
        tvSplat.setText(_directoryFiles.get(position).Splat?"*":tvSplat.getText());
        if (position ==_highlightedItem){
            rowView.setBackgroundColor(Color.argb(255,80,80,255));
        }
        return rowView;
    }

    public void setOnFileStatusUpdateListener(FileStatusUpdateListener listener){
        _fileStatusUpdateListener = listener;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        _highlightedItem = i;
        this.notifyDataSetChanged();
        if (_fileStatusUpdateListener != null) _fileStatusUpdateListener.OnFileStatusUpdated(_directoryFiles.get(i));
    }
}


