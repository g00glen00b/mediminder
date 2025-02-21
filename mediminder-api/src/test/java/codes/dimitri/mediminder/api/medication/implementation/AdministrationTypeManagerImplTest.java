package codes.dimitri.mediminder.api.medication.implementation;

import codes.dimitri.mediminder.api.medication.AdministrationTypeDTO;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdministrationTypeManagerImplTest {
    @InjectMocks
    private AdministrationTypeManagerImpl manager;
    @Mock
    private AdministrationTypeEntityRepository repository;
    @Spy
    private AdministrationTypeEntityMapper mapper = Mappers.getMapper(AdministrationTypeEntityMapper.class);

    @Test
    void findAllByMedicationId() {
        // Given
        var medicationTypeId = Instancio.create(String.class);
        var entity = Instancio.create(AdministrationTypeEntity.class);
        var pageRequest = PageRequest.of(0, 10);
        // When
        when(repository.findAllByMedicationTypeId(any(), any())).thenReturn(new PageImpl<>(List.of(entity)));
        // Then
        Page<AdministrationTypeDTO> result = manager.findAllByMedicationTypeId(medicationTypeId, pageRequest);
        assertThat(result.getContent()).containsOnly(new AdministrationTypeDTO(
            entity.getId(),
            entity.getName()
        ));
        verify(repository).findAllByMedicationTypeId(medicationTypeId, pageRequest);
    }
}