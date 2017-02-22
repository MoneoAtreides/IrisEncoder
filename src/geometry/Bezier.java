package geometry;

import java.util.LinkedList;

public class Bezier 
{	
	private double krok = 0;
	
	public Bezier ()
	{
		krok = 0.1;
	}
	
	public Bezier (double k)
	{
		krok = k;
	}
	
	public Point [] policzWszystko (LinkedList <Point> lista)
	{
		if (krok == 0) return null;
		Point P [] = new Point [(int) (1/krok)];
		
		Point P1 = lista.get(0),
			  P2 = lista.get(1),
			  P3 = lista.get(2),
			  P4 = null;
		
		if (lista.size() > 3)  P4 = lista.get(3);
		
		int indeks = 0;
		for (double i = 0; i < 1; i += krok)
		{
			if (indeks < P.length)
			{
				if (P4 != null) P [indeks++] = policzPunkt(i, P1, P2, P3, P4);
				else 			P [indeks++] = policzPunkt(i, P1, P2, P3);
			}
		}
		
		return P;
	}
	
	public Point [] policzDowolny (Point tablica [])
	{
		if (krok == 0) return null;
		Point P [] = new Point [(int) (1/krok)];
		
		
		double t = 0;
		int sumaX = 0, sumaY = 0;
		int indeks = 0;
		for (; t < 1; t += krok)
		{
			for (int i = 0; i < tablica.length; i++)
			{
				double px = policzB (i,tablica.length,t) *  tablica [i].x;
				double py = policzB (i,tablica.length,t) *  tablica [i].y;
				
				sumaX += px;
				sumaY += py;
				
				if (indeks < P.length)
					P [indeks++] = new Point ((int) px, (int) py);
			}
				
			
		}
		
		return P;
	}
	
	public double policzB (int i, int n, double t)
	{
		double pierwszy = Newton (i, n);
		double wynik = pierwszy * Math.pow(t, i) * Math.pow(1-t, n-i);
		
		return wynik;
	}
	
	public double Newton (int n, int k)
	{
		double licznik = silnia (n);
		double mianownik = silnia (k) * silnia (n-k);
		
		return licznik / mianownik;
	}
	
	public static double silnia (double x)
	{
		 	
		if (x < 1) return 1;
		
		else return x * (silnia (x-1));
		
	}
	
	public Point policzPunkt (double t, Point P1, Point P2, Point P3, Point P4)
	{
		Point P = new Point ();
		
		P.x = (int) (Math.pow (1-t, 3) * P1.x + 
				3*Math.pow(1-t, 2)*t*P2.x + 
						3 * (1-t) * Math.pow(t, 2) * P3.x + Math.pow(t, 3)*P4.x);
		
		P.y = (int) (Math.pow ((1-t), 3) * P1.y + 
				3*Math.pow(1-t, 2)*t*P2.y + 
						3 * (1-t) * Math.pow(t, 2) * P3.y + Math.pow(t, 3)*P4.y);
		
		return P;
	}
	
	public Point policzPunkt (double t, Point P1, Point P2, Point P3)
	{
		Point P = new Point ();
		
		P.x = (int) (Math.pow (1-t, 2) * P1.x + 
				2*t*(1-t)*P2.x + 
						Math.pow(t, 2) * P3.x);
		
		P.y = (int) (Math.pow ((1-t), 2) * P1.y + 
				2*t*(1-t)*P2.y + 
						Math.pow(t, 2) * P3.y);
		

		return P;
	}

	public void setKrok(double d)
	{
		krok = d;
	}
}
