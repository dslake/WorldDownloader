package net.minecraft.wdl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;

public class ChunkProviderWDL implements IChunkProvider{
	
	Minecraft mc;
	World worldObj;
    
    private static final Logger logger = LogManager.getLogger();

    /**
     * The completely empty chunk used by ChunkProviderClient when chunkMapping doesn't contain the requested
     * coordinates.
     */
    private Chunk blankChunk;
    
    /**
     * The mapping between ChunkCoordinates and Chunks that ChunkProviderClient would maintain.  
     * ie what we get from the server.  WDL steals this one to save
     */
    private LongHashMap chunkMappingStandard = new LongHashMap();
    
    /**
     * The mapping between ChunkCoordinates and Chunks that ChunkProviderWDL maintains.
     * can include chunks loaded from disk as well as what's been received from the server
     */
    private LongHashMap chunkMapping = new LongHashMap();
    
    /**
     * This may have been intended to be an iterable version of all currently loaded chunks (MultiplayerChunkCache),
     * with identical contents to chunkMapping's values. However it is never actually added to.
     */
    private List chunkListing = new ArrayList();
    
    int lastPlayerChunkX = 0;
    int lastPlayerChunkZ = 0;
    int deleteRange = 16;

	public ChunkProviderWDL(World world) {
        this.blankChunk = new EmptyChunk(world, 0, 0);
        this.mc = Minecraft.getMinecraft();
		this.worldObj = world;
	}

    /**
     * Checks to see if a chunk exists at x, y
     */
	@Override
    public boolean chunkExists(int par1, int par2)
    {
        return true;
    }
	
