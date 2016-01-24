package dragonBones.fast;

import dragonBones.core.DBObject;
import flash.errors.ArgumentError;
import flash.errors.IllegalOperationError;
import flash.geom.ColorTransform;
import flash.geom.Matrix;

import dragonBones.cache.SlotFrameCache;
import dragonBones.core.IArmature;
import dragonBones.core.ISlotCacheGenerator;
import dragonBones.fast.animation.FastAnimationState;
import dragonBones.objects.DisplayData;
import dragonBones.objects.Frame;
import dragonBones.objects.SlotData;
import dragonBones.objects.SlotFrame;
import dragonBones.utils.ColorTransformUtil;
import dragonBones.utils.TransformUtil;

import java.util.ArrayList;
import java.util.Objects;

//use namespace dragonBones_internal;

abstract public class FastSlot extends FastDBObject implements ISlotCacheGenerator
{
	/** @private Need to keep the reference of DisplayData. When slot switch displayObject, it need to restore the display obect's origional pivot. */
	ArrayList<DisplayData> _displayDataList;
	/** @private */
	double _originZOrder;
	/** @private */
	double _tweenZOrder;
	/** @private */
	double _originDisplayIndex;
	/** @private */
	protected double _offsetZOrder;

	protected ArrayList<Object> _displayList;
	protected int _currentDisplayIndex;
	public ColorTransform _colorTransform;
	public boolean _isColorChanged;
	protected Object _currentDisplay;

	protected String _blendMode;

	public boolean hasChildArmature;
	public FastSlot()
	{
		super();

		hasChildArmature = false;
		_currentDisplayIndex = -1;

		_originZOrder = 0;
		_tweenZOrder = 0;
		_offsetZOrder = 0;
		_colorTransform = new ColorTransform();
		_isColorChanged = false;
		_displayDataList = null;
		_currentDisplay = null;

		this.inheritRotation = true;
		this.inheritScale = true;
	}

	public void initWithSlotData(SlotData slotData)
	{
		setName(slotData.name);
		setBlendMode(slotData.blendMode);
		_originZOrder = slotData.zOrder;
		_displayDataList = slotData.getDisplayDataList();
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

		_displayDataList = null;
		_displayList = null;
		_currentDisplay = null;
	}

	//动画
	/** @private */
	@Override
	void updateByCache()
	{
		super.updateByCache();
		updateTransform();
	//颜色
		ColorTransform cacheColor = ((SlotFrameCache)this._frameCache).colorTransform;
		boolean cacheColorChanged = cacheColor != null;
		if(	this.getColorChanged() != cacheColorChanged ||
			(this.getColorChanged() && cacheColorChanged && !ColorTransformUtil.isEqual(_colorTransform, cacheColor)))
		{
			cacheColor = cacheColor != null ? cacheColor : ColorTransformUtil.originalColor;
			updateDisplayColor(
				cacheColor.alphaOffset,
				cacheColor.redOffset,
				cacheColor.greenOffset,
				cacheColor.blueOffset,
				cacheColor.alphaMultiplier,
				cacheColor.redMultiplier,
				cacheColor.greenMultiplier,
				cacheColor.blueMultiplier,
				cacheColorChanged
			);
		}

	//displayIndex
		changeDisplayIndex(((SlotFrameCache)this._frameCache).displayIndex);
	}

	/** @private */
	void update()
	{
		if(this._parent._needUpdate <= 0)
		{
			return;
		}

		updateGlobal();
		updateTransform();
	}

	@Override protected void calculateRelativeParentTransform()
	{
		_global.copy(this._origin);
		_global.x += this._parent._tweenPivot.x;
		_global.y += this._parent._tweenPivot.y;
	}

	void initDisplayList(ArrayList<Object> newDisplayList)
	{
		this._displayList = newDisplayList;
	}

	private int clearCurrentDisplay()
	{
		if(hasChildArmature)
		{
			IArmature targetArmature = (IArmature)this.getChildArmature();
			if(targetArmature != null)
			{
				targetArmature.resetAnimation();
			}
		}
		if (_isColorChanged)
		{
			updateDisplayColor(0, 0, 0, 0, 1, 1, 1, 1, true);
		}
		int slotIndex = getDisplayIndex();
		removeDisplayFromContainer();
		return slotIndex;
	}

