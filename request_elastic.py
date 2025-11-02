import requests
from pathlib import Path

url = "http://127.0.0.1:5000/generate_trec"

payload = {
    "query_json_path": str(Path("/home/giovanni/Uni/ingDati/cran_queries_simple.json")),
    "output_path": str(Path("/home/giovanni/Uni/ingDati/output.trec")),
    "size": 1400
}

response = requests.post(url, json=payload)

print(response.status_code)
print(response.json())
