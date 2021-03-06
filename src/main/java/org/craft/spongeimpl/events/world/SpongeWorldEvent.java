package org.craft.spongeimpl.events.world;

import org.craft.spongeimpl.events.*;
import org.spongepowered.api.*;
import org.spongepowered.api.event.world.*;
import org.spongepowered.api.world.*;

public abstract class SpongeWorldEvent extends SpongeGameEvent implements WorldEvent
{

    private World world;

    public SpongeWorldEvent(Game game, World world)
    {
        super(game);
        this.world = world;
    }

    @Override
    public World getWorld()
    {
        return world;
    }

}
