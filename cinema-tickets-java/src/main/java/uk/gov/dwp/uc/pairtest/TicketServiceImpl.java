package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {

    private static final int MAX_TICKETS = 25;
    private static final int ADULT_PRICE  = 25;
    private static final int CHILD_PRICE  = 15;

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl(TicketPaymentService ticketPaymentService,
                             SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests)
            throws InvalidPurchaseException {

        validateAccountId(accountId);
        validateTicketRequests(ticketTypeRequests);

        int adults   = countByType(ticketTypeRequests, Type.ADULT);
        int children = countByType(ticketTypeRequests, Type.CHILD);
        int infants  = countByType(ticketTypeRequests, Type.INFANT);

        validateBusinessRules(adults, children, infants);

        int totalAmount = (adults * ADULT_PRICE) + (children * CHILD_PRICE);
        int totalSeats  = adults + children;

        ticketPaymentService.makePayment(accountId, totalAmount);
        seatReservationService.reserveSeat(accountId, totalSeats);
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private void validateAccountId(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException();
        }
    }

    private void validateTicketRequests(TicketTypeRequest... requests) {
        if (requests == null || requests.length == 0) {
            throw new InvalidPurchaseException();
        }
        int total = 0;
        for (TicketTypeRequest request : requests) {
            if (request.getNoOfTickets() < 0) {
                throw new InvalidPurchaseException();
            }
            total += request.getNoOfTickets();
        }
        if (total == 0) {
            throw new InvalidPurchaseException();
        }
        if (total > MAX_TICKETS) {
            throw new InvalidPurchaseException();
        }
    }

    private void validateBusinessRules(int adults, int children, int infants) {
        if (adults == 0 && (children > 0 || infants > 0)) {
            throw new InvalidPurchaseException();
        }
        if (infants > adults) {
            throw new InvalidPurchaseException();
        }
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private int countByType(TicketTypeRequest[] requests, Type type) {
        int count = 0;
        for (TicketTypeRequest request : requests) {
            if (request.getTicketType() == type) {
                count += request.getNoOfTickets();
            }
        }
        return count;
    }
}