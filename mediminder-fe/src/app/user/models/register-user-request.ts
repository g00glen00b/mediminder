export interface RegisterUserRequest {
  email: string;
  password: string;
  name?: string;
  timezone?: string;
}
