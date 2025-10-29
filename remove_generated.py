import os
import shutil

def move_generated_files(base_path="./dataset", target_base="./dataset_titolato"):
    """
    Sposta tutti i file che iniziano con 'generated_' in un nuovo percorso target,
    mantenendo la struttura delle sottocartelle e rimuovendo il prefisso 'generated_'.
    """
    for folder_name in os.listdir(base_path):
        folder_path = os.path.join(base_path, folder_name)
        if os.path.isdir(folder_path):
            target_folder_path = os.path.join(target_base, folder_name)
            os.makedirs(target_folder_path, exist_ok=True)
            
            for filename in os.listdir(folder_path):
                if filename.startswith("generated_") and filename.endswith(".txt"):
                    original_file_path = os.path.join(folder_path, filename)
                    # Rimuove il prefisso 'generated_'
                    new_filename = filename[len("generated_"):]
                    target_file_path = os.path.join(target_folder_path, new_filename)
                    
                    try:
                        shutil.move(original_file_path, target_file_path)
                        print(f"Moved: {original_file_path} -> {target_file_path}")
                    except Exception as e:
                        print(f"Error moving {original_file_path}: {e}")

# Esempio di utilizzo
move_generated_files()
