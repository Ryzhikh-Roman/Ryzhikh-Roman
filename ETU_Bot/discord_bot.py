import os
import json
import discord
from event_manager import Events
import github
from discord import app_commands
from discord_manager import DiscordManager

DISCORD_BOT_TOKEN = os.environ.get('DISCORD_BOT_TOKEN')
print(DISCORD_BOT_TOKEN)
GIT_TOKEN = os.environ.get('GIT_TOKEN')
print(GIT_TOKEN)

intents = discord.Intents.all()
intents.message_content = True


def start():
    client.run(DISCORD_BOT_TOKEN)


async def get_pulls(repo_name):
    g = github.Github(GIT_TOKEN)
    repo = g.get_repo(repo_name)
    pulls = repo.get_pulls(state='open', sort='created')
    return pulls


async def generate_message(message_type):
    with open('settings.json', 'r', encoding='utf_8_sig') as f:
        settings = json.load(f)
    return settings["messages"][message_type]


class DiscordBot(discord.Client):
    def __init__(self):
        super().__init__(intents=intents)
        self.events = Events
        self.synced = False
        self.message_id = None
        self.roles = None

    def track_message(self, message_id, roles):
        self.message_id = int(message_id)
        self.roles = roles.split(',')
        print(roles)

    async def on_ready(self):
        if not self.synced:
            await tree.sync()
            self.synced = True
        print('We have successfully loggged in as {0.user}'.format(self))

    async def on_message(self, message):
        await DiscordManager.send_message(message=message, user=client.user)

    async def on_raw_reaction_add(self, payload):
        if self.message_id is None:
            return
        await DiscordManager.add_role_by_reaction(client=client, payload=payload, message_id=self.message_id, roles=self.roles)

    async def on_member_join(self, member):
        guild = self.get_guild(self.guild)
        await member.add_roles(discord.utils.get(guild.roles, name='2022'))


client = DiscordBot()
tree = app_commands.CommandTree(client)

from slash_commands import *
