from datetime import date, datetime, timedelta
from ics import Calendar, Event
from ics.grammar.parse import ContentLine
from event import Event as Discord_event


class Events:
    events = []

    def add_event(self, title, repeat, time, description, roles):
        if len(self.events) == 0:
            index = 0
        else:
            index = self.events[-1].index + 1
        one_event = Discord_event(index, title, repeat, time, description, roles)
        self.events.append(one_event)

    def delete_event(self, index, title, repeat, time, description, roles):
        del self.events[index]

    def show(self):
        # Возвращать строку или сразу объект embed?
        msgs = []
        for i in self.events:
            msgs.append(i.get_embed())
        return msgs

    """
    def save(self, filename):
        calendar = Calendar()
        monday = date.today() + timedelta(days=-date.today().weekday(), weeks=1)
        utc_timetable = ["5:00", "6:50", "8:40", "10:40", "12:30", "14:20", "16:05", "17:50"]
        for event in self.events:
            today = monday + timedelta(days=self.days.index(event.week_day)) + timedelta(weeks=int(event.week) - 1)
            event = Event()
            event.name = f'{event.subject} | {event.teacher}'
            lesson_time = utc_timetable[event.lesson_index]
            lesson_begin = f'{today.strftime("%Y-%m-%d")} ' \
                           f'{lesson_time if len(lesson_time.split(":")[0]) != 1 else "0" + lesson_time}:00'
            event.begin = lesson_begin
            event.end = datetime.strftime(datetime.strptime(lesson_begin, '%Y-%m-%d %H:%M:%S') +
                                          timedelta(hours=1, minutes=35), '%Y-%m-%d %H:%M:%S')
            extra = ContentLine.parse('RRULE:FREQ=WEEKLY;INTERVAL=2')
            event.extra.append(extra)
            calendar.events.add(event)
        with open(filename, 'w', encoding='utf-8') as file:
            file.writelines(calendar)

    # The function of loading the schedule from a file
    def load(cls, filename):
        with open(filename, 'r', encoding='utf-8') as file:
            calendar = Calendar(file.read())
        monday = date.today() + timedelta(days=-date.today().weekday(), weeks=1)
        manager = cls()
        for event in calendar.events:
            week = '2' if datetime.strptime(event.begin.strftime('%Y-%m-%d %H:%M:%S'), '%Y-%m-%d %H:%M:%S') > \
                          datetime.combine(monday + timedelta(weeks=1), datetime.min.time()) else '1'
            lesson_begin = event.begin.strftime('%H:%M')
            lesson_index = cls.timetable.index(lesson_begin if lesson_begin[0] != '0' else lesson_begin[1:])
            week_day = cls.days[event.begin.weekday()]
            teacher = event.name.split(' | ')[-1]
            subject = event.name.split(' | ')[0]
            manager.add_lesson(subject, week, week_day, lesson_index, teacher=teacher)
        return manager
    """




