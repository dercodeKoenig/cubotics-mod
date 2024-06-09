package com.example.robots;

import com.example.robots.AI.AIBlockScan;
import com.example.robots.AI.AStarPathfinder;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.lang.System.out;

public class EntityRobot extends Entity {

    private BlockPos homeLocation;
    private Block homeBlock;

    private double yFlightOffset = 0.25;
    double dockingOffsetY = 0.1;

    private int blockScansPerTick = 2500;
    private AIBlockScan homeBlockScanner;

    private AStarPathfinder pathfinder;
    private int pathChecksPerTick = 500;
    private List<BlockPos> current_path;

    private double speed = 0.05 * 3;

    private Vec3d serverPos;
    float serveryaw;
    float serverpitch;

    List<BlockPos>pathFailCache;

    public EntityRobot(World worldIn) {
        super(worldIn);

        homeBlock = Blocks.CARPET;
        homeBlockScanner = new AIBlockScan(worldIn);

        pathfinder = new AStarPathfinder(64, worldIn);
        homeLocation = new BlockPos(-1,-1,-1);
        pathfinder.find(homeLocation,homeLocation);
        pathfinder.update();
        current_path = pathfinder.getPath(); // should be empty
        pathFailCache = new LinkedList<>();


        // for smooth client update
        this.serverPos = new Vec3d(this.posX, this.posY, this.posZ);
        serveryaw = 0;
        serverpitch = 0;
    }

    @Override
    protected void entityInit() {
        setSize(0.35F, 0.35F);
    }

