package geometry;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import org.opencv.core.Core;
import org.opencv.core.Mat;

public class CircleTest
{

	public static void main(String[] args)
	{
		System.out.println( new Date());
		/* try
		   {
		         System.loadLibrary( Core.NATIVE_LIBRARY_NAME );   
		   } 
		   catch (Exception e)
		   {
		         System.out.println("Error: " + e.getMessage());
		   }
		 */
		 int count = 0;
		Ellipse2D circle = new Ellipse2D.Double(0, 0, 100,100);
		//Circle c = new Circle (50,50,50,new Mat ());
		//System.out.println("UWAGA " + c.circle.contains(1,41));
		
		PathIterator iterator = circle.getPathIterator(null);
		ArrayList <Point []> lista = new ArrayList <Point []> ();
		
		int cubic = PathIterator.SEG_CUBICTO,
			quad =	PathIterator.SEG_QUADTO,
			line = PathIterator.SEG_LINETO,
			move = PathIterator.SEG_MOVETO,
			end = PathIterator.SEG_CLOSE;
		
		Bezier bezier = new Bezier();
		LinkedList <Point> controlPoints = new LinkedList <Point> ();
				
		double [] coords = new double [6];
		Point currentPoint = null, moveToPoint = null;
		int segmentType = 0;
		while ((segmentType = iterator.currentSegment(coords)) != PathIterator.SEG_CLOSE)
		{
			System.out.println("NEW SEGMENT : " + checkSegmentType(segmentType));

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
				Line2D line2D = new Line2D.Double(currentPoint.x, currentPoint.y, moveToPoint.x, moveToPoint.y);
				LinkedList <Point> linePoints = new LinkedList<>();
				
				double [] lineCoords = new double [6];
				PathIterator lineIterator = line2D.getPathIterator(null);
				while (lineIterator.isDone())
				{
					System.out.println(lineIterator.currentSegment(lineCoords));
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
			}
			
			controlPoints.clear();
			controlPoints.add(currentPoint);
			iterator.next();
		}
		
		
/*		for (int x = 0; x < 1000; x++);
		for (int i = 0; i < 200; i++)
		{
			for (int j = 0; j < 200; j++)
			{
				if (circle.contains(i,j))
				{
					System.out.println(i + " " + j);
					count++;
				}
			}
		}*/
		System.out.println( new Date());

		//System.out.println(c.);
	}
	
	
	private static String checkSegmentType (int ret)
	{
		switch (ret)
		{
			case 0: return "MOVE TO";
			case 1: return "LINE TO";
			case 2: return "QUAD TO";
			case 3: return "QUBIC TO";
			case 4: return "CLOSE";
			default: return null;
			
		}
	}

}
