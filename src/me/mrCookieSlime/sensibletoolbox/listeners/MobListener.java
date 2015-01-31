package me.mrCookieSlime.sensibletoolbox.listeners;

import me.mrCookieSlime.CSCoreLibPlugin.CSCoreLib;
import me.mrCookieSlime.sensibletoolbox.SensibleToolboxPlugin;
import me.mrCookieSlime.sensibletoolbox.api.items.BaseSTBItem;
import me.mrCookieSlime.sensibletoolbox.items.components.InfernalDust;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class MobListener extends STBBaseListener {
	
    public MobListener(SensibleToolboxPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        if (event.getEntity().getType() == EntityType.BLAZE && event.getEntity().getKiller() != null) {
            InfernalDust dust = new InfernalDust();
            Player killer = event.getEntity().getKiller();
            if (dust.checkPlayerPermission(killer, BaseSTBItem.ItemAction.CRAFT)) {
                int chance = 20, amount = 1;
                Random r = CSCoreLib.randomizer();
                ItemStack item = killer.getItemInHand();
                if (item != null) {
                    switch (item.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS)) {
                        case 1:
                            chance = 30;
                            amount = r.nextInt(2) + 1;
                            break;
                        case 2:
                            chance = 40;
                            amount = r.nextInt(2) + 1;
                            break;
                        case 3:
                            chance = 50;
                            amount = r.nextInt(3) + 1;
                            break;
                    }
                }
                if (r.nextInt(100) < chance) {
                    event.getDrops().add(dust.toItemStack(amount));
                }
            }
        }
    }
}
