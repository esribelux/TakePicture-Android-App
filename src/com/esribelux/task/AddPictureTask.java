package com.esribelux.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.esri.core.geometry.Point;

import android.content.Context;
import android.os.Environment;
import android.util.Base64;

public class AddPictureTask {
	private String _url;
	
	public AddPictureTask(String url)
	{
		this._url = url;
	}
	
	public String add(Point point,String comment,File file) throws Exception
	{
		SimpleDateFormat dateformat = new SimpleDateFormat("MMddyyyyhhmmss");
		HttpClient httpclient = new DefaultHttpClient();
		StringBuilder strNow = new StringBuilder( dateformat.format( new Date() ) );
		HttpPost httpost = new HttpPost(_url+"/exts/TakePictureSOE/add?nocache="+strNow);
		
		System.out.println(_url+"/exts/TakePictureSOE/add?nocache="+strNow);
		
		HttpResponse response;
        try {
        	
        	//FILE
        	
        	byte[] byteFile = new byte[(int) file.length()];
           	FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(byteFile);
        	
            String picture = Base64.encodeToString(byteFile,android.util.Base64.DEFAULT);
            
        	//POINT
        	JSONObject jsonPoint = new JSONObject();
        	jsonPoint.put("x",point.getX());
        	jsonPoint.put("y",point.getY());
        	
        	
        	
        	// Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("f", "json"));
            nameValuePairs.add(new BasicNameValuePair("point",jsonPoint.toString()));
            nameValuePairs.add(new BasicNameValuePair("comment",comment));
            nameValuePairs.add(new BasicNameValuePair("picture",picture));
            
            
            /*File myFile = getTempFile(this._context);
			myFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(myFile);
            
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fOut);
            outputStreamWriter.write(picture);
            outputStreamWriter.close();*/

        	httpost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            httpost.setHeader("Accept", "*/*");
            httpost.setHeader("Content-type", "application/x-www-form-urlencoded");
            
            response = httpclient.execute(httpost);
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            String json = reader.readLine();
            
            System.out.println(json);
            
            return json;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
        
	}
	
}
