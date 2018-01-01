package com.numbersixgames.d64master;

import com.numbersixgames.d64master.d64lib.FileTableEntry;

/**
 * Created by Six on 12/31/2017.
 */

public interface FileStatusUpdateListener {
    public void OnFileStatusUpdated(FileTableEntry fileTableEntry);
}
