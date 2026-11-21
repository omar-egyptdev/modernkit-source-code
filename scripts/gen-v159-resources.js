#!/usr/bin/env node
// Generates all v1.5.9 blockstates, block/item models, recipes and loot tables.
// Run from repo root:  node scripts/gen-v159-resources.js
const fs = require('fs');
const path = require('path');

const RES = 'src/main/resources';
const A_MC = path.join(RES, 'assets/minecraft');
const A_MK = path.join(RES, 'assets/modernkit');
const D_MC = path.join(RES, 'data/minecraft');
const D_MK = path.join(RES, 'data/modernkit');

const write = (p, obj) => {
  fs.mkdirSync(path.dirname(p), { recursive: true });
  fs.writeFileSync(p, JSON.stringify(obj, null, 2) + '\n');
};

// ---------------------------------------------------------------- definitions
const SLABS = [
  { name: 'obsidian',  tex: 'minecraft:block/obsidian_block',   dbl: 'minecraft:block/obsidian_block' },
  { name: 'rainbow',   tex: 'minecraft:block/rainbow_block',    dbl: 'minecraft:block/rainbow_block' },
  { name: 'gold',      tex: 'minecraft:block/gold_block',       dbl: 'minecraft:block/gold_block' },
  { name: 'diamond',   tex: 'minecraft:block/diamond_block',    dbl: 'minecraft:block/diamond_block' },
  { name: 'emerald',   tex: 'minecraft:block/emerald_block',    dbl: 'minecraft:block/emerald_block' },
  { name: 'coal',      tex: 'minecraft:block/coal_block',       dbl: 'minecraft:block/coal_block' },
  { name: 'redstone',  tex: 'minecraft:block/redstone_block',   dbl: 'minecraft:block/redstone_block' },
];
const DOORS = [
  { name: 'obsidian', bottom: 'minecraft:block/obsidian_door_bottom', top: 'minecraft:block/obsidian_door_top' },
  { name: 'rainbow',  bottom: 'minecraft:block/rainbow_door_bottom',  top: 'minecraft:block/rainbow_door_top' },
];
const GEAR = ['sword', 'pickaxe', 'axe', 'shovel', 'helmet', 'chestplate', 'leggings', 'boots'];
const HANDHELD = ['sword', 'pickaxe', 'axe', 'shovel'];

// ------------------------------------------------------------------ blockstates
for (const s of SLABS) {
  write(path.join(A_MC, `blockstates/${s.name}_slab.json`), {
    variants: {
      'type=bottom': { model: `minecraft:block/${s.name}_slab` },
      'type=top': { model: `minecraft:block/${s.name}_slab_top` },
      'type=double': { model: s.dbl },
    },
  });
}
write(path.join(A_MC, 'blockstates/obsidian_stairs.json'),
  JSON.parse(fs.readFileSync('/tmp/opencode/vanilla/assets/minecraft/blockstates/oak_stairs.json', 'utf8')
    .replace(/oak_stairs/g, 'obsidian_stairs')));
for (const d of DOORS) {
  write(path.join(A_MC, `blockstates/${d.name}_door.json`),
    JSON.parse(fs.readFileSync('/tmp/opencode/vanilla/assets/minecraft/blockstates/oak_door.json', 'utf8')
      .replace(/oak_door/g, `${d.name}_door`)));
}
write(path.join(A_MC, 'blockstates/rainbow_block.json'), {
  variants: { '': { model: 'minecraft:block/rainbow_block' } },
});

// ---------------------------------------------------------------- block models
write(path.join(A_MC, 'models/block/obsidian_block.json'),
  { parent: 'block/cube_all', textures: { all: 'minecraft:block/obsidian_block' } });
write(path.join(A_MC, 'models/block/rainbow_block.json'),
  { parent: 'block/cube_all', textures: { all: 'minecraft:block/rainbow_block' } });
for (const s of SLABS) {
  write(path.join(A_MC, `models/block/${s.name}_slab.json`), {
    parent: 'block/slab',
    textures: { bottom: s.tex, top: s.tex, side: s.tex },
  });
  write(path.join(A_MC, `models/block/${s.name}_slab_top.json`), {
    parent: 'block/slab_top',
    textures: { bottom: s.tex, top: s.tex, side: s.tex },
  });
}
for (const m of ['', '_inner', '_outer']) {
  const parent = m === '' ? 'block/stairs' : `block${m}_stairs`;
  write(path.join(A_MC, `models/block/obsidian_stairs${m}.json`), {
    parent,
    textures: { bottom: 'minecraft:block/obsidian_block', top: 'minecraft:block/obsidian_block', side: 'minecraft:block/obsidian_block' },
  });
}
for (const d of DOORS) {
  for (const [suffix, parent] of [
    ['bottom', 'block/door_bottom'],
    ['bottom_hinge', 'block/door_bottom_rh'],
    ['top', 'block/door_top'],
    ['top_hinge', 'block/door_top_rh'],
  ]) {
    write(path.join(A_MC, `models/block/${d.name}_door_${suffix}.json`), {
      parent,
      textures: { bottom: d.bottom, top: d.top },
    });
  }
}

// ----------------------------------------------------------------- item models
for (const g of GEAR) {
  write(path.join(A_MC, `models/item/obsidian_${g}.json`), {
    parent: HANDHELD.includes(g) ? 'item/handheld' : 'item/generated',
    textures: { layer0: `modernkit:item/obsidian_${g}` },
  });
}
for (const s of SLABS) write(path.join(A_MC, `models/item/${s.name}_slab.json`),
  { parent: `minecraft:block/${s.name}_slab` });
