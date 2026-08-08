import subprocess
import os

base_dir = os.path.dirname(os.path.abspath(__file__))

def run_git(cmd):
    res = subprocess.run(cmd, cwd=base_dir, capture_output=True, text=True)
    print(f"Executing: {' '.join(cmd)}")
    print(f"STDOUT: {res.stdout}")
    if res.stderr:
        print(f"STDERR: {res.stderr}")
    return res.returncode

print("=== URL Shortener Git Push ===")
run_git(["git", "add", "."])
run_git(["git", "commit", "-m", "refactor: optimize AnalyticsController null safety, add platform execution wrappers, and system design solutions documentation"])
run_git(["git", "push", "origin", "main"])
