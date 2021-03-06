package platformer.world;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import platformer.GameUtil;
import platformer.MainClient;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class WorldSegment {
    public static final int BASE_BLOCK_HEIGHT = 100;
    public static final int BLOCK_DRAW_HEIGHT = 400;
    // amount of segments per block
    public static final int BLOCKS_PER_SEGMENT = 20;
    // size of each block in segment
    public static final int TERRAIN_BLOCK_SIZE = 100;
    // size of each segment
    public static final int WORLD_SEGMENT_SIZE = BLOCKS_PER_SEGMENT * TERRAIN_BLOCK_SIZE;
    public static final int HEIGHT_STEP = 20;
    private final World world;
    private Block[] terrainBlocks = new Block[BLOCKS_PER_SEGMENT];
    private int terrainSegmentIndex;
    private boolean hidden;
    Set<WorldObj> objects = new HashSet<>();

    public WorldSegment(World world, int terrainSegmentIndex) {
        this.world = world;
        this.terrainSegmentIndex = terrainSegmentIndex;

        terrainBlocks[0] = new Block(0,
                terrainSegmentIndex > 0 ?
                        world.getSegmentAt(terrainSegmentIndex - 1).terrainBlocks[terrainBlocks.length - 1].height :
                        terrainSegmentIndex < 0 ?
                                world.getSegmentAt(terrainSegmentIndex + 1).terrainBlocks[0].height :
                                BASE_BLOCK_HEIGHT
        );

        for (int i = 1; i < terrainBlocks.length; i++) {
            int relativeHeight = terrainSegmentIndex % 3 == 0 ? 1 : -1;
//            // next is random number from 0 to 15
//            int next = terrainSegmentIndex < 5 ? 1 : (int) (world.getRandom().nextDouble() * 15);
//
//            // 50% chance of staying at the same elevation; 25% of going up, 25% of going down (by HEIGHT_STEP).
//            if (next > 11) { // 12,13,14,15 (4 values)
//                relativeHeight = HEIGHT_STEP;
//            } else if (next < 4) { // 0,1,2,3 (4 values)
//                relativeHeight = -HEIGHT_STEP;
//            } else { // 4,5,6,7,8,9,10,11 (8 values)
//                relativeHeight = 0;
//            }
            terrainBlocks[i] = new Block(i, terrainBlocks[i - 1].height + relativeHeight);
        }

        hidden = true; // blocks should be invisible by default
    }

    public void updateObjects() {
        for (WorldObj obj : objects) {
            obj.update();

            WorldSegment currentSeg = world.getSegmentAt(obj.getLocation());
            if (currentSeg != this)
                world.transferredObject.add(new World.Tuple(obj, this, currentSeg));
        }
    }

    public void updateShapes() {
        Platform.runLater(() -> {
            showSegment();
            Location screenLocation = MainClient.getScreenLocation();
            for (Block block : terrainBlocks) {
                GameUtil.setRelativeTo(block.rectangle, screenLocation, getLeftPosX() - getLocalOffset(block.id), block.height);
                if (block.tree != null) {
                    GameUtil.setRelativeTo(block.tree.trunk, screenLocation, getLeftPosX() - getLocalOffset(block.id), block.height + block.tree.getTrunkHeight());
                    GameUtil.setRelativeTo(block.tree.leaves, screenLocation, getLeftPosX() - getLocalOffset(block.id) + block.tree.getLeavesWidth() / 2, block.height + block.tree.getHeight());
                }
                if (block.house != null) {
                    block.house.imageView.setX(screenLocation.getX() - getLeftPosX() - getLocalOffset(block.id));
                    block.house.imageView.setY(screenLocation.getY() - block.height - block.house.getHeight() + 285);
                }
            }

            for (WorldObj obj : objects) {
                obj.updateDraw();
            }
        });
    }

    public Collection<WorldObj> getObjects() {
        return objects;
    }

    public int getTerrainSegmentIndex() {
        return terrainSegmentIndex;
    }

    public int getLeftPosX() {
        return terrainSegmentIndex * WORLD_SEGMENT_SIZE;
    }

    public int getRightPosX() {
        return (terrainSegmentIndex + 1) * WORLD_SEGMENT_SIZE - 1;
    }

    // localPosX is the pos local to this segment; 0 represents the left corner of this segment, not the left corner of the world
    public double getTerrainHeightAtLocalPos(double localPosX) {
        return getBlockAtLocalPos(localPosX).height;
    }

    public double getTerrainWidthAtLocalPos(double localPosX) {
        return getBlockAtLocalPos(localPosX).width;
    }

    public Block getBlockAtLocalPos(double localPosX) {
        int blockIndex = (int) (Math.abs(localPosX) / TERRAIN_BLOCK_SIZE);
        if (blockIndex >= terrainBlocks.length)
            throw new IllegalArgumentException("Block index \"" + blockIndex + "\" is out of bounds; localPosX should be relative to left corner of segment");
        return terrainBlocks[blockIndex];
    }

    public void showSegment() {
        if (!hidden)
            return;

        for (Block block : terrainBlocks) {
            MainClient.root.getChildren().add(block.rectangle);
            if (block.tree != null) {
                MainClient.root.getChildren().add(block.tree.trunk);
                MainClient.root.getChildren().add(block.tree.leaves);
            }
            if (block.house != null) {
                MainClient.root.getChildren().add(block.house.imageView);
            }
        }

        for (WorldObj obj : getObjects()) {
            MainClient.root.getChildren().add(obj.getShape());
        }

        hidden = false;
    }

    public static int getSegmentAt(double x) {
        return (int) x / WORLD_SEGMENT_SIZE;
    }

    public static int getCurrentSegment() {
        return getSegmentAt(MainClient.getScreenLocation().getX());
    }

    private int getLocalOffset(int index) {
        return index * TERRAIN_BLOCK_SIZE;
    }

    @Override
    public String toString() {
        return "[" + terrainSegmentIndex + "]";
    }

    public class Block {
        private Rectangle rectangle;
        private int height;
        private int width;
        private int id;
        private Tree tree;
        private House house;

        // todo - work with negative segments
        public Block(int id, int height) {
            this.id = id;
            this.width = TERRAIN_BLOCK_SIZE;
            if (height < 1)
                height = 1;
            this.height = height;
            this.rectangle = new Rectangle(TERRAIN_BLOCK_SIZE, height + BLOCK_DRAW_HEIGHT, Color.GREENYELLOW);
            if (id % 13 == 0)
                tree = new Tree(this);
            if (id % 37 == 0)
                house = new House();
        }

        double getLeftBlockPosX() {
            return getLeftPosX() + id * TERRAIN_BLOCK_SIZE;
        }

        double getRightBlockPosX() {
            return getRightPosX() + id * TERRAIN_BLOCK_SIZE;
        }

        public Rectangle getRectangle() {
            return rectangle;
        }
    }

    private static class Tree {
        private Rectangle trunk, leaves;

        public Tree(Block block) {
            trunk = new Rectangle(20, 100, Color.BROWN);
            trunk.setOpacity(0.5);
            leaves = new Rectangle(80, 40, Color.GREEN);
            leaves.setOpacity(0.5);
        }

        public double getHeight() {
            return trunk.getHeight() + leaves.getHeight();
        }

        public double getLeavesWidth() {
            return leaves.getWidth();
        }

        public double getTrunkHeight() {
            return trunk.getHeight();
        }
    }

    private static class House {
        private ImageView imageView = new ImageView(MainClient.HOUSE_IMAGE);

        {
            imageView.setOpacity(Math.random() * 0.3 + 0.4);
            double scale = Math.random() * 0.25 + 0.15;
            imageView.setScaleX(scale);
            imageView.setScaleY(scale);
        }

        public double getHeight() {
            return MainClient.HOUSE_IMAGE.getHeight();
        }
    }
}
