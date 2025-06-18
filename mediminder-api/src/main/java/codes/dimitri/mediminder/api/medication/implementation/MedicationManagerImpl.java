package codes.dimitri.mediminder.api.medication.implementation;

import codes.dimitri.mediminder.api.medication.*;
import codes.dimitri.mediminder.api.user.CurrentUserNotFoundException;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@Service
@Validated
@Transactional(readOnly = true)
@RequiredArgsConstructor
class MedicationManagerImpl implements MedicationManager {
    private final UserManager userManager;
    private final MedicationEntityRepository medicationEntityRepository;
    private final MedicationTypeEntityRepository medicationTypeEntityRepository;
    private final AdministrationTypeEntityRepository administrationTypeEntityRepository;
    private final DoseTypeEntityRepository doseTypeEntityRepository;
    private final MedicationEntityMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public MedicationDTO createForCurrentUser(@Valid @NotNull CreateMedicationRequestDTO request) {
        UserDTO currentUser = findCurrentUser();
        MedicationTypeEntity medicationType = findMedicationType(request.medicationTypeId());
        DoseTypeEntity doseType = findDoseType(request.medicationTypeId(), request.doseTypeId());
        AdministrationTypeEntity administrationType = findAdministrationType(request.medicationTypeId(), request.administrationTypeId());
        MedicationEntity entity = new MedicationEntity(
            currentUser.id(),
            request.name(),
            medicationType,
            administrationType,
            doseType,
            request.dosesPerPackage(),
            request.color()
        );
        return mapper.toDTO(medicationEntityRepository.save(entity));
    }

    @Override
    public Page<MedicationDTO> findAllForCurrentUser(String search, @NotNull Pageable pageable) {
        UserDTO currentUser = findCurrentUser();
        Page<MedicationEntity> page = findAllEntities(search, pageable, currentUser);
        return page.map(mapper::toDTO);
    }

    private Page<MedicationEntity> findAllEntities(String search, Pageable pageable, UserDTO currentUser) {
        if (search == null || search.isBlank()) {
            return medicationEntityRepository.findAllByUserId(currentUser.id(), pageable);
        } else {
            return medicationEntityRepository.findAllByUserIdAndNameContainingIgnoreCase(currentUser.id(), search, pageable);
        }
    }

    @Override
    public MedicationDTO findByIdForCurrentUser(UUID id) {
        UserDTO currentUser = findCurrentUser();
        return findByIdAndUserId(id, currentUser.id());
    }

    @Override
    public MedicationDTO findByIdAndUserId(@NotNull UUID id, @NotNull String userId) {
        return medicationEntityRepository
            .findByIdAndUserId(id, userId)
            .map(mapper::toDTO)
            .orElseThrow(() -> new MedicationNotFoundException(id));
    }

    @Override
    @Transactional
    public void deleteByIdForCurrentUser(@NotNull UUID id) {
        UserDTO currentUser = findCurrentUser();
        MedicationEntity entity = findEntity(id, currentUser);
        medicationEntityRepository.delete(entity);
        eventPublisher.publishEvent(new MedicationDeletedEvent(id));
    }

    @Override
    @Transactional
    public MedicationDTO updateForCurrentUser(@NotNull UUID id, @NotNull @Valid UpdateMedicationRequestDTO request) {
        UserDTO currentUser = findCurrentUser();
        MedicationEntity entity = findEntity(id, currentUser);
        DoseTypeEntity doseType = findDoseType(entity.getMedicationType().getId(), request.doseTypeId());
        AdministrationTypeEntity administrationType = findAdministrationType(entity.getMedicationType().getId(), request.administrationTypeId());
        entity.setName(request.name());
        entity.setAdministrationType(administrationType);
        entity.setDoseType(doseType);
        entity.setDosesPerPackage(request.dosesPerPackage());
        entity.setColor(request.color());
        return mapper.toDTO(entity);
    }

    @Override
    public void deleteAllByUserId(String userId) {
        List<MedicationEntity> entities = medicationEntityRepository.findAllByUserId(userId);
        if (entities.isEmpty()) return;
        medicationEntityRepository.deleteAll(entities);
        entities.forEach(entity -> eventPublisher.publishEvent(new MedicationDeletedEvent(entity.getId())));
    }

    private MedicationEntity findEntity(UUID id, UserDTO currentUser) {
        return medicationEntityRepository
            .findByIdAndUserId(id, currentUser.id())
            .orElseThrow(() -> new MedicationNotFoundException(id));
    }

    private UserDTO findCurrentUser() {
        try {
            return userManager.findCurrentUser();
        } catch (CurrentUserNotFoundException ex) {
            throw new InvalidMedicationException("User is not authenticated", ex);
        }
    }

    private MedicationTypeEntity findMedicationType(String medicationTypeId) {
        return medicationTypeEntityRepository
            .findById(medicationTypeId)
            .orElseThrow(() -> new MedicationTypeNotFoundException(medicationTypeId));
    }

    private AdministrationTypeEntity findAdministrationType(String medicationTypeId, String administrationTypeId) {
        return administrationTypeEntityRepository
            .findByIdAndMedicationTypeId(administrationTypeId, medicationTypeId)
            .orElseThrow(() -> new AdministrationTypeNotFoundException(administrationTypeId));
    }

    private DoseTypeEntity findDoseType(String medicationTypeId, String doseTypeId) {
        return doseTypeEntityRepository
            .findByIdAndMedicationTypeId(doseTypeId, medicationTypeId)
            .orElseThrow(() -> new DoseTypeNotFoundException(doseTypeId));
    }
}
