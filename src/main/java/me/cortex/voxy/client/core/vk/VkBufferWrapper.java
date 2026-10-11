package me.cortex.voxy.client.core.vk;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;

import java.nio.LongBuffer;

public class VkBufferWrapper {
    private long buffer = 0;
    private long memory = 0;
    private final long size;

    public VkBufferWrapper(long size, int usage, int memoryProperties) {
        this.size = size;

        if (VoxyVulkanContext.MOCK_VULKAN) {
            this.buffer = 1; // Dummy values
            this.memory = 1;
            return;
        }

        VkDevice device = VoxyVulkanContext.getInstance().getDevice();
        if (device == null) {
            throw new RuntimeException("Vulkan device is null");
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Create buffer
            VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.calloc(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                    .size(size)
                    .usage(usage)
                    .sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);

            LongBuffer pBuffer = stack.mallocLong(1);
            if (VK10.vkCreateBuffer(device, bufferInfo, null, pBuffer) != VK10.VK_SUCCESS) {
                throw new RuntimeException("Failed to create buffer!");
            }
            this.buffer = pBuffer.get(0);

            // Get memory requirements
            VkMemoryRequirements memRequirements = VkMemoryRequirements.calloc(stack);
            VK10.vkGetBufferMemoryRequirements(device, this.buffer, memRequirements);

            // Allocate memory
            VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.calloc(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                    .allocationSize(memRequirements.size())
                    .memoryTypeIndex(VoxyVulkanContext.getInstance().findMemoryType(memRequirements.memoryTypeBits(), memoryProperties));

            LongBuffer pMemory = stack.mallocLong(1);
            if (VK10.vkAllocateMemory(device, allocInfo, null, pMemory) != VK10.VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate buffer memory!");
            }
            this.memory = pMemory.get(0);

            // Bind memory to buffer
            if (VK10.vkBindBufferMemory(device, this.buffer, this.memory, 0) != VK10.VK_SUCCESS) {
                throw new RuntimeException("Failed to bind buffer memory!");
            }
        }
    }

    public long getBuffer() {
        return buffer;
    }

    public long getMemory() {
        return memory;
    }

    public long getSize() {
        return size;
    }

    public void free() {
        if (VoxyVulkanContext.MOCK_VULKAN) {
            this.buffer = 0;
            this.memory = 0;
            return;
        }

        VkDevice device = VoxyVulkanContext.getInstance().getDevice();
        if (device != null) {
            if (this.buffer != 0) {
                VK10.vkDestroyBuffer(device, this.buffer, null);
                this.buffer = 0;
            }
            if (this.memory != 0) {
                VK10.vkFreeMemory(device, this.memory, null);
                this.memory = 0;
            }
        }
    }
}
