package me.jellysquid.mods.sodium.mixin.entity.collisions;

import me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Replaces collision testing methods against the world border with faster checks.
 */
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public World level;

    @Shadow
    public abstract AxisAlignedBB getBoundingBox();

    /**
     * Uses a very quick check to determine if the player is outside the world border (which would disable collisions
     * against it). We also perform an additional check to see if the player can even collide with the world border in
     * this physics step, allowing us to remove it from collision testing in later code.
     *
     * @return True if no collision resolution will be performed against the world border, which removes it from the
     * stream of shapes to consider in entity collision code.
     */
    @Redirect(method = "collide(Lnet/minecraft/util/math/vector/Vector3d;)Lnet/minecraft/util/math/vector/Vector3d;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/shapes/VoxelShapes;joinIsNotEmpty(Lnet/minecraft/util/math/shapes/VoxelShape;Lnet/minecraft/util/math/shapes/VoxelShape;Lnet/minecraft/util/math/shapes/IBooleanFunction;)Z"))
    private boolean redirectWorldBorderMatchesAnywhere(VoxelShape borderShape, VoxelShape entityShape, IBooleanFunction func, Vector3d motion) {
        boolean isWithinWorldBorder = LithiumEntityCollisions.isWithinWorldBorder(this.level.getWorldBorder(), this.getBoundingBox().deflate(1.0E-7D));

        // If the entity is within the world border (enabling collisions against it), check that the player will cross the
        // border this physics step.
        if (isWithinWorldBorder) {
            return LithiumEntityCollisions.isWithinWorldBorder(this.level.getWorldBorder(), this.getBoundingBox().expandTowards(motion));
        }

        return true;
    }
}
