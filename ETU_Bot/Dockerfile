#FROM python:3

FROM gorialis/discord.py

RUN mkdir -p /usr/src/bot
RUN pip install pygithub
RUN pip install transliterate
RUN pip install ics

WORKDIR /usr/src/bot



COPY . .



CMD [ "python3", "main.py" ]
