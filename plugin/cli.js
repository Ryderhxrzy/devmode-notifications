#!/usr/bin/env node

const fs = require('node:fs');
const path = require('node:path');

const command = process.argv[2] || 'doctor';
const projectRoot = process.cwd();

function readJson(file) {
  return JSON.parse(fs.readFileSync(file, 'utf8'));
}

function inspectProject() {
  const appJsonPath = path.join(projectRoot, 'app.json');
  const packagePath = path.join(projectRoot, 'package.json');
  const findings = [];
  if (!fs.existsSync(packagePath)) findings.push(['error', 'package.json was not found.']);
  if (!fs.existsSync(appJsonPath)) {
    findings.push(['warn', 'app.json was not found. Configure app.config.js/ts manually.']);
    return { findings };
  }

  const document = readJson(appJsonPath);
  const expo = document.expo || document;
  const plugins = expo.plugins || [];
  const hasPlugin = plugins.some(plugin => plugin === '@devmode/notifications' || (Array.isArray(plugin) && plugin[0] === '@devmode/notifications'));
  if (!hasPlugin) findings.push(['error', '@devmode/notifications is missing from expo.plugins.']);
  const servicesFile = expo.android?.googleServicesFile;
  if (!servicesFile) findings.push(['error', 'expo.android.googleServicesFile is not configured.']);
  else if (!fs.existsSync(path.resolve(projectRoot, servicesFile))) findings.push(['error', `${servicesFile} does not exist.`]);
  else if (expo.android?.package) {
    const services = readJson(path.resolve(projectRoot, servicesFile));
    const packages = (services.client || []).map(client => client.client_info?.android_client_info?.package_name).filter(Boolean);
    if (!packages.includes(expo.android.package)) {
      findings.push(['error', `google-services.json does not contain Android package ${expo.android.package}.`]);
    }
  }
  return { findings, document, appJsonPath, expo };
}

function setup() {
  const result = inspectProject();
  if (!result.document) {
    console.error('Automatic setup currently supports app.json. Add the plugin manually in dynamic Expo configs.');
    process.exitCode = 1;
    return;
  }
  const expo = result.expo;
  expo.plugins = expo.plugins || [];
  const hasPlugin = expo.plugins.some(plugin => plugin === '@devmode/notifications' || (Array.isArray(plugin) && plugin[0] === '@devmode/notifications'));
  if (!hasPlugin) expo.plugins.push('@devmode/notifications');
  fs.writeFileSync(result.appJsonPath, `${JSON.stringify(result.document, null, 2)}\n`);
  console.log('Added @devmode/notifications to app.json.');
  if (!expo.android?.googleServicesFile) console.log('Next: set expo.android.googleServicesFile to your Firebase google-services.json.');
}

function doctor() {
  const { findings } = inspectProject();
  if (!findings.length) {
    console.log('devmode-notifications doctor: configuration looks valid.');
    return;
  }
  findings.forEach(([level, message]) => console.log(`${level.toUpperCase()}: ${message}`));
  if (findings.some(([level]) => level === 'error')) process.exitCode = 1;
}

if (command === 'setup') setup();
else if (command === 'doctor') doctor();
else {
  console.error(`Unknown command: ${command}. Use setup or doctor.`);
  process.exitCode = 1;
}
