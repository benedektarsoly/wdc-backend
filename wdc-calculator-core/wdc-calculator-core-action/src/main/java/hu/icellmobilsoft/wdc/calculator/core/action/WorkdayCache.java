/*-
 * #%L
 * WDC
 * %%
 * Copyright (C) 2020 i-Cell Mobilsoft Zrt.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package hu.icellmobilsoft.wdc.calculator.core.action;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;

import hu.icellmobilsoft.wdc.calculator.core.action.type.HolidayType;

/**
 * Workday Calendar caching class
 *
 * @author janos.hamrak
 */
@ApplicationScoped
public class WorkdayCache {

    private Map<Year, TreeMap<LocalDate, WorkdayCacheData>> cache = new HashMap<>();

    /**
     * Returns the cache of workdays
     *
     * @return Map<Year, TreeMap<LocalDate, WorkdayCacheData>> Map with initialized years as keys and TreeMaps as values. Every TreeMap has LocalDates as keys
     *         (every date in the given year) and a WorkdayCacheData as value.
     */
    public Map<Year, TreeMap<LocalDate, WorkdayCacheData>> getCache() {
        return cache;
    }

    /**
     * Puts the given date and workday boolean in the cache. If the given date is not present in the cache, first initializes its year.
     *
     * @param date
     *            LocalDate to put
     * @param isWorkday
     *            boolean value, referring to whether the given date is a workday
     * @deprecated
     *          As of version 1.2.0.
     *          Use {@link #put(LocalDate, boolean, HolidayType, LocalDate, String)} instead.
     */
    @Deprecated
    public void put(LocalDate date, boolean isWorkday) {
        put(date, isWorkday, isWorkday ? null : HolidayType.WEEKEND, null, null);
    }

    /**
     * Inserts given date, with given descriptor information into the cache.
     * If the given date is not present in the cache, first initializes its year.
     *
     * @param date
     *          date to insert
     * @param isWorkday
     *          boolean value, referring to whether the given date is a workday
     * @param holidayType
     *          type of non-workday date
     * @param substitutedDay
     *          workday, non-workday substitution descriptor
     * @param description
     *          description of date
     */
    public void put(LocalDate date, boolean isWorkday, HolidayType holidayType, LocalDate substitutedDay, String description) {
        if (date == null) {
            return;
        }
        Year year = Year.of(date.getYear());
        if (!this.getCache().containsKey(year)) {
            this.initYear(year);
        }

        WorkdayCacheData workdayCacheData = new WorkdayCacheData();
        workdayCacheData.setDate(date);
        workdayCacheData.setWorkday(isWorkday);
        workdayCacheData.setSubstitutedDay(substitutedDay);
        workdayCacheData.setDescription(description);
        workdayCacheData.setHolidayType(holidayType);

        this.getCache().get(year).put(date, workdayCacheData);
    }

    /**
     * Initializing cache of given year with weekdays and weekends.
     *
     * @param year
     *            year to init
     */
    public void initYear(Year year) {
        if (year == null)
            return;
        LocalDate yearWithNowDate = LocalDate.now().withYear(year.getValue());
        final LocalDate start = yearWithNowDate.with(TemporalAdjusters.firstDayOfYear());
        final LocalDate end = yearWithNowDate.with(TemporalAdjusters.lastDayOfYear());
        final long days = start.until(end, ChronoUnit.DAYS);
        cache.put(year, Stream.iterate(start, d -> d.plusDays(1)).limit(days + 1)
                .collect(Collectors.toMap(d -> d, WorkdayCache::initDay, (o1, o2) -> o2, TreeMap::new)));
    }

    private static boolean isWeekday(LocalDate localDate) {
        if (localDate == null)
            return false;
        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        return !(dayOfWeek.equals(DayOfWeek.SATURDAY) || dayOfWeek.equals(DayOfWeek.SUNDAY));
    }

    private static WorkdayCacheData initDay(LocalDate date) {
        if (date == null) {
            return null;
        }

        WorkdayCacheData workdayCacheData = new WorkdayCacheData();
        workdayCacheData.setDate(date);
        workdayCacheData.setWorkday(isWeekday(date));

        return workdayCacheData;
    }
}
