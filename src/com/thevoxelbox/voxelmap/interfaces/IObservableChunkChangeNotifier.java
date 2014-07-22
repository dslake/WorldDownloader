package com.thevoxelbox.voxelmap.interfaces;

import java.util.Observer;

import net.minecraft.world.chunk.Chunk;

public interface IObservableChunkChangeNotifier {

	void chunkChanged(Chunk chunk);

	void addObserver(Observer observer);

}
