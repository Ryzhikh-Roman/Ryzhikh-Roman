# Удаление пользователей указаной роли
import discord
from discord.ext import commands

from discord_bot import tree


@tree.command(name="delete_members_by_role", description="Delete all members attached to chosen role")#,guild=discord.Object(id=1031609704188739614))
@commands.has_permissions(administrator=True)
async def self(ctx: commands.Context, role: str):
    members = []
    for member in ctx.guild.members:
        if discord.utils.get(ctx.guild.roles, name=role) in member.roles:
            members.append(await member.kick())
    await ctx.response.send_message(f"{ctx.user}, u have kicked '{len(members)}' members", ephemeral=True, delete_after=3.0)