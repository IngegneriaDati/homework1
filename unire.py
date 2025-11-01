import os
from pathlib import Path

def unisci_file_testo(directory_input, nome_file_output="output_unito.trec"):
    """
    Trova tutti i file .trec nella directory specificata e li unisce 
    in un unico file di output.

    Args:
        directory_input (str): Il percorso della cartella contenente i file.
        nome_file_output (str): Il nome del file in cui unire i contenuti.
    """
    
    # Converte la stringa del percorso in un oggetto Path per una gestione più semplice
    input_path = Path(directory_input)
    
    # Contatore per il numero di file uniti
    conteggio_file = 0
    
    try:
        # Apre il file di output in modalità scrittura ('w'). 
        # Lo apriamo una sola volta per efficienza.
        with open(nome_file_output, 'w', encoding='utf-8') as outfile:
            
            # Itera su tutti gli elementi nella directory, inclusi i file in sottocartelle (glob='**/*')
            # e filtra solo i file che finiscono per .trec
            for file_path in input_path.glob('**/*.trec'):
                
                # Assicurati che sia un file e non una directory
                if file_path.is_file():
                    
                    try:
                        # Legge il contenuto del file
                        contenuto = file_path.read_text(encoding='utf-8')
                        
                        # Scrive il contenuto
                        outfile.write(contenuto)
                        
                        conteggio_file += 1
                        
                    except UnicodeDecodeError:
                        print(f"ATTENZIONE: Impossibile leggere {file_path.name} (errore di codifica). Saltato.")
                    except Exception as e:
                        print(f"Errore nella lettura di {file_path.name}: {e}")


        print(f"\n--- Operazione Completata ---")
        print(f"Uniti {conteggio_file} file .trec nel file '{nome_file_output}'")
        
    except FileNotFoundError:
        print(f"ERRORE: La directory di input '{directory_input}' non è stata trovata.")
    except Exception as e:
        print(f"Si è verificato un errore: {e}")

# ==============================================================================
# Esempio di Utilizzo:
# ==============================================================================

# 1. Definisci il percorso della directory da scansionare
#    (Sostituisci con il tuo percorso effettivo)
DIR_DA_SCANNER = "results_lucene_keyword_analyzer_standard_analyzer" 

# 2. Esegui la funzione
unisci_file_testo(DIR_DA_SCANNER, "archivio_unificato.trec")