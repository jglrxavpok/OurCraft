package org.craft.entity;

import org.craft.blocks.*;
import org.craft.client.*;
import org.craft.utils.*;
import org.craft.utils.CollisionInfos.CollisionType;
import org.craft.world.*;
import org.lwjgl.input.*;

public class EntityPlayer extends Entity
{

    public EntityPlayer(World world)
    {
        super(world);
        setSize(0.75f, 1.80f, 0.75f);
    }

    public void onEntityUpdate()
    {
        float speed = 1f / 10f;
        if(!isOnGround())
            speed /= 50f;
        if(Keyboard.isKeyDown(Keyboard.KEY_Z))
        {
            this.moveForward(speed);
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_S))
        {
            this.moveBackwards(speed);
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_Q))
        {
            this.moveLeft(speed);
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_D))
        {
            this.moveRight(speed);
        }

        yaw += (float) Math.toRadians(OurCraft.getOurCraft().getMouseHandler().getDX());
        pitch += (float) Math.toRadians(-OurCraft.getOurCraft().getMouseHandler().getDY());

        CollisionInfos infos = OurCraft.getOurCraft().getObjectInFront();
        if(infos.type == CollisionType.BLOCK)
        {
            while(Mouse.next())
            {
                if((Mouse.getEventButton() == 0) && (Mouse.getEventButtonState()))
                {
                    getWorld().setBlock((int) Math.round(infos.x), (int) Math.round(infos.y), (int) Math.round(infos.z), Blocks.air);
                }
                else if((Mouse.getEventButton() == 1) && (Mouse.getEventButtonState()))
                {
                    int x = (int) infos.x + infos.side.getTranslationX();
                    int y = (int) (infos.y) + infos.side.getTranslationY();
                    int z = (int) (infos.z + infos.side.getTranslationZ());
                    getWorld().setBlock(x, y, z, Blocks.log);
                    Blocks.log.onBlockAdded(getWorld(), x, y, z, infos.side, this);
                }
            }

        }
    }

    public float getEyeOffset()
    {
        return 1.7f;
    }

}
