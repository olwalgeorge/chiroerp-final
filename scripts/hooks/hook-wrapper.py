#!/usr/bin/env python3
"""Wrapper to call PowerShell pre-commit hooks on Windows"""
import sys
import subprocess
import os

def main():
    if len(sys.argv) < 2:
        print("Usage: hook-wrapper.py <hook-script.ps1> [files...]")
        return 1

    script_name = sys.argv[1]
    files = sys.argv[2:]

    if not files:
        return 0  # No files to check

    script_dir = os.path.dirname(os.path.abspath(__file__))
    script_path = os.path.join(script_dir, script_name)

    cmd = ['pwsh', '-File', script_path] + files
    result = subprocess.run(cmd, capture_output=False)

    return result.returncode

if __name__ == '__main__':
    sys.exit(main())
