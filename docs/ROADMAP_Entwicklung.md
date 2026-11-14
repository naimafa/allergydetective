# Entwicklungs-Fahrplan: "App fertigstellen"

> Fokus: Erst bauen, Bürokratie (Impressum, Gewerbe, Datenschutzerklärung) später.

Vorgehen nach dem **Sandwich-Prinzip**: Zuerst das Herzstück, dann die Hülle.

---

## Phase 1: Das Daten-Herz (Die Engine)
Bevor ein einziges Icon designt wird, muss die Logik stehen.
- [ ] **Datenbank:** Lokale SQLite-Tabellen anlegen (siehe [TRD](TRD_Technik.md), Kapitel 1).
- [ ] **Der Algorithmus:** Funktion schreiben, die prüft: *„Wenn Symptom X um 16:00 Uhr auftrat, welche Mahlzeiten liegen im T1-, T2- und T3-Zeitfenster davor?“*
- [ ] **Test:** Engine mit eigenen alten Notizen als Testdaten füttern. Stimmen die Ergebnisse, ist das Fundament sicher.

## Phase 2: Das Skelett (Funktionales UI)
Screens aus dem [UX-Prototyp](UX_UI_Prototyp.md) nachbauen – ohne "Schönheit".
- [ ] **Eingabemaske:** Funktioniert der Zeit-Slider?
- [ ] **Voice-Matching:** Erkennt die App das Wort "Pizza" und verknüpft es mit der Favoriten-ID?
- [ ] **Stress-Slider:** Werden die Werte korrekt gespeichert?

## Phase 3: Das Fleisch (Design & Content)
Jetzt wird es hübsch und benutzerfreundlich.
- [ ] **Symptom-Kacheln:** Icons und die 19 vordefinierten Symptome einfügen (siehe [CONTENT_Symptome.md](CONTENT_Symptome.md)).
- [ ] **Motivations-Sprüche:** Liste mit 30–50 Sätzen in den App-Code laden.
- [ ] **Farben:** Petrol/Salbeigrün/Koralle-Schema anwenden.

## Phase 4: Der "Self-Test"
Du bist dein erster und wichtigster User.
- [ ] App **7 Tage lang** konsequent selbst nutzen.
- [ ] Fragen: Nervt mich etwas? Dauert das Loggen zu lange? Ist der Bericht am Ende klar verständlich?

---

## Vorteil der Local-First-Architektur beim Bauen
Da **keine Cloud** genutzt wird:
1. Kein Server-Backend nötig.
2. Kein Login-System (kein "Passwort vergessen"-Stress).
3. Die App ist superschnell, weil alles lokal auf dem Handy passiert.
