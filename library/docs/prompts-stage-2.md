# Prompt log: Stage 2

| Number | Model | Exact prompt text | Files changed |
|--------|--|----|---|
| 1      | claude-sonnet-5-5 | can you apply the changes described in change 7 to  memberservice.java and create the prompts-stage-2.md file with the same format as prompts-stage-1.md, starting with this prompt? | `service/MemberService.java`, `docs/prompts-stage-2.md` (new) |
| 2      | claude-sonnet-5-5 | apply every change described in change 8 to LoanService.java | `service/LoanService.java`, `docs/prompts-stage-2.md` |
| 3      | claude-sonnet-5-5 | apply the changes described in change 9 to ReservationService.java | `service/ReservationService.java`, `docs/prompts-stage-2.md` |
| 4      | claude-sonnet-5-5 | Change the wiring in app.java according to what's described in change 11, then configure the JSON mapper to write LocalDate values as strings and add exception handlers as written in the document. | `App.java`, `docs/prompts-stage-2.md` |
| 5      | claude-sonnet-5-5 | Delete the BookDao field and the constructor parameter in BookController. The GET /books route calls the service as written in change 12 | `controller/BookController.java`, `docs/prompts-stage-2.md` |
| 6      | claude-sonnet-5-5 | Delete the MemberService field and the constructor parameter in LoanController. The POST /loans route calls the loan service written in change 12 | `controller/LoanController.java`, `docs/prompts-stage-2.md` |
| 7      | claude-sonnet-5-5 | Delete the two Objects.requireNonNull calls and the java.util.Objects import in ReservationController | `controller/ReservationController.java`, `docs/prompts-stage-2.md` |
