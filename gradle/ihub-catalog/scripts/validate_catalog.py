#!/usr/bin/env python3
"""校验 catalog 与 libs.versions.toml 的一致性

检查项：
1. id 全局唯一
2. 必填字段不为空（id, name, domain, type, description）
3. domain 值在 taxonomy 定义的领域列表中
4. version_ref 在 libs.versions.toml [versions] 中存在（null 跳过）
5. gradle_ref 在 libs.versions.toml [libraries] 中存在（null 跳过）

用法：python3 validate_catalog.py
退出码：0 = 全部通过，1 = 存在错误
"""
import json
import os
import re
import sys

CATALOG_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DOMAINS_DIR = os.path.join(CATALOG_DIR, 'domains')
TAXONOMY_FILE = os.path.join(CATALOG_DIR, 'taxonomy.json')
LIBS_DIR = os.path.dirname(CATALOG_DIR)
TOML_FILE = os.path.join(LIBS_DIR, 'libs.versions.toml')

REQUIRED_FIELDS = ['id', 'name', 'domain', 'type', 'description']
VALID_TYPES = ['ihub-component', 'third-party-reference', 'platform-bom']


def parse_toml_keys(toml_path):
    """从 toml 中解析 [versions] 和 [libraries] 的 key 集合"""
    version_keys = set()
    library_keys = set()
    current_section = None

    with open(toml_path) as f:
        for line in f:
            line = line.strip()
            if line.startswith('#') or not line:
                continue
            if line == '[versions]':
                current_section = 'versions'
                continue
            elif line == '[libraries]':
                current_section = 'libraries'
                continue
            elif line.startswith('['):
                current_section = None
                continue

            if current_section and '=' in line:
                key = line.split('=', 1)[0].strip()
                if current_section == 'versions':
                    version_keys.add(key)
                elif current_section == 'libraries':
                    library_keys.add(key)

    return version_keys, library_keys


def main():
    errors = []
    warnings = []

    # Load taxonomy
    with open(TAXONOMY_FILE) as f:
        taxonomy = json.load(f)
    valid_domains = set(taxonomy.get('domains', {}).keys())

    # Load toml keys
    version_keys, library_keys = parse_toml_keys(TOML_FILE)

    # Load all domain files
    seen_ids = {}
    total = 0

    for fname in sorted(os.listdir(DOMAINS_DIR)):
        if not fname.endswith('.json'):
            continue
        expected_domain = fname.replace('.json', '')
        fpath = os.path.join(DOMAINS_DIR, fname)

        with open(fpath) as f:
            entries = json.load(f)

        for i, entry in enumerate(entries):
            total += 1
            loc = f'{fname}[{i}]'
            entry_id = entry.get('id', f'<missing-id-at-{loc}>')

            # 1. Required fields
            for field in REQUIRED_FIELDS:
                if not entry.get(field):
                    errors.append(f'{loc} ({entry_id}): missing required field "{field}"')

            # 2. Unique id
            if entry_id in seen_ids:
                errors.append(
                    f'{loc}: duplicate id "{entry_id}" '
                    f'(also in {seen_ids[entry_id]})'
                )
            else:
                seen_ids[entry_id] = loc

            # 3. Valid domain
            domain = entry.get('domain', '')
            if domain not in valid_domains:
                errors.append(
                    f'{loc} ({entry_id}): unknown domain "{domain}" '
                    f'(valid: {", ".join(sorted(valid_domains))})'
                )

            # 4. Domain matches filename
            if domain != expected_domain:
                warnings.append(
                    f'{loc} ({entry_id}): domain "{domain}" '
                    f'does not match filename "{fname}"'
                )

            # 5. Valid type
            entry_type = entry.get('type', '')
            if entry_type not in VALID_TYPES:
                errors.append(
                    f'{loc} ({entry_id}): unknown type "{entry_type}" '
                    f'(valid: {", ".join(VALID_TYPES)})'
                )

            # 6. version_ref in toml [versions]
            vref = entry.get('version_ref')
            if vref and vref not in version_keys:
                errors.append(
                    f'{loc} ({entry_id}): version_ref "{vref}" '
                    f'not found in libs.versions.toml [versions]'
                )

            # 7. gradle_ref in toml [libraries]
            gref = entry.get('gradle_ref')
            if gref:
                refs = gref if isinstance(gref, list) else [gref]
                for r in refs:
                    if r not in library_keys:
                        errors.append(
                            f'{loc} ({entry_id}): gradle_ref "{r}" '
                            f'not found in libs.versions.toml [libraries]'
                        )

    # Report
    print(f'Validated {total} components across {len(valid_domains)} domains')
    print(f'TOML keys: {len(version_keys)} versions, {len(library_keys)} libraries')
    print()

    if warnings:
        print(f'⚠️  {len(warnings)} warning(s):')
        for w in warnings:
            print(f'  ⚠ {w}')
        print()

    if errors:
        print(f'❌ {len(errors)} error(s):')
        for e in errors:
            print(f'  ✗ {e}')
        print()
        sys.exit(1)
    else:
        print('✅ All checks passed')
        sys.exit(0)


if __name__ == '__main__':
    main()
