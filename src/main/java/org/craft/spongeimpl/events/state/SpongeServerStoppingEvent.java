package org.craft.spongeimpl.events.state;

import org.spongepowered.api.*;
import org.spongepowered.api.event.state.*;

public class SpongeServerStoppingEvent extends SpongeStateEvent implements ServerStoppingEvent
{

    public SpongeServerStoppingEvent(Game game)
    {
        super(game);
    }

    @Override
    public boolean isCancellable()
    {
        return false;
    }

}