	/** @private */
	void changeDisplayIndex(int displayIndex)
	{
		if(_currentDisplayIndex == displayIndex)
		{
			return;
		}

		int slotIndex = -1;

		if(_currentDisplayIndex >=0)
		{
			slotIndex = clearCurrentDisplay();
		}

		_currentDisplayIndex = displayIndex;

		if(_currentDisplayIndex >=0)
		{
			this._origin.copy(_displayDataList.get(_currentDisplayIndex).transform);
			this.initCurrentDisplay(slotIndex);
		}
	}

	//currentDisplayIndex不变，改变内容，必须currentDisplayIndex >=0
	private void changeSlotDisplay(Object value)
	{
		int slotIndex = clearCurrentDisplay();
		_displayList.set(_currentDisplayIndex, value);
		this.initCurrentDisplay(slotIndex);
	}

	private void initCurrentDisplay(int slotIndex)
	{
		Object display = _displayList.get(_currentDisplayIndex);
		if (display != null)
		{
			if(display instanceof FastArmature)
			{
				_currentDisplay = ((FastArmature)display).getDisplay();
			}
			else
			{
				_currentDisplay = display;
			}
		}
		else
		{
			_currentDisplay = null;
		}

		updateDisplay(_currentDisplay);
		if(_currentDisplay != null)
		{
			if(slotIndex != -1)
			{
				addDisplayToContainer(this.armature.getDisplay(), slotIndex);
			}
			else
			{
				this.armature._slotsZOrderChanged = true;
				addDisplayToContainer(this.armature.getDisplay());
			}

			if(_blendMode != null)
			{
				updateDisplayBlendMode(_blendMode);
			}
			if(_isColorChanged)
			{
				updateDisplayColor(	_colorTransform.alphaOffset,
					_colorTransform.redOffset,
					_colorTransform.greenOffset,
					_colorTransform.blueOffset,
					_colorTransform.alphaMultiplier,
					_colorTransform.redMultiplier,
					_colorTransform.greenMultiplier,
					_colorTransform.blueMultiplier,
					true);
			}
			updateTransform();

			if(display instanceof FastArmature)
			{
				FastArmature targetArmature = (FastArmature)display;

				if(	this.armature != null &&
					this.armature.getAnimation().animationState != null &&
					targetArmature.getAnimation().hasAnimation(this.armature.getAnimation().animationState.name))
				{
					targetArmature.getAnimation().gotoAndPlay(this.armature.getAnimation().animationState.name);
				}
				else
				{
					targetArmature.getAnimation().play();
				}
			}
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
		//todo: 考虑子骨架变化的各种情况
		if(value == null)
		{
			throw new ArgumentError();
		}

		Object newDisplay = value.get(_currentDisplayIndex);
		boolean displayChanged = _currentDisplayIndex >= 0 && _displayList.get(_currentDisplayIndex) != newDisplay;

		_displayList = value;

		if(displayChanged)
		{
			changeSlotDisplay(newDisplay);
		}
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
		//todo: 考虑子骨架变化的各种情况进行进一步测试
		if (_currentDisplayIndex < 0)
		{
			_currentDisplayIndex = 0;
		}
		if(_displayList.get(_currentDisplayIndex) == value)
		{
			return;
		}

		changeSlotDisplay(value);
	}

	/**
	 * The sub-armature of this Slot instance.
	 */
	public Object getChildArmature()
	{
		return _displayList.get(_currentDisplayIndex) instanceof IArmature ? (IArmature) _displayList.get(_currentDisplayIndex) : null;
	}

	public void setChildArmature(Object value)
	{
		setDisplay(value);
	}
	/**
	 * zOrder. Support decimal for ensure dynamically added slot work toghther with animation controled slot.
	 * @return zOrder.
	 */
	public double getZOrder()
	{
		return _originZOrder + _tweenZOrder + _offsetZOrder;
	}
	public void setZOrder(double value)
	{
		if(getZOrder() != value)
		{
			_offsetZOrder = value - _originZOrder - _tweenZOrder;
			if(this.armature != null)
			{
				this.armature._slotsZOrderChanged = true;
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
		if(!Objects.equals(_blendMode, value))
		{
			_blendMode = value;
			updateDisplayBlendMode(_blendMode);
		}
	}

	/**
	 * Indicates the Bone instance that directly contains this DBObject instance if any.
	 */
	public ColorTransform getColorTransform()
	{
		return _colorTransform;
	}

	public int getDisplayIndex()
	{
		return _currentDisplayIndex;
	}

	public boolean getColorChanged()
	{
		return _isColorChanged;
	}

//Abstract method
	/**
	 * @private
	 */
	void updateDisplay(Object value)
	{
		throw new IllegalOperationError("Abstract method needs to be implemented in subclass!");
	}

	void addDisplayToContainer(Object container)
	{
		addDisplayToContainer(container, -1);
	}

	/**
	 * @private
	 * Adds the original display object to another display object.
	 * @param container
	 * @param index
	 */
	void addDisplayToContainer(Object container, int index)
	{
		throw new IllegalOperationError("Abstract method needs to be implemented in subclass!");
	}

	/**
	 * @private
	 * remove the original display object from its parent.
	 */
	void removeDisplayFromContainer()
	{
		throw new IllegalOperationError("Abstract method needs to be implemented in subclass!");
	}

	/**
	 * @private
	 * Updates the transform of the slot.
	 */
	void updateTransform()
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

	void updateDisplayColor(
		double aOffset,
		double rOffset,
		double gOffset,
		double bOffset,
		double aMultiplier,
		double rMultiplier,
		double gMultiplier,
		double bMultiplier
	) {
		updateDisplayColor(aOffset, rOffset, gOffset, bOffset, aMultiplier, rMultiplier, gMultiplier, bMultiplier, false);
	}

	/**
	 * @private
	 * Updates the color of the display object.
	 * @param aOffset
	 * @param rOffset
	 * @param gOffset
	 * @param bOffset
	 * @param aMultiplier
	 * @param rMultiplier
	 * @param gMultiplier
	 * @param bMultiplier
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
		boolean colorChanged
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
	void updateDisplayBlendMode(String value)
	{
		throw new IllegalOperationError("Abstract method needs to be implemented in subclass!");
	}

	/** @private When slot timeline enter a key frame, call this func*/
	public void arriveAtFrame(Frame frame, FastAnimationState animationState)
	{
		SlotFrame slotFrame = (SlotFrame)frame;
		int displayIndex = slotFrame.displayIndex;
		changeDisplayIndex(displayIndex);
		updateDisplayVisible(slotFrame.visible);
		if(displayIndex >= 0)
		{
			if(!Double.isNaN(slotFrame.zOrder) && slotFrame.zOrder != _tweenZOrder)
			{
				_tweenZOrder = slotFrame.zOrder;
				this.armature._slotsZOrderChanged = true;
			}
		}
		//[TODO]currently there is only gotoAndPlay belongs to frame action. In future, there will be more.
		//后续会扩展更多的action，目前只有gotoAndPlay的含义
		if(frame.action != null)
		{
			IArmature targetArmature = (IArmature)getChildArmature();
			if (targetArmature != null)
			{
				targetArmature.getAnimation().gotoAndPlay(frame.action);
			}
		}
	}

			/** @private */
	void hideSlots()
	{
		changeDisplayIndex( -1);
		removeDisplayFromContainer();
		if (_frameCache != null)
		{
			this._frameCache.clear();
		}
	}

	protected Object updateGlobal()
	{
		calculateRelativeParentTransform();
		TransformUtil.transformToMatrix(_global, _globalTransformMatrix);
		DBObject.TempOutput output = calculateParentTransform();
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
		changeDisplayIndex((int)_originDisplayIndex);
		updateDisplayColor(0, 0, 0, 0, 1, 1, 1, 1, true);
	}
}
