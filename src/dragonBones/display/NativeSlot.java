package dragonBones.display;

import flash.display.BlendMode;
import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.geom.ColorTransform;

import dragonBones.Slot;
import dragonBones.core.dragonBones_internal;

//use namespace dragonBones_internal;

public class NativeSlot extends Slot
{
	private DisplayObject _nativeDisplay;

	public NativeSlot()
	{
		super(this);
		_nativeDisplay = null;
	}

	@Override
	public void dispose()
	{
		super.dispose();

		_nativeDisplay = null;
	}


	//Abstract method

	/** @private */
	@Override private void updateDisplay(Object value)
	{
		_nativeDisplay = (DisplayObject)value;
	}

	/** @private */
	@Override private int getDisplayIndex()
	{
		if(_nativeDisplay && _nativeDisplay.parent)
		{
			return _nativeDisplay.parent.getChildIndex(_nativeDisplay);
		}
		return -1;
	}

	/** @private */
	@Override private void addDisplayToContainer(Object container, int index = -1)
	{
		DisplayObjectContainer nativeContainer = (DisplayObjectContainer)container;
		if(_nativeDisplay && nativeContainer)
		{
			if (index < 0)
			{
				nativeContainer.addChild(_nativeDisplay);
			}
			else
			{
				nativeContainer.addChildAt(_nativeDisplay, Math.min(index, nativeContainer.numChildren));
			}
		}
	}

	/** @private */
	@Override private void removeDisplayFromContainer()
	{
		if(_nativeDisplay && _nativeDisplay.parent)
		{
			_nativeDisplay.parent.removeChild(_nativeDisplay);
		}
	}

	/** @private */
	@Override private void updateTransform()
	{
		if(_nativeDisplay)
		{
			_nativeDisplay.transform.matrix = this._globalTransformMatrix;
		}
	}

	/** @private */
	@Override private void updateDisplayVisible(boolean value)
	{
		if(_nativeDisplay)
		{
			_nativeDisplay.visible = this._parent.visible && this._visible && value;
		}
	}

	/** @private */
	@Override private void updateDisplayColor(
		double aOffset,
		double rOffset,
		double gOffset,
		double bOffset,
		double aMultiplier,
		double rMultiplier,
		double gMultiplier,
		double bMultiplier,
		boolean colorChanged = false
	)
	{
		if(_nativeDisplay)
		{
			super.updateDisplayColor(aOffset, rOffset, gOffset, bOffset, aMultiplier, rMultiplier, gMultiplier, bMultiplier,colorChanged);


			_nativeDisplay.transform.colorTransform = _colorTransform;
		}
	}

	/** @private */
	@Override private void updateDisplayBlendMode(String value)
	{
		if(_nativeDisplay)
		{
			switch(blendMode)
			{
				case BlendMode.ADD:
				case BlendMode.ALPHA:
				case BlendMode.DARKEN:
				case BlendMode.DIFFERENCE:
				case BlendMode.ERASE:
				case BlendMode.HARDLIGHT:
				case BlendMode.INVERT:
				case BlendMode.LAYER:
				case BlendMode.LIGHTEN:
				case BlendMode.MULTIPLY:
				case BlendMode.NORMAL:
				case BlendMode.OVERLAY:
				case BlendMode.SCREEN:
				case BlendMode.SHADER:
				case BlendMode.SUBTRACT:
					_nativeDisplay.blendMode = blendMode;
					break;

				default:
					//_nativeDisplay.blendMode = BlendMode.NORMAL;
					break;
			}
		}
	}
}
