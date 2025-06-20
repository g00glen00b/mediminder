// Environment variables are replaced by system environment variables when running through Docker
// see nginx.sh
export const environment = {
  production: true,
  logoutHandler: 'https://dev-vmcdsx7rgptuj26g.us.auth0.com/v2/logout?client_id=4KZ1MpQoRQpb7YZrYVF8U91pcajlS2HV&returnTo=http://localhost:4200',
  loginHandler: '/oauth2/authorization/auth0',
  apiUrl: '../api'
};
