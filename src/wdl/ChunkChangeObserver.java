package wdl;

import java.util.Observable;
import java.util.Observer;

import net.minecraft.world.chunk.Chunk;

public class ChunkChangeObserver implements Observer {
	
	public ChunkChangeObserver() {
        if (classExists("com.thevoxelbox.voxelmap.interfaces.AbstractVoxelMap")) {
        	com.thevoxelbox.voxelmap.interfaces.AbstractVoxelMap.getInstance().getNotifier().addObserver(this);
        }
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof Chunk)
			WDL.chunkChanged((Chunk)arg);
	}
	
	public static boolean classExists(String className) {
		try {
			Class.forName (className);
			return true;
		}
		catch (ClassNotFoundException exception) {
			return false;
		}
	}

}
