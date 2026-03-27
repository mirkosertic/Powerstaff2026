# Implementierungsplan: Klassische Profilsuche im Modul `profilesearch`

## Context

Das Modul `profilesearch` bietet aktuell nur eine Chat-basierte KI-Profilsuche. Dieser Plan erweitert das Modul um eine klassische Google-Style Suche mit Filterfeldern und sortierbaren Suchergebnissen. Die beiden Bereiche (Chat und Suche) werden über eine Tab-Navigation zugänglich gemacht.

**Warum**: Nutzer möchten neben der KI-gestützten Chat-Suche auch eine klassische Suchoberfläche mit direkten Filterfeldern haben.

**Ziel**: Zweigeteile Profilsuche mit:
- Chat (bestehend, Standard)
- Klassische Suche (neu) mit Mock-Service für erste Version

---

## Anforderungen

### Funktionale Anforderungen

1. **Tab-Navigation**: Umschalten zwischen "Chat" und "Suche"
2. **Suchformular**:
   - Suchbegriff (Textfeld, optional)
   - Tagessatz von/bis (Zahlenfelder, optional)
   - Tag-Liste (TagType==TYP, Mehrfachauswahl, optional)
   - **Validierung**: Mindestens eines der Felder muss ausgefüllt sein
3. **Suchergebnisse**:
   - Anzeige: Name1, Name2, Letzter Kontakt, Tagessatz, Verfügbarkeit, Code, Tags
   - Sortierbar nach: Name1, Name2, Letzter Kontakt, Tagessatz, Verfügbarkeit, Code
   - Infinite Scrolling (PAGE_SIZE=20)
   - Klick auf Zeile → Freiberufler-Detail (`/freelancer/{id}?returnTo=profilesearch-search`)
   - Klick auf Tag → Freiberufler-QBE mit Tag-Filter (`/freelancer/search?tagId={id}&returnTo=profilesearch-search`)
4. **Visuelle Markierung**: Freiberufler mit Kontaktsperre (`contactforbidden=true`) rot markieren
5. **Zurück-Navigation**: Von Freiberufler-Detail und Freiberufler-QBE zurück zur Profilsuche-Suche

### Nicht-funktionale Anforderungen

- **Mock-Service**: Erste Version mit Mock-Daten (echte Suche später)
- **ADR-002**: Query-Seite darf cross-module DB-Reads machen
- **ADR-017**: Kein HttpSession, State in URL-Parametern
- **ADR-019**: GET-basiert, bookmarkable URLs, Cache-Control-Header
- **Keine neuen CSS-Klassen**: Nur bestehende CSS-Klassen verwenden

---

## Architektur-Entscheidungen

### 1. URL-Struktur

**Entscheidung**: Separate URLs für Chat und Suche

- `/profilesearch` → redirect zu `/profilesearch/chat`
- `/profilesearch/chat` → redirect zu neuester Chat oder neuen Chat erstellen
- `/profilesearch/chat/{chatId}` → Chat-Ansicht (bestehend)
- `/profilesearch/search` → Klassische Suche (neu)

**Begründung**:
- Stateless (ADR-017)
- Bookmarkable (ADR-019)
- Browser-Back funktioniert natürlich

### 2. Mock-Service vs. Echte Suche

**Entscheidung**: ProfileSearchQueryService mit Mock-Implementierung

**Begründung**:
- Schnelle erste Version zum Testen der UI
- Echte SQL-Abfrage wird später implementiert
- Mock liefert realistische Testdaten mit allen erforderlichen Feldern

### 3. Kontaktsperre-Markierung

**Entscheidung**: Inline-Style für Tabellenzeilen mit `contactforbidden=true`

```html
<tr th:style="${r.contactForbidden()} ? 'background: var(--danger-l); color: var(--danger)' : ''">
```

**Begründung**:
- Keine neue CSS-Klasse erforderlich
- Wiederverwendung bestehender CSS-Variablen (`--danger-l`, `--danger`)
- Gilt für Profilsuche UND Freiberufler-QBE

### 4. Tag-Klick Navigation

**Entscheidung**: Freiberufler-QBE erweitern um `tagId`-Parameter

**Begründung**:
- User möchte alle Freiberufler mit einem bestimmten Tag finden
- QBE-Suche muss um Tag-Filter erweitert werden (neuer Parameter)
- Zurück-Navigation muss `returnTo=profilesearch` unterstützen

