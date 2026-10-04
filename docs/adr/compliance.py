#!/usr/bin/env python3
"""Checks de cumplimiento de ADRs. Corre en pre-commit (.githooks/pre-commit); sale con 1 si alguno falla."""
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MODS = sorted(p.parents[3] for p in ROOT.glob('*/src/main/resources/fabric.mod.json'))
errors = []


def meta(mod):
    return json.loads((mod / 'src/main/resources/fabric.mod.json').read_text())


def java(mod):
    return {f: f.read_text() for f in sorted((mod / 'src').rglob('*.java'))}


def adr_0006():
    for mod in MODS:
        props = mod / 'gradle.properties'
        if not props.exists() or not re.search(r'^version=', props.read_text(), re.M):
            errors.append(f'ADR-0006 {mod.name}: falta gradle.properties con version=')
        for f in ('build.gradle', 'settings.gradle'):
            if (mod / f).exists():
                errors.append(f'ADR-0006 {mod.name}/{f}: el build se define solo en la raíz')


def adr_0002():
    for mod in MODS:
        lines = [l.strip() for src in java(mod).values() for l in src.splitlines() if l.strip()]
        comments = sum(l.startswith(('//', '/*', '*')) for l in lines)
        print(f'ADR-0002 métrica {mod.name}: {len(lines) - comments} líneas de código, {comments} de comentarios')


def adr_0003():
    for mod in MODS:
        modid = meta(mod)['id']
        res = mod / 'src/main/resources'
        registered = set(re.findall(rf'fromNamespaceAndPath\("{modid}", "(\w+)"\)', ''.join(java(mod).values())))
        jsons = {f: f.read_text() for f in res.rglob('*.json')}
        for kind in ('assets/{m}/blockstates', 'assets/{m}/items', 'data/{m}/loot_table/blocks'):
            for f in (res / kind.format(m=modid)).glob('*.json'):
                if f.stem not in registered:
                    errors.append(f'ADR-0003 {f.relative_to(ROOT)}: no hay bloque ni ítem {modid}:{f.stem}')
        for lang in (res / f'assets/{modid}/lang').glob('*.json'):
            for key in json.loads(jsons[lang]):
                if key.split('.')[-1] not in registered:
                    errors.append(f'ADR-0003 {lang.relative_to(ROOT)}: {key} no corresponde a nada registrado')
        for kind in ('models', 'textures'):
            base = res / f'assets/{modid}/{kind}'
            for f in base.rglob('*.*'):
                rid = f'{modid}:{f.relative_to(base).with_suffix("").as_posix()}'
                if not any(rid in text for other, text in jsons.items() if other != f):
                    errors.append(f'ADR-0003 {f.relative_to(ROOT)}: nadie usa {rid}')


def adr_0005():
    expected = {'minecraft': '${minecraft_dependency}', 'fabricloader': '>=${loader_version}', 'java': '>=25'}
    for mod in MODS:
        depends = meta(mod).get('depends', {})
        for dep, value in expected.items():
            if depends.get(dep) != value:
                errors.append(f'ADR-0005 {mod.name}: depends.{dep} debe ser "{value}"')


for check in (adr_0002, adr_0003, adr_0005, adr_0006):
    check()
print('\n'.join(errors) or f'OK: ADRs cumplidos en {len(MODS)} mod(s)')
sys.exit(1 if errors else 0)
