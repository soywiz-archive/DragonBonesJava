package dragonBones.factories;

import dragonBones.Armature;
import dragonBones.core.IName;
import dragonBones.core.ISetName;
import flash.Runnable1;
import flash.errors.ArgumentError;
import flash.errors.IllegalOperationError;
import flash.events.Event;
import flash.events.EventDispatcher;
import flash.geom.Matrix;
import flash.utils.ByteArray;
import flash.utils.Dictionary;

import dragonBones.Bone;
import dragonBones.Slot;
import dragonBones.core.dragonBones_internal;
import dragonBones.fast.FastArmature;
import dragonBones.fast.FastBone;
import dragonBones.fast.FastSlot;
import dragonBones.objects.ArmatureData;
import dragonBones.objects.BoneData;
import dragonBones.objects.DataParser;
import dragonBones.objects.DataSerializer;
import dragonBones.objects.DecompressedData;
import dragonBones.objects.DisplayData;
import dragonBones.objects.DragonBonesData;
import dragonBones.objects.SkinData;
import dragonBones.objects.SlotData;
import dragonBones.textures.ITextureAtlas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

//use namespace dragonBones_internal;

abstract public class BaseFactory  extends EventDispatcher
{
	protected static final Matrix _helpMatrix = new Matrix();

	/** @private */
	protected Map<String, DragonBonesData> dragonBonesDataDic = new HashMap<>();

	/** @private */
	protected Map<String, ArrayList<ITextureAtlas>> textureAtlasDic = new HashMap<>();
	public BaseFactory()
	{
		super();
	}

	public void dispose()
	{
		dispose(true);
	}

	/**
	 * Cleans up resources used by this BaseFactory instance.
	 * @param disposeData (optional) Destroy all internal references.
	 */
	public void dispose(boolean disposeData)
	{
		if(disposeData)
		{
			for(String skeletonName : dragonBonesDataDic.keySet())
			{
				((DragonBonesData)dragonBonesDataDic.get(skeletonName)).dispose();
				dragonBonesDataDic.remove(skeletonName);
			}

			for (String textureAtlasName : textureAtlasDic.keySet())
			{
				ArrayList<ITextureAtlas> textureAtlasArr = textureAtlasDic.get(textureAtlasName);
				if (textureAtlasArr != null)
				{
					for (int i = 0, len = textureAtlasArr.size(); i < len; i++ )
					{
						textureAtlasArr.get(i).dispose();
					}
				}
				textureAtlasDic.remove(textureAtlasName);
			}
		}

		dragonBonesDataDic = null;
		textureAtlasDic = null;
		//_currentDataName = null;
		//_currentTextureAtlasName = null;
	}

	/**
	 * Returns a SkeletonData instance.
	 * @param name The name of an existing SkeletonData instance.
	 * @return A SkeletonData instance with given name (if exist).
	 */
	public DragonBonesData getSkeletonData(String name)
	{
		return dragonBonesDataDic.get(name);
	}

	public void addSkeletonData(DragonBonesData data)
	{
		addSkeletonData(data, null);
	}

	/**
	 * Add a SkeletonData instance to this BaseFactory instance.
	 * @param data A SkeletonData instance.
	 * @param name (optional) A name for this SkeletonData instance.
	 */
	public void addSkeletonData(DragonBonesData data, String name)
	{
		if(data == null)
		{
			throw new ArgumentError();
		}
		name = name != null ? name : data.name;
		if(name == null)
		{
			throw new ArgumentError("Unnamed data!");
		}
		if(dragonBonesDataDic.containsKey(name))
		{
			throw new ArgumentError();
		}
		dragonBonesDataDic.put(name, data);
	}

	/**
	 * Remove a SkeletonData instance from this BaseFactory instance.
	 * @param name The name for the SkeletonData instance to remove.
	 */
	public void removeSkeletonData(String name)
	{
		dragonBonesDataDic.remove(name);
	}

	/**
	 * Return the TextureAtlas by name.
	 * @param name The name of the TextureAtlas to return.
	 * @return A textureAtlas.
	 */
	public Object getTextureAtlas(String name)
	{
		return textureAtlasDic.get(name);
	}

	public void addTextureAtlas(ITextureAtlas textureAtlas)
	{
		addTextureAtlas(textureAtlas, null);
	}

