FROM node:alpine AS build
WORKDIR /usr/src/app
COPY package.json package-lock.json ./
RUN npm install
COPY angular.json tsconfig.* ngsw-config.json ./
COPY src/ src/
ENV NODE_ENV=production
RUN npm run build

FROM nginx:alpine-slim
COPY --from=build /usr/src/app/dist/mediminder-fe/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/nginx.conf.template
COPY entrypoint.sh /bin/entrypoint.sh
RUN touch /var/run/nginx.pid && \
    touch /etc/nginx/conf.d/default.conf && \
    chown -R nginx:nginx /var/cache/nginx /var/run/nginx.pid /bin/entrypoint.sh /usr/share/nginx/html /etc/nginx/conf.d/default.conf
USER nginx
EXPOSE 8080
ENTRYPOINT ["/bin/entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
