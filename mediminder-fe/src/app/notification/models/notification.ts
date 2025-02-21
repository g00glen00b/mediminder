export interface Notification {
  id: string;
  type: 'INFO' | 'WARNING';
  title: string;
  message: string;
  createdDate: string;
}
