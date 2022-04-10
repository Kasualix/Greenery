package teksturepako.greenery.common.world.grass

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.chunk.IChunkProvider
import net.minecraft.world.gen.IChunkGenerator
import net.minecraftforge.fml.common.IWorldGenerator
import teksturepako.greenery.common.registry.ModBlocks
import teksturepako.greenery.common.util.WorldGenUtil
import teksturepako.greenery.common.world.IGreeneryWorldGenerator
import teksturepako.greenery.config.Config
import java.util.*

class WorldGenTallGrass : IGreeneryWorldGenerator {

    private val block = ModBlocks.blockTallGrass
    private val config = Config.generation.grass

    private val generationChance = config.generationChance
    private val patchAttempts = config.patchAttempts
    private val plantAttempts = config.plantAttempts
    override val validBiomeTypes = config.validBiomeTypes.toMutableList()
    private val inverted = config.inverted

    override fun generate(
        rand: Random,
        chunkX: Int,
        chunkZ: Int,
        world: World,
        chunkGenerator: IChunkGenerator,
        chunkProvider: IChunkProvider
    ) {
        val random = world.rand
        val chunkPos = world.getChunk(chunkX, chunkZ).pos
        val biome = WorldGenUtil.getBiomeInChunk(world, chunkX, chunkZ)

        if (rand.nextDouble() < generationChance && WorldGenUtil.areBiomeTypesValid(biome, validBiomeTypes, inverted)) {
            repeat(patchAttempts) {
                val x = random.nextInt(16) + 8
                val z = random.nextInt(16) + 8

                val yRange = world.getHeight(chunkPos.getBlock(0, 0, 0).add(x, 0, z)).y + 32
                val y = random.nextInt(yRange)

                val pos = chunkPos.getBlock(0, 0, 0).add(x, y, z)
                generatePlants(world, random, pos)
            }
        }
    }

    private fun generatePlants(world: World, rand: Random, targetPos: BlockPos) {
        repeat(plantAttempts) {
            val pos = targetPos.add(
                rand.nextInt(8) - rand.nextInt(8),
                rand.nextInt(4) - rand.nextInt(4),
                rand.nextInt(8) - rand.nextInt(8)
            )

            if (world.isAirBlock(pos)) {
                placePlant(world, pos, rand)
            }
        }
    }

    private fun placePlant(world: World, pos: BlockPos, rand: Random) {
        val startingAge = rand.nextInt(block.maxAge)
        val state = block.defaultState.withProperty(block.ageProperty, startingAge)
        val maxState = block.defaultState.withProperty(block.ageProperty, block.maxAge)

        if (block.canBlockStay(world, pos, state)) {
            world.setBlockState(pos, state, 2)

            if (rand.nextDouble() < 0.2) {
                world.setBlockState(pos, maxState, 2)

                if (world.isAirBlock(pos.up()) && block.canBlockStay(world, pos.up(), state)) {
                    world.setBlockState(pos.up(), state, 2)
                }
            }
        }
    }
}