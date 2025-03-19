export interface ConfirmationDialogData {
  title: string;
  content: string;
  okLabel: string;
  cancelLabel: string;
  type: 'info' | 'error';
}