---

## Implementierung

### Phase 1: Backend - Mock-Service

#### 1.1 Data Transfer Objects erstellen

**Datei**: `src/main/java/de/mirkosertic/powerstaff/profilesearch/query/ProfileSearchCriteria.java`

```java
package de.mirkosertic.powerstaff.profilesearch.query;

public record ProfileSearchCriteria(
    String searchTerm,          // Pflichtfeld
    Long salaryPerDayFrom,      // Optional
    Long salaryPerDayTo,        // Optional
    String tagIds,              // Comma-separated IDs, optional
    String sortField,           // name1, name2, last_contact_date, salary_per_day_long, availability_as_date, code
    String sortDir              // asc, desc
) {
}
```

**Datei**: `src/main/java/de/mirkosertic/powerstaff/profilesearch/query/ProfileSearchResult.java`

```java
package de.mirkosertic.powerstaff.profilesearch.query;

import de.mirkosertic.powerstaff.shared.query.TagView;
import java.time.LocalDateTime;
import java.util.List;

public record ProfileSearchResult(
    Long id,
    String code,
    String name1,
    String name2,
    LocalDateTime lastContactDate,
    Long salaryPerDayLong,
    LocalDateTime availabilityAsDate,
    boolean contactForbidden,
    List<TagView> tags
) {
}
```

#### 1.2 ProfileSearchQueryService erweitern

**Datei**: `src/main/java/de/mirkosertic/powerstaff/profilesearch/query/ProfileSearchQueryService.java`

Methoden hinzufügen:

```java
private static final Set<String> SEARCH_SORT_FIELDS = Set.of(
    "name1", "name2", "last_contact_date", "salary_per_day_long",
    "availability_as_date", "code"
);

public List<ProfileSearchResult> searchFreelancers(
        final ProfileSearchCriteria criteria,
        final int offset,
        final int limit) {

    // MOCK-IMPLEMENTIERUNG - echte Suche später
    // Generiere 50 Mock-Ergebnisse
    final List<ProfileSearchResult> mockResults = new ArrayList<>();
    for (int i = 1; i <= 50; i++) {
        mockResults.add(new ProfileSearchResult(
            (long) i,
            "CODE-" + i,
            "Name" + i,
            "Vorname" + i,
            LocalDateTime.now().minusDays(i * 10),
            500L + (i * 10),
            LocalDateTime.now().plusDays(i * 5),
            i % 7 == 0, // Jeder 7. hat Kontaktsperre
            List.of(
                new TagView((long) i, "Java", "TYP"),
                new TagView((long) (i + 100), "Remote", "EINSATZORT")
            )
        ));
    }

    // OPTIONAL: Grundlegende Filterung simulieren für bessere Testbarkeit
    List<ProfileSearchResult> filtered = mockResults;

    if (criteria.searchTerm() != null && !criteria.searchTerm().isBlank()) {
        final String search = criteria.searchTerm().toLowerCase();
        filtered = filtered.stream()
            .filter(r -> r.name1().toLowerCase().contains(search) ||
                         r.name2().toLowerCase().contains(search))
            .toList();
    }

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

    // Pagination
    final int start = Math.min(offset, filtered.size());
    final int end = Math.min(offset + limit, filtered.size());
    return filtered.subList(start, end);
}

public long countSearchFreelancers(final ProfileSearchCriteria criteria) {
    // MOCK - Wendet gleiche Filterung an wie searchFreelancers
    return searchFreelancers(criteria, 0, Integer.MAX_VALUE).size();
}
```

#### 1.3 ProfileSearchController erweitern

**Datei**: `src/main/java/de/mirkosertic/powerstaff/profilesearch/api/ProfileSearchController.java`

1. **Dependency Injection erweitern**:
   - `SharedQueryService` für Tag-Laden injizieren

2. **index()** ändern:
```java
@GetMapping
public void index(final Principal principal, final HttpServletResponse response) throws IOException {
    response.sendRedirect("/profilesearch/chat");
}
```

