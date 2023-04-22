package com.lumaa.act.ai;

import com.lumaa.act.ActMod;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Random;

public class Path {
    private Pathfinder pathfinder;
    private BlockPos origin;
    private BlockPos destination;
    private int iterations = 0;

    private BlockPos lastStep;
    private BlockPos currentStep;
    private ArrayList<BlockPos> steps = new ArrayList<>();

    private PathDirection lastDir;
    private PathDirection currentDir;


    private final int maxIterations = 500;

    public PathDirection startDir;

    public Path(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
        this.origin = pathfinder.origin;
        this.destination = pathfinder.destination;
        this.lastStep = null;
        this.lastDir = null;
    }

    public void tick() {

    }

    private void nextStep() {
        this.lastStep = this.currentStep;
        if (origin.getY() != destination.getY()) {
            BlockPos step = origin.add(0, destination.getY() > origin.getY() ? 1 : -1, 0);
            if (walkable(step)) {
                this.currentStep = step;
                this.steps.add(step);
                this.iterations += 1;
            }
        } else {
            if (this.lastDir != null) {
                this.currentDir = this.findRandomDirection(this.lastDir);
                switch (this.currentDir) {
                    case NORTH -> {
                        int x = this.destination.getX() - this.origin.getX();
                        int z = this.destination.getZ() - this.origin.getZ();
                    }
                }
            }
        }
    }

    private PathDirection findRandomDirection(@Nullable PathDirection lastDir) {
        if (this.getPathfinder().paths.size() < 1) {
            PathDirection[] l = PathDirection.values();
            PathDirection i =  l[new Random().nextInt(l.length)];
            if (this.startDir == null) this.startDir = i;
            return i;
        } else {
            ArrayList<PathDirection> unusedDirections = new ArrayList<>();
            unusedDirections.add(PathDirection.NORTH);
            unusedDirections.add(PathDirection.SOUTH);
            unusedDirections.add(PathDirection.EAST);
            unusedDirections.add(PathDirection.WEST);

            if (lastDir != null) unusedDirections.remove(lastDir);

            this.getPathfinder().paths.forEach(path -> {
                if (unusedDirections.contains(path.startDir)) {
                    unusedDirections.remove(path.startDir);
                } else {
                    ActMod.print("Anomaly paths");
                }
            });

            if (unusedDirections.size() > 0) {
                PathDirection l = unusedDirections.get(unusedDirections.size());
                if (this.startDir == null) this.startDir = l;
                return l;
            } else {
                this.stopPath();
                return null;
            }
        }
    }

    private PathDirection findDirection() {

    }

    private boolean walkable(BlockPos pos) {
        BlockState block = this.getPathfinder().world.getBlockState(pos);
        BlockState block2 = this.getPathfinder().world.getBlockState(pos.add(0, 1, 0));
        return block.isAir() || block2.isAir();
    }

    private void stopPath() {
        this.getPathfinder().paths.add(this);
    }

    private void updateIterations() {
        this.iterations = this.steps.size();
    }

    public int getIterations() {
        return iterations;
    }

    public Pathfinder getPathfinder() {
        return pathfinder;
    }

    public enum PathDirection {
        NORTH,
        SOUTH,
        EAST,
        WEST
    }
}
