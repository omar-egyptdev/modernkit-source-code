# ModernKit — Codex Project Context and Working Instructions

## 1. Purpose

This file provides persistent project context and instructions for Codex.
Read it before inspecting or modifying the repository.

This handoff is based on the project history available to ChatGPT. It is
not a verbatim transcript of every previous conversation. The repository,
Git history, and current build output remain the authoritative sources.

Do not invent missing implementation details or claim that a feature is
complete without verifying it.

## 2. Project identity

- Project: ModernKit
- Repository: omar-egyptdev/modernkit-source-code
- Working directory: /workspaces/bedrock-mod
- Minecraft: Java Edition 1.16.5
- Mod loader: Fabric
- Language: Java
- Target bytecode: Java 8
- Main package: com.example.modernkit
- Development environment: GitHub Codespaces
- Build system: Gradle
- Known build command: ./gradlew build

## 3. Overall project vision

ModernKit is a Minecraft 1.16.5 Fabric mod that brings selected modern
Minecraft equipment and mechanics into the older version.

The project contains or has been developing:

1. A Bedrock Pickaxe.
2. A Mace with custom smash-attack mechanics.
3. A collection of modern-style spears with associated mechanics.
4. Supporting recipes, enchantments, loot, advancements, sounds,
   particles, and animations.

The goal is to implement these features properly rather than create
visual placeholders.

Preserve the existing project architecture and compatibility.

## 4. Spear project

The spear backport is a major part of the project.

The planned feature set includes:

- Six material variants.
- A Lunge enchantment.
- Spear charging mechanics.
- Spear combat behavior.
- Appropriate animations.
- Sounds.
- Particles.
- Crafting recipes.
- Smithing recipes where appropriate.
- Loot-table integration.
- An advancement.

These are the known requirements. The exact implementation details
and completion status must be established by inspecting the source.

Do not assume all six variants or all supporting features are complete.

When investigating the spear implementation, inspect:

- Item registration.
- Item classes.
- Material statistics.
- Attack and charge behavior.
- Enchantment registration and compatibility.
- Client-side rendering and animations.
- Sound event registration.
- Particle registration and spawning.
- Recipe JSON files.
- Loot-table JSON files.
- Advancement JSON files.
- Language translation files.
- Any mixins or other compatibility code.

Check consistency between registry identifiers, resource paths,
translation keys, and Java references.

## 5. Mace project

The project has an existing Mace implementation.

Historical design decisions included:

- MaceItem should extend ToolItem rather than SwordItem.
- The reason was to avoid sword sweep behavior.
- Intended statistics were 6 attack damage, 0.6 attack speed,
  and 500 durability.
- Breeze Rod was intended as a repair ingredient.
- A smash-attack implementation was planned using a mixin.
- The planned falling trigger was greater than 1.5 blocks.
- Additional damage tiers were planned.
- The planned smash radius was 3.5 blocks.
- Fall damage was intended to be reset or handled by the smash mechanic.

These are historical design notes, not proof that the current
implementation matches them.

Inspect the current Mace code before changing it. Preserve working
behavior and verify the actual implementation against these notes.

Do not rewrite the Mace system without first explaining the reason.

## 6. Bedrock Pickaxe project

The Bedrock Pickaxe is an existing part of ModernKit.

The project originally included work on a Fabric 1.16.5 Bedrock Pickaxe,
using the Yarn mappings and the project's existing Java and Gradle setup.

Inspect its current implementation, registration, statistics,
durability, recipes, translations, and assets before making changes.

Do not assume its current behavior from historical descriptions alone.

## 7. Confirmed build history

A recent build failed because the main mod class did not expose
ModernKitMod.LOGGER.

The reported locations were:

- advancement/MobKabobAdvancement.java, around line 35.
- loot/SpearWorldLoot.java, around line 87.

The repair added the following imports to ModernKitMod.java:

    import org.apache.logging.log4j.LogManager;
    import org.apache.logging.log4j.Logger;

And the following logger declaration:

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

After this repair, the user reported a successful build:

    BUILD SUCCESSFUL

This is the latest confirmed build result in the available history.
It does not guarantee that the current working tree is unchanged.

Verify the current source and Git state before relying on this result.

## 8. Technical compatibility requirements

These requirements are important:

