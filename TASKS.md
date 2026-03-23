# TASKS.md – Powerstaff 2026 Implementierungsplan

Dieses Dokument listet alle Implementierungsaufgaben als granulare Checkpunkte.
Der Agent markiert jede abgeschlossene Task mit `[x]` und erstellt danach einen Git-Commit.

**Konventionen:**
- `[ ]` = offen · `[x]` = erledigt · `[-]` = übersprungen / nicht applicable
- Jede Task endet mit einem Git-Commit (vgl. CLAUDE.md – Commit-Konvention)
- Reihenfolge ist verbindlich: Abhängigkeiten nach oben hin auflösen

---

## Fehlerkorrekturen

- [x] Die Testabdeckung von FreelancerController.java ist nicht optimal. Die Methode buildSearchMoreUrl ist gar nicht getestet.
- [x] Die Testabdeckung von FreelancerController.java ist nicht optimal. Die Methode buildEditSearchUrl ist nur partiell getestet.
- [x] FreelancerController.java: Hier stellt sich auch die Frage, ob der /search-more nicht redundant zum /search Endpunkt ist,
    /search-more hat ja die gleiche API, aber zusätzlich einen offset. /search und /search-more verhalten sich gleich, wenn der offset 0 ist.
    Ich wäre dafür, den Offset in die API von /search als optionalen Parameter aufzunehmen, und den Default auf 0 zu setzen.
- [x] FreelancerCommandService: Das Löschen einer Kontaktmöglichkeit sollte nicht möglich sein und einen Fehler werden. Das gilt auch für die Kontakthistorie.
    Dieser Fehler muss für die Formulare Freelander, Partner und Kunden korrigiert werden. Die Korrektur soll über entsprechende
    Tests abgesichert werden.
- [x] FreelancerCommandService: Es gibt keine Tests für das Hinzufügen und Entfernen von Tags zu einem Freiberufler. Diese
    Tests müssen noch implementiert werden, so wie es am Sinnvollsten ist (Unit vs. IT Test)
- [x] FreelancerCommandService.java: Es gibt keine Tests für die Aktualisierung und das Löschen eines Kontakthistorieneintrages.
    Diese Tests müssen implementiert werden, und auch für Partner, Kunden und Projekte.
- [x] FreelancerCommandService: Es darf nicht möglich sein, einen Kontakthistorieneintrag ohne ID zu Ändern oder zu Löschen. Dies
    muss einen Fehler werden, und durch entsprechende Tests überprüft werden.
- [x] FreelancerCOmmandService: Es gibt keine Tests für die Zuweisung eines Freiberuflers zu einem Partner, und für das
    Löschen einer derartigen Zuweisung.
- [x] ProjectController.java: Es gibt keine Tests für die Abfrage der Projektpositionen sowie für die Zuordnung und die Löschung.
- [x] ProjectController.java: Die Methode buildEditSearchUrl ist nur partiell getestet.
- [x] Prüfe bitte, dass alle QBE-Varianten für Freiberufler, Partner, Kunden und Projekte vollständig getestet werden. Besonders
    in Verbindung mit der countSearch() Implementierung habe ich mögliche Probleme gefunden; search() und countSearch()
    sollten die gleichen Abfragen benutzen, korrekt? In diesem Fall sollte die Abfrage an einem Ort zusammengebaut werden,
    z.B. in der appendStringCriteria Methode. Prüfe diese Inkonsistenzen bitte für alle Formulare und korrigiere sie entsprechend.
- [x] ProjectPositionCommandService.java: die Methode updateEditable ist nur partiell getestet.
- [x] Make sure you created E2E Tests für the changes from above
- [x] FreelancerController.java: the methods buildEditSearchUrl and buildSearchMoreUrl are still redundant, can be unified, and not
    every path is covered by tests. I am also not sure if the fromPath() in buildEditSearchUrl is correct. This issue likely
    exisis for the Customer, Partner and Project controllers as well.
- [x] The appendLike() Method for QBE is prone to SQL injection attacks. This must be fixed for every form and every search
    operation, so please check the Freelancer, Partner, Customer and Project services and repositories for this
    misaligned pattern.
