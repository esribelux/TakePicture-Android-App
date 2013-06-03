package com.esribelux.takepicture.tools;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

public class Utils {
	 /*=================*/
    /* ERROR           */
    /*=================*/
    public static void showError(String msg,Context context)
    {
    	Toast toast = Toast.makeText(context,msg,Toast.LENGTH_SHORT);
		toast.show();
    }
    
    public static File getTempFile(Context context)
    {
    	final File path = new File( Environment.getExternalStorageDirectory(), context.getPackageName() );
    	if(!path.exists())
    	{
    		path.mkdir();
    	}
    	
    	return new File(path, "image.tmp");
	}
}
