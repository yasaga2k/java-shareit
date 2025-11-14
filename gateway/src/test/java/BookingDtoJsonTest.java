import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.ShareItGatewayApp;
import ru.practicum.shareit.dtos.BookingDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
@ContextConfiguration(classes = ShareItGatewayApp.class)
class BookingDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializeDeserializeShouldWorkCorrectly() throws Exception {
        BookingDto original = new BookingDto(1L, 1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));

        String json = objectMapper.writeValueAsString(original);
        BookingDto deserialized = objectMapper.readValue(json, BookingDto.class);

        assertEquals(original.getId(), deserialized.getId());
        assertEquals(original.getItemId(), deserialized.getItemId());
        assertEquals(original.getStart(), deserialized.getStart());
        assertEquals(original.getEnd(), deserialized.getEnd());
    }
}