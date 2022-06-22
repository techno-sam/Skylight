package com.slimeist.skylight.client.render.sky;

/*
   Copyright 2020 qouteall

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

Modified by Slimeist to only render the sky
 */

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionType;

import java.awt.*;
import java.util.ArrayList;

public class McHelper {

    /*public static IEThreadedAnvilChunkStorage getIEStorage(RegistryKey<DimensionType> dimension) {
        return (IEThreadedAnvilChunkStorage) (
                (ServerChunkCache) MiscHelper.getServer().getLevel(dimension).getChunkSource()
        ).chunkMap;
    }

    public static ArrayList<ServerPlayerEntity> getCopiedPlayerList() {
        return new ArrayList<>(MiscHelper.getServer().getPlayerList().getPlayers());
    }

    public static ArrayList<ServerPlayerEntity> getRawPlayerList() {
        return MiscHelper.getServer().getPlayerList().getPlayers();
    }*/

    public static Vec3d lastTickPosOf(Entity entity) {
        return new Vec3d(entity.prevX, entity.prevY, entity.prevZ);
    }

    /*public static ServerWorld getOverWorldOnServer() {
        return MiscHelper.getServer().getLevel(Level.OVERWORLD);
    }

    public static void serverLog(
            ServerPlayerEntity player,
            String text
    ) {
        Helper.log(text);
        player.displayClientMessage(new TextComponent(text), false);
    }

    public static long getServerGameTime() {
        return getOverWorldOnServer().getGameTime();
    }

    public static <T> void performMultiThreadedFindingTaskOnServer(
            Stream<T> stream,
            Predicate<T> predicate,
            IntPredicate taskWatcher,//return false to abort the task
            Consumer<T> onFound,
            Runnable onNotFound,
            Runnable finalizer
    ) {
        int[] progress = new int[1];
        Helper.SimpleBox<Boolean> isAborted = new Helper.SimpleBox<>(false);
        Helper.SimpleBox<Runnable> finishBehavior = new Helper.SimpleBox<>(() -> {
            Helper.err("Error Occured");
        });
        CompletableFuture<Void> future = CompletableFuture.runAsync(
                () -> {
                    try {
                        T result = stream.peek(
                                obj -> {
                                    progress[0] += 1;
                                }
                        ).filter(
                                predicate
                        ).findFirst().orElse(null);
                        if (result != null) {
                            finishBehavior.obj = () -> onFound.accept(result);
                        }
                        else {
                            finishBehavior.obj = onNotFound;
                        }
                    }
                    catch (Throwable t) {
                        t.printStackTrace();
                        finishBehavior.obj = () -> {
                            t.printStackTrace();
                        };
                    }
                },
                Util.backgroundExecutor()
        );
        IPGlobal.serverTaskList.addTask(() -> {
            if (future.isDone()) {
                if (!isAborted.obj) {
                    finishBehavior.obj.run();
                    finalizer.run();
                }
                else {
                    Helper.log("Future done but the task is aborted");
                }
                return true;
            }
            if (future.isCancelled()) {
                Helper.err("The future is cancelled");
                finalizer.run();
                return true;
            }
            if (future.isCompletedExceptionally()) {
                Helper.err("The future is completed exceptionally");
                finalizer.run();
                return true;
            }
            boolean shouldContinue = taskWatcher.test(progress[0]);
            if (!shouldContinue) {
                isAborted.obj = true;
                future.cancel(true);
                finalizer.run();
                return true;
            }
            else {
                return false;
            }
        });
    }

    public static <ENTITY extends Entity> List<ENTITY> getEntitiesNearby(
            Level world,
            Vec3 center,
            Class<ENTITY> entityClass,
            double range
    ) {
        return findEntitiesRough(
                entityClass,
                world,
                center,
                (int) (range / 16 + 1),
                e -> true
        );
    }

    public static <ENTITY extends Entity> List<ENTITY> getEntitiesNearby(
            Entity center,
            Class<ENTITY> entityClass,
            double range
    ) {
        return getEntitiesNearby(
                center.level,
                center.position(),
                entityClass,
                range
        );
    }

    public static int getRenderDistanceOnServer() {
        return getIEStorage(Level.OVERWORLD).ip_getWatchDistance();
    }*/

    public static void setPosAndLastTickPos(
            Entity entity,
            Vec3d pos,
            Vec3d lastTickPos
    ) {
        entity.setPosition(pos.x, pos.y, pos.z);
        entity.lastRenderX = lastTickPos.x;
        entity.lastRenderY = lastTickPos.y;
        entity.lastRenderZ = lastTickPos.z;
        entity.prevX = lastTickPos.x;
        entity.prevY = lastTickPos.y;
        entity.prevZ = lastTickPos.z;
    }

