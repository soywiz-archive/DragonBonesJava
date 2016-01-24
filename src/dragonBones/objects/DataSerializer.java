package dragonBones.objects;

import dragonBones.utils.BytesType;

import flash.display.Loader;
import flash.events.EventDispatcher;
import flash.utils.ByteArray;

///** Dispatched after a sucessful call to decompressData(). */
//@EventInfo(name="complete", type="flash.events.Event")
public class DataSerializer extends EventDispatcher
{
	public DataSerializer()
	{
	}

	/**
	 * Compress all data into a ByteArray for serialization.
	 * @param dragonBonesData The DragonBones data.
	 * @param textureAtlasData The TextureAtlas data.
	 * @param textureAtlasBytes The ByteArray representing the map.
	 * @return ByteArray. A DragonBones compatible ByteArray.
	 */

	static public ByteArray compressDataToByteArray(Object dragonBonesData, Object textureAtlasData, ByteArray textureAtlasBytes)
	{
		ByteArray outputBytes = new ByteArray();
		outputBytes.writeBytes(textureAtlasBytes);

		ByteArray dataBytes = new ByteArray();
		dataBytes.writeObject(textureAtlasData);
		dataBytes.compress();

		outputBytes.setPosition(outputBytes.getLength());
		outputBytes.writeBytes(dataBytes);
		outputBytes.writeInt(dataBytes.getLength());

		dataBytes.setLength(0);
		dataBytes.writeObject(dragonBonesData);
		dataBytes.compress();

		outputBytes.setPosition(outputBytes.getLength());
		outputBytes.writeBytes(dataBytes);
		outputBytes.writeInt(dataBytes.getLength());

		return outputBytes;
	}

	/**
	 * Decompress a compatible DragonBones data.
	 * @param inputByteArray The ByteArray to decompress.
	 * @return A DecompressedData instance.
	 */
	public static DecompressedData decompressData(ByteArray inputByteArray)
	{
		String dataType = BytesType.getType(inputByteArray);
		switch (dataType)
		{
			case BytesType.SWF:
			case BytesType.PNG:
			case BytesType.JPG:
			case BytesType.ATF:
				Object dragonBonesData;
				String textureAtlasData;
				Object textureAtlas;
				ByteArray bytesToDecompress = null;
				try
				{
					ByteArray tempByteArray = new ByteArray();
					bytesToDecompress = new ByteArray();
					bytesToDecompress.writeBytes(inputByteArray);

				//Read DragonBones Data
					bytesToDecompress.setPosition(bytesToDecompress.getLength() - 4);
					int strSize = bytesToDecompress.readInt();
					int position = bytesToDecompress.getLength() - 4 - strSize;
					tempByteArray.writeBytes(bytesToDecompress, position, strSize);
					tempByteArray.uncompress();
					dragonBonesData = tempByteArray.readObject();

					tempByteArray.setLength(0);
					bytesToDecompress.setLength(position);

				//Read TextureAtlas Data
					bytesToDecompress.setPosition(bytesToDecompress.getLength() - 4);
					strSize = bytesToDecompress.readInt();
					position = bytesToDecompress.getLength() - 4 - strSize;
					tempByteArray.writeBytes(bytesToDecompress, position, strSize);
					tempByteArray.uncompress();
					textureAtlasData = (String) tempByteArray.readObject();
					bytesToDecompress.setLength(position);
				}
				catch (Exception e)
				{
					throw new Error("Data error!");
				}

				DecompressedData outputDecompressedData = new DecompressedData();
				outputDecompressedData.textureBytesDataType = dataType;
				outputDecompressedData.dragonBonesData = dragonBonesData;
				outputDecompressedData.textureAtlasData = textureAtlasData;
				outputDecompressedData.textureAtlasBytes = bytesToDecompress;

				return outputDecompressedData;
		}

		throw new Error("Nonsupport data!");
	}
}
