package dragonBones.objects;

import flash.geom.Point;

/**
 * 目前只支持两个控制点的贝塞尔曲线
 * @author CG
 */
public class CurveData
{
	private static final int SamplingTimes = 20;
	private static final double SamplingStep = 0.05;
	private boolean _dataChanged = false;

	private ArrayList<Point> _pointList = new ArrayList<Point>();
	public ArrayList<Point> sampling = new ArrayList<Point>(SamplingTimes);

	public CurveData()
	{
		for(int i=0; i < SamplingTimes-1; i++)
		{
			sampling[i] = new Point();
		}
		sampling.fixed = true;
	}

	public double getValueByProgress(double progress)
	{
		if(_dataChanged)
		{
			refreshSampling();
		}
		for (int i = 0; i < SamplingTimes-1; i++)
		{
			Point point = sampling[i];
			if (point.x >= progress)
			{
				if(i == 0)
				{
					return point.y * progress / point.x;
				}
				else
				{
					var prevPoint:Point = sampling[i-1];
					return prevPoint.y + (point.y - prevPoint.y) * (progress - prevPoint.x) / (point.x - prevPoint.x);
				}

			}
		}
		return point.y + (1 - point.y) * (progress - point.x) / (1 - point.x);
	}

	public void refreshSampling()
	{
		for(int i = 0; i < SamplingTimes-1; i++)
		{
			bezierCurve(SamplingStep * (i+1), sampling[i]);
		}
		_dataChanged = false;
	}

	private void bezierCurve(double t, Point outputPoint)
	{
		double l_t = 1-t;
		outputPoint.x = 3* point1.x*t*l_t*l_t + 3*point2.x*t*t*l_t + Math.pow(t,3);
		outputPoint.y = 3* point1.y*t*l_t*l_t + 3*point2.y*t*t*l_t + Math.pow(t,3);
	}

	public void setPointList(ArrayList<Point> value)
	{
		_pointList = value;
		_dataChanged = true;
	}

	public ArrayList<Point> getPointList()
	{
		return _pointList;
	}

	public boolean isCurve()
	{
		return point1.x != 0 || point1.y != 0 || point2.x != 1 || point2.y != 1;
	}
	public Point getPoint1()
	{
		return _pointList[0];
	}
	public Point getPoint2()
	{
		return _pointList[1];
	}
}
