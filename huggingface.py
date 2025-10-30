import ir_datasets

# Carica il dataset
dataset = ir_datasets.load("wikir/en1k")
len_dataset = len(list(dataset.docs_iter()))
print(f"Numero di documenti nel dataset wikir/en1k: {len_dataset}")