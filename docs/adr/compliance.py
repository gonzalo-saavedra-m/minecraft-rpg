#!/usr/bin/env python3
"""Checks de cumplimiento de ADRs. Corre en pre-commit (.githooks/pre-commit); sale con 1 si alguno falla.

Los checks de Java reciben {archivo: texto} y retornan errores, para probarlos en test_compliance.py. Una
violación se salta con `// adr-skip ADR-NNNN: <motivo>` en su línea o la anterior (ADR-0005)."""
import json
import re
import sys
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MODS = sorted(p.parents[3] for p in ROOT.glob('*/src/main/resources/fabric.mod.json'))
TOKENS = re.compile(r'"""[\s\S]*?"""|"(?:\\.|[^"\\\n])*"|\'(?:\\.|[^\'\\\n])*\'|//[^\n]*|/\*[\s\S]*?\*/')
KEYWORDS = r'(?!(?:return|new|throw|else|yield|case|assert|break|continue|package|import|record|class|interface|enum|' \
           r'public|protected|private|static|final|abstract|synchronized|default|native|transient|volatile)\b)'
TYPE = r'[\w.]+(?:<[^;{}()=]*>)?(?:\[\])*'
METHOD = re.compile(rf'^[ \t]*(?:(?:public|protected|private|static|final|abstract|synchronized|default|native)\s+)*'
                    rf'(?:<[^>]*>\s+)?{KEYWORDS}(?P<type>{TYPE})\s+(?P<name>\w+)\s*\(', re.M)
EXEMPT = re.compile(r'@(?:Override|Inject|Redirect|Modify\w*|WrapOperation|Accessor|Invoker|Shadow)\b')


def meta(mod):
    return json.loads((mod / 'src/main/resources/fabric.mod.json').read_text())


def java(mod):
    return {f: f.read_text() for f in sorted((mod / 'src').rglob('*.java'))}


def code(src):
    """El código sin comentarios ni contenido de strings, con los mismos saltos de línea y posiciones."""
    def blank(m):
        t = m.group()
        keep = t[0] in '"\''
        return t[0] + re.sub(r'[^\n]', ' ', t[1:-1]) + t[-1] if keep else re.sub(r'[^\n]', ' ', t)
    return TOKENS.sub(blank, src)


def hits(adr, files, pattern):
    """(ubicación, match) de cada match de pattern en el código que no tenga adr-skip de adr."""
    for f, src in files.items():
        body = code(src)
        for m in re.finditer(pattern, body):
            n = body.count('\n', 0, m.start(m.lastindex or 0))
            if not skipped(src, n, adr):
                yield f'{adr} {rel(f)}:{n + 1}', m


def skipped(src, n, adr):
    return any(re.search(rf'adr-skip {adr}:\s*\S', l) for l in src.splitlines()[max(n - 1, 0):n + 1])


def rel(f):
    return f.relative_to(ROOT) if f.is_relative_to(ROOT) else f


def adr_0001(mod):
    props = mod / 'gradle.properties'
    errors = [] if props.exists() and re.search(r'^version=', props.read_text(), re.M) else [
        f'ADR-0001 {mod.name}: falta gradle.properties con version=']
    return errors + [f'ADR-0001 {mod.name}/{f}: el build se define solo en la raíz'
                     for f in ('build.gradle', 'settings.gradle') if (mod / f).exists()]


def adr_0002(mod):
    lines = [l.strip() for src in java(mod).values() for l in src.splitlines() if l.strip()]
    comments = sum(l.startswith(('//', '/*', '*')) for l in lines)
    print(f'ADR-0002 métrica {mod.name}: {len(lines) - comments} líneas de código, {comments} de comentarios')
    return []


