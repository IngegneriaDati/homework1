import re
import os
import pathlib

def sanitize_filename(name):
    # Rimuove caratteri non validi per un nome file
    return re.sub(r'[\\/:"*?<>|]+', '_', name).strip()

def parse_and_export(input_path, output_dir):
    os.makedirs(output_dir, exist_ok=True)

    with open(input_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    current = {'I': None, 'T': [], 'A': [], 'B': [], 'W': []}
    field = None

    for line in lines:
        line = line.rstrip('\n')
        if line.startswith('.I '):
            # Salva il precedente, se esiste
            if current['I'] is not None:
                save_doc(current, output_dir)
            # Inizia uno nuovo
            current = {'I': line.split()[1], 'T': [], 'A': [], 'B': [], 'W': []}
            field = None
        elif line.startswith('.T'):
            field = 'T'
        elif line.startswith('.A'):
            field = 'A'
        elif line.startswith('.B'):
            field = 'B'
        elif line.startswith('.W'):
            field = 'W'
        else:
            # Aggiungi la riga al campo corrente, anche se vuota
            if field and field in current:
                current[field].append(line)

    # Salva l'ultimo
    if current['I'] is not None:
        save_doc(current, output_dir)

def save_doc(doc, output_dir):
    doc_id = doc['I']
    title = ' '.join(doc['T']).strip() or f"doc_{doc_id}"
    author = ' '.join(doc['A']).strip()
    bibliography = ' '.join(doc['B']).strip()
    # Mantieni le righe originali per la W, preservando la formattazione
    content = '\n'.join(doc['W']).strip()

    filename = sanitize_filename(title)
    # Limita la lunghezza del nome file per evitare problemi
    filename = filename[:200] if len(filename) > 200 else filename
    filepath = os.path.join(output_dir, f"{filename}.txt")

    with open(filepath, 'w', encoding='utf-8') as out:
        out.write(f"ID: {doc_id}\n")
        if author:
            out.write(f"Autore: {author}\n")
        if bibliography:
            out.write(f"Bibliografia: {bibliography}\n")
        out.write("\n")
        out.write(content)

    print(f"Salvato: {filepath}")

# Esempio d'uso
parse_and_export("./cran/cran.all.1400", "crandataset")