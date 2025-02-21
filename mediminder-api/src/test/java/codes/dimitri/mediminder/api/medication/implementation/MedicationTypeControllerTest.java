package codes.dimitri.mediminder.api.medication.implementation;

import codes.dimitri.mediminder.api.common.SecurityConfiguration;
import codes.dimitri.mediminder.api.medication.*;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MedicationTypeController.class)
@Import(SecurityConfiguration.class)
class MedicationTypeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private MedicationTypeManager medicationTypeManager;
    @MockBean
    private AdministrationTypeManager administrationTypeManager;
    @MockBean
    private DoseTypeManager doseTypeManager;

    @Test
    @WithMockUser
    void findAll() throws Exception {
        // Given
        var medicationType = Instancio.create(MedicationTypeDTO.class);
        // When
        when(medicationTypeManager.findAll(any())).thenReturn(new PageImpl<>(List.of(medicationType)));
        // Then
        mockMvc
            .perform(get("/api/medication-type")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .queryParam("sort", "name,asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(medicationType.id()));
        verify(medicationTypeManager).findAll(PageRequest.of(0, 10, Sort.Direction.ASC, "name"));
    }

    @Test
    void findAll_unauthorized() throws Exception {
        mockMvc
            .perform(get("/api/medication-type")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .queryParam("sort", "name,asc"))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(medicationTypeManager);
    }

    @Test
    @WithMockUser
    void findAllDoseTypes() throws Exception {
        // Given
        var doseType = Instancio.create(DoseTypeDTO.class);
        var id = "TABLET";
        // When
        when(doseTypeManager.findAllByMedicationTypeId(anyString(), any())).thenReturn(new PageImpl<>(List.of(doseType)));
        // Then
        mockMvc
            .perform(get("/api/medication-type/{id}/dose-type", id)
                .queryParam("page", "0")
                .queryParam("size", "10")
                .queryParam("sort", "name,asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(doseType.id()));
        verify(doseTypeManager).findAllByMedicationTypeId(id, PageRequest.of(0, 10, Sort.Direction.ASC, "name"));
    }

    @Test
    void findAllDoseTypes_unauthorized() throws Exception {
        // Given
        var id = "TABLET";
        // Then
        mockMvc
            .perform(get("/api/medication-type/{id}/dose-type", id)
                .queryParam("page", "0")
                .queryParam("size", "10")
                .queryParam("sort", "name,asc"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void findAllAdministrationTypes() throws Exception {
        // Given
        var administrationTypeDTO = Instancio.create(AdministrationTypeDTO.class);
        var id = "TABLET";
        // When
        when(administrationTypeManager.findAllByMedicationTypeId(anyString(), any())).thenReturn(new PageImpl<>(List.of(administrationTypeDTO)));
        // Then
        mockMvc
            .perform(get("/api/medication-type/{id}/administration-type", id)
                .queryParam("page", "0")
                .queryParam("size", "10")
                .queryParam("sort", "name,asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(administrationTypeDTO.id()));
        verify(administrationTypeManager).findAllByMedicationTypeId(id, PageRequest.of(0, 10, Sort.Direction.ASC, "name"));
    }

    @Test
    void findAllAdministrationTypes_unauthorized() throws Exception {
        // Given
        var id = "TABLET";
        // Then
        mockMvc
            .perform(get("/api/medication-type/{id}/administration-type", id)
                .queryParam("page", "0")
                .queryParam("size", "10")
                .queryParam("sort", "name,asc"))
            .andExpect(status().isUnauthorized());
    }
}