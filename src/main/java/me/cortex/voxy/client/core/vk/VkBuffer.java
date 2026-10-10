package me.cortex.voxy.client.core.vk;

import me.cortex.voxy.common.util.MemoryBuffer;
import me.cortex.voxy.common.util.TrackedObject;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.vulkan.VK10.*;

public class VkBuffer extends TrackedObject {
    public final long id;
    public long memoryId; // Store memory allocation id
    private final long size;
    private final int flags;

    private static int COUNT;
    private static long TOTAL_SIZE;

    public VkBuffer(long size) {
        this(size, 0);
    }

    public VkBuffer(MemoryBuffer buffer) {
        this(buffer.size, 0, false, buffer.address);
    }

    public VkBuffer(long size, boolean zero) {
        this(size, 0, zero);
    }

    public VkBuffer(long size, int flags) {
        this(size, flags, true);
    }

    public VkBuffer(long size, int flags, boolean zero) {
        this(size, flags, zero, 0);
    }

    private VkBuffer(long size, int flags, boolean zero, long data) {
        this.flags = flags;
        this.size = size;

        if (VoxyVulkanContext.MOCK_VULKAN) {
            this.id = MemoryUtil.memAddress(MemoryUtil.memAlloc(1));
        } else {
            try (org.lwjgl.system.MemoryStack stack = org.lwjgl.system.MemoryStack.stackPush()) {
                org.lwjgl.vulkan.VkDevice device = VoxyVulkanContext.getInstance().getDevice();

                org.lwjgl.vulkan.VkBufferCreateInfo bufferInfo = org.lwjgl.vulkan.VkBufferCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                    .size(size)
                    .usage(flags)
                    .sharingMode(VK_SHARING_MODE_EXCLUSIVE);

                java.nio.LongBuffer pBuffer = stack.mallocLong(1);
                if (vkCreateBuffer(device, bufferInfo, null, pBuffer) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create buffer");
                }
                this.id = pBuffer.get(0);

                org.lwjgl.vulkan.VkMemoryRequirements memRequirements = org.lwjgl.vulkan.VkMemoryRequirements.calloc(stack);
                vkGetBufferMemoryRequirements(device, this.id, memRequirements);

                org.lwjgl.vulkan.VkMemoryAllocateInfo allocInfo = org.lwjgl.vulkan.VkMemoryAllocateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                    .allocationSize(memRequirements.size())
                    .memoryTypeIndex(VoxyVulkanContext.getInstance().findMemoryType(memRequirements.memoryTypeBits(), VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT));

                java.nio.LongBuffer pMemory = stack.mallocLong(1);
                if (vkAllocateMemory(device, allocInfo, null, pMemory) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to allocate buffer memory");
                }
                this.memoryId = pMemory.get(0);

                vkBindBufferMemory(device, this.id, this.memoryId, 0);
            }
        }

        if ((flags & 0x01 /* VK_BUFFER_CREATE_SPARSE_BINDING_BIT */) == 0 && zero) {
            this.zero();
        }

        COUNT++;
        TOTAL_SIZE += size;
    }

    @Override
    public void free() {
        this.free0();
        if (VoxyVulkanContext.MOCK_VULKAN) {
            MemoryUtil.memFree(MemoryUtil.memByteBuffer(this.id, 1));
        } else {
            org.lwjgl.vulkan.VkDevice device = VoxyVulkanContext.getInstance().getDevice();
            vkDestroyBuffer(device, this.id, null);
            vkFreeMemory(device, this.memoryId, null);
        }

        COUNT--;
        TOTAL_SIZE -= this.size;
    }

    public boolean isSparse() {
        return (this.flags & 0x01 /* VK_BUFFER_CREATE_SPARSE_BINDING_BIT */) != 0;
    }

    public long size() {
        return this.size;
    }

    public VkBuffer zero() {
        // vkCmdFillBuffer placeholder
        return this;
    }

    public VkBuffer zeroRange(long offset, long size) {
        // vkCmdFillBuffer placeholder
        return this;
    }

    public VkBuffer fill(int data) {
        // vkCmdFillBuffer placeholder
        return this;
    }

    public static int getCount() {
        return COUNT;
    }

    public static long getTotalSize() {
        return TOTAL_SIZE;
    }

    public VkBuffer name(String name) {
        return this; // Placeholder for Vulkan Debug Marker
    }
}