	/**
	 * Add a textureAtlas to this BaseFactory instance.
	 * @param textureAtlas A textureAtlas to add to this BaseFactory instance.
	 * @param name (optional) A name for this TextureAtlas.
	 */
	public void addTextureAtlas(ITextureAtlas textureAtlas, String name)
	{
		if(textureAtlas == null)
		{
			throw new ArgumentError();
		}
		if(name == null && textureAtlas instanceof ITextureAtlas)
		{
			name = ((ITextureAtlas) textureAtlas).getName();
		}
		if(name == null)
		{
			throw new ArgumentError("Unnamed data!");
		}
		ArrayList<ITextureAtlas> textureAtlasArr = textureAtlasDic.get(name);
		if (textureAtlasArr == null)
		{
			textureAtlasArr = new ArrayList<>();
			textureAtlasDic.put(name, textureAtlasArr);
		}
		if(textureAtlasArr.indexOf(textureAtlas) != -1)
		{
			throw new ArgumentError();
		}
		textureAtlasArr.add(textureAtlas);
	}

	/**
	 * Remove a textureAtlas from this baseFactory instance.
	 * @param name The name of the TextureAtlas to remove.
	 */
	public void removeTextureAtlas(String name)
	{
		textureAtlasDic.remove(name);
	}

	public Object getTextureDisplay(String textureName)
	{
		return getTextureDisplay(textureName, null, Double.NaN, Double.NaN);
	}

	public Object getTextureDisplay(String textureName, String textureAtlasName)
	{
		return getTextureDisplay(textureName, textureAtlasName);
	}

	/**
	 * Return the TextureDisplay.
	 * @param textureName The name of this Texture.
	 * @param textureAtlasName The name of the TextureAtlas.
	 * @param pivotX The registration pivotX position.
	 * @param pivotY The registration pivotY position.
	 * @return An Object.
	 */
	public Object getTextureDisplay(String textureName, String textureAtlasName, double pivotX, double pivotY)
	{
		ITextureAtlas targetTextureAtlas = null;
		ArrayList<ITextureAtlas> textureAtlasArr;
		int i;
		int len;

		if(textureAtlasName != null)
		{
			textureAtlasArr = textureAtlasDic.get(textureAtlasName);
			if (textureAtlasArr != null)
			{
				for (i = 0, len = textureAtlasArr.size(); i < len; i++)
				{
					targetTextureAtlas = textureAtlasArr.get(i);
					if (targetTextureAtlas.getRegion(textureName) != null)
					{
						break;
					}
					targetTextureAtlas = null;
				}
			}
		}
		else
		{
			for (String textureAtlasName2 : textureAtlasDic.keySet())
			{
				textureAtlasArr = textureAtlasDic.get(textureAtlasName2);
				if (textureAtlasArr != null)
				{
					for (i = 0, len = textureAtlasArr.size(); i < len; i++)
					{
						targetTextureAtlas = textureAtlasArr.get(i);
						if (targetTextureAtlas.getRegion(textureName) != null)
						{
							break;
						}
						targetTextureAtlas = null;
					}
					if (targetTextureAtlas != null)
					{
						break;
					}
				}
			}
		}

		if(targetTextureAtlas == null)
		{
			return null;
		}

		if(Double.isNaN(pivotX) || Double.isNaN(pivotY))
		{
			//默认dragonBonesData的名字和和纹理集的名字是一致的
			DragonBonesData data = dragonBonesDataDic.get(textureAtlasName);
			data = data != null ? data : findFirstDragonBonesData();
			if(data != null)
			{
				DisplayData displayData = data.getDisplayDataByName(textureName);
				if(displayData != null)
				{
					pivotX = displayData.pivot.x;
					pivotY = displayData.pivot.y;
				}
			}
		}

		return generateDisplay(targetTextureAtlas, textureName, pivotX, pivotY);
	}

	//一般情况下dragonBonesData和textureAtlas是一对一的，通过相同的key对应。
	public Armature buildArmature(String armatureName, String fromDragonBonesDataName= null, String fromTextureAtlasName= null, String skinName= null)
	{
		BuildArmatureDataPackage buildArmatureDataPackage = new BuildArmatureDataPackage();
		fillBuildArmatureDataPackageArmatureInfo(armatureName, fromDragonBonesDataName, buildArmatureDataPackage);
		if (fromTextureAtlasName == null)
		{
			fromTextureAtlasName = buildArmatureDataPackage.dragonBonesDataName;
		}

		DragonBonesData dragonBonesData = buildArmatureDataPackage.dragonBonesData;
		ArmatureData armatureData = buildArmatureDataPackage.armatureData;

		if(!armatureData)
		{
			return null;
		}

		return buildArmatureUsingArmatureDataFromTextureAtlas(dragonBonesData, armatureData, fromTextureAtlasName, skinName);
	}

