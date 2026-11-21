# ModernKit v1.5.9 — TODO & Implementation Plan

**Status:** implemented as **v1.5.9** (build successful). The 5 material doors (gold/diamond/emerald/coal/redstone) were deferred to a later update at the user's request — everything else shipped. Remaining: in-game test by user, then commit/push on request.
**Target:** Minecraft 1.16.5, Fabric, Java 8. All new items/blocks registered under the `minecraft:` namespace (project convention).

---

## Scope — 24 new things

| # | Group | Contents | New textures needed |
|---|-------|----------|---------------------|
| 1 | Obsidian gear (8 items) | Sword, Axe, Pickaxe, Shovel, Helmet, Chestplate, Leggings, Boots | 8 item icons + 2 armor layers |
| 2 | Obsidian blocks (3) | Door, Stairs, Slab | none — reuse vanilla `block/obsidian` |
| 3 | Rainbow blocks (3) | Block, Slab, Door | block texture + door bottom/top |
| 4 | Material slabs (5) | Gold, Diamond, Emerald, Coal, Redstone | none — reuse vanilla block textures |
| 5 | Material doors (5) | Gold, Diamond, Emerald, Coal, Redstone | none — reuse vanilla block textures |

Java classes needed: `ObsidianToolMaterial`, `ObsidianArmorMaterial`, `ObsidianItems`, plus 16 new blocks in `ModBlocks` (with BlockItems). No new entity/mixin work.

---

## Verified facts (checked against the 1.16.5 loom jar this session)

- **Heavy Core survival bug confirmed:** `AbstractBlock.getLootTableId()` lazily builds `<namespace>:blocks/<path>` from the registry id. `minecraft:heavy_core` → loot table `minecraft:blocks/heavy_core`, which doesn't exist → **Heavy Core drops nothing when broken in survival**. Retroactive fix ships in 1.5.9: `data/minecraft/loot_tables/blocks/heavy_core.json`.
- **Doors & Material.METAL:** `DoorBlock.onUse` returns PASS when `material == METAL` (iron-door behavior: redstone-only). Open/close sounds also key off METAL (1011/1005 iron vs 1012/1006 wooden). `isWoodenDoor` (zombie door-breaking) checks `Material.WOOD`/`NETHER_WOOD`.
  → **All 7 new doors use `Material.STONE`**: hand-openable ✓, wooden door sounds, zombies can't break them ✓ (gold/diamond/etc. doors stay safe).
- **Tool-required drops:** `Settings.requiresTool()` exists but defaults to **false** in 1.16.5. We will NOT call it → every new block drops when broken, even by hand.
- **Pickaxe mining speed:** 1.16.5 `PickaxeItem` uses a hardcoded `EFFECTIVE_BLOCKS` Set (not a tag) — can't cheaply make vanilla pickaxes mine our blocks faster. Skipped (polish only).
- **1.16.5 door resources:** per-door blockstate = 32 variants referencing 4 models; those models parent the generic `block/door_bottom`, `door_bottom_rh`, `door_top`, `door_top_rh` and bind `bottom`/`top` textures. Door **item** model = `item/generated` + `layer0` icon texture (vanilla uses dedicated `item/oak_door.png`).
- **Slab resources:** blockstate 3 variants (`type=bottom/top/double`, double → full base-block model). Model = parent `block/slab` (+`slab_top`) with `bottom/top/side`. Loot: no `survives_explosion` on the pool; `set_count 2` gated on `block_state_property` `type=double`, plus `explosion_decay`.
- **Stairs resources:** `StairBlock(baseBlockState, settings)`; blockstate = 40 variants; 3 models parenting `block/stairs`, `inner_stairs`, `outer_stairs` with `bottom/top/side`.
- **Door loot:** drop gated on `block_state_property` `half=lower` + `survives_explosion`.
- **Armor layers:** loaded from `assets/minecraft/textures/models/armor/<material.getName()>_layer_1.png` (+`_layer_2`) in the `minecraft:` namespace. Existing convention: `modernkit_emerald_*`, `modernkit_rainbow_*`.
- **Patterns to mirror:** `EmeraldItems` (registration), `EmeraldToolMaterial` / `EmeraldArmorMaterial` (stats layout), `RainbowArmorMaterial`, `ModBlocks` (Heavy Core), vanilla-template blockstates/models extracted to `/tmp/opencode/vanilla/`.
- Obsidian armor will use the **vanilla armor model** (no custom client model class like Emerald/Rainbow) — simplest and reliable; can upgrade later.

