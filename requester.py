import requests
import json
import os

with open('./cran_queries_simple.json') as f:
    querys = json.load(f)


for query in querys:    
    payload = {
        "queryId": query['ID'],
        "runName": "LuceneRun_KeywordAnalyzer&StandardAnalyzer",
        "queryText": query['W']
    }
    r = requests.post("http://localhost:8080/search/trec", params=payload)
    trec_text = r.text
    os.makedirs('./results_lucene_keyword_analyzer_standard_analyzer', exist_ok=True)
    with open(f'./results_lucene_keyword_analyzer_standard_analyzer/query_{query["ID"]}.trec', 'w') as f:
        f.write(trec_text)
        f.close()
    
