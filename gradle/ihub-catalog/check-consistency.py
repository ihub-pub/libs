#!/usr/bin/env python3
"""
Verify catalog entries are consistent with libs.versions.toml.
- Every version_ref must exist in [versions]
- Every gradle_ref must exist in [libraries] or [bundles]
- Every stage value must be a valid taxonomy stage id
- Every id must be unique across all domain files
Usage: python3 check-consistency.py path/to/libs.versions.toml
"""
import json, os, re, sys

VALID_STAGES = {'ideate', 'init', 'code', 'llm', 'adapt', 'qa', 'ops', 'migrate'}
VALID_STATUSES = {'stable', 'experimental', 'deprecated', 'legacy'}
VALID_TYPES = {'ihub-component', 'third-party-reference', 'platform-bom'}

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
    seen_ids = {}

    for fname in sorted(os.listdir(DOMAINS_DIR)):
        if not fname.endswith('.json'):
            continue

        with open(os.path.join(DOMAINS_DIR, fname), 'r') as f:
            entries = json.load(f)

        for entry in entries:
            eid = entry.get('id', '?')

            # Check id uniqueness
            if eid in seen_ids:
                errors.append(f'{eid}: duplicate id (also in {seen_ids[eid]})')
            else:
                seen_ids[eid] = fname

            # Check version_ref
            vref = entry.get('version_ref')
            if vref and vref not in versions:
                errors.append(f'{eid}: version_ref "{vref}" not found in [versions]')

            # Check gradle_ref (can be a string or a list of strings)
            gref = entry.get('gradle_ref')
            if gref:
                refs = gref if isinstance(gref, list) else [gref]
                for ref in refs:
                    if ref and ref not in libraries and ref not in bundles:
                        errors.append(f'{eid}: gradle_ref "{ref}" not found in [libraries] or [bundles]')

            # Check stage values
            for s in entry.get('stage') or []:
                if s not in VALID_STAGES:
                    errors.append(f'{eid}: invalid stage "{s}" (valid: {sorted(VALID_STAGES)})')

            # Check dependency references (strings or dicts with component_id)
            for dep in entry.get('dependencies', []):
                # deps can be strings (domain names) or dicts with component_id
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
