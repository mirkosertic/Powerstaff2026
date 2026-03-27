# Analyse: Klassische Profilsuche - Logikfehler, UI-Probleme & Verbesserungen

**Datum**: 2026-03-26
**Kontext**: Erweiterung der Profilsuche um klassische Google-Style Suche

---

## 1. Design-System-Prüfung

### Benötigte CSS-Komponenten

| Komponente | Bereits vorhanden? | Status | Anmerkung |
|------------|-------------------|--------|-----------|
| `.chip` / `.chip-row` | ✅ Ja | OK | Definiert in UI-DESIGNSYSTEM.md §14 |
| `.chip-xs` | ✅ Ja | OK | Kleine Chips für Tabellen |
| `.chip-add` | ✅ Ja | OK | "Hinzufügen" Dropdown |
| `.tag-grid` / `.tg-label` | ✅ Ja | OK | 2-spaltige Tag-Grid-Struktur |
| `.tbl-wrap` | ✅ Ja | OK | Tabellen-Wrapper für Suchergebnisse |
| `.srt`, `.srt-asc`, `.srt-desc` | ✅ Ja | OK | Sortierbare Spaltenheader |
| `.btn-row` | ✅ Ja | OK | Button-Gruppen mit Abstand |
| `.btn-pri`, `.btn-ghost` | ✅ Ja | OK | Button-Styles für Tabs |
| `.banner-error` | ✅ Ja | OK | Fehlerbanner |
| `--danger-l`, `--danger` | ✅ Ja | OK | CSS-Variablen für Kontaktsperre-Markierung |
| `.chip.selected` | ❌ Nein | **NEU** | Muss hinzugefügt werden |
| Tab-Navigation-Styles | ❌ Nein | **OPTIONAL** | Buttons funktionieren, aber dedizierte Tab-Komponente wäre besser |

### Erforderliche CSS-Erweiterungen

#### 1. Ausgewählte Tags (`.chip.selected`)

**Datei**: `src/main/frontend/src/css/components2.css`

```css
/* Selected chip state for tag filtering (profilsuche) */
.chip.selected {
  background: var(--pri);
  color: white;
  border-color: var(--pri-d);
}

.chip.selected .chip-x {
  color: white;
  opacity: 0.9;
}

.chip.selected .chip-x:hover {
  opacity: 1;
}
```

**Begründung**: Die Chips müssen visuell zeigen, welche Tags ausgewählt sind. Der aktuelle Plan verwendet `onClick="toggleTag()"`, aber es gibt keine CSS-Klasse für den selected-State.

#### 2. Optional: Dedizierte Tab-Navigation

**Problem**: Der Plan verwendet `.btn-row` mit `.btn-pri` / `.btn-ghost` für Tabs. Das funktioniert, ist aber semantisch nicht ideal.

**Empfehlung**: Entweder:
- **A)** Bei der aktuellen Lösung bleiben (einfacher, kein neues CSS)
- **B)** Eine dedizierte `.tabs` / `.tab` Komponente erstellen (sauberer, wiederverwendbar)

**Wenn B gewählt wird:**

```css
/* Tab navigation */
.tabs {
  display: flex;
  gap: 0;
  border-bottom: 2px solid var(--border-l);
  margin-bottom: 1rem;
}

.tab {
  padding: 10px 20px;
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  color: var(--text-2);
  font-size: 0.88rem;
  font-weight: 600;
  cursor: pointer;
  text-decoration: none;
  margin-bottom: -2px;
  transition: all 0.2s;
}

.tab:hover {
  color: var(--pri);
  background: var(--pri-l);
}

.tab.active {
  color: var(--pri);
  border-bottom-color: var(--pri);
}
```

**Empfehlung**: Variante A (Button-basiert) für diese Iteration beibehalten. Tab-Komponente kann später als Design-System-Erweiterung hinzugefügt werden.

---

## 2. Identifizierte Logikfehler und UI-Probleme

### Problem 1: Suchbegriff als Pflichtfeld zu restriktiv

