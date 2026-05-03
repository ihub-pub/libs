#!/usr/bin/env python3
"""
Verify catalog entries are consistent with libs.versions.toml.
- Every version_ref must exist in [versions]
- Every gradle_ref must exist in [libraries] or [bundles]
Usage: python3 check-consistency.py path/to/libs.versions.toml
"""
import json, os, re, sys

CATALOG_DIR = os.path.dirname(os.path.abspath(__file__))
DOMAINS_DIR = os.path.join(CATALOG_DIR, 'domains')

def parse_toml_keys(toml_path):
    """Extract all keys from [versions], [libraries], and [bundles] sections."""
    versions = set()
    libraries = set()
    bundles = set()

    with open(toml_path, 'r') as f:
        content = f.read()

    # Find [versions] section
    v_match = re.search(r'\[versions\](.*?)(?=\[|$)', content, re.DOTALL)
    if v_match:
        for line in v_match.group(1).strip().split('\n'):
            line = line.strip()
            if line and not line.startswith('#'):
                m = re.match(r'^([\w.-]+)\s*=', line)
                if m:
                    versions.add(m.group(1))

    # Find [libraries] section
    l_match = re.search(r'\[libraries\](.*?)(?=\[|$)', content, re.DOTALL)
    if l_match:
        for line in l_match.group(1).strip().split('\n'):
            line = line.strip()
            if line and not line.startswith('#'):
                m = re.match(r'^([\w.-]+)\s*=', line)
                if m:
                    libraries.add(m.group(1))

    # Find [bundles] section
    b_match = re.search(r'\[bundles\](.*?)$', content, re.DOTALL)
    if b_match:
        for line in b_match.group(1).strip().split('\n'):
            line = line.strip()
            if line and not line.startswith('#'):
                m = re.match(r'^([\w.-]+)\s*=', line)
                if m:
                    bundles.add(m.group(1))

    return versions, libraries, bundles

def main():
    if len(sys.argv) < 2:
        toml_path = os.path.join(os.path.dirname(CATALOG_DIR), 'libs.versions.toml')
    else:
        toml_path = sys.argv[1]

    if not os.path.exists(toml_path):
        print(f'ERROR: {toml_path} not found')
        sys.exit(1)

    versions, libraries, bundles = parse_toml_keys(toml_path)
    errors = []

    for fname in sorted(os.listdir(DOMAINS_DIR)):
        if not fname.endswith('.json'):
            continue

        with open(os.path.join(DOMAINS_DIR, fname), 'r') as f:
            entries = json.load(f)

        for entry in entries:
            eid = entry.get('id', '?')

            # Check version_ref
            vref = entry.get('version_ref')
            if vref and vref not in versions:
                errors.append(f'{eid}: version_ref "{vref}" not found in [versions]')

            # Check gradle_ref
            gref = entry.get('gradle_ref')
            if gref and gref not in libraries and gref not in bundles:
                errors.append(f'{eid}: gradle_ref "{gref}" not found in [libraries] or [bundles]')

            # Check dependency references
            for dep in entry.get('dependencies', []):
                dep_id = dep.get('component_id', '?')
                # We'll just warn, not error, for cross-domain deps that may not be loaded yet
                pass  # Cross-domain validation would need full catalog merge

    if errors:
        print(f'ERRORS ({len(errors)}):')
        for e in errors:
            print(f'  - {e}')
        sys.exit(1)
    else:
        print(f'OK: All catalog entries consistent with {toml_path}')
        print(f'  [versions]: {len(versions)} keys')
        print(f'  [libraries]: {len(libraries)} keys')
        print(f'  [bundles]: {len(bundles)} keys')

if __name__ == '__main__':
    main()