    public static Vec3d getEyePos(Entity entity) {
        Vec3d eyeOffset = new Vec3d(0, entity.getEyeHeight(entity.getPose()), 0);
        return entity.getPos().add(eyeOffset);
    }

    public static Vec3d getLastTickEyePos(Entity entity) {
        Vec3d eyeOffset = new Vec3d(0, entity.getEyeHeight(entity.getPose()), 0);
        return lastTickPosOf(entity).add(eyeOffset);
    }

    public static void setEyePos(Entity entity, Vec3d eyePos, Vec3d lastTickEyePos) {
        Vec3d eyeOffset = new Vec3d(0, entity.getEyeHeight(entity.getPose()), 0);

        setPosAndLastTickPos(
                entity,
                eyePos.subtract(eyeOffset),
                lastTickEyePos.subtract(eyeOffset)
        );

//        float eyeHeight = entity.getStandingEyeHeight();
//        setPosAndLastTickPos(
//            entity,
//            eyePos.add(0, -eyeHeight, 0),
//            lastTickEyePos.add(0, -eyeHeight, 0)
//        );
    }
/*
    public static double getVehicleY(Entity vehicle, Entity passenger) {
        return passenger.getY() - vehicle.getPassengersRidingOffset() - passenger.getMyRidingOffset();
    }

    public static void adjustVehicle(Entity entity) {
        Entity vehicle = entity.getVehicle();
        if (vehicle == null) {
            return;
        }

        Vec3 currVelocity = vehicle.getDeltaMovement();

        double newX = entity.getX();
        double newY = getVehicleY(vehicle, entity);
        double newZ = entity.getZ();
        vehicle.setPos(newX, newY, newZ);
        Vec3 newPos = new Vec3(newX, newY, newZ);
        McHelper.setPosAndLastTickPos(
                vehicle, newPos, newPos
        );

        // MinecartEntity or LivingEntity use position interpolation
        // disable the interpolation or it may interpolate into unloaded chunks
        vehicle.lerpTo(
                newX, newY, newZ, vehicle.getYRot(), vehicle.getXRot(),
                0, false
        );

        vehicle.setDeltaMovement(currVelocity);

    }*/
/*
    public static LevelChunk getServerChunkIfPresent(
            ResourceKey<Level> dimension,
            int x, int z
    ) {
        ChunkHolder chunkHolder_ = getIEStorage(dimension).ip_getChunkHolder(ChunkPos.asLong(x, z));
        if (chunkHolder_ == null) {
            return null;
        }
        return chunkHolder_.getTickingChunk();
    }

    public static LevelChunk getServerChunkIfPresent(
            ServerLevel world, int x, int z
    ) {
        ChunkHolder chunkHolder_ = ((IEThreadedAnvilChunkStorage) (
                (ServerChunkCache) world.getChunkSource()
        ).chunkMap).ip_getChunkHolder(ChunkPos.asLong(x, z));
        if (chunkHolder_ == null) {
            return null;
        }
        return chunkHolder_.getTickingChunk();
    }

    @Deprecated
    public static <ENTITY extends Entity> Stream<ENTITY> getServerEntitiesNearbyWithoutLoadingChunk(
            Level world,
            Vec3 center,
            Class<ENTITY> entityClass,
            double range
    ) {
        return McHelper.findEntitiesRough(
                entityClass,
                world,
                center,
                (int) (range / 16),
                e -> true
        ).stream();
    }

    public static void updateBoundingBox(Entity pla
import it.unimi.dsi.fastutil.objects.ObjectArrayList;yer) {
        player.setPos(player.getX(), player.getY(), player.getZ());
    }

    public static void updatePosition(Entity entity, Vec3 pos) {
        entity.setPos(pos.x, pos.y, pos.z);
    }

    public static <T extends Entity> List<T> getEntitiesRegardingLargeEntities(
            Level world,
            AABB box,
            double maxEntitySizeHalf,
            Class<T> entityClass,
            Predicate<T> predicate
    ) {
        return findEntitiesByBox(
                entityClass,
                world,
                box,
                maxEntitySizeHalf,
                predicate
        );
    }


    public static Portal copyEntity(Portal portal) {
        Portal newPortal = ((Portal) portal.getType().create(portal.level));

        Validate.notNull(newPortal);

        newPortal.load(portal.saveWithoutId(new CompoundTag()));
        return newPortal;
    }

    public static boolean getIsServerChunkGenerated(ResourceKey<Level> toDimension, BlockPos toPos) {
        return getIEStorage(toDimension)
                .portal_isChunkGenerated(new ChunkPos(toPos));
    }

    // because withUnderline is client only
    @Environment(EnvType.CLIENT)
    public static MutableComponent getLinkText(String link) {
        return new TextComponent(link).withStyle(
                style -> style.withClickEvent(new ClickEvent(
                        ClickEvent.Action.OPEN_URL, link
                )).withUnderlined(true)
        );
    }

    public static void validateOnServerThread() {
        Validate.isTrue(Thread.currentThread() == MiscHelper.getServer().getRunningThread(), "must be on server thread");
    }

    public static void invokeCommandAs(Entity commandSender, List<String> commandList) {
        CommandSourceStack commandSource = commandSender.createCommandSourceStack().withPermission(2).withSuppressedOutput();
        Commands commandManager = MiscHelper.getServer().getCommands();

        for (String command : commandList) {
            commandManager.performCommand(commandSource, command);
        }
    }

    public static void resendSpawnPacketToTrackers(Entity entity) {
        getIEStorage(entity.level.dimension()).ip_resendSpawnPacketToTrackers(entity);
    }

    public static void sendToTrackers(Entity entity, Packet<?> packet) {
        ChunkMap.TrackedEntity entityTracker =
                getIEStorage(entity.level.dimension()).ip_getEntityTrackerMap().get(entity.getId());
        if (entityTracker == null) {
//            Helper.err("missing entity tracker object");
            return;
        }

        entityTracker.broadcastAndSend(packet);
    }

    //it's a little bit incorrect with corner glass pane
    @Nullable
    public static AABB getWallBox(Level world, IntBox glassArea) {
        return glassArea.stream().map(blockPos -> {
            VoxelShape collisionShape = world.getBlockState(blockPos).getCollisionShape(world, blockPos);

            if (collisionShape.isEmpty()) {
                return null;
            }

            return collisionShape.bounds().move(Vec3.atLowerCornerOf(blockPos));
        }).filter(b -> b != null).reduce(AABB::minmax).orElse(null);
    }

    public static interface ChunkAccessor {
        LevelChunk getChunk(int x, int z);
    }

    public static ChunkAccessor getChunkAccessor(Level world) {
        if (world.isClientSide()) {
            return world::getChunk;
        }
        else {
            return (x, z) -> getServerChunkIfPresent(((ServerLevel) world), x, z);
        }
    }

    public static <T extends Entity> List<T> findEntities(
            Class<T> entityClass,
            LevelEntityGetter<Entity> entityLookup,
            int chunkXStart,
            int chunkXEnd,
            int chunkYStart,
            int chunkYEnd,
            int chunkZStart,
            int chunkZEnd,
            Predicate<T> predicate
    ) {
        ArrayList<T> result = new ArrayList<>();

        foreachEntities(
                entityClass, entityLookup,
                chunkXStart, chunkXEnd, chunkYStart, chunkYEnd, chunkZStart, chunkZEnd,
                entity -> {
                    if (predicate.test(entity)) {
                        result.add(entity);
                    }
                }
        );
        return result;
    }

    /**
     * the range is inclusive on both ends
     * similar to {@link SectionedEntityCache#forEachInBox(Box, Consumer)}
     * but without hardcoding the max entity radius
     */
    /*
    public static <T extends Entity> void foreachEntities(
            Class<T> entityClass, LevelEntityGetter<Entity> entityLookup,
            int chunkXStart, int chunkXEnd,
            int chunkYStart, int chunkYEnd,
            int chunkZStart, int chunkZEnd,
            Consumer<T> consumer
    ) {
        Validate.isTrue(chunkXEnd >= chunkXStart);
        Validate.isTrue(chunkYEnd >= chunkYStart);
        Validate.isTrue(chunkZEnd >= chunkZStart);
        Validate.isTrue(chunkXEnd - chunkXStart < 1000, "too big");
        Validate.isTrue(chunkZEnd - chunkZStart < 1000, "too big");

        EntityTypeTest<T, T> typeFilter = EntityTypeTest.forClass(entityClass);

        EntitySectionStorage<Entity> cache =
                (EntitySectionStorage<Entity>) ((IELevelEntityGetterAdapter) entityLookup).getCache();

        ((IESectionedEntityCache) cache).forEachSectionInBox(
                chunkXStart, chunkXEnd,
                chunkYStart, chunkYEnd,
                chunkZStart, chunkZEnd,
                entityTrackingSection -> {
                    ((IEEntityTrackingSection) entityTrackingSection).myForeach(
                            typeFilter, consumer
                    );
                }
        );
    }

    //faster
    public static <T extends Entity> List<T> findEntitiesRough(
            Class<T> entityClass,
            Level world,
            Vec3 center,
            int radiusChunks,
            Predicate<T> predicate
    ) {
        // the minimun is 1
        if (radiusChunks == 0) {
            radiusChunks = 1;
        }

        ChunkPos chunkPos = new ChunkPos(new BlockPos(center));
        return findEntities(
                entityClass,
                ((IEWorld) world).portal_getEntityLookup(),
                chunkPos.x - radiusChunks,
                chunkPos.x + radiusChunks,
                McHelper.getMinSectionY(world), McHelper.getMaxSectionYExclusive(world) - 1,
                chunkPos.z - radiusChunks,
                chunkPos.z + radiusChunks,
                predicate
        );
    }

    //does not load chunk on server and works with large entities
    public static <T extends Entity> List<T> findEntitiesByBox(
            Class<T> entityClass,
            Level world,
            AABB box,
            double maxEntityRadius,
            Predicate<T> predicate
    ) {
        ArrayList<T> result = new ArrayList<>();

        foreachEntitiesByBox(entityClass, world, box, maxEntityRadius, predicate, result::add);
        return result;
    }*/
/*
    public static <T extends Entity> void foreachEntitiesByBox(
            Class<T> entityClass, Level world, AABB box,
            double maxEntityRadius, Predicate<T> predicate, Consumer<T> consumer
    ) {

        foreachEntitiesByBoxApproximateRegions(entityClass, world, box, maxEntityRadius, entity -> {
            if (entity.getBoundingBox().intersects(box) && predicate.test(entity)) {
                consumer.accept(entity);
            }
        });
    }

    public static <T extends Entity> void foreachEntitiesByBoxApproximateRegions(
            Class<T> entityClass, Level world, AABB box, double maxEntityRadius, Consumer<T> consumer
    ) {
        int xMin = (int) Math.floor(box.minX - maxEntityRadius);
        int yMin = (int) Math.floor(box.minY - maxEntityRadius);
        int zMin = (int) Math.floor(box.minZ - maxEntityRadius);
        int xMax = (int) Math.ceil(box.maxX + maxEntityRadius);
        int yMax = (int) Math.ceil(box.maxY + maxEntityRadius);
        int zMax = (int) Math.ceil(box.maxZ + maxEntityRadius);

        int minChunkY = McHelper.getMinSectionY(world);
        int maxChunkYExclusive = McHelper.getMaxSectionYExclusive(world);

        foreachEntities(
                entityClass, ((IEWorld) world).portal_getEntityLookup(),
                xMin >> 4, xMax >> 4,
                Mth.clamp(yMin >> 4, minChunkY, maxChunkYExclusive - 1),
                Mth.clamp(yMax >> 4, minChunkY, maxChunkYExclusive - 1),
                zMin >> 4, zMax >> 4,
                consumer
        );
    }

    public static <T extends Entity> void foreachEntitiesByPointAndRoughRadius(
            Class<T> entityClass, Level world, Vec3 point, int roughRadius,
            Consumer<T> consumer
    ) {
        SectionPos sectionPos = SectionPos.of(new BlockPos(point));
        int roughRadiusChunks = roughRadius / 16;
        if (roughRadiusChunks == 0) {
            roughRadiusChunks = 1;
        }

        foreachEntities(
                entityClass, ((IEWorld) world).portal_getEntityLookup(),
                sectionPos.x() - roughRadiusChunks,
                sectionPos.x() + roughRadiusChunks,
                sectionPos.y() - roughRadiusChunks,
                sectionPos.y() + roughRadiusChunks,
                sectionPos.z() - roughRadiusChunks,
                sectionPos.z() + roughRadiusChunks,
                consumer
        );
    }


    public static ResourceLocation dimensionTypeId(ResourceKey<Level> dimType) {
        return dimType.location();
    }

    public static <T> String serializeToJson(T object, Codec<T> codec) {
        DataResult<JsonElement> r = codec.encode(object, JsonOps.INSTANCE, new JsonObject());
        Either<JsonElement, DataResult.PartialResult<JsonElement>> either = r.get();
        JsonElement result = either.left().orElse(null);
        if (result != null) {
            return IPGlobal.gson.toJson(result);
        }

        return either.right().map(DataResult.PartialResult::toString).orElse("");
    }*/

