package dragonBones.objects;

import dragonBones.events.EventInfo;
import dragonBones.utils.BytesType;

import flash.display.Loader;
import flash.events.EventDispatcher;
import flash.utils.ByteArray;

/** Dispatched after a sucessful call to decompressData(). */
@EventInfo(name="complete", type="flash.events.Event")
public class DataSerializer extends EventDispatcher
{
	public DataSerializer()
	{
	}

	/**
	 * Compress all data into a ByteArray for serialization.
	 * @param The DragonBones data.
	 * @param The TextureAtlas data.
	 * @param The ByteArray representing the map.
	 * @return ByteArray. A DragonBones compatible ByteArray.
	 */

	static public ByteArray compressDataToByteArray(Object dragonBonesData, Object textureAtlasData, ByteArray textureAtlasBytes)
	{
		ByteArray outputBytes = new ByteArray();
		outputBytes.writeBytes(textureAtlasBytes);

		ByteArray dataBytes = new ByteArray();
		dataBytes.writeObject(textureAtlasData);
		dataBytes.compress();

		outputBytes.position = outputBytes.length;
		outputBytes.writeBytes(dataBytes);
		outputBytes.writeInt(dataBytes.length);

		dataBytes.length = 0;
		dataBytes.writeObject(dragonBonesData);
		dataBytes.compress();

		outputBytes.position = outputBytes.length;
		outputBytes.writeBytes(dataBytes);
		outputBytes.writeInt(dataBytes.length);

		return outputBytes;
	}

	/**
	 * Decompress a compatible DragonBones data.
	 * @param compressedByteArray The ByteArray to decompress.
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
				Object textureAtlasData;
				Object textureAtlas;
				try
				{
					ByteArray tempByteArray = new ByteArray();
					ByteArray bytesToDecompress = new ByteArray();
					bytesToDecompress.writeBytes(inputByteArray);

				//Read DragonBones Data
					bytesToDecompress.position = bytesToDecompress.length - 4;
					int strSize = bytesToDecompress.readInt();
					int position = bytesToDecompress.length - 4 - strSize;
					tempByteArray.writeBytes(bytesToDecompress, position, strSize);
					tempByteArray.uncompress();
					dragonBonesData = tempByteArray.readObject();

					tempByteArray.length = 0;
					bytesToDecompress.length = position;

				//Read TextureAtlas Data
					bytesToDecompress.position = bytesToDecompress.length - 4;
					strSize = bytesToDecompress.readInt();
					position = bytesToDecompress.length - 4 - strSize;
					tempByteArray.writeBytes(bytesToDecompress, position, strSize);
					tempByteArray.uncompress();
					textureAtlasData = tempByteArray.readObject();
					bytesToDecompress.length = position;
				}
				catch (Exception e)
				{
					throw new Exception("Data error!");
				}

				DecompressedData outputDecompressedData = new DecompressedData();
				outputDecompressedData.textureBytesDataType = dataType;
				outputDecompressedData.dragonBonesData = dragonBonesData;
				outputDecompressedData.textureAtlasData = textureAtlasData;
				outputDecompressedData.textureAtlasBytes = bytesToDecompress

				return outputDecompressedData;

			default:
				throw new Error("Nonsupport data!");
		}

		return null;
	}
}
