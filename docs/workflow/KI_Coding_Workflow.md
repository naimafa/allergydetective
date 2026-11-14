# KI-Coding-Workflow (Gemini in Android Studio)

Ziel: Das begrenzte Gemini-Kontingent maximal schonen, indem die KI nie "raten" muss. Die Markdown-Dokumente in `docs/` sind die Blaupause und werden gezielt per Copy-Paste als Kontext mitgegeben.

---

## 1. Docs in Android Studio sichtbar machen
Die Projektansicht links oben von **"Android"** auf **"Project"** umstellen – dann ist der `docs/`-Ordner sichtbar. Dateien direkt öffnen, `Strg+A` / `Strg+C` und in den Gemini-Chat einfügen.

| Datei | Wofür |
| :--- | :--- |
| `docs/PRD_Vision.md` | Was gebaut wird (Vision, Features, Monetarisierung) |
| `docs/TRD_Technik.md` | Wie es technisch funktioniert (DB, Algorithmus) |
| `docs/UX_UI_Prototyp.md` | Screens, Flows, Farben |
| `docs/CONTENT_Symptome.md` | Symptom-IDs, Overlay, Eingabe-Regeln |

---

## 2. Grundregeln
* **Nie alle Dokumente auf einmal** – nur das, was für die aktuelle Aufgabe nötig ist ("Rollen-Prinzip").
* **Einzelne Dateien statt ganzer App** anfragen – kürzerer, präziserer Code, weniger Verbrauch.
* **"Keine Erklärungen"** im Prompt spart Output-Tokens.
* Allgemeine Fragen (z.B. "Wie installiere ich Android Studio?") woanders klären; Gemini-Kontingent nur fürs **Schreiben und Debuggen von Code** nutzen.

---

## 3. System-Anweisung (am Chat-Anfang / in den Einstellungen)
> Du bist ein erfahrener Android-Entwickler. Ich arbeite mit einem strengen Token-Limit. Bitte antworte ab jetzt extrem kurz, biete nur Code-Lösungen an und verzichte auf Smalltalk oder Einleitungen wie 'Sicher, das mache ich gerne'.

---

## 4. Prompt-Vorlagen

### 4.0 Kontext-Anker (optional, erster Prompt)
> Ich entwickle eine App. Hier ist mein technisches Gerüst (TRD) und mein Produktkonzept (PRD). Bitte lies es nur und bestätige mit 'Bereit', wenn du die Logik (T1–T3 und lokale Datenhaltung) verstanden hast.

### 4.1 Datenbank-Architektur (nur TRD)
> Ich baue das Backend. Hier ist mein **TRD**. Erstelle mir basierend auf Kapitel 1 (Daten-Architektur) die Room-Entities und DAOs. Antworte nur mit dem Code, keine Erklärungen.

### 4.2 User-Interface (nur UX/UI)
> Ich baue den Screen "Essen loggen". Hier ist mein **UX-Design**. Erstelle mir das Jetpack Compose Layout für den Voice-Button und das Favoriten-Grid. Nutze die Meal-Entity aus dem vorherigen Schritt.

### 4.3 Logik-Prüfung (nur TRD, Kapitel 2)
> Prüfe meine `Repository.kt`. Entsprechen die Zeitfenster exakt den T1-, T2-, T3-Fenstern aus meinem **TRD**? Wenn nein, korrigiere nur die SQL-Abfrage.

### 4.4 Fehlerkorrektur
> Der Algorithmus berechnet das Zeitfenster falsch. Schau in mein **TRD** unter 'Reaktions-Zeitfenster' (Kapitel 1.1) und korrigiere die Abfrage auf die dort definierten Intervalle.

---

## Werkzeugkiste
* **Links:** `docs/`-Ordner (Logik & IP)
* **Mitte:** Kotlin-Code (das Produkt)
* **Rechts:** Gemini-Chat (Assistent fürs Handwerk)
