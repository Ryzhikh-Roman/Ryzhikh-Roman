#Вывод ивентов
import discord
from discord.ext import commands

from discord_bot import tree, client

@tree.command(name="show_events", description="show events")
@commands.has_permissions(administrator=True)
async def self(ctx: commands.Context):
    msgs = client.events.show(client.events)
    for msg in msgs:
        await ctx.channel.send(embed=msg)
