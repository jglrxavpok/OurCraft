package org.craft.network;

import io.netty.buffer.*;

public class NettyPacket
{

    ByteBuf     payload;
    int         id;
    NetworkSide side;

    NettyPacket()
    {

    }

    public NettyPacket(int id, ByteBuf payload, NetworkSide side)
    {
        this.side = side;
        this.id = id;
        this.payload = payload;
    }

    public int getID()
    {
        return id;
    }

    public ByteBuf getPayload()
    {
        return payload;
    }

    public NetworkSide getSide()
    {
        return side;
    }
}
