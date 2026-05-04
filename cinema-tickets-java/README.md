# Cinema Tickets — DWP Coding Exercise

**Candidate Application ID:** 17047830

## Approach

I implemented `TicketServiceImpl` by first identifying all invalid states
before writing any business logic. Validation is split into three layers:

1. **Account validation** — ID must be a positive non-null integer
2. **Request validation** — at least one ticket requested, no more than 25 total
3. **Business rule validation** — no children or infants without an adult;
   infants cannot exceed adults since each sits on a lap

**Pricing:** Adults £25 · Children £15 · Infants free  
**Seating:** Adults and Children get seats · Infants do not

Both `TicketPaymentService` and `SeatReservationService` are injected via
constructor to keep the implementation testable and decoupled.

## Running the tests

```bash
cd cinema-tickets-java
mvn test
```

## Tech

Java 21 · JUnit 5 · Mockito · Maven