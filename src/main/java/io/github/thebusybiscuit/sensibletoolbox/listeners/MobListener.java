package io.github.thebusybiscuit.sensibletoolbox.listeners;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.items.components.InfernalDust;

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
                int chance = 20;
                int amount = 1;

                Random random = ThreadLocalRandom.current();
                ItemStack item = killer.getInventory().getItemInMainHand();
                switch (item.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS)) {
                case 1:
                    chance = 30;
                    amount = random.nextInt(2) + 1;
                    break;
                case 2:
                    chance = 40;
                    amount = random.nextInt(2) + 1;
                    break;
                case 3:
                    chance = 50;
                    amount = random.nextInt(3) + 1;
                    break;
                default:
                    break;
                }

                if (random.nextInt(100) < chance) {
                    event.getDrops().add(dust.toItemStack(amount));
                }
            }
        }
    }
}