- [x] PartnerController.buildSearchMoreUrl is completely untested, this can be unified while fixing the issued above.
- [x] KundeController.buildSearchMoreUrl is completely untested, this can be unified while fixing the issued above.
- [x] The E2E / Docker Tests sometimes leave dangling volumes and do not properly clean up. This should be fixed.
- [x] FreeelancerCommandService.findByCode is completely untested.
- [x] Bearbeiten und Löschen von Projektpositionen ist nicht möglich. In der Browser-Console werden die JavaScript Fehler
  Bearbeiten: Uncaught ReferenceError: openEditPositionModal is not defined
  onclick http://localhost:8080/project/1:1
  Löschen: Uncaught ReferenceError: openDeletePositionModal is not defined
  onclick http://localhost:8080/project/1:1
  Dieser Fehle muss korrigiert und durch E2E Playwright Tests validiert werden!
- [x] Der Button "Freiberufler diesem Projekt zuordnen" auf dem Freiberufler-Formular ist zu groß. Es soll der Text
  "Projekt zuordnen" verwendet werden!
- [x] Der "Suchen" Button auf dem Freiberufler, Partner und Kunden Formular soll nur bei leeren Formularen angezeigt
  werden, also wenn ich auf "Neu" klicke bzw. der Datensatz noch keine Id hat. Dieses Verhalten soll auch über E2E
  Tests mittels Playwright getestet werden.
- [x] Die Audit-Info in der Navbar soll aus Platzgründen zweizeilig angezeigt werden, und mit etwas kleinerer Schrift.
  Die Erste Zeile enweder "Neu, noch nicht gespeichert" bzw. "Erfast ...", die zweite Zeile dann mit "Geändert ..."
  Diese Anpassung soll für die Formulare Freiberufler, Partner und Kunden umgesetzt werden.
- [x] Der Profilsuche-Chat Bereich im Profilsuche Formular hat links und rechts noch einen kleinen Rand, weshalb
  er ein wenig breiter ist als die Fluchtlinien und die Darstellung z.B. auf dem Freiberufler-Formular. Dieser
  rand bzw. abstand hat auch eine komische Farbe (gräulich), was irgendwie unpassend wirkt. Der Abstand soll
  entfernt werden.
- [x] Der Administrationsbereich soll um eine Benutzerverwaltung erweitert werden. Es soll eine Liste aller
  Benutzer angezeigt werden, und es sollen auch neue Benutzer angelegt, bestehende Benutzer bearbeitet und
  auch gelöscht werden können. Bearbeitet werden sollen alle Merkmale eines Benutzers, für die boolean Attribute
  sollen Checkboxen verwendet werden.
- [x] Fehler bei der Validierung von Datumsfeldern. Dieser Fehler betrifft vermutlich alle Orte, wo ein Datum bzw.
  ein Zeitstempel / Localdatetime eingegeben werden kann, entweder über Freitext oder über ein Datepicker.
  Im Backend wird folgende Fehlermeldung ausgegeben:  [Failed to convert property value of type 'java.lang.String' to required type 'java.time.LocalDateTime' for property 'lastContactDate'; Failed to convert from type [java.lang.String] to type [@org.springframework.data.relational.core.mapping.Column java.time.LocalDateTime] for value [2005-01-01]]]
  im Frontend habe ich allerdings keinen Validierungsfehler vor dem Speichern des Formulars bekommen. Im Fall
  von falschen Datumsangaben möchte ich im Frontend einen Validierungsfehler angezeigt bekommen, und natürlich
  muss das Backend diese Eingabe auch bearbeiten können. Als Datumsformat soll das Format dd.MM.yyyy verwendet werden,
  die Zeitzone ist die aktuelle Server-Zeitzone. Für diesen Fehler müssen Tests und idealerweise auch E2E Playwright
  Tests für alle Formulare mit Datumsfeldern implementiert werden.