---

## Phase 0 — texture hunt (user)

13 files minimum. PNG, RGBA. Item icons ideally exactly 16×16 (clean 32×32 also fine — we downscale). Hard-alpha pipeline is applied to item icons automatically; block/door/layer textures need no binarization.

| # | What | Repo path | Size | Notes |
|---|------|-----------|------|-------|
| 1 | Obsidian Sword icon | `assets/minecraft/textures/item/obsidian_sword.png` | 16×16 | vanilla tool-art style |
| 2 | Obsidian Pickaxe icon | `assets/minecraft/textures/item/obsidian_pickaxe.png` | 16×16 | |
| 3 | Obsidian Axe icon | `assets/minecraft/textures/item/obsidian_axe.png` | 16×16 | |
| 4 | Obsidian Shovel icon | `assets/minecraft/textures/item/obsidian_shovel.png` | 16×16 | |
| 5 | Obsidian Helmet icon | `assets/minecraft/textures/item/obsidian_helmet.png` | 16×16 | |
| 6 | Obsidian Chestplate icon | `assets/minecraft/textures/item/obsidian_chestplate.png` | 16×16 | |
| 7 | Obsidian Leggings icon | `assets/minecraft/textures/item/obsidian_leggings.png` | 16×16 | |
| 8 | Obsidian Boots icon | `assets/minecraft/textures/item/obsidian_boots.png` | 16×16 | |
| 9 | Obsidian armor layer 1 | `assets/minecraft/textures/models/armor/modernkit_obsidian_layer_1.png` | 64×32 | vanilla armor layout |
| 10 | Obsidian armor layer 2 | `assets/minecraft/textures/models/armor/modernkit_obsidian_layer_2.png` | 64×32 | leggings/boots layer |
| 11 | Rainbow Block | `assets/minecraft/textures/block/rainbow_block.png` | 16×16 | tileable, fully opaque |
| 12 | Rainbow Door bottom | `assets/minecraft/textures/block/rainbow_door_bottom.png` | 16×16 | door-panel art |
| 13 | Rainbow Door top | `assets/minecraft/textures/block/rainbow_door_top.png` | 16×16 | door-panel art (window optional) |

Optional 14: dedicated door item-icon art (one reusable 16×16 sprite) — otherwise door icons reuse the door-bottom texture.

---

## Phase 1 — Java

- [ ] `ObsidianToolMaterial` — proposed stats (tunable):
  - `DURABILITY = 2048`, `MINING_LEVEL = 3`, `ENCHANTABILITY = 18`, `MATERIAL_ATTACK_DAMAGE = 4.0F`
  - mining speeds: Sword 9.0, Pickaxe 15.0, Axe 17.0, Shovel 21.0 (one step above Emerald)
  - same per-tool attack bonuses/speeds as Emerald (sword 12→13 dmg via the 4.0 material bonus)
  - repair ingredient: `minecraft:obsidian`
- [ ] `ObsidianArmorMaterial` — proposed stats (tunable): protection `{3, 6, 8, 3}` (20 = diamond cap), toughness `3.0F` (netherite-level), durability ×40, enchantability 18, equip sound DIAMOND, `getName() = "modernkit_obsidian"`, knockback 0, repair obsidian
- [ ] `ObsidianItems` — 8 items mirroring `EmeraldItems` (sword COMBAT; pickaxe/axe/shovel TOOLS; armor COMBAT). Reuse plain `PickaxeItem`/`SwordItem` + existing `CustomAxeItem`/`AreaShovelItem`? — decide: plain vanilla `AxeItem`/`ShovelItem` for obsidian (keep special classes Emerald-only), default yes
- [ ] `ModBlocks` — 16 new blocks + BlockItems (BUILDING_BLOCKS group):
  - `obsidian_slab` (SlabBlock, STONE, strength 50/1200), `obsidian_stairs` (StairBlock on `Blocks.OBSIDIAN.getDefaultState()`, 50/1200), `obsidian_door` (DoorBlock, STONE, strength 3/1200)
  - `rainbow_block` (Block, METAL, 5/6), `rainbow_slab` (5/6), `rainbow_door` (STONE door)
  - `gold_slab`/`gold_door` (METAL 3/6), `diamond_slab`/`diamond_door`, `emerald_slab`/`emerald_door` (5/6), `coal_slab`/`coal_door` (STONE 5/6), `redstone_slab`/`redstone_door` (STONE 5/6) — all doors `Material.STONE`, sound groups chosen per material (METAL for gold/diamond/emerald, STONE for obsidian/coal/redstone/rainbow)
  - no `requiresTool()` anywhere
