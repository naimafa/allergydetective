# Zutaten-Datenbank & Trigger-Zuordnung: Allergie-Detektiv

**Version:** 0.1 (Startliste, **vorläufig – vor Release fachlich prüfen**)
**Zweck:** Die App muss wissen, welche Auslöser in einer Zutat stecken (Spinat → Histamin). Nur dann kann sie Zeitfenster und Plausibilität berechnen (siehe [TRD](TRD_Technik.md), Kapitel 1.1).

> Die Zeitfenster und Zuordnungen sind Design-Annahmen nach gängigen Beschreibungen, **keine medizinischen Fakten**. Sie werden in der Gesamtprüfung vor Release verifiziert.

---

## 1. Die 12 Basis-Kategorien

| ID | Kategorie |
| :--- | :--- |
| 1 | Getreide & Backwaren |
| 2 | Milch & Milchprodukte |
| 3 | Käse |
| 4 | Fleisch & Wurst |
| 5 | Fisch & Meeresfrüchte |
| 6 | Eier |
| 7 | Gemüse & Kartoffeln |
| 8 | Obst |
| 9 | Nüsse & Samen |
| 10 | Hülsenfrüchte & Soja |
| 11 | Getränke |
| 12 | Würzmittel, Saucen & Süßes |

---

## 2. Trigger-Eigenschaften (`properties`)

Symptomgruppen: **A** Magen & Darm · **B** Haut · **C** Kopf, Energie & Kreislauf · **D** Atemwege, Augen & Mund

### 2.1 Unverträglichkeiten & Stoffe

| ID | Eigenschaft | Fenster (vorläufig) | Label | Plausible Gruppen | Hinweis |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 101 | Histamin (histaminreich) | 0–4h | T2 | A, B, C, D | Wirkung oft kumulativ über den Tag |
| 106 | Histamin-Liberator | 0–4h | T2 | A, B, C, D | Setzt körpereigenes Histamin frei |
| 102 | Gluten | 1–48h | T3 | A, B, C | Zöliakie / Gluten-Sensitivität |
| 103 | Laktose | 0,25–6h | T2 | A | Dosisabhängig |
| 104 | Fruktose | 0,5–8h | T2 | A | Dosisabhängig |
| 105 | Sorbit / Polyole | 0,5–8h | T2 | A | Auch in "zuckerfrei"-Produkten |
| 107 | Fruktane | 1–12h | T3 | A | Weizen, Zwiebel, Knoblauch (FODMAP) |
| 127 | Galaktane (GOS) | 1–12h | T3 | A | Hülsenfrüchte (FODMAP) |
| 119 | Alkohol | 0–12h | T3 | A, B, C | Zusätzlich Histamin-Abbau-Hemmer |
| 120 | Koffein | 0–6h | T2 | A, C | |
| 121 | Scharf (Capsaicin) | 0–6h | T2 | A | |
| 122 | Fettreich | 0–6h | T2 | A | Sodbrennen, Übelkeit, Durchfall |
| 123 | Sulfite | 0–2h | T1 | B, D | EU-14-Allergen |

### 2.2 Allergene (Sofortreaktion, angelehnt an die EU-14-Kennzeichnung)

| ID | Eigenschaft | Fenster | Label | Plausible Gruppen |
| :--- | :--- | :--- | :--- | :--- |
| 108 | Schalenfrüchte (Nüsse) | 0–2h | T1 | A, B, D |
| 109 | Erdnuss | 0–2h | T1 | A, B, D |
| 110 | Ei | 0–2h | T1 | A, B, D |
| 111 | Fisch | 0–2h | T1 | A, B, D |
| 112 | Krebstiere | 0–2h | T1 | A, B, D |
| 126 | Weichtiere | 0–2h | T1 | A, B, D |
| 113 | Soja | 0–2h | T1 | A, B, D |
| 114 | Sellerie | 0–2h | T1 | A, B, D |
| 115 | Senf | 0–2h | T1 | A, B, D |
| 116 | Sesam | 0–2h | T1 | A, B, D |
| 117 | Milcheiweiß | 0–2h | T1 | A, B, D |
| 125 | Lupine | 0–2h | T1 | A, B, D |
| 118 | Pollen-kreuzreaktiv (Birke) | 0–1h | T1 | B, D |

