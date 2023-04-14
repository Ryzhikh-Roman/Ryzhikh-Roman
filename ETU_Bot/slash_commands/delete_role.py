# Удаление указаной роли у всех участников
import discord
from discord.ext import commands

from discord_bot import tree


@tree.command(name="delete_role", description="Delete chosen role from all members")#,guild=discord.Object(id=1031609704188739614))
@commands.has_permissions(administrator=True)
async def self(ctx: commands.Context, role: str):
    for member in ctx.guild.members:
        await member.remove_roles(discord.utils.get(ctx.guild.roles, name=role))
    await ctx.response.send_message(f"{ctx.user}, u have delete role '{role}' from all members", ephemeral=True, delete_after=3.0)