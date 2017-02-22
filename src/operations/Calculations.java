package operations;

import geometry.Circle;

//import org.opencv.core.Point;

import geometry.LineIterator;
import geometry.Point;

import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Line2D.Double;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import javax.sound.sampled.Line;

import org.opencv.core.Size;

public class Calculations
{
	ImageOperations io;
	
	public Calculations(ImageOperations imageOperations)
	{
		this.io = imageOperations;
	}

	public static boolean fitsImage (double radius, double x0, double y0, Size imgSize)
	{
		if (x0 - radius >= 0 && x0 + radius < imgSize.width &&
			y0 - radius >= 0 && y0 + radius < imgSize.height)
			return true;
		
		else return false;
	}
	
	public static boolean fitsRect (double radius, double x0, double y0, Rectangle rect)
	{
		if (x0 - radius >= rect.getX() && x0 + radius < rect.getWidth() &&
				y0 - radius >= rect.getY() && y0 + radius < rect.getHeight())
				{
					return true;
				}
				
		return false;
	}
	
	public static Circle findCircleCenter (Point p1, Point p2, Point p3)
	{
		double denominator = p1.y * p3.x - p1.y * p2.x + p2.y * p1.x - 
							 p2.y * p3.x + p3.y * p2.x - p3.y * p1.x;
		
		double counterX1 = countCounterX (p2, p3, p2, p3, p1, p3, p1, p3),
			   counterX2 = countCounterX (p1, p2, p1, p2, p3, p2, p3, p2),
			   counterX3 = countCounterX (p3, p1, p3, p1, p2, p1, p2, p1),
			   counterY1 = countCounterY (p1, p3, p1, p3, p2, p3, p2, p3),
			   counterY2 = countCounterY (p3, p2, p3, p2, p1, p2, p1, p2),
			   counterY3 = countCounterY (p2, p1, p2, p1, p3, p1, p3, p1);
		
		double centerX = (counterX1/denominator + counterX2/denominator + counterX3/denominator) / 2,
				centerY = (counterY1/denominator + counterY2/denominator + counterY3/denominator) / 2;
		
		double radius = Math.sqrt(Math.pow (p1.x - centerX, 2) + Math.pow (p1.y - centerY, 2));

		return new Circle ((int) centerX, (int) centerY, (int) radius);
		
	}
	
	private static double countCounterX (Point p1, Point p2, Point p3, Point p4,
										  Point p5, Point p6, Point p7, Point p8)
	{
		return Math.pow(p1.x, 2) * p2.y + Math.pow(p3.y, 2) * p4.y -
						 Math.pow(p5.x, 2) * p6.y - Math.pow(p7.y, 2) * p8.y;
	}
	
	private static double countCounterY (Point p1, Point p2, Point p3, Point p4,
			 							  Point p5, Point p6, Point p7, Point p8)
	{
		return Math.pow(p1.x, 2) * p2.x + Math.pow(p3.y, 2) * p4.x -
				Math.pow(p5.x, 2) * p6.x - Math.pow(p7.y, 2) * p8.x;
	}
	
	public HashMap <Point, Point> convertToPolarCoords (Circle iris, Circle pupil, double radialRes, double angularRes)
	{
		//Point [] polarCoords = new Point [(int) (radialRes * angularRes)];
		HashMap <Point, Point> polar = new HashMap<>();
		ArrayList <Point> circlePixels = iris.getAngularPoints (512);
		
		for (double theta = 0; theta < 2*Math.PI; theta += 2 * Math.PI / angularRes)
		{
			double yP = Math.sin(theta) * pupil.getR();
			double xP = Math.cos(theta) * pupil.getR();
			
			Point pupilBorder = new Point ((int) (pupil.getX() + xP), 
											(int) (pupil.getY() - yP));						
			Point irisBorder = findIrisBorder (circlePixels, pupilBorder, pupil, iris, theta);
			Point [] radialPoints = new Point [(int) radialRes - 1];
			
			for (int i = 0; i < radialRes-1; i++)
			{
				int x1 = (int) pupilBorder.x, x2 = (int) irisBorder.x,
					 y1 =  (int) pupilBorder.y, y2 = (int) irisBorder.y;
				
				double x, y;
				
				if (x1 > x2)
				{
					x = x1 - (i * Math.abs(x2 - x1) / radialRes);
				}
				else
				{
					x = x1 + (i * Math.abs(x2 - x1) / radialRes);
				}
				
				if (y1 > y2)
				{
					y = y1 - (i * Math.abs (y2 - y1) / radialRes);
				}
				else
				{
					y = y1 + (i * Math.abs (y2 - y1) / radialRes);
				}
				
				radialPoints [i] = new Point (x,y);
			}
			
			double radius = Math.hypot(Math.abs(pupilBorder.x - irisBorder.x),
										Math.abs(pupilBorder.y - irisBorder.y));
			for (int i = 0; i < radialPoints.length; i++)
			{
				double distFromPupilBorder = Math.hypot(pupilBorder.x - radialPoints[i].x, 
														pupilBorder.y - radialPoints [i].y);
				double r = distFromPupilBorder / radius;
				polar.put(radialPoints [i], 
						new Point ((theta/(Math.PI*2)) * angularRes, 
									(r) * radialRes));
			}		
		}
		
		return polar;
	}
	