> Glutenhaltiges Getreide ist ebenfalls ein EU-14-Allergen. Es läuft in der App über die Eigenschaft 102 Gluten.

---

## 3. Startliste Zutaten

### 1 · Getreide & Backwaren
| Zutat | Trigger |
| :--- | :--- |
| Weißbrot / Weizenbrötchen | Gluten, Fruktane |
| Vollkornbrot (Weizen) | Gluten, Fruktane |
| Roggenbrot | Gluten, Fruktane |
| Dinkelbrot | Gluten, Fruktane |
| Nudeln (Weizen) | Gluten, Fruktane |
| Pizzateig | Gluten, Fruktane |
| Couscous / Bulgur | Gluten, Fruktane |
| Haferflocken | Gluten ¹ |
| Croissant / Blätterteig | Gluten, Fruktane, Fettreich, Milcheiweiß |
| Kuchen (Rührteig) | Gluten, Ei, Milcheiweiß, Laktose |
| Reis | – |
| Mais / Polenta | – |
| Glutenfreies Brot | – |

¹ Hafer ist meist nur durch Verunreinigung glutenhaltig. Die Zuordnung ist vorsichtig gewählt.

### 2 · Milch & Milchprodukte
| Zutat | Trigger |
| :--- | :--- |
| Kuhmilch | Laktose, Milcheiweiß |
| Laktosefreie Milch | Milcheiweiß |
| Joghurt | Laktose, Milcheiweiß |
| Quark | Laktose, Milcheiweiß |
| Sahne | Laktose, Milcheiweiß, Fettreich |
| Butter | Milcheiweiß, Fettreich |
| Kefir | Laktose, Milcheiweiß, Histamin |
| Milchspeiseeis | Laktose, Milcheiweiß, Fettreich |
| Sojadrink | Soja |
| Haferdrink | Gluten ¹ |

### 3 · Käse
| Zutat | Trigger |
| :--- | :--- |
| Hartkäse gereift (Parmesan, Bergkäse, alter Gouda) | Histamin, Milcheiweiß ² |
| Schnittkäse jung (junger Gouda, Edamer) | Milcheiweiß |
| Weichkäse (Camembert, Brie) | Histamin, Milcheiweiß |
| Mozzarella | Laktose, Milcheiweiß |
| Frischkäse | Laktose, Milcheiweiß |
| Hüttenkäse | Laktose, Milcheiweiß |
| Feta / Schafskäse | Milcheiweiß |

² Gereifter Hartkäse ist praktisch laktosefrei.

### 4 · Fleisch & Wurst
| Zutat | Trigger |
| :--- | :--- |
| Frisches Fleisch (Huhn, Rind, Schwein) | – |
| Hackfleisch (frisch) | – |
| Salami / Rohwurst | Histamin, Fettreich |
| Rohschinken | Histamin |
| Kochschinken | – |
| Bratwurst | Fettreich |
| Speck | Fettreich |

### 5 · Fisch & Meeresfrüchte
| Zutat | Trigger |
| :--- | :--- |
| Frischer Fisch | Fisch |
| Thunfisch (Dose) | Fisch, Histamin |
| Räucherfisch (Lachs, Makrele) | Fisch, Histamin |
| Sardellen / Sardinen | Fisch, Histamin |
| Garnelen | Krebstiere |
| Muscheln / Tintenfisch | Weichtiere |

### 6 · Eier
| Zutat | Trigger |
| :--- | :--- |
| Ei | Ei, Histamin-Liberator |
| Mayonnaise | Ei, Fettreich |

### 7 · Gemüse & Kartoffeln
| Zutat | Trigger |
| :--- | :--- |
| Tomate | Histamin, Histamin-Liberator |
| Spinat | Histamin |
| Aubergine | Histamin |
| Avocado | Histamin |
| Sauerkraut | Histamin |
| Zwiebel | Fruktane |
| Knoblauch | Fruktane |
| Lauch | Fruktane |
| Pilze | Polyole |
| Blumenkohl | Polyole |
| Karotte (roh) | Pollen-kreuzreaktiv |
| Sellerie | Sellerie, Pollen-kreuzreaktiv |
| Chili | Scharf |
| Kartoffel | – |
| Paprika | – |
| Gurke | – |
| Zucchini | – |
| Blattsalat | – |