3. **chatIndex()** hinzufügen:
```java
@GetMapping("/chat")
public void chatIndex(final Principal principal, final HttpServletResponse response) throws IOException {
    final String userId = principal.getName();
    final var latestChatId = queryService.findLatestChatByUser(userId);
    if (latestChatId.isPresent()) {
        response.sendRedirect("/profilesearch/chat/" + latestChatId.get());
    } else {
        final Long projectId = rememberedProjectService.get(userId).orElse(null);
        final Long chatId = commandService.createChat(userId, projectId);
        response.sendRedirect("/profilesearch/chat/" + chatId);
    }
}
```

4. **search()** hinzufügen:
```java
@GetMapping("/search")
public String search(@ModelAttribute final ProfileSearchCriteria criteria,
                     @RequestParam(defaultValue = "0") final int offset,
                     final Model model,
                     final HttpServletResponse response,
                     final Principal principal) {

    // Validierung: Mindestens ein Suchkriterium muss angegeben sein (nur bei offset=0)
    if (offset == 0) {
        final boolean hasSearchTerm = criteria.searchTerm() != null && !criteria.searchTerm().isBlank();
        final boolean hasSalaryFilter = criteria.salaryPerDayFrom() != null || criteria.salaryPerDayTo() != null;
        final boolean hasTagFilter = criteria.tagIds() != null && !criteria.tagIds().isBlank();

        if (!hasSearchTerm && !hasSalaryFilter && !hasTagFilter) {
            model.addAttribute("error", "Bitte mindestens ein Suchkriterium angeben");
            model.addAttribute("availableTags", sharedQueryService.findTagsByType(TagType.TYP));
            model.addAttribute("criteria", criteria);
            model.addAttribute("rememberedProject", buildRememberedProjectInfo(principal));
            return "profilesearch/search-page";
        }
    }

    // Infinite scroll fragment
    if (offset > 0) {
        final var results = queryService.searchFreelancers(criteria, offset, PAGE_SIZE);
        final long total = queryService.countSearchFreelancers(criteria);
        final int nextOffset = offset + PAGE_SIZE;
        if (nextOffset < total) {
            response.setHeader("X-Next-Url", buildSearchMoreUrl(criteria, nextOffset));
        }
        model.addAttribute("results", results);
        return "profilesearch/search-results :: results";
    }

    // Full page - Cache-Control per ADR-019
    response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "0");

    // Tags für Filter laden
    final var availableTags = sharedQueryService.findTagsByType(TagType.TYP);

    // Suche ausführen
    final var results = queryService.searchFreelancers(criteria, 0, PAGE_SIZE);
    final long total = queryService.countSearchFreelancers(criteria);

    model.addAttribute("results", results);
    model.addAttribute("totalCount", total);
    model.addAttribute("criteria", criteria);
    model.addAttribute("sortField", criteria.sortField());
    model.addAttribute("sortDir", criteria.sortDir());
    model.addAttribute("availableTags", availableTags);
    model.addAttribute("rememberedProject", buildRememberedProjectInfo(principal));

    final String nextUrl = results.size() == PAGE_SIZE ?
        buildSearchMoreUrl(criteria, PAGE_SIZE) : null;
    model.addAttribute("nextUrl", nextUrl);

    return "profilesearch/search-page";
}

private String buildSearchMoreUrl(final ProfileSearchCriteria c, final int offset) {
    final var b = UriComponentsBuilder.fromPath("/profilesearch/search")
        .queryParam("offset", offset);
    if (c.searchTerm() != null) b.queryParam("searchTerm", c.searchTerm());
    if (c.salaryPerDayFrom() != null) b.queryParam("salaryPerDayFrom", c.salaryPerDayFrom());
    if (c.salaryPerDayTo() != null) b.queryParam("salaryPerDayTo", c.salaryPerDayTo());
    if (c.tagIds() != null) b.queryParam("tagIds", c.tagIds());
    if (c.sortField() != null) b.queryParam("sortField", c.sortField());
    if (c.sortDir() != null) b.queryParam("sortDir", c.sortDir());
    return b.encode().build().toUriString();
}
```

5. **SharedQueryService injizieren**:
```java
private final SharedQueryService sharedQueryService;

public ProfileSearchController(..., final SharedQueryService sharedQueryService) {
    ...
    this.sharedQueryService = sharedQueryService;
}
```

---

### Phase 2: Frontend - Templates

#### 2.1 Chat-Template aktualisieren

**Datei**: `src/main/resources/templates/profilesearch/form.html`

Tab-Navigation am Anfang von `<div id="chat-page">` hinzufügen:

