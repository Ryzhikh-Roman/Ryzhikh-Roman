import discord
from discord.utils import get

class DiscordManager:
    async def send_message(message, user):
        if message.author == user:
            return
        return

    async def add_role_by_reaction(client, payload, message_id, roles):
        channel = client.get_channel(payload.channel_id)
        message = await channel.fetch_message(payload.message_id)
        guild = client.get_guild(payload.guild_id)
        reaction = get(message.reactions, emoji=payload.emoji.name)

        if payload.member.id == client.user.id:
            return

        roles_list = []
        for i in roles:
            roles_list.append(discord.utils.get(guild.roles, name=i))

        if payload.message_id == message_id:
            match reaction.emoji:
                case '0️⃣':
                    if roles[0]:
                        role = discord.utils.get(guild.roles, name=roles[0])
                case '1️⃣':
                    if roles[1]:
                        role = discord.utils.get(guild.roles, name=roles[1])
                case '2️⃣':
                    if roles[2]:
                        role = discord.utils.get(guild.roles, name=roles[2])
                case '3️⃣':
                    if roles[3]:
                        role = discord.utils.get(guild.roles, name=roles[3])
                case '4️⃣':
                    if roles[4]:
                        role = discord.utils.get(guild.roles, name=roles[4])
                case '5️⃣':
                    if roles[5]:
                        role = discord.utils.get(guild.roles, name=roles[5])
                case '6️⃣':
                    if roles[6]:
                        role = discord.utils.get(guild.roles, name=roles[6])
                case '7️⃣':
                    if roles[7]:
                        role = discord.utils.get(guild.roles, name=roles[7])
                case '8️⃣':
                    if roles[8]:
                        role = discord.utils.get(guild.roles, name=roles[8])
                case '9️⃣':
                    if roles[9]:
                        role = discord.utils.get(guild.roles, name=roles[9])
                case _:
                    role = None

            for member_role in payload.member.roles:
                for _role in roles_list:
                    if member_role == _role:
                        try:
                            await payload.member.remove_roles(member_role)
                        except:
                            print('Error trying to remove roles: Member role is higher than bot role')

            if role:
                try:
                    await payload.member.add_roles(role)
                except:
                    print('Error trying to add role: Member role is higher than bot role')