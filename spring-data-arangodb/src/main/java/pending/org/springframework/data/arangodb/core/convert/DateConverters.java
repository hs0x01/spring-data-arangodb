package pending.org.springframework.data.arangodb.core.convert;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

/**
 * Java の日付とカレンダーの規格外コンバージョンです。
 *
 * @author hs0x01
 */
public final class DateConverters {

	private DateConverters() {
	}

	/**
	 * このクラスで記述された全てのコンバータを返します。
	 *
	 * @return コンバータのリスト
	 */
	public static Collection<Converter<?, ?>> getConvertersToRegister() {
		List<Converter<?, ?>> converters = new ArrayList<Converter<?, ?>>();

		converters.add(DateToLongConverter.INSTANCE);
		converters.add(CalendarToLongConverter.INSTANCE);
		converters.add(NumberToDateConverter.INSTANCE);
		converters.add(NumberToCalendarConverter.INSTANCE);
		converters.add(StringToDateConverter.INSTANCE);
		converters.add(StringToCalendarConverter.INSTANCE);

		return converters;
	}

	@WritingConverter
	public enum DateToLongConverter implements Converter<Date, Long> {
		INSTANCE;

		@Override
		public Long convert(Date source) {
			return source == null ? null : source.getTime();
		}
	}

	@WritingConverter
	public enum CalendarToLongConverter implements Converter<Calendar, Long> {
		INSTANCE;

		@Override
		public Long convert(Calendar source) {
			return source == null ? null : source.getTimeInMillis() / 1000;
		}
	}

	@ReadingConverter
	public enum NumberToDateConverter implements Converter<Number, Date> {
		INSTANCE;

		@Override
		public Date convert(Number source) {
			if (source == null) {
				return null;
			}

			Date date = new Date();
			date.setTime(source.longValue());
			return date;
		}
	}

	@ReadingConverter
	public enum NumberToCalendarConverter implements Converter<Number, Calendar> {
		INSTANCE;

		@Override
		public Calendar convert(Number source) {
			if (source == null) {
				return null;
			}

			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(source.longValue() * 1000);
			return calendar;
		}
	}
	
	@ReadingConverter
	public enum StringToDateConverter implements Converter<String, Date> {
		INSTANCE;

		@Override
		public Date convert(String source) {
			if (source == null) {
				return null;
			}

			Date date = new Date();
			date.setTime(Long.valueOf(source));
			return date;
		}
	}

	@ReadingConverter
	public enum StringToCalendarConverter implements Converter<String, Calendar> {
		INSTANCE;

		@Override
		public Calendar convert(String source) {
			if (source == null) {
				return null;
			}

			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(Long.valueOf(source) * 1000);
			return calendar;
		}
	}
}
