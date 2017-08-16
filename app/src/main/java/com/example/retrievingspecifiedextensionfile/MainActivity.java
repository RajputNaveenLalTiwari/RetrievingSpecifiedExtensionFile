package com.example.retrievingspecifiedextensionfile;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.text.format.Formatter;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";
    private Context context;
    private ContentResolver contentResolver;
    private Cursor cursor;
    private Uri uri;
    String[]    columns;
    String      whereClause;
    String[]    whereArgs;
    String      sortingOrder;

    static
    {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = MainActivity.this;
        contentResolver = getContentResolver();
        Log.i(TAG,"Hello");
        /*if (Build.VERSION.SDK_INT >= 11)
        {
            uri = MediaStore.Files.getContentUri("external");
            columns = new String[]{MediaStore.Files.FileColumns.DATA};
            whereClause = MediaStore.Files.FileColumns.MEDIA_TYPE + " =? ";
//            whereArgs = new String[]{MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")};
            whereArgs = new String[]{String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_NONE)};
            sortingOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";
            cursor = contentResolver.query(uri,columns,whereClause,whereArgs,sortingOrder);

            if( cursor != null && cursor.moveToFirst() )
            {
                do
                {
                    String data = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                    if(getMimeType(data) != null && getMimeType(data).equals("application/vnd.android.package-archive"))
                    {
                        Log.i(TAG, data + "  " + getMimeType(data));
                    }
                }while(cursor.moveToNext());
            }
        }
        else
        {

        }*/


        /*String s = getStoragepath();
        if(s!=null)
        {
            Log.i(TAG,s);
        }*/

        /*HashSet<String> paths = getExternalMounts();

        Iterator<String> iterator = paths.iterator();

        while (iterator.hasNext())
        {
            getDeviceInternalExternalMemoryInfo(iterator.next());
        }*/

//        readProcMountsFile();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {

            File[] fs = context.getExternalFilesDirs(null);
            String extPath = "";
            // at index 0 you have the internal storage and at index 1 the real external...
            if (fs != null && fs.length >= 2)
            {
                extPath = fs[0].getAbsolutePath();
                Log.e("SD Path",fs[0].getAbsolutePath());

                StatFs statFs = new StatFs(fs[0].getAbsolutePath());
                long    availableFreeMemoryOnFileSystem     = 0,
                        blockSizeOnFileSystem               = 0,
                        numberOfBlocksOnFileSystem          = 0,
                        availableMemory                     = 0,
                        totalMemory                         = 0;

                String  totalMemoryFormatFileSize       = null,
                        availableMemoryFormatFileSize   = null;

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                {
                    availableFreeMemoryOnFileSystem = statFs.getAvailableBlocksLong();
                    blockSizeOnFileSystem           = statFs.getBlockSizeLong();
                    numberOfBlocksOnFileSystem      = statFs.getBlockCountLong();
                }
                else
                {
                    availableFreeMemoryOnFileSystem = (long) statFs.getAvailableBlocks();
                    blockSizeOnFileSystem           = (long) statFs.getBlockSize();
                    numberOfBlocksOnFileSystem      = (long) statFs.getBlockCount();
                }

                totalMemory     = numberOfBlocksOnFileSystem * blockSizeOnFileSystem;
                availableMemory = availableFreeMemoryOnFileSystem * blockSizeOnFileSystem;

                totalMemoryFormatFileSize       = Formatter.formatFileSize(this,totalMemory);
                availableMemoryFormatFileSize   = Formatter.formatFileSize(this,availableMemory);

                Log.i(TAG,"Available Memory "+availableMemoryFormatFileSize);
                Log.i(TAG,"Total Memory "+totalMemoryFormatFileSize);
                Toast.makeText(context,"TM = "+totalMemoryFormatFileSize,Toast.LENGTH_LONG).show();
            }
        }
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public String getStoragepath() {
        String finalpath = null;
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;
            String[] patharray = new String[10];
            int i = 0;
            int available = 0;

            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                String mount = new String();
                if (line.contains("secure"))
                    continue;
                if (line.contains("asec"))
                    continue;

                if (line.contains("fat")) {// TF card
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        mount = mount.concat(columns[1] + "/requiredfiles");

                        patharray[i] = mount;
                        i++;

                        // check directory is exist or not
                        File dir = new File(mount);
                        if (dir.exists() && dir.isDirectory()) {
                            // do something here

                            available = 1;
                            finalpath = mount;
                            break;
                        } else {

                        }
                    }
                }
            }
            if (available == 1) {

            } else if (available == 0) {
                finalpath = patharray[0];
            }

        } catch (Exception e) {

        }
        return finalpath;
    }

    public static HashSet<String> getExternalMounts()
    {
        final HashSet<String> out = new HashSet<String>();
        String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
        String s = "";
        try {
            final Process process = new ProcessBuilder().command("mount")
                    .redirectErrorStream(true).start();
            process.waitFor();
            final InputStream is = process.getInputStream();
            final byte[] buffer = new byte[1024];
            while (is.read(buffer) != -1) {
                s = s + new String(buffer);
            }
            is.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // parse output
        final String[] lines = s.split("\n");
        for (String line : lines) {

            if (!line.toLowerCase(Locale.US).contains("asec")) {

                if (line.matches(reg)) {
                    String[] parts = line.split(" ");
                    for (String part : parts) {
                        if (part.startsWith("/"))
                            if (!part.toLowerCase(Locale.US).contains("vold"))
                                out.add(part);
                    }
                }
            }
        }
        return out;
    }

    private String getDeviceInternalExternalMemoryInfo(String path)
    {
        StatFs statFs = null;
        //if (android.os.Build.DEVICE.contains("samsung")|| android.os.Build.MANUFACTURER.contains("samsung"))
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            statFs = new StatFs(path);
        }
        else
        {
            statFs = new StatFs(System.getenv("SECONDARY_STORAGE"));
        }


        /*else if(android.os.Build.DEVICE.contains("samsung")|| android.os.Build.MANUFACTURER.contains("samsung"))
        {
            statFs = new StatFs(path);
        }*/

        long    availableFreeMemoryOnFileSystem     = 0,
                blockSizeOnFileSystem               = 0,
                numberOfBlocksOnFileSystem          = 0,
                availableMemory                     = 0,
                totalMemory                         = 0;

        String  totalMemoryFormatFileSize       = null,
                availableMemoryFormatFileSize   = null;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            availableFreeMemoryOnFileSystem = statFs.getAvailableBlocksLong();
            blockSizeOnFileSystem           = statFs.getBlockSizeLong();
            numberOfBlocksOnFileSystem      = statFs.getBlockCountLong();
        }
        else
        {
            availableFreeMemoryOnFileSystem = (long) statFs.getAvailableBlocks();
            blockSizeOnFileSystem           = (long) statFs.getBlockSize();
            numberOfBlocksOnFileSystem      = (long) statFs.getBlockCount();
        }

        totalMemory     = numberOfBlocksOnFileSystem * blockSizeOnFileSystem;
        availableMemory = availableFreeMemoryOnFileSystem * blockSizeOnFileSystem;

        totalMemoryFormatFileSize       = Formatter.formatFileSize(this,totalMemory);
        availableMemoryFormatFileSize   = Formatter.formatFileSize(this,availableMemory);

		Log.i(TAG,"Available Memory "+availableMemoryFormatFileSize);
		Log.i(TAG,"Total Memory "+totalMemoryFormatFileSize);

