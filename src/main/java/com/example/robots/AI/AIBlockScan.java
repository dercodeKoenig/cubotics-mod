package com.example.robots.AI;

import java.util.Iterator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AIBlockScan {

    public int searchRadius;
    private int searchX;
    private int searchY;
    private int searchZ;
    private BlockPos startpos;

private World world;
    public AIBlockScan(World world) {
        searchRadius = 99999;
        this.world = world;
    }


    public boolean hasNext() {
        return searchRadius < 64;
    }

    public BlockPos getCurrent(){
        return new BlockPos(searchX, searchY, searchZ).add(startpos);
    }

    public BlockPos next() {
        // Step through each block in a hollow cube of size (searchRadius * 2 -1), if done
        // add 1 to the radius and start over.


        // Step to the next Y
        if (Math.abs(searchX) == searchRadius || Math.abs(searchZ) == searchRadius) {
            searchY += 1;
        } else {
            searchY += searchRadius * 2;
        }

        if (searchY > searchRadius ||searchY > world.getHeight(startpos).add(new BlockPos(searchX,searchY,searchZ)).getY()) {
            // Step to the next Z
            searchY = -searchRadius;
            searchZ += 1;

            if (searchZ > searchRadius) {
                // Step to the next X
                searchZ = -searchRadius;
                searchX += 1;

                if (searchX > searchRadius) {
                    // Step to the next radius
                    searchRadius += 1;
                    searchX = -searchRadius;
                    searchY = -searchRadius;
                    searchZ = -searchRadius;
                }
            }
        }

        return new BlockPos(searchX, searchY, searchZ).add(startpos);
    }


    public void reset(BlockPos p) {
        searchRadius = 0;
        searchX = -searchRadius;
        searchY = -searchRadius;
        searchZ = -searchRadius;
        startpos = p;
    }

}