input_path = "../cran/cranqrel"
output_path = "../cran/cranqrel_trec"

with open(input_path, "r") as f_in, open(output_path, "w") as f_out:
    for line in f_in:
        parts = line.strip().split()
        if len(parts) != 3:
            continue
        query_id, doc_id, relevance = parts
        f_out.write(f"{query_id} 0 {doc_id} {relevance}\n")

print(f"File convertito in TREC-ready: {output_path}")
