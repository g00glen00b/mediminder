package codes.dimitri.mediminder.api.medication.implementation;

import codes.dimitri.mediminder.api.common.SecurityConfiguration;
import codes.dimitri.mediminder.api.medication.*;
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

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MedicationTypeController.class)
@Import(SecurityConfiguration.class)
class MedicationTypeControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private MedicationTypeManager typeManager;
    @MockitoBean
    private DoseTypeManager doseTypeManager;
    @MockitoBean
    private AdministrationTypeManager administrationTypeManager;

    @Nested
    class findAll {
        @Test
        void returnsResults() throws Exception {
            var type = new MedicationTypeDTO("TABLET", "Tablet");
            var pageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "id");
            var page = new PageImpl<>(List.of(type));
            when(typeManager.findAll(pageRequest)).thenReturn(page);
            mvc
                .perform(get("/api/medication-type")
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "id,asc")
                    .with(user("me@example.org")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(type.id()))
                .andExpect(jsonPath("$.content[0].name").value(type.name()));
        }
    }

    @Nested
    class findAllDoseTypes {
        @Test
        void returnsResults() throws Exception {
            var type = new DoseTypeDTO("TABLET", "Tablet");
            var pageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "id");
            var page = new PageImpl<>(List.of(type));
            when(doseTypeManager.findAllByMedicationTypeId("TABLET", pageRequest)).thenReturn(page);
            mvc
                .perform(get("/api/medication-type/{id}/dose-type", "TABLET")
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "id,asc")
                    .with(user("me@example.org")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(type.id()))
                .andExpect(jsonPath("$.content[0].name").value(type.name()));
        }
    }

    @Nested
    class findAllAdministrationTypes {
        @Test
        void returnsResults() throws Exception {
            var type = new AdministrationTypeDTO("ORAL", "Oral");
            var pageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "id");
            var page = new PageImpl<>(List.of(type));
            when(administrationTypeManager.findAllByMedicationTypeId("TABLET", pageRequest)).thenReturn(page);
            mvc
                .perform(get("/api/medication-type/{id}/administration-type", "TABLET")
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "id,asc")
                    .with(user("me@example.org")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(type.id()))
                .andExpect(jsonPath("$.content[0].name").value(type.name()));
        }
    }
}