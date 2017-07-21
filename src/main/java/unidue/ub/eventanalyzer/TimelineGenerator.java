package unidue.ub.eventanalyzer;

import org.springframework.beans.factory.annotation.Value;
import unidue.ub.media.monographs.Event;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Calculates a List of <code>ItemCounter</code> from list of
 * <code>Event</code>-objects.
 * 
 * @author Eike Spielberg
 * @version 1
 */
class TimelineGenerator {

	private List<ItemCounter> timeline = new ArrayList<>();

	@Value("${ub.statistics.status.lbs}")
	private  String lbs;

	@Value("${ub.statistics.status.student}")
	private String studentUser;

	@Value("${ub.statistics.status.intern}")
	private String internUser;

	@Value("${ub.statistics.status.extern}")
	private String externUser;

	@Value("${ub.statistics.status.happ}")
	private String happUser;

	@Value("${ub.statistics.status.lendable}")
	private String lendable;

	/**
	 * Calculates the individual part of different user groups at all
	 * <code>Event</code>-times.
	 *
	 * @param events
	 *            a list of Event-objects.
	 */

	TimelineGenerator(List<Event> events) {
		Collections.sort(events);
		ItemCounter itemcounter = new ItemCounter();

		if (!events.isEmpty()) {
			for (Event event : events) {
				String eventDate = event.getDate();
				if (event.getType().equals("loan") && event.getBorrowerStatus() != null) {
					if (studentUser.contains(event.getBorrowerStatus()))
						itemcounter.studentLoans++;
					else if (externUser.contains(event.getBorrowerStatus()))
						itemcounter.externLoans++;
					else if (internUser.contains(event.getBorrowerStatus()))
						itemcounter.internLoans++;
					else if (happUser.contains(event.getBorrowerStatus()))
						itemcounter.happLoans++;
					else
						itemcounter.elseLoans++;
				} else if (event.getType().equals("return") && event.getBorrowerStatus() != null) {
					if (studentUser.contains(event.getBorrowerStatus()))
						itemcounter.studentLoans--;
					else if (externUser.contains(event.getBorrowerStatus()))
						itemcounter.externLoans--;
					else if (internUser.contains(event.getBorrowerStatus()))
						itemcounter.internLoans--;
					else if (happUser.contains(event.getBorrowerStatus()))
						itemcounter.happLoans--;
					else
						itemcounter.elseLoans--;
				} else if (event.getType().equals("inventory") && event.getItem() != null) {
					itemcounter.stock++;
					if (event.getItem().getItemStatus() != null) {
						if (lendable.contains(event.getItem().getItemStatus()))
							itemcounter.stockLendable++;
					}
					if (lbs.contains(event.getItem().getCollection())) {
						itemcounter.stockLBS++;
					}
				} else if (event.getType().equals("deletion") && event.getItem() != null) {
					itemcounter.stock--;
					if (event.getItem().getItemStatus() != null) {
						if (lendable.contains(event.getItem().getItemStatus()))
							itemcounter.stockLendable--;
					}
					if (lbs.contains(event.getItem().getCollection())) {
						itemcounter.stockLBS--;
					}
					itemcounter.stockDeleted++;
				} else if (event.getType().equals("request") && event.getBorrowerStatus() != null) {
					if (studentUser.contains(event.getBorrowerStatus()))
						itemcounter.studentRequests++;
					else if (externUser.contains(event.getBorrowerStatus()))
						itemcounter.externRequests++;
					else if (internUser.contains(event.getBorrowerStatus()))
						itemcounter.internRequests++;
					else if (happUser.contains(event.getBorrowerStatus()))
						itemcounter.happRequests++;
					else
						itemcounter.elseRequests++;
				} else if (event.getType().equals("hold") && event.getBorrowerStatus() != null) {
					if (studentUser.contains(event.getBorrowerStatus()))
						itemcounter.studentRequests--;
					else if (externUser.contains(event.getBorrowerStatus()))
						itemcounter.externRequests--;
					else if (internUser.contains(event.getBorrowerStatus()))
						itemcounter.internRequests--;
					else if (happUser.contains(event.getBorrowerStatus()))
						itemcounter.happRequests--;
					else
						itemcounter.elseRequests--;
				}
				if (itemcounter.stock != 0) {
					ItemCounter dayInTimeline = itemcounter.clone();
					dayInTimeline.setDay(eventDate.substring(0, 10));
					timeline.add(dayInTimeline);
				}
			}
			ItemCounter lastDayInTimline = timeline.get(timeline.size() - 1);
			ItemCounter statusQuo = lastDayInTimline.clone();
			statusQuo.setDay(LocalDate.now().toString());
			timeline.add(statusQuo);
		} else {
			// if no events are found, just return an empty day with zeros
			ItemCounter dayInTimeline = new ItemCounter().setDay(LocalDate.now().toString());
			timeline.add(dayInTimeline);
		}
	}

	/**
	 * returns the timeline as list of <code>ItemCounter</code> objects.
	 * 
	 * @return timeline the timeline
	 */

	List<ItemCounter> getTimeline() {
		return timeline;
	}
}
