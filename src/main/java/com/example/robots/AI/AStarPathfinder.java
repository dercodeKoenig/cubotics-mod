package com.example.robots.AI;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEnchantmentTable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.*;

import static java.lang.System.out;

public class AStarPathfinder {
    private PriorityQueue<Node> openSet;
    private Set<BlockPos> closedSet;
    private Map<BlockPos, BlockPos> cameFrom;
    private Map<BlockPos, Integer> gScore;
    private Map<BlockPos, Double> fScore;
    private BlockPos start;
    private BlockPos goal;
    private int max_r;
    private World world;
    private boolean pathFound;
    private int n;

    public AStarPathfinder(int max_r, World world) {
        this.start = new BlockPos(0,0,0);;
        this.goal = new BlockPos(0,0,0);
        this.max_r = max_r;
        this.world = world;
        reset();
    }

    public void reset(){
        n=0;
        this.pathFound = false;
        this.openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> fScore.getOrDefault(n.pos, Double.MAX_VALUE)));
        this.closedSet = new HashSet<>();
        this.cameFrom = new HashMap<>();
        this.gScore = new HashMap<>();
        this.fScore = new HashMap<>();

        this.gScore.put(start, 0);
        this.fScore.put(start, heuristic(start, goal));
        this.openSet.add(new Node(start));
    }
    public void find(BlockPos start, BlockPos goal) {

        if (start.equals(this.start) && goal.equals(this.goal)) {
            return;
        }
        this.start = start;
        this.goal = goal;
        reset();
    }

    public boolean update() {
        if (openSet.isEmpty()) {
            reset();
            return true; // No path found
        }

        Node current = openSet.poll();
        if (current.pos.equals(goal)) {
            pathFound = true;
            return true; // Path found
        }

        closedSet.add(current.pos);

        for (BlockPos neighbor : getNeighbors(current.pos)) {
            if (closedSet.contains(neighbor) || !isValidBlockPos(neighbor) || !checkAirPath(current.pos, neighbor)) {
                continue;
            }
            n+=1;
            if (n > 20*500*5){ // 5 seconds search max
                reset();
                out.println("give up path search");
                return true; // give up
            }

            int tentativeGScore = gScore.getOrDefault(current.pos, Integer.MAX_VALUE) + 1;

            if (!openSet.contains(new Node(neighbor))) {
                openSet.add(new Node(neighbor));
            } else if (tentativeGScore >= gScore.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                continue;
            }

            cameFrom.put(neighbor, current.pos);
            gScore.put(neighbor, tentativeGScore);
            fScore.put(neighbor, tentativeGScore + heuristic(neighbor, goal));
        }

        return false; // Pathfinding still in progress
    }

    private List<BlockPos> optimizedPath(List<BlockPos> path){
        List<BlockPos> optimizedPath = new ArrayList<>();
        for (int i = 0; i < path.size(); i++) {
            optimizedPath.add(path.get(i));
            for (int j = i + 2; j < path.size(); j++) {
                BlockPos p1 = path.get(i);
                BlockPos p2 = path.get(j);
                int d = Math.abs(p1.getX() - p2.getX()) + Math.abs(p1.getY() - p2.getY()) + Math.abs(p1.getZ() - p2.getZ());
                if (d == 1) {
                    i = j-1;
                    break;
                }
            }
        }
        return optimizedPath;
    }
    public List<BlockPos> getPath() {
        if (!pathFound) {
            return Collections.emptyList();
        }

        List<BlockPos> path = new LinkedList<>();
        BlockPos current = goal;
        while (cameFrom.containsKey(current)) {
            path.add(0, current);
            current = cameFrom.get(current);
        }
        path.add(0, start);
        return optimizedPath(path);
    }

    // an air path is only valid if it is the shortest move towards the goal
    private boolean checkAirPath(BlockPos from, BlockPos to) {

        double l0 = to.distanceSq(goal);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (!isValidBlockPos(to.add(new Vec3i(x, y, z)))) {
                        return true;
                    }
                }
            }
        }

        double l1 = from.add(new Vec3i(1, 0, 0)).distanceSq(goal) + 0.000001;
        double l2 = from.add(new Vec3i(-1, 0, 0)).distanceSq(goal) + 0.000001;
        double l3 = from.add(new Vec3i(0, 1, 0)).distanceSq(goal) + 0.000001;
        double l4 = from.add(new Vec3i(0, -1, 0)).distanceSq(goal) + 0.000001;
        double l5 = from.add(new Vec3i(0, 0, 1)).distanceSq(goal) + 0.000001;
        double l6 = from.add(new Vec3i(0, 0, -1)).distanceSq(goal) + 0.000001;

        if (l1 < l0 || l2 < l0 || l3 < l0 || l4 < l0 || l5 < l0 || l6 < l0)
            return false;

        return true;
    }

    private double heuristic(BlockPos a, BlockPos b) {
        return Math.sqrt(a.distanceSq(b));
    }

    private List<BlockPos> getNeighbors(BlockPos pos) {
        List<BlockPos> neighbors = new ArrayList<>();
        neighbors.add(pos.add(1, 0, 0));
        neighbors.add(pos.add(-1, 0, 0));
        neighbors.add(pos.add(0, 1, 0));
        neighbors.add(pos.add(0, -1, 0));
        neighbors.add(pos.add(0, 0, 1));
        neighbors.add(pos.add(0, 0, -1));
        return neighbors;
    }

    private boolean isValidBlockPos(BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        Material material = block.getMaterial(world.getBlockState(pos));

        // Check if the block is air
        if (material == Material.AIR) {
            return true;
        }

        // Check if the block is a liquid (like water or lava)
        if (material.isLiquid()) {
            return true;
        }

        // Check if the block is a plant (like grass, flowers, saplings, or mushrooms)
        if (material == Material.PLANTS || material == Material.VINE || material == Material.CACTUS || material == Material.GOURD || material == Material.CARPET) {
            return true;
        }

        // Add more checks here as needed

        return false;
    }


    private static class Node {
        BlockPos pos;

        Node(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return Objects.equals(pos, node.pos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos);
        }
    }

    public static List<BlockPos> STATICgetNeighbors(BlockPos pos) {
        List<BlockPos> neighbors = new ArrayList<>();
        neighbors.add(pos.add(1, 0, 0));
        neighbors.add(pos.add(-1, 0, 0));
        neighbors.add(pos.add(0, 1, 0));
        neighbors.add(pos.add(0, -1, 0));
        neighbors.add(pos.add(0, 0, 1));
        neighbors.add(pos.add(0, 0, -1));
        return neighbors;
    }
}