- Keep Minecraft at 1.16.5.
- Keep Fabric as the mod loader.
- Maintain Java 8 bytecode compatibility.
- Do not upgrade Minecraft or dependencies without explicit approval.
- Use the project's actual mappings and dependency versions.
- Use APIs available in the target version.
- Do not accidentally introduce APIs from newer Minecraft versions.
- Follow existing registration and resource conventions.
- Preserve existing working functionality.
- Avoid unnecessary dependencies.
- Avoid unrelated refactoring.
- Do not silently replace existing systems with different architectures.

If a requested feature requires a newer API, investigate a compatible
1.16.5 implementation before proposing an upgrade.

## 9. Repository inspection procedure

Before editing anything:

1. Read this AGENTS.md file.
2. Read any other applicable AGENTS.md files.
3. Inspect the current working directory.
4. Inspect the repository structure.
5. Run git status --short.
6. Review the current Git diff.
7. Inspect relevant source files and resources.
8. Inspect build.gradle, settings.gradle, gradle.properties,
   and other relevant project configuration.
9. Identify the exact task and its dependencies.
10. Report what you found before modifying files.

Do not overwrite the user's uncommitted work.

Do not assume that a clean build from an earlier conversation means
the current checkout is clean.

## 10. Build and testing procedure

The known build command is:

    ./gradlew build

Run it from the repository root.

When a build fails:

- Read the complete error output.
- Identify the first meaningful failure.
- Inspect the relevant source and configuration.
- Find the underlying cause.
- Make a focused repair.
- Run the build again.
- Report the actual result.

Do not suppress errors simply to make the build pass.

A successful compilation does not automatically prove that gameplay,
recipes, loot, advancements, sounds, particles, or rendering work.

Use additional validation when available.

## 11. Java implementation rules

When changing Java code:

- Follow the existing package structure.
- Follow the project's naming conventions.
- Keep Java 8 compatibility.
- Avoid unsupported language features.
- Use the project's existing Fabric and Minecraft APIs.
- Avoid unnecessary class duplication.
- Keep responsibilities clear.
- Handle client-only code appropriately.
- Avoid introducing server/client class-loading problems.
- Preserve existing registrations and identifiers.

Before adding a new class, inspect whether an equivalent class already
exists.

Before changing a registry identifier, search for all references to it.

## 12. Resource implementation rules

When changing JSON resources or assets:

- Follow the existing directory layout.
- Validate JSON syntax.
- Check every referenced item and identifier.
- Check translation keys.
- Check recipe ingredients and outputs.
- Check loot-table references.
- Check advancement criteria and rewards.
- Avoid missing textures or sound references.
- Avoid inconsistent capitalization and naming.

Do not create placeholder resources and describe them as finished assets.

## 13. Combat and gameplay rules

For weapons and combat mechanics, inspect existing implementations
before changing behavior.

Consider:

- Attack damage.
- Attack speed.
- Attack cooldown.
- Durability consumption.
- Enchantment compatibility.
- Entity damage.
- Knockback.
- Player movement.
- Fall damage.
- Server-side authority.
- Client-side animation and effects.
- Multiplayer behavior.

Avoid accidentally changing unrelated weapon behavior.

Do not assume that a mechanic works correctly just because its code
compiles.

## 14. Git safety

Never discard user changes.

Do not run destructive commands such as:

- git reset --hard
- git clean -fd
- git checkout -- .
- Force-push operations

unless the user explicitly requests the particular operation and
understands its consequences.

Inspect Git status before and after significant changes.

Do not commit or push changes unless the user asks.

## 15. Communication preferences

The user prefers practical, direct technical assistance.

When asked to provide code:

- Provide complete code when appropriate.
- Avoid unexplained fragments.
- Make commands copyable.
- Identify the terminal and directory where commands should run.
- Explain important changes.
- Do not claim that something was tested if it was not.

When continuing project development:

- Establish the actual repository state first.
- Explain what is complete.
- Explain what is incomplete.
- Identify uncertainty.
- Propose the next safe step.
- Avoid unnecessary questions when the repository already contains
  the information needed.
- Do not start editing before explaining findings when the task
  requires investigation.

Do not use excessive celebratory language or claim progress that
has not been verified.

## 16. Current starting point

The last confirmed successful build followed the logger repair
described in Section 7.

The next task is to inspect the actual repository and determine:

