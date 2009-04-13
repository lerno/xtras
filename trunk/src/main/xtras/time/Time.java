package xtras.time;

import xtras.lang.StringExtras;
import xtras.util.CollectionExtras;

import java.util.LinkedList;
import java.util.Date;
import java.text.SimpleDateFormat;

/** @author Christoffer Lerno */
public final class Time
{
	public static final long TEN_MILLISECONDS = 10;
	public static final long TWENTYFIVE_MILLISECONDS = 25;
	public static final long TWO_HUNDRED_MILLISECONDS = 200;

	public static final long HALF_A_SECOND = 500;
	public static final long ONE_SECOND = 1000;
	public static final long ONE_AND_A_HALF_SECOND = ONE_SECOND + HALF_A_SECOND;
	public static final long TWO_SECONDS = 2 * ONE_SECOND;
	public static final long THREE_SECONDS = 3 * ONE_SECOND;
	public static final long FOUR_SECONDS = 4 * ONE_SECOND;
	public static final long FIVE_SECONDS = 5 * ONE_SECOND;
	public static final long TEN_SECONDS = 10 * ONE_SECOND;
	public static final long FIFTEEN_SECONDS = 15 * ONE_SECOND;
	public static final long TWENTY_SECONDS = 20 * ONE_SECOND;
	public static final long THIRTY_SECONDS = 30 * ONE_SECOND;

	public static final long HALF_MINUTE = THIRTY_SECONDS;
	public static final long ONE_MINUTE = 60 * ONE_SECOND;
	public static final long TWO_MINUTES = 2 * ONE_MINUTE;
	public static final long THREE_MINUTES = 3 * ONE_MINUTE;
	public static final long FIVE_MINUTES = 5 * ONE_MINUTE;
	public static final long TEN_MINUTES = 10 * ONE_MINUTE;
	public static final long FIFTEEN_MINUTES = 15 * ONE_MINUTE;
	public static final long TWENTY_MINUTES = 20 * ONE_MINUTE;
	public static final long THIRTY_MINUTES = 30 * ONE_MINUTE;

	public static final long HALF_HOUR = THIRTY_MINUTES;
	public static final long ONE_HOUR = 60 * ONE_MINUTE;
	public static final long ONE_HOUR_AND_A_HALF = ONE_HOUR + HALF_HOUR;
	public static final long TWO_HOURS = 2 * ONE_HOUR;
	public static final long FIVE_HOURS = 5 * ONE_HOUR;
	public static final long SIX_HOURS = 6 * ONE_HOUR;
	public static final long TWELVE_HOURS = 12 * ONE_HOUR;

	public static final long ONE_DAY = 24 * ONE_HOUR;

	public static final long ONE_WEEK = 7 * ONE_DAY;

	public final static SimpleDateFormat TIME_FORMAT_MS = new SimpleDateFormat("HH:mm:ss.SSS");

	Time()
	{}

	/**
	 * Return a string describing the time difference as a string.
	 *
	 * @param timeDifferenceInMs the time difference in milliseconds.
	 * @return a string of the format x seconds/x minutes/x hours/x days.
	 */
	public static String timeIntervalAsPrettyString(long timeDifferenceInMs)
	{
		long timeDifferenceInSeconds = timeDifferenceInMs / 1000;
		int seconds = (int) (timeDifferenceInSeconds % 60);
		timeDifferenceInSeconds /= 60;
		int minutes = (int) (timeDifferenceInSeconds % 60);
		timeDifferenceInSeconds /= 60;
		int hours = (int) (timeDifferenceInSeconds % 24);
		timeDifferenceInSeconds /= 24;
		if (timeDifferenceInSeconds > 0)
		{
			return String.format("%dd %dh %dm %ds", timeDifferenceInSeconds, hours, minutes, seconds);
		}
		else if (hours > 0)
		{
			return String.format("%dh %dm %ds", hours, minutes, seconds);
		}
		else if (minutes > 0)
		{
			return String.format("%d min %d sec", minutes, seconds);
		}
		return String.format("%d second%s", seconds, seconds == 1 ? "" : "s");
	}

	/**
	 * Parses a string value to a long value representing milliseconds.
	 * <p>
	 * Ex. "2d 3h 30s"
	 *
	 * @param timeIntervalString a string to parse as a time interval.
	 * @return time in ms
	 * @throws IllegalArgumentException if this string couldn't be parsed.
	 */
	@SuppressWarnings({"IfStatementWithTooManyBranches"})
	public static long parseTimeInterval(String timeIntervalString)
	{

		long time = 0;
		for (String part : StringExtras.split(timeIntervalString, " "))
		{
			try
			{
				if (part.endsWith("ms"))
				{
					time += Long.parseLong(part.substring(0, part.length() - 2));
				}
				else if (part.endsWith("s"))
				{
					time += ONE_SECOND * Long.parseLong(part.substring(0, part.length() - 1));
				}
				else if (part.endsWith("m"))
				{
					time += ONE_MINUTE * Long.parseLong(part.substring(0, part.length() - 1));
				}
				else if (part.endsWith("h"))
				{
					time += ONE_HOUR * Long.parseLong(part.substring(0, part.length() - 1));
				}
				else if (part.endsWith("d"))
				{
					time += ONE_DAY * Long.parseLong(part.substring(0, part.length() - 1));
				}
				else
				{
					throw new IllegalArgumentException("Time unit missing from " + part);
				}
			}
			catch (NumberFormatException e)
			{
				throw new IllegalArgumentException("Bad value");
			}
		}
		return time;
	}

	/**
	 * Returns a representation of a ms time.
	 *
	 * @param timeValue the value in ms.
	 * @return time in the format "?d ?h ?m ?s ?ms"
	 */
	public static String timeIntervalToString(long timeValue)
	{
		LinkedList<String> rep = new LinkedList<String>();
		if (timeValue % 1000 != 0)
		{
			rep.addFirst(timeValue % 1000 + "ms");
		}
		timeValue /= 1000;
		if (timeValue % 60 != 0)
		{
			rep.addFirst(timeValue % 60 + "s");
		}
		timeValue /= 60;
		if (timeValue % 60 != 0)
		{
			rep.addFirst(timeValue % 60+ "m");
		}
		timeValue /= 60;
		if (timeValue % 24 != 0)
		{
			rep.addFirst(timeValue % 24 + "h");
		}
		timeValue /= 24;
		if (timeValue > 0)
		{
			rep.addFirst(timeValue  + "d");
		}
		return rep.isEmpty() ? "0ms" : CollectionExtras.join(rep, " ");
	}

	public static String toMsClock(long timeInMs)
	{
		return TIME_FORMAT_MS.format(new Date(timeInMs));
	}

}
