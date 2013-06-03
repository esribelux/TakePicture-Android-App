package com.esribelux.takepicture.tools;

import com.esri.android.map.MapView;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;

public class MapTool {
	 public static Envelope calculateEnvelopeWithTolerance(Point point,int tolerancePixel,MapView map)
	    {
	        int mapWidth = map.getWidth();
	        int mapHeight = map.getHeight();
	        
	        Point topLeftCorner = map.getExtent().getPoint(0);
	        Point topRightCorner = map.getExtent().getPoint(1);
	        Point bottomRightCorner = map.getExtent().getPoint(2);
	        Point bottomLeftCorner = map.getExtent().getPoint(3);
	        
	        double distanceX = topLeftCorner.getX() - topRightCorner.getX();
	        double distanceY = topLeftCorner.getY() - topRightCorner.getY();
	        
	        double distance = Math.abs(distanceX)/mapWidth;
	        double tolerance = distance*tolerancePixel;
	        
	        double xmin = point.getX() - tolerance;
			double ymin = point.getY() - tolerance;
			double xmax = point.getX() + tolerance;
			double ymax = point.getY() + tolerance;
			
			return new Envelope(xmin,ymin,xmax,ymax);
	    }
}