	private Point findIrisBorder(ArrayList <Point> circlePixels, Point pupilBorder,
									Circle pupil, Circle iris, double theta)
	{
		double h = Math.sin(theta) * iris.getR()*3,
				w = Math.cos(theta) * iris.getR()*3;
		
		Line2D.Double line;
		double x1, x2 = 0, y1, y2 = 0;
		x1 = pupil.getX();
		y1 = pupil.getY();
		x2 = x1 + w;
		y2 = y2 - h;
		
		line = new Line2D.Double (x1, y1, x2, y2);
		
		ArrayList <Point2D> listOfPoints2D = new ArrayList<>();
		
		LineIterator iterator = new LineIterator(line);
		while (iterator.hasNext())
		{
			Point2D next = iterator.next();
			if ((Math.hypot(next.getX() - pupil.getX(), 
							(next.getY() - pupil.getY()))) > pupil.getR())
			listOfPoints2D.add (next);
		}
		
		ArrayList <Point> listOfPoints = new ArrayList<>();
		for (Point2D point2D : listOfPoints2D)
		{
			listOfPoints.add(new Point (point2D.getX(), point2D.getY()));
		}
		
		for (Point p : circlePixels)
		{
			for (Point linePoint : listOfPoints)
			{
				for (int x = (int) linePoint.x-1; x < linePoint.x+1; x++)
				{
					for (int y = (int) linePoint.y-1; y < linePoint.y+1; y++)
					{
						if (x == p.x && y == p.y)
						{
							return p;
						}
					}
				}
			}
		}
		
		return null;
	}

	public LinkedList <Point> findPointsInsideCircle (Circle circle)
	{
		LinkedList <Point> circlePoints = new LinkedList<Point>();

		for (int x = 0; x < circle.getRect ().width; x++)
		{
		    for (int y = 0; y < circle.getRect ().height; y++)
		    {
		        double dx = x - circle.getX();
		        double dy = y - circle.getY();
		        double distanceSquared = dx * dx + dy * dy;

		        if (distanceSquared <= Math.pow (circle.getR(), 2))
		        {
		        	circlePoints.add (new Point (x,y));
		        }
		    }
		}
		
		return circlePoints;
	}
	
	public LinkedList <Point> findPointOnCircle (Circle circle)
	{
		LinkedList <Point> circlePoints = new LinkedList<Point>();
		int x0 = (int) circle.getRect().getX(),
				y0 = (int) circle.getRect().getY();
		int width =  circle.getRect().width,
			height = circle.getRect().height;
		
		for (int x = x0; x < x0 + width; x++)
		{
		    for (int y = y0; y < y0 + height; y++)
		    {
		        int dx = (int) Math.abs (x - circle.getX());
		        int dy = (int) Math.abs (y - circle.getY());
		        int distanceSquared = dx * dx + dy * dy;

		        if (distanceSquared == (int) Math.pow (circle.getR(), 2))
		        {
		        	circlePoints.add (new Point (x,y));
		        }
		    }
		}
		
		return circlePoints;
	}
	
	public double getLinearValue (Point p1, Point p2, double x)
	{
		double a = p2.y - p1.y / p2.x - p1.x,
				b = p1.y - a * p1.x;
		
		return a*x + b;
	}
}
