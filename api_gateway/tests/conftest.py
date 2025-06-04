import os
import webbrowser
from datetime import (
    datetime as dt,
    timezone as tz
    )

import sys
sys.path.insert(0, os.path.abspath(os.getcwd()))


def pytest_unconfigure(config):
    report_path = os.path.abspath("report.html")
    if os.path.exists(report_path):
        timestamp = dt.now(tz.utc).strftime("%Y%m%d_%H%M%S")
        renamed_path = os.path.abspath(f"tests/reports/report_{timestamp}.html")
        os.makedirs(os.path.dirname(renamed_path), exist_ok=True)
        os.rename(report_path, renamed_path)
        webbrowser.open_new_tab(f"file://{renamed_path}")