package com.example.robots;


import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import static java.lang.System.out;

public class RenderEntityRobot extends Render<EntityRobot> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Robots.MODID, "textures/entity/robotpump.png");
    private final ModelRobotPump pump = new ModelRobotPump();

    public RenderEntityRobot(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityRobot entity) {
        return TEXTURE;
    }

    @Override
    public void doRender(EntityRobot entity, double x, double y, double z, float entityYaw, float partialTicks) {
        //out. println("Render method is called");
        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();

        // Interpolate the entity's position between ticks
        double interpolatedX = x - (entity.posX - entity.lastTickPosX) * (1-partialTicks);
        double interpolatedY = y - (entity.posY - entity.lastTickPosY) * (1-partialTicks);
        double interpolatedZ = z - (entity.posZ - entity.lastTickPosZ) * (1-partialTicks);

        GlStateManager.translate((float)interpolatedX, (float)interpolatedY, (float)interpolatedZ);
        GlStateManager.rotate(- entity.rotationYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(entity.rotationPitch, 1.0F, 0.0F, 0.0F);


        //GlStateManager.translate((float)x, (float)y, (float)z);
        this.bindTexture(getEntityTexture(entity));

        pump.render((Entity) entity,0,0,0,0,0,(float)(1.0/16.0));

        GlStateManager.popMatrix();

        super.doRender(entity, x, y, z, entityYaw, partialTicks);

    }

}
