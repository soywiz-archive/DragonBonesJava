package dragonBones.events;

import flash.errors.IllegalOperationError;
import flash.events.EventDispatcher;

@EventInfo(name="sound",type="dragonBones.events.SoundEvent")
/**
 * 全局声音管理，通过监听SoundEventManager的SoundEvent事件得到动画的声音触发时间和声音的名字
 */
public final class SoundEventManager extends EventDispatcher
{
	private static SoundEventManager _instance;

	public static SoundEventManager getInstance()
	{
		if (!_instance)
		{
			_instance = new SoundEventManager();
		}
		return _instance;
	}

	public SoundEventManager()
	{
		super();
		if (_instance != null)
		{
			throw new IllegalOperationError("Singleton already constructed!");
		}
	}
}