**Beschreibung**:
Der Plan definiert `searchTerm` als Pflichtfeld. Aber eine Suche nur mit Tags (z.B. "Alle Java-Entwickler") oder nur mit Tagessatz-Filter wäre ebenfalls sinnvoll.

**Auswirkung**:
User können nicht nach Tags oder Tagessatz filtern ohne einen künstlichen Suchbegriff einzugeben (z.B. "a" oder "*").

**Empfohlene Lösung**:
`searchTerm` als optional markieren. Validierung: Mindestens eines der Felder (searchTerm, salaryPerDayFrom, salaryPerDayTo, tagIds) muss ausgefüllt sein.

```java
if (offset == 0) {
    final boolean hasSearchTerm = criteria.searchTerm() != null && !criteria.searchTerm().isBlank();
    final boolean hasSalaryFilter = criteria.salaryPerDayFrom() != null || criteria.salaryPerDayTo() != null;
    final boolean hasTagFilter = criteria.tagIds() != null && !criteria.tagIds().isBlank();

    if (!hasSearchTerm && !hasSalaryFilter && !hasTagFilter) {
        model.addAttribute("error", "Bitte mindestens ein Suchkriterium angeben");
        // ... render form
    }
}
```

**Priorität**: Mittel

---

### Problem 2: Zurück-Navigation mehrdeutig

**Beschreibung**:
Der Parameter `returnTo=profilesearch` ist mehrdeutig. Es gibt jetzt zwei Bereiche in der Profilsuche:
- Chat (`/profilesearch/chat`)
- Suche (`/profilesearch/search`)

Wenn der User von der Suche zu einem Freiberufler navigiert und dann "Zurück" klickt, sollte er zur Suche zurückkommen, nicht zum Chat.

**Aktueller Plan**:
```html
<tr onclick="window.location='/freelancer/42?returnTo=profilesearch'">
```

Dann im Freiberufler-Formular:
```html
<a th:if="${returnTo == 'profilesearch'}" href="/profilesearch/search">
  ← Zurück zur Profilsuche
</a>
```

**Problem**: Der Link geht immer zu `/profilesearch/search`, auch wenn der User vom Chat kam.

**Empfohlene Lösung**:

**Variante A** (einfach): Verschiedene `returnTo` Werte:
- `returnTo=profilesearch-chat` → `/profilesearch/chat/{chatId}`
- `returnTo=profilesearch-search` → `/profilesearch/search` (mit Suchkriterien in URL)

**Variante B** (sauber): `returnTo` als vollständige URL:
```html
<tr onclick="window.location='/freelancer/42?returnTo=' + encodeURIComponent(window.location.href)">
```

Dann im Freiberufler-Formular:
```html
<a th:if="${returnTo != null}" th:href="${returnTo}">
  ← Zurück
</a>
```

**Empfehlung**: Variante A ist ausreichend für diese Iteration. Die Suchkriterien gehen verloren, aber der User kommt zumindest zur richtigen Ansicht zurück.

**Priorität**: Hoch

---

### Problem 3: Tag-Klick startet Freiberufler-QBE ohne Context-Erhaltung

**Beschreibung**:
Wenn der User auf einen Tag in den Suchergebnissen klickt:
```html
<span onclick="window.location='/freelancer/search?tagId=42&returnTo=profilesearch'">
```

Problem: Die ursprünglichen Suchkriterien der Profilsuche gehen verloren. Wenn der User zurück zur Profilsuche will, müsste er die Suche erneut durchführen.

**Empfohlene Lösung**:

**Variante A**: Suchkriterien in Cookie speichern (ADR-017: erlaubt für "last viewed")
```javascript
// Vor Navigation zur Freiberufler-QBE
localStorage.setItem('profilesearch-last-criteria', JSON.stringify({
  searchTerm: '...',
  salaryPerDayFrom: ...,
  // ...
}));
```

**Variante B**: Vollständige URL als `returnTo`
```html
<span onclick="event.stopPropagation();
  window.location='/freelancer/search?tagId=42&returnTo=' +
  encodeURIComponent('/profilesearch/search?searchTerm=...')">
```