```html
<div id="chat-page">
  <!-- Tab Navigation -->
  <div class="btn-row mb-md" style="padding: 0 1rem;">
    <a href="/profilesearch/chat" class="btn btn-pri">Chat</a>
    <a href="/profilesearch/search" class="btn btn-ghost">Suche</a>
  </div>

  <!-- Rest bleibt unverändert -->
```

#### 2.2 Search-Page Template erstellen

**Datei**: `src/main/resources/templates/profilesearch/search-page.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      th:replace="~{fragments/layout :: layout('Profilsuche', 'profilesearch', ~{::body})}">
<body>

  <!-- Toolbar -->
  <div th:replace="~{fragments/toolbar :: toolbar(
      'Profilsuche',
      null, null, null, null, null, null,
      null, null,
      null,
      null,
      ${rememberedProject},
      null,
      null)}">
  </div>

  <div id="page">

    <!-- Tab Navigation -->
    <div class="btn-row mb-md">
      <a href="/profilesearch/chat" class="btn btn-ghost">Chat</a>
      <a href="/profilesearch/search" class="btn btn-pri">Suche</a>
    </div>

    <!-- Error Message -->
    <div th:if="${error}" class="banner banner-error mb-md" th:text="${error}"></div>

    <!-- Search Form -->
    <form method="get" action="/profilesearch/search" class="mb-lg">
      <div class="fcard">
        <div class="fcard-hd">
          <div class="fcard-title">Suchkriterien</div>
        </div>
        <div class="fcard-body">
          <div class="form-grid">

            <!-- Search Term (Optional) -->
            <div class="fg">
              <label for="searchTerm">Suchbegriff</label>
              <input type="text"
                     id="searchTerm"
                     name="searchTerm"
                     th:value="${criteria?.searchTerm()}"
                     placeholder="Name, Skills..."
                     data-testid="field-search-term">
            </div>

            <!-- Salary Range -->
            <div class="fg2">
              <div class="fg">
                <label for="salaryPerDayFrom">Tagessatz von (€)</label>
                <input type="number"
                       id="salaryPerDayFrom"
                       name="salaryPerDayFrom"
                       th:value="${criteria?.salaryPerDayFrom()}"
                       data-testid="field-salary-from">
              </div>

              <div class="fg">
                <label for="salaryPerDayTo">Tagessatz bis (€)</label>
                <input type="number"
                       id="salaryPerDayTo"
                       name="salaryPerDayTo"
                       th:value="${criteria?.salaryPerDayTo()}"
                       data-testid="field-salary-to">
              </div>
            </div>

            <!-- Tags (multi-select chips) -->
            <div class="fg col-wide">
              <label>Typ-Tags</label>
              <div class="chip-row" id="tag-selector">
                <span th:each="tag : ${availableTags}"
                      class="chip"
                      th:classappend="${criteria?.tagIds() != null and #strings.contains(criteria.tagIds(), tag.id().toString())} ? ' selected' : ''"
                      th:data-tag-id="${tag.id()}"
                      onclick="toggleTag(this)">
                  [[${tag.tagname()}]]
                </span>
              </div>
              <input type="hidden" id="tagIds" name="tagIds" th:value="${criteria?.tagIds()}">
            </div>

          </div>
        </div>
      </div>

      <div class="btn-row mt-md">
        <button type="submit" class="btn btn-pri" data-testid="btn-search">Suchen</button>
        <a href="/profilesearch/search" class="btn btn-ghost">Zurücksetzen</a>
      </div>
    </form>

    <!-- Results -->
    <div th:if="${totalCount != null}">
      <div class="btn-row mb-md">
        <span class="text-muted" th:text="${totalCount} + ' Treffer'"></span>
      </div>

      <p th:if="${#lists.isEmpty(results)}" class="text-muted">Keine Treffer gefunden.</p>

      <div class="tbl-wrap" th:if="${!#lists.isEmpty(results)}">
        <table>
          <thead>
            <tr>
              <th class="srt" th:classappend="${sortField == 'name1'} ? (${sortDir == 'asc'} ? ' srt-asc' : ' srt-desc') : ''">
                <a th:href="@{/profilesearch/search(
                    searchTerm=${criteria.searchTerm()},
                    salaryPerDayFrom=${criteria.salaryPerDayFrom()},
                    salaryPerDayTo=${criteria.salaryPerDayTo()},
                    tagIds=${criteria.tagIds()},
                    sortField='name1',
                    sortDir=${sortField == 'name1' and sortDir == 'asc' ? 'desc' : 'asc'}
                )}">Name 1</a>
              </th>
              <th class="srt" th:classappend="${sortField == 'name2'} ? (${sortDir == 'asc'} ? ' srt-asc' : ' srt-desc') : ''">
                <a th:href="@{/profilesearch/search(
                    searchTerm=${criteria.searchTerm()},
                    salaryPerDayFrom=${criteria.salaryPerDayFrom()},
                    salaryPerDayTo=${criteria.salaryPerDayTo()},
                    tagIds=${criteria.tagIds()},
                    sortField='name2',
                    sortDir=${sortField == 'name2' and sortDir == 'asc' ? 'desc' : 'asc'}
                )}">Name 2</a>
              </th>
              <th class="srt" th:classappend="${sortField == 'last_contact_date'} ? (${sortDir == 'asc'} ? ' srt-asc' : ' srt-desc') : ''">
                <a th:href="@{/profilesearch/search(
                    searchTerm=${criteria.searchTerm()},
                    salaryPerDayFrom=${criteria.salaryPerDayFrom()},
                    salaryPerDayTo=${criteria.salaryPerDayTo()},
                    tagIds=${criteria.tagIds()},
                    sortField='last_contact_date',
                    sortDir=${sortField == 'last_contact_date' and sortDir == 'asc' ? 'desc' : 'asc'}
                )}">Letzter Kontakt</a>
              </th>
              <th class="srt" th:classappend="${sortField == 'salary_per_day_long'} ? (${sortDir == 'asc'} ? ' srt-asc' : ' srt-desc') : ''">
                <a th:href="@{/profilesearch/search(
                    searchTerm=${criteria.searchTerm()},
                    salaryPerDayFrom=${criteria.salaryPerDayFrom()},
                    salaryPerDayTo=${criteria.salaryPerDayTo()},
                    tagIds=${criteria.tagIds()},
                    sortField='salary_per_day_long',
                    sortDir=${sortField == 'salary_per_day_long' and sortDir == 'asc' ? 'desc' : 'asc'}
                )}">Tagessatz</a>
              </th>
              <th class="srt" th:classappend="${sortField == 'availability_as_date'} ? (${sortDir == 'asc'} ? ' srt-asc' : ' srt-desc') : ''">
                <a th:href="@{/profilesearch/search(
                    searchTerm=${criteria.searchTerm()},
                    salaryPerDayFrom=${criteria.salaryPerDayFrom()},
                    salaryPerDayTo=${criteria.salaryPerDayTo()},
                    tagIds=${criteria.tagIds()},
                    sortField='availability_as_date',
                    sortDir=${sortField == 'availability_as_date' and sortDir == 'asc' ? 'desc' : 'asc'}
                )}">Verfügbar ab</a>
              </th>
              <th class="srt" th:classappend="${sortField == 'code'} ? (${sortDir == 'asc'} ? ' srt-asc' : ' srt-desc') : ''">
                <a th:href="@{/profilesearch/search(
                    searchTerm=${criteria.searchTerm()},
                    salaryPerDayFrom=${criteria.salaryPerDayFrom()},
                    salaryPerDayTo=${criteria.salaryPerDayTo()},
                    tagIds=${criteria.tagIds()},
                    sortField='code',
                    sortDir=${sortField == 'code' and sortDir == 'asc' ? 'desc' : 'asc'}
                )}">Code</a>
              </th>
              <th>Tags</th>
            </tr>
          </thead>
          <tbody>
            <tr th:each="r : ${results}"
                th:attr="data-testid='freelancer-row-' + ${r.id()}"
                th:onclick="|window.location='/freelancer/${r.id()}?returnTo=profilesearch-search'|"
                th:style="${r.contactForbidden()} ? 'background: var(--danger-l); color: var(--danger); cursor: pointer' : 'cursor: pointer'">
              <td class="td-name" th:text="${r.name1()}"></td>
              <td th:text="${r.name2()}"></td>
              <td th:text="${r.lastContactDate() != null} ? ${#temporals.format(r.lastContactDate(), 'dd.MM.yyyy')} : ''"></td>
              <td th:text="${r.salaryPerDayLong() != null} ? ${r.salaryPerDayLong()} + ' €' : ''"></td>
              <td th:text="${r.availabilityAsDate() != null} ? ${#temporals.format(r.availabilityAsDate(), 'dd.MM.yyyy')} : ''"></td>
              <td th:text="${r.code()}"></td>
              <td>
                <span th:each="tag : ${r.tags()}"
                      class="chip-xs"
                      style="cursor:pointer"
                      th:onclick="|event.stopPropagation(); window.location='/freelancer/search?tagId=${tag.id()}&returnTo=profilesearch'|"
                      th:title="'Suche nach Tag: ' + ${tag.tagname()}">
                  [[${tag.tagname()}]]
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <ps-infinite-scroll
          th:if="${nextUrl != null}"
          th:attr="data-next-url=${nextUrl}"
          data-target=".tbl-wrap table tbody">
      </ps-infinite-scroll>
    </div>

  </div>

  <!-- JavaScript for tag selection -->
  <script th:inline="javascript">
    /*<![CDATA[*/
    function toggleTag(chipEl) {
      const tagId = chipEl.dataset.tagId;
      const hiddenInput = document.getElementById('tagIds');
      let currentIds = hiddenInput.value ? hiddenInput.value.split(',') : [];

      if (chipEl.classList.contains('selected')) {
        currentIds = currentIds.filter(id => id !== tagId);
        chipEl.classList.remove('selected');
      } else {
        currentIds.push(tagId);
        chipEl.classList.add('selected');
      }

      hiddenInput.value = currentIds.filter(id => id).join(',');
    }
    /*]]>*/
  </script>

</body>
</html>
```

