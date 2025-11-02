from elasticsearch import Elasticsearch
import json
from pathlib import Path
from typing import Any, Dict, List
import copy

# --- Template keys ---
KEY_QUERY = "query"
KEY_BOOL = "bool"
KEY_SHOULD = "should"
KEY_MATCH_PHRASE = "match_phrase"
KEY_FIELDS = "fields"
KEY_SIZE = "size"

# --- Template names ---
MULTI_MATCH_TEMPLATE = "multi_match_template"
PHRASE_TEMPLATE = "phrase_template"
PHRASE_MULTI_TEMPLATE = "phrase_multi_template"

# --- Index fields ---
FIELDS_ALL = ["title", "title.english", "title.keyword", "corpo"]
FIELDS_TITLE = ["title", "title.english", "title.keyword"]
FIELDS_BODY = ["corpo"]

# --- Target fields for phrase search ---
TARGET_TITLE = "title"
TARGET_BODY = "corpo"

# --- User input prefixes ---
PREFIX_TITOLO = "titolo:"
PREFIX_CORPO = "corpo:"


class ElasticSearcher:
    def __init__(self, es_client: Elasticsearch, index_name="bg_index", query_map_path="./app/maps/querys.json"):
        self.es = es_client
        self.index_name = index_name
        with open(query_map_path, encoding="utf-8") as f:
            self.queries_map: Dict[str, Any] = json.load(f)

    # ------------------ Costruzione query ------------------ #
    def json_query_formation(self, query_text: str, tipo: str,
                             size: int = 10, fields: List[str] = None, target_field: str = None):
        query_body = copy.deepcopy(self.queries_map[tipo])
        query_body_str = json.dumps(query_body)

        query_body_str = query_body_str.replace("{{query_text}}", query_text)

        if fields is not None:
            query_body_str = query_body_str.replace("\"__FIELDS__\"", json.dumps(fields))

        if target_field is not None:
            query_body_str = query_body_str.replace("{{target_field}}", target_field)

        query_body = json.loads(query_body_str)
        query_body[KEY_SIZE] = size
        return query_body

    def build_multi_match(self, query_text: str, fields: List[str], size: int = 10):
        return self.json_query_formation(query_text, tipo=MULTI_MATCH_TEMPLATE, fields=fields, size=size)

    def build_phrase(self, query_text: str, target_field: str = None, multi_fields: List[str] = None, size: int = 10):
        if target_field:
            return self.json_query_formation(query_text, tipo=PHRASE_TEMPLATE, target_field=target_field, size=size)

        elif multi_fields:
            # Creazione di un match_phrase su più campi
            template = self.queries_map[PHRASE_MULTI_TEMPLATE][KEY_QUERY][KEY_BOOL][KEY_SHOULD][0]
            should_list = []
            for field in multi_fields:
                tpl_str = json.dumps(template)
                tpl_str = tpl_str.replace("{{field}}", field).replace("{{query_text}}", query_text)
                should_list.append(json.loads(tpl_str))
            query_body = {
                KEY_QUERY: {KEY_BOOL: {KEY_SHOULD: should_list}},
                KEY_SIZE: size
            }
            return query_body
        else:
            # Default: tutti i campi
            return self.build_phrase(query_text, multi_fields=FIELDS_ALL, size=size)

    # ------------------ Ricerca ------------------ #
    def search_query_text(self, user_input: str, size: int = 10):
        """
        Interpreta l'input dell'utente e costruisce la query appropriata.
        Supporta prefissi "titolo:" e "corpo:" e virgolette per ricerche esatte.
        """
        user_input = user_input.strip()

        if user_input.lower().startswith(PREFIX_TITOLO):
            q_text = user_input[len(PREFIX_TITOLO):].strip()
            if q_text.startswith('"') and q_text.endswith('"'):
                q_text = q_text.strip('"')
                query_body = self.build_phrase(q_text, target_field=TARGET_TITLE, size=size)
            else:
                query_body = self.build_multi_match(q_text, fields=FIELDS_TITLE, size=size)

        elif user_input.lower().startswith(PREFIX_CORPO):
            q_text = user_input[len(PREFIX_CORPO):].strip()
            if q_text.startswith('"') and q_text.endswith('"'):
                q_text = q_text.strip('"')
                query_body = self.build_phrase(q_text, target_field=TARGET_BODY, size=size)
            else:
                query_body = self.build_multi_match(q_text, fields=FIELDS_BODY, size=size)

        else:
            if user_input.startswith('"') and user_input.endswith('"'):
                q_text = user_input.strip('"')
                query_body = self.build_phrase(q_text, multi_fields=FIELDS_ALL, size=size)
            else:
                query_body = self.build_multi_match(user_input, fields=FIELDS_ALL, size=size)

        response = self.es.search(index=self.index_name, body=query_body)
        return response['hits']['hits']

    # ------------------ Generatore .trec ------------------ #
    def generate_trec_from_json(self, query_json_path: Path, output_path: Path, size: int = 1400):
        """
        Genera un file .trec dai risultati delle query contenute in un JSON.
        Supporta virgolette e prefissi "titolo:" o "corpo:" nelle query.
        """
        with open(query_json_path, "r", encoding="utf-8") as f:
            queries = json.load(f)

        with open(output_path, "w", encoding="utf-8") as f_out:
            for query_obj in queries:
                query_id = str(query_obj.get("ID"))
                query_text = query_obj.get("W", "").strip()
                if not query_text:
                    continue

                # Usa search_query_text per interpretare eventuali prefissi/virgolette
                results = self.search_query_text(user_input=query_text, size=size)

                for rank, hit in enumerate(results, start=1):
                    doc_id = hit["_id"]
                    score = hit["_score"]
                    f_out.write(f"{query_id} Q0 {doc_id} {rank} {score} STANDARD\n")

        print(f".trec generato in {output_path}")
