package com.numbersixgames.d64master;

import android.content.Context;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Created by Six on 12/28/2017.
 */

public class FileTools {
    public static byte[] ReadFileAsByteArray(Context context, Uri filename)
    {
        byte[] byteArray = null;
        try
        {
            InputStream  inputStream = context.getContentResolver().openInputStream(filename);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024*8];
            int bytesRead =0;

            while ((bytesRead = inputStream.read(b)) != -1)
            {
                bos.write(b, 0, bytesRead);
            }

            byteArray = bos.toByteArray();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return byteArray;
    }
    public static byte[] ReadAssetAsByteArray(Context context, String filename)
    {
        byte[] byteArray = null;
        try
        {

            InputStream inputStream = context.getAssets().open(filename);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024*8];
            int bytesRead =0;

            while ((bytesRead = inputStream.read(b)) != -1)
            {
                bos.write(b, 0, bytesRead);
            }

            byteArray = bos.toByteArray();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return byteArray;
    }
}