- Which spear features are implemented.
- Which spear features are incomplete.
- Whether all six material variants exist.
- Whether Lunge is registered and functional.
- Whether charging and combat behavior are implemented.
- Whether sounds, particles, and animations are connected.
- Whether recipes and loot integration are present.
- Whether the advancement is valid.
- Whether the Mace implementation matches its intended behavior.
- Whether the Bedrock Pickaxe remains functional.
- Whether the project builds successfully now.

Do not guess which development stage comes next.

First inspect and report the current state. Then propose the next
safe development step.

## 17. Historical information limitations

This document preserves the project details available in the
conversation context. It does not contain every previous message,
every stage checklist, every source file, or every debugging log.

If an exact historical requirement is needed but is not recorded
here, do not fabricate it.

Use the repository, Git history, and any transcript supplied by
the user to recover the missing information.

## 18. Instructions to Codex

Treat this file as persistent project guidance.

Start by inspecting the repository and its Git status.

Do not modify any files yet.

Compare the actual project with the goals and history documented
here. Report:

1. The current repository structure.
2. The current Git status.
3. The actual state of the spear implementation.
4. The actual state of the Mace implementation.
5. The actual state of the Bedrock Pickaxe.
6. Any build or compatibility problems found.
7. The next recommended development step.

Wait for the user's approval before making changes.
NOTE: OpenCode, read this and please make sure you know. For the current task u were doing (the obsidian/redstone task thing), Dont commit or push yet, and make the verson 1.5.97, I will give you the sound files after you finish, ok? bro stop thinking and then say ok so i can confirm u saw this then PLS continue doing what u were doing/thinking.

## 19. For All Agents

You need to read this, So after you've done a task, you can put all the memory from that current task that you've did and you know into this slot, for example do Memory 1: then Memory 2:

Memory 1:
v1.5.97 task (Nov 17 2026): built dist/modernkit-1.5.97.jar, NOT committed/pushed (user ordered "don't commit or push yet" for this task; user will send door sound files after). What changed: (1) `block.minecraft.rainbow_block` = "Rainbow Block" lang key added — in v1.5.9 the gen script only generated keys for slabs/doors/gear, never the full rainbow block. (2) Redstone Slab fixed: new class `block/RedstoneSlabBlock.java extends SlabBlock` overriding `getSidesShape` → `VoxelShapes.fullCube()` AND `ModBlocks.REDSTONE_SLAB` built with `AbstractBlock.Settings.solidBlock((state, world, pos) -> true)`. These are the only two 1.16.5 knobs needed: `RedstoneWireBlock` checks dust placement/wire-climb via `isSideSolidFullSquare` → `SideShapeType.FULL` → `getSidesShape`, and power conduction through a block (`World.getEmittedRedstonePower`) gates on `BlockState.isSolidBlock` → the `solidBlockPredicate` ContextPredicate from Settings (there is no block-level isSolidBlock override in 1.16.5). (3) Door item icons: `scripts/make_door_item_icons.py` writes `assets/modernkit/textures/item/obsidian_door.png` (16×16: obsidian-block panel, dark 2px frame, 2×2 3px transparent windows, hard alpha) and `rainbow_door.png` (16×160 animated, 10 frames, frametime 3 interpolate) + mcmeta; door item models now use `layer0: modernkit:item/{obsidian,rainbow}_door`. (4) All obsidian items now `.fireproof()`: 8 gear + obsidian slab/stairs/door BlockItems (ModBlocks.block() gained an Item.Settings overload + fireproofItem() helper). Durability re-verified (tools 2048, armor ×40).

Memory 2:
1.16.5 DoorBlock sound facts (for the pending custom obsidian door sounds): `DoorBlock.getOpenSoundEventId()` / `getCloseSoundEventId()` are PRIVATE and pick world-event IDs by `Material` (METAL → open 1011 / close 1005, else wooden 1012 / 1006); `DoorBlock.playOpenCloseSound(World, BlockPos, boolean)` is PRIVATE and plays via `World.syncWorldEvent(player, eventId, pos, 0)`. Plan when the user's sound files arrive: mixin on `DoorBlock#playOpenCloseSound` (method HEAD, cancellable) — if `world.getBlockState(pos).getBlock() == ModBlocks.OBSIDIAN_DOOR`, cancel and `world.playSound(null, pos, ourSoundEvent, category, volume, pitch)` with SoundEvents registered alongside ModSounds. Sound files must be `.ogg` for Minecraft — if the user sends wav/mp3, convert with ffmpeg (check it exists first). The other 7 doors keep vanilla sounds.

