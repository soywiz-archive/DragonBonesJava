package dragonBones;

import flash.errors.IllegalOperationError;
import flash.geom.ColorTransform;
import flash.geom.Matrix;

import dragonBones.animation.AnimationState;
import dragonBones.animation.SlotTimelineState;
import dragonBones.core.DBObject;
import dragonBones.core.dragonBones_internal;
import dragonBones.objects.DBTransform;
import dragonBones.objects.DisplayData;
import dragonBones.objects.Frame;
import dragonBones.objects.SlotData;
import dragonBones.objects.SlotFrame;
import dragonBones.utils.TransformUtil;

import java.util.ArrayList;

//import dragonBones.objects.FrameCached;
//import dragonBones.objects.TimelineCached;

//use namespace dragonBones_internal;

public class Slot extends DBObject
{
	/** @private Need to keep the reference of DisplayData. When slot switch displayObject, it need to restore the display obect's origional pivot. */
	private ArrayList<DisplayData> _displayDataList;
	/** @private */
	private double _originZOrder;
	/** @private */
	private double _tweenZOrder;
	/** @private */
	private double _originDisplayIndex;
	/** @private */
	protected double _offsetZOrder;

	protected ArrayList<Object> _displayList;
	protected int _currentDisplayIndex;
	protected ColorTransform _colorTransform;
	//TO DO: 以后把这两个属性变成getter
	//另外还要处理 isShowDisplay 和 visible的矛盾
	protected Object _currentDisplay;
	private boolean _isShowDisplay;

	//protected var _childArmature:Armature;
	protected String _blendMode;

	/** @private */
	public boolean _isColorChanged;
	private boolean _needUpdate;
	/** @private */
//		protected var _timelineStateList:Vector.<SlotTimelineState>;

	public Slot(Slot self)
	{
		super();

		if(self != this)
		{
			throw new IllegalOperationError("Abstract class can not be instantiated!");
		}

		_displayList = [];
		_currentDisplayIndex = -1;

		_originZOrder = 0;
		_tweenZOrder = 0;
		_offsetZOrder = 0;
		_isShowDisplay = false;
		_isColorChanged = false;
		_colorTransform = new ColorTransform();
		_displayDataList = null;
		//_childArmature = null;
		_currentDisplay = null;
//			_timelineStateList = new Vector.<SlotTimelineState>;

		this.inheritRotation = true;
		this.inheritScale = true;
	}

