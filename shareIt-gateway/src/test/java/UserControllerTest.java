import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItGatewayApp;
import ru.practicum.shareit.client.UserClient;
import ru.practicum.shareit.controller.UserController;
import ru.practicum.shareit.dtos.UserDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ContextConfiguration(classes = ShareItGatewayApp.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    @Test
    void createShouldReturnUser() throws Exception {
        UserDto requestDto = new UserDto(null, "Tester", "tester@example.com");
        UserDto responseDto = new UserDto(1L, "Tester", "tester@example.com");

        when(userClient.createUser(any(UserDto.class))).thenReturn(org.springframework.http.ResponseEntity.ok(responseDto));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getByIdShouldReturnUser() throws Exception {
        Long userId = 1L;
        UserDto responseDto = new UserDto(userId, "Tester", "tester@example.com");

        when(userClient.getUserById(eq(userId))).thenReturn(org.springframework.http.ResponseEntity.ok(responseDto));

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));
    }
}