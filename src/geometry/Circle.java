package geometry;

import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.opencv.core.Mat;
import org.opencv.core.Size;

public class Circle 
{
	Ellipse2D.Double circle;
	int r = 0;
	double x, y;
	Rectangle rect;
	
	public int getR() 
	{
		return r;
	}

	public void setR(int r)
	{
		this.r = r;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}
	
	
	//Rectangle rect;
	Mat sourceMat, minimumMat;
	private ArrayList<Point> circlePoints;
	
	public ArrayList<Point> getCirclePoints()
	{
		return circlePoints;
	}

	public Circle (final int x0, final int y0, final int r, Mat source)
	{
		sourceMat = source;
		this.r = r;
		this.x = x0;
		this.y = y0;
		
		circle = new Ellipse2D.Double (x-r, y-r, 2*r, 2*r);

		circlePoints = getAllCirclePixels();

		//rect = new Rectangle ((int) circle.getX(), (int) circle.getY(), (int) circle.getWidth(), (int) circle.getHeight());
	}
	
	public Circle (final int x0, final int y0, final int r)
	{
		this.r = r;
		this.x = x0;
		this.y = y0;
		
		circle = new Ellipse2D.Double (x-r, y-r, 2*r, 2*r);
		circlePoints = getAllCirclePixels();
		//rect = new Rectangle ((int) circle.getX(), (int) circle.getY(), (int) circle.getWidth(), (int) circle.getHeight());
	}
	
	public Rectangle getRect ()
	{
		return rect;
	}
	
	public void setRect (Rectangle rectangle)
	{
		rect = rectangle;
	}
	
	public void setCirclePoints (ArrayList <Point> points)
	{
		circlePoints = points;
	}
	
	public double getSumImproved ()
	{	
		int sum = 0;
		
			for (Point point : circlePoints)
			{
				try
				{
					if (point.x < sourceMat.cols() && point.y < sourceMat.rows())
					sum += sourceMat.get((int) point.y, (int) point.x) [0];
				}
				catch (NullPointerException e)
				{
					System.err.println("NULL TUTAJ O: " + point.x + " " + point.y);
				}
				catch (Exception unknown)
				{
					unknown.printStackTrace();
				}
				
				
			}
		
		return (double) sum / r;

	}

	public ArrayList <Point> getAngularPoints (double angularRes)
	{
		ArrayList <Point> points = new ArrayList<>();
		for (double theta = 0; theta < Math.PI*2; theta += (Math.PI*2)/angularRes)
		{
			double xc,yc;
			int w = (int) (Math.cos(theta) * r),
				h = (int) (Math.sin(theta) * r);
			
			points.add(new Point (x+w,y+h));
		}
	
		return points;	
	}
	
	public ArrayList <Point> getAllCirclePixels ()
	{
		PathIterator iterator = circle.getPathIterator(null);
		ArrayList<Point> circlePoints = new ArrayList <> ();
		
		Bezier bezier = new Bezier();
		LinkedList <Point> controlPoints = new LinkedList <Point> ();
				
		double [] coords = new double [6];
		Point currentPoint = null, moveToPoint = null;
		int segmentType = 0;
		while ((segmentType = iterator.currentSegment(coords)) != PathIterator.SEG_CLOSE)
		{
			for (double coord : coords)
			{
				coord = 0;
			}
			
			if (segmentType == PathIterator.SEG_MOVETO)
			{
				moveToPoint = new Point (coords [0], coords [1]);
				currentPoint = moveToPoint;
			}
			
			if (segmentType == PathIterator.SEG_CUBICTO || segmentType == PathIterator.SEG_QUADTO)
			{
				for (int i = 0; i < coords.length; i += 2)
				{
					if (coords [i] != 0 || coords [i+1] != 0)
					{
						controlPoints.add(new Point (coords [i], coords [i+1]));
						currentPoint = new Point (coords [i], coords [i+1]);
					}
				}
				
				Point [] array = bezier.policzWszystko(controlPoints);
				for (Point p : array)
				{
					if (p.x >= x - r + Math.sqrt(2) * r ||
						p.x <= x + r - Math.sqrt(2) * r)
					{
						circlePoints.add(p);
					}
				}
			}
			
			controlPoints.clear();
			controlPoints.add(currentPoint);
			iterator.next();
		}
			
		return circlePoints;
	}
	
	public ArrayList <Point> getAllCirclePixels (double krok)
	{
		PathIterator iterator = circle.getPathIterator(null);
		ArrayList<Point> circlePoints = new ArrayList <> ();
		
		Bezier bezier = new Bezier(krok);
		LinkedList <Point> controlPoints = new LinkedList <Point> ();
				
		double [] coords = new double [6];
		Point currentPoint = null, moveToPoint = null;
		int segmentType = 0;
		while ((segmentType = iterator.currentSegment(coords)) != PathIterator.SEG_CLOSE)
		{
			for (double coord : coords)
			{
				coord = 0;
			}
			
			if (segmentType == PathIterator.SEG_MOVETO)
			{
				moveToPoint = new Point (coords [0], coords [1]);
				currentPoint = moveToPoint;
			}
			
			if (segmentType == PathIterator.SEG_CUBICTO || segmentType == PathIterator.SEG_QUADTO)
			{
				for (int i = 0; i < coords.length; i += 2)
				{
					if (coords [i] != 0 || coords [i+1] != 0)
					{
						controlPoints.add(new Point (coords [i], coords [i+1]));
						currentPoint = new Point (coords [i], coords [i+1]);
					}
				}
				
				Point [] array = bezier.policzWszystko(controlPoints);
				for (Point p : array)
				{
					if (p.x >= x - r + Math.sqrt(2) * r ||
						p.x <= x + r - Math.sqrt(2) * r)
					{
						circlePoints.add(p);
					}
				}
			}
			
			controlPoints.clear();
			controlPoints.add(currentPoint);
			iterator.next();
		}
			
		return circlePoints;
	}
	
