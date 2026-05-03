#!/usr/bin/env python3
"""
Merge all domain JSON files + taxonomy.json into catalog.json.
Usage: python3 merge-catalog.py [--check]
  --check  Exit with non-zero if catalog.json would change (CI mode)
"""
import json, os, sys

CATALOG_DIR = os.path.dirname(os.path.abspath(__file__))
DOMAINS_DIR = os.path.join(CATALOG_DIR, 'domains')
TAXONOMY_PATH = os.path.join(CATALOG_DIR, 'taxonomy.json')
CATALOG_PATH = os.path.join(CATALOG_DIR, 'catalog.json')

def load_json(path):
    with open(path, 'r') as f:
        return json.load(f)

def build_catalog():
    taxonomy = load_json(TAXONOMY_PATH)

    domains = {}
    all_entries = []
    seen_ids = set()

    for fname in sorted(os.listdir(DOMAINS_DIR)):
        if fname.endswith('.json'):
            domain_name = fname[:-5]
            entries = load_json(os.path.join(DOMAINS_DIR, fname))
            domains[domain_name] = entries

            for entry in entries:
                eid = entry.get('id', '')
                if eid in seen_ids:
                    print(f'WARNING: duplicate entry id: {eid}', file=sys.stderr)
                seen_ids.add(eid)
                all_entries.append(entry)

    # Build stage index
    stage_index = {}
    for stage_id in taxonomy['stages']:
        stage_index[stage_id] = [
            e['id'] for e in all_entries if stage_id in e.get('stage', [])
        ]

    # Build alternatives graph (adjacency list)
    alternatives_graph = {}
    for entry in all_entries:
        alts = entry.get('alternatives', [])
        if alts:
            alternatives_graph[entry['id']] = alts

    # Build domain index
    domain_index = {}
    for domain_id in taxonomy['domains']:
        domain_index[domain_id] = [e['id'] for e in all_entries if e.get('domain') == domain_id]

    return {
        'catalog_version': taxonomy['catalog_version'],
        'generated': taxonomy['generated'],
        'taxonomy': taxonomy,
        'domains': domains,
        'domain_index': domain_index,
        'stage_index': stage_index,
        'alternatives_graph': alternatives_graph,
        'total_entries': len(all_entries)
    }

def main():
    check_mode = '--check' in sys.argv

    catalog = build_catalog()
    new_content = json.dumps(catalog, ensure_ascii=False, indent=2) + '\n'

    if check_mode:
        if not os.path.exists(CATALOG_PATH):
            print('ERROR: catalog.json does not exist. Run without --check to generate.')
            sys.exit(1)
        with open(CATALOG_PATH, 'r') as f:
            existing = f.read()
        if existing != new_content:
            print('ERROR: catalog.json is out of date. Run merge-catalog.py to regenerate.')
            sys.exit(1)
        print('OK: catalog.json is up to date')
    else:
        with open(CATALOG_PATH, 'w') as f:
            f.write(new_content)
        print(f'Generated {CATALOG_PATH} ({catalog["total_entries"]} entries in {len(catalog["domains"])} domains)')

if __name__ == '__main__':
    main()
