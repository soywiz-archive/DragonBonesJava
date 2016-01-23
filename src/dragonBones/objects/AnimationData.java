package dragonBones.objects;
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
			hideTimelineNameMap = new ArrayList<String>;
			hideTimelineNameMap.fixed = true;
			hideSlotTimelineNameMap = new ArrayList<String>;
			hideSlotTimelineNameMap.fixed = true;
			
			_timelineList = new ArrayList<TransformTimeline>;
			_timelineList.fixed = true;
			_slotTimelineList = new ArrayList<SlotTimeline>;
			_slotTimelineList.fixed = true;
		}
		
		@Override
		public void dispose()
		{
			super.dispose();
			
			hideTimelineNameMap.fixed = false;
			hideTimelineNameMap.length = 0;
			hideTimelineNameMap = null;
			
			_timelineList.fixed = false;
			for (TransformTimeline timeline : _timelineList)
			{
				timeline.dispose();
			}
			_timelineList.fixed = false;
			_timelineList.length = 0;
			_timelineList = null;
			
			_slotTimelineList.fixed = false;
			for (SlotTimeline slotTimeline : _slotTimelineList)
			{
				slotTimeline.dispose();
			}
			_slotTimelineList.fixed = false;
			_slotTimelineList.length = 0;
			_slotTimelineList = null;
		}
		
		public TransformTimeline getTimeline(String timelineName)
		{
			int i = _timelineList.length;
			while(i -- > 0)
			{
				if(_timelineList[i].name == timelineName)
				{
					return _timelineList[i];
				}
			}
			return null;
		}
		
		public void addTimeline(TransformTimeline timeline)
		{
			if(!timeline)
			{
				throw new ArgumentError();
			}
			
			if(_timelineList.indexOf(timeline) < 0)
			{
				_timelineList.fixed = false;
				_timelineList[_timelineList.length] = timeline;
				_timelineList.fixed = true;
			}
		}
		
		public SlotTimeline getSlotTimeline(String timelineName)
		{
			int i = _slotTimelineList.length;
			while(i -- > 0)
			{
				if(_slotTimelineList[i].name == timelineName)
				{
					return _slotTimelineList[i];
				}
			}
			return null;
		}
		
		public void addSlotTimeline(SlotTimeline timeline)
		{
			if(!timeline)
			{
				throw new ArgumentError();
			}
			
			if(_slotTimelineList.indexOf(timeline) < 0)
			{
				_slotTimelineList.fixed = false;
				_slotTimelineList[_slotTimelineList.length] = timeline;
				_slotTimelineList.fixed = true;
			}
		}
	}
