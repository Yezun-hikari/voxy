package me.cortex.voxy.client.core.vk;

import me.cortex.voxy.common.util.TrackedObject;

public class VkTexture extends TrackedObject {
    public long id;
    public long memoryId;
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
        COUNT++;
    }

    private VkTexture(int type, boolean useGenTypes) {
        this.type = type;
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

        if (VoxyVulkanContext.MOCK_VULKAN) {
            this.id = 1; // Dummy ID
        } else {
            try (org.lwjgl.system.MemoryStack stack = org.lwjgl.system.MemoryStack.stackPush()) {
                org.lwjgl.vulkan.VkDevice device = VoxyVulkanContext.getInstance().getDevice();

                org.lwjgl.vulkan.VkImageCreateInfo imageInfo = org.lwjgl.vulkan.VkImageCreateInfo.calloc(stack)
                    .sType(org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
                    .imageType(this.type)
                    .extent(it -> it.width(this.width).height(this.height).depth(1))
                    .mipLevels(this.levels)
                    .arrayLayers(1)
                    .format(this.format)
                    .tiling(org.lwjgl.vulkan.VK10.VK_IMAGE_TILING_OPTIMAL)
                    .initialLayout(org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED)
                    .usage(org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT | org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT)
                    .samples(org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT)
                    .sharingMode(org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE);

                java.nio.LongBuffer pImage = stack.mallocLong(1);
                if (org.lwjgl.vulkan.VK10.vkCreateImage(device, imageInfo, null, pImage) != org.lwjgl.vulkan.VK10.VK_SUCCESS) {
                    throw new RuntimeException("Failed to create image");
                }
                this.id = pImage.get(0);

                org.lwjgl.vulkan.VkMemoryRequirements memRequirements = org.lwjgl.vulkan.VkMemoryRequirements.calloc(stack);
                org.lwjgl.vulkan.VK10.vkGetImageMemoryRequirements(device, this.id, memRequirements);

                org.lwjgl.vulkan.VkMemoryAllocateInfo allocInfo = org.lwjgl.vulkan.VkMemoryAllocateInfo.calloc(stack)
                    .sType(org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                    .allocationSize(memRequirements.size())
                    .memoryTypeIndex(0); // Placeholder for proper memory type lookup

                java.nio.LongBuffer pMemory = stack.mallocLong(1);
                if (org.lwjgl.vulkan.VK10.vkAllocateMemory(device, allocInfo, null, pMemory) != org.lwjgl.vulkan.VK10.VK_SUCCESS) {
                    throw new RuntimeException("Failed to allocate image memory");
                }
                this.memoryId = pMemory.get(0);

                org.lwjgl.vulkan.VK10.vkBindImageMemory(device, this.id, this.memoryId, 0);
            }
        }
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
        if (!VoxyVulkanContext.MOCK_VULKAN) {
            org.lwjgl.vulkan.VkDevice device = VoxyVulkanContext.getInstance().getDevice();
            org.lwjgl.vulkan.VK10.vkDestroyImage(device, this.id, null);
            org.lwjgl.vulkan.VK10.vkFreeMemory(device, this.memoryId, null);
        }
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