#### 2.3 Search-Results Fragment erstellen

**Datei**: `src/main/resources/templates/profilesearch/search-results.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>

<div th:fragment="results" th:remove="tag">
  <tr th:each="r : ${results}"
      th:attr="data-testid='freelancer-row-' + ${r.id()}"
      th:onclick="|window.location='/freelancer/${r.id()}?returnTo=profilesearch-search'|"
      th:style="${r.contactForbidden()} ? 'background: var(--danger-l); color: var(--danger); cursor: pointer' : 'cursor: pointer'">
    <td class="td-name" th:text="${r.name1()}"></td>
    <td th:text="${r.name2()}"></td>
    <td th:text="${r.lastContactDate() != null} ? ${#temporals.format(r.lastContactDate(), 'dd.MM.yyyy')} : ''"></td>
    <td th:text="${r.salaryPerDayLong() != null} ? ${r.salaryPerDayLong()} + ' €' : ''"></td>
    <td th:text="${r.availabilityAsDate() != null} ? ${#temporals.format(r.availabilityAsDate(), 'dd.MM.yyyy')} : ''"></td>
    <td th:text="${r.code()}"></td>
    <td>
      <span th:each="tag : ${r.tags()}"
            class="chip-xs"
            style="cursor:pointer"
            th:onclick="|event.stopPropagation(); window.location='/freelancer/search?tagId=${tag.id()}&returnTo=profilesearch-search'|"
            th:title="'Suche nach Tag: ' + ${tag.tagname()}">
        [[${tag.tagname()}]]
      </span>
    </td>
  </tr>
</div>

</body>
</html>
```

