# SubtleNotify

"Service of unpredictable notifications". A service that sends users notifications not on a schedule, but based on patterns in their activity. Since the notifications are not random but predictable, albeit in an unexpected way, the project is named **SubtleNotify** - subtle notification.

## Tech stack:
• Java 17
• Spring Boot 3.5.7
• JPA (HSQL)
• LogBack
• Actuators
• Swagger
• Gradle

## To compile

• gradle

## To run

• gradle bootRun


### Behavior Logic

With each [REST/action](http://localhost:8080/swagger-ui/index.html#/:~:text=/api/subtlenotify/action) call, `actionType` sets appropriate triggers and associated notifications. The order of operations is as follows:

- Record `action` in the database
- Generate statistics as a series of `timestamp` for a specific `userId` and `actionType` over a known time period
- Find triggers matching the `actionType` and apply rules to the timestamp series
- If any rule triggers, save an "unpredictable" notification to the database

> **PS:** We'll limit ourselves to weekly notifications. Monthly and yearly notifications are expected in future versions :)<br/>
> **PS:** No restrictions are provided for "annoying messages" except for deduplication

## Examples of Triggers and Notifications

1. **writes comments** - at night three days in a row → morning notification
2. **buys coffee** - on Mon, Wed, Fri → Tuesday notification about a discount
3. **opens app** - every day at 9 AM → if opened at 8 AM, notify immediately
4. **reads articles** - on Sundays → next week Saturday evening send notification
5. **orders delivery** - every other day → on the third cycle send notification
6. **drinks tea** - every evening or on Sundays → if missed, notify next time
7. **takes steps** - at 10 AM every day → next morning send notification
8. **checks tasks** - on weekdays after noon → next week morning send notification
9. **likes posts** - on Wednesdays → notify next Wednesday morning


## Heuristic of Unpredictability

"Unpredictability" is embedded in the trigger data mainly in fields starting with expect***. Notifications are generated either immediately with a small 10-minute delay or in advance on a specific day and hour of the next week (`NotifyMoment`). Multiple notifications can be generated at once.

### Example of a "morning" trigger set on weekdays Monday, Wednesday and Friday for 2 weeks in a row:
  ```json
{
      ***
     "expectWeekDays" : "mon,wed,fri",
     "expectHowOften" : 2,
     "expectFromHr" : 9,
     "expectToHr" : 11,
      ***
}
```


### Example of an "afternoon" trigger set as a sequence of days, more precisely every 2nd day (every other day) 3 times in a row:
  ```json
{
      ***
     "expectEveryDays" : 2,
     "expectHowOften" : 3,
     "expectFromHr" : 13,
     "expectToHr" : 17,
      ***
}
```

- `expectWeekDays` - which days of the week are expected for `action` from the range "sun, mon, tue, wed, thu, fri, sat" and for how many consecutive weeks specified in `expectHowOften`
- `expectEveryDays` - how many consecutive days (1) or every other day (2) or every third day (3) etc. are `action` expected and for how many consecutive days specified in `expectHowOften`
- `expectFromHr` and `expectToHr` - expected hours of `action` (default from 0 to 24 hours)
- `missPreviousTime` - true/false flag to calculate the case when the previous notification was missed (see examples [#6 "drinks tea"](https://github.com/serhioms/SubtleNotify?tab=readme-ov-file#:~:text=%D1%82%D1%80%D0%B5%D1%82%D0%B8%D0%B9%20%D1%86%D0%B8%D0%BA%D0%BB%20%D1%83%D0%B2%D0%B5%D0%B4%D0%BE%D0%BC%D0%B8%D1%82%D1%8C-,%D0%BF%D1%8C%D1%91%D1%82%20%D1%87%D0%B0%D0%B9,-%D0%BA%D0%B0%D0%B6%D0%B4%D1%8B%D0%B9%20%D0%B2%D0%B5%D1%87%D0%B5%D1%80%20%D0%B8%D0%BB%D0%B8))
> **PS:** Be sure to set one of two rules `expectWeekDays` or `expectEveryDays`, but not both at the same time!


### Notification Structure:
  ```json
{
  "timestamp": "2025-11-06T21:40:00",
  "notification": "Looks like you're a night owl 🦉",
}
```

### Date and time of notification is set in the trigger:

 ```json
{
    ***
    "notifyMoment" : "immediately | next_time",
    "actualWeekDays" : "sat,tue",
    "actualHours" : "5,6,7,8", 
    ***
}
```

- `notifyMoment` - two options are provided: `immediately` i.e. 10 minutes after `actualHours` and `next_time` i.e. on the next day/week in `actualHours`
- `actualWeekDays` - an appropriate day is selected from the list. If not specified, an appropriate day is selected from the `expectEveryDays` list
- `actualHours` - an appropriate hour is selected from the list. If not specified, it is taken from `expectFromHr` plus/minus 10 minutes


## Microservice Architecture

Standard layered architecture - [SubtleNotifyController](src/main/java/ru/alumni/hub/subtlenotify/controller/SubtleNotifyController.java) → [Services](src/main/java/ru/alumni/hub/subtlenotify/service) → [Repositories](src/main/java/ru/alumni/hub/subtlenotify/repository) → [Model](src/main/java/ru/alumni/hub/subtlenotify/model)

The main class where unpredictable notifications are generated is [SubtleNotifyService.java](src/main/java/ru/alumni/hub/subtlenotify/service/SubtleNotifyService.java)

The notification generation procedure is called asynchronously via REST `/action` on a "fire and forget" principle.

### Pros/Cons:

- ✅ Reduce `/action` microservice execution time
- ✅ Server load is controlled by a limited pool of worker threads [AsyncConfig.java](src/main/java/ru/alumni/hub/subtlenotify/config/AsyncConfig.java)


[REST API](http://localhost:8080/swagger-ui/index.html)

[Postman Collection](http://localhost:8080/AlumniHub.postman_collection.json) / [LOCAL environment](http://localhost:8080/LOCAL.postman_environment.json)

[Actuators](http://localhost:8080/actuators.html)

[DB Schema](http://localhost:8080/SubtleNotifyDB.png)


