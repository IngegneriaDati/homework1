from google import genai
import os
import re
import time
import random
from tenacity import retry, stop_after_attempt, wait_exponential

client = genai.Client()

base_path = "./dataset"

def sanitize_filename(title):
    title = re.sub(r'[<>:"/\\|?*]', '', title)
    return title[:100].strip()

@retry(stop=stop_after_attempt(3), wait=wait_exponential(multiplier=1, min=4, max=10))
def get_generated_title(text):
    prompt = f"Generate exactly one short and concise title for the following text. Do not add any explanations, greetings, or punctuation other than the title itself:\n{text}\nTitle:"
    response = client.models.generate_content(
        model="gemini-2.5-flash",
        contents=prompt
    )
    return response.text

for folder_name in os.listdir(base_path):
    folder_path = os.path.join(base_path, folder_name)
    if os.path.isdir(folder_path):
        for filename in sorted(os.listdir(folder_path)):
            file_path = os.path.join(folder_path, filename)
            
            if filename.endswith(".txt"):
                if filename.startswith("generated_"):
                    continue

                with open(file_path, "r", encoding="utf-8") as f:
                    text = f.read()
                
                try:
                    generated_title = sanitize_filename(get_generated_title(text))
                    new_file_path = os.path.join(folder_path, f"generated_{generated_title}.txt")
                    
                    if new_file_path != file_path:
                        os.rename(file_path, new_file_path)
                        print(f"File '{filename}' rinominato in '{generated_title}.txt'")
                    else:
                        print(f"File '{filename}' già corretto, salto")
                except Exception as e:
                    print(f"Errore nel processare '{filename}': {str(e)}")
                    continue