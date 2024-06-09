package com.example.robots;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelRobotPump extends ModelBase {
    private final ModelRenderer body;
    private final ModelRenderer body_r1;
    private final ModelRenderer antenna;
    private final ModelRenderer antenna_r1;
    private final ModelRenderer wingleft;
    private final ModelRenderer wingright;

    public ModelRobotPump() {
        textureWidth = 32;
        textureHeight = 32;

        body = new ModelRenderer(this);
        body.setRotationPoint(1.0F, 18.0F, 0.0F);


        body_r1 = new ModelRenderer(this);
        body_r1.setRotationPoint(-1.0F, 4.0F, 0.0F);
        body.addChild(body_r1);
        setRotationAngle(body_r1, 0.0F, 1.5708F, 0.0F);
        body_r1.cubeList.add(new ModelBox(body_r1, 0, 0, -3.1F, -20.0F, -1.0F, 1, 2, 2, 0.0F, false));
        body_r1.cubeList.add(new ModelBox(body_r1, 0, 0, -3.0F, -22.0F, -3.0F, 6, 6, 6, 0.0F, false));

        antenna = new ModelRenderer(this);
        antenna.setRotationPoint(1.0F, 23.0F, 0.0F);


        antenna_r1 = new ModelRenderer(this);
        antenna_r1.setRotationPoint(-1.0F, -1.0F, 0.0F);
        antenna.addChild(antenna_r1);
        setRotationAngle(antenna_r1, 0.0F, 1.5708F, 0.0F);
        antenna_r1.cubeList.add(new ModelBox(antenna_r1, 0, 17, 1.0F, -17.0F, -2.0F, 1, 6, 1, -0.35F, false));

        wingleft = new ModelRenderer(this);
        wingleft.setRotationPoint(0.0F, 22.0F, 0.0F);
        wingleft.cubeList.add(new ModelBox(wingleft, 0, 12, -5.0F, -21.0F, -2.0F, 2, 1, 4, 0.0F, false));

        wingright = new ModelRenderer(this);
        wingright.setRotationPoint(0.0F, 22.0F, 0.0F);
        wingright.cubeList.add(new ModelBox(wingright, 12, 12, 3.0F, -21.0F, -2.0F, 2, 1, 4, 0.0F, false));

    }

        @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        body.render(f5);
        antenna.render(f5);
        wingleft.render(f5);
        wingright.render(f5);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}