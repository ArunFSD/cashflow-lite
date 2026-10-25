package com.cashflowlite.config;

import java.time.ZoneId;

/**
 * Time settings shared by the whole application.
 */
public final class AppTime {

	/** "Today" and "this month" are judged in this zone. */
	public static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");

	private AppTime() {
	}
}
