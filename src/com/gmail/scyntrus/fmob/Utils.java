package com.gmail.scyntrus.fmob;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import net.minecraft.server.v1_7_R3.Entity;
import net.minecraft.server.v1_7_R3.EntityAnimal;
import net.minecraft.server.v1_7_R3.EntityCreeper;
import net.minecraft.server.v1_7_R3.EntityPlayer;
import net.minecraft.server.v1_7_R3.EntitySlime;
import net.minecraft.server.v1_7_R3.EntityWolf;
import net.minecraft.server.v1_7_R3.EntityZombie;
import net.minecraft.server.v1_7_R3.IMonster;
import net.minecraft.server.v1_7_R3.Item;
import net.minecraft.server.v1_7_R3.ItemStack;
import net.minecraft.server.v1_7_R3.NBTTagCompound;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import com.gmail.scyntrus.ifactions.Faction;
import com.gmail.scyntrus.ifactions.UPlayer;

public class Utils {
	public static int FactionCheck(Entity entity, Faction faction) {
		if (entity == null || faction == null || faction.isNone()) {
			return 0;
		}
		if (entity.getBukkitEntity().hasMetadata("MyPet"))
			return 0;
		if (entity instanceof EntityPlayer) {
			Player player = ((EntityPlayer)entity).getBukkitEntity();
			if (player.getGameMode() == GameMode.CREATIVE) return 1;
			return UPlayer.getPlayerFaction(player).getRelationTo(faction);
		} else if (entity instanceof FactionMob) {
			FactionMob fmob = (FactionMob) entity;
			if (fmob.getFaction() == null) {
				return 0;
			}
			return fmob.getFaction().getRelationTo(faction);
		} else if (entity instanceof EntityWolf) {
			EntityWolf wolf = (EntityWolf) entity;
			if (wolf.isTamed()) {
				if (wolf.getOwner() != null) {
					return FactionCheck(wolf.getOwner(), faction);
				} else {
					return 0;
				}
			} else if (wolf.isAngry()) {
				return -1;
			} else {
				return 0;
			}
		} else if (entity instanceof EntityCreeper) {
			return 1;
		} else if (!FactionMobs.attackMobs) {
			return 0;
		} else if (entity instanceof EntityAnimal) {
			return 0;
		} else if (entity instanceof EntityZombie) {
			if (FactionMobs.attackZombies) {
				return -1;
			}
			return 0;
		} else if (entity instanceof EntitySlime) {
			EntitySlime slime = (EntitySlime) entity;
			if (slime.getSize() > 1) {
				return -1;
			} else {
				return 0;
			}
		} else if (entity instanceof IMonster) {
			return -1;
		}
		return 1;
	}
	
	public static void giveColorArmor(FactionMob entity) {
		int color = -1;
		if (entity.getFaction() == null) {
			return;
		} else if (FactionMobs.factionColors.containsKey(entity.getFaction().getName())) {
			color = FactionMobs.factionColors.get(entity.getFaction().getName());
		} else {
			FactionMobs.factionColors.put(entity.getFaction().getName(), 10511680);
		}
		
		if (color == -1 || color == 10511680) {
			entity.getEntity().setEquipment(1, new ItemStack((Item)Item.REGISTRY.a("leather_boots")));
			entity.getEntity().setEquipment(2, new ItemStack((Item)Item.REGISTRY.a("leather_leggings")));
			entity.getEntity().setEquipment(3, new ItemStack((Item)Item.REGISTRY.a("leather_chestplate")));
			entity.getEntity().setEquipment(4, new ItemStack((Item)Item.REGISTRY.a("leather_helmet")));
			return;
		}
		
		ItemStack[] itemStacks = {
				new ItemStack((Item)Item.REGISTRY.a("leather_boots")), 
				new ItemStack((Item)Item.REGISTRY.a("leather_leggings")), 
				new ItemStack((Item)Item.REGISTRY.a("leather_chestplate")),
				new ItemStack((Item)Item.REGISTRY.a("leather_helmet"))};

	    for (ItemStack i : itemStacks) {
	    	NBTTagCompound n = i.getTag();
			if (n == null) {
				n = new NBTTagCompound();
				i.setTag(n);
			}
			NBTTagCompound n2 = n.getCompound("display");
			if (!n.hasKey("display"))
				n.set("display", n2);
			n2.setInt("color", color);
	    }
	    
	    entity.getEntity().setEquipment(1, itemStacks[0]);
	    entity.getEntity().setEquipment(2, itemStacks[1]);
	    entity.getEntity().setEquipment(3, itemStacks[2]);
	    entity.getEntity().setEquipment(4, itemStacks[3]);
        return;
	}
	
	public FactionMob mobCreate() { // not implemented yet
		return null;
	}
	
	public static double dist3D(double x1, double x2, double y1, double y2, double z1, double z2) {
		return Math.sqrt(Math.pow(x1-x2,2) + Math.pow(y1-y2,2) + Math.pow(z1-z2,2));
	}
	
	public static double countMobPowerInFaction(Faction faction) {
		double power = 0;
		for (FactionMob fmob : FactionMobs.mobList) {
			if (fmob.getFactionName().equals(faction.getName())) {
				power += fmob.getPowerCost();
			}
		}
		return power;
	}
	
	public static int countMobsInFaction(Faction faction) {
		int count = 0;
		for (FactionMob fmob : FactionMobs.mobList) {
			if (fmob.getFactionName().equals(faction.getName())) {
				count++;
			}
		}
		return count;
	}
	
	public static void copyDefaultConfig() {
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("/config.yml");
		if (stream == null) {
			if (!FactionMobs.silentErrors)
				System.out.println("Unable to find default config.yml");
			return;
		}
		OutputStream resStreamOut = null;
		int readBytes;
		byte[] buffer = new byte[4096];
		try {
			resStreamOut = new FileOutputStream(new File(FactionMobs.instance.getDataFolder(), "configDefaults.yml"));
			while ((readBytes = stream.read(buffer)) > 0) {
				resStreamOut.write(buffer, 0, readBytes);
			}
		} catch (Exception e) {
			if (!FactionMobs.silentErrors){
				System.out.println("Unable to create configDefaults.yml. Check permissions or create it manually.");
				//debugging check should be here
				e.printStackTrace();
			}
		} finally {
			try {
				stream.close();
			} catch (Exception e) {
				if (!FactionMobs.silentErrors)
					e.printStackTrace();
			}
			try {
				resStreamOut.close();
			} catch (Exception e) {
				if (!FactionMobs.silentErrors)
					e.printStackTrace();
			}
		}
	}
}
