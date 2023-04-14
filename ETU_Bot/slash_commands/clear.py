# Очистка сообщений канала
import discord
from discord.ext import commands

from discord_bot import tree


@tree.command(name="clear", description="Clear chosen amount of chat messages")#,guild=discord.Object(id=1031609704188739614))
@commands.has_permissions(administrator=True)
async def self(ctx: commands.Context, amount: int):
    deleted = await ctx.channel.purge(limit=amount)
    await ctx.response.send_message(f"{ctx.user}, u have deleted {len(deleted)} messages", ephemeral=True, delete_after=3.0)