Memory 3:
Codespace /tmp wipes on restart (hit twice now): `/tmp/opencode/vanilla` (oak door/stairs blockstates + oak door/slab loot tables, needed by `scripts/gen-v159-resources.js`) must be re-extracted from the loom merged jar `/home/codespace/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged/1.16.5-net.fabricmc.yarn.1_16_5.1.16.5+build.10-v2/minecraft-merged-1.16.5-net.fabricmc.yarn.1_16_5.1.16.5+build.10-v2.jar` (paths: `assets/minecraft/blockstates/oak_door.json`, `assets/minecraft/blockstates/oak_stairs.json`, `data/minecraft/loot_tables/blocks/oak_door.json`, `data/minecraft/loot_tables/blocks/oak_slab.json`). Same for the texture tooling in `/tmp/opencode/testsrc` + `/tmp/opencode/testbuild` + natives (`/tmp/opencode/lwjnatives`: liblwjgl/liblwjgl_stb/libjemalloc .so from libraries.minecraft.net lwjgl 3.2.2 natives-linux jars) + `/tmp/opencode/cp.txt` classpath file (loom jar + gson/guava/log4j/DFU/brigadier/fastutil/lang3/commons-io/lwjgl/lwjgl-stb from $M2).

Memory 4:
Sounds batch in v1.5.97 (Nov 17 2026, built into dist/modernkit-1.5.97.jar, NOT committed). SPEAR (user-supplied, 9 oggs mono 48 kHz, named must start with spear_): placed `assets/modernkit/sounds/entity/spear/spear_attack{1,2,3}.ogg`/`spear_hit{1,2,3}.ogg`/`spear_lunge{1,2,3}.ogg`; mapping attack1-3 → `entity.modernkit.spear.jab_start`, hit1-3 → `jab_impact`, lunge1/2/3 → `charge_engaged`/`charge_tired`/`charge_disengaged` (order-based 1:1, user can swap); sounds.json's 5 spear entries switched from `type:event` (vanilla stand-ins) to `type:file`. Files normalized where peak > 0.99 (hit1/hit3 re-encoded, rest copied). Uploads arrive at `/tmp/opencode/uploads/<uuid>/<name>.ogg` — session db had files:0, uploads dir is authoritative; glob `/tmp/opencode/uploads/*/{kind}[123].ogg`. OBSDOOR (synthesized, user had no recordings): `scripts/make_door_sounds.py` (numpy 2.5.5 + soundfile 0.14.0, both pip-installed; libsndfile bundled supports OGG VORBIS; NO ffmpeg on box) writes `assets/modernkit/sounds/block/obsidian_door/{open.ogg 0.95s, close.ogg 0.75s}` mono 44.1 kHz peak ~0.88 seed 20261117; open = latch clack + 55 Hz sub thump + stone grind (110–900 Hz noise, 2.7 Hz wobble) + 175→92 Hz whoosh + settle thump; close = crack + 64 Hz body + octave + rumble tail + 2380 Hz glassy ping + 48 Hz sub. Wired: sounds.json keys `block.modernkit.obsidian_door.{open,close}`, ModSounds fields+registerSound calls `OBSIDIAN_DOOR_OPEN`/`OBSIDIAN_DOOR_CLOSE`. Mace + Wind Charge sounds still pending from user.

