package me.cortex.voxy.client.core.vk;

import me.cortex.voxy.common.util.MemoryBuffer;
import me.cortex.voxy.common.util.TrackedObject;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.vulkan.VK10.*;

public class VkBuffer extends TrackedObject {
    public final long id;
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

        // Placeholder for real Vulkan buffer creation via VMA or vkCreateBuffer
        this.id = MemoryUtil.memAddress(MemoryUtil.memAlloc(1));

        if ((flags & 0x01 /* VK_BUFFER_CREATE_SPARSE_BINDING_BIT */) == 0 && zero) {
            this.zero();
        }

        COUNT++;
        TOTAL_SIZE += size;
    }

    @Override
    public void free() {
        this.free0();
        // Placeholder for real Vulkan buffer deletion
        MemoryUtil.memFree(MemoryUtil.memByteBuffer(this.id, 1));

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
