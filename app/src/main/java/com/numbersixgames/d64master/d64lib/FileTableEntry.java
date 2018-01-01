package com.numbersixgames.d64master.d64lib;

/**
 * Created by Six on 12/28/2017.
 * Bytes: $00-1F: First directory entry
 00-01: Track/Sector location of next directory sector ($00 $00 if
 not the first entry in the sector)
 02: File type.
 Typical values for this location are:
 $00 - Scratched (deleted file entry)
 80 - DEL
 81 - SEQ
 82 - PRG
 83 - USR
 84 - REL
 Bit 0-3: The actual filetype
 000 (0) - DEL
 001 (1) - SEQ
 010 (2) - PRG
 011 (3) - USR
 100 (4) - REL
 Values 5-15 are illegal, but if used will produce
 very strange results. The 1541 is inconsistent in
 how it treats these bits. Some routines use all 4
 bits, others ignore bit 3,  resulting  in  values
 from 0-7.
 Bit   4: Not used
 Bit   5: Used only during SAVE-@ replacement
 Bit   6: Locked flag (Set produces ">" locked files)
 Bit   7: Closed flag  (Not  set  produces  "*", or "splat"
 files)
 03-04: Track/sector location of first sector of file
 05-14: 16 character filename (in PETASCII, padded with $A0)
 15-16: Track/Sector location of first side-sector block (REL file
 only)
 17: REL file record length (REL file only, max. value 254)
 18-1D: Unused (except with GEOS disks)
 1E-1F: File size in sectors, low/high byte  order  ($1E+$1F*256).
 The approx. filesize in bytes is <= #sectors * 254
 */

public class FileTableEntry {
    public D64FileType FileType;
    public boolean Splat;
    public boolean Scratched;
    public boolean Locked;
    public DiskPointer FirstSector;
    public String FileName;
    public byte[] PETSCIIFileName;
    public DiskPointer FirstRELSideSector;
    public int RELRecordLength;
    public byte[] GEOSBullshit;
    public int FileSize;

    public FileTableEntry(byte[] dataBytes){
        byte fileTypeByte = dataBytes[2];
        switch(fileTypeByte & 0x0F){
            case 0x00:
                FileType = D64FileType.DEL;
                break;
            case 0x01:
                FileType = D64FileType.SEQ;
                break;
            case 0x02:
                FileType = D64FileType.PRG;
                break;
            case 0x03:
                FileType = D64FileType.USR;
                break;
            case 0x04:
                FileType = D64FileType.REL;
                break;
            default:
                FileType = D64FileType.DEL;
                break;
        }
        Scratched = (FileType == D64FileType.DEL);
        Locked = ((fileTypeByte & 0x40) == 0x40);
        Splat = ((fileTypeByte & 0x80) != 0x80);
        FirstSector = new DiskPointer(dataBytes[3],dataBytes[4]);
        FileName = "";
        for (int i=0x05;i<=0x14;i++){
            if (dataBytes[i]>0) FileName += (char)dataBytes[i];
        }
        //1E-1F: File size in sectors, low/high byte  order  ($1E+$1F*256).
        FileSize = ((dataBytes[0x1e] & 0xff)+((dataBytes[0x1f] & 0xff)*256)*254);
    }

}