Memory 5:
DoorBlockMixin (v1.5.97, shipped): `mixin/DoorBlockMixin.java` @Mixin(DoorBlock.class), added to common `mixins` array in modernkit.mixins.json. Two handlers: (1) `@Redirect` on `onUse`'s inline `World.syncWorldEvent` (target `Lnet/minecraft/world/World;syncWorldEvent(Lnet/minecraft/entity/player/PlayerEntity;ILnet/minecraft/util/math/BlockPos;I)V`) — obsidian door only: reads `world.getBlockState(pos).get(DoorBlock.OPEN)` AFTER the setBlockState (redirect handler's enclosing-args are entry values, so the `state` param is PRE-cycle and unusable); else forwards vanilla. (2) `@Inject` HEAD-cancellable on private `playOpenCloseSound(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Z)V`. CRITICAL 1.16.5 Yarn quirk (verified by decompiling ClientWorld→WorldRenderer.processWorldEvent lookupswitch): world-event ids 1005/1006 = door OPEN sounds, 1011/1012 = door CLOSE sounds, so Yarn's `getOpenSoundEventId`(1011/1012)/`getCloseSoundEventId`(1005/1006) names are swapped relative to actual playback, and `playOpenCloseSound`'s boolean param actually means "opening" (true → getCloseSoundEventId → 1005/1006 → open sound; setOpen passes its `open` param straight through, neighborUpdate passes the new powered state). Shared helper `modernkit$playObsidianDoorSound(world,pos,open)` plays `world.playSound(null, pos, open ? OBSIDIAN_DOOR_OPEN : OBSIDIAN_DOOR_CLOSE, SoundCategory.BLOCKS, 1.0F, 1.0F)` guarded `!world.isClient`, cancels vanilla on BOTH sides → exactly one server broadcast, double-play-proof (client may or may not run these paths). Other 7 doors keep vanilla sounds. Refmap verified resolved (method_9534 onUse, method_10036 playOpenCloseSound, method_8444 syncWorldEvent).

