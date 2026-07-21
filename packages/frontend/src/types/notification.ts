import { NotificationType } from './api';

export interface Notification {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  referenceId: string | null;
  isRead: boolean;
  createdAt: string;
}

export interface UnreadCountResponse {
  count: number;
}

export interface NotificationPageResponse {
  content: Notification[];
  totalElements: number;
  totalPages: number;
}
