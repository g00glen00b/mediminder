package codes.dimitri.mediminder.api.planner.implementation;

import codes.dimitri.mediminder.api.common.SecurityConfiguration;
import codes.dimitri.mediminder.api.medication.*;
import codes.dimitri.mediminder.api.planner.MedicationPlannerDTO;
import codes.dimitri.mediminder.api.planner.PlannerManager;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PlannerController.class)
@Import(SecurityConfiguration.class)
class PlannerControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private PlannerManager manager;

    @Nested
    class findAll {
        @Test
        void returnsResults() throws Exception {
            var result = new MedicationPlannerDTO(
                new MedicationDTO(
                    UUID.randomUUID(),
                    "Dafalgan",
                    new MedicationTypeDTO("TABLET", "Tablet"),
                    new AdministrationTypeDTO("ORAL", "Oral"),
                    new DoseTypeDTO("TABLET", "tablet(s)"),
                    new BigDecimal("100"),
                    Color.RED
                ),
                new BigDecimal("20"),
                new BigDecimal("10")
            );
            var pageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "id");
            var page = new PageImpl<>(List.of(result));
            var date = LocalDate.of(2025, 6, 10);
            when(manager.findAll(date, pageRequest)).thenReturn(page);
            mvc
                .perform(get("/api/planner/{targetDate}", date)
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "id,asc")
                    .with(user("me@example.org")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].medication.id").value(result.medication().id().toString()))
                .andExpect(jsonPath("$.content[0].medication.name").value(result.medication().name()))
                .andExpect(jsonPath("$.content[0].medication.doseType.name").value(result.medication().doseType().name()))
                .andExpect(jsonPath("$.content[0].medication.administrationType.name").value(result.medication().administrationType().name()))
                .andExpect(jsonPath("$.content[0].medication.dosesPerPackage").value(result.medication().dosesPerPackage()))
                .andExpect(jsonPath("$.content[0].availableDoses").value(result.availableDoses()))
                .andExpect(jsonPath("$.content[0].requiredDoses").value(result.requiredDoses()));

        }
    }
}