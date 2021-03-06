package com.fsk.airline.reservation.domain.spi.stub;

import com.fsk.airline.reservation.domain.model.ReservedTicket;
import com.fsk.airline.reservation.domain.model.TicketNumber;
import com.fsk.airline.reservation.domain.spi.ReservedTickets;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ReservedTicketsInMemory implements ReservedTickets {

	private final Map<TicketNumber, ReservedTicket> reservedTickets = new HashMap<>();

	@Override
	public Optional<ReservedTicket> findOne(String customerLogin, TicketNumber ticketNumber) {
		ReservedTicket reservedTicket = reservedTickets.get(ticketNumber);
		if (ticketExistsAndBelongsToCustomer(customerLogin, reservedTicket)) {
			return Optional.of(reservedTicket);
		}
		return Optional.empty();
	}

	@Override
	public boolean exists(String customerLogin, TicketNumber ticketNumber) {
		return findOne(customerLogin, ticketNumber).isPresent();
	}

	private boolean ticketExistsAndBelongsToCustomer(String customerLogin, ReservedTicket reservedTicket) {
		return reservedTicket != null && reservedTicket.getCustomerLogin().equals(customerLogin);
	}

	@Override
	public void save(ReservedTicket reservedTicket) {
		reservedTickets.put(reservedTicket.getNumber(), reservedTicket);
	}
}
