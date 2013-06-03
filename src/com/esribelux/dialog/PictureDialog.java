package com.esribelux.dialog;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.esribelux.takepicture.R;
import com.esribelux.takepicture.TakePicActivity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

public class PictureDialog {
	public Dialog dlgPicture;
	
	private Context _context;
	private EditText _text;
	
	public PictureDialog(Context context,File pictureFile) throws IOException
	{
		this._context = context;
		//Dialog
		dlgPicture = new Dialog(_context,android.R.style.Theme_Black_NoTitleBar_Fullscreen); 
		dlgPicture.setContentView(R.layout.picture);
		dlgPicture.setTitle("Save picture"); 
		
		//Listener
		this.initializeEventListener();
		
		Bitmap bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());
		Bitmap picture = android.graphics.Bitmap.createScaledBitmap(bitmap,400, 300,true);
		
	
	    FileOutputStream fOut = new FileOutputStream(pictureFile);

	    picture.compress(Bitmap.CompressFormat.JPEG, 80, fOut);
	    fOut.flush();
	    fOut.close();
	    
	    this._text = (EditText)dlgPicture.findViewById(R.id.pictureText);
		
		//File
		ImageView imageView = (ImageView)dlgPicture.findViewById(R.id.pictureImage);
		Drawable d = Drawable.createFromPath(pictureFile.getAbsolutePath());
		imageView.setImageDrawable(d);
	}
	
	private void initializeEventListener()
	{
		//Cancel button
		ImageButton buttonCancel = (ImageButton) dlgPicture.findViewById(R.id.pictureCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
        @Override
            public void onClick(View v) {
        		dlgPicture.dismiss();
            }
        });
        
        //Save button
        ImageButton buttonSave = (ImageButton) dlgPicture.findViewById(R.id.pictureSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
        @Override
            public void onClick(View v) {
        	
        	((TakePicActivity)_context).savePicture(_text.getText().toString());
        	dlgPicture.dismiss();
            }
        });
	}
	
	public void show()
	{
		dlgPicture.show();
	}
	
	public void hide()
	{
		dlgPicture.hide();
	}
	
}