**Empfehlung**: Variante A (localStorage) ist sauberer und vermeidet extrem lange URLs.

**Priorität**: Mittel (Nice-to-have für erste Version)

---

### Problem 4: Mock-Service ignoriert Suchkriterien komplett

**Beschreibung**:
Der Mock-Service gibt immer dieselben 50 Ergebnisse zurück, unabhängig von:
- Suchbegriff
- Tagessatz-Filter
- Tag-Auswahl
- Sortierung

**Auswirkung**:
- Verwirrend für Entwickler und Tester
- User-Tests sind nicht aussagekräftig
- Bugs in der Kriterien-Verarbeitung werden nicht erkannt

**Empfohlene Lösung**:
Mock sollte zumindest grundlegende Filterung simulieren:

```java
public List<ProfileSearchResult> searchFreelancers(
        final ProfileSearchCriteria criteria,
        final int offset,
        final int limit) {

    // Generiere Mock-Daten
    final List<ProfileSearchResult> allMockResults = generateMockData();

    // Simuliere Filterung
    List<ProfileSearchResult> filtered = allMockResults;

    // Filter nach Suchbegriff (simuliert)
    if (criteria.searchTerm() != null && !criteria.searchTerm().isBlank()) {
        filtered = filtered.stream()
            .filter(r -> r.name1().toLowerCase().contains(criteria.searchTerm().toLowerCase()) ||
                         r.name2().toLowerCase().contains(criteria.searchTerm().toLowerCase()))
            .toList();
    }

    // Filter nach Tagessatz
    if (criteria.salaryPerDayFrom() != null) {
        filtered = filtered.stream()
            .filter(r -> r.salaryPerDayLong() >= criteria.salaryPerDayFrom())
            .toList();
    }
    if (criteria.salaryPerDayTo() != null) {
        filtered = filtered.stream()
            .filter(r -> r.salaryPerDayLong() <= criteria.salaryPerDayTo())
            .toList();
    }

    // Simuliere Sortierung
    if (criteria.sortField() != null && SEARCH_SORT_FIELDS.contains(criteria.sortField())) {
        filtered = sortMockResults(filtered, criteria.sortField(), criteria.sortDir());
    }

    // Pagination
    final int start = Math.min(offset, filtered.size());
    final int end = Math.min(offset + limit, filtered.size());
    return filtered.subList(start, end);
}

public long countSearchFreelancers(final ProfileSearchCriteria criteria) {
    // Gleiche Filterlogik anwenden
    return searchFreelancers(criteria, 0, Integer.MAX_VALUE).size();
}
```

**Priorität**: Hoch (für Testbarkeit)

---

### Problem 5: Kontaktsperre-Feld in FreelancerSearchResult

**Beschreibung**:
Der Plan sagt, dass `FreelancerSearchResult` um `contactForbidden` erweitert werden muss. Aber es wird nicht geprüft, ob dieses Feld bereits existiert.

**Empfohlene Aktion**:
Vor der Implementierung prüfen:
```bash
grep -n "record FreelancerSearchResult" src/main/java/de/mirkosertic/powerstaff/freelancer/query/FreelancerSearchResult.java
```

Falls `contactForbidden` fehlt:
- Hinzufügen als `Boolean contactForbidden` (nullable)
- In `FreelancerQueryService.search()` das Feld aus `freelancer.contactforbidden` laden
- In `search-results.html` verwenden

**Priorität**: Hoch (Breaking Change wenn vergessen)

---

### Problem 6: Tag-Filter in Freiberufler-QBE nur für einzelnen Tag

**Beschreibung**:
Der Plan erweitert die Freiberufler-QBE um `tagId` (Singular). Aber was, wenn der User mehrere Tags in der Profilsuche ausgewählt hat?

**Aktueller Plan**:
Beim Klick auf einen Tag wird nur dieser eine Tag an die Freiberufler-QBE übergeben.

**Diskussion**:
- **Pro Einzeltag**: Einfacher, klarer Use-Case ("Zeige mir alle mit diesem Tag")
- **Contra**: Verlust der anderen ausgewählten Tags

