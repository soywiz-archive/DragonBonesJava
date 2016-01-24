package dragonBones.objects;

import flash.errors.ArgumentError;

import java.util.ArrayList;
import java.util.Objects;

final public class AnimationData extends Timeline
	{
		public String name;
		public int frameRate;
		public double fadeTime;
		public int playTimes;
		//use frame tweenEase, NaN
		//overwrite frame tweenEase, [-1, 0):ease in, 0:line easing, (0, 1]:ease out, (1, 2]:ease in out
		public double tweenEasing;
		public boolean autoTween;
		public int lastFrameDuration;
		
		public ArrayList<String> hideTimelineNameMap;
		public ArrayList<String> hideSlotTimelineNameMap;
		
		private ArrayList<TransformTimeline> _timelineList;
		public ArrayList<TransformTimeline> getTimelineList()
		{
			return _timelineList;
		}
		
		private ArrayList<SlotTimeline> _slotTimelineList;
		public ArrayList<SlotTimeline> getSlotTimelineList()
		{
			return _slotTimelineList;
		}
		
		public AnimationData()
		{
			super();
			fadeTime = 0;
			playTimes = 0;
			autoTween = true;
			tweenEasing = Double.NaN;
			hideTimelineNameMap = new ArrayList<String>();
			hideSlotTimelineNameMap = new ArrayList<String>();

			_timelineList = new ArrayList<TransformTimeline>();
			_slotTimelineList = new ArrayList<SlotTimeline>();
		}
		
		@Override
		public void dispose()
		{
			super.dispose();
			
			hideTimelineNameMap.clear();
			hideTimelineNameMap = null;
			
			for (TransformTimeline timeline : _timelineList)
			{
				timeline.dispose();
			}
			_timelineList.clear();
			_timelineList = null;
			
			for (SlotTimeline slotTimeline : _slotTimelineList)
			{
				slotTimeline.dispose();
			}
			_slotTimelineList.clear();
			_slotTimelineList = null;
		}
		
		public TransformTimeline getTimeline(String timelineName)
		{
			int i = _timelineList.size();
			while(i -- > 0)
			{
				if(Objects.equals(_timelineList.get(i).name, timelineName))
				{
					return _timelineList.get(i);
				}
			}
			return null;
		}
		
		public void addTimeline(TransformTimeline timeline)
		{
			if(timeline == null)
			{
				throw new ArgumentError();
			}
			
			if(_timelineList.indexOf(timeline) < 0)
			{
				_timelineList.set(_timelineList.size(), timeline);
			}
		}
		
		public SlotTimeline getSlotTimeline(String timelineName)
		{
			int i = _slotTimelineList.size();
			while(i -- > 0)
			{
				if(Objects.equals(_slotTimelineList.get(i).name, timelineName))
				{
					return _slotTimelineList.get(i);
				}
			}
			return null;
		}
		
		public void addSlotTimeline(SlotTimeline timeline)
		{
			if(timeline == null)
			{
				throw new ArgumentError();
			}
			
			if(_slotTimelineList.indexOf(timeline) < 0)
			{
				_slotTimelineList.set(_slotTimelineList.size(), timeline);
			}
		}
	}
