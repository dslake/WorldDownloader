package wdl.litemod;

import com.mamiyaotaru.chatbubbles.litemod.ChatBubblesObfuscationTable;
import com.mumfrey.liteloader.core.runtime.Obf;

public class WDLObfuscationTable extends Obf {
		
	public static WDLObfuscationTable GuiIngameMenu = new WDLObfuscationTable("net.minecraft.client.gui.GuiIngameMenu", "bdp");
	public static WDLObfuscationTable GuiButton = new WDLObfuscationTable("net.minecraft.client.gui.GuiButton", "bcb");
	public static WDLObfuscationTable initGui = new WDLObfuscationTable("func_73866_w_", "b", "initGui");
	public static WDLObfuscationTable actionPerformed = new WDLObfuscationTable("func_146284_a", "a", "actionPerformed");
	
	public static WDLObfuscationTable World = new WDLObfuscationTable("net.minecraft.world.World", "ahb");
	public static WDLObfuscationTable WorldClient = new WDLObfuscationTable("net.minecraft.client.multiplayer.WorldClient", "bjf");
	public static WDLObfuscationTable Block = new WDLObfuscationTable("net.minecraft.block.Block", "aji");
	public static WDLObfuscationTable Entity = new WDLObfuscationTable("net.minecraft.entity.Entity", "sa");
	public static WDLObfuscationTable tick = new WDLObfuscationTable("func_72835_b", "b", "tick");
	public static WDLObfuscationTable doPreChunk = new WDLObfuscationTable("func_73025_a", "a", "doPreChunk");
	public static WDLObfuscationTable removeEntityFromWorld = new WDLObfuscationTable("func_73028_b", "b", "removeEntityFromWorld");
	public static WDLObfuscationTable addBlockEvent = new WDLObfuscationTable("func_147452_c", "c", "addBlockEvent");

	public static WDLObfuscationTable NetHandlerPlayClient = new WDLObfuscationTable("net.minecraft.client.network.NetHandlerPlayClient", "bjb");
	public static WDLObfuscationTable S40PacketDisconnect = new WDLObfuscationTable("net.minecraft.network.play.server.S40PacketDisconnect", "gs");
	public static WDLObfuscationTable IChatComponent = new WDLObfuscationTable("net.minecraft.util.IChatComponent", "fj");
	public static WDLObfuscationTable handleDisconnect = new WDLObfuscationTable("func_147253_a", "a", "handleDisconnect");
	public static WDLObfuscationTable onDisconnect = new WDLObfuscationTable("func_147231_a", "a", "onDisconnect");

	//WorldClient
	//addBlockEvent
	protected WDLObfuscationTable(String name) {
		super(name, name, name);
	}
	
	protected WDLObfuscationTable(String seargeName, String obfName) {
		super(seargeName, obfName, seargeName);
	}
	
	protected WDLObfuscationTable(String seargeName, String obfName, String mcpName) {
		super(seargeName, obfName, mcpName);
	}

}
