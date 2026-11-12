#!/usr/bin/env node
'use strict';

/**
 * npm run build:java2
 *
 * Like build:java, but instead of auto-bumping the version it asks for it:
 *
 *   1. Prompts: "Put the version of the new JAR you want it to be: "
 *   2. Writes that version into gradle.properties (mod_version), so the built
 *      jar's real version - its file name AND its internal fabric.mod.json
 *      metadata - is exactly the version that was typed in.
 *   3. Runs `gradlew clean build`.
 *   4. On success: copies the jar to dist/ (old jars there are removed) and
 *      deletes any leftover failed-build.log.
 *   5. On failure: restores gradle.properties to the previous version and
 *      writes the full build output (the errors) to failed-build.log.
 */

const fs = require('fs');
const path = require('path');
const readline = require('readline');
const { spawnSync } = require('child_process');

const ROOT = path.resolve(__dirname, '..');
const PROPS = path.join(ROOT, 'gradle.properties');
const DIST = path.join(ROOT, 'dist');
const LIBS = path.join(ROOT, 'build', 'libs');
const LOG = path.join(ROOT, 'failed-build.log');

function escapeRe(s) {
  return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function getProp(text, key) {
  const m = text.match(new RegExp('^' + key + '\\s*=\\s*(.+?)\\s*$', 'm'));
  return m ? m[1] : null;
}

function setProp(text, key, value) {
  return text.replace(new RegExp('^(' + key + '\\s*=).*$', 'm'), (m, p1) => p1 + value);
}

function askVersion(rl) {
  return new Promise((resolve) => {
    rl.question('Put the version of the new JAR you want it to be: ', (answer) => {
      resolve((answer || '').trim());
    });
  });
}

async function main() {
  const original = fs.readFileSync(PROPS, 'utf8');
  const base = getProp(original, 'archives_base_name') || 'modernkit';
  const currentVersion = getProp(original, 'mod_version') || '1.0.0';

  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
  });

  let version = null;

  for (;;) {
    const answer = await askVersion(rl);

    if (!answer) {
      rl.close();
      console.error('No version given. Aborting (gradle.properties unchanged).');
      process.exit(1);
    }

    if (!/^\d+\.\d+\.\d+$/.test(answer)) {
      console.error('"' + answer + '" is not a valid version. Use three numbers, like 1.5.7.');
      continue;
    }

    version = answer;
    break;
  }

  rl.close();

  // The real version, not just the file name: build.gradle reads mod_version
  // for the project version and expands it into fabric.mod.json inside the jar.
  fs.writeFileSync(PROPS, setProp(original, 'mod_version', version));
  console.log('Building ' + base + ' version ' + version + ' (mod_version set in gradle.properties) ...');

  let ok = false;
  let output = '';

  try {
    const isWin = process.platform === 'win32';
    const gradlew = isWin ? 'gradlew.bat' : './gradlew';
    console.log('Running ' + gradlew + ' clean build ...');

    const res = spawnSync(gradlew, ['clean', 'build', '--console=plain'], {
      cwd: ROOT,
      encoding: 'utf8',
      shell: isWin,
      maxBuffer: 1024 * 1024 * 100
    });

    output = (res.stdout || '') + (res.stderr || '');
    if (res.error) output += '\n' + String(res.error);
    ok = res.status === 0 && !res.error;

    if (ok) {
      const jar = path.join(LIBS, base + '-' + version + '.jar');
      if (!fs.existsSync(jar)) {
        ok = false;
        output += '\nBuild reported success but the jar was not found: ' + jar + '\n';
      } else {
        fs.mkdirSync(DIST, { recursive: true });
        for (const file of fs.readdirSync(DIST)) {
          if (file.endsWith('.jar')) fs.unlinkSync(path.join(DIST, file));
        }
        fs.copyFileSync(jar, path.join(DIST, path.basename(jar)));
        if (fs.existsSync(LOG)) fs.unlinkSync(LOG);
        console.log('\nBUILD SUCCESSFUL');
        console.log('dist/' + path.basename(jar) + ' is the new jar (old jars removed).');
      }
    }
  } finally {
    // A failed build must not leave a changed version behind.
    if (!ok) fs.writeFileSync(PROPS, original);
  }

  if (!ok) {
    const header =
      'ModernKit build failed\n' +
      'Time: ' + new Date().toISOString() + '\n' +
      'Attempted version: ' + version + ' (gradle.properties restored to ' + currentVersion + ')\n' +
      '==============================\n\n';
    fs.writeFileSync(LOG, header + output);

    const lines = output.split('\n');
    const errors = lines.filter((l) => /error|FAILED|What went wrong/i.test(l));
    console.error('\nBUILD FAILED');
    console.error((errors.length ? errors : lines).slice(-20).join('\n'));
    console.error('\nFull output saved to failed-build.log');
    process.exit(1);
  }
}

main();