**Empfohlene Lösung**:
Für diese Iteration: Einzeltag-Filter ist ausreichend. Der Use-Case ist: "Ich sehe einen interessanten Tag in den Ergebnissen und möchte alle Freiberufler mit diesem Tag sehen."

**Zukünftige Erweiterung**:
Freiberufler-QBE könnte später `tagIds` (Plural, comma-separated) unterstützen für Multi-Tag-Filter.

**Priorität**: Niedrig (aktueller Ansatz ist OK)

---

### Problem 7: URL-Länge bei vielen Tags

**Beschreibung**:
Bei Auswahl vieler Tags wird die URL sehr lang:
```
/profilesearch/search?searchTerm=Java&tagIds=1,2,3,4,5,6,7,8,9,10&salaryPerDayFrom=500&...
```

**Auswirkung**:
- Potenzielle Browser-/Server-Limits (meist 2048 Zeichen)
- Unhandliche URLs beim Bookmarking
- Cache-Keys werden groß

**Empfohlene Lösung**:
Für diese Iteration: Akzeptabel, da TagType.TYP vermutlich nicht hunderte von Tags hat.

**Zukünftige Lösung** (bei Bedarf):
- POST-basierte Suche mit Session-State
- URL-Shortening via Suche-ID

**Priorität**: Niedrig

---

### Problem 8: Infinite Scrolling ohne "Ende"-Indikator

**Beschreibung**:
Wenn alle Ergebnisse geladen sind, verschwindet das `<ps-infinite-scroll>` Element. Aber es gibt keinen visuellen Indikator "Alle Ergebnisse geladen".

**Auswirkung**:
User scrollen nach unten und wissen nicht, ob noch mehr Daten kommen oder ob sie am Ende sind.

**Empfohlene Lösung**:
Nach dem letzten `<ps-infinite-scroll>` (wenn `nextUrl == null`) eine kleine Info-Zeile anzeigen:

```html
<div th:if="${nextUrl == null and totalCount > PAGE_SIZE}"
     class="text-muted"
     style="text-align: center; padding: 1rem;">
  Alle [[${totalCount}]] Treffer geladen
</div>
```

**Priorität**: Niedrig (Nice-to-have)

---

### Problem 9: Freiberufler-QBE: Fehlende Tag-Anzeige

**Beschreibung**:
Die Freiberufler-QBE muss um Tag-Filter erweitert werden (`tagId`). Aber der Plan zeigt nicht, wie der ausgewählte Tag im Suchformular angezeigt wird.

**Empfohlene Lösung**:
Im Freiberufler-Formular (QBE):
```html
<!-- Neues Feld für Tag-Filter -->
<div class="fg">
  <label>Tag-Filter</label>
  <div th:if="${tagId != null}">
    <span class="chip">
      [[${tagName}]]
      <button class="chip-x"
              onclick="window.location='/freelancer/new'">×</button>
    </span>
  </div>
  <p th:if="${tagId == null}" class="text-muted">Kein Tag-Filter aktiv</p>
</div>
```

Dies erfordert, dass der Controller auch den Tag-Namen lädt:
```java
if (criteria.tagId() != null) {
    final var tag = tagQueryService.findById(criteria.tagId());
    model.addAttribute("tagName", tag.map(TagView::tagname).orElse("Unbekannt"));
}
```

**Priorität**: Mittel

---

## 3. Zusätzliche Verbesserungsvorschläge

### Verbesserung 1: Leeres Suchergebnis besser gestalten

**Aktueller Plan**:
```html
<p th:if="${#lists.isEmpty(results)}" class="text-muted">Keine Treffer gefunden.</p>
```

**Verbesserung**:
```html
<div th:if="${#lists.isEmpty(results)}"
     style="text-align: center; padding: 3rem 1rem;">
  <p style="font-size: 1.1rem; margin-bottom: 0.5rem;">Keine Treffer gefunden</p>
  <p class="text-muted" style="font-size: 0.85rem;">
    Versuchen Sie es mit anderen Suchkriterien oder weniger Filtern.
  </p>
</div>
```

