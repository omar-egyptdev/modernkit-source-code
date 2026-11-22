#!/usr/bin/env node
'use strict';

const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');

const ROOT = path.resolve(__dirname, '..');
const PROPS = path.join(ROOT, 'gradle.properties');
const DIST = path.join(ROOT, 'dist');
const LIBS = path.join(ROOT, 'build', 'libs');
const LOG = path.join(ROOT, 'gradle-failed-build.log');

function getProp(text, key) {
  const m = text.match(new RegExp('^' + key + '\\s*=\\s*(.+?)\\s*$', 'm'));
  return m ? m[1] : null;
}

function main() {
  const properties = fs.readFileSync(PROPS, 'utf8');
  const base = getProp(properties, 'archives_base_name') || 'modernkit';
  const version = getProp(properties, 'mod_version');

  if (!version) {
    throw new Error('mod_version is missing from gradle.properties.');
  }

  console.log('Building ModernKit version: ' + version);
  console.log('The version is read directly from gradle.properties; it will not be auto-incremented.');

  const isWin = process.platform === 'win32';
  const gradlew = isWin ? 'gradlew.bat' : './gradlew';
  console.log('Running ' + gradlew + ' clean build ...');

  const res = spawnSync(gradlew, ['clean', 'build', '--console=plain'], {
    cwd: ROOT,
    encoding: 'utf8',
    shell: isWin,
    maxBuffer: 1024 * 1024 * 100
  });

  const output = (res.stdout || '') + (res.stderr || '');
  if (res.error || res.status !== 0) {
    const header =
      'ModernKit build failed\n' +
      'Time: ' + new Date().toISOString() + '\n' +
      'Target version: ' + version + '\n' +
      '==============================\n\n';
    fs.writeFileSync(LOG, header + output);
    const lines = output.split('\n');
    const errors = lines.filter((line) => /error|FAILED|What went wrong/i.test(line));
    console.error('\nBUILD FAILED');
    console.error((errors.length ? errors : lines).slice(-30).join('\n'));
    console.error('\nFull output saved to gradle-failed-build.log');
    process.exit(1);
  }

  const jarName = base + '-' + version + '.jar';
  const builtJar = path.join(LIBS, jarName);
  if (!fs.existsSync(builtJar)) {
    throw new Error('Gradle reported success, but the expected JAR was not found: ' + builtJar);
  }

  fs.mkdirSync(DIST, { recursive: true });
  fs.copyFileSync(builtJar, path.join(DIST, jarName));
  if (fs.existsSync(LOG)) fs.unlinkSync(LOG);

  console.log('\nBUILD SUCCESSFUL');
  console.log('Created dist/' + jarName);
  console.log('Existing JARs in dist/ were preserved.');
}

main();