	public FastArmature buildFastArmature(String armatureName, String fromDragonBonesDataName = null, String fromTextureAtlasName = null, String skinName = null)
	{
		BuildArmatureDataPackage buildArmatureDataPackage = new BuildArmatureDataPackage();
		fillBuildArmatureDataPackageArmatureInfo(armatureName, fromDragonBonesDataName, buildArmatureDataPackage);
		if (fromTextureAtlasName == null)
		{
			fromTextureAtlasName = buildArmatureDataPackage.dragonBonesDataName;
		}
		DragonBonesData dragonBonesData = buildArmatureDataPackage.dragonBonesData;
		ArmatureData armatureData = buildArmatureDataPackage.armatureData;

		if(armatureData == null)
		{
			return null;
		}

		return buildFastArmatureUsingArmatureDataFromTextureAtlas(dragonBonesData, armatureData, fromTextureAtlasName, skinName);
	}

	protected Armature buildArmatureUsingArmatureDataFromTextureAtlas(DragonBonesData dragonBonesData, ArmatureData armatureData, String textureAtlasName, String skinName = null)
	{
		Armature outputArmature = generateArmature();
		outputArmature.name = armatureData.name;
		outputArmature.__dragonBonesData = dragonBonesData;
		outputArmature._armatureData = armatureData;
		outputArmature.animation.animationDataList = armatureData.animationDataList;

		buildBones(outputArmature);

		buildSlots(outputArmature, skinName, textureAtlasName);

		outputArmature.advanceTime(0);
		return outputArmature;
	}

	protected FastArmature buildFastArmatureUsingArmatureDataFromTextureAtlas(DragonBonesData dragonBonesData, ArmatureData armatureData, String textureAtlasName, String skinName = null)
	{
		FastArmature outputArmature = generateFastArmature();
		outputArmature.name = armatureData.name;
		outputArmature.__dragonBonesData = dragonBonesData;
		outputArmature._armatureData = armatureData;
		outputArmature.animation.animationDataList = armatureData.animationDataList;

		buildFastBones(outputArmature);
		buildFastSlots(outputArmature, skinName, textureAtlasName);

		outputArmature.advanceTime(0);

		return outputArmature;
	}

	//暂时不支持ifRemoveOriginalAnimationList为false的情况
	public boolean copyAnimationsToArmature(Armature toArmature, String fromArmatreName, String fromDragonBonesDataName = null, boolean ifRemoveOriginalAnimationList = true)
	{
		BuildArmatureDataPackage buildArmatureDataPackage = new BuildArmatureDataPackage();
		if(!fillBuildArmatureDataPackageArmatureInfo(fromArmatreName, fromDragonBonesDataName, buildArmatureDataPackage))
		{
			return false;
		}

		ArmatureData fromArmatureData = buildArmatureDataPackage.armatureData;
		toArmature.animation.animationDataList = fromArmatureData.animationDataList;

	//处理子骨架的复制
		SkinData fromSkinData = fromArmatureData.getSkinData("");
		SlotData fromSlotData;
		DisplayData fromDisplayData;

		ArrayList<Slot> toSlotList = toArmature.getSlots(false);
		Slot toSlot;
		ArrayList<Object> toSlotDisplayList;
		int toSlotDisplayListLength;
		Object toDisplayObject;
		Armature toChildArmature;

		for (Object toSlot : toSlotList)
		{
			toSlotDisplayList = toSlot.displayList;
			toSlotDisplayListLength = toSlotDisplayList.size();
			for(int i = 0; i < toSlotDisplayListLength; i++)
			{
				toDisplayObject = toSlotDisplayList.get(i);

				if(toDisplayObject instanceof Armature)
				{
					toChildArmature = (Armature)toDisplayObject;

					fromSlotData = fromSkinData.getSlotData(toSlot.name);
					fromDisplayData = fromSlotData.displayDataList[i];
					if(Objects.equals(fromDisplayData.type, DisplayData.ARMATURE))
					{
						copyAnimationsToArmature(toChildArmature, fromDisplayData.name, buildArmatureDataPackage.dragonBonesDataName, ifRemoveOriginalAnimationList);
					}
				}
			}
		}

		return true;
	}

