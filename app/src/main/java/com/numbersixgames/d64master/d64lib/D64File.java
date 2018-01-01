package com.numbersixgames.d64master.d64lib;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Six on 12/28/2017.

 Information Found at http://unusedino.de/ec64/technical/formats/d64.html

 Track #Sect #SectorsIn D64 Offset   Track #Sect #SectorsIn D64 Offset
 ----- ----- ---------- ----------   ----- ----- ---------- ----------
 1     21       0       $00000      21     19     414       $19E00
 2     21      21       $01500      22     19     433       $1B100
 3     21      42       $02A00      23     19     452       $1C400
 4     21      63       $03F00      24     19     471       $1D700
 5     21      84       $05400      25     18     490       $1EA00
 6     21     105       $06900      26     18     508       $1FC00
 7     21     126       $07E00      27     18     526       $20E00
 8     21     147       $09300      28     18     544       $22000
 9     21     168       $0A800      29     18     562       $23200
 10     21     189       $0BD00      30     18     580       $24400
 11     21     210       $0D200      31     17     598       $25600
 12     21     231       $0E700      32     17     615       $26700
 13     21     252       $0FC00      33     17     632       $27800
 14     21     273       $11100      34     17     649       $28900
 15     21     294       $12600      35     17     666       $29A00
 16     21     315       $13B00      36(*)  17     683       $2AB00
 17     21     336       $15000      37(*)  17     700       $2BC00
 18     19     357       $16500      38(*)  17     717       $2CD00
 19     19     376       $17800      39(*)  17     734       $2DE00
 20     19     395       $18B00      40(*)  17     751       $2EF00

 */

public class D64File {
    private int[] TrackOffsets = {
            0x00000,0x01500,0x02A00,0x03F00,0x05400,0x06900,0x07E00,0x09300,
            0x0A800,0x0BD00,0x0D200,0x0E700,0x0FC00,0x11100,0x12600,0x13B00,
            0x15000,0x16500,0x17800,0x18B00,0x19E00,0x1B100,0x1C400,0x1D700,
            0x1EA00,0x1FC00,0x20E00,0x22000,0x23200,0x24400,0x25600,0x26700,
            0x27800,0x28900,0x29A00,0x2AB00,0x2BC00,0x2CD00,0x2DE00,0x2EF00
    };

    private int[] SectorsPerTrack = {
            21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21,
            19, 19, 19, 19, 19, 19, 19, 18, 18, 18, 18, 18, 18, 17, 17, 17, 17,
            17, 17, 17, 17, 17, 17
    };


    public String fileName;
    private byte[] _fileData;

    public D64File(byte[] fileData){
        _fileData = fileData;
    }

    /*
     Bytes:$00-01: Track/Sector location of the first directory sector (should
                be set to 18/1 but it doesn't matter, and don't trust  what
                is there, always go to 18/1 for first directory entry)
            02: Disk DOS version type (see note below)
                  $41 ("A")
            03: Unused
         04-8F: BAM entries for each track, in groups  of  four  bytes  per
                track, starting on track 1 (see below for more details)
         90-9F: Disk Name (padded with $A0)
         A0-A1: Filled with $A0
         A2-A3: Disk ID
            A4: Usually $A0
         A5-A6: DOS type, usually "2A"
         A7-AA: Filled with $A0
         AB-FF: Normally unused ($00), except for 40 track extended format,
                see the following two entries:
         AC-BF: DOLPHIN DOS track 36-40 BAM entries (only for 40 track)
         C0-D3: SPEED DOS track 36-40 BAM entries (only for 40 track)
     */

    public String diskTitle() {
        String strDiskTitle = GetString(18,0,0x90,0x10);
        return strDiskTitle;

    }

    public String diskId(){
        return GetString(18,0,0xa2,8);
    }

    public String GetString(int track, int sector, int offset, int strlen){
        String response = "";
        byte[] diskData = GetBytes(track,sector,offset,strlen);

        for (int i = 0;i<strlen;i++){
                response += (char) (diskData[i] & 0xff);
        }
        return response;
    }

    public byte[] GetBytes(int track,int sector, int offset, int numbytes){
        byte[] sectorData = GetSector(track,sector);
        return Arrays.copyOfRange(sectorData, offset, offset+numbytes);
    }

