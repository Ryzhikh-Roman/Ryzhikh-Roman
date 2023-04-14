import discord
from discord.ext import commands

from discord_bot import tree


@tree.command(name="test", description="test")#, guild=discord.Object(id=1031609704188739614))
@commands.has_permissions(administrator=True)
async def self(interaction: discord.Interaction, name: str):
    await interaction.response.send_message(f"Hello {name}", delete_after=3.0)