	private boolean fillBuildArmatureDataPackageArmatureInfo(String armatureName, String dragonBonesDataName, BuildArmatureDataPackage outputBuildArmatureDataPackage)
	{
		if(dragonBonesDataName != null)
		{
			outputBuildArmatureDataPackage.dragonBonesDataName = dragonBonesDataName;
			outputBuildArmatureDataPackage.dragonBonesData = dragonBonesDataDic.get(dragonBonesDataName);
			outputBuildArmatureDataPackage.armatureData = outputBuildArmatureDataPackage.dragonBonesData.getArmatureDataByName(armatureName);
			return true;
		}
		else
		{
			for(Object dragonBonesDataName : dragonBonesDataDic)
			{
				outputBuildArmatureDataPackage.dragonBonesData = dragonBonesDataDic.get(dragonBonesDataName);
				outputBuildArmatureDataPackage.armatureData = outputBuildArmatureDataPackage.dragonBonesData.getArmatureDataByName(armatureName);
				if(outputBuildArmatureDataPackage.armatureData != null)
				{
					outputBuildArmatureDataPackage.dragonBonesDataName = dragonBonesDataName;
					return true;
				}
			}
		}
		return false;
	}

	private void fillBuildArmatureDataPackageTextureInfo(String fromTextureAtlasName, BuildArmatureDataPackage outputBuildArmatureDataPackage)
	{
		outputBuildArmatureDataPackage.textureAtlasName = fromTextureAtlasName;
	}

	protected DragonBonesData findFirstDragonBonesData()
	{
		for (DragonBonesData outputDragonBonesData : dragonBonesDataDic.values())
		{
			if(outputDragonBonesData != null)
			{
				return outputDragonBonesData;
			}
		}
		return null;
	}

	protected Object findFirstTextureAtlas()
	{
		for (Object outputTextureAtlas : textureAtlasDic.values())
		{
			if(outputTextureAtlas != null)
			{
				return outputTextureAtlas;
			}
		}
		return null;
	}

	protected void buildBones(Armature armature)
	{
		//按照从属关系的顺序建立
		ArrayList<BoneData> boneDataList = armature.getArmatureData().boneDataList;

		BoneData boneData;
		Bone bone;
		String parent;
		for(int i = 0;i < boneDataList.size();i ++)
		{
			boneData = boneDataList.get(i);
			bone = Bone.initWithBoneData(boneData);
			parent = boneData.parent;
			if(	parent != null && armature.getArmatureData().getBoneData(parent) == null)
			{
				parent = null;
			}
			armature.addBone(bone, parent, true);
		}
		armature.updateAnimationAfterBoneListChanged();
	}

	protected void buildFastBones(FastArmature armature)
	{
		//按照从属关系的顺序建立
		ArrayList<BoneData> boneDataList = armature.armatureData.boneDataList;

		BoneData boneData;
		FastBone bone;
		for(int i = 0;i < boneDataList.size();i ++)
		{
			boneData = boneDataList.get(i);
			bone = FastBone.initWithBoneData(boneData);
			armature.addBone(bone, boneData.parent);
		}
	}

	protected void buildFastSlots(FastArmature armature, String skinName, String textureAtlasName)
	{
	//根据皮肤初始化SlotData的DisplayDataList
		SkinData skinData = armature.getArmatureData().getSkinData(skinName);
		if(skinData == null)
		{
			return;
		}
		armature.getArmatureData().setSkinData(skinName);

		ArrayList<Object> displayList = new ArrayList<>();
		ArrayList<SlotData> slotDataList = armature.getArmatureData().getSlotDataList();
		SlotData slotData;
		FastSlot slot;
		for(int i = 0; i < slotDataList.size(); i++)
		{
			displayList.clear();
			slotData = slotDataList.get(i);
			slot = generateFastSlot();
			slot.initWithSlotData(slotData);

			int l = slotData.getDisplayDataList().size();
			while(l-- > 0)
			{
				DisplayData displayData = slotData.getDisplayDataList().get(l);

				switch(displayData.type)
				{
					case DisplayData.ARMATURE:
						FastArmature childArmature = buildFastArmatureUsingArmatureDataFromTextureAtlas(armature.__dragonBonesData, armature.__dragonBonesData.getArmatureDataByName(displayData.name), textureAtlasName, skinName);
						displayList.set(l, childArmature);
						slot.hasChildArmature = true;
						break;

					case DisplayData.IMAGE:
					default:
						displayList[l] = getTextureDisplay(displayData.name, textureAtlasName, displayData.pivot.x, displayData.pivot.y);
						break;

				}
			}
			//==================================================
			//如果显示对象有name属性并且name属性可以设置的话，将name设置为与slot同名，dragonBones并不依赖这些属性，只是方便开发者
			for (Object displayObject : displayList)
			{
				if(displayObject == null)
				{
					continue;
				}
				if(displayObject instanceof FastArmature)
				{
					displayObject = ((FastArmature)displayObject).getDisplay();
				}

				if(displayObject instanceof ISetName)
				{
					((ISetName)displayObject).setName(slot.getName());
				}
			}
			//==================================================
			slot.initDisplayList(displayList.clone());
			armature.addSlot(slot, slotData.parent);
			slot.changeDisplayIndex(slotData.displayIndex);
		}
	}

