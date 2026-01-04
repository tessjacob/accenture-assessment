package com.github.tessdev.holidayservice.model;

import java.util.Comparator;

public enum SortOrder {
    ASC {
        @Override
        public Comparator<HolidayCountResult> comparator() {
            return Comparator.comparingInt(HolidayCountResult::count);
        }
    },
    DESC {
        @Override
        public Comparator<HolidayCountResult> comparator() {
            return Comparator.comparingInt(HolidayCountResult::count).reversed();
        }
    };

    public abstract Comparator<HolidayCountResult> comparator();
}
