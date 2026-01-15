// webStorage.js
export function saveToIndexedDB(fileName, data, mimeType) {
    const DB_NAME = "TreaseVideoDB";
    const STORE_NAME = "files";

    const req = indexedDB.open(DB_NAME, 2);

    req.onupgradeneeded = (event) => {
        const db = event.target.result;
        if (!db.objectStoreNames.contains(STORE_NAME)) {
            db.createObjectStore(STORE_NAME);
        }
    };

    req.onsuccess = (event) => {
        const db = event.target.result;
        const tx = db.transaction(STORE_NAME, "readwrite");
        const store = tx.objectStore(STORE_NAME);
        const blob = new Blob([data], { type: mimeType });
        store.put(blob, fileName);
    };
}

export function loadFromIndexedDB(fileName) {
    return new Promise((resolve, reject) => {
        const req = indexedDB.open("TreaseVideoDB", 2);

        req.onsuccess = (event) => {
            const db = event.target.result;
            if (!db.objectStoreNames.contains("files")) {
                resolve(null);
                return;
            }

            const tx = db.transaction("files", "readonly");
            const store = tx.objectStore("files");
            const getReq = store.get(fileName);

            getReq.onsuccess = () => {
                if (getReq.result) {
                    resolve(URL.createObjectURL(getReq.result));
                } else {
                    resolve(null);
                }
            };
            getReq.onerror = () => resolve(null);
        };

        req.onerror = () => resolve(null);
    });
}