	protected void buildSlots(Armature armature, String skinName, String textureAtlasName)
	{
		SkinData skinData = armature.getArmatureData().getSkinData(skinName);
		if(skinData == null)
		{
			return;
		}
		armature.getArmatureData().setSkinData(skinName);
		ArrayList<Object> displayList = new ArrayList<>();
		ArrayList<SlotData> slotDataList = armature.getArmatureData().getSlotDataList();
		SlotData slotData;
		Slot slot;
		Bone bone;
		Object skinListObject = { };
		for(int i = 0; i < slotDataList.size(); i++)
		{
			displayList.clear();
			slotData = slotDataList.get(i);
			bone = armature.getBone(slotData.parent);
			if(bone == null)
			{
				continue;
			}

			slot = generateSlot();
			slot.initWithSlotData(slotData);
			bone.addSlot(slot);

			int l = slotData.getDisplayDataList().size();
			while(l-- > 0)
			{
				DisplayData displayData = slotData.getDisplayDataList().get(l);

				switch(displayData.type)
				{
					case DisplayData.ARMATURE:
						Armature childArmature = buildArmatureUsingArmatureDataFromTextureAtlas(armature.__dragonBonesData, armature.__dragonBonesData.getArmatureDataByName(displayData.name), textureAtlasName, skinName);
						displayList.set(l, childArmature);
						break;

					case DisplayData.IMAGE:
					default:
						displayList.set(l, getTextureDisplay(displayData.name, textureAtlasName, displayData.pivot.x, displayData.pivot.y));
						break;

				}
			}
			//==================================================
			//如果显示对象有name属性并且name属性可以设置的话，将name设置为与slot同名，dragonBones并不依赖这些属性，只是方便开发者
			for (Object displayObject : displayList)
			{
				if(displayObject == null)
				{
					continue;
				}
				if(displayObject instanceof Armature)
				{
					displayObject = ((Armature)displayObject).getDisplay();
				}

				if(displayObject instanceof ISetName)
				{
					((ISetName) displayObject).setName(slot.name);
				}
			}
			//==================================================
			skinListObject[slotData.name] = displayList.clone();
			slot.displayList = displayList;
			slot.changeDisplay(slotData.displayIndex);
		}
		armature.addSkinList(skinName, skinListObject);
	}


