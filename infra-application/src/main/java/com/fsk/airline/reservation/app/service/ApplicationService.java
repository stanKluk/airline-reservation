package com.fsk.airline.reservation.app.service;

import com.fsk.airline.reservation.app.controller.ReserveTicketDto;
import com.fsk.airline.reservation.app.controller.ReservedTicketDto;
import com.fsk.airline.reservation.app.controller.ReservedTicketNumberDto;
import com.fsk.airline.reservation.domain.api.AddGuestSeatUseCase;
import com.fsk.airline.reservation.domain.api.GetReservedTicketPriceUseCase;
import com.fsk.airline.reservation.domain.api.ReserveTicketUseCase;
import com.fsk.airline.reservation.domain.api.SearchReservedTicketUseCase;
import com.fsk.airline.reservation.domain.command.ReserveTicketRequest;
import com.fsk.airline.reservation.domain.command.ReserveTicketRequestBuilder;
import com.fsk.airline.reservation.domain.model.ReservedTicket;
import com.fsk.airline.reservation.domain.model.TicketNumber;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Optional;

@Service
public class ApplicationService {

	private final ReserveTicketUseCase reserveTicketUseCase;
	private final SearchReservedTicketUseCase searchReservedTicketUseCase;
	private final GetReservedTicketPriceUseCase getReservedTicketPriceUseCase;
	private final AddGuestSeatUseCase addGuestSeatUseCase;

	public ApplicationService(ReserveTicketUseCase reserveTicketUseCase,
	                          SearchReservedTicketUseCase searchReservedTicketUseCase,
	                          GetReservedTicketPriceUseCase getReservedTicketPriceUseCase,
	                          AddGuestSeatUseCase addGuestSeatUseCase) {
		this.reserveTicketUseCase = reserveTicketUseCase;
		this.searchReservedTicketUseCase = searchReservedTicketUseCase;
		this.getReservedTicketPriceUseCase = getReservedTicketPriceUseCase;
		this.addGuestSeatUseCase = addGuestSeatUseCase;
	}

	@Transactional
	public ReservedTicketNumberDto reserveTicket(String customerLogin, ReserveTicketDto reserveTicketDto) {
		ReserveTicketRequest reserveTicketRequest = new ReserveTicketRequestBuilder()
				.customerLogin(customerLogin)
				.cityFrom(reserveTicketDto.getCityFrom())
				.cityTo(reserveTicketDto.getCityTo())
				.departureDate(reserveTicketDto.getDepartureDate())
				.build();
		ReservedTicket reservedTicket = reserveTicketUseCase.reserveTicket(reserveTicketRequest);

		ReservedTicketNumberDto reservedTicketDto = new ReservedTicketNumberDto();
		reservedTicketDto.setTicketNumber(reservedTicket.getNumber().getValue());
		return reservedTicketDto;
	}

	@Transactional
	public ReservedTicketDto getReservedTicket(String customerLogin, String ticketNumber) {
		Optional<ReservedTicket> optionalReservedTicket = searchReservedTicketUseCase.findReservedTicket(customerLogin, TicketNumber.of(ticketNumber));

		if (optionalReservedTicket.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find ticket " + ticketNumber);
		}

		BigDecimal ticketPrice = getReservedTicketPriceUseCase.getReservedTicketPrice(customerLogin, TicketNumber.of(ticketNumber));
		return toReservedTicketDto(ticketPrice, optionalReservedTicket.get());
	}

	private ReservedTicketDto toReservedTicketDto(BigDecimal ticketPrice, ReservedTicket reservedTicket) {
		ReservedTicketDto reservedTicketDto = new ReservedTicketDto();
		reservedTicketDto.setTicketNumber(reservedTicket.getNumber().getValue());
		reservedTicketDto.setCustomerLogin(reservedTicket.getCustomerLogin());
		reservedTicketDto.setFrom(reservedTicket.getFrom().getValue());
		reservedTicketDto.setTo(reservedTicket.getTo().getValue());
		reservedTicketDto.setDepartureDate(reservedTicket.getDepartureDate());
		reservedTicketDto.setDistanceInKm(reservedTicket.getDistanceInKm());
		reservedTicketDto.setPrice(ticketPrice.doubleValue());
		return reservedTicketDto;
	}

	@Transactional
	public void addGuestToReservedTicket(String customerLogin, String ticketNumber) {
		addGuestSeatUseCase.addGuestToReservation(customerLogin, TicketNumber.of(ticketNumber));
	}

}
