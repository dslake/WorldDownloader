package com.thevoxelbox.voxelmap.interfaces;


public abstract class AbstractVoxelMap implements IVoxelMap {
	
	public static AbstractVoxelMap instance = null;
	
	public static AbstractVoxelMap getInstance() {
		return instance;
	}

}

/* exists solely so I can provide getInstance() in a static context for other mods to be able to grab without
needing to know about VoxelMap itself.  IVoxelMap as an interface cannot provide a method that can be accessed statically
and I don't want people to need a stub of VoxelMap to compile against, so an interface plus a simple abstract class 
to compile against seems the next best thing after just an interface */