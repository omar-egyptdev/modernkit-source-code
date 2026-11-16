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
