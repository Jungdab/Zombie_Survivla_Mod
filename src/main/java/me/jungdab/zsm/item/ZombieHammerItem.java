package me.jungdab.zsm.item;

import me.jungdab.zsm.registry.ModEffects;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

import java.util.List;

public class ZombieHammerItem extends Item {

    public ZombieHammerItem(Settings settings) {
        super(settings.maxDamage(250).attributeModifiers(createAttributeModifiers()).component(DataComponentTypes.LORE, new LoreComponent(List.of(Text.literal(""), Text.translatable("item.zsm.zombie_hammer.lore"), Text.translatable("item.zsm.zombie_hammer.lore2")))));
    }

    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if(!(attacker instanceof PlayerEntity)) target.addStatusEffect(new StatusEffectInstance(ModEffects.STUN, 40, 0));
        return true;
    }

    public void postDamageEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.damage(1, attacker, EquipmentSlot.MAINHAND);
    }

    public void attack(LivingEntity target, PlayerEntity attacker, float attackCooldown) {
        if(attackCooldown >= 1.0f) {
            target.addStatusEffect(new StatusEffectInstance(ModEffects.STUN, 40, 0));
            smash(target, attacker);
        }
    }

    private void smash(LivingEntity mob, PlayerEntity attacker) {
        if(!(attacker.getWorld() instanceof ServerWorld world)) return;

        mob.playSound(SoundEvents.ITEM_MACE_SMASH_GROUND_HEAVY, 1F, 1F);

        List<LivingEntity> targetList = mob.getWorld().getEntitiesByClass(LivingEntity.class, new Box(mob.getX() - 5, mob.getY() - 1, mob.getZ() - 5, mob.getX() + 5, mob.getY() + 1.5, mob.getZ() + 5), entity -> !(entity instanceof PlayerEntity) && !entity.equals(mob));
        if(targetList.isEmpty()) return;

        DamageSource damageSource = attacker.getDamageSources().playerAttack(attacker);

        float damage = (float) attacker.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);

        for(LivingEntity entity : targetList) {
            entity.damage(world, damageSource, damage);
        }
    }



    private static AttributeModifiersComponent createAttributeModifiers() {
        return AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.ATTACK_DAMAGE,
                        new EntityAttributeModifier(
                                BASE_ATTACK_DAMAGE_MODIFIER_ID, ((float) 9), EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .add(
                        EntityAttributes.ATTACK_SPEED,
                        new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, (float) -3.4, EntityAttributeModifier.Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND
                )
                .build();
    }
}
