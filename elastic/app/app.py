from flask import Flask, request, jsonify, render_template
from elasticsearch import Elasticsearch
from pathlib import Path
import atexit

from indice import ElasticIndex
from search import ElasticSearcher
from connection import ElasticConnection

# --- Config Flask ---
app = Flask(__name__)

# --- Connessione Elasticsearch ---
es_conn = ElasticConnection(env_path="./elastic-start-local/.env")
es_client: Elasticsearch = es_conn.get_client()

# --- Inizializza indice ---
index_name = "bg_index"
mapping_path = "./app/maps/Index_mapping.json"
elastic_index = ElasticIndex(es_client, mapping_path=mapping_path, index_name=index_name)
elastic_index.create_index()
elastic_index.bulk_index_documents(Path("/home/giovanni/Uni/ingDati/crandataset"))

# --- ElasticSearcher ---
searcher = ElasticSearcher(es_client, index_name=index_name, query_map_path="./app/maps/querys.json")

# --- Chiusura sicura al termine ---
def cleanup():
    print("Pulizia risorse Elasticsearch in corso...")
    elastic_index.delete_index()
    es_conn.close()

atexit.register(cleanup)

# --- Rotta principale per la pagina HTML ---
@app.route("/")
def index():
    return render_template("index.html")

# --- Rotta per query testuali ---
@app.route("/search", methods=["GET"])
def search():
    query_text = request.args.get("q", "")
    size = int(request.args.get("size", 10))
    
    if not query_text:
        return jsonify({"error": "Parametro 'q' mancante"}), 400
    
    # Passiamo il parametro come posizionale
    results = searcher.search_query_text(query_text, size=size)
    
    hits = [{"id": hit["_id"], "score": hit["_score"], "source": hit["_source"]} for hit in results]
    return jsonify({"results": hits})


# --- Rotta opzionale per generare .trec dalle query JSON ---
@app.route("/generate_trec", methods=["POST"])
def generate_trec():
    """
    POST JSON: {"query_json_path": "path_al_file.json", "output_path": "output.trec"}
    """
    data = request.get_json()
    query_json_path = data.get("query_json_path")
    output_path = data.get("output_path")
    size = data.get("size", 1400)

    if not query_json_path or not output_path:
        return jsonify({"error": "query_json_path e output_path richiesti"}), 400
    
    searcher.generate_trec_from_json(Path(query_json_path), Path(output_path), size=size)
    return jsonify({"message": f".trec generato in {output_path}"})

# --- Avvio Flask ---
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