- [x] Es werden Thymeleaf-Fehler beim Rendern der Projektseite im Backend-Log angezeigt: 2026-03-24 00:24:22 [http-nio-127.0.0.1-8080-exec-10] ERROR org.thymeleaf.TemplateEngine - [THYMELEAF][http-nio-127.0.0.1-8080-exec-10] Exception processing template "project/form": An error happened during template parsing (template: "class path resource [templates/project/form.html]")
  org.thymeleaf.exceptions.TemplateInputException: An error happened during template parsing (template: "class path resource [templates/project/form.html]")
  at org.thymeleaf.templateparser.markup.AbstractMarkupTemplateParser.parse(AbstractMarkupTemplateParser.java:241)
  at org.thymeleaf.templateparser.markup.AbstractMarkupTemplateParser.parseStandalone(AbstractMarkupTemplateParser.java:100)
  at org.thymeleaf.engine.TemplateManager.parseAndProcess(TemplateManager.java:666)
  at org.thymeleaf.TemplateEngine.process(TemplateEngine.java:1103)
  at org.thymeleaf.TemplateEngine.process(TemplateEngine.java:1077)
  at org.thymeleaf.spring6.view.ThymeleafView.renderFragment(ThymeleafView.java:372)
  at org.thymeleaf.spring6.view.ThymeleafView.render(ThymeleafView.java:192)
  at org.springframework.web.servlet.DispatcherServlet.render(DispatcherServlet.java:1305)
  at org.springframework.web.servlet.DispatcherServlet.processDispatchResult(DispatcherServlet.java:1042)
  at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:980)
  at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:866)
  at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1000)
  at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:892)
  at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:622)
  at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:874)
  at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:710)
  at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:128)
  at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53)
  at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)
  at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:110)
  at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)
  at org.springframework.web.filter.CompositeFilter$VirtualFilterChain.doFilter(CompositeFilter.java:108)
  at org.springframework.security.web.FilterChainProxy.lambda$doFilterInternal$3(FilterChainProxy.java:235)
  at org.springframework.security.web.ObservationFilterChainDecorator$FilterObservation$SimpleFilterObservation.lambda$wrap$1(ObservationFilterChainDecorator.java:493)
  at org.springframework.security.web.ObservationFilterChainDecorator$AroundFilterObservation$SimpleAroundFilterObservation.lambda$wrap$1(ObservationFilterChainDecorator.java:354)
  at org.springframework.security.web.ObservationFilterChainDecorator.lambda$wrapSecured$0(ObservationFilterChainDecorator.java:86)
  at org.springframework.security.web.ObservationFilterChainDecorator$VirtualFilterChain.doFilter(ObservationFilterChainDecorator.java:132)
  at org.springframework.security.web.access.intercept.AuthorizationFilter.doFilter(AuthorizationFilter.java:101)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.wrapFilter(ObservationFilterChainDecorator.java:244)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.doFilter(ObservationFilterChainDecorator.java:231)
  at org.springframework.security.web.ObservationFilterChainDecorator$VirtualFilterChain.doFilter(ObservationFilterChainDecorator.java:141)
  at org.springframework.security.web.access.ExceptionTranslationFilter.doFilter(ExceptionTranslationFilter.java:126)
  at org.springframework.security.web.access.ExceptionTranslationFilter.doFilter(ExceptionTranslationFilter.java:120)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.wrapFilter(ObservationFilterChainDecorator.java:244)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.doFilter(ObservationFilterChainDecorator.java:231)
  at org.springframework.security.web.ObservationFilterChainDecorator$VirtualFilterChain.doFilter(ObservationFilterChainDecorator.java:141)
  at org.springframework.security.web.authentication.AnonymousAuthenticationFilter.doFilter(AnonymousAuthenticationFilter.java:100)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.wrapFilter(ObservationFilterChainDecorator.java:244)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.doFilter(ObservationFilterChainDecorator.java:231)
  at org.springframework.security.web.ObservationFilterChainDecorator$VirtualFilterChain.doFilter(ObservationFilterChainDecorator.java:141)
  at org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter.doFilter(SecurityContextHolderAwareRequestFilter.java:181)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.wrapFilter(ObservationFilterChainDecorator.java:244)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.doFilter(ObservationFilterChainDecorator.java:231)
  at org.springframework.security.web.ObservationFilterChainDecorator$VirtualFilterChain.doFilter(ObservationFilterChainDecorator.java:141)
  at org.springframework.security.web.savedrequest.RequestCacheAwareFilter.doFilter(RequestCacheAwareFilter.java:63)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.wrapFilter(ObservationFilterChainDecorator.java:244)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.doFilter(ObservationFilterChainDecorator.java:231)
  at org.springframework.security.web.ObservationFilterChainDecorator$VirtualFilterChain.doFilter(ObservationFilterChainDecorator.java:141)
  at de.mirkosertic.powerstaff.auth.MustChangePasswordFilter.doFilterInternal(MustChangePasswordFilter.java:54)
  at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.wrapFilter(ObservationFilterChainDecorator.java:244)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.doFilter(ObservationFilterChainDecorator.java:231)
  at org.springframework.security.web.ObservationFilterChainDecorator$VirtualFilterChain.doFilter(ObservationFilterChainDecorator.java:141)
  at org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter.doFilter(AbstractAuthenticationProcessingFilter.java:245)
  at org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter.doFilter(AbstractAuthenticationProcessingFilter.java:239)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.wrapFilter(ObservationFilterChainDecorator.java:244)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.doFilter(ObservationFilterChainDecorator.java:231)
  at org.springframework.security.web.ObservationFilterChainDecorator$VirtualFilterChain.doFilter(ObservationFilterChainDecorator.java:141)
  at org.springframework.security.web.authentication.logout.LogoutFilter.doFilter(LogoutFilter.java:110)
  at org.springframework.security.web.authentication.logout.LogoutFilter.doFilter(LogoutFilter.java:96)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.wrapFilter(ObservationFilterChainDecorator.java:244)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.doFilter(ObservationFilterChainDecorator.java:231)
  at org.springframework.security.web.ObservationFilterChainDecorator$VirtualFilterChain.doFilter(ObservationFilterChainDecorator.java:141)
  at org.springframework.security.web.csrf.CsrfFilter.doFilterInternal(CsrfFilter.java:118)
  at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.wrapFilter(ObservationFilterChainDecorator.java:244)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.doFilter(ObservationFilterChainDecorator.java:231)
  at org.springframework.security.web.ObservationFilterChainDecorator$VirtualFilterChain.doFilter(ObservationFilterChainDecorator.java:141)
  at org.springframework.security.web.header.HeaderWriterFilter.doHeadersAfter(HeaderWriterFilter.java:90)
  at org.springframework.security.web.header.HeaderWriterFilter.doFilterInternal(HeaderWriterFilter.java:75)
  at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.wrapFilter(ObservationFilterChainDecorator.java:244)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.doFilter(ObservationFilterChainDecorator.java:231)
  at org.springframework.security.web.ObservationFilterChainDecorator$VirtualFilterChain.doFilter(ObservationFilterChainDecorator.java:141)
  at org.springframework.security.web.context.SecurityContextHolderFilter.doFilter(SecurityContextHolderFilter.java:82)
  at org.springframework.security.web.context.SecurityContextHolderFilter.doFilter(SecurityContextHolderFilter.java:69)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.wrapFilter(ObservationFilterChainDecorator.java:244)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.doFilter(ObservationFilterChainDecorator.java:231)
  at org.springframework.security.web.ObservationFilterChainDecorator$VirtualFilterChain.doFilter(ObservationFilterChainDecorator.java:141)
  at org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter.doFilterInternal(WebAsyncManagerIntegrationFilter.java:62)
  at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.wrapFilter(ObservationFilterChainDecorator.java:244)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.doFilter(ObservationFilterChainDecorator.java:231)
  at org.springframework.security.web.ObservationFilterChainDecorator$VirtualFilterChain.doFilter(ObservationFilterChainDecorator.java:141)
  at org.springframework.security.web.session.DisableEncodeUrlFilter.doFilterInternal(DisableEncodeUrlFilter.java:42)
  at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.wrapFilter(ObservationFilterChainDecorator.java:244)
  at org.springframework.security.web.ObservationFilterChainDecorator$AroundFilterObservation$SimpleAroundFilterObservation.lambda$wrap$0(ObservationFilterChainDecorator.java:337)
  at org.springframework.security.web.ObservationFilterChainDecorator$ObservationFilter.doFilter(ObservationFilterChainDecorator.java:228)
  at org.springframework.security.web.ObservationFilterChainDecorator$VirtualFilterChain.doFilter(ObservationFilterChainDecorator.java:141)
  at org.springframework.security.web.FilterChainProxy.doFilterInternal(FilterChainProxy.java:237)
  at org.springframework.security.web.FilterChainProxy.doFilter(FilterChainProxy.java:195)
  at org.springframework.web.filter.CompositeFilter$VirtualFilterChain.doFilter(CompositeFilter.java:113)
  at org.springframework.web.filter.ServletRequestPathFilter.doFilter(ServletRequestPathFilter.java:52)
  at org.springframework.web.filter.CompositeFilter$VirtualFilterChain.doFilter(CompositeFilter.java:113)
  at org.springframework.web.filter.CompositeFilter.doFilter(CompositeFilter.java:74)
  at org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration$CompositeFilterChainProxy.doFilter(WebSecurityConfiguration.java:317)
  at org.springframework.web.filter.DelegatingFilterProxy.invokeDelegate(DelegatingFilterProxy.java:355)
  at org.springframework.web.filter.DelegatingFilterProxy.doFilter(DelegatingFilterProxy.java:272)
  at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)
  at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100)
  at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)
  at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)
  at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)
  at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)
  at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)
  at org.springframework.web.filter.ServerHttpObservationFilter.doFilterInternal(ServerHttpObservationFilter.java:110)
  at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)
  at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)
  at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:199)
  at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)
  at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107)
  at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:165)
  at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:77)
  at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:492)
  at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:113)
  at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:83)
  at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:72)
  at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:341)
  at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:397)
  at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63)
  at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:903)
  at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1779)
  at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52)
  at org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:946)
  at org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:480)
  at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:57)
  at java.base/java.lang.Thread.run(Thread.java:1474)
  Caused by: org.attoparser.ParseException: Exception evaluating SpringEL expression: "entry.changedDate() != null and entry.changedUser() != null
  and !entry.creationDate().equals(entry.changedDate())" (template: "project/form" - line 285, col 21)
  at org.attoparser.MarkupParser.parseDocument(MarkupParser.java:393)
  at org.attoparser.MarkupParser.parse(MarkupParser.java:257)
  at org.thymeleaf.templateparser.markup.AbstractMarkupTemplateParser.parse(AbstractMarkupTemplateParser.java:230)
  ... 127 common frames omitted
  Caused by: org.thymeleaf.exceptions.TemplateProcessingException: Exception evaluating SpringEL expression: "entry.changedDate() != null and entry.changedUser() != null
  and !entry.creationDate().equals(entry.changedDate())" (template: "project/form" - line 285, col 21)
  at org.thymeleaf.spring6.expression.SPELVariableExpressionEvaluator.evaluate(SPELVariableExpressionEvaluator.java:292)
  at org.thymeleaf.standard.expression.VariableExpression.executeVariableExpression(VariableExpression.java:166)
  at org.thymeleaf.standard.expression.SimpleExpression.executeSimple(SimpleExpression.java:66)
  at org.thymeleaf.standard.expression.Expression.execute(Expression.java:109)
  at org.thymeleaf.standard.expression.Expression.execute(Expression.java:138)
  at org.thymeleaf.standard.expression.Expression.execute(Expression.java:125)
  at org.thymeleaf.standard.processor.StandardIfTagProcessor.isVisible(StandardIfTagProcessor.java:59)
  at org.thymeleaf.standard.processor.AbstractStandardConditionalVisibilityTagProcessor.doProcess(AbstractStandardConditionalVisibilityTagProcessor.java:61)
  at org.thymeleaf.processor.element.AbstractAttributeTagProcessor.doProcess(AbstractAttributeTagProcessor.java:74)
  at org.thymeleaf.processor.element.AbstractElementTagProcessor.process(AbstractElementTagProcessor.java:95)
  at org.thymeleaf.util.ProcessorConfigurationUtils$ElementTagProcessorWrapper.process(ProcessorConfigurationUtils.java:633)
  at org.thymeleaf.engine.ProcessorTemplateHandler.handleOpenElement(ProcessorTemplateHandler.java:1314)
  at org.thymeleaf.engine.OpenElementTag.beHandled(OpenElementTag.java:205)
  at org.thymeleaf.engine.Model.process(Model.java:282)
  at org.thymeleaf.engine.Model.process(Model.java:290)
  at org.thymeleaf.engine.IteratedGatheringModelProcessable.processIterationModel(IteratedGatheringModelProcessable.java:368)
  at org.thymeleaf.engine.IteratedGatheringModelProcessable.process(IteratedGatheringModelProcessable.java:294)
  at org.thymeleaf.engine.ProcessorTemplateHandler.handleCloseElement(ProcessorTemplateHandler.java:1640)
  at org.thymeleaf.engine.CloseElementTag.beHandled(CloseElementTag.java:139)
  at org.thymeleaf.engine.Model.process(Model.java:282)
  at org.thymeleaf.engine.ProcessorTemplateHandler.handleStandaloneElement(ProcessorTemplateHandler.java:1204)
  at org.thymeleaf.engine.StandaloneElementTag.beHandled(StandaloneElementTag.java:228)
  at org.thymeleaf.engine.Model.process(Model.java:282)
  at org.thymeleaf.engine.ProcessorTemplateHandler.handleOpenElement(ProcessorTemplateHandler.java:1587)
  at org.thymeleaf.engine.TemplateHandlerAdapterMarkupHandler.handleOpenElementEnd(TemplateHandlerAdapterMarkupHandler.java:304)
  at org.thymeleaf.templateparser.markup.InlinedOutputExpressionMarkupHandler$InlineMarkupAdapterPreProcessorHandler.handleOpenElementEnd(InlinedOutputExpressionMarkupHandler.java:278)
  at org.thymeleaf.standard.inline.OutputExpressionInlinePreProcessorHandler.handleOpenElementEnd(OutputExpressionInlinePreProcessorHandler.java:186)
  at org.thymeleaf.templateparser.markup.InlinedOutputExpressionMarkupHandler.handleOpenElementEnd(InlinedOutputExpressionMarkupHandler.java:124)
  at org.attoparser.HtmlElement.handleOpenElementEnd(HtmlElement.java:109)
  at org.attoparser.HtmlMarkupHandler.handleOpenElementEnd(HtmlMarkupHandler.java:297)
  at org.attoparser.MarkupEventProcessorHandler.handleOpenElementEnd(MarkupEventProcessorHandler.java:402)
  at org.attoparser.ParsingElementMarkupUtil.parseOpenElement(ParsingElementMarkupUtil.java:159)
  at org.attoparser.MarkupParser.parseBuffer(MarkupParser.java:710)
  at org.attoparser.MarkupParser.parseDocument(MarkupParser.java:301)
  ... 129 common frames omitted
  Caused by: org.springframework.expression.spel.SpelEvaluationException: EL1011E: Method call: Attempted to call method equals(java.time.LocalDateTime) on null context object
  at org.springframework.expression.spel.ast.MethodReference.nullTargetException(MethodReference.java:235)
  at org.springframework.expression.spel.ast.MethodReference.getValueRef(MethodReference.java:111)
  at org.springframework.expression.spel.ast.CompoundExpression.getValueRef(CompoundExpression.java:75)
  at org.springframework.expression.spel.ast.CompoundExpression.getValueInternal(CompoundExpression.java:96)
  at org.springframework.expression.spel.ast.SpelNodeImpl.getValue(SpelNodeImpl.java:198)
  at org.springframework.expression.spel.ast.OperatorNot.getValueInternal(OperatorNot.java:47)
  at org.springframework.expression.spel.ast.OperatorNot.getValueInternal(OperatorNot.java:36)
  at org.springframework.expression.spel.ast.SpelNodeImpl.getValue(SpelNodeImpl.java:198)
  at org.springframework.expression.spel.ast.OpAnd.getBooleanValue(OpAnd.java:59)
  at org.springframework.expression.spel.ast.OpAnd.getValueInternal(OpAnd.java:54)
  at org.springframework.expression.spel.ast.SpelNodeImpl.getValue(SpelNodeImpl.java:114)
  at org.springframework.expression.spel.standard.SpelExpression.getValue(SpelExpression.java:330)
  at org.thymeleaf.spring6.expression.SPELVariableExpressionEvaluator.evaluate(SPELVariableExpressionEvaluator.java:265)
  ... 162 common frames omitted