- [ ] Client: new `client/BlockClient.java` (ClientModInitializer) — `BlockRenderLayerMap` cutout for all 7 doors (their textures may have transparent pixels); add to `fabric.mod.json` client entrypoints

## Phase 2 — Assets

- [ ] Blockstates (`assets/minecraft/blockstates/`): `obsidian_slab` + `rainbow_slab` + 5 material slabs (3 variants; `double` → base block model), `obsidian_stairs` (vanilla 40-variant pattern), `rainbow_block` (single variant), 7 door blockstates (copy vanilla oak 32-variant pattern, rename model refs)
- [ ] Block models: 7× slab + 7× slab_top; 3× stairs models (`obsidian_stairs`, `_inner`, `_outer`); 7×4 door models (`<name>_door_bottom`, `_bottom_hinge`, `_top`, `_top_hinge` parenting the generic door templates); 1× rainbow_block (`cube_all`)
- [ ] Door textures for vanilla-material doors: bind `block/obsidian`, `block/gold_block`, `block/diamond_block`, `block/emerald_block`, `block/coal_block`, `block/redstone_block` as both `bottom` and `top` — zero new art
- [ ] Item models: 8 obsidian gear (tools → `item/handheld` like emerald gear, armor → `item/generated`); 16 block-items → parent `block/<name>`; 7 door items → `item/generated` with `layer0` = door-bottom texture
- [ ] Place Phase 0 textures at the repo paths above (item icons through the binarize/extrusion pipeline)
- [ ] Lang (`assets/modernkit/lang/en_us.json`): 8 × `item.minecraft.obsidian_*` + 16 × `block.minecraft.*` keys

## Phase 3 — Data

- [ ] 24 recipes in `data/modernkit/recipes/` (namespace convention unchanged):
  - 8 obsidian gear (obsidian material, standard vanilla shapes)
  - `obsidian_slab` (3 obsidian → 6), `obsidian_stairs` (6 obsidian → 4), `obsidian_door` (6 obsidian → 3)
  - `rainbow_block` (9 rainbow_ingot → 1), `rainbow_slab` (3 rainbow_block → 6), `rainbow_door` (6 → 3)
  - 5 material slabs (3 of gold_block/diamond_block/emerald_block/coal_block/redstone_block → 6)
  - 5 material doors (6 of each block → 3)
- [ ] 17 loot tables in `data/minecraft/loot_tables/blocks/`:
  - `heavy_core.json` — **retroactive bug fix** (drop self, survives_explosion)
  - 7 slabs (self; `type=double → count 2`), 7 doors (self; `half=lower`), `obsidian_stairs`, `rainbow_block`
- [ ] Optional extras (skip unless asked): slab→block recombine recipes, rainbow stairs

## Phase 4 — Build, test, release

- [ ] User picks version (e.g. 1.5.9) → `echo "<version>" | npm run build:java2`
- [ ] Verify jar: version string, new resources present, sodium override intact, docs match version
- [ ] In-game checklist for user: craft + place + break every block type; all doors open by hand; obsidian gear stats/enchants; **Heavy Core now drops in survival** (old bug); doors survive zombie proximity
- [ ] Docs: `README.md` + `dist/README.txt` byte-identical, `dist/CHANGELOG.txt` latest-release-only (include Heavy Core drop fix as a bugfix line)
- [ ] Commit/push only on request (as `JustANormalDeveloper <omar772.pro@gmail.com>`)

---

## Open questions for the user

1. Version number for this release.
2. Approve/tune the proposed obsidian gear + armor stats.
3. Rainbow block recipe cost: 9 rainbow ingots OK, or cheaper?
4. Obsidian stairs/slab authentic hardness 50 (slow to mine like obsidian) or friendlier value?
5. Door item icons: reuse door-bottom texture, or hunt a dedicated door icon sprite?

## 1.16.5 API quick-reference (for implementation)