write(path.join(A_MC, 'models/item/obsidian_stairs.json'), { parent: 'minecraft:block/obsidian_stairs' });
write(path.join(A_MC, 'models/item/rainbow_block.json'), { parent: 'minecraft:block/rainbow_block' });
for (const d of DOORS) write(path.join(A_MC, `models/item/${d.name}_door.json`), {
  parent: 'item/generated',
  textures: { layer0: `modernkit:item/${d.name}_door` },
});

// --------------------------------------------------------------------- recipes
const recipe = (name, json) => write(path.join(D_MK, `recipes/${name}.json`), json);
const item = (k, id) => ({ [k]: { item: id } });
const shaped = (pattern, key, result, count = 1) =>
  ({ type: 'minecraft:crafting_shaped', pattern, key, result: { item: result, count } });

const O = item('O', 'minecraft:obsidian'), S = item('S', 'minecraft:stick');
recipe('obsidian_sword', shaped(['O', 'O', 'S'], { ...O, ...S }, 'minecraft:obsidian_sword'));
recipe('obsidian_pickaxe', shaped(['OOO', ' S ', ' S '], { ...O, ...S }, 'minecraft:obsidian_pickaxe'));
recipe('obsidian_axe', shaped(['OO', 'OS', ' S'], { ...O, ...S }, 'minecraft:obsidian_axe'));
recipe('obsidian_shovel', shaped(['O', 'S', 'S'], { ...O, ...S }, 'minecraft:obsidian_shovel'));
recipe('obsidian_helmet', shaped(['OOO', 'O O'], { ...O }, 'minecraft:obsidian_helmet'));
recipe('obsidian_chestplate', shaped(['O O', 'OOO', 'OOO'], { ...O }, 'minecraft:obsidian_chestplate'));
recipe('obsidian_leggings', shaped(['OOO', 'O O', 'O O'], { ...O }, 'minecraft:obsidian_leggings'));
recipe('obsidian_boots', shaped(['O O', 'O O'], { ...O }, 'minecraft:obsidian_boots'));

recipe('obsidian_slab', shaped(['OOO'], { ...O }, 'minecraft:obsidian_slab', 6));
recipe('obsidian_stairs', shaped(['O  ', 'OO ', 'OOO'], { ...O }, 'minecraft:obsidian_stairs', 4));
recipe('obsidian_door', shaped(['OO', 'OO', 'OO'], { ...O }, 'minecraft:obsidian_door', 3));

const R = item('R', 'minecraft:rainbow_block');
recipe('rainbow_block', shaped(['III', 'III', 'III'], item('I', 'minecraft:rainbow_ingot'), 'minecraft:rainbow_block'));
recipe('rainbow_slab', shaped(['RRR'], { ...R }, 'minecraft:rainbow_slab', 6));
recipe('rainbow_door', shaped(['RR', 'RR', 'RR'], { ...R }, 'minecraft:rainbow_door', 3));

for (const s of SLABS.filter(s => !['obsidian', 'rainbow'].includes(s.name))) {
  const X = item('X', `minecraft:${s.name}_block`);
  recipe(`${s.name}_slab`, shaped(['XXX'], { ...X }, `minecraft:${s.name}_slab`, 6));
}

// ----------------------------------------------------------------- loot tables
const simpleLoot = (name) => write(path.join(D_MC, `loot_tables/blocks/${name}.json`), {
  type: 'minecraft:block',
  pools: [{
    rolls: 1,
    entries: [{ type: 'minecraft:item', name: `minecraft:${name}` }],
    conditions: [{ condition: 'minecraft:survives_explosion' }],
  }],
});
simpleLoot('heavy_core');
simpleLoot('rainbow_block');
simpleLoot('obsidian_stairs');

const vanillaSlabLoot = JSON.parse(fs.readFileSync('/tmp/opencode/vanilla/data/minecraft/loot_tables/blocks/oak_slab.json', 'utf8'));
const vanillaDoorLoot = JSON.parse(fs.readFileSync('/tmp/opencode/vanilla/data/minecraft/loot_tables/blocks/oak_door.json', 'utf8'));
for (const s of SLABS) {
  write(path.join(D_MC, `loot_tables/blocks/${s.name}_slab.json`),
    JSON.parse(JSON.stringify(vanillaSlabLoot).replace(/oak_slab/g, `${s.name}_slab`)));
}
for (const d of DOORS) {
  write(path.join(D_MC, `loot_tables/blocks/${d.name}_door.json`),
    JSON.parse(JSON.stringify(vanillaDoorLoot).replace(/oak_door/g, `${d.name}_door`)));
}

// ------------------------------------------------------------------- lang keys
const langPath = path.join(A_MK, 'lang/en_us.json');
const lang = JSON.parse(fs.readFileSync(langPath, 'utf8'));
const title = (w) => w[0].toUpperCase() + w.slice(1);
for (const g of GEAR) lang[`item.minecraft.obsidian_${g}`] = `Obsidian ${title(g)}`;
for (const s of SLABS) lang[`block.minecraft.${s.name}_slab`] = `${title(s.name)} Slab`;
lang['block.minecraft.obsidian_stairs'] = 'Obsidian Stairs';
lang['block.minecraft.rainbow_block'] = 'Rainbow Block';
for (const d of DOORS) lang[`block.minecraft.${d.name}_door`] = `${title(d.name)} Door`;
fs.writeFileSync(langPath, JSON.stringify(lang, null, 2) + '\n');

console.log('generated: blockstates, models, recipes, loot tables, lang');
