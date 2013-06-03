package com.esribelux.takepicture;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.AttachmentManager;
import com.esri.android.map.AttachmentManager.AttachmentDownloadListener;
import com.esri.android.map.AttachmentManager.AttachmentRetrievalListener;
import com.esri.android.map.Callout;
import com.esri.android.map.LocationService;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISPopupInfo;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.popup.Popup;
import com.esri.android.map.popup.PopupContainer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
//import com.esri.core.map.FeatureSet;
import com.esri.core.map.AttachmentInfo;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.map.popup.PopupInfo;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;

import com.esribelux.dialog.PictureDialog;
import com.esribelux.takepicture.tools.MapTool;
import com.esribelux.takepicture.tools.Utils;
import com.esribelux.task.AddPictureTask;



public class TakePicActivity extends Activity {
	
	public static final int MSG_ERR = 0;
	public static final int MSG_ADDPICTURE = 1;
	public static final int MSG_QUERY = 2;
	
	private static final int TAKE_PHOTO_CODE = 1;
	
	String hostname = "ags10-1.esribelux.eu";
	
	/*======================*/
	/*  PARAMETERS
	/*======================*/
	
	Context context;
	ProgressDialog progressDialog;
	File currentPicture;
	MapView map ;
	Graphic currentGraphic;

    /*================*/
	/*   INITIALIZE   */
	/*================*/
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //Initialize
        this.progressDialog = new ProgressDialog(this);
        this.context = this;

		//Initialize map
   	 	map = (MapView) findViewById(R.id.map);
   	 	map.setExtent(new Envelope(469994.94963825,6598989.23646709,489524.545989109,6610465.56150307));
   	 	
   	 	ArcGISTiledMapServiceLayer topoLayer = new ArcGISTiledMapServiceLayer("http://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer");
   	 	topoLayer.setName("ArcGIS Online Topo");
   	 	topoLayer.setVisible(true);
		map.addLayer(topoLayer);
		
		ArcGISDynamicMapServiceLayer pictureLayer = new ArcGISDynamicMapServiceLayer("http://"+this.hostname+"/arcgis/rest/services/takePicture/MapServer");
		pictureLayer.setName("Picture");
		pictureLayer.setVisible(true);
		map.addLayer(pictureLayer);
		
		//Add map event
		map.setOnSingleTapListener(new OnSingleTapListener() {
			private static final long serialVersionUID = 1L;
			public void onSingleTap(float x, float y) {
				if (!map.isLoaded())
					return;
				
				Callout callout = map.getCallout();
				if (callout != null && callout.isShowing()) {
					callout.hide();
				}
				
				query(map.toMapPoint(new Point(x, y)));
			}
		});
		
   	 	//Initialize Gui
   	 	findViewById(R.id.buttonCamera).setOnClickListener(new View.OnClickListener() {
 		@Override
	 		public void onClick(View v) {
	 			takePicture();
	 		}
	 	});
   	 	