	public void unloadChunk(int chunkX, int chunkZ) {
        Chunk chunk = this.provideChunk(chunkX, chunkZ);
        if (!chunk.isEmpty()) {
        	chunk.onChunkUnload();
        }
        this.chunkMappingStandard.remove(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ)); // delete from standard chunk map
        if (Math.abs(chunkX - lastPlayerChunkX) > deleteRange || Math.abs(chunkZ - lastPlayerChunkZ) > deleteRange) { // also out of our render range, delete from our distance based chunk map too 
        	this.chunkMapping.remove(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ));
        	this.chunkListing.remove(chunk);
        }
        
	}
	
	public void unloadStoredChunk(int chunkX, int chunkZ) {
		Chunk chunkStandard = (Chunk)this.chunkMappingStandard.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ));
        Chunk chunk = this.provideChunk(chunkX, chunkZ);

        if (!chunk.isEmpty() && chunkStandard == null) // not a chunk that would have been stored by vanilla ChunkProviderClient
        {
            chunk.onChunkUnload();
        }

        if (chunkStandard == null) { // remove from our distance based mapping if it isn't in the standard mapping
        	this.chunkMapping.remove(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ));
        	this.chunkListing.remove(chunk);
        }
	}

	@Override
	public Chunk loadChunk(int chunkX, int chunkZ) {
        Chunk chunk = new Chunk(this.worldObj, chunkX, chunkZ);
        this.chunkMappingStandard.add(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ), chunk);
        this.chunkMapping.add(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ), chunk);
        this.chunkListing.add(chunk);
        chunk.isChunkLoaded = true;
		return chunk;
	}
	
	public Chunk loadStoredChunk(int chunkX, int chunkZ) {
		Chunk chunk = provideChunk(chunkX, chunkZ);
		if (chunk.isEmpty()) { // only load if it isn't loaded already
			IChunkLoader chunkloader = null;
			if (mc.isIntegratedServerRunning()) { // get chunkLoader for singleplayer world
				WorldServer worldServer = mc.getIntegratedServer().worldServerForDimension(mc.thePlayer.dimension);
				chunkloader = worldServer.getSaveHandler().getChunkLoader(worldServer.provider);
			}
			else { // try to get WDL chunkloader
				chunkloader = (WDL.downloading)?WDL.chunkLoader:null; // only use WDL chunkLoader if it is downloading.  Otherwise no guarantee it is using current world/name/etc
			}
			if (chunkloader != null) { // if there is a chunkloader
				try {
					chunk = chunkloader.loadChunk(worldObj, chunkX, chunkZ); // load chunk from storage
					if (chunk != null) {
						this.chunkMapping.add(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ), chunk);
						this.chunkListing.add(chunk);
						chunk.isChunkLoaded = true;
				        this.worldObj.markBlockRangeForRenderUpdate(chunkX << 4, 0, chunkX << 4, (chunkX << 4) + 15, 256, (chunkZ << 4) + 15);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return chunk;
	}
	
	@Override
	public Chunk provideChunk(int chunkX, int chunkZ) {
        Chunk var3 = (Chunk)this.chunkMapping.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ));
        return var3 == null ? this.blankChunk : var3;	
    }

	/**
     * Two modes of operation: if passed true, save all Chunks in one go.  If passed false, save up to two chunks.
     * Return true if all chunks have been saved.
     */
	@Override
    public boolean saveChunks(boolean par1, IProgressUpdate par2IProgressUpdate)
    {
        return true;
    }
	
    /**
     * Save extra data not associated with any Chunk.  Not saved during autosave, only during world unload.  Currently
     * unimplemented.
     */
	@Override
    public void saveExtraData() {}

	/**
	 * called once per tick.  Can use for loading and unloading stored chunks
	 */
	@Override
	public boolean unloadQueuedChunks() {
	   	int playerChunkX = this.mc.thePlayer.chunkCoordX;
    	int playerChunkZ = this.mc.thePlayer.chunkCoordZ;
    	
    	if (playerChunkX != lastPlayerChunkX || playerChunkZ != lastPlayerChunkZ) {
    		int offsetX = Math.abs(playerChunkX - lastPlayerChunkX);
    		int offsetZ = Math.abs(playerChunkZ - lastPlayerChunkZ);
    		lastPlayerChunkX = playerChunkX;
    		lastPlayerChunkZ = playerChunkZ;
    		int range = this.mc.gameSettings.renderDistanceChunks;
    		for (int t = lastPlayerChunkX - range; t <= lastPlayerChunkX + range; t++) {
    			for (int s = lastPlayerChunkZ - range; s <= lastPlayerChunkZ + range; s++) {
    				this.loadStoredChunk(t, s);
    			}
    		}
    		
    		for (int t = lastPlayerChunkX - deleteRange - offsetX; t <= lastPlayerChunkX + deleteRange + offsetX; t++) {
    			for (int s = 1; s <= offsetZ; s++) {
    				this.unloadStoredChunk(t, lastPlayerChunkZ + deleteRange + s);
    				this.unloadStoredChunk(t, lastPlayerChunkZ - deleteRange - s);
    			}
    		}
    		for (int t = 1; t <= offsetX; t++) {
    			for (int s = lastPlayerChunkZ - deleteRange - offsetZ; s <= lastPlayerChunkZ + deleteRange + offsetZ; s++) {
    				this.unloadStoredChunk(lastPlayerChunkX + deleteRange + t, s);
    				this.unloadStoredChunk(lastPlayerChunkX - deleteRange - t, s);
    			}
    		}
    	}

        long var1 = System.currentTimeMillis();
        Iterator var3 = this.chunkListing.iterator();

        while (var3.hasNext())
        {
            Chunk var4 = (Chunk)var3.next();
            var4.func_150804_b(System.currentTimeMillis() - var1 > 5L);
        }

        if (System.currentTimeMillis() - var1 > 100L)
        {
            logger.info("Warning: Clientside chunk ticking took {} ms", new Object[] {Long.valueOf(System.currentTimeMillis() - var1)});
        }

        return false;
	}

    /**
     * Returns if the IChunkProvider supports saving.
     */
	@Override
    public boolean canSave()
    {
        return false;
    }
	
    /**
     * Populates chunk with ores etc etc
     */
	@Override
    public void populate(IChunkProvider par1IChunkProvider, int par2, int par3) {}

    /**
     * Converts the instance data to a readable string.
     */
	@Override
    public String makeString()
    {
        return "MultiplayerChunkCache: " + this.chunkMapping.getNumHashElements() + ", " + this.chunkListing.size();
    }

    /**
     * Returns a list of creatures of the specified type that can spawn at the given location.
     */
	@Override
    public List getPossibleCreatures(EnumCreatureType par1EnumCreatureType, int par2, int par3, int par4)
    {
        return null;
    }

	@Override
    public ChunkPosition findClosestStructure(World p_147416_1_, String p_147416_2_, int p_147416_3_, int p_147416_4_, int p_147416_5_)
    {
        return null;
    }

	@Override
    public int getLoadedChunkCount()
    {
        return this.chunkListing.size();
    }

	@Override
    public void recreateStructures(int par1, int par2) {}


}
