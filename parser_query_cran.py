import json
import os

def parse_cran_qry_simple(input_path):
    """
    Parsa il file di query Cranfield (cran.qry) estraendo solo l'ID (.I)
    e il testo della query (.W), come singola stringa.
    Converte l'ID della query in un INTERO per rimuovere lo zero-padding.
    """
    queries = []
    current_id = None
    current_text = []
    
    # Flag per indicare se stiamo leggendo il testo (.W)
    reading_text = False

    try:
        with open(input_path, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.strip()

                if line.startswith('.I '):
                    # Trovato un nuovo ID query: salva la query precedente
                    if current_id is not None:
                        queries.append({
                            # CONVERSIONE CRUCIALE: Rimuove il padding convertendo a int
                            'ID': int(current_id), 
                            'W': ' '.join(current_text).strip()
                        })
                        
                    # Inizia la nuova query
                    # current_id è ancora una stringa qui, per sicurezza
                    current_id = line.split()[1] 
                    current_text = []
                    reading_text = False
                
                elif line.startswith('.W'):
                    # Inizia a leggere il testo della query
                    reading_text = True
                
                elif line.startswith('.'):
                    # Trovato un altro tag (es. .A, .T): interrompi la lettura del testo
                    reading_text = False
                
                elif reading_text:
                    # Se il flag è attivo, aggiungi la riga al testo della query
                    current_text.append(line)

        # Salva l'ultima query dopo la fine del file
        if current_id is not None:
            queries.append({
                # CONVERSIONE CRUCIALE per l'ultima query: Rimuove il padding
                'ID': int(current_id), 
                'W': ' '.join(current_text).strip()
            })

    except FileNotFoundError:
        print(f"Errore: File non trovato all'indirizzo {input_path}")
        return []
        
    return queries

# ----------------------------------------
# ESECUZIONE DEL CODICE
# ----------------------------------------

# Assicurati che il percorso del file sia corretto
input_path = './cran/cran.qry' 

queries_list = parse_cran_qry_simple(input_path=input_path)

# Stampa i primi due elementi della lista per verifica
if queries_list:
    print(f"Trovate {len(queries_list)} query totali.")
    print("---")
    print("Esempio di prima query (ID come intero senza padding):")
    # L'ID sarà stampato come '1' anziché '0001'
    print(queries_list[0])
else:
    print("Nessuna query parsata o file non trovato/vuoto.")
    
# Salva le query in un file JSON
output_json_path = './cran_queries_simple.json'
# Utilizza sort_keys=False per mantenere l'ordine in cui hai aggiunto gli elementi (ID, W)
json.dump(queries_list, open(output_json_path, 'w', encoding='utf-8'), indent=2, ensure_ascii=False, sort_keys=False)