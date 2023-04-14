# История переписки на почту

import os
import typing
import smtplib
import discord
import mimetypes
from email import encoders
from discord_bot import tree
from discord.ext import commands
from email.mime.base import MIMEBase
from email.mime.text import MIMEText
from email.mime.image import MIMEImage
from email.mime.audio import MIMEAudio
from email.mime.multipart import MIMEMultipart


def send_email(addr_to, msg_subj, msg_text, files):
    addr_from = "79215507822@yandex.ru"
    password = "cigpknfbcjdgaclr"

    msg = MIMEMultipart()
    msg['From'] = addr_from
    msg['To'] = addr_to
    msg['Subject'] = msg_subj

    body = msg_text
    msg.attach(MIMEText(body, 'plain'))

    process_attachement(msg, files)

    server = smtplib.SMTP_SSL('smtp.yandex.ru', 465)
    server.login(addr_from, password)
    server.send_message(msg)
    server.quit()


def process_attachement(msg, files):
    for f in files:
        if os.path.isfile(f):
            attach_file(msg, f)
        elif os.path.exists(f):
            dir = os.listdir(f)
            for file in dir:
                attach_file(msg, f + "/" + file)


def attach_file(msg, filepath):
    filename = os.path.basename(filepath)
    ctype, encoding = mimetypes.guess_type(filepath)
    if ctype is None or encoding is not None:
        ctype = 'application/octet-stream'
    maintype, subtype = ctype.split('/', 1)
    if maintype == 'text':
        with open(filepath, encoding="utf-8") as fp:
            file = MIMEText(fp.read(), _subtype=subtype)
            fp.close()
    elif maintype == 'image':
        with open(filepath, 'rb') as fp:
            file = MIMEImage(fp.read(), _subtype=subtype)
            fp.close()
    elif maintype == 'audio':
        with open(filepath, 'rb') as fp:
            file = MIMEAudio(fp.read(), _subtype=subtype)
            fp.close()
    else:
        with open(filepath, 'rb') as fp:
            file = MIMEBase(maintype, subtype)
            file.set_payload(fp.read())
            fp.close()
            encoders.encode_base64(file)
    file.add_header('Content-Disposition', 'attachment', filename=filename)
    msg.attach(file)


@tree.command(name="chat_history", description="Sends chat history to specified email")
              #guild=discord.Object(id=1031609704188739614))
@commands.has_permissions(administrator=True)
async def self(ctx: commands.Context, email: str, limit: int = 200):
    with open(ctx.channel.name + ".txt", "w+", encoding="utf-8") as file:
        messages = [message async for message in ctx.channel.history(limit=limit)]
        for message in reversed(messages):
            file.write(
                f"{message.created_at.strftime('%Y-%m-%d %H:%M:%S')} {message.author.display_name}: {message.content}\n")

    path = os.path.abspath(file.name)
    files = [path]
    send_email(email, "История чата", f"История чата {ctx.channel.name}:", files)

    await ctx.response.send_message(f"{ctx.user}, u write {email} and {ctx.channel}", ephemeral=True, delete_after=3.0)
