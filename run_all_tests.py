#!/usr/bin/env python3
import os
import sys
import subprocess

target_script = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "AI-Software-Engineering-Intelligence-Platform", "run_all_tests.py"))
if os.path.exists(target_script):
    res = subprocess.run([sys.executable, target_script] + sys.argv[1:])
    sys.exit(res.returncode)
else:
    print(f"Error: Target script not found at {target_script}")
    sys.exit(1)

