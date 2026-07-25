"""Tiny HTTP server that logs POST bodies — a stand-in "installer" for demos/tests.

Any app whose install_url points here (http://hookcatcher:8000/...) will have its
install payload (the JSON Form content + install_uuid + secret_key) printed to the
container log. View it with, e.g.:

    docker compose -f docker-compose.keycloak.yml logs -f hookcatcher
    docker compose -f docker-compose.yml -f docker-compose.prod-keycloak.yml logs -f hookcatcher
"""
from http.server import BaseHTTPRequestHandler, HTTPServer


class Handler(BaseHTTPRequestHandler):
    def do_POST(self):
        length = int(self.headers.get("Content-Length", 0))
        body = self.rfile.read(length).decode("utf-8", "replace")
        print(f"HOOK RECEIVED POST {self.path} -> {body}", flush=True)
        self.send_response(200)
        self.end_headers()
        self.wfile.write(b"ok")

    def log_message(self, *args):
        pass  # silence default request logging; we print our own line


if __name__ == "__main__":
    print("hookcatcher listening on :8000", flush=True)
    HTTPServer(("0.0.0.0", 8000), Handler).serve_forever()