def adr_0003(mod):
    modid, res = meta(mod)['id'], mod / 'src/main/resources'
    files = java(mod)
    registered = set(re.findall(rf'fromNamespaceAndPath\("{modid}", "(\w+)"\)', ''.join(files.values())))
    jsons = {f: f.read_text() for f in res.rglob('*.json')}
    errors = dead_java(files, ''.join(t for f, t in jsons.items() if f.name.endswith(('fabric.mod.json', 'mixins.json'))))
    for kind in ('assets/{m}/blockstates', 'assets/{m}/items', 'data/{m}/loot_table/blocks'):
        for f in (res / kind.format(m=modid)).glob('*.json'):
            if f.stem not in registered:
                errors.append(f'ADR-0003 {f.relative_to(ROOT)}: no hay bloque ni ítem {modid}:{f.stem}')
    literals = set(re.findall(r'"((?:\\.|[^"\\\n])*)"', ''.join(files.values())))
    # Claves armadas en el código ("stat." + id); el prefijo del modid solo no cuenta, lo pone Text.tr a todas.
    prefixes = tuple(l for l in literals if l.endswith('.') and l != f'{modid}.')
    for lang in (res / f'assets/{modid}/lang').glob('*.json'):
        for key in json.loads(jsons[lang]):
            short = key.removeprefix(f'{modid}.')
            if key.split('.')[-1] not in registered and not {key, short} & literals and not key.startswith(prefixes) \
                    and not short.startswith(prefixes):
                errors.append(f'ADR-0003 {lang.relative_to(ROOT)}: {key} no corresponde a nada registrado ni se usa en el código')
    for kind in ('models', 'textures'):
        base = res / f'assets/{modid}/{kind}'
        for f in base.rglob('*.*'):
            rid = f'{modid}:{f.relative_to(base).with_suffix("").as_posix()}'
            if not any(rid in text for other, text in jsons.items() if other != f):
                errors.append(f'ADR-0003 {f.relative_to(ROOT)}: nadie usa {rid}')
    return errors


def dead_java(files, metadata=''):
    """ADR-0003 en Java: imports sin uso, declaraciones cuyo nombre aparece una sola vez y código comentado."""
    corpus = ''.join(map(code, files.values()))
    uses = Counter(re.findall(r'\w+', corpus + metadata))
    iterated = set(re.findall(r'\b(\w+)(?:\.values\(\)|::values\b)', corpus))  # sus constantes se usan vía values()
    declarations = (r'\b(?:class|interface|enum|record)\s+(?P<name>\w+)', METHOD,
                    rf'\b{KEYWORDS}{TYPE}\s+(?P<name>\w+)\s*[=;]',
                    r'(?:\benum\s+\w+[^{]*\{|,)\s*(?P<name>[A-Z]\w*)\s*(?=[,;}(])')
    errors = []
    for f, src in files.items():
        body = code(src)
        constants = [e.span(2) for e in re.finditer(r'\benum\s+(\w+)[^{]*\{([^;}]*)', body) if e[1] in iterated]
        for at, m in hits('ADR-0003', {f: src}, r'(?m)^import\s+(?:static\s+)?[\w.]+\.(\w+)\s*;'):
            if len(re.findall(rf'\b{m[1]}\b', body)) == 1:
                errors.append(f'{at}: import sin uso de {m[1]}')
        for pattern in declarations:
            for at, m in hits('ADR-0003', {f: src}, pattern):
                prefix = body[max(body.rfind(c, 0, m.start()) for c in ';{}') + 1:m.start()]
                inside = body.count('(', 0, m.start()) > body.count(')', 0, m.start())
                listed = any(start <= m.start('name') < end for start, end in constants)
                if uses[m['name']] == 1 and not EXEMPT.search(prefix) and not inside and not listed:
                    errors.append(f'{at}: {m["name"]} no se usa')
        for c in TOKENS.finditer(src):
            n = src.count('\n', 0, c.start())
            if c.group().startswith('//') and c.group().rstrip()[-1] in ';{}' and not skipped(src, n, 'ADR-0003'):
                errors.append(f'ADR-0003 {rel(f)}:{n + 1}: código comentado')
    return errors


