package org.craft.blocks.states;

import java.util.*;

public final class BlockStates
{

    /**
     * Block state handling logs orientation
     */
    public static BlockState                                                   logOrientation;

    /**
     * Block states handling connexions between cables
     */
    public static BlockState                                                   cableConnexions;

    /**
     * Block state handling electric power (0-15)
     */
    public static BlockState                                                   electricPower;

    /**
     * Maps String representations of block states to their respective BlockState object
     */
    public static final HashMap<String, BlockState>                            BLOCK_STATES_REGISTRY        = new HashMap<String, BlockState>();

    /**
     * Maps String representations of block states values to their respective IBlockStateValue instance.
     */
    public static final HashMap<BlockState, HashMap<String, IBlockStateValue>> BLOCK_STATES_VALUES_REGISTRY = new HashMap<BlockState, HashMap<String, IBlockStateValue>>();

    /**
     * Registers all blocks states of the game
     */
    public static void init()
    {
        registerState(logOrientation = new BaseBlockState("orientation"));
        for(EnumLogBlockStates state : EnumLogBlockStates.values())
        {
            registerValue(logOrientation, state);
        }

        registerState(cableConnexions = new BaseBlockState("connexions"));
        for(EnumConnexionStates state : EnumConnexionStates.values())
        {
            registerValue(cableConnexions, state);
        }

        registerState(electricPower = new BaseBlockState("electricPower"));
        for(EnumPowerStates state : EnumPowerStates.values())
        {
            registerValue(electricPower, state);
        }
    }

    /**
     * Registers a block state into the BLOCK_STATE_REGISTRY field
     */
    public static void registerState(BlockState state)
    {
        if(BLOCK_STATES_REGISTRY.containsKey(state.toString()))
        {
            throw new IllegalArgumentException("Id " + state.toString() + " is already used by " + BLOCK_STATES_REGISTRY.get(state.toString()) + " when trying to add " + state);
        }
        BLOCK_STATES_REGISTRY.put(state.toString(), state);
        BLOCK_STATES_VALUES_REGISTRY.put(state, new HashMap<String, IBlockStateValue>());
    }

    /**
     * Returns the block state in BLOCK_STATE_REGISTRY with given id
     */
    public static BlockState getState(String string)
    {
        return BLOCK_STATES_REGISTRY.get(string);
    }

    /**
     * Registers a block state into the BLOCK_STATE_REGISTRY field
     */
    public static void registerValue(BlockState state, IBlockStateValue value)
    {
        if(BLOCK_STATES_VALUES_REGISTRY.containsKey(value.toString()))
        {
            throw new IllegalArgumentException("Id " + value.toString() + " is already used by " + BLOCK_STATES_VALUES_REGISTRY.get(value.toString()) + " when trying to add " + value);
        }
        HashMap<String, IBlockStateValue> map = BLOCK_STATES_VALUES_REGISTRY.get(state);
        map.put(value.toString(), value);
    }

    /**
     * Returns the block state in BLOCK_STATE_REGISTRY with given id
     */
    public static IBlockStateValue getValue(BlockState state, String string)
    {
        return BLOCK_STATES_VALUES_REGISTRY.get(state).get(string);
    }

}
