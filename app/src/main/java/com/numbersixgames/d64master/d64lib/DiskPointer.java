package com.numbersixgames.d64master.d64lib;

/**
 * Created by Six on 12/29/2017.
 */

public class DiskPointer {
    public int Track;
    public int Sector;

    public DiskPointer(int track, int sector){
        Track = track;
        Sector = sector;
    }
}
