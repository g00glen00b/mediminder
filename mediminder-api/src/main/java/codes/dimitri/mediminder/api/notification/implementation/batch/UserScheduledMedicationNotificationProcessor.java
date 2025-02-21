package codes.dimitri.mediminder.api.notification.implementation.batch;

import codes.dimitri.mediminder.api.cabinet.CabinetEntryManager;
import codes.dimitri.mediminder.api.medication.MedicationDTO;
import codes.dimitri.mediminder.api.medication.MedicationManager;
import codes.dimitri.mediminder.api.notification.NotificationType;
import codes.dimitri.mediminder.api.notification.implementation.NotificationEntity;
import codes.dimitri.mediminder.api.notification.implementation.NotificationProperties;
import codes.dimitri.mediminder.api.schedule.ScheduleManager;
import codes.dimitri.mediminder.api.schedule.SchedulePeriodDTO;
import codes.dimitri.mediminder.api.schedule.UserScheduledMedicationDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserScheduledMedicationNotificationProcessor implements ItemProcessor<UserScheduledMedicationDTO, NotificationEntity> {
    private final Clock clock;
    private final ScheduleManager scheduleManager;
    private final MedicationManager medicationManager;
    private final CabinetEntryManager cabinetEntryManager;
    private final NotificationProperties properties;
    private final UserManager userManager;

    @Override
    public NotificationEntity process(UserScheduledMedicationDTO item) {
        BigDecimal remainingDoses = cabinetEntryManager.calculateTotalRemainingDosesByMedicationId(item.medicationId());
        if (isOutOfDoses(remainingDoses)) return createOutOfMedicationNotification(item);
        BigDecimal requiredDoses = calculateRequiredDosesDuringWarnPeriod(item);
        BigDecimal remainingDosesAfterWarnPeriod = remainingDoses.subtract(requiredDoses);
        if (isOutOfDoses(remainingDosesAfterWarnPeriod)) return createAlmostOutOfMedicationNotification(item);
        return null;
    }

    private BigDecimal calculateRequiredDosesDuringWarnPeriod(UserScheduledMedicationDTO item) {
        LocalDateTime today = userManager.calculateTodayForUser(item.userId());
        SchedulePeriodDTO period = new SchedulePeriodDTO(today.toLocalDate(), today.toLocalDate().plus(properties.dose().warnPeriod()));
        return scheduleManager.calculateRequiredDoses(item.medicationId(), period);
    }

    private static boolean isOutOfDoses(BigDecimal remainingDoses) {
        return BigDecimal.ZERO.compareTo(remainingDoses) >= 0;
    }

    private NotificationEntity createOutOfMedicationNotification(UserScheduledMedicationDTO item) {
        Optional<MedicationDTO> medication = medicationManager.findByIdAndUserId(item.medicationId(), item.userId());
        return medication.map(medicationDTO -> new NotificationEntity(
            item.userId(),
            NotificationType.SCHEDULE_OUT_OF_DOSES,
            item.medicationId(),
            "Out of medication",
            "You ran out of " + medicationDTO.name(),
            Instant.now(clock).plus(properties.dose().lifetime())
        )).orElse(null);
    }

    private NotificationEntity createAlmostOutOfMedicationNotification(UserScheduledMedicationDTO item) {
        Optional<MedicationDTO> medication = medicationManager.findByIdAndUserId(item.medicationId(), item.userId());
        return medication.map(medicationDTO -> new NotificationEntity(
            item.userId(),
            NotificationType.SCHEDULE_ALMOST_OUT_OF_DOSES,
            item.medicationId(),
            "Almost out of medication",
            "You will soon run out of " + medicationDTO.name(),
            Instant.now(clock).plus(properties.dose().lifetime())
        )).orElse(null);
    }
}