	public void addSkinToArmature(Armature armature, String skinName, String textureAtlasName)
	{
		SkinData skinData = armature.getArmatureData().getSkinData(skinName);
		if(skinData == null || textureAtlasName == null)
		{
			return;
		}
		ArrayList<Object> displayList = new ArrayList<>();
		ArrayList<SlotData> slotDataList = armature.getArmatureData().getSlotDataList();
		SlotData slotData;
		Slot slot;
		Bone bone;
		Object skinListData = { };
		ArrayList<DisplayData> displayDataList;

		for(int i = 0; i < slotDataList.size(); i++)
		{
			displayList.clear();
			slotData = slotDataList.get(i);
			bone = armature.getBone(slotData.parent);
			if(bone == null)
			{
				continue;
			}

			int l = 0;
			if (i >= skinData.getSlotDataList().size())
			{
				l = 0;
			}
			else
			{
				displayDataList = skinData.getSlotDataList().get(i).getDisplayDataList();
				l = displayDataList.size();
			}
			while(l-- > 0)
			{
				DisplayData displayData = displayDataList.get(l);

				switch(displayData.type)
				{
					case DisplayData.ARMATURE:
						Armature childArmature = buildArmatureUsingArmatureDataFromTextureAtlas(armature.__dragonBonesData, armature.__dragonBonesData.getArmatureDataByName(displayData.name), textureAtlasName, skinName);
						displayList.set(l, childArmature);
						break;

					case DisplayData.IMAGE:
					default:
						// displayList[l] = (displayData.name, textureAtlasName, displayData.pivot.x, displayData.pivot.y);
						throw new Error("BUG in original code?");
						break;

				}
			}
			//==================================================
			//如果显示对象有name属性并且name属性可以设置的话，将name设置为与slot同名，dragonBones并不依赖这些属性，只是方便开发者
			for (Object displayObject : displayList)
			{
				if(displayObject instanceof Armature)
				{
					displayObject = ((Armature)displayObject).getDisplay();
				}

				if(displayObject instanceof ISetName)
				{
					((ISetName)displayObject).setName(slot.getName());
				}
			}
			//==================================================
			skinListData[slotData.name] = displayList.clone();
		}
		armature.addSkinList(skinName, skinListData);
	}

	/**
	 * Parses the raw data and returns a SkeletonData instance.
	 * @example
	 * <listing>
	 * import flash.events.Event;
	 * import dragonBones.factorys.NativeFactory;
	 *
	 * [Embed(source = "../assets/Dragon1.swf", mimeType = "application/octet-stream")]
	 *	private static const ResourcesData:Class;
	 * var factory:NativeFactory = new NativeFactory();
	 * factory.addEventListener(Event.COMPLETE, textureCompleteHandler);
	 * factory.parseData(new ResourcesData());
	 * </listing>
	 * @param bytes ByteArray. Represents the raw data for the whole DragonBones system.
	 * @param dataName String. (optional) The SkeletonData instance name.
	 * @return A SkeletonData instance.
	 */
	public void parseData(ByteArray bytes, String dataName = null)
	{
		if(bytes == null)
		{
			throw new ArgumentError();
		}

		DecompressedData decompressedData = DataSerializer.decompressData(bytes);

		DragonBonesData dragonBonesData = DataParser.parseData(decompressedData.dragonBonesData);
		decompressedData.name = dataName != null ? dataName : dragonBonesData.name;
		decompressedData.addEventListener(Event.COMPLETE, new Runnable1<Event>() {
			@Override
			public void run(Event event) {
				DecompressedData decompressedData = (DecompressedData)event.target;
				decompressedData.removeEventListener(Event.COMPLETE, this);

				Object textureAtlas = generateTextureAtlas(decompressedData.textureAtlas, decompressedData.textureAtlasData);
				addTextureAtlas(textureAtlas, decompressedData.name);

				decompressedData.dispose();
				dispatchEvent(new Event(Event.COMPLETE));

			}
		});
		decompressedData.parseTextureAtlasBytes();

		addSkeletonData(dragonBonesData, dataName);
	}

	/** @private */
	protected ITextureAtlas generateTextureAtlas(Object content, Object textureAtlasRawData)
	{
		return null;
	}

	/**
	 * @private
	 * Generates an Armature instance.
	 * @return Armature An Armature instance.
	 */
	protected Armature generateArmature()
	{
		return null;
	}

	/**
	 * @private
	 * Generates an Armature instance.
	 * @return Armature An Armature instance.
	 */
	protected FastArmature generateFastArmature()
	{
		return null;
	}

	/**
	 * @private
	 * Generates an Slot instance.
	 * @return Slot An Slot instance.
	 */
	protected Slot generateSlot()
	{
		return null;
	}

	/**
	 * @private
	 * Generates an Slot instance.
	 * @return Slot An Slot instance.
	 */
	protected FastSlot generateFastSlot()
	{
		return null;
	}

	/**
	 * @private
	 * Generates a DisplayObject
	 * @param textureAtlas The TextureAtlas.
	 * @param fullName A qualified name.
	 * @param pivotX A pivot x based value.
	 * @param pivotY A pivot y based value.
	 * @return
	 */
	protected Object generateDisplay(Object textureAtlas, String fullName, Number pivotX, Number pivotY)
	{
		return null;
	}

}

class BuildArmatureDataPackage
{
	public String dragonBonesDataName;
	public DragonBonesData dragonBonesData;
	public ArmatureData armatureData;
	public String textureAtlasName;
}