- `SlabBlock(AbstractBlock.Settings)`, `StairBlock(BlockState, AbstractBlock.Settings)`, `DoorBlock(AbstractBlock.Settings)`
- `AbstractBlock.Settings`: `.of(Material)`, `.strength(float, float)`, `.sounds(BlockSoundGroup)`, `.nonOpaque()` (use for doors), `.requiresTool()` (do NOT use)
- `Settings.lootTable(Identifier)` / `.dropsNothing()` / `.dropsLike(Block)` exist but default-derived `blocks/<name>` is the project convention
- Fabric render layer: `net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutout())`

---

## v1.5.97 — fixes batch (built Nov 17 2026, NOT committed — user ordered "don't commit or push yet")

**Status:** built as `dist/modernkit-1.5.97.jar` (`BUILD SUCCESSFUL`, jar verified). ALL custom sounds shipped (spear, mace, wind charge) and NOW ACTUALLY PLAYING (sound file names needed the `modernkit:` namespace prefix — fixed after user test). Wind Charge mechanics reworked to vanilla-1.21 behavior (fall-floor rule, ~6-block launch, gust ring visuals, full-volume burst). Obsidian door reverted to normal vanilla door sound on user request. Door icons redrawn in vanilla door-icon style. Waiting for user test, then commit on request.

