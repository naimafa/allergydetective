# Entwicklungs-Fahrplan: "App fertigstellen"

> Fokus: Erst bauen, Bürokratie (Impressum, Gewerbe, Datenschutzerklärung) später.

Vorgehen nach dem **Sandwich-Prinzip**: Zuerst das Herzstück, dann die Hülle.

---

## Phase 1: Das Daten-Herz (Die Engine)
Bevor ein einziges Icon designt wird, muss die Logik stehen.
- [ ] **Datenbank:** Room-Schema anlegen (siehe [TRD](TRD_Technik.md), Kapitel 1).
- [ ] **Startdaten:** Zutatenliste, Trigger, Zeitfenster und Symptome als Seed-Daten importieren ([CONTENT_Zutaten.md](CONTENT_Zutaten.md)).
- [ ] **Der Algorithmus:** Episoden → passende Fenster je Auslöser → Lift "mit vs. ohne" → Evidenzstufe (TRD Kapitel 2).
- [ ] **Test:** Unit-Tests mit synthetischen Daten (Laktose-Muster muss erkannt werden, tägliches Brot ohne Beschwerden darf nicht verdächtig sein), danach mit eigenen alten Notizen.

## Phase 2: Das Skelett (Funktionales UI)
Screens aus dem [UX-Prototyp](UX_UI_Prototyp.md) nachbauen – ohne "Schönheit".
- [ ] **Eingabemaske:** Funktioniert der Zeit-Slider?
- [ ] **Voice-Matching:** Erkennt die App das Wort "Pizza" und verknüpft es mit der Favoriten-ID?
- [ ] **Tages-Abschluss:** Werden Wohlbefinden, Schlaf, Stress und Status korrekt gespeichert?
- [ ] **Notfall-Hinweis:** Erscheint er bei Atemnot und Schwellung?

## Phase 3: Das Fleisch (Design & Content)
Jetzt wird es hübsch und benutzerfreundlich.
- [ ] **Symptom-Kacheln:** Icons und die vordefinierten Symptome einfügen (siehe [CONTENT_Symptome.md](CONTENT_Symptome.md)).
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
