package wdl.litemod;

import java.io.File;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.INetHandler;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IntHashMap;
import wdl.ReflectionUtils;
import wdl.WDL;

import com.mumfrey.liteloader.ChatListener;
import com.mumfrey.liteloader.JoinGameListener;
import com.mumfrey.liteloader.Permissible;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.permissions.PermissionsManager;
import com.mumfrey.liteloader.permissions.PermissionsManagerClient;
import com.mumfrey.liteloader.transformers.event.EventInfo;
import com.mumfrey.liteloader.transformers.event.ReturnEventInfo;

public class LiteModWorldDownloader implements JoinGameListener, Tickable, Permissible, ChatListener 
{

	private long lastTick = 0;
	private boolean hasVoxelPlugins = true;
	
	private boolean canCacheChunks = true;
	
	//private PacketHandler packetHandler; // can be used to get name of multiverse world from voxel plugin installed on server (if any)
	
	public LiteModWorldDownloader()
	{
	}
		
	@Override
	public String getName()
	{
		return "WorldDownloader";
	}
	
	@Override
	public String getVersion()
	{
		return "1.0.0";
	}
	
	@Override
	public void init(File configPath)
	{
/*		hasVoxelPlugins = hasVoxelPlugins && ReflectionUtils.classExists("com.thevoxelbox.voxelpacket.client.VoxelPacketClient");
		hasVoxelPlugins = hasVoxelPlugins && ReflectionUtils.classExists("com.thevoxelbox.voxelpacket.common.VoxelMessage");
		hasVoxelPlugins = hasVoxelPlugins && ReflectionUtils.classExists("com.thevoxelbox.voxelpacket.common.interfaces.IVoxelMessagePublisher");
		hasVoxelPlugins = hasVoxelPlugins && ReflectionUtils.classExists("com.thevoxelbox.voxelpacket.common.interfaces.IVoxelMessageSubscriber");
		if (hasVoxelPlugins)
			packetHandler = new PacketHandler(configPath);*/
	}

	@Override
	public void upgradeSettings(String version, File configPath, File oldConfigPath)
	{
	}
	
	@Override
	public void onTick(Minecraft minecraft, float partialTicks, boolean inGame,	boolean clock) {
        if( WDL.mc.theWorld != WDL.wc ) {
        	if (WDL.mc.theWorld == null) {
        		WDL.stop();
        		WDL.wc = null;
        	}
        	else {
        		WDL.onWorldLoad();
        	}
        }
	}
	
	/* (non-Javadoc)
	 * @see net.minecraft.src.BaseMod#serverConnect(net.minecraft.src.NetClientHandler)
	 */
	@Override
	public void onJoinGame(INetHandler netHandler, S01PacketJoinGame joinGamePacket) {
	//	if (hasVoxelPlugins && packetHandler != null) {
	//		packetHandler.onServerConnect(netHandler);
	//	}
	}

    @Override
    public String getPermissibleModName() {
    	return getName().toLowerCase();
    }

    @Override
    public float getPermissibleModVersion() {
    	return Float.parseFloat(getVersion().replace(".", ""));
    }

    @Override
    public void registerPermissions(PermissionsManagerClient permissionsManager) {
    	permissionsManager.registerModPermission(this, "cachechunks");
    }

    @Override
    public void onPermissionsCleared(PermissionsManager manager) {
    	canCacheChunks = true;
    }

    @Override
    public void onPermissionsChanged(PermissionsManager manager) {
    	canCacheChunks = ((PermissionsManagerClient)manager).getModPermission(this, "cachechunks");
    	// same as
    	//canCacheChunks = manager.getPermissions(this).getHasPermission("mod.worlddownloader.cachechunks");
 // TODO   	WDL.setPermissions(canCacheChunks);
    }

	@Override
	public void onChat(IChatComponent chat, String message) {
		WDL.handleServerSeedMessage(message);
	}
	
	
	
	
    public static void initGui(EventInfo<GuiIngameMenu> e) {
    	GuiIngameMenu ingameMenu = e.getSource();
    	List buttonList = (List)ReflectionUtils.getPrivateFieldValueByType(ingameMenu, GuiScreen.class, List.class);
        WDL.injectWDLButtons(ingameMenu, buttonList);
    }

    public static void actionPerformed(EventInfo<GuiIngameMenu> e, GuiButton p_146284_1_) {
        WDL.handleWDLButtonClick(e.getSource(), p_146284_1_);
    }
    
    public static void tick(EventInfo<WorldClient> e) {
        if( wdl.WDL.downloading )
        {
            if( wdl.WDL.tp.openContainer != wdl.WDL.windowContainer )
            {
                if( wdl.WDL.tp.openContainer == wdl.WDL.tp.inventoryContainer )
                    wdl.WDL.onItemGuiClosed();
                else
                    wdl.WDL.onItemGuiOpened();
                wdl.WDL.windowContainer = wdl.WDL.tp.openContainer;
            }
        }
    }
    
    public static void doPreChunk(EventInfo<WorldClient> e, int p_73025_1_, int p_73025_2_, boolean p_73025_3_) {
    	WorldClient wc = e.getSource();
        if (p_73025_3_)
        {
            if( wc != wdl.WDL.wc ) wdl.WDL.onWorldLoad();
        }
        else
        {
            if( wdl.WDL.downloading ) wdl.WDL.onChunkNoLongerNeeded( wc.getChunkProvider().provideChunk(p_73025_1_, p_73025_2_) );
        }
    }
    
    public static void removeEntityFromWorld(ReturnEventInfo<WorldClient, ?> e, int p_73028_1_) {
    	WorldClient wc = e.getSource();
    	IntHashMap entityHashSet = (IntHashMap)ReflectionUtils.getPrivateFieldValueByType(wc, WorldClient.class, IntHashMap.class);
        Entity var2 = (Entity)entityHashSet.lookup(p_73028_1_); // lookup instead of removeObject so removeObject has something to remove when the actual method runs
        
        if(wdl.WDL.shouldKeepEntity(var2)) 
        {
        	entityHashSet.removeObject(p_73028_1_); // remove for real since we'll be canceling the actual method (in which it would have been removed regardless)
        	e.setReturnValue(null);
        }
    }
    
    public static void addBlockEvent(EventInfo<WorldClient> e, int par1, int par2, int par3, Block par4, int par5, int par6) {
        if( wdl.WDL.downloading )
            wdl.WDL.onBlockEvent( par1, par2, par3, par4, par5, par6 );
    }
    
    public static void handleDisconnect(EventInfo<NetHandlerPlayClient> e, S40PacketDisconnect arg1) {
        if (wdl.WDL.downloading)
        {
            wdl.WDL.stop();

            try
            {
                Thread.sleep(2000L);
            }
            catch (Exception var3)
            {
                ;
            }
        }
    }
    
    public static void onDisconnect(EventInfo<NetHandlerPlayClient> e, IChatComponent arg1) {
        if (wdl.WDL.downloading)
        {
            wdl.WDL.stop();

            try
            {
                Thread.sleep(2000L);
            }
            catch (Exception var3)
            {
                ;
            }
        }
    }

}