- [x] Beim erfolgreichen Zuordnen eines Freiberuflers zu einem Projekt soll ein Hinweis angezeigt werden.
- [x] Der Abstand zwischen der Freiberufler / Projektpositionsliste un den Blöcken darüber ist zu klein, bzw. existiert nicht.
  es soll der gleiche Abstand wie bei den anderen Blöcken verwendet werden für ein einheitliches Erlebnis.
- [x] Das Bearbeiten und das Löschen von Projektpositionen / Freiberuflern auf dem Projekte-Formular funktioniert noch immer nicht.
  Es treten noch immer die JavaScript Fehler von oben auf.
- [x] Der Chat-Bereich der Profilsuche ist n
- [x] Der Button "Freiberufler über Code zuordnen" hat noch immer keine Funktion, wenn ich darauf klicke.
- [x] Wenn ich einen neuen Freinerufler erfasse, steht im Audit Log Erfasst: null null. Das ist falsch, dort sollte der Name des aktuell angemeldeten Benutzer stehen und der Zeitpunkt.
  Beim Editieren wird das korrekte "Geändert" Datum und Benutzer gesetzt.
- [x] Wenn ich einen neuen Partner oder Kunden speichere, erhält er im Audit-Log sowohl ein Erstellungs als auch ein Geändert Datum, als ob er zweimal gespeichert wird. Das sollte so nicht sein.
- [x] Ich kann kein neues Projekt anlegen. Wenn ich die Projektmaske leere, muss der Speichern-Button angezeigt werden, das wird er aber im Moment nicht.