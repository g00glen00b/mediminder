package codes.dimitri.mediminder.api.planner.implementation;

import codes.dimitri.mediminder.api.common.SecurityConfiguration;
import codes.dimitri.mediminder.api.planner.MedicationPlannerDTO;
import codes.dimitri.mediminder.api.planner.PlannerManager;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PlannerController.class)
@Import(SecurityConfiguration.class)
class PlannerControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PlannerManager manager;

    @Test
    @WithMockUser
    void findAll_ok() throws Exception {
        // Given
        var date = LocalDate.of(2024, 6, 30);
        var dto = Instancio.create(MedicationPlannerDTO.class);
        // When
        when(manager.findAll(any(), any())).thenReturn(new PageImpl<>(List.of(dto)));
        // Then
        mockMvc
            .perform(get("/api/planner/{targetDate}", date)
                .queryParam("page", "0")
                .queryParam("size", "10")
                .queryParam("sort", "name,asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].medication.id").value(dto.medication().id().toString()));
        verify(manager).findAll(date, PageRequest.of(0, 10, Sort.Direction.ASC, "name"));
    }

    @Test
    void findAll_unauthorized() throws Exception {
        // Given
        var date = LocalDate.of(2024, 6, 30);
        // Then
        mockMvc
            .perform(get("/api/planner/{targetDate}", date)
                .queryParam("page", "0")
                .queryParam("size", "10")
                .queryParam("sort", "name,asc"))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(manager);
    }
}