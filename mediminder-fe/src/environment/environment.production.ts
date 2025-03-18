// Environment variables are replaced by system environment variables when running through Docker
// see nginx.sh
export const environment = {
  production: true,
  apiUrl: '${ENV_API_URL}',
};