**Priorität**: Niedrig

---

### Verbesserung 2: Sortierung visuell verdeutlichen

**Problem**:
Der sortierte Spaltenheader hat `.srt-asc` / `.srt-desc` Klassen, aber für unerfahrene User ist nicht klar, dass man auf die Spaltenheader klicken kann.

**Verbesserung**:
```css
/* In components2.css ergänzen */
.srt a {
  cursor: pointer;
  user-select: none;
}

.srt a:hover {
  color: var(--pri);
}
```

**Priorität**: Niedrig (CSS bereits vorhanden laut Grep-Ergebnis)

---

### Verbesserung 3: Tags in Suchergebnissen nach Typ gruppieren

**Problem**:
In den Suchergebnissen werden alle Tags eines Freiberuflers als flache Liste angezeigt. Bei vielen Tags wird das unübersichtlich.

**Verbesserung**:
Tags nach TagType gruppieren (wie im Freiberufler-Formular):

```html
<td>
  <div style="display: flex; flex-direction: column; gap: 4px;">
    <div th:each="type : ${T(de.mirkosertic.powerstaff.shared.TagType).values()}">
      <span th:each="tag : ${r.tags()}"
            th:if="${tag.type() == type.name()}"
            class="chip-xs"
            style="cursor:pointer"
            th:onclick="...">
        [[${tag.tagname()}]]
      </span>
    </div>
  </div>
</td>
```

**Problem**: Zu komplex für Tabellenzelle.

**Alternative**: Tags nur für ausgewählten TagType (TYP) anzeigen:
```html
<td>
  <span th:each="tag : ${r.tags()}"
        th:if="${tag.type() == 'TYP'}"
        class="chip-xs"
        th:onclick="...">
    [[${tag.tagname()}]]
  </span>
</td>
```

**Priorität**: Niedrig

---

### Verbesserung 4: Suchkriterien zurücksetzen

**Aktueller Plan**:
```html
<a href="/profilesearch/search" class="btn btn-ghost">Zurücksetzen</a>
```

**Problem**: Der Link führt zur Suche ohne Kriterien, aber zeigt dann einen Validierungsfehler ("Mindestens ein Suchkriterium angeben").

**Verbesserung**:
Zurücksetzen-Button führt zu einem leeren Formular, ohne Suche auszuführen:

```html
<a href="/profilesearch/search?reset=true" class="btn btn-ghost">Zurücksetzen</a>
```

Dann im Controller:
```java
@GetMapping("/search")
public String search(@ModelAttribute final ProfileSearchCriteria criteria,
                     @RequestParam(defaultValue = "false") final boolean reset,
                     ...) {

    if (reset) {
        // Zeige leeres Formular, keine Suche
        model.addAttribute("availableTags", sharedQueryService.findTagsByType(TagType.TYP));
        model.addAttribute("criteria", new ProfileSearchCriteria(null, null, null, null, null, null));
        model.addAttribute("rememberedProject", buildRememberedProjectInfo(principal));
        return "profilesearch/search-page";
    }

    // ... normale Suche
}
```

**Priorität**: Mittel

---

## 4. Zusammenfassung: Empfohlene Änderungen am Plan

### Sofort umzusetzen (Hoch)

1. **Suchbegriff optional machen**: Validierung auf "mindestens ein Kriterium"
2. **`returnTo` differenzieren**: `returnTo=profilesearch-chat` vs. `returnTo=profilesearch-search`
3. **Kontaktsperre-Feld prüfen**: `FreelancerSearchResult.contactForbidden` vor Implementierung verifizieren
4. **Mock-Service verbessern**: Grundlegende Filterung und Sortierung implementieren

### Mittelfristig (Mittel)

5. **Tag-Filter-Anzeige**: In Freiberufler-QBE den aktiven Tag anzeigen
6. **Zurücksetzen-Button**: Leeres Formular statt Validierungsfehler

### Optional (Niedrig)

