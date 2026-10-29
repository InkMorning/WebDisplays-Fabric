package net.montoyo.wd.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * Registers into the vanilla registries right away, which is when Fabric calls entrypoints.
 */
public class DeferredRegister<T> {
    private final ResourceKey<? extends Registry<T>> key;
    private final String namespace;

    private DeferredRegister(ResourceKey<? extends Registry<T>> key, String namespace) {
        this.key = key;
        this.namespace = namespace;
    }

    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> key, String namespace) {
        return new DeferredRegister<>(key, namespace);
    }

    @SuppressWarnings("unchecked")
    private Registry<T> registry() {
        ResourceLocation id = key.location();

        if (id.equals(Registries.BLOCK.location()))
            return (Registry<T>) BuiltInRegistries.BLOCK;
        if (id.equals(Registries.ITEM.location()))
            return (Registry<T>) BuiltInRegistries.ITEM;
        if (id.equals(Registries.BLOCK_ENTITY_TYPE.location()))
            return (Registry<T>) BuiltInRegistries.BLOCK_ENTITY_TYPE;
        if (id.equals(Registries.SOUND_EVENT.location()))
            return (Registry<T>) BuiltInRegistries.SOUND_EVENT;
        if (id.equals(Registries.CREATIVE_MODE_TAB.location()))
            return (Registry<T>) BuiltInRegistries.CREATIVE_MODE_TAB;

        Registry<T> found = (Registry<T>) BuiltInRegistries.REGISTRY.get(id);
        if (found == null)
            throw new RuntimeException("No built-in registry for " + id);
        return found;
    }

    public <U extends T> RegistryObject<U> register(String name, Supplier<? extends U> supplier) {
        U value = supplier.get();
        return new RegistryObject<>(Registry.register(registry(), new ResourceLocation(namespace, name), value));
    }

    public void register() {
        // Values are registered eagerly by register(String, Supplier).
    }
}
