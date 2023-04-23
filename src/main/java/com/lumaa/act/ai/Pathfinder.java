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
    private boolean positionsSet = false;
    public Path path;
    public World world;
    public BlockPos origin;
    public BlockPos destination;

    public Pathfinder(ActorAI ai) {
        this.ai = ai;
        this.actor = ai.actor;
        this.movement = ai.movement;
        this.action = ai.action;
        this.world = ai.actor.world;
    }

    public void setPositions(BlockPos origin, BlockPos destination) {
        this.origin = origin;
        this.destination = destination;
        this.path = new Path(this);
        this.positionsSet = true;
    }

    public void execute() {
        if (positionsSet) {
            this.path.tick();
        }
    }
}
