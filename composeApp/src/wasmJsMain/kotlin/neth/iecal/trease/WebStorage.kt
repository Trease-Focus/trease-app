package neth.iecal.trease

import kotlinx.coroutines.await
import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.set
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise
object WebStorage {

    suspend fun saveFile(fileName: String, data: ByteArray) {
        val mimeType = when {
            fileName.endsWith(".mp4", true) -> "video/mp4"
            fileName.endsWith(".webm", true) -> "video/webm"
            fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> "image/jpeg"
            fileName.endsWith(".png", true) -> "image/png"
            fileName.endsWith(".json", true) -> "application/json"
            fileName.endsWith(".txt", true) -> "text/plain"
            else -> "application/octet-stream"
        }

        val jsArray = Uint8Array(data.size)
        for (i in data.indices) {
            jsArray[i] = data[i]
        }

        saveToIndexedDB(fileName, jsArray, mimeType)
    }


    @OptIn(ExperimentalWasmJsInterop::class)
    suspend fun loadFileUrl(fileName: String): String? = suspendCancellableCoroutine { continuation ->
        val jsStr = loadFromIndexedDB(fileName).then(
            onFulfilled = { result ->
                // Convert JsString? to Kotlin String?
                return@then result
            },
        )
        continuation.resume(jsStr.toString())

    }
}


@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(fileName, data, mimeType) => { const DB_NAME = 'TreaseVideoDB'; const STORE_NAME = 'files'; const req = indexedDB.open(DB_NAME, 2); req.onupgradeneeded = (event) => { const db = event.target.result; if (!db.objectStoreNames.contains(STORE_NAME)) { db.createObjectStore(STORE_NAME); } }; req.onsuccess = (event) => { const db = event.target.result; const tx = db.transaction(STORE_NAME, 'readwrite'); const store = tx.objectStore(STORE_NAME); const blob = new Blob([data], { type: mimeType }); store.put(blob, fileName); }; }")
private external fun saveToIndexedDB(fileName: String, data: Uint8Array, mimeType: String)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(fileName) => new Promise((resolve, reject) => { const req = indexedDB.open('TreaseVideoDB', 2); req.onsuccess = (event) => { const db = event.target.result; if (!db.objectStoreNames.contains('files')) { resolve(null); return; } const tx = db.transaction('files', 'readonly'); const store = tx.objectStore('files'); const getReq = store.get(fileName); getReq.onsuccess = () => { if (getReq.result) { resolve(URL.createObjectURL(getReq.result)); } else { resolve(null); } }; getReq.onerror = () => resolve(null); }; req.onerror = () => resolve(null); })")
private external fun loadFromIndexedDB(fileName: String): Promise<JsString?>