7. **"Alle geladen"-Indikator**: Nach Infinite Scroll
8. **Leeres Suchergebnis**: Besseres Styling
9. **Tag-Gruppierung**: In Suchergebnissen

---

## 5. Design-System-Erweiterungen für SPEC

Die `UI-DESIGNSYSTEM.md` sollte um folgende Abschnitte ergänzt werden:

### Neu: §14.1 Selected Chip State

```markdown
#### Selected State (Tag-Auswahl)

Chips können als ausgewählt markiert werden (z.B. in Filterformularen):

```html
<div class="chip-row">
  <span class="chip selected">Java</span>
  <span class="chip">Python</span>
</div>
```

**Verhalten:**
- Ausgewählte Chips: blauer Hintergrund (`var(--pri)`), weißer Text
- Toggle via JavaScript: `chipElement.classList.toggle('selected')`
```

### Neu: §4.1 Tab-Navigation (Optional)

Falls die dedizierte Tab-Komponente später hinzugefügt wird:

```markdown
## Tab-Navigation

Für Ansichten mit mehreren Modi (z.B. Chat vs. Suche in Profilsuche):

```html
<div class="tabs">
  <a href="/path/view1" class="tab active">Ansicht 1</a>
  <a href="/path/view2" class="tab">Ansicht 2</a>
</div>
```

**Hinweis:** Alternativ kann `.btn-row` mit `.btn-pri` (aktiv) / `.btn-ghost` (inaktiv) verwendet werden.
```

---

## 6. Offene Fragen zur Klärung

1. **Suchbegriff-Pflicht**: Soll der Suchbegriff wirklich Pflichtfeld bleiben, oder reicht "mindestens ein Kriterium"?

2. **Tab-Navigation**: Button-basiert (einfach) oder dedizierte Tab-Komponente (sauberer)?

3. **Mock-Service**: Soll der Mock realistische Filterung simulieren, oder ist es OK, dass er immer dieselben Daten liefert?

4. **Tag-Klick**: Was ist die erwartete UX? Sollen ursprüngliche Suchkriterien erhalten bleiben?

5. **Kontaktsperre-Anzeige**: Reicht rote Hintergrundfarbe, oder soll zusätzlich ein Icon/Badge angezeigt werden?

---

## 7. Testfälle für manuelle Verifikation

Nach Implementierung testen:

### Basisfunktionalität
- [ ] Tab-Wechsel zwischen Chat und Suche funktioniert
- [ ] Suchformular validiert mindestens ein Kriterium
- [ ] Tags können ausgewählt/abgewählt werden (visuelles Feedback)
- [ ] Suche liefert Ergebnisse (Mock)
- [ ] Infinite Scrolling funktioniert

### Kontaktsperre
- [ ] Freiberufler mit `contactForbidden=true` haben roten Hintergrund (Profilsuche)
- [ ] Freiberufler mit `contactForbidden=true` haben roten Hintergrund (Freiberufler-QBE)

### Navigation
- [ ] Klick auf Suchergebnis öffnet Freiberufler-Detail mit `returnTo=profilesearch-search`
- [ ] Zurück-Button im Freiberufler-Detail führt zur Profilsuche-Suche
- [ ] Klick auf Tag startet Freiberufler-QBE mit `tagId=X`
- [ ] Zurück-Button in Freiberufler-QBE führt zur Profilsuche-Suche

### Edge Cases
- [ ] Suche ohne Kriterien → Validierungsfehler
- [ ] Suche mit nur Tags → funktioniert
- [ ] Suche mit nur Tagessatz → funktioniert
- [ ] Leeres Suchergebnis → "Keine Treffer"-Anzeige
- [ ] 50+ Ergebnisse → Infinite Scroll lädt nach
- [ ] Sortierung funktioniert (zumindest visuell, Mock ignoriert es)

---

**Fazit**: Der Plan ist grundsätzlich solide, benötigt aber Anpassungen in den Bereichen Validierung, Navigation und Mock-Implementierung, um eine gute User Experience zu gewährleisten.