//        File a = new File(path).getTotalSpace();

//        Log.i(TAG,"Available Memory "+new File(path).getFreeSpace());
//        Log.i(TAG,"Total Memory "+new File(path).getTotalSpace());
        return null;
    }

    private String readProcMountsFile()
    {
        RandomAccessFile randomAccessFile = null;
        StringBuilder stringBuilder = new StringBuilder();
        String secondToken = "";
        try
        {
            randomAccessFile = new RandomAccessFile("/proc/mounts","r");
            String line;
//            String extPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            while ((line = randomAccessFile.readLine()) != null)
            {
                if(line.toLowerCase().contains("fuse"))
                {
                    stringBuilder.append(line).append("\n");
                    StringTokenizer stringTokenizer = new StringTokenizer(line," ");
                    stringTokenizer.nextToken();    // First Token
                    secondToken = stringTokenizer.nextToken();
                    Log.i("Token",secondToken);
                }

                if (line.toLowerCase().contains("vfat") &&  // Only vfat
                        line.toLowerCase().contains("vold") &&  // Only Volume Manager entries
                        !line.toLowerCase().contains("asec"))   // No Remove Ap2SD
                {
                    stringBuilder.append(line).append("\n");
                    StringTokenizer stringTokenizer = new StringTokenizer(line," ");
                    stringTokenizer.nextToken();    // First Token
                    secondToken = stringTokenizer.nextToken();
                }



               /* String[] split = line.split("\\s+");
                for(int i = 0; i < split.length - 1; i++)
                {
                    if(
                            split[i].contains("sdcard") ||
                            split[i].contains("_sd") ||
                            split[i].contains("extSd") ||
                            split[i].contains("_SD"))   // Add wildcards to match against here
                    {
                        String strMount = split[i];
                        String strFileSystem = split[i+1];

                        // Add to a list/array of mount points and file systems here...
                        Log.i("SDCard", "mount point: "+ strMount + " file system: " + strFileSystem);
                    }
                }*/
            }


        }
        catch (FileNotFoundException e)
        {
            Log.e(TAG,"FileNotFoundException \n"+e.getMessage());
        } catch (IOException e)
        {
            Log.e(TAG,"IOException \n"+e.getMessage());
        }
        finally
        {
            try
            {
                if (randomAccessFile != null)
                {
                    randomAccessFile.close();
                }
            }
            catch (IOException e)
            {
                Log.e(TAG,"IOException while closing RandomAccessFile \n"+e.getMessage());
            }
        }

        Log.i(TAG,secondToken);
    return secondToken;
    }

    /*public string getStorage()
    {
        try
        {
            var procb = new ProcessBuilder();
            procb.Command("mount");
            procb.RedirectErrorStream(true);
            Java.Lang.Process proc = procb.Start();

            proc.WaitFor();

            var resi = proc.InputStream;
            var rdr = new StreamReader(resi);
            string str = rdr.ReadToEnd();

            String[] lines = str.Split('\n');

            str = "";
            foreach (String line :lines)
            {
                if (line.ToLower().Contains("fuse"))
                {
                    String[] entry = line.Split(' ');
                    Log.i("DSService", entry[1]);
                    str += entry[1] + "\r\n";
                }

                if (line.ToLower().Contains("vfat") &&  // Only vfat
                        line.ToLower().Contains("vold") &&  // Only Volume Manager entries
                        !line.ToLower().Contains("asec"))   // No Remove Ap2SD
                {
                    String[] entry = line.Split(' ');
                    Log.i("DSService", entry[1]);
                    str += entry[1] + "\r\n";
                }
            }

            return str;
        }
        catch (Exception ex)
        {
            Log.e("DSService", ex.getMessage());
            return null;
        }
    }*/
}
