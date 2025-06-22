export type NotificationType = 'CABINET_ENTRY_EXPIRED' | 'CABINET_ENTRY_ALMOST_EXPIRED' | 'DOCUMENT_EXPIRED' | 'DOCUMENT_ALMOST_EXPIRED' | 'SCHEDULE_OUT_OF_DOSES' | 'SCHEDULE_ALMOST_OUT_OF_DOSES' | 'INTAKE_EVENT';

export interface Notification {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  createdDate: string;
}
