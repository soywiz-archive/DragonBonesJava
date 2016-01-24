package dragonBones.display;

import flash.display.BlendMode;
import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.geom.ColorTransform;

import dragonBones.Slot;

//use namespace dragonBones_internal;

public class NativeSlot extends Slot
{
	private DisplayObject _nativeDisplay;

	public NativeSlot()
	{
		super();
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
	private void updateDisplay(Object value)
	{
		_nativeDisplay = (DisplayObject)value;
	}

	/** @private */
	private int getDisplayIndex()
	{
		if(_nativeDisplay != null && _nativeDisplay.getParent() != null)
		{
			return _nativeDisplay.getParent().getChildIndex(_nativeDisplay);
		}
		return -1;
	}

	private void addDisplayToContainer(Object container)
	{
		addDisplayToContainer(container, -1);
	}

		/** @private */
	private void addDisplayToContainer(Object container, int index)
	{
		DisplayObjectContainer nativeContainer = (DisplayObjectContainer)container;
		if(_nativeDisplay != null && nativeContainer != null)
		{
			if (index < 0)
			{
				nativeContainer.addChild(_nativeDisplay);
			}
			else
			{
				nativeContainer.addChildAt(_nativeDisplay, Math.min(index, nativeContainer.getNumChildren()));
			}
		}
	}

	/** @private */
	private void removeDisplayFromContainer()
	{
		if(_nativeDisplay != null && _nativeDisplay.getParent() != null)
		{
			_nativeDisplay.getParent().removeChild(_nativeDisplay);
		}
	}

	/** @private */
	private void updateTransform()
	{
		if(_nativeDisplay != null)
		{
			_nativeDisplay.getTransform().setMatrix(this._globalTransformMatrix);
		}
	}

	/** @private */
	private void updateDisplayVisible(boolean value)
	{
		if(_nativeDisplay != null)
		{
			_nativeDisplay.setVisible(this._parent.getVisible() && this._visible && value);
		}
	}

	/** @private */
	@Override
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
		if(_nativeDisplay != null)
		{
			super.updateDisplayColor(aOffset, rOffset, gOffset, bOffset, aMultiplier, rMultiplier, gMultiplier, bMultiplier,colorChanged);


			_nativeDisplay.getTransform().setColorTransform(_colorTransform);
		}
	}

	/** @private */
	private void updateDisplayBlendMode(String value)
	{
		if(_nativeDisplay != null)
		{
			switch(getBlendMode())
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
					_nativeDisplay.setBlendMode(getBlendMode());
					break;

				default:
					//_nativeDisplay.blendMode = BlendMode.NORMAL;
					break;
			}
		}
	}
}
