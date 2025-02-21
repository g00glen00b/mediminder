package codes.dimitri.mediminder.api.medication.implementation;

import codes.dimitri.mediminder.api.medication.MedicationTypeDTO;
import org.instancio.Instancio;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicationTypeManagerImplTest {
    @InjectMocks
    private MedicationTypeManagerImpl manager;
    @Mock
    private MedicationTypeEntityRepository repository;
    @Spy
    private MedicationTypeEntityMapper mapper = Mappers.getMapper(MedicationTypeEntityMapper.class);

    @Nested
    class findAll {
        @Test
        void returnsDTO() {
            // Given
            var entity = Instancio.create(MedicationTypeEntity.class);
            PageRequest pageRequest = PageRequest.of(0, 20);
            // When
            when(repository.findAll(ArgumentMatchers.<Pageable>any())).thenReturn(new PageImpl<>(List.of(entity)));
            // Then
            Page<MedicationTypeDTO> results = manager.findAll(pageRequest);
            assertThat(results).containsOnly(new MedicationTypeDTO(
               entity.getId(),
               entity.getName()
            ));
            verify(repository).findAll(pageRequest);
        }
    }
}