- [x] **Rainbow Block name:** the `block.minecraft.rainbow_block` key was never generated in v1.5.9 (only slabs/doors/gear had keys). Added `"Rainbow Block"` to `assets/modernkit/lang/en_us.json` + `gen-v159-resources.js`.
- [x] **Redstone Slab works like a full block:** new `block/RedstoneSlabBlock.java extends SlabBlock`, override `getSidesShape` → `VoxelShapes.fullCube()` (this is what `SideShapeType.FULL` / `isSideSolidFullSquare` reads — gates dust placement + wire climbing). `ModBlocks.REDSTONE_SLAB` built with `AbstractBlock.Settings.solidBlock((state, world, pos) -> true)` (this is what `BlockState.isSolidBlock` evaluates — gates power conduction in `World.getEmittedRedstonePower`, i.e. pistons/components receiving power through the slab). Works on single and doubled states. Note: dust on a *single* bottom slab floats ~8px above the slab surface (dust always renders at the bottom of its own block position — same as vanilla's hopper exception); it still functions.
- [x] **Door item icons:** new `scripts/make_door_item_icons.py` → `assets/modernkit/textures/item/obsidian_door.png` (16×16, obsidian block panel + dark 2px frame + 2×2 transparent windows, hard alpha) and `rainbow_door.png` (16×160 animated, 10 frames, `frametime 3, interpolate true`) + mcmeta. Door item models now `layer0: modernkit:item/{obsidian,rainbow}_door`. Vanilla also uses a dedicated `item/oak_door.png` icon (not the block texture).
- [x] **Obsidian items fireproof:** all 8 gear items + obsidian slab/stairs/door BlockItems get `.fireproof()` (`ModBlocks.block()` gained a settings overload + `fireproofItem()` helper). Durability re-verified: tools 2048, armor ×40 multiplier.
- [x] **Custom obsidian door open/close sounds — DONE (synthesized with Python, user had no recordings).** `scripts/make_door_sounds.py` (numpy + soundfile; seed 20261117) writes `assets/modernkit/sounds/block/obsidian_door/open.ogg` (0.95s: latch clack, sub thump, stone grind with 2.7 Hz wobble, motion whoosh, settle thump) and `close.ogg` (0.75s: impact crack, 64 Hz body, rumble tail, faint glassy ping, sub). Mono 44.1 kHz OGG Vorbis, peak ~0.88. sounds.json keys `block.modernkit.obsidian_door.{open,close}`; events registered in `ModSounds` (`OBSIDIAN_DOOR_OPEN`/`OBSIDIAN_DOOR_CLOSE`).
  - Vanilla path (verified in bytecode): `DoorBlock.getOpenSoundEventId()/getCloseSoundEventId()` are **private**, return world-event ids by `Material` (METAL → 1011 / 1005, else wooden 1012 / 1006). **1.16.5 Yarn quirk found by decompiling the client's world-event lookupswitch: ids 1005/1006 actually play the door OPEN sounds and 1011/1012 the CLOSE sounds — so the two private getters' names are swapped relative to playback, and the boolean arg of `playOpenCloseSound` actually means "opening" (true → getCloseSoundEventId → 1005/1006 → open sound).** `DoorBlock.onUse` calls `World.syncWorldEvent` INLINE (offset 66, after setBlockState) — it does NOT route through `playOpenCloseSound`.
  - Fix (shipped): `mixin/DoorBlockMixin.java` — `@Redirect` on `onUse`'s `syncWorldEvent` call (obsidian door only: read `world.getBlockState(pos).get(DoorBlock.OPEN)` post-write; else forward vanilla unchanged) + `@Inject` HEAD-cancellable on `playOpenCloseSound(...Z)V` (obsidian door only: boolean = opening). Custom sound played via `world.playSound(null, pos, event, SoundCategory.BLOCKS, 1.0F, 1.0F)` guarded `!world.isClient` and cancelled on both sides → exactly one server-side broadcast, no double-play even if the client also runs these paths. Other 7 doors keep vanilla sounds.
- [x] **Spear sounds — DONE (user-supplied files, Nov 17 2026).** 9 user OGGs (mono 48 kHz) placed as `assets/modernkit/sounds/entity/spear/spear_attack{1,2,3}.ogg` / `spear_hit{1,2,3}.ogg` / `spear_lunge{1,2,3}.ogg` (hit1/hit3 re-encoded to peak ≤0.99, rest copied). sounds.json's 5 spear events switched from vanilla `type:event` refs to custom `type:file`: jab_start ← attack1-3, jab_impact ← hit1-3, charge_engaged ← lunge1, charge_tired ← lunge2, charge_disengaged ← lunge3 (lunge mapping is order-based — swap files if the user wants different order).
- [x] **Wind Charge sounds — DONE (user-supplied bursts + synthesized throw, Nov 17 2026).** User's 3 burst OGGs (mono 48 kHz, peaks ≤0.998, copied as-is) replaced the placeholder `assets/modernkit/sounds/entity/wind_charge/wind_burst{1,2,3}.ogg`. Throw event `item.modernkit.wind_charge.throw` was still a vanilla `type:event` ref (`minecraft:item.trident.throw`) — replaced with synthesized `entity/wind_charge/wind_throw.ogg` via new `scripts/make_wind_throw_sound.py` (0.50s airy "fwoof": 300–2400 Hz air band swell + delayed 120–600 Hz air mass + faint 140→300 Hz doppler body; mono 44.1 kHz peak 0.85; played in game at 0.5 vol / 0.9–1.1 pitch). sounds.json now has ZERO vanilla `type:event` refs. (`SmashAttack`'s shockwave has since been given its own `MACE_SMASH_AIR` event — see the Mace bullet below.)
- [x] **Wind Charge mechanics rework — DONE (Nov 17 2026), vanilla-1.21 behavior.** (1) Fall-floor rule: new `entity/WindChargeFloors.java` (static `Map<UUID,Double>`); `WindExplosion.knockbackNearby` calls `WindChargeFloors.setFloor(player, burstY)` for every pushed ServerPlayerEntity (after the velocity packet) — while feet Y ≥ floor, `fallDistance = 0` each tick; entry removed when player null / `!isAlive()` / `isOnGround()`. 1.16.5 Yarn gotchas hit here: `Entity.removed` is a public FIELD (no `isRemoved()`), `onGround` is protected (use `isOnGround()`), `fallDistance` is public. `ModernKitMod.onInitialize` registers `ServerTickEvents.END_SERVER_TICK.register(WindChargeFloors::tick)` (fabric-lifecycle-events-v1 1.2.2 on classpath; `EndTick.onEndTick(MinecraftServer)` signature matches). Only players get the floor — mobs keep fall damage (vanilla-faithful); the mace smash attacker is `excluded` from the radial burst so no floor (their successful smash already cancels fall damage). (2) Vertical knockback `0.45 + strength*0.65` → `0.35 + strength*0.45` (full strength ≈ 0.98 velocity ≈ ~6 blocks, vanilla number). (3) Particles: CLOUD×24 + POOF×8 replaced by `spawnGust()` — two flat CLOUD rings (r=0.7 @10 pts, r=1.5 @16 pts, explicit angles), rising center column (6 pts), 12 END_ROD edge sparks, soft base puff — plus `spawnRing()` helper. (4) `WindChargeEntity` burst volume 0.8F → 1.0F. Verified in jar: WindChargeFloors.class, END_SERVER_TICK invokedynamic in ModernKitMod, spawnGust/spawnRing methods, constants 0.35/0.45, setFloor invokestatic, no 0.65 / no 0.8F left.
- [x] **Post-test fixes — DONE (Nov 17 2026; user tested and reported ALL custom sounds silent + door icons not looking like real MC doors).** ROOT CAUSE of the silence: sounds.json file entries had names WITHOUT a namespace prefix (`"entity/mace/smash_ground1"`), and 1.16.5's `Sound` constructor does `new Identifier(name)` → plain names resolve to the MINECRAFT namespace, so the game looked for `minecraft:sounds/...` (missing) → every custom event silent. Confirmed by decompiling `net.minecraft.client.sound.Sound` + `SoundEntryDeserializer` + `Sound$RegistrationType` from the Yarn-named loom jar: enum names are literally `"file"`/`"event"` (so `"type":"file"` was VALID — not the bug), and real vanilla 1.16.5 sounds.json (fetched from Mojang CDN, not shipped in the game jar) uses 2164 plain strings / 1361 no-type objects / 33 `type:event` / ZERO `type:file`. FIX: rewrote all 10 sound entries as vanilla-canonical PLAIN STRINGS with `modernkit:` prefix (`"modernkit:entity/mace/smash_ground1"` etc.), zero type fields. **LESSON: every sounds.json file entry in a mod MUST be prefixed `modernkit:`.** Obsidian Door: user wanted normal sound → DoorBlockMixin DELETED (java file + mixins.json entry), OBSIDIAN_DOOR_OPEN/CLOSE removed from ModSounds + sounds.json, `sounds/block/` oggs deleted; door block is Material.STONE → vanilla now plays the normal wooden door sound (hand + redstone paths). Door icons: `scripts/make_door_item_icons.py` rewritten — vanilla oak door item sprite structure extracted pixel-by-pixel from the 1.16.5 jar (shades L/o/d/#; windows are TRANSPARENT holes; hinges col 3 rows 4+12; handle row 8 cols 10-11; recessed panels = L blocks rows 9-13; base row dark), recolored: obsidian purple-black ramp (77,55,116)/(56,39,90)/(36,24,62)/(13,5,24); rainbow = same template, hue-cycling 10 frames 16×160 (existing mcmeta frametime 3 + interpolate kept). Door item models already matched vanilla (`item/generated` + layer0) — untouched.
- [x] **Door item icons v2 — DONE (Nov 18 2026; user said the obsidian icon looked completely different from the placed door).** Rewrote `scripts/make_door_item_icons.py` to SAMPLE the actual placed-door block textures instead of using an invented palette: `assets/minecraft/textures/block/{obsidian,rainbow}_door_top.png` (upper icon half) + `_bottom.png` (lower icon half); rainbow ones are 16×160 animated (10 frames → 10 icon frames). Mapping: icon body cols 3-12 → texture cols 0-15, icon rows 2-8 → top texture rows 0-15, icon rows 9-15 → bottom texture rows 0-15; brightness multipliers L×1.45 / o×1.15 / d×0.85 (panel/edge readability); '#' hinge/handle pixels use the darkest color present in the texture (obsidian → #000000); sampled texture window holes fall back to nearest opaque row. Result verified upscaled beside the block textures (obsidian swirls + rainbow gradient now match the placed door). Jar re-verified: item textures byte-identical to source.
- [x] **Wind Charge mechanics round 3 — DONE (Nov 18 2026; user tested round 2, said the mechanics were "poor and not even good" → full reprogram, sounds kept).** ROOT CAUSE of the weak feel: `knockbackNearby` measured falloff from `entity.getBodyY(0.5)` (mid-body) instead of feet — a burst at the feet had distance 0.9/2.4 → strength only 0.625×base — and used a flat `0.35 + strength*0.45` vertical pop with horizontal-only direction; measured self-launch ≈3.4 blocks (vanilla ~6). ALSO `WindChargeFloors.tick` removed the floor entry when `player.isOnGround()`, which is TRUE on the burst tick itself (player still standing) → protection silently lost on every feet-burst → fall damage from the small hop. FIX (1): `knockbackNearby` rewritten to the vanilla 1.16.5 explosion loop (decompiled from the loom jar, `Explosion.collectBlocksAndDamageEntities`): distance = `sqrt(entity.squaredDistanceTo(center))` — feet-based `getY()`, divisor = radius (vanilla local f3 = power×2), direction = (pos − center) normalized using `entity.getEyeY()` for Y, `j = (1 − d) × Explosion.getExposure(center, entity)` — `getExposure(Vec3d, Entity)` is public static in 1.16.5, so line-of-sight now blocks pushes through walls; kept the existing Blast Protection dampener `1 − min(20, level*2)/25` + knockback-resistance attribute; `velocity.add(dx*j, dy*j, dz*j)`; the 0.35 base and 0.45 factor are gone from the method (verified by javap). (2): Wind charge epicenter base `1.4D → 1.1D` (`WindChargeEntity.KNOCKBACK`) → standing feet-burst ≈ 7 blocks up (vanilla physics sim `v'=(v−0.08)*0.98`, sum of positive velocities: v=1.0→5.9, 1.1→7.0, 1.22→8.4); a jump on top clears the 8-block "Who Needs Rockets?" height. Mace Wind Burst keeps base 1.4×multiplier (now vanilla-shaped too: primary victim ≈1.25 @I vs old ≈0.91). (3): `WindChargeFloors` rewritten with a `Floor` state class: zero `fallDistance` while `getY() >= floor.y`; entry removed only when `launched` (player left the ground at least once) && `isOnGround()` again (landed), or after a 40-tick grace if never launched, or on death/logout — never on the burst tick. (4): new projectile trail — `WindChargeEntity.tick()` with `world.isClient` guard → `spawnTrail()` = per-tick CLOUD spiral (r=0.22, phase `age*0.7`, y-bob `cos(age*0.45)*0.12`) + END_ROD every 4 ticks; spawned on every client that ticks the entity, so all players see it. Verified in `build/classes/java/main` (named): getEyeY/squaredDistanceTo/getExposure present in knockbackNearby, getBodyY gone, tick/spawnTrail/CLOUD/END_ROD present, Floor grace/launched fields present; rebuilt `dist/modernkit-1.5.97.jar` (BUILD SUCCESSFUL).
- [x] **Last vanilla spear sound removed:** `SpearMobCombat` mob charge impact played `SoundEvents.ITEM_TRIDENT_HIT` directly — now `ModSounds.SPEAR_JAB_IMPACT` (0.8F, pitch 0.9, HOSTILE), `SoundEvents` import dropped. Remaining direct vanilla `SoundEvents.` uses are legit: `WindExplosion` lever/trapdoor/fence-gate sounds (opened blocks' own sounds), armor material equip sounds, and `SmashAttack` mace smash sounds (anvil/explode/golem — pending user's Mace files).
- [x] **Mace sounds — DONE (user-supplied files, Nov 17 2026).** 8 user OGGs (mono 48 kHz; smash_ground1 re-encoded peak 1.028→0.99, smash_air1 0.998→0.99, smash_air3 0.994→0.99, rest copied) placed as `assets/modernkit/sounds/entity/mace/smash_ground{1,2,3,4}.ogg`, `smash_ground_heavy.ogg`, `smash_air{1,2,3}.ogg`. Mapping (function-based): `entity.modernkit.mace.smash_ground` ← ground1-4 = smash impact in `SmashAttack.doEffects` (replaced BLOCK_ANVIL_LAND, vol 1.0 / pitch 0.9+rand*0.2); `smash_ground_heavy` = extra layer when fall > 5 (replaced ENTITY_IRON_GOLEM_HURT, vol 1.0/1.0); `smash_air` ← air1-3 = Wind Burst launch shockwave in `windBurstLaunch` (replaced the WIND_CHARGE_BURST borrow, 1.0F / 0.9+rand*0.2 — also plays for mobs with Wind Burst maces via MobEntityMixin). The fake ENTITY_GENERIC_EXPLODE layer was DROPPED (user files are complete sounds). `SoundEvents` import removed from SmashAttack. ModSounds gained MACE_SMASH_GROUND / MACE_SMASH_GROUND_HEAVY / MACE_SMASH_AIR. If user meant smash_air for regular mace melee hits instead (vanilla 1.21 semantic), rewire PlayerEntityMixin hit path.