def adr_0009(mod):
    """Todos los idiomas de un mod (salvo en_us, cuyo inglés está en el código) traducen las mismas claves."""
    langs = {f: set(json.loads(f.read_text())) for f in (mod / 'src/main/resources/assets').glob('*/lang/*.json') if f.stem != 'en_us'}
    every = set().union(*langs.values()) if langs else set()
    return [f'ADR-0009 {rel(f)}: faltan {", ".join(sorted(every - keys))}' for f, keys in langs.items() if every - keys]


def adr_0004(mod):
    expected = {'minecraft': '${minecraft_dependency}', 'fabricloader': '>=${loader_version}', 'java': '>=25'}
    depends = meta(mod).get('depends', {})
    return [f'ADR-0004 {mod.name}: depends.{dep} debe ser "{value}"' for dep, value in expected.items()
            if depends.get(dep) != value]


def adr_0006(files):
    pattern = r'\binstanceof\b|\b(?:getClass|isInstance|isAssignableFrom)\b|' \
              r'\bcase\s+[A-Z][\w.]*(?:<[^>]*>)?\s*(?:\(|[a-z_]\w*\s*(?:->|:|when\b))'
    return [f'{at}: inspección de tipos fuera de un proxy marcado' for at, _ in hits('ADR-0006', files, pattern)]


def adr_0007(files):
    errors = []
    for f, src in files.items():
        body = code(src)
        for at, m in hits('ADR-0007', {f: src}, METHOD):
            if re.search(r'^(?:Object|Either)\b|[<,]\s*(?:\?|Object\b)', m[1]):
                errors.append(f'{at}: {m[2]} retorna {m[1]}; un método retorna un solo tipo')
            elif m[1] == 'String' and literal_only(body, m.end()):
                errors.append(f'{at}: {m[2]} solo retorna strings literales; usa un enum')
    return errors


def literal_only(body, start):
    """Si el método que empieza en start tiene cuerpo y todos sus resultados son literales (o null)."""
    open_ = body.find('{', start)
    if open_ < 0 or ';' in body[start:open_]:
        return False
    depth, i = 0, open_
    while i < len(body):
        depth += {'{': 1, '}': -1}.get(body[i], 0)
        if depth == 0:
            break
        i += 1
    block = body[open_:i]
    results = re.findall(r'\b(?:return|yield)\b([^;]*);', block)
    if any(r.strip().startswith('switch') for r in results):
        results = [r for r in results if not r.strip().startswith('switch')] + re.findall(r'->\s*([^;{}]*);', block)
    lit = r'\s*(?:[^;]*\?\s*(?:"[^"]*"|null)\s*:\s*)?(?:"[^"]*"|null)\s*'
    return bool(results) and all(re.fullmatch(lit, r) for r in results)


def adr_0008(files):
    """Números distintos de 0 y 1 solo en una constante static final o en los argumentos de constantes de enum."""
    errors = []
    for f, src in files.items():
        body = code(src)
        enums = [(m.end(), body.find(';', m.end())) for m in re.finditer(r'\benum\s+\w+[^{]*\{', body)]
        for at, m in hits('ADR-0008', {f: src}, r'(?<![\w.])(\d+(?:\.\d+)?)[fFdDlL]?(?![\w.])'):
            start = max(body.rfind(c, 0, m.start()) for c in ';{}') + 1
            constant = re.search(r'\bstatic\s+final\b', body[start:m.start()])
            listed = any(open_ < m.start() < close for open_, close in enums)
            if float(m[1]) not in (0, 1) and not constant and not listed:
                errors.append(f'{at}: número mágico {m[1]}; defínelo como constante con nombre')
    return errors


if __name__ == '__main__':
    errors = []
    for mod in MODS:
        files = java(mod)
        errors += adr_0001(mod) + adr_0002(mod) + adr_0003(mod) + adr_0004(mod) + adr_0009(mod) + adr_0006(files) + adr_0007(files) + adr_0008(files)
    print('\n'.join(errors) or f'OK: ADRs cumplidos en {len(MODS)} mod(s)')
    sys.exit(1 if errors else 0)
