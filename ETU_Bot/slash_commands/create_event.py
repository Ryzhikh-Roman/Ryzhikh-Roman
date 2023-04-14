#Создание ивента
import discord
from discord.ext import commands
from discord_bot import tree, client
import enum
import typing

class Time(enum.Enum):
    apple = 1
    banana = 2
    cherry = 3

@tree.command(name="create_event", description="create event")
@commands.has_permissions(administrator=True)
async def self(ctx: commands.Context, title: str, repeat: typing.Literal['week','day'], time: Time, description: str, roles: str):
    client.events.add_event(client.events, title, repeat, time, description, roles)
    await ctx.response.send_message(f"U created event", ephemeral=True, delete_after=3.0)
