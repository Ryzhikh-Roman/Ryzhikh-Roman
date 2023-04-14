# Создание категории
import discord
from discord_bot import tree
from discord.ext import commands


@tree.command(name="add_category", description="Add category to the guild")#,guild=discord.Object(id=1031609704188739614))
@commands.has_permissions(administrator=True)
async def self(ctx: commands.Context, category: str):
    await ctx.guild.create_category(name=category)
    await ctx.response.send_message(f"{ctx.user}, u have created category '{category}'", ephemeral=True, delete_after=3.0)