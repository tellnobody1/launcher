#!/usr/bin/env python3

import os
import xml.etree.ElementTree as ET

def get_strings_from_file(file_path):
    strings = set()
    tree = ET.parse(file_path)
    root = tree.getroot()
    for string in root.findall('string'):
        if string.get('translatable', 'true') == 'true':
            strings.add(string.get('name'))
    return strings

def calculate_coverage(base_strings, locale_strings):
    return len(locale_strings & base_strings) / len(base_strings) * 100

def generate_coverage_report(base_strings, flavor):
    base_path = f'app/src/{flavor}/res'
    locales = [d for d in os.listdir(base_path) if d.startswith('values-')]
    coverage_report = {}

    for locale in locales:
        locale_path = os.path.join(base_path, locale, 'strings.xml')
        if os.path.exists(locale_path):
            locale_strings = get_strings_from_file(locale_path)
            coverage = calculate_coverage(base_strings, locale_strings)
            coverage_report[locale] = coverage

    return coverage_report

def print_report(coverage_report):
    for locale, coverage in coverage_report.items():
        print(f'{locale}: {coverage:.0f}%')

def main():
    base_path = 'app/src/main/res/values/strings.xml'
    base_strings = get_strings_from_file(base_path)

    print("Main Flavor:")
    main_coverage_report = generate_coverage_report(base_strings, 'main')
    print_report(main_coverage_report)

    print("\nFull Flavor:")
    full_coverage_report = generate_coverage_report(base_strings, 'full')
    print_report(full_coverage_report)

if __name__ == '__main__':
    main()
