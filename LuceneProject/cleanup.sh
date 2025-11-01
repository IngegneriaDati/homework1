#!/bin/bash

echo "🧹 Pulizia Indice Lucene..."

# Rimuovi il lock file se esiste
if [ -f "./dir_index/write.lock" ]; then
    echo "🔓 Rimozione write.lock..."
    rm -f ./dir_index/write.lock
fi

# Rimuovi l'intera directory indice per una pulizia completa
if [ -d "./dir_index" ]; then
    echo "🗑️ Rimozione directory indice..."
    rm -rf ./dir_index
fi

echo "✅ Pulizia completata!"
echo "Ora puoi riavviare l'applicazione"