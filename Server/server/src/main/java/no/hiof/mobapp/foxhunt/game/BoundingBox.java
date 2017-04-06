package no.hiof.mobapp.foxhunt.game;

public class BoundingBox {
	
	// Old lower left - upper right boundary storage
	public LatLongCoord c1;
	public LatLongCoord c2;
	
	// New Point-list storage (poly)
	public LatLongCoord[] cords;
	
	// Cached points to speed up calculations (reduce search) - REMOVE?
	LatLongCoord c_t, c_r, c_b, c_l;
	
	public BoundingBox(LatLongCoord[] cords) {
		this.cords = cords;
		c_t = c_r = c_b = c_l = null;
	}
	
	public BoundingBox(double lon1, double lat1, double lon2, double lat2) {
		cords = new LatLongCoord[] {
									new LatLongCoord(lat2,lon1),
									new LatLongCoord(lat2,lon2),
									new LatLongCoord(lat1,lon2),
									new LatLongCoord(lat1,lon1)
		};
		
		c_t = c_r = c_b = c_l = null;
	}
	
	public LatLongCoord getPointLeft()
	{
		if (c_l == null) {
			c_l = cords[0];
		
			for (int i = 0; i < cords.length; i++)
				if (cords[i].lon < c_l.lon)
					c_l = cords[i];
		}
		
		return c_l;
	}
	
	public LatLongCoord getPointRight()
	{
		if (c_r == null) {
			c_r = cords[0];
		
			for (int i = 0; i < cords.length; i++)
				if (cords[i].lon > c_r.lon)
					c_r = cords[i];
		}
		
		return c_r;
	}
	
	public LatLongCoord getPointTop()
	{
		if (c_t == null) {
			c_t = cords[0];
		
			for (int i = 0; i < cords.length; i++)
				if (cords[i].lat > c_l.lat)
					c_t = cords[i];
		}
		
		return c_t;
	}
	
	public LatLongCoord getPointBottom()
	{
		if (c_b == null) {
			c_b = cords[0];
		
			for (int i = 0; i < cords.length; i++)
				if (cords[i].lat < c_l.lat)
					c_b = cords[i];
		}
		
		return c_b;
	}
	
	public LatLongCoord getRandomPointInside() {
		double newlon = getPointLeft().lon + Math.random()*(getPointRight().lon - getPointLeft().lon);
		double newlat = getPointBottom().lat + Math.random()*(getPointTop().lat -getPointBottom().lat);
		
		return new LatLongCoord(newlat,newlon);
		
	}
	

	
	
    public static LatLongCoord pointIntersectSegment(LatLongCoord p1, LatLongCoord p2, LatLongCoord p3) {

    	final double xDelta = p2.lon - p1.lon;
    	final double yDelta = p2.lat - p1.lat;

    	if ((xDelta == 0) && (yDelta == 0)) {
    	    throw new IllegalArgumentException("p1 and p2 cannot be the same point");
    	}

    	final double u = ((p3.lon - p1.lon) * xDelta + (p3.lat - p1.lat) * yDelta) / (xDelta * xDelta + yDelta * yDelta);

    	final LatLongCoord closestPoint;
    	if (u < 0) {
    	    closestPoint = p1;
    	} else if (u > 1) {
    	    closestPoint = p2;
    	} else {
    	    closestPoint = new LatLongCoord(p1.lat + u * yDelta, p1.lon + u * xDelta);
    	}

    	return closestPoint;
    }

    public LatLongCoord[] closestEdgeIntersectionCoordinates(LatLongCoord point) {
    	// Hardcoded right now
    	/*	P0--P1
    	 *  |    |
    	 *  P3--P2
    	 */ 
    	
    	LatLongCoord xAxis, xTmp;
    	LatLongCoord yAxis, yTmp;
    	
    	xAxis = pointIntersectSegment(cords[1], cords[2], point);
    	xTmp = pointIntersectSegment(cords[0], cords[3], point);
    	
    	yAxis = pointIntersectSegment(cords[0], cords[1], point);
    	yTmp = pointIntersectSegment(cords[3], cords[2], point);
    	
    	if (xAxis.getDistance(point) > xTmp.getDistance(point))
    		xAxis = xTmp;
    	
    	if (yAxis.getDistance(point) > yTmp.getDistance(point))
    		yAxis = yTmp;
    	
    	return new LatLongCoord[] {xAxis, yAxis};
    }
	
