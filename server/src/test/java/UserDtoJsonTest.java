import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.config.ObjectMapperConfig;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
@ContextConfiguration(classes = ObjectMapperConfig.class)
class UserDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializeDeserializeShouldWorkCorrectly() throws Exception {
        UserDto original = new UserDto(1L, "Tester", "tester@example.com");

        String json = objectMapper.writeValueAsString(original);
        UserDto deserialized = objectMapper.readValue(json, UserDto.class);

        assertEquals(original.getId(), deserialized.getId());
        assertEquals(original.getName(), deserialized.getName());
        assertEquals(original.getEmail(), deserialized.getEmail());
    }
}