    public static class MyDecodeException extends RuntimeException {

        public MyDecodeException(String message) {
            super(message);
        }
    }

    public static <T, Serialized> T decodeFailHard(
            Codec<T> codec,
            DynamicOps<Serialized> ops,
            Serialized target
    ) {
        return codec.decode(ops, target)
                .getOrThrow(false, s -> {
                    throw new MyDecodeException("Cannot decode" + s + target);
                }).getFirst();
    }

    public static <Serialized> Serialized getElementFailHard(
            DynamicOps<Serialized> ops,
            Serialized target,
            String key
    ) {
        return ops.get(target, key).getOrThrow(false, s -> {
            throw new MyDecodeException("Cannot find" + key + s + target);
        });
    }

    public static <T, Serialized> void encode(
            Codec<T> codec,
            DynamicOps<Serialized> ops,
            Serialized target,
            T object
    ) {
        codec.encode(object, ops, target);
    }

    public static <Serialized, T> T decodeElementFailHard(
            DynamicOps<Serialized> ops, Serialized input,
            Codec<T> codec, String key
    ) {
        return decodeFailHard(
                codec, ops,
                getElementFailHard(ops, input, key)
        );
    }
/*
    public static void sendMessageToFirstLoggedPlayer(Component text) {
        Helper.log(text.getContents());
        IPGlobal.serverTaskList.addTask(() -> {
            MinecraftServer server = MiscHelper.getServer();
            if (server == null) {
                return false;
            }

            List<ServerPlayer> playerList = server.getPlayerList().getPlayers();
            if (playerList.isEmpty()) {
                return false;
            }

            for (ServerPlayer player : playerList) {
                player.displayClientMessage(text, false);
            }

            return true;
        });
    }

    public static Iterable<Entity> getWorldEntityList(Level world) {
        if (world.isClientSide()) {
            return CHelper.getWorldEntityList(world);
        }
        else {
            if (world instanceof ServerLevel) {
                return ((ServerLevel) world).getAllEntities();
            }
            else {
                return ((Iterable<Entity>) Collections.emptyList().iterator());
            }
        }
    }

    /**
     * It will spawn even if the chunk is not loaded
     *
     * @link ServerWorld#addEntity(Entity)
     */
    /*
    public static void spawnServerEntity(Entity entity) {
        Validate.isTrue(!entity.level.isClientSide());

        boolean spawned = entity.level.addFreshEntity(entity);

        if (!spawned) {
            Helper.err("Failed to spawn " + entity + entity.level);
        }
    }

    public static ServerLevel getServerWorld(ResourceKey<Level> dim) {
        ServerLevel world = MiscHelper.getServer().getLevel(dim);
        if (world == null) {
            throw new RuntimeException("Missing dimension " + dim.location());
        }
        return world;
    }

    public static Component compoundTagToTextSorted(CompoundTag tag, String indent, int depth) {
        return new MyNbtTextFormatter(" ", 0).apply(tag);
    }

    public static int getMinY(LevelAccessor world) {
        return world.getMinBuildHeight();
    }

    public static int getMaxYExclusive(LevelAccessor world) {
        return world.getMaxBuildHeight();
    }

    public static int getMaxContentYExclusive(LevelAccessor world) {
        return world.dimensionType().logicalHeight() + getMinY(world);
    }

    public static int getMinSectionY(LevelAccessor world) {
        return world.getMinSection();
    }

    public static int getMaxSectionYExclusive(LevelAccessor world) {
        return world.getMaxSection();
    }

    public static int getYSectionNumber(LevelAccessor world) {
        return getMaxSectionYExclusive(world) - getMinSectionY(world);
    }

    public static AABB getBoundingBoxWithMovedPosition(
            Entity entity, Vec3 newPos
    ) {
        return entity.getBoundingBox().move(
                newPos.subtract(entity.position())
        );
    }

    public static String readTextResource(ResourceLocation identifier) {
        String result = null;
        try {
            InputStream inputStream =
                    Minecraft.getInstance().getResourceManager().getResource(
                            identifier
                    ).getInputStream();

            result = IOUtils.toString(inputStream, Charset.defaultCharset());
        }
        catch (IOException e) {
            throw new RuntimeException("Error loading " + identifier, e);
        }
        return result;
    }

    public static Vec3d getWorldVelocity(Entity entity) {
        return GravityChangerInterface.invoker.getWorldVelocity(entity);
    }

    public static void setWorldVelocity(Entity entity, Vec3 newVelocity) {
        GravityChangerInterface.invoker.setWorldVelocity(entity, newVelocity);
    }

    public static Vec3 getEyeOffset(Entity entity) {
        return GravityChangerInterface.invoker.getEyeOffset(entity);
    }
*/
}