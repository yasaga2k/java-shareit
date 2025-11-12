import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItGatewayApp;
import ru.practicum.shareit.controller.BookingController;
import ru.practicum.shareit.dtos.BookingDto;
import ru.practicum.shareit.client.BookingClient;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@ContextConfiguration(classes = ShareItGatewayApp.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void createShouldReturnBooking() throws Exception {
        Long userId = 1L;
        BookingDto requestDto = new BookingDto(null, 1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));
        BookingDto responseDto = new BookingDto(1L, 1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));

        when(bookingClient.create(eq(userId), any(BookingDto.class))).thenReturn(org.springframework.http.ResponseEntity.ok(responseDto));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getByIdShouldReturnBooking() throws Exception {
        Long userId = 1L;
        Long bookingId = 1L;
        BookingDto responseDto = new BookingDto(bookingId, 1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));

        when(bookingClient.getById(eq(userId), eq(bookingId))).thenReturn(org.springframework.http.ResponseEntity.ok(responseDto));

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId));
    }

    @Test
    void approveShouldReturnBooking() throws Exception {
        Long userId = 1L;
        Long bookingId = 1L;
        Boolean approved = true;
        BookingDto responseDto = new BookingDto(bookingId, 1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));

        when(bookingClient.approve(eq(userId), eq(bookingId), eq(approved))).thenReturn(org.springframework.http.ResponseEntity.ok(responseDto));

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .param("approved", approved.toString())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId));
    }
}