FROM node:12

RUN mkdir -p /home/node/app/node_modules && chown -R node:node /home/node/app

# создание директории приложения
WORKDIR /home/node/app

# установка зависимостей
# символ астериск ("*") используется для того чтобы по возможности
# скопировать оба файла: package.json и package-lock.json
COPY /src/package*.json ./

USER node

RUN npm install
# Если вы создаете сборку для продакшн

# копируем исходный код
COPY --chown=node:node . .

EXPOSE 3000
CMD [ "node", "./src/bin/www" ]
