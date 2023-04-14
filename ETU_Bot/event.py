import discord

class Event:
    def __init__(self, index, title, repeat, time, description, roles):
        self.index = index
        self.title = title
        self.time = time
        self.repeat = repeat
        self.description = description
        self.roles = roles
        #freq
        #interval

    def __str__(self):
        return f"{self.index}: {self.time} {self.repeat} event ({self.description}) for roles {self.roles}"

    def get_embed(self):
        embed = discord.Embed(title=self.title, description=self.description, color=0xdb0000)
        embed.add_field(name="Участвующие роли", value=self.roles, inline=False)
        embed.add_field(name="Время проведения", value=f"{self.time}, {self.repeat}", inline=False)
        embed.set_image(url="https://sun9-68.userapi.com/impf/_zQ2R2fKZOfoDKpuCB7105S9KyR_LYTZ3dq-zg/Xx0EgGVkYeE.jpg?size=2100x2100&quality=96&sign=991f0bdc232198c23847cf2118bfdf7a&type=album")
        return embed
        #await ctx.send(embed=embed)

    def show(self):
        return f"{self.index}: {self.time} {self.repeat} event ({self.description}) for roles {self.roles}"
        #return embed

    @classmethod
    def create(cls, **kwargs):
        event = cls(None, None, None, None, None, None)
        [event.__setattr__(attr, kwargs[attr]) for attr in kwargs]
        return event