    public void clipPoint (LatLongCoord point) {
    	
    	LatLongCoord right, left;
    	LatLongCoord top, bottom;
    	
    	right = pointIntersectSegment(cords[1], cords[2], point);
    	left = pointIntersectSegment(cords[0], cords[3], point);
    	
    	top = pointIntersectSegment(cords[0], cords[1], point);
    	bottom = pointIntersectSegment(cords[3], cords[2], point);
    	
    	// Check to see if the point is closest to the left or right side
    	if (left.getDistance(point) < right.getDistance(point)) {
    		// Left side is closest
    		// Check if point is outside the polygon (to the left of the intersection point on segment 0-3)
    		if (point.lon < left.lon) {
    			// Point is outside - Clip to inside
    			point.lon = left.lon*1.000001;
    		}
    		
    	} else {
    		// Right side is closest
    		// Check if point is outside the polygon (to the right of the intersection point on segment 1-2)
    		if (point.lon > right.lon) {
    			// Point is outside - Clip to inside
    			point.lon = right.lon*0.999999;
    		}
    		
    	}
    	
    	// Check to see if the point is closest to the left or right side
    	if (top.getDistance(point) < bottom.getDistance(point)) {
    		// Top side is closest
    		// Check if point is outside the polygon (to the top of the intersection point on segment 0-1)
    		if (point.lat > top.lat) {
    			// Point is outside - Clip to inside
    			point.lat = top.lat*0.999999;
    		}
    		
    	} else {
    		// Bottom side is closest
    		// Check if point is outside the polygon (to the right of the intersection point on segment 2-3)
    		if (point.lat < bottom.lat) {
    			// Point is outside - Clip to inside
    			point.lat = bottom.lat*1.000001;
    		}
    		
    	}
    	    	
    }
	public double width() {
		return Math.abs(c2.lon - c1.lon);
	}
	
	public double height() {
		return Math.abs(c2.lat - c1.lat);
	}
	
	public double distanceFromEdge(LatLongCoord point)
	{
		/*
		double distToBottom = Math.abs(point.lat - c1.lat);
		double distToTop = Math.abs(point.lat - c2.lat);
		double distToRight = Math.abs(point.lon - c2.lat);
		double distToLeft = Math.abs(point.lon - c1.lon);
		
		return Math.min(LatLongCoord.metersPerLat * Math.min(distToBottom, distToTop), LatLongCoord.metersPerLon *  Math.min(distToRight, distToLeft));
		*/
		return closestEdgeCoordinate(point).getDistance(point);
	}
	
	public LatLongCoord closestEdgeCoordinate(LatLongCoord point)
	{
		double distToBottom = Math.abs(point.lat - c1.lat);
		double distToTop = Math.abs(point.lat - c2.lat);
		double distToRight = Math.abs(point.lon - c2.lon);
		double distToLeft = Math.abs(point.lon - c1.lon);
		
		LatLongCoord out = new LatLongCoord(0,0);
		
		// Check to see if we are closest to the (top|bottom) or (left|right)
		if (LatLongCoord.metersPerLat * Math.min(distToBottom, distToTop) < LatLongCoord.metersPerLon *  Math.min(distToRight, distToLeft))
		{
			// Top - Bottom closest - check which one
			if (distToBottom < distToTop)
			{
				//System.out.println("Bottom edge closest");
				out.lat = c1.lat;
			} else {
				//System.out.println("Top edge closest");
				out.lat = c2.lat;
			}
			out.lon = point.lon;
		} else {
			// Left -Right closest - check which one
			if (distToLeft < distToRight)
			{
				//System.out.println("Left edge closest");
				out.lon = c1.lon;
			} else {
				//System.out.println("Right edge closest");
				out.lon = c2.lon;
			}
			out.lat = point.lat;
		}
		
		//out.lat = (distToBottom < distToTop) ? c1.lat+distToBottom : c2.lat-distToTop;
		//out.lon = (distToLeft < distToRight) ? c1.lon+distToLeft : c2.lon-distToRight;
		
		return out;
		//return Math.min(LatLongCoord.metersPerLat * Math.min(distToBottom, distToTop), LatLongCoord.metersPerLon *  Math.min(distToRight, distToLeft));
	}
	
	public BoundingBox closestEdgeCoordinates(LatLongCoord point)
	{
		BoundingBox edges = new BoundingBox(0,0,0,0);
		
		double distToBottom = Math.abs(point.lat - c1.lat);
		double distToTop = Math.abs(point.lat - c2.lat);
		double distToRight = Math.abs(point.lon - c2.lon);
		double distToLeft = Math.abs(point.lon - c1.lon);
		
		// Top - Bottom closest - check which one
		if (distToBottom < distToTop)
		{
			//System.out.println("Bottom edge closest");
			edges.c1.lat = c1.lat;
		} else {
			//System.out.println("Top edge closest");
			edges.c1.lat = c2.lat;
		}
		edges.c1.lon = point.lon;
		
		// Left -Right closest - check which one
		if (distToLeft < distToRight)
		{
			//System.out.println("Left edge closest");
			edges.c2.lon = c1.lon;
		} else {
			//System.out.println("Right edge closest");
			edges.c2.lon = c2.lon;
		}
		edges.c2.lat = point.lat;
		
		return edges;
	}
}