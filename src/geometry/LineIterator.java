package geometry;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Iterator;

public class LineIterator implements Iterator<Point2D>
{
	final static double DEFPREC = 1.0;
	final Line2D line;
	final double precision;
	
	final double dx, dy;
	final double sx, sy;
	
	double x,y,error;
	
	public LineIterator(Line2D line, double precision)
	{
		this.line = line;
		this.precision = precision;

		sx = line.getX1() < line.getX2() ? precision : -1 * precision;
		sy = line.getY1() < line.getY2() ? precision : -1 * precision;
			 
		dx =  Math.abs(line.getX2() - line.getX1());
		dy = Math.abs(line.getY2() - line.getY1());
		
		error = dx - dy;
		
		y = line.getY1();
		x = line.getX1();
	}
	
	public LineIterator(Line2D line)
	{
		this(line, DEFPREC);
	}
	
	@Override
	public boolean hasNext()
	{
	    return Math.abs( x - line.getX2()) > 0.9 || ( Math.abs(y - line.getY2()) > 0.9);
	}

	@Override
	public Point2D next() 
	{
		Point2D ret = new Point2D.Double(x, y);
		
		double e2 = 2*error;
		if (e2 > -dy) 
		{
			error -= dy;
			x += sx;
		}
		
		if (e2 < dx)
		{
			error += dx;
			y += sy;
		}
		
		return ret;
	}
}