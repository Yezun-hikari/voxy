package me.cortex.voxy.client.core.vk;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VK10;

public class VoxyVulkanContext {
    private static VoxyVulkanContext INSTANCE;

    public static final boolean MOCK_VULKAN = true;

    private VkDevice device;
    private VkPhysicalDevice physicalDevice;
    private VkQueue queue;
    private long commandPool;

    private VoxyVulkanContext() {
        if (MOCK_VULKAN) {
            // Setup dummy pointers
            long mockDevicePtr = MemoryUtil.memAddress(MemoryUtil.memAlloc(1));
            long mockPhysDevicePtr = MemoryUtil.memAddress(MemoryUtil.memAlloc(1));
            long mockQueuePtr = MemoryUtil.memAddress(MemoryUtil.memAlloc(1));

            // Note: In real app these should be initialized with real VkInstance and VkDeviceCreateInfo
            // But since MOCK_VULKAN is true, these are simply placeholders to prevent crashes
        }
    }

    public static void init() {
        if (INSTANCE == null) {
            INSTANCE = new VoxyVulkanContext();
        }
    }

    public static VoxyVulkanContext getInstance() {
        return INSTANCE;
    }

    public VkDevice getDevice() {
        return device;
    }

    public VkPhysicalDevice getPhysicalDevice() {
        return physicalDevice;
    }

    public VkQueue getQueue() {
        return queue;
    }

    public long getCommandPool() {
        return commandPool;
    }


    public int findMemoryType(int typeFilter, int properties) {
        if (MOCK_VULKAN) {
            return 0;
        }
        try (org.lwjgl.system.MemoryStack stack = org.lwjgl.system.MemoryStack.stackPush()) {
            org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties memProperties = org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties.calloc(stack);
            org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceMemoryProperties(physicalDevice, memProperties);
            for (int i = 0; i < memProperties.memoryTypeCount(); i++) {
                if ((typeFilter & (1 << i)) != 0 && (memProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
                    return i;
                }
            }
            throw new RuntimeException("Failed to find suitable memory type!");
        }
    }

    public void cleanup() {
        if (MOCK_VULKAN) {
            return;
        }
        if (commandPool != 0) {
            VK10.vkDestroyCommandPool(device, commandPool, null);
        }
    }
}
