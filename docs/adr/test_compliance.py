#!/usr/bin/env python3
"""Prueba los checks de Java de compliance.py: cada uno detecta su violación, acepta el código correcto y
respeta `adr-skip` (ADR-0005). Corre en pre-commit."""
from pathlib import Path

from compliance import adr_0006, adr_0007, adr_0008, dead_java


def check(fn, src, expected, metadata=''):
    errors = fn({Path('T.java'): src}, metadata) if metadata else fn({Path('T.java'): src})
    assert len(errors) == expected, f'{fn.__name__}: {expected} errores esperados, salió {errors}\n{src}'


USED = '''import java.util.List;
public class T implements Runnable {
	static final int LIMIT = 3;
	enum Load { LIGHT, HEAVY }
	@Override
	public void run() {
		int n = helper(LIMIT);
		List.of(n, Load.LIGHT, Load.HEAVY);
	}
	private static int helper(int x) { return x; } // "https://no.es/código;"
}'''
check(dead_java, USED, 0, metadata='"main": ["T"]')
check(dead_java, 'import java.util.List;\nclass T {}', 2)  # import y clase sin uso
check(dead_java, 'class T {\n\tvoid run() {}\n\tprivate int unused() { return 1; }\n}', 1, metadata='T run')
check(dead_java, 'class T { void run() { int x = 1; } }', 1, metadata='T run')
check(dead_java, 'class T { enum E { A, B } void run() { E.A.name(); } }', 1, metadata='T run')
check(dead_java, 'class T { enum E { A, B } void run() { E.values(); } }', 0, metadata='T run')
check(dead_java, 'class T {\n\t// foo();\n}', 1, metadata='T')
check(dead_java, 'class T {\n\t// adr-skip ADR-0003: API pública\n\tpublic int api() { return 1; }\n}', 0, metadata='T')

check(adr_0006, 'class T { boolean f(Object o) { return o instanceof String; } }', 1)
check(adr_0006, 'class T { Class<?> f(Object o) { return o.getClass(); } }', 1)
check(adr_0006, 'class T { Optional<T> f(Object o) { return Optional.of(o).filter(T.class::isInstance); } }', 1)
check(adr_0006, 'class T { int f(Object o) { return switch (o) { case String s -> 1; default -> 0; }; } }', 1)
check(adr_0006, 'class T { int f(E e) { return switch (e) { case A -> 1; case B -> 2; }; } }', 0)
check(adr_0006, 'class T { String f() { return "x instanceof y"; } }', 0)
check(adr_0006, 'class T {\n\t// adr-skip ADR-0006: proxy de jugador\n\tboolean f(Object o) { return o instanceof String; }\n}', 0)

check(adr_0007, 'class T {\n\tObject f() { return null; }\n}', 1)
check(adr_0007, 'class T {\n\tMap<String, Object> f() { return null; }\n}', 1)
check(adr_0007, 'class T {\n\tList<?> f() { return null; }\n}', 1)
check(adr_0007, 'class T {\n\tOptional<String> f() { return Optional.empty(); }\n}', 0)
check(adr_0007, 'class T {\n\tString f(boolean b) { if (b) return "a"; return "b"; }\n}', 1)
check(adr_0007, 'class T {\n\tString f(boolean b) { return b ? "a" : null; }\n}', 1)
check(adr_0007, 'class T {\n\tString f(E e) { return switch (e) { case A -> "a"; case B -> "b"; }; }\n}', 1)
check(adr_0007, 'class T {\n\tString f(int n) { return "n=" + n; }\n}', 0)
check(adr_0007, 'class T {\n\tString f() { return "a"; } // adr-skip ADR-0007: lo exige la interfaz\n}', 0)

check(adr_0008, 'class T { int f(int n) { return 100 * n; } }', 1)
check(adr_0008, 'class T { float f(float r) { return r < 0.3f ? 1 : 0; } }', 1)
check(adr_0008, 'class T {\n\tprivate static final int XP_PER_LEVEL = 100;\n\tint f(int n) { return XP_PER_LEVEL * n; }\n}', 0)
check(adr_0008, 'class T { enum E { A(1.05f, 0.02), B(1, 0); E(float a, double b) {} } int f(int n) { return n - 1; } }', 0)
check(adr_0008, 'class T { String f() { return "100 puntos"; } int adr_0008() { return 0; } }', 0)
check(adr_0008, 'class T { int f(int n) { return 2 * n; } // adr-skip ADR-0008: fórmula vanilla\n}', 0)
print('OK: checks de compliance.py probados')
