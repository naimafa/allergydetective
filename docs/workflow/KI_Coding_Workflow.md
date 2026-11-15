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

## 5. Token sparen (gilt für Claude und Gemini)
1. **Ein Chat pro Aufgabe.** Bei jeder Nachricht wird der ganze bisherige Verlauf neu verarbeitet. Lange Chats werden also mit jeder Nachricht teurer. Nach einem Meilenstein `PROGRESS.md` aktualisieren lassen und einen neuen Chat starten.
2. **Kontext aus Dateien statt Copy-Paste.** Bei Claude liegen die Docs im Projekt. Bei Gemini eine Datei per Kontext-Funktion anhängen statt den Text einzufügen. Nur das nötige Dokument bzw. Kapitel.
3. **Präzise Prompts.** Datei und Kapitel nennen ("TRD 2.4, Lift"), Ziel und Grenzen in einem Satz.
4. **Nur Änderungen anfordern.** "Zeig nur die geänderte Funktion" bzw. "nur den Diff" statt der ganzen Datei.
5. **Fragen bündeln.** Eine Nachricht mit drei Fragen ist günstiger als drei Nachrichten.
6. **Kurze Antworten verlangen.** "Kurz, ohne Erklärung", wenn nur Code gebraucht wird.
7. **Passendes Modell.** Einfache Aufgaben (Boilerplate, Umbenennen) mit einem kleineren Modell, schwierige (Engine, Architektur) mit dem stärksten.
8. **Kleine Dateien im Code** (siehe [CODING_RICHTLINIEN.md](../CODING_RICHTLINIEN.md)). Die KI muss dann weniger lesen.
9. **Keine großen Logs** einfügen. Nur die relevante Fehlermeldung plus Stacktrace-Anfang.
10. **Projekt-Docs nicht ständig ändern.** Jede Änderung invalidiert den Cache für alle Chats im Projekt. Besser gesammelt am Ende eines Arbeitsschritts aktualisieren.

---

## Werkzeugkiste
* **Links:** `docs/`-Ordner (Logik & IP)
* **Mitte:** Kotlin-Code (das Produkt)
* **Rechts:** Gemini-Chat (Assistent fürs Handwerk)