	public double getSum ()
	{	
		int sum = 0;
		//System.out.println("----- NEW CIRCLE ----");
		/*while ((returnValue = iterator.currentSegment(coords)) != PathIterator.SEG_CLOSE)
		{
			
			//if (returnValue == PathIterator.SEG_MOVETO) System.out.println("MOVETO : " + returnValue);
			//if (returnValue == PathIterator.seg)
			System.out.println("NEW SEGMENT : " + checkSegmentType(returnValue) + " (" + countSegment++ + "):");
				for (int i = 0; i < coords.length-1; i += 2)
				{
					if (coords [i] != 0 && coords [i+1] != 0)
					{	
						try
						{
							
							//System.out.println (coords [i+1] + " " + coords [i]);
							//if (coords [i] < 1) coords [i] = 1;
							//if (coords [i+1] < 1) coords [i+1] = 1;

							double [] intensity = sourceMat.get ((int) coords [i+1], (int) coords [i]);
							if (intensity == null) System.out.println("NULL " + coords [i] + " " + coords [i+1]);

							sum += intensity [0];
						}
						catch (Exception e)
						{
							for (int x = 0; x < coords.length-1; x++)
							{
								System.out.println("COORDS:");
								System.out.println(coords [0] + " " + coords [1]);
								System.out.println("---");
							}
						}
					}
				}
				
				iterator.next();			
		}
		*/
		
		
		PathIterator iterator = circle.getPathIterator(null);
		ArrayList <Point []> lista = new ArrayList <Point []> ();
		LinkedList <Point> circlePoints = new LinkedList<>();
		
		Bezier bezier = new Bezier();
		LinkedList <Point> controlPoints = new LinkedList <Point> ();
				
		double [] coords = new double [6];
		Point currentPoint = null, moveToPoint = null;
		int segmentType = 0;
		while ((segmentType = iterator.currentSegment(coords)) != PathIterator.SEG_CLOSE)
		{
			//System.out.println("NEW SEGMENT : " + checkSegmentType(segmentType));

			for (double coord : coords)
			{
				coord = 0;
			}
			
			
			if (segmentType == PathIterator.SEG_MOVETO)
			{
				moveToPoint = new Point (coords [0], coords [1]);
				currentPoint = moveToPoint;
			}
			else if (segmentType == PathIterator.SEG_CLOSE)
			{
				Line2D line2D = new Line2D.Double(currentPoint.x, currentPoint.y, 
													moveToPoint.x, moveToPoint.y);
				LinkedList <Point> linePoints = new LinkedList<>();
				
				double [] lineCoords = new double [6];
				PathIterator lineIterator = line2D.getPathIterator(null);
				while (lineIterator.isDone())
				{
					//System.out.println(lineIterator.currentSegment(lineCoords));
					lineIterator.next();
				}
			}
			
			if (segmentType == PathIterator.SEG_CUBICTO || segmentType == PathIterator.SEG_QUADTO)
			{
				for (int i = 0; i < coords.length; i += 2)
				{
					if (coords [i] != 0 || coords [i+1] != 0)
					{
						controlPoints.add(new Point (coords [i], coords [i+1]));
						currentPoint = new Point (coords [i], coords [i+1]);
					}
				}
				
				lista.add(bezier.policzWszystko(controlPoints));
				/*for (Point p : array)
				{
					if (p.x >= x - r + Math.sqrt(2) * r ||
						p.x <= x + r - Math.sqrt(2) * r)
					{
						circlePoints.add(p);
					}
				}*/
			}
			
			controlPoints.clear();
			controlPoints.add(currentPoint);
			iterator.next();
		}
			for (Point [] p : lista)
			for (Point point : p)
			{
				sum += sourceMat.get((int) point.y, (int) point.x) [0];
				
			}
		//HashMap <Point, Double> pixelsIntensities = new HashMap<>();
		/*for (int potentialX = (int) x-r; potentialX < x+r; potentialX++)
		{
			for (int potentialY = (int) y-r; potentialY < y+r; potentialY++)
			{
				double xSquared = Math.pow((x-potentialX), 2);
				double ySquared = Math.pow((y-potentialY), 2);
				
				if (xSquared + ySquared == r*r)
				{
					pixelsIntensities.put(new Point (potentialX, potentialY), 
											sourceMat.get((int) potentialY, (int) potentialX) [0]);
				}
				
				if (circle.contains(potentialX, potentialY))
				{
					sum += sourceMat.get(potentialY, potentialX) [0];
					
					pixelsIntensities.put(new Point (potentialX, potentialY), 
							sourceMat.get((int) potentialY, (int) potentialX) [0]);
				}
			}
		}*/
		
		return (double) sum / r;

	}
}