	public void initWithSlotData(SlotData slotData)
	{
		name = slotData.name;
		blendMode = slotData.blendMode;
		_originZOrder = slotData.zOrder;
		_displayDataList = slotData.displayDataList;
		_originDisplayIndex = slotData.displayIndex;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void dispose()
	{
		if(_displayList == null)
		{
			return;
		}

		super.dispose();

		_displayList.clear();
//			_timelineStateList.length = 0;

		_displayDataList = null;
		_displayList = null;
		_currentDisplay = null;
//			_timelineStateList = null;

	}

//		private function sortState(state1:SlotTimelineState, state2:SlotTimelineState):int
//		{
//			return state1._animationState.layer < state2._animationState.layer?-1:1;
//		}

	/** @private */
//		dragonBones_internal function addState(timelineState:SlotTimelineState):void
//		{
//			if(_timelineStateList.indexOf(timelineState) < 0)
//			{
//				_timelineStateList.push(timelineState);
//				_timelineStateList.sort(sortState);
//			}
//		}

	/** @private */
//		dragonBones_internal function removeState(timelineState:SlotTimelineState):void
//		{
//			var index:int = _timelineStateList.indexOf(timelineState);
//			if(index >= 0)
//			{
//				_timelineStateList.splice(index, 1);
//			}
//		}

//骨架装配
	/** @private */
	@Override dragonBones_internal void setArmature(Armature value)
	{
		if(_armature == value)
		{
			return;
		}
		if(_armature != null)
		{
			_armature.removeSlotFromSlotList(this);
		}
		_armature = value;
		if(_armature != null)
		{
			_armature.addSlotToSlotList(this);
			_armature._slotsZOrderChanged = true;
			addDisplayToContainer(this._armature.display);
		}
		else
		{
			removeDisplayFromContainer();
		}
	}

//动画
	/** @private */
	private void update()
	{
		if(this._parent._needUpdate <= 0 && !_needUpdate)
		{
			return;
		}

		updateGlobal();
		updateTransform();
		_needUpdate = false;
	}

	@Override protected void calculateRelativeParentTransform()
	{
		_global.scaleX = this._origin.scaleX * this._offset.scaleX;
		_global.scaleY = this._origin.scaleY * this._offset.scaleY;
		_global.skewX = this._origin.skewX + this._offset.skewX;
		_global.skewY = this._origin.skewY + this._offset.skewY;
		_global.x = this._origin.x + this._offset.x + this._parent._tweenPivot.x;
		_global.y = this._origin.y + this._offset.y + this._parent._tweenPivot.y;
	}

	private void updateChildArmatureAnimation()
	{
		if(childArmature)
		{
			if(_isShowDisplay)
			{
				if(
					this._armature &&
					this._armature.animation.lastAnimationState &&
					childArmature.animation.hasAnimation(this._armature.animation.lastAnimationState.name)
				)
				{
					childArmature.animation.gotoAndPlay(this._armature.animation.lastAnimationState.name);
				}
				else
				{
					childArmature.animation.play();
				}
			}
			else
			{
				childArmature.animation.stop();
				childArmature.animation._lastAnimationState = null;
			}
		}
	}

	/** @private */
	private void changeDisplay(int displayIndex)
	{
		if (displayIndex < 0)
		{
			if(_isShowDisplay)
			{
				_isShowDisplay = false;
				removeDisplayFromContainer();
				updateChildArmatureAnimation();
			}
		}
		else if (_displayList.size() > 0)
		{
			int length = _displayList.size();
			if(displayIndex >= length)
			{
				displayIndex = length - 1;
			}

			if(_currentDisplayIndex != displayIndex)
			{
				_isShowDisplay = true;
				_currentDisplayIndex = displayIndex;
				updateSlotDisplay();
				//updateTransform();//解决当时间和bone不统一时会换皮肤时会跳的bug
				updateChildArmatureAnimation();
				if(
					_displayDataList != null &&
					_displayDataList.size() > 0 &&
					_currentDisplayIndex < _displayDataList.size()
				)
				{
					this._origin.copy(_displayDataList.get(_currentDisplayIndex).transform);
				}
				_needUpdate = true;
			}
			else if(!_isShowDisplay)
			{
				_isShowDisplay = true;
				if(this._armature != null)
				{
					this._armature._slotsZOrderChanged = true;
					addDisplayToContainer(this._armature.display);
				}
				updateChildArmatureAnimation();
			}

		}
	}

	/** @private
	 * Updates the display of the slot.
	 */
	private void updateSlotDisplay()
	{
		int currentDisplayIndex = -1;
		if(_currentDisplay != null)
		{
			currentDisplayIndex = getDisplayIndex();
			removeDisplayFromContainer();
		}
		Object displayObj = _displayList[_currentDisplayIndex];
		if (displayObj != null)
		{
			if(displayObj instanceof Armature)
			{
				//_childArmature = display as Armature;
				_currentDisplay = ((Armature)displayObj).getDisplay();
			}
			else
			{
				//_childArmature = null;
				_currentDisplay = displayObj;
			}
		}
		else
		{
			_currentDisplay = null;
			//_childArmature = null;
		}
		updateDisplay(_currentDisplay);
		if(_currentDisplay != null)
		{
			if(this._armature != null && _isShowDisplay)
			{
				if(currentDisplayIndex < 0)
				{
					this._armature._slotsZOrderChanged = true;
					addDisplayToContainer(this._armature.getDisplay());
				}
				else
				{
					addDisplayToContainer(this._armature.getDisplay(), currentDisplayIndex);
				}
			}
			updateDisplayBlendMode(_blendMode);
			updateDisplayColor(	_colorTransform.alphaOffset, _colorTransform.redOffset, _colorTransform.greenOffset, _colorTransform.blueOffset,
								_colorTransform.alphaMultiplier, _colorTransform.redMultiplier, _colorTransform.greenMultiplier, _colorTransform.blueMultiplier,true);
			updateDisplayVisible(_visible);
			updateTransform();
		}
	}

	/** @private */
	@Override public void setVisible(boolean value)
	{
		if(this._visible != value)
		{
			this._visible = value;
			updateDisplayVisible(this._visible);
		}
	}

	/**
	 * The DisplayObject list belonging to this Slot instance (display or armature). Replace it to implement switch texture.
	 */
	public ArrayList<Object> getDisplayList()
	{
		return _displayList;
	}
	public void setDisplayList(ArrayList<Object> value)
	{
		if(value == null)
		{
			throw new ArgumentError();
		}

		//为什么要修改_currentDisplayIndex?
		if (_currentDisplayIndex < 0)
		{
			_currentDisplayIndex = 0;
		}
		int i = _displayList.length = value.size();
		while(i -- > 0)
		{
			_displayList[i] = value[i];
		}

		//在index不改变的情况下强制刷新 TO DO需要修改
		int displayIndexBackup = _currentDisplayIndex;
		_currentDisplayIndex = -1;
		changeDisplay(displayIndexBackup);
		updateTransform();
	}

	/**
	 * The DisplayObject belonging to this Slot instance. Instance type of this object varies from flash.display.DisplayObject to startling.display.DisplayObject and subclasses.
	 */
	public Object getDisplay()
	{
		return _currentDisplay;
	}
	public void setDisplay(Object value)
	{
		if (_currentDisplayIndex < 0)
		{
			_currentDisplayIndex = 0;
		}
		if(_displayList.get(_currentDisplayIndex) == value)
		{
			return;
		}
		_displayList.set(_currentDisplayIndex, value);
		updateSlotDisplay();
		updateChildArmatureAnimation();
		updateTransform();//是否可以延迟更新？
	}

	/**
	 * The sub-armature of this Slot instance.
	 */
	public Armature getChildArmature()
	{
		return (_displayList.get(_currentDisplayIndex) instanceof Armature) ? (Armature)_displayList.get(_currentDisplayIndex) : null;
	}
	public void setChildArmature(Armature value)
	{
		//设计的不好，要修改
		setDisplay(value);
	}

	/**
	 * zOrder. Support decimal for ensure dynamically added slot work toghther with animation controled slot.
	 * @return zOrder.
	 */
	public double  getZOrder()
	{
		return _originZOrder + _tweenZOrder + _offsetZOrder;
	}
	public void setZOrder(double value)
	{
		if(zOrder != value)
		{
			_offsetZOrder = value - _originZOrder - _tweenZOrder;
			if(this._armature != null)
			{
				this._armature._slotsZOrderChanged = true;
			}
		}
	}

	/**
	 * blendMode
	 * @return blendMode.
	 */
	public String getBlendMode()
	{
		return _blendMode;
	}
	public void setBlendMode(String value)
	{
		if(_blendMode != value)
		{
			_blendMode = value;
			updateDisplayBlendMode(_blendMode);
		}
	}

	//Abstract method

	/**
	 * @private
	 */
	private void updateDisplay(Object value)
	{
		throw new IllegalOperationError("Abstract method needs to be implemented in subclass!");
	}

	/**
	 * @private
	 */
	private int getDisplayIndex()
	{
		throw new IllegalOperationError("Abstract method needs to be implemented in subclass!");
	}

	/**
	 * @private
	 * Adds the original display object to another display object.
	 * @param container
	 * @param index
	 */
	private void addDisplayToContainer(Object container, int index = -1)
	{
		throw new IllegalOperationError("Abstract method needs to be implemented in subclass!");
	}

	/**
	 * @private
	 * remove the original display object from its parent.
	 */
	private void removeDisplayFromContainer()
	{
		throw new IllegalOperationError("Abstract method needs to be implemented in subclass!");
	}

	/**
	 * @private
	 * Updates the transform of the slot.
	 */
	private void updateTransform()
	{
		throw new IllegalOperationError("Abstract method needs to be implemented in subclass!");
	}

	/**
	 * @private
	 */
	void updateDisplayVisible(boolean value)
	{
		/**
		 * bone.visible && slot.visible && updateVisible
		 * this._parent.visible && this._visible && value;
		 */
		throw new IllegalOperationError("Abstract method needs to be implemented in subclass!");
	}

	/**
	 * @private
	 * Updates the color of the display object.
	 * @param a
	 * @param r
	 * @param g
	 * @param b
	 * @param aM
	 * @param rM
	 * @param gM
	 * @param bM
	 */
	public void updateDisplayColor(
		double aOffset,
		double rOffset,
		double gOffset,
		double bOffset,
		double aMultiplier,
		double rMultiplier,
		double gMultiplier,
		double bMultiplier,
		boolean colorChanged=false
	)
	{
		_colorTransform.alphaOffset = aOffset;
		_colorTransform.redOffset = rOffset;
		_colorTransform.greenOffset = gOffset;
		_colorTransform.blueOffset = bOffset;
		_colorTransform.alphaMultiplier = aMultiplier;
		_colorTransform.redMultiplier = rMultiplier;
		_colorTransform.greenMultiplier = gMultiplier;
		_colorTransform.blueMultiplier = bMultiplier;

		_isColorChanged = colorChanged;
	}

	/**
	 * @private
	 * Update the blend mode of the display object.
	 * @param value The blend mode to use.
	 */
	private void updateDisplayBlendMode(String value)
	{
		throw new IllegalOperationError("Abstract method needs to be implemented in subclass!");
	}

	/** @private When slot timeline enter a key frame, call this func*/
	public void arriveAtFrame(Frame frame, SlotTimelineState timelineState, AnimationState animationState, boolean isCross)
	{
		boolean displayControl = animationState.displayControl &&
									 animationState.containsBoneMask(getParent().name);

		if(displayControl)
		{
			SlotFrame slotFrame = (SlotFrame)frame;
			int displayIndex = slotFrame.displayIndex;
			Slot childSlot;
			changeDisplay(displayIndex);
			updateDisplayVisible(slotFrame.visible);
			if(displayIndex >= 0)
			{
				if(!Double.isNaN(slotFrame.zOrder) && slotFrame.zOrder != _tweenZOrder)
				{
					_tweenZOrder = slotFrame.zOrder;
					this._armature._slotsZOrderChanged = true;
				}
			}

			//[TODO]currently there is only gotoAndPlay belongs to frame action. In future, there will be more.
			//后续会扩展更多的action，目前只有gotoAndPlay的含义
			if(frame.action != null)
			{
				if (childArmature)
				{
					childArmature.animation.gotoAndPlay(frame.action);
				}
			}
		}
	}

	@Override protected Object updateGlobal()
	{
		calculateRelativeParentTransform();
		TransformUtil.transformToMatrix(_global, _globalTransformMatrix);
		Object output = calculateParentTransform();
		if(output != null)
		{
			//计算父骨头绝对坐标
			Matrix parentMatrix = output.parentGlobalTransformMatrix;
			_globalTransformMatrix.concat(parentMatrix);
		}
		TransformUtil.matrixToTransform(_globalTransformMatrix,_global,true,true);
		return output;
	}

	public void resetToOrigin()
	{
		changeDisplay(_originDisplayIndex);
		updateDisplayColor(0, 0, 0, 0, 1, 1, 1, 1, true);
	}
}
