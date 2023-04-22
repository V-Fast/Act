package com.lumaa.act.ai;

import com.lumaa.act.entity.ActorEntity;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

public class Pathfinder {
    private ActorAI ai;
    private ActorEntity actor;
    private ActorMovement movement;
    private ActorAction action;
    public ArrayList<Path> paths = new ArrayList<>();
    public World world;
    public BlockPos origin;
    public BlockPos destination;

    public Pathfinder(ActorAI ai) {
        this.ai = ai;
        this.actor = ai.actor;
        this.movement = ai.movement;
        this.action = ai.action;
    }

    public void setPositions(BlockPos origin, BlockPos destination) {
        this.origin = origin;
        this.destination = destination;
    }

    public void execute(boolean move) {

    }

    private void discover() {

    }

    private boolean walkable(BlockPos pos) {
        BlockState block = world.getBlockState(pos);
        BlockState block2 = world.getBlockState(pos.add(0, 1, 0));
        return block.isAir() || block2.isAir();
    }
}