    public byte[] PETSCIIdiskTitle() {
        return GetBytes(18,0,0x90,0x0f);
    }

    public byte[] PETSCIIdiskId(){
        return GetBytes(18,0,0xa2,0x08);
    }

    public byte[] GetSector(int track, int sector){
        if (sector <= SectorsPerTrack[track-1]) {
            int sectorBase = TrackOffsets[track-1] + (sector * 256);
            byte[] sectorBytes = Arrays.copyOfRange(_fileData, sectorBase, sectorBase + 256);
            return sectorBytes;
        } else {
            return new byte[1];
        }
    }

    public int BlocksFree(){
        byte[] BAM = GetSector(18,0);
        int blocksFree = 0;
        for (int i = 0x04;i<=0x8c;i+=4){
            if (i !=0x48) blocksFree +=(BAM[i] & 0xff);
        }
        return blocksFree;
    }
    public int LoadAddressOfFile(FileTableEntry fileTableEntry){
        byte[] loadAddressBytes = GetBytes(fileTableEntry.FirstSector.Track,fileTableEntry.FirstSector.Sector,2,2);
        return ((loadAddressBytes[1] & 0xff) * 256)+(loadAddressBytes[0] & 0xff);
    }

    public ArrayList<FileTableEntry> directory(boolean includeDEL) {
        ArrayList<FileTableEntry> response = new ArrayList<>();
        byte[] sectorBytes = GetSector(18,1);
        boolean doneflag = false;
        while(!doneflag){
            DiskPointer nextDirSector = new DiskPointer(sectorBytes[0],sectorBytes[1]);
            if (includeDEL) {
                response.add(new FileTableEntry(Arrays.copyOfRange(sectorBytes, 0x00, 0x20)));
                response.add(new FileTableEntry(Arrays.copyOfRange(sectorBytes, 0x20, 0x40)));
                response.add(new FileTableEntry(Arrays.copyOfRange(sectorBytes, 0x40, 0x60)));
                response.add(new FileTableEntry(Arrays.copyOfRange(sectorBytes, 0x60, 0x80)));
                response.add(new FileTableEntry(Arrays.copyOfRange(sectorBytes, 0x80, 0xa0)));
                response.add(new FileTableEntry(Arrays.copyOfRange(sectorBytes, 0xa0, 0xc0)));
                response.add(new FileTableEntry(Arrays.copyOfRange(sectorBytes, 0xc0, 0xe0)));
                response.add(new FileTableEntry(Arrays.copyOfRange(sectorBytes, 0xe0, 0x100)));
            } else {
                if ((sectorBytes[0x02] & 0x8f) > 0x00) response.add(new FileTableEntry(Arrays.copyOfRange(sectorBytes, 0x00, 0x20)));
                if ((sectorBytes[0x22] & 0x8f) > 0x00) response.add(new FileTableEntry(Arrays.copyOfRange(sectorBytes, 0x20, 0x40)));
                if ((sectorBytes[0x42] & 0x8f) > 0x00) response.add(new FileTableEntry(Arrays.copyOfRange(sectorBytes, 0x40, 0x60)));
                if ((sectorBytes[0x62] & 0x8f) > 0x00) response.add(new FileTableEntry(Arrays.copyOfRange(sectorBytes, 0x60, 0x80)));
                if ((sectorBytes[0x82] & 0x8f) > 0x00) response.add(new FileTableEntry(Arrays.copyOfRange(sectorBytes, 0x80, 0xa0)));
                if ((sectorBytes[0xa2] & 0x8f) > 0x00) response.add(new FileTableEntry(Arrays.copyOfRange(sectorBytes, 0xa0, 0xc0)));
                if ((sectorBytes[0xc2] & 0x8f) > 0x00) response.add(new FileTableEntry(Arrays.copyOfRange(sectorBytes, 0xc0, 0xe0)));
                if ((sectorBytes[0xe2] & 0x8f) > 0x00) response.add(new FileTableEntry(Arrays.copyOfRange(sectorBytes, 0xe0, 0x100)));
            }
            if (nextDirSector.Track > 0 && nextDirSector.Sector > 0){
                sectorBytes = GetSector(nextDirSector.Track,nextDirSector.Sector);
            } else {
                doneflag = true;
            }
        }
        return response;
    }



}