    @Override
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        double halfWidth = width / 2;
        this.setEntityBoundingBox(new AxisAlignedBB(this.posX - halfWidth, this.posY, this.posZ - halfWidth, this.posX + halfWidth, this.posY + height, this.posZ + halfWidth));
    }

    BlockPos getDoublePosition(){
        return new BlockPos(Math.round(this.posX-0.5), Math.round(this.posY), Math.round(this.posZ-0.5));
    }

    boolean programFindPath(BlockPos target) {
        long startTime = System.nanoTime();

        pathfinder.find(this.getDoublePosition(), target);
        out.println(this.getUniqueID()+" looking for path to "+target.getX()+" "+target.getY()+" "+target.getZ());


        //if a neighbor block was failed to be reached earlier
        for (BlockPos i: pathFailCache){
            for (BlockPos p: AStarPathfinder.STATICgetNeighbors(i)){
                if(p.equals(target)){
                    // if a neighbor of the target was failed to be reached
                    // it is likely that this position will fail too
                    pathFailCache.add(p);
                    this.current_path = Collections.emptyList();
                    out.println("this path will be skipped because it is likely a not reachable target");
                    return true;
                }
            }
        }

        for (int i = 0; i < pathChecksPerTick; i++) {
            if (pathfinder.update()) {
                this.current_path = pathfinder.getPath();
                out.println("path calculation finished - "+this.current_path.size());
                if (this.current_path.isEmpty()) {
                    pathFailCache.add(target);
                    out.println("failed to reach target. adding to failCache");
                }
                return true;
            }
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);  // compute the duration in nanoseconds

        System.out.println("Execution time in nanoseconds: " + duration);
        System.out.println("Execution time in milliseconds: " + duration / 1000000);


        return false;
    }

    void idleDown(){
        this.move(MoverType.SELF,0,-0.05,0);
        this.rotationPitch = 0;
    }

    void programFindNewHome() {
        this.idleDown();

        if (!homeBlockScanner.hasNext()) {
            homeBlockScanner.reset(getDoublePosition());
            this.pathFailCache.clear();
        }
        BlockPos target = homeBlockScanner.getCurrent();
        if (world.getBlockState(target).getBlock() == homeBlock) {
            boolean done = programFindPath(target);
            // new home found ?
            if (done) {
                if (!current_path.isEmpty()) {
                    homeLocation = target;
                    homeBlockScanner.reset(homeLocation);
                } else {
                    homeBlockScanner.next();
                }
            }
        } else {
            //out.println(this.getUniqueID()+" scanning blocks in radius "+homeBlockScanner.searchRadius);
            //long startTime = System.nanoTime();

            for (int i = 0; i < blockScansPerTick; i++) {
                if (world.getBlockState(homeBlockScanner.next()).getBlock() == homeBlock) {
                    //out.println("found a home block");
                    break;
                }
            }

            //long endTime = System.nanoTime();
            //long duration = (endTime - startTime);  // compute the duration in nanoseconds

            //System.out.println("Execution time in nanoseconds: " + duration);
            //System.out.println("Execution time in milliseconds: " + duration / 1000000);

        }
    }

    void programMoveHome(){
        if (world.getBlockState(homeLocation).getBlock() != homeBlock) {
            programFindNewHome();
        }
        else{
            double distance = new Vec3d(this.posX-0.5, this.posY-dockingOffsetY, this.posZ-0.5).distanceTo(new Vec3d(homeLocation.getX(), homeLocation.getY(),homeLocation.getZ()));

            if (distance < 0.1){
                this.setPosition(homeLocation.getX()+0.5, homeLocation.getY()+dockingOffsetY, homeLocation.getZ()+0.5);
                this.rotationPitch = 0;
            }
            else if (distance < 1 && posY >= homeLocation.getY()+dockingOffsetY){
                moveTo(homeLocation.getX(), homeLocation.getY()+dockingOffsetY, homeLocation.getZ(), false);
            }
            else if (!programMoveToBlock(homeLocation, yFlightOffset)){
                homeLocation = getPosition();
            }
        }
    }

    boolean moveTo(double X, double Y, double Z, boolean rotateX) {
        X += 0.5;
        Z += 0.5;
        double dx = X - posX;
        double dy = Y - posY;
        double dz = Z - posZ;
        Vec3d vec = new Vec3d(dx, dy, dz);
        if (vec.lengthVector() > this.speed)
            vec = vec.normalize().scale(this.speed);

        Vec3d lastposition = new Vec3d(posX,posY,posZ);
        this.move(MoverType.SELF, vec.x, vec.y, vec.z);
        Vec3d newposition = new Vec3d(posX,posY,posZ);
        if (lastposition.distanceTo(newposition) < 0.001) {
            this.move(MoverType.SELF, -vec.x*2, -vec.y*2, -vec.z*2); // move a little back
            this.current_path.clear(); //we are colliding... recalculate path
            return false;
        }
        if (Math.abs(vec.x)+Math.abs(vec.z)>0.001) {
            double horizontalRotation = Math.atan2(-vec.x, vec.z) * (180.0 / Math.PI);
            this.rotationYaw = (float) horizontalRotation;
        }
        if (rotateX) {
            double verticalRotation = -Math.asin(vec.y / Math.sqrt(vec.x * vec.x + vec.y * vec.y + vec.z * vec.z)) * (180.0 / Math.PI);
            this.rotationPitch = (float) verticalRotation;
        } else
            this.rotationPitch = 0;

        return true;
    }

    boolean programMoveToBlock(BlockPos pos, double Yoffset) {
        //check if no path exists
        if (current_path.isEmpty() || !this.current_path.get(this.current_path.size() - 1).equals(pos)) {
            if (this.getDoublePosition().equals(pos)) {
                moveTo(pos.getX(), pos.getY() + Yoffset, pos.getZ(), true);
                return true; // destination reached
            }
            if (programFindPath(pos)) {
                if (this.current_path.isEmpty()) {
                    return false; // no path found
                }
            }
        } else {
            BlockPos first_position = this.current_path.get(0);
            moveTo(first_position.getX(), first_position.getY() + Yoffset, first_position.getZ(), true);
            if (first_position.distanceSq(this.getDoublePosition()) < 0.25 * 0.25) {
                this.current_path.remove(first_position);
            }
        }
        return true;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote) {
            programMoveHome();

        }else{
            this.interpolatePosition();
        }
    }
    private void interpolatePosition() {

        float speed = 0.1F; // about 5 ticks behind server

        Vec3d mypos = new Vec3d(posX, posY, posZ);
        Vec3d interpolatedPos = mypos.add(serverPos.subtract(mypos).scale(speed));
        this.setPosition(interpolatedPos.x, interpolatedPos.y, interpolatedPos.z);

        this.rotationYaw = (float) (this.rotationYaw+(serveryaw-this.rotationYaw)*speed*5);
        this.rotationPitch = (float) (this.rotationPitch+(serverpitch-this.rotationPitch)*speed*5);


        //for home calculation
        double distance = new Vec3d(this.posX-0.5, this.posY-dockingOffsetY, this.posZ-0.5).distanceTo(new Vec3d(homeLocation.getX(), homeLocation.getY(),homeLocation.getZ()));
        if (distance < 0.1){
            this.setPosition(homeLocation.getX()+0.5, homeLocation.getY()+dockingOffsetY, homeLocation.getZ()+0.5);
            this.rotationPitch = 0;
        }
    }
    @Override
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        this.serverPos = new Vec3d(x, y, z);
        this.serveryaw = yaw;
        this.serverpitch = pitch;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        int hx = nbt.getInteger("HomeX");
        int hy = nbt.getInteger("HomeY");
        int hz = nbt.getInteger("HomeZ");

        this.homeLocation = new BlockPos(hx,hy,hz);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setInteger("HomeX", homeLocation.getX());
        nbt.setInteger("HomeY", homeLocation.getY());
        nbt.setInteger("HomeZ", homeLocation.getZ());
    }

    // Other necessary overrides...
}
