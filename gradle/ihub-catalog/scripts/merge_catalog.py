#!/usr/bin/env python3
"""合并 domains/*.json + taxonomy.json → catalog.json

用法：python3 merge_catalog.py
输出：gradle/ihub-catalog/catalog.json
"""
import json
import os
import sys
from datetime import date

CATALOG_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DOMAINS_DIR = os.path.join(CATALOG_DIR, 'domains')
TAXONOMY_FILE = os.path.join(CATALOG_DIR, 'taxonomy.json')
OUTPUT = os.path.join(CATALOG_DIR, 'catalog.json')


def main():
    if not os.path.isfile(TAXONOMY_FILE):
        print(f'ERROR: taxonomy.json not found at {TAXONOMY_FILE}', file=sys.stderr)
        sys.exit(1)

    with open(TAXONOMY_FILE) as f:
        taxonomy = json.load(f)

    components = []
    domain_files = sorted(
        f for f in os.listdir(DOMAINS_DIR) if f.endswith('.json')
    )

    for fname in domain_files:
        fpath = os.path.join(DOMAINS_DIR, fname)
        with open(fpath) as f:
            entries = json.load(f)
        if not isinstance(entries, list):
            print(f'ERROR: {fname} is not a JSON array', file=sys.stderr)
            sys.exit(1)
        components.extend(entries)

    catalog = {
        'catalog_version': taxonomy.get('catalog_version', '1.0.0'),
        'generated': str(date.today()),
        'taxonomy': taxonomy,
        'components': components,
    }

    with open(OUTPUT, 'w') as f:
        json.dump(catalog, f, ensure_ascii=False, indent=2)
        f.write('\n')

    print(
        f'Merged {len(components)} components '
        f'from {len(domain_files)} domain files → {OUTPUT}'
    )


if __name__ == '__main__':
    main()
