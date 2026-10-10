package me.cortex.voxy.client.core.rendering.section.backend.mdic;

import me.cortex.voxy.client.core.RenderProperties;
import me.cortex.voxy.client.core.vk.VkBuffer;
import me.cortex.voxy.client.core.rendering.Viewport;
import me.cortex.voxy.client.core.rendering.hierachical.HierarchicalOcclusionTraverser;

public class MDICViewport extends Viewport<MDICViewport> {
    public final VkBuffer drawCountCallBuffer = new VkBuffer(1024).zero();
    public final VkBuffer drawCallBuffer = new VkBuffer(5*4*(MDICSectionRenderer.OPAQUE_DRAW_COUNT+MDICSectionRenderer.TRANSLUCENT_DRAW_COUNT+MDICSectionRenderer.TEMPORAL_DRAW_COUNT)).zero();//400k draw calls
    public final VkBuffer positionScratchBuffer  = new VkBuffer(8*400000).zero();//400k positions
    public final VkBuffer indirectLookupBuffer = new VkBuffer(HierarchicalOcclusionTraverser.MAX_QUEUE_SIZE*4+4);//In theory, this could be global/not unique to the viewport
    public final VkBuffer visibilityBuffer;
    public org.lwjgl.vulkan.VkCommandBuffer commandBuffer;

    public MDICViewport(RenderProperties properties, int maxSectionCount) {
        super(properties);
        this.visibilityBuffer = new VkBuffer(maxSectionCount*4L);
        if (!me.cortex.voxy.client.core.vk.VoxyVulkanContext.MOCK_VULKAN) {
            try (org.lwjgl.system.MemoryStack stack = org.lwjgl.system.MemoryStack.stackPush()) {
                org.lwjgl.vulkan.VkCommandBufferAllocateInfo allocInfo = org.lwjgl.vulkan.VkCommandBufferAllocateInfo.calloc(stack)
                    .sType(org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                    .commandPool(me.cortex.voxy.client.core.vk.VoxyVulkanContext.getInstance().getCommandPool())
                    .level(org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                    .commandBufferCount(1);

                org.lwjgl.PointerBuffer pCommandBuffer = stack.mallocPointer(1);
                if (org.lwjgl.vulkan.VK10.vkAllocateCommandBuffers(me.cortex.voxy.client.core.vk.VoxyVulkanContext.getInstance().getDevice(), allocInfo, pCommandBuffer) != org.lwjgl.vulkan.VK10.VK_SUCCESS) {
                    throw new RuntimeException("Failed to allocate command buffers!");
                }
                this.commandBuffer = new org.lwjgl.vulkan.VkCommandBuffer(pCommandBuffer.get(0), me.cortex.voxy.client.core.vk.VoxyVulkanContext.getInstance().getDevice());
            }
        }
    }

    @Override
    protected void delete0() {
        super.delete0();
        this.visibilityBuffer.free();
        this.indirectLookupBuffer.free();
        this.drawCountCallBuffer.free();
        this.drawCallBuffer.free();
        this.positionScratchBuffer.free();
    }

    @Override
    public VkBuffer getRenderList() {
        return this.indirectLookupBuffer;
    }
}
