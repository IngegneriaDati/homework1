from elasticsearch import Elasticsearch, helpers
import json
from pathlib import Path
import re

class ElasticIndex:
    def __init__(self, es_client : Elasticsearch, mapping_path="./app/maps/Index_mapping.json", index_name="bg_index"):
        self.es = es_client
        self.index_name = index_name
        
        with open(mapping_path) as f:
            self.mapping = json.load(f)

    def create_index(self):
        if not self.es.indices.exists(index=self.index_name):
            result = self.es.indices.create(index=self.index_name, body=self.mapping)
            print("Indice creato.")
        else:
            print("Indice già esistente.")
            result = self.es.indices.get(index=self.index_name)
        print(result)
        print("Connessione OK:", self.es.ping())

    def delete_index(self):
        if self.es.indices.exists(index=self.index_name):
            self.es.indices.delete(index=self.index_name)
            print(f"Index {self.index_name} deleted.")
    
    def index_add_document(self, path:Path):
        with open(path, 'r', encoding='utf-8') as f:
            document = json.load(f)
            self.es.index(index=self.index_name, document=document)
            print(f"Document from {path} indexed.")
            
    def parse_file(self, path: Path):
        """
        Legge un file e restituisce un dizionario pronto per Elasticsearch:
        id, autore, bibliografia, title, corpo
        """
        text = path.read_text(encoding="utf-8").strip()

        # Estrazione dei metadati
        id_match = re.search(r"ID:\s*(\d+)", text, re.IGNORECASE)
        autore_match = re.search(r"Autore:\s*(.+)", text, re.IGNORECASE)
        biblio_match = re.search(r"Bibliografia:\s*(.+)", text, re.IGNORECASE)

        # Separazione corpo (tutto dopo Bibliografia + vuoto)
        corpo_split = re.split(r"Bibliografia:.*?\n\n", text, flags=re.IGNORECASE)
        corpo = corpo_split[-1].strip() if len(corpo_split) > 1 else ""

        return {
            "id": id_match.group(1) if id_match else None,
            "autore": autore_match.group(1).strip() if autore_match else None,
            "bibliografia": biblio_match.group(1).strip() if biblio_match else None,
            "title": path.stem,
            "corpo": corpo
        }
    
    def index_add_document(self, path: Path):
        """
        Indica un singolo documento in Elasticsearch usando parse_file.
        """
        doc = self.parse_file(path)
        if not doc["id"]:
            raise ValueError(f"Documento senza ID: {path}")
        self.es.index(index=self.index_name, id=doc["id"], document=doc)
        print(f"Indicizzato documento {doc['id']} → {path.name}")
        self.refresh_index()
        
    def bulk_index_documents(self, directory: Path):
        """
        Indicizza tutti i documenti in una directory usando operazioni bulk.
        """
        actions = []
        for file_path in directory.glob('*.txt'):
            doc = self.parse_file(file_path)
            if not doc["id"]:
                print(f"ATTENZIONE: Documento senza ID: {file_path}. Saltato.")
                continue
            action = {
                "_index": self.index_name,
                "_id": doc["id"],
                "_source": doc
            }
            actions.append(action)
        
        if actions:
            helpers.bulk(self.es, actions)
            print(f"Indicizzati {len(actions)} documenti da {directory}")
            self.refresh_index()
        else:
            print("Nessun documento da indicizzare.")
        

    def __del__(self):
        self.delete_index()
        
    def refresh_index(self):
        self.es.indices.refresh(index=self.index_name)
