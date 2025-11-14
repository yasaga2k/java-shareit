import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItGatewayApp;
import ru.practicum.shareit.client.ItemClient;
import ru.practicum.shareit.controller.ItemController;
import ru.practicum.shareit.dtos.ItemDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@ContextConfiguration(classes = ShareItGatewayApp.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    void createShouldReturnItem() throws Exception {
        Long userId = 1L;
        ItemDto requestDto = new ItemDto();
        requestDto.setId(null);
        requestDto.setName("Drill");
        requestDto.setDescription("Powerful drill");
        requestDto.setAvailable(true);
        requestDto.setRequestId(null);

        ItemDto responseDto = new ItemDto();
        responseDto.setId(1L);
        responseDto.setName("Drill");
        responseDto.setDescription("Powerful drill");
        responseDto.setAvailable(true);
        responseDto.setRequestId(null);

        when(itemClient.create(eq(userId), any(ItemDto.class))).thenReturn(org.springframework.http.ResponseEntity.ok(responseDto));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getByIdShouldReturnItem() throws Exception {
        Long userId = 1L;
        Long itemId = 1L;
        ItemDto responseDto = new ItemDto();
        responseDto.setId(itemId);
        responseDto.setName("Drill");
        responseDto.setDescription("Powerful drill");
        responseDto.setAvailable(true);
        responseDto.setRequestId(null);

        when(itemClient.getById(eq(userId), eq(itemId))).thenReturn(org.springframework.http.ResponseEntity.ok(responseDto));

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId));
    }
}