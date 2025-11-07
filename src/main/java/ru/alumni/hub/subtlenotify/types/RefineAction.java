package ru.alumni.hub.subtlenotify.types;

import ru.alumni.hub.subtlenotify.model.Action;

import java.time.temporal.WeekFields;
import java.util.Locale;

public class RefineAction extends Action {

    public RefineAction(Action action) {
        super(action.getId(), action.getUserId(), action.getActionType(), action.getTimestamp());
    }

    public Integer getWeekOfYear(){
        return super.getTimestamp().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
    }

    public int getDayOfYear(){
        return super.getTimestamp().getDayOfYear();
    }

    public String getDayOfWeek(){
        return super.getTimestamp().getDayOfWeek().name();
    }

    public int getHourOfDay(){
        return super.getTimestamp().getHour();
    }

    public int minuteOfHour(){
        return super.getTimestamp().getMinute();
    }
}
