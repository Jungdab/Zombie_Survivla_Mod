package me.jungdab.zsm.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.jungdab.zsm.ZSM;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.*;

@Mixin(LogoDrawer.class)
public class LogoDrawerMixin {
    @Shadow @Final public static Identifier EDITION_TEXTURE;
    @Unique
    private static final Identifier ZSM_LOGO_TEXTURE = Identifier.of(ZSM.MOD_ID, "textures/gui/title/zsm.png");


    /**
     * @author Jungdab
     * @reason changing logo
     */
    @Overwrite
    public void draw(DrawContext context, int screenWidth, float alpha, int y) {
        RenderSystem.enableBlend();
        int i = screenWidth / 2 - 128;
        context.drawTexture(RenderLayer::getGuiTextured, ZSM_LOGO_TEXTURE, i, y + 10, 0.0F, 0.0F, 256, 28, 256, 28);
        int j = screenWidth / 2 - 64;
        int k = y + 44 - 7;
        context.drawTexture(RenderLayer::getGuiTextured, EDITION_TEXTURE, j, k, 0.0F, 0.0F, 128, 14, 128, 16);
        RenderSystem.disableBlend();
    }
}
