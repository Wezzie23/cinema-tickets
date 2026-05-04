package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock private TicketPaymentService ticketPaymentService;
    @Mock private SeatReservationService seatReservationService;

    private TicketServiceImpl ticketService;

    @BeforeEach
    void setUp() {
        ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);
    }

    //  Valid purchases

    @Test
    @DisplayName("Adult only: correct payment and seat count")
    void adultOnly_correctPaymentAndSeats() {
        ticketService.purchaseTickets(1L, new TicketTypeRequest(Type.ADULT, 3));
        verify(ticketPaymentService).makePayment(1L, 75);
        verify(seatReservationService).reserveSeat(1L, 3);
    }

    @Test
    @DisplayName("Adult + Child: correct totals")
    void adultAndChild_correctTotals() {
        ticketService.purchaseTickets(1L,
                new TicketTypeRequest(Type.ADULT, 2),
                new TicketTypeRequest(Type.CHILD, 2));
        verify(ticketPaymentService).makePayment(1L, 80);
        verify(seatReservationService).reserveSeat(1L, 4);
    }

    @Test
    @DisplayName("Infant is free and gets no seat")
    void infant_freeAndNoSeat() {
        ticketService.purchaseTickets(1L,
                new TicketTypeRequest(Type.ADULT, 2),
                new TicketTypeRequest(Type.CHILD, 1),
                new TicketTypeRequest(Type.INFANT, 1));
        verify(ticketPaymentService).makePayment(1L, 65);
        verify(seatReservationService).reserveSeat(1L, 3);
    }

    @Test
    @DisplayName("Exactly 25 tickets is allowed")
    void exactly25Tickets_isValid() {
        assertDoesNotThrow(() ->
                ticketService.purchaseTickets(1L, new TicketTypeRequest(Type.ADULT, 25)));
    }

    //  Invalid account

    @Test
    @DisplayName("Account ID of zero is rejected")
    void accountIdZero_rejected() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(0L, new TicketTypeRequest(Type.ADULT, 1)));
    }

    @Test
    @DisplayName("Negative account ID is rejected")
    void negativeAccountId_rejected() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(-1L, new TicketTypeRequest(Type.ADULT, 1)));
    }

    @Test
    @DisplayName("Null account ID is rejected")
    void nullAccountId_rejected() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(null, new TicketTypeRequest(Type.ADULT, 1)));
    }

    //  Rules for invalid tickets

    @Test
    @DisplayName("Over 25 tickets is rejected")
    void over25Tickets_rejected() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L, new TicketTypeRequest(Type.ADULT, 26)));
    }

    @Test
    @DisplayName("Child without Adult is rejected")
    void childWithoutAdult_rejected() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L, new TicketTypeRequest(Type.CHILD, 1)));
    }

    @Test
    @DisplayName("Infant without Adult is rejected")
    void infantWithoutAdult_rejected() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L, new TicketTypeRequest(Type.INFANT, 1)));
    }

    @Test
    @DisplayName("More infants than adults is rejected")
    void moreInfantsThanAdults_rejected() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L,
                        new TicketTypeRequest(Type.ADULT, 1),
                        new TicketTypeRequest(Type.INFANT, 2)));
    }

    @Test
    @DisplayName("No payment or reservation made on invalid purchase")
    void invalidPurchase_noExternalCallsMade() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L, new TicketTypeRequest(Type.CHILD, 1)));
        verifyNoInteractions(ticketPaymentService, seatReservationService);
    }
}