#### 2.4 CSS für ausgewählte Tags

**Datei**: `src/main/frontend/src/css/components2.css`

Am Ende der Datei hinzufügen:

```css
/* Selected chip state for tag filtering (profilsuche) */
.chip.selected {
  background: var(--pri);
  color: white;
}
```

---

### Phase 3: Freiberufler-Modul erweitern

#### 3.1 FreelancerSearchCriteria erweitern

**Datei**: `src/main/java/de/mirkosertic/powerstaff/freelancer/query/FreelancerSearchCriteria.java`

Parameter `tagId` hinzufügen:

```java
public record FreelancerSearchCriteria(
    // ... bestehende Parameter ...
    Long tagId,  // NEU: Filter nach Tag-ID
    String sortField,
    String sortDir
) {
}
```

#### 3.2 FreelancerQueryService erweitern

**Datei**: `src/main/java/de/mirkosertic/powerstaff/freelancer/query/FreelancerQueryService.java`

In `search()` und `countSearch()` Methoden:

```java
// In appendStringCriteria() oder direkt in search():
if (criteria.tagId() != null) {
    sql.append(" AND EXISTS (SELECT 1 FROM freelancer_tags ft WHERE ft.freelancer_id = freelancer.id AND ft.tag_id = :tagId)");
    params.put("tagId", criteria.tagId());
}
```

#### 3.3 FreelancerController erweitern

**Datei**: `src/main/java/de/mirkosertic/powerstaff/freelancer/api/FreelancerController.java`

1. **show()** Methode erweitern um `returnTo` Parameter:

```java
@GetMapping("/{id}")
public String show(@PathVariable final long id,
                   @RequestParam(required = false) final String returnTo,
                   final HttpServletResponse response,
                   final Model model,
                   final Principal principal) {
    // ... bestehender Code ...
    model.addAttribute("returnTo", returnTo);
    return "freelancer/form";
}
```

2. **search()** Methode erweitern um `returnTo` Parameter:

```java
@GetMapping("/search")
public String search(@ModelAttribute final FreelancerSearchCriteria criteria,
                     @RequestParam(required = false, defaultValue = "0") final int offset,
                     @RequestParam(required = false) final String returnTo,
                     final Model model,
                     final HttpServletResponse response) {
    // ... bestehender Code ...
    model.addAttribute("returnTo", returnTo);
    // ...
}
```

#### 3.4 Freiberufler-Templates erweitern

**Datei**: `src/main/resources/templates/freelancer/search-page.html`

Zurück-Button erweitern:

```html
<div class="btn-row mb-md">
  <a class="btn-ghost"
     th:href="${returnTo == 'profilesearch-search'} ? '/profilesearch/search' : ${editSearchUrl}">
    &#8592; <span th:text="${returnTo == 'profilesearch-search'} ? 'Zurück zur Profilsuche' : 'Suche bearbeiten'"></span>
  </a>
  <span class="text-muted" th:text="${totalCount} + ' Treffer'"></span>
</div>
```

**Datei**: `src/main/resources/templates/freelancer/search-results.html`

Kontaktsperre-Markierung hinzufügen:

```html
<tr th:each="r : ${results}"
    th:attr="data-testid='freelancer-row-' + ${r.id()}"
    th:onclick="|window.location='/freelancer/${r.id()}' + (${returnTo != null} ? '?returnTo=' + ${returnTo} : '')|"
    th:style="${r.contactForbidden != null and r.contactForbidden} ? 'background: var(--danger-l); color: var(--danger); cursor: pointer' : 'cursor: pointer'">
```

**HINWEIS**: `FreelancerSearchResult` muss um `contactForbidden` erweitert werden (falls nicht vorhanden).

**Datei**: `src/main/java/de/mirkosertic/powerstaff/freelancer/query/FreelancerSearchResult.java`

```java
public record FreelancerSearchResult(
    // ... bestehende Felder ...
    Boolean contactForbidden  // NEU
)
```

**Datei**: `src/main/resources/templates/freelancer/form.html`

Toolbar-Zurück-Button erweitern (falls `returnTo=profilesearch`):

```html
<!-- In Toolbar: Zurück-Button hinzufügen wenn returnTo gesetzt -->
<a th:if="${returnTo == 'profilesearch-search'}"
   class="btn btn-ghost"
   href="/profilesearch/search">
  &#8592; Zurück zur Profilsuche
</a>
```

---

## Kritische Dateien

### Neu zu erstellen:
1. `src/main/java/de/mirkosertic/powerstaff/profilesearch/query/ProfileSearchCriteria.java`
2. `src/main/java/de/mirkosertic/powerstaff/profilesearch/query/ProfileSearchResult.java`
3. `src/main/resources/templates/profilesearch/search-page.html`
4. `src/main/resources/templates/profilesearch/search-results.html`

### Zu modifizieren:
1. `src/main/java/de/mirkosertic/powerstaff/profilesearch/query/ProfileSearchQueryService.java`
2. `src/main/java/de/mirkosertic/powerstaff/profilesearch/api/ProfileSearchController.java`
3. `src/main/resources/templates/profilesearch/form.html`
4. `src/main/frontend/src/css/components2.css`
5. `src/main/java/de/mirkosertic/powerstaff/freelancer/query/FreelancerSearchCriteria.java`
6. `src/main/java/de/mirkosertic/powerstaff/freelancer/query/FreelancerQueryService.java`
7. `src/main/java/de/mirkosertic/powerstaff/freelancer/query/FreelancerSearchResult.java`
8. `src/main/java/de/mirkosertic/powerstaff/freelancer/api/FreelancerController.java`
9. `src/main/resources/templates/freelancer/search-page.html`
10. `src/main/resources/templates/freelancer/search-results.html`
11. `src/main/resources/templates/freelancer/form.html`

---

## Verifikation & Testing

### Manuelle Tests

1. **Tab-Navigation**:
   - `/profilesearch` → redirect zu Chat
   - Tab "Suche" klicken → `/profilesearch/search`
   - Tab "Chat" klicken → zurück zu Chat

2. **Suchformular**:
   - Ohne Suchbegriff absenden → Fehlermeldung
   - Mit Suchbegriff absenden → Ergebnisse anzeigen
   - Tags auswählen → visuell markiert (blau)
   - Tagessatz-Filter setzen → funktioniert (Mock ignoriert es)

3. **Suchergebnisse**:
   - 50 Mock-Ergebnisse
   - Infinite Scrolling funktioniert (20er Seiten)
   - Sortierung funktioniert (Mock: unsortiert)
   - Jeder 7. Freiberufler hat roten Hintergrund (Kontaktsperre)
   - Klick auf Zeile → `/freelancer/{id}?returnTo=profilesearch`
   - Klick auf Tag → `/freelancer/search?tagId={id}&returnTo=profilesearch`

4. **Freiberufler-QBE**:
   - Tag-Filter funktioniert (`?tagId=...`)
   - Zurück-Button zeigt "Zurück zur Profilsuche" wenn `returnTo=profilesearch`
   - Kontaktsperre rot markiert

5. **Freiberufler-Detail**:
   - Zurück-Button zur Profilsuche wenn `returnTo=profilesearch`

### Automatisierte Tests (später)

**Unit-Tests** (`*Spec.groovy`):
- `ProfileSearchQueryServiceSpec`: Mock-Methoden testen
- `ProfileSearchControllerSpec`: Routing, Model-Attribute

**Integrationstests** (`*IT.java`):
- Später bei echter Implementierung

**E2E-Tests** (`*.spec.ts`):
```typescript
test('can switch between chat and search tabs', async ({ page }) => {
  await page.goto('/profilesearch');
  await expect(page).toHaveURL(/\/profilesearch\/chat/);

  await page.click('a.btn:has-text("Suche")');
  await expect(page).toHaveURL('/profilesearch/search');

  await page.click('a.btn:has-text("Chat")');
  await expect(page).toHaveURL(/\/profilesearch\/chat/);
});

test('search requires search term', async ({ page }) => {
  await page.goto('/profilesearch/search');
  await page.click('button[data-testid="btn-search"]');
  await expect(page.locator('.banner-error')).toBeVisible();
});

test('can perform search with tags', async ({ page }) => {
  await page.goto('/profilesearch/search');
  await page.fill('#searchTerm', 'Java');
  await page.click('.chip:first-child'); // Tag auswählen
  await page.click('button[data-testid="btn-search"]');

  await expect(page.locator('table tbody tr')).toHaveCount(20); // Erste Seite
  await expect(page.locator('[data-testid="freelancer-row-7"]')).toHaveCSS('background-color', /fef2f2/); // Kontaktsperre
});

test('clicking tag navigates to freelancer search', async ({ page }) => {
  await page.goto('/profilesearch/search?searchTerm=test');
  await page.click('.chip-xs:first-child');
  await expect(page).toHaveURL(/\/freelancer\/search\?tagId=/);
  await expect(page).toHaveURL(/returnTo=profilesearch/);
});
```

---

## Offene Punkte für spätere Implementierung

1. **Echte Suche**: Mock durch SQL-Query ersetzen
   - Suchbegriff durchsucht `name1`, `name2`, `skills` mit LIKE
   - Tagessatz-Filter mit `>=` und `<=`
   - Tag-Filter mit JOIN auf `freelancer_tags`
   - Batch-Loading der Tags für Suchergebnisse

2. **Sortierung**: SQL ORDER BY statt Mock

3. **Performance-Optimierung**: Indexes auf `freelancer.name1`, `freelancer.salary_per_day_long`, etc.

4. **Paginierung**: URL-Parameter für Offset persistieren

5. **Suchkriterien-Persistierung**: Cookie oder URL-Parameter für "letzte Suche"
