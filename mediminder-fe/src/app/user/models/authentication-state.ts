import {User} from './user';

export interface AuthenticationState {
  user?: User;
  initialized: boolean;
}

export const EMPTY_STATE: AuthenticationState = {initialized: false};