   	 	//Start GPS
   	 	this.startGps();
    }
    
    /*===============*/
    /* QUERY
    /*===============*/
    private void query(Point point)
    {
    	try
    	{
    		final Point mapPoint = point;
    		this.progressDialog = ProgressDialog.show(this, "Please wait",
                    "Search picture", true);
    		
    		new Thread((new Runnable() 
        	{
                @Override
                public void run() 
                {
		    		//Query
		    		Query query = new Query();				
					query.setReturnGeometry(true);
					query.setGeometry(MapTool.calculateEnvelopeWithTolerance(mapPoint, 35, map));
					
					String[] outfields = new String[] { "*"};
					query.setOutFields(outfields);
					query.setInSpatialReference(map.getSpatialReference());
					query.setOutSpatialReference(map.getSpatialReference());
		
					//Execute task
					Message msg = null;
					QueryTask queryTask = new QueryTask("http://"+hostname+"/arcgis/rest/services/takePicture/MapServer/0");
					try {
						FeatureSet fs = queryTask.execute(query);
						msg = mHandler.obtainMessage(MSG_QUERY,fs);
					} catch (Exception e) {
						msg = mHandler.obtainMessage(MSG_ERR,e.getMessage());
						e.printStackTrace();
					}
					
					mHandler.sendMessage(msg);
                }
        	})).start();
		
    	}
    	catch(Exception e)
    	{
    		Utils.showError(e.getMessage(),context);
    		e.printStackTrace();
    	}
    }
    
    private void queryResult(FeatureSet fs)
    {
    	Graphic[] graphics = fs.getGraphics();
    	if(graphics==null || graphics.length==0)
    		return;
    	this.currentGraphic = graphics[0];
    	
    	this.showCallout(this.currentGraphic);

    }
    
    
    
    
    public void showCallout(Graphic graphic)
    {
    	try
    	{
	    	//SHOW CALLOUT
	    	Callout callout = this.map.getCallout();
			String comment = (String) graphic.getAttributeValue("TEXT");
			
			Point mapPoint = (Point)graphic.getGeometry();
			
			callout.setContent(loadView(comment));
			callout.setStyle(R.xml.commentpop);
			callout.refresh();
			callout.show(mapPoint);
			
    	}
    	catch(Exception e)
    	{
    		Utils.showError(e.getMessage(),context);
    	}
    }
    
	private View loadView(String comment) throws FileNotFoundException {
		View view = LayoutInflater.from(context).inflate(R.layout.pictureview, null);

		final TextView txtComment = (TextView) view.findViewById(R.id.comment);
		txtComment.setText(comment);
			
		return view;
	}
	
	
    /*==============*/
    /* Take picture */
    /*==============*/
    public void takePicture()
    {
    	try
    	{
    		Point mapPoint = map.getLocationService().getPoint();
    		if(mapPoint!=null)
    		{
    			final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(Utils.getTempFile(this)) ); 
    			startActivityForResult(intent, TAKE_PHOTO_CODE);
    		}
    		else
    		{
    			Utils.showError("Gps location is not yet enabled.",context);
    		}
    	}
    	catch(Exception e)
    	{
    		Utils.showError(e.getMessage(),context);
    	}
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (resultCode == RESULT_OK) {
        switch(requestCode){
          case TAKE_PHOTO_CODE:
            showPicture();
          break;
        }
      }
    }

    public void showPicture()
    {
    	try
    	{
    		PictureDialog pictureDialog = new PictureDialog(this,Utils.getTempFile(this));
    		pictureDialog.show();
    	}
    	catch(Exception e)
    	{
    		Utils.showError(e.getMessage(),context);
    	}
    }
    
    /*=================*/
    /* Save picture
    /*=================*/
    
    public void savePicture(String comment)
    {
    	
    	try
    	{
    		this.currentPicture = Utils.getTempFile(this);
    		this.progressDialog = ProgressDialog.show(this, "Please wait",
                    "Save picture", true);
    		
    		final Point mapPoint = map.getLocationService().getPoint();
    		final String text=comment;
    		
        	new Thread((new Runnable() 
        	{
                @Override
                public void run() 
                {
                		Message msg = null;
    			    	try{
    			    		//SEND PICTURE
    				    	AddPictureTask pictureTask = new AddPictureTask("http://"+hostname+"/arcgis/rest/services/takePicture/MapServer");
    				    	String response = pictureTask.add(mapPoint,text, currentPicture);

    				    	msg = mHandler.obtainMessage(MSG_ADDPICTURE,response);
    			    	}catch(Exception e)
    			    	{
    			    		msg = mHandler.obtainMessage(MSG_ERR,e.getMessage());
    			    		e.printStackTrace();
    			    	}
    			    	
    			    	mHandler.sendMessage(msg);
                }
        	})).start();
    	}
    	catch(Exception e)
    	{
    		Utils.showError(e.getMessage(),context);
    		e.printStackTrace();
    	}
    }
    
    
    /*========*/
    /*   GPS  */
    /*========*/
    public void startGps()
    {
    	try {
    		LocationService ls = map.getLocationService();
    		ls.setAccuracyCircleOn(true);
    		ls.setAutoPan(true);
    		
    		if(!ls.isStarted())
    			ls.start();
    	}
    	catch(Exception e)
    	{
    		Utils.showError(e.getMessage(),context);
    	}
    }

    /*=================*/
    /* ROTATION SCREEN */
    /*=================*/
    @Override
    public void onConfigurationChanged(Configuration newConfig) 
    {
         super.onConfigurationChanged(newConfig);
    }
    
    //====================
    // THREAD HANDLER
    //====================
    final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
        	
        	if (progressDialog.isShowing()) {
            	progressDialog.dismiss();
            }
        	
            switch (msg.what) {
            case MSG_ERR:
                Utils.showError((String)msg.obj,context);
                break;
                
            case MSG_ADDPICTURE:
            	Utils.showError((String)msg.obj,context);
                break;
            case MSG_QUERY:
            	queryResult((FeatureSet)msg.obj);
                break;
            default: // should never happen
                break;
            }
        }
    };

    
	@Override 
	protected void onDestroy() { 
		System.out.println("onDestroy");
		super.onDestroy();
 }
	@Override
	protected void onPause() {
		System.out.println("onPause");
		super.onPause();
		//map.pause();
 }
	@Override 	protected void onResume() {
		System.out.println("onResume");
		super.onResume(); 
		//map.unpause();
	}

}