package dragonBones.display;

import starling.display.BlendMode;
import flash.geom.Matrix;

import dragonBones.fast.FastArmature;
import dragonBones.fast.FastSlot;

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
	private void  updateDisplay(Object value)
	{
		_starlingDisplay = (DisplayObject)value;
	}


	//Abstract method

	/** @private */
	public int getDisplayIndex()
	{
		if(_starlingDisplay != null && _starlingDisplay.getParent() != null)
		{
			return _starlingDisplay.getParent().getChildIndex(_starlingDisplay);
		}
		return -1;
	}

	public void addDisplayToContainer(Object container)
	{
		addDisplayToContainer(container, -1);
	}

		/** @private */
	public void addDisplayToContainer(Object container, int index)
	{
		DisplayObjectContainer starlingContainer = (DisplayObjectContainer)container;
		if(_starlingDisplay != null && starlingContainer != null)
		{
			if (index < 0)
			{
				starlingContainer.addChild(_starlingDisplay);
			}
			else
			{
				starlingContainer.addChildAt(_starlingDisplay, Math.min(index, starlingContainer.getNumChildren()));
			}
		}
	}

	/** @private */
	public void removeDisplayFromContainer()
	{
		if(_starlingDisplay != null && _starlingDisplay.getParent() != null)
		{
			_starlingDisplay.getParent().removeChild(_starlingDisplay);
		}
	}

	/** @private */
	private void updateTransform()
	{
		if(_starlingDisplay != null)
		{
			double pivotX = _starlingDisplay.getPivotX();
			double pivotY = _starlingDisplay.getPivotY();


			if(updateMatrix)
			{
				//_starlingDisplay.transformationMatrix setter 比较慢暂时走下面
				_starlingDisplay.setTransformationMatrix(_globalTransformMatrix);
				if(pivotX != 0 || pivotY != 0)
				{
					_starlingDisplay.setPivotX(pivotX);
					_starlingDisplay.setPivotY(pivotY);
				}
			}
			else
			{
				Matrix displayMatrix = _starlingDisplay.getTransformationMatrix();
				displayMatrix.a = _globalTransformMatrix.a;
				displayMatrix.b = _globalTransformMatrix.b;
				displayMatrix.c = _globalTransformMatrix.c;
				displayMatrix.d = _globalTransformMatrix.d;
				//displayMatrix.copyFrom(_globalTransformMatrix);
				if(pivotX != 0 || pivotY != 0)
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
	private void updateDisplayVisible(boolean value)
	{
//			if(_starlingDisplay && this._parent)
//			{
//				_starlingDisplay.visible = this._parent.visible && this._visible && value;
//			}
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
		if(_starlingDisplay != null)
		{
			super.updateDisplayColor(aOffset, rOffset, gOffset, bOffset, aMultiplier, rMultiplier, gMultiplier, bMultiplier,colorChanged);
			_starlingDisplay.setAlpha(aMultiplier);
			if (_starlingDisplay instanceof Quad)
			{
				((Quad)_starlingDisplay).setColor(((int)(rMultiplier * 0xff) << 16) | ((int)(gMultiplier * 0xff) << 8) | (int)(bMultiplier * 0xff));
			}
		}
	}

	/** @private */
	private void updateDisplayBlendMode(String value)
	{
		if(_starlingDisplay != null)
		{
			switch(getBlendMode())
			{
				case BlendMode.NONE:
				case BlendMode.AUTO:
				case BlendMode.ADD:
				case BlendMode.ERASE:
				case BlendMode.MULTIPLY:
				case BlendMode.NORMAL:
				case BlendMode.SCREEN:
					_starlingDisplay.setBlendMode(getBlendMode());
					break;

				case BlendMode.ALPHA:
				case BlendMode.DARKEN:
				case BlendMode.DIFFERENCE:
				case BlendMode.HARDLIGHT:
				case BlendMode.INVERT:
				case BlendMode.LAYER:
				case BlendMode.LIGHTEN:
				case BlendMode.OVERLAY:
				case BlendMode.SHADER:
				case BlendMode.SUBTRACT:
					break;

				default:
					break;
			}
		}
	}
}
