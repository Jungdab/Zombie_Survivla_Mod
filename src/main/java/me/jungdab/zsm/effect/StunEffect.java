package me.jungdab.zsm.effect;

import me.jungdab.zsm.ZSM;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.util.Identifier;

public class StunEffect extends StatusEffect {
    public StunEffect() {
        super(StatusEffectCategory.HARMFUL, 0xFAB12F);

        this.addAttributeModifier(EntityAttributes.JUMP_STRENGTH, Identifier.of(ZSM.MOD_ID, "effect.stun"), -100F, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            .addAttributeModifier(EntityAttributes.MOVEMENT_SPEED, Identifier.of(ZSM.MOD_ID, "effect.stun"), -100F, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }
}
