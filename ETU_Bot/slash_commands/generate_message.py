# Получение списка пул-реквестов
import re
import discord
from discord.utils import get
from discord.ext import commands
from transliterate import translit
from discord_bot import tree, DiscordBot, generate_message, get_pulls

roles_list = ['Преподаватель', 'Модератор']


@tree.command(name="generate_message", description="Generate message for students")#,guild=discord.Object(id=1031609704188739614))
async def self(ctx: commands.Context, message_type: str, repo_name: str, work: str = None):
    check = False
    for member_role in ctx.user.roles:
        for _role in roles_list:
            if member_role.name == _role:
                check = True

    if not check:
        await ctx.response.send_message(f"You don't have permissions", delete_after=3.0)
        return

    text = await generate_message(message_type)
    text_begin, text_end = text.split('$people')
    pulls = await get_pulls(repo_name)
    mentions = ""
    work_number = None
    for pr in pulls:
        try:
            surname, name, work_type = re.findall('[^_]+', pr.title)
        except:
            await ctx.channel.send(f"""
                    {text_begin}
                    {pr.title}
                    {text_end}
                    """)
            await ctx.response.send_message("Success!", ephemeral=True, delete_after=3.0)
            return

        if work_type != work and work is not None:
            continue

        group = re.search('\d+', ctx.channel.name)
        if group is None:
            await ctx.response.send_message("Вы не в канале группы!", ephemeral=True, delete_after=3.0)
            return

        full_nick = surname + '.' + name + '.' + group[0]
        nick = full_nick.replace('ya', 'ja')
        nick = translit(nick, 'ru')
        user = get(ctx.guild.members, nick=nick)

        if work:
            work_number = re.search('\d+', work)
            if work_number:
                work_number = work_number[0]

        if work_number:
            if user:
                mentions += '\n' + user.mention + f" -- Лабораторная работа номер {work_number}"
            else:
                mentions += '\n' + nick + f" -- Лабораторная работа номер {work_number}"
        elif work == 'cw':
            if user:
                mentions += '\n' + user.mention + f" -- Курсовая работа"
            else:
                mentions += '\n' + nick + f" -- Курсовая работа"
        else:
            if user:
                mentions += '\n' + user.mention + f" -- работа {work_type}"
            else:
                mentions += '\n' + nick + f" -- работа {work_type}"

    if len(mentions) == 0:
        await ctx.response.send_message("No one has done this job", ephemeral=True, delete_after=3.0)
        return

    await ctx.channel.send(f"""
        {text_begin}
        {mentions}
        {text_end}
        """)
    await ctx.response.send_message("Success!", ephemeral=True, delete_after=3.0)