import os
from dotenv import load_dotenv
from elasticsearch import Elasticsearch
import atexit

class ElasticConnection:
    def __init__(self, env_path="./elastic-start-local/.env"):
        load_dotenv(dotenv_path=env_path)
        host = f"http://localhost:{os.getenv('ES_LOCAL_PORT', '9200')}"
        password = os.getenv("ES_LOCAL_PASSWORD")
        
        self.es = Elasticsearch(
            host,
            basic_auth=("elastic", password),
            verify_certs=False,
        )
        self.es = self.es.options(ignore_status=[400])
        
        # Basic connection check
        if not self.es.ping():
            raise ConnectionError("Failed to connect to Elasticsearch")
        
        # Registra cleanup alla chiusura del processo
        atexit.register(self.close)
    
    def get_client(self):
        return self.es
    
    def close(self):
        """
        Chiude la connessione Elasticsearch in modo sicuro.
        """
        try:
            if self.es:
                # L'oggetto Elasticsearch non ha un metodo close ufficiale,
                # ma possiamo eliminare il client per liberare risorse
                self.es.transport.close()
                self.es = None
                print("Connessione Elasticsearch chiusa correttamente.")
        except Exception as e:
            print(f"Errore durante la chiusura della connessione Elasticsearch: {e}")
