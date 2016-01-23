package dragonBones.display;

import flash.display.BlendMode;
import flash.geom.Matrix;

import dragonBones.core.dragonBones_internal;
import dragonBones.fast.FastArmature;
import dragonBones.fast.FastSlot;

import starling.display.BlendMode;
import starling.display.DisplayObject;
import starling.display.DisplayObjectContainer;
import starling.display.Quad;

//use namespace dragonBones_internal;
	
public class StarlingFastSlot extends FastSlot
{
	private DisplayObject _starlingDisplay;

	public boolean updateMatrix;


	public StarlingFastSlot()
	{
		super(this);

		_starlingDisplay = null;

		updateMatrix = false;
	}

	@Override
	public void dispose()
	{
		for (Object content : this._displayList)
		{
			if(content instanceof FastArmature)
			{
				((FastArmature)content).dispose();
			}
			else if(content instanceof DisplayObject)
			{
				((DisplayObject)content).dispose();
			}
		}
		super.dispose();

		_starlingDisplay = null;
	}

	/** @private */
	@Override private void  updateDisplay(Object value)
	{
		_starlingDisplay = (DisplayObject)value;
	}


	//Abstract method

	/** @private */
	@Override private int getDisplayIndex()
	{
		if(_starlingDisplay && _starlingDisplay.parent)
		{
			return _starlingDisplay.parent.getChildIndex(_starlingDisplay);
		}
		return -1;
	}

	/** @private */
	@Override private void addDisplayToContainer(Object container, int index = -1)
	{
		DisplayObjectContainer starlingContainer = (DisplayObjectContainer)container;
		if(_starlingDisplay && starlingContainer)
		{
			if (index < 0)
			{
				starlingContainer.addChild(_starlingDisplay);
			}
			else
			{
				starlingContainer.addChildAt(_starlingDisplay, Math.min(index, starlingContainer.numChildren));
			}
		}
	}

	/** @private */
	@Override private void removeDisplayFromContainer()
	{
		if(_starlingDisplay && _starlingDisplay.parent)
		{
			_starlingDisplay.parent.removeChild(_starlingDisplay);
		}
	}

	/** @private */
	@Override private void updateTransform()
	{
		if(_starlingDisplay)
		{
			double pivotX = _starlingDisplay.pivotX;
			double pivotY = _starlingDisplay.pivotY;


			if(updateMatrix)
			{
				//_starlingDisplay.transformationMatrix setter 比较慢暂时走下面
				_starlingDisplay.transformationMatrix = _globalTransformMatrix;
				if(pivotX || pivotY)
				{
					_starlingDisplay.pivotX = pivotX;
					_starlingDisplay.pivotY = pivotY;
				}
			}
			else
			{
				Matrix displayMatrix = _starlingDisplay.transformationMatrix;
				displayMatrix.a = _globalTransformMatrix.a;
				displayMatrix.b = _globalTransformMatrix.b;
				displayMatrix.c = _globalTransformMatrix.c;
				displayMatrix.d = _globalTransformMatrix.d;
				//displayMatrix.copyFrom(_globalTransformMatrix);
				if(pivotX || pivotY)
				{
					displayMatrix.tx = _globalTransformMatrix.tx - (displayMatrix.a * pivotX + displayMatrix.c * pivotY);
					displayMatrix.ty = _globalTransformMatrix.ty - (displayMatrix.b * pivotX + displayMatrix.d * pivotY);
				}
				else
				{
					displayMatrix.tx = _globalTransformMatrix.tx;
					displayMatrix.ty = _globalTransformMatrix.ty;
				}
			}
		}
	}

	/** @private */
	@Override private void updateDisplayVisible(boolean value)
	{
//			if(_starlingDisplay && this._parent)
//			{
//				_starlingDisplay.visible = this._parent.visible && this._visible && value;
//			}
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
		if(_starlingDisplay)
		{
			super.updateDisplayColor(aOffset, rOffset, gOffset, bOffset, aMultiplier, rMultiplier, gMultiplier, bMultiplier,colorChanged);
			_starlingDisplay.alpha = aMultiplier;
			if (_starlingDisplay instanceof Quad)
			{
				((Quad)_starlingDisplay).color = (uint(rMultiplier * 0xff) << 16) + (uint(gMultiplier * 0xff) << 8) + uint(bMultiplier * 0xff);
			}
		}
	}

	/** @private */
	@Override private void updateDisplayBlendMode(String value)
	{
		if(_starlingDisplay)
		{
			switch(blendMode)
			{
				case starling.display.BlendMode.NONE:
				case starling.display.BlendMode.AUTO:
				case starling.display.BlendMode.ADD:
				case starling.display.BlendMode.ERASE:
				case starling.display.BlendMode.MULTIPLY:
				case starling.display.BlendMode.NORMAL:
				case starling.display.BlendMode.SCREEN:
					_starlingDisplay.blendMode = blendMode;
					break;

				case flash.display.BlendMode.ADD:
					_starlingDisplay.blendMode = starling.display.BlendMode.ADD;
					break;

				case flash.display.BlendMode.ERASE:
					_starlingDisplay.blendMode = starling.display.BlendMode.ERASE;
					break;

				case flash.display.BlendMode.MULTIPLY:
					_starlingDisplay.blendMode = starling.display.BlendMode.MULTIPLY;
					break;

				case flash.display.BlendMode.NORMAL:
					_starlingDisplay.blendMode = starling.display.BlendMode.NORMAL;
					break;

				case flash.display.BlendMode.SCREEN:
					_starlingDisplay.blendMode = starling.display.BlendMode.SCREEN;
					break;

				case flash.display.BlendMode.ALPHA:
				case flash.display.BlendMode.DARKEN:
				case flash.display.BlendMode.DIFFERENCE:
				case flash.display.BlendMode.HARDLIGHT:
				case flash.display.BlendMode.INVERT:
				case flash.display.BlendMode.LAYER:
				case flash.display.BlendMode.LIGHTEN:
				case flash.display.BlendMode.OVERLAY:
				case flash.display.BlendMode.SHADER:
				case flash.display.BlendMode.SUBTRACT:
					break;

				default:
					break;
			}
		}
	}
}
