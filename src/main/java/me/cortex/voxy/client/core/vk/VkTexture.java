package me.cortex.voxy.client.core.vk;

import me.cortex.voxy.common.util.TrackedObject;

public class VkTexture extends TrackedObject {
    public final long id;
    private final int type;
    private int format;
    private int width;
    private int height;
    private int levels;
    private boolean hasAllocated;

    private static int COUNT;
    private static long ESTIMATED_TOTAL_SIZE;

    public VkTexture() {
        this(2 /* VK_IMAGE_TYPE_2D */);
    }

    public VkTexture(int type) {
        this.type = type;
        // Placeholder for vkCreateImage
        this.id = 1;
        COUNT++;
    }

    private VkTexture(int type, boolean useGenTypes) {
        this.type = type;
        this.id = 1;
        COUNT++;
    }

    public VkTexture store(int format, int levels, int width, int height) {
        if (this.hasAllocated) {
            throw new IllegalStateException("Texture already allocated");
        }
        this.hasAllocated = true;
        this.format = format;
        this.width = width;
        this.height = height;
        this.levels = levels;
        ESTIMATED_TOTAL_SIZE += this.getEstimatedSize();
        return this;
    }

    public VkTexture createView() {
        this.assertAllocated();
        return new VkTexture(this.type, true);
    }

    @Override
    public void free() {
        if (this.hasAllocated) {
            ESTIMATED_TOTAL_SIZE -= this.getEstimatedSize();
        }
        COUNT--;
        this.hasAllocated = false;
        super.free0();
        // Placeholder for vkDestroyImage
    }

    public VkTexture name(String name) {
        this.assertAllocated();
        return this; // Placeholder for Vulkan Debug Marker
    }

    public int getWidth() {
        this.assertAllocated();
        return this.width;
    }

    public int getHeight() {
        this.assertAllocated();
        return this.height;
    }

    public int getLevels() {
        this.assertAllocated();
        return this.levels;
    }

    public int getFormat() {
        this.assertAllocated();
        return this.format;
    }

    public int getPixelTransferFormat() {
        this.assertAllocated();
        return 0; // Needs proper mapping to Vulkan formats
    }

    private long getEstimatedSize() {
        this.assertAllocated();
        long elemSize = 4; // Simplified
        long size = 0;
        for (int lvl = 0; lvl < this.levels; lvl++) {
            size += Math.max((((long)this.width)>>lvl), 1) * Math.max((((long)this.height)>>lvl), 1) * elemSize;
        }
        return size;
    }

    public void assertAllocated() {
        if (!this.hasAllocated) {
            throw new IllegalStateException("Texture not yet allocated");
        }
    }

    public VkTexture zero() {
        this.assertAllocated();
        // Placeholder for vkCmdClearColorImage
        return this;
    }

    public static int getCount() {
        return COUNT;
    }

    public static long getEstimatedTotalSize() {
        return ESTIMATED_TOTAL_SIZE;
    }
}