Memory 6:
Wind Charge + spear-default-sound removal (v1.5.97, Nov 17 2026, built, NOT committed). User's 3 wind burst OGGs (mono 48 kHz, peaks ≤0.998 → copied as-is) replaced placeholder `sounds/entity/wind_charge/wind_burst{1,2,3}.ogg`. `item.modernkit.wind_charge.throw` was the LAST vanilla sounds.json ref (`type:event minecraft:item.trident.throw`) — replaced with synthesized `entity/wind_charge/wind_throw.ogg` via `scripts/make_wind_throw_sound.py` (0.50s fwoof: 300–2400 Hz air swell + delayed 120–600 Hz air mass + 140→300 Hz doppler body, peak 0.85 seed 20261117; in-game play: 0.5 vol, 0.9–1.1 random pitch from WindChargeItem). sounds.json now 9 events, ALL custom files, zero `type:event`. `SpearMobCombat` mob charge impact had hardcoded `SoundEvents.ITEM_TRIDENT_HIT` — now `ModSounds.SPEAR_JAB_IMPACT` (0.8F/0.9 pitch/HOSTILE); SoundEvents import removed. IMPORTANT: `SmashAttack` (mace smash shockwave) reuses `ModSounds.WIND_CHARGE_BURST` at ~line 131 vol 1.0 — user's real burst files now also drive the mace smash; when Mace files arrive give it dedicated events (mace.smash etc.) and replace SmashAttack's 3 vanilla playSound calls (BLOCK_ANVIL_LAND, ENTITY_GENERIC_EXPLODE, ENTITY_IRON_GOLEM_HURT ~lines 160-163). Remaining direct SoundEvents uses are intentional: WindExplosion's lever/trapdoor/fence-gate open/close (opened blocks' own sounds), armor-material equip sounds. Wind charge burst plays via `WindExplosion.burst(..., WIND_CHARGE_BURST, 0.8F, 0.9+rand*0.2)`; throw via WindChargeItem `playSound(null, xyz, WIND_CHARGE_THROW, PLAYERS, 0.5F, 0.9+rand*0.2)`.

Memory 7:
Mace sounds (v1.5.97, Nov 17 2026, built, NOT committed). 8 user OGGs (mono 48 kHz; smash_ground1 peak 1.028→re-encoded 0.99, smash_air1 0.998→0.99, smash_air3 0.994→0.99, rest copied) → `sounds/entity/mace/smash_ground{1,2,3,4}.ogg`, `smash_ground_heavy.ogg`, `smash_air{1,2,3}.ogg`. 3 new events registered (`MACE_SMASH_GROUND`, `MACE_SMASH_GROUND_HEAVY`, `MACE_SMASH_AIR` = `entity.modernkit.mace.*`). Mapping by function: smash impact (`SmashAttack.doEffects`) ← ground1-4 vol 1.0 pitch 0.9+rand*0.2 (replaced BLOCK_ANVIL_LAND); fall > 5 adds `smash_ground_heavy` 1.0/1.0 (replaced ENTITY_IRON_GOLEM_HURT); Wind Burst shockwave (`windBurstLaunch`, also fires for mobs via MobEntityMixin) ← `smash_air` 1.0F/0.9+rand*0.2 (replaced the WIND_CHARGE_BURST borrow — now the mace no longer borrows wind charge sounds). The fake `ENTITY_GENERIC_EXPLODE` layer in doEffects was dropped. `SoundEvents` import removed from SmashAttack. sounds.json: 12 events, ZERO vanilla refs. MaceItem.java itself has no sound code; all mace sounds live in SmashAttack (invoked from PlayerEntityMixin + MobEntityMixin). Note for future: vanilla 1.21 semantics use `item.mace.smash_air` for regular melee hits — if user wants that, wire smash_air into the normal-hit path instead of windBurstLaunch. Direct vanilla `SoundEvents.` uses remaining in code are intentional only: WindExplosion lever/trapdoor/fence-gate (opened blocks' own sounds) + armor material equip sounds.

Memory 8:
Wind Charge mechanics rework (v1.5.97, Nov 17 2026, built, NOT committed) — makes it behave like vanilla 1.21 (facts from minecraft.wiki/w/Wind_charge). User verdict was: skeleton matched vanilla but (a) missing the signature fall rule "fall damage accumulates only when falling below the Y level where the burst hit", (b) self-launch ~10 blocks vs vanilla ~6, (c) flat CLOUD/POOF puff visuals, (d) burst too quiet. Changes: (1) NEW `entity/WindChargeFloors.java` — static `Map<UUID,Double>` floors; `setFloor(player, burstY)` called from `WindExplosion.knockbackNearby` for every ServerPlayerEntity that receives radial knockback (placed AFTER the EntityVelocityUpdateS2CPacket send); per-server-tick: while `player.getY() >= floor` → `player.fallDistance = 0.0F`; entry removed when player null / `!isAlive()` / `isOnGround()`. 1.16.5 Yarn API gotchas (compile errors hit and fixed): `Entity.removed` is a public FIELD (no `isRemoved()` method — that's 1.17+), `Entity.onGround` is PROTECTED (use public `isOnGround()`), `fallDistance` is public float (assignable). Registered via `ServerTickEvents.END_SERVER_TICK.register(WindChargeFloors::tick)` in `ModernKitMod.onInitialize` — fabric-lifecycle-events-v1 1.2.2+3cc0f0907d is on the classpath (verify with javap against `$HOME/.gradle/caches/modules-2/files-2.1/net.fabricmc.fabric-api/fabric-lifecycle-events-v1/...`), `EndTick.onEndTick(MinecraftServer)` signature matches the static tick method. Floor is PLAYERS ONLY (mobs keep fall damage — vanilla); the mace smash attacker is `excluded` in `WindExplosion.burst` so gets NO floor (their landing fall damage preserved; a successful smash already cancels it anyway); mace wind-burst VICTIMS do get a floor. (2) `WindExplosion.knockbackNearby` vertical formula `0.45D + strength * 0.65D` → `0.35D + strength * 0.45D` (full-strength up-push ≈ 0.98 velocity ≈ ~6 blocks straight-down throw, vanilla). (3) Particles: replaced CLOUD×24 + POOF×8 with `spawnGust(serverWorld,x,y,z)` = two flat CLOUD rings via `spawnRing()` helper (r=0.7 @10 pts then r=1.5 @+0.15Y @16 pts, explicit angular positions, count=1 per call), rising center CLOUD column (6 pts, 0.35Y spacing), 12 END_ROD sparks on a r=0.9 circle at y+0.3, soft base CLOUD puff (6 @ y+0.2). (4) `WindChargeEntity.burst` volume 0.8F → 1.0F (vanilla plays wind burst at volume 4/attenuation 8; 1.0 is our max sensible). Verified in jar: WindChargeFloors.class present, END_SERVER_TICK invokedynamic in ModernKitMod bytecode, spawnGust/spawnRing methods, ldc2_w 0.35/0.45 constants, WindChargeFloors.setFloor invokestatic, zero leftover 0.65/0.8F. Mace smash launch (`SmashAttack.windBurstLaunch` attacker launch value) is SEPARATE code and untouched.

Memory 9:
Post-test fix round (v1.5.97, Nov 17 2026, built, NOT committed) after user tested and reported ALL custom sounds silent + door icons not looking like real MC doors. CRITICAL ROOT CAUSE OF SILENT MOD SOUNDS (1.16.5): sounds.json file entry names WITHOUT a namespace prefix resolve via `new Identifier(name)` in `net.minecraft.client.sound.Sound`'s constructor to the MINECRAFT namespace — so `"entity/mace/smash_ground1"` looked for `minecraft:sounds/entity/mace/smash_ground1.ogg` (missing) and every custom event was silent (also why the obsidian door was silent: DoorBlockMixin canceled vanilla and played the broken event). Verified by decompiling the Yarn-named loom jar classes: `SoundEntryDeserializer` + `Sound$RegistrationType` (enum FILE name `"file"`, SOUND_EVENT name `"event"` → `"type":"file"` IS valid, NOT the bug); vanilla 1.16.5 sounds.json is NOT shipped in the game jar (fetched from Mojang CDN piston-meta assetIndex→resources.download.minecraft.net) and uses 2164 plain strings / 1361 no-type objects / 33 `type:event` / ZERO `type:file`. FIX: all 10 sound entries rewritten as plain strings WITH `modernkit:` prefix; zero type fields. **RULE FOR ALL FUTURE MOD SOUND FILES: always prefix `modernkit:` in sounds.json.** Obsidian Door: user asked for normal sound → DoorBlockMixin deleted (java + mixins.json entry), OBSIDIAN_DOOR_OPEN/CLOSE removed from ModSounds/sounds.json, `sounds/block/` dir deleted; door block is Material.STONE → vanilla wooden door sound plays (hand via onUse syncWorldEvent, redstone via playOpenCloseSound). `scripts/make_door_sounds.py` kept for future use. Door icons: user rejected the previous custom icons → `scripts/make_door_item_icons.py` rewritten to recolor the VANILLA oak door item sprite structure (extracted pixel-by-pixel from the 1.16.5 jar `assets/minecraft/textures/item/oak_door.png`: 16×16, windows are TRANSPARENT holes rows 3-4/6-7 cols 5-7+9-11, left edge dark with hinge pixels '#' at rows 4+12, handle '#' row 8 cols 10-11, recessed panels as L-light blocks rows 9-10+12-13, dark base row 15; four shades L/o/d/# mapped to material palettes; door item model is just `item/generated`+layer0, same as vanilla). New textures: obsidian_door.png 16×16 static (ramp 77,55,116 / 56,39,90 / 36,24,62 / 13,5,24), rainbow_door.png 16×160 (10 hue-cycled frames, existing mcmeta frametime 3 + interpolate kept). ModSounds now 10 events (5 spear, 3 mace, 2 wind). CHANGELOG/README/README.txt updated (obsidian door custom-sound claims removed; fix note added).

Memory 10:
Door item icon v2 (v1.5.97, Nov 18 2026, built, NOT committed) — user said the obsidian door icon looked completely different from the PLACED door. Cause: v1 icon used an invented bright purple flat palette while the placed door uses `assets/minecraft/textures/block/obsidian_door_top.png` + `_bottom.png` (near-black obsidian swirls, palette #000000/#0d080d/#130d13/#150e15/#201520/#231d30/#312c4a/#41345a; top has the 4 vanilla-style window holes; both are essentially vanilla oak door textures recolored to obsidian). FIX: rewrote `scripts/make_door_item_icons.py` to SAMPLE the placed-door block textures inside the vanilla door-icon template instead of using invented colors: upper icon half (rows 2-8) samples `_top`, lower half (rows 9-15) samples `_bottom`, icon cols 3-12 map to texture cols 0-15 proportionally; brightness multipliers L×1.45 / o×1.15 / d×0.85 keep panels/edges readable; '#' (hinges/handle) = darkest color present in the sampled texture (obsidian #000000, computed per texture with luminance); window holes from the template stay transparent; if a sampled texture pixel is a window hole (alpha 0) it falls back to the nearest opaque row. Rainbow door block textures are 16×160 animated (10 frames) → rainbow item icon is 16×160 with 10 correspondingly sampled frames (existing mcmeta frametime 3 + interpolate kept). Verified visually by upscaling icon vs block textures side by side (obsidian/rainbow both match); jar re-verified byte-identical to source (obsidian 16×16, rainbow 16×160). Placed door models/blockstates untouched (`minecraft:block/obsidian_door_*` / `rainbow_door_*`, parent `block/door_bottom`/`door_top`).

Memory 11:
Wind Charge mechanics round 3 (v1.5.97, Nov 18 2026, built, NOT committed) — user tested round 2 and said the mechanics were "poor and not even good" → full reprogram, sounds kept. ROOT CAUSE of the weak feel: `WindExplosion.knockbackNearby` measured falloff from `entity.getBodyY(0.5)` (mid-body) so a feet-burst had distance 0.9/2.4 → 0.625×base, plus a flat `0.35 + strength*0.45` vertical pop with horizontal-only direction → self-launch ≈3.4 blocks (vanilla ~6). Second bug: `WindChargeFloors.tick` removed the floor entry when `player.isOnGround()` — true on the burst tick itself (player still standing) — so protection was silently lost on EVERY feet-burst → fall damage from the hop. FIX (1) `knockbackNearby` now replicates the vanilla 1.16.5 explosion loop, decompiled from the Yarn loom jar (`Explosion.collectBlocksAndDamageEntities`): `d = sqrt(entity.squaredDistanceTo(center)) / radius` — `Entity.squaredDistanceTo(Vec3d)` is FEET-based (`getY()`), vanilla divisor local f3 = `power * 2.0F` (= our radius param); direction = (entity pos − center) normalized with `entity.getEyeY()` for the Y component (vanilla uses getEyeY, getY only for TNT); `j = (1 − d) × Explosion.getExposure(center, entity)` — **`Explosion.getExposure(Vec3d, Entity)` is PUBLIC STATIC in 1.16.5** (verified by javap), so line-of-sight occlusion now weakens pushes through walls; kept existing Blast Protection dampener `1 − min(20, level*2)/25` (NOT vanilla's `ProtectionEnchantment.transformExplosionKnockback` = `kb − floor(kb × level × 0.15)` — that floor-based vanilla formula does NOTHING at our ~1 kb magnitudes, so the smooth dampener is kept to honor the wiki "Blast Protection reduces knockback"); kept knockback-resistance attribute; result `velocity.add(dx*j, dy*j, dz*j)` + `velocityModified` + velocity packet + `setFloor` for players. The 0.35 base / 0.45 factor are gone (verified javap: no `double 0.45d` in the method; the only remaining 0.35 in WindExplosion is `spawnGust`'s Y-spacing — don't confuse them). (2) `WindChargeEntity.KNOCKBACK = 1.1D` (was inline `1.4D`): standing feet-burst ≈ 7 blocks up. Height reference (vanilla physics `v' = (v − 0.08) × 0.98`, height = sum of POSITIVE velocities used for movement before each update — validated: jump v=0.42 → exactly 1.25 blocks): v=1.0→5.9, 1.1→7.0, 1.15→7.6, 1.22→8.4. Jump on top clears the 8-block "Who Needs Rockets?" advancement. Mace Wind Burst untouched (base 1.4×multiplier, now vanilla-shaped: primary victim ≈1.25 @I). (3) `WindChargeFloors` rewritten: `Map<UUID, Floor>` with `Floor { y, grace=40, launched }`; each tick zero `fallDistance` while `getY() >= floor.y`; remove entry when (a) player null/dead, (b) `launched && isOnGround()` (landed after take-off), or (c) `!launched && --grace <= 0` (never took off). This fixes the burst-tick removal: on the burst tick the player is still on the ground, grace just decrements. (4) Flight trail: `WindChargeEntity.tick()` → `super.tick()` then client-only `spawnTrail()` — per-tick CLOUD at spiral position (r=0.22, phase `age*0.7`, y-bob `cos(age*0.45)*0.12`, vel 0/0.01/0) + END_ROD every 4 ticks; client-side spawn means every client ticking the entity shows it. APIs verified by javap before coding: `Entity.age` public int, `Entity.squaredDistanceTo(Vec3d)` public, `Entity.getEyeY()` public, `World.addParticle(ParticleEffect, double×6)` public, `ParticleTypes.CLOUD`/`END_ROD` exist. Projectile self-hit checked: `ProjectileEntity.method_26958` (=canHit) excludes the owner until `leftOwner` → feet-bursts don't self-damage. Wiki facts used: straight-line flight (no gravity ✓ correct), 2.4-block sphere, fall damage only below burst Y, advancement "begin falling at least 7 blocks above the burst position", "~6 blocks aimed straight down". CHANGELOG (new bullets: proper launch / directional push / flight trail / fall-protection reliability), README.md + dist/README.txt (Wind Charge line: ~7-block launch + trail), TODO updated.