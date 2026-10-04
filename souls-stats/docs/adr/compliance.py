#!/usr/bin/env python3
"""Checks de cumplimiento de los ADRs de souls-stats. Corre en pre-commit."""
import json
import re
import sys
from pathlib import Path

MOD = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(MOD.parent / 'docs/adr'))
from compliance import hits, java  # noqa: E402


def adr_0001():
    table = json.loads((MOD / 'src/main/resources/data/soulsstats/mob_xp.json').read_text())
    return [f'souls-stats ADR-0001: mob_xp.json {k}: {v} debe ser "namespace:id": {{"xp": entero >= 0, "max_level": entero > 0 opcional}}'
            for k, v in table.items()
            if not re.fullmatch(r'[a-z0-9_.-]+:[a-z0-9_./-]+', k)
            or not re.fullmatch(r'\{"xp": \d+(, "max_level": [1-9]\d*)?\}', json.dumps(v))]


def adr_0007():
    table = json.loads((MOD / 'src/main/resources/data/soulsstats/items.json').read_text())
    def valid(v):
        return isinstance(v, dict) and set(v) <= {'weight', 'stats', 'requires', 'scaling', 'usable_at_level', 'equipable_at_level'} \
            and isinstance(v.get('weight', 0), (int, float)) and v.get('weight', 0) >= 0 \
            and all(isinstance(n, int) for n in [*v.get('stats', {}).values(), *v.get('requires', {}).values()]) \
            and all(isinstance(n, (int, float)) for n in v.get('scaling', {}).values()) \
            and all(isinstance(v.get(k, 1), int) and v.get(k, 1) > 0 for k in ('usable_at_level', 'equipable_at_level'))
    return [f'souls-stats ADR-0007: items.json {k}: {v} debe ser "namespace:id": {{"weight": n >= 0, "stats": {{"<stat>": entero}}}}'
            for k, v in table.items() if not re.fullmatch(r'[a-z0-9_.-]+:[a-z0-9_./-]+', k) or not valid(v)]


def adr_0006():
    config = json.loads((MOD / 'src/main/resources/data/soulsstats/config.json').read_text())
    fields = set(re.findall(r'fieldOf\("(\w+)"\)', (MOD / 'src/main/java/soulsstats/data/Config.java').read_text()))
    shapes = {'linear': {'per_point'}, 'power': {'per_point', 'exponent'}, 'logarithmic': {'scale'},
              'hyperbolic': {'max', 'half'}, 'soft_caps': {'steps'}}
    errors = [f'souls-stats ADR-0006: config.json no trae {f}' for f in sorted(fields - config.keys())]
    errors += [f'souls-stats ADR-0006: config.json trae {f}, que Config no lee' for f in sorted(config.keys() - fields)]
    for key, value in config.items():
        if isinstance(value, (int, float)) or isinstance(value, dict) and 'type' not in value \
                and all(isinstance(n, (int, float)) for n in value.values()):
            continue
        required = shapes.get(value.get('type')) if isinstance(value, dict) else None
        if required is None or not required <= set(value) <= required | {'type', 'base'}:
            errors.append(f'souls-stats ADR-0006: config.json {key}: {value} debe ser un número o una escala con "type" '
                          f'({", ".join(shapes)}) y sus campos')
    return errors


def adr_0005():
    files = {f: src for f, src in java(MOD).items() if not {'command', 'display'} & set(f.parts)}
    pattern = r'\b(?:Component|ChatFormatting)\.|\b(?:sendSystemMessage|sendSuccess|sendFailure|displayClientMessage)\b'
    return [f'souls-stats {at}: lo que ve el jugador va en command/ o display/' for at, _ in hits('ADR-0005', files, pattern)]


errors = adr_0001() + adr_0005() + adr_0006() + adr_0007()
print('\n'.join(errors) or 'OK: ADRs de souls-stats cumplidos')
sys.exit(1 if errors else 0)