### 8 · Obst
| Zutat | Trigger |
| :--- | :--- |
| Apfel | Fruktose, Sorbit, Pollen-kreuzreaktiv |
| Birne | Fruktose, Sorbit, Pollen-kreuzreaktiv |
| Kirsche | Sorbit, Pollen-kreuzreaktiv |
| Pfirsich / Nektarine | Sorbit, Pollen-kreuzreaktiv |
| Pflaume | Sorbit, Pollen-kreuzreaktiv |
| Mango | Fruktose |
| Wassermelone | Fruktose, Polyole |
| Banane | Histamin-Liberator |
| Erdbeere | Histamin-Liberator |
| Zitrusfrüchte | Histamin-Liberator |
| Ananas | Histamin-Liberator |
| Kiwi | Histamin-Liberator |
| Trockenobst | Fruktose, Sorbit, Sulfite |
| Heidelbeeren / Himbeeren | – |

### 9 · Nüsse & Samen
| Zutat | Trigger |
| :--- | :--- |
| Haselnuss | Schalenfrüchte, Pollen-kreuzreaktiv, Histamin-Liberator |
| Walnuss | Schalenfrüchte, Histamin-Liberator |
| Mandel | Schalenfrüchte |
| Cashew | Schalenfrüchte, Galaktane |
| Pistazie | Schalenfrüchte, Fruktane |
| Erdnuss / Erdnussbutter | Erdnuss, Histamin-Liberator |
| Sesam / Tahin | Sesam |

### 10 · Hülsenfrüchte & Soja
| Zutat | Trigger |
| :--- | :--- |
| Kichererbsen | Galaktane |
| Hummus | Galaktane, Sesam, Fruktane |
| Linsen | Galaktane |
| Bohnen | Galaktane |
| Tofu | Soja |
| Sojasoße | Soja, Gluten, Histamin |
| Lupinenmehl | Lupine |

### 11 · Getränke
| Zutat | Trigger |
| :--- | :--- |
| Kaffee | Koffein |
| Schwarzer / Grüner Tee | Koffein |
| Cola / Energy-Drink | Koffein |
| Rotwein | Alkohol, Histamin, Sulfite |
| Weißwein / Sekt | Alkohol, Sulfite |
| Bier | Alkohol, Gluten, Histamin |
| Spirituosen | Alkohol |
| Apfelsaft | Fruktose, Sorbit |
| Orangensaft | Histamin-Liberator |
| Wasser / Kräutertee | – |

### 12 · Würzmittel, Saucen & Süßes
| Zutat | Trigger |
| :--- | :--- |
| Senf | Senf |
| Essig | Histamin |
| Ketchup | Histamin |
| Pesto (Basilikum) | Milcheiweiß, Histamin, Fruktane |
| Scharfe Soße | Scharf |
| Honig | Fruktose |
| Agavendicksaft | Fruktose |
| Milchschokolade | Histamin-Liberator, Laktose, Milcheiweiß |
| Zartbitterschokolade / Kakao | Histamin-Liberator, Koffein |
| Zuckerfreie Kaugummis / Bonbons | Sorbit |
| Zucker | – |

---

## 4. Regeln für eigene Zutaten & Fertigprodukte
* Eigene Zutat = Name + Kategorie + **"ähnlich wie …"**. Die Trigger der Basis-Zutat werden vorgeschlagen und sind änderbar.
* **Fertiggerichte** am besten als Favorit mit Hauptzutaten anlegen (z.B. "Pizza Margherita" = Pizzateig, Tomate, Mozzarella), nicht als eine Zutat.
* Kandidaten mit "–" erscheinen trotzdem in der Analyse **als Zutat**, nur ohne Trigger-Gruppe. Ihr Fenster ist dann das breiteste Fenster (0–48h), die Plausibilität gilt für alle Gruppen.
