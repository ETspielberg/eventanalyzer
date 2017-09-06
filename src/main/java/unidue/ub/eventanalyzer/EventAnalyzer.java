package unidue.ub.eventanalyzer;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.log4j.Logger;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import unidue.ub.media.analysis.Eventanalysis;
import unidue.ub.media.monographs.Event;
import unidue.ub.settings.fachref.ItemGroup;
import unidue.ub.settings.fachref.Stockcontrol;
import unidue.ub.settings.fachref.UserGroup;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;



/**
 * Calculates <code>Eventanalysis</code> from list of <code>Event</code>-objects
 * or a document docNumber.
 * 
 * @author Eike Spielberg
 * @version 1
 */
@StepScope
class EventAnalyzer {

	EventAnalyzer() {}

	private static final Logger LOGGER = Logger.getLogger(EventAnalyzer.class);

	@Value("${ub.statistics.settings.url}")
	private static String settingsUrl;

	private String relevantUserCategories;

	private String irrelevantUserCategories;

	private String relevantItemCategories;

	private UsageCounters usagecounter;

	private Map<String, String> userGroups;

	private Map<String, String> itemGroups;

	/**
	 * Calculates the loan and request parameters for a given List of Events
	 * with the parameters in the Stockcontrol. The results are stored
	 * in an DocumentAnalysis object.
	 *
	 * @param events a list of Event-objects.
	 */

	Eventanalysis analyze(List<Event> events, Stockcontrol stockcontrol) {
		Collections.sort(events);

		//build new analysis and set some fields
		Eventanalysis analysis = new Eventanalysis();
		analysis.setDate(new Date());
		if (stockcontrol.getIdentifier() != null)
			analysis.setStockcontrolId(stockcontrol.getIdentifier());

		//start analysis
		if (!events.isEmpty()) {
			prepareUserCategories();
			prepareItemCategories();

			LocalDate TODAY = LocalDate.now();

			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			Hashtable<Integer, Long> allMaxLoansAbs = new Hashtable<>();
			LocalDate scpStartDate = TODAY.minus(stockcontrol.getYearsToAverage(), ChronoUnit.YEARS);
			LocalDate scpStartYearRequests = TODAY.minus(stockcontrol.getYearsOfRequests(), ChronoUnit.YEARS);
			LocalDate scpMiniumumDate = TODAY.minus(stockcontrol.getMinimumYears(), ChronoUnit.YEARS);
			Collections.sort(events);

			usagecounter = new UsageCounters();

			Integer yearsBefore = TODAY.getYear() - LocalDate.parse(events.get(0).getDate(), dtf).getYear();
			for (int year = 0; year <= yearsBefore; year++) {
				allMaxLoansAbs.put(year, 0L);
			}
			UsageCounters oldUsagecounter = usagecounter.clone();
			for (Event event : events) {
				LocalDate eventDate = LocalDate.parse(event.getDate().substring(0, 10), dtf);
				if (!eventDate.isAfter(TODAY))
					continue;

				updateItemCounter(event);
				int timeIntervall = TODAY.getYear() - eventDate.getYear();
				long loans = usagecounter.getAllLoans();
				for (int i = timeIntervall; i <= yearsBefore; i++) {
					long maxLoans = Math.max(allMaxLoansAbs.get(i), loans);
					allMaxLoansAbs.replace(i, maxLoans);
				}
				if (eventDate.isAfter(scpStartDate)) {
					loans = Math.max(loans, oldUsagecounter.getAllLoans());
					analysis.setMaxLoansAbs(Math.max(loans, analysis.getMaxLoansAbs()));
					double relativeLoan = usagecounter.getRelativeLoan();
					analysis.setMaxRelativeLoan(Math.max(relativeLoan, analysis.getMaxRelativeLoan()));
				} else
					oldUsagecounter = usagecounter.clone();
				long maxItemsNeeded = usagecounter.getStockLendable() + usagecounter.requests;
				if (eventDate.isAfter(scpStartYearRequests)) {
					analysis.setMaxNumberRequest(Math.max(usagecounter.requests, analysis.getNumberRequests()));
					analysis.setMaxItemsNeeded(Math.max(maxItemsNeeded, analysis.getMaxItemsNeeded()));
				}


				// try to get the end date. If no end date is given in the event,
				// set the end date to the actual date.
				Event endEvent = event.getEndEvent();
				LocalDate endDate;
				if (endEvent != null)
					endDate = LocalDate.parse(endEvent.getDate().substring(0, 10), dtf);
				else
					endDate = TODAY;
				if (endDate.isBefore(scpStartDate))
					continue;
				else if (eventDate.isBefore(scpStartDate))
					eventDate = scpStartDate;
				int days = (int) ChronoUnit.DAYS.between(eventDate, endDate) + 1;

				// analyze loan events
				if (event.getType().equals("loan")) {
					if (event.getBorrowerStatus() == null) {
						LOGGER.info("no Borrower given");
						usagecounter.daysLoaned +=days;
					} else {
						if (relevantUserCategories.contains(event.getBorrowerStatus()))
							usagecounter.daysLoaned += days;
						else if (irrelevantUserCategories.contains(event.getBorrowerStatus()))
							usagecounter.daysStockLendable -= days;
						else
							usagecounter.daysLoaned += days;
					}

					// analyze stock events
				} else if (event.getType().equals("inventory")) {
					if (event.getItem() != null) {
						if (event.getItem().getItemStatus() != null) {
							if (relevantItemCategories.contains(event.getItem().getItemStatus()))
								usagecounter.daysStockLendable += days;
						} else
							usagecounter.daysStockLendable += days;
					} else
						usagecounter.daysStockLendable += days;

					// analyze request events
				} else if (event.getType().equals("request")) {
					if (eventDate.isAfter(scpStartYearRequests)) {
						analysis.increaseNumberRequests();
						usagecounter.daysRequested += days;
					}
				}
			}
			double slope = calculateSlope(allMaxLoansAbs,yearsBefore);
			analysis.setSlope(slope);
			analysis.setDaysRequested(usagecounter.daysRequested);
			analysis.setMeanRelativeLoan(usagecounter.getMeanRelativeLoan());

			double staticBuffer = stockcontrol.getStaticBuffer();
			double variableBuffer = stockcontrol.getVariableBuffer();

			if (analysis.getMaxRelativeLoan() != 0) {
				double ratio = analysis.getMeanRelativeLoan() / analysis.getMaxRelativeLoan();
				if (staticBuffer < 1 && variableBuffer < 1)
					analysis.setProposedDeletion((int) ((analysis.getLastStock() - analysis.getMaxLoansAbs()) * (1
							- staticBuffer
							- variableBuffer * ratio)));
				else if (staticBuffer >= 1 && variableBuffer < 1)
					analysis.setProposedDeletion(
							(int) ((analysis.getLastStock() - analysis.getMaxLoansAbs() - staticBuffer)
									* (1 - variableBuffer * ratio)));
				else if (staticBuffer >= 1 && variableBuffer >= 1)
					analysis.setProposedDeletion(
							(int) ((analysis.getLastStock() - analysis.getMaxLoansAbs() - staticBuffer)
									- variableBuffer * ratio));
				else if (staticBuffer < 1 && variableBuffer < 1)
					analysis.setProposedDeletion(
							(int) ((analysis.getLastStock() - analysis.getMaxLoansAbs()) * (1 - staticBuffer)
									- variableBuffer * ratio));

				if (analysis.getProposedDeletion() < 0)
					analysis.setProposedDeletion(0);
				if (analysis.getProposedDeletion() == 0 && ratio > 0.5)
					analysis.setProposedPurchase((int) (-1 * analysis.getLastStock() * 0.001 * ratio));
			} else {
				if (staticBuffer < 1)
					analysis.setProposedDeletion(
							(int) ((analysis.getLastStock() - analysis.getMaxLoansAbs()) * (1 - staticBuffer)));
				else
					analysis.setProposedDeletion(
							(int) (analysis.getLastStock() - analysis.getMaxLoansAbs() - staticBuffer));
			}
			if (events.size() > 0) {
				if (LocalDate.parse(events.get(0).getDate().substring(0, 10), dtf).isAfter(scpMiniumumDate))
					analysis.setProposedDeletion(0);
			}

			if (analysis.getLastStock() - analysis.getProposedDeletion() < 2 && analysis.getLastStock() >= 3)
				analysis.setComment("ggf. umstellen");

			if ((double) analysis.getDaysRequested() / (double) analysis.getNumberRequests() >= stockcontrol.getMinimumDaysOfRequest()) {
				analysis.setProposedPurchase(analysis.getMaxItemsNeeded() - analysis.getLastStock());
			}
			analysis.setLastStock(usagecounter.getStockLendable());
		} else {
			analysis.setComment("no events found");
		}
		return analysis;
	}

	private double calculateSlope(Hashtable<Integer, Long> allMaxLoansAbs, int yearsBefore) {
		SimpleRegression trend = new SimpleRegression();
		for (int year = 1; year <= yearsBefore; year++) {
			trend.addData(year, (double) allMaxLoansAbs.get(year));
		}
		double slope = 0;
		if (trend.getN() > 1)
			slope = trend.getSlope();
		return slope;
	}

	private void updateItemCounter(Event event) {
		if (event.getType().equals("loan") && event.getBorrowerStatus() != null) {
			if (userGroups.get("student").contains(event.getBorrowerStatus()))
				usagecounter.studentLoans++;
			else if (userGroups.get("extern").contains(event.getBorrowerStatus()))
				usagecounter.externLoans++;
			else if (userGroups.get("intern").contains(event.getBorrowerStatus()))
				usagecounter.internLoans++;
			else if (userGroups.get("happ").contains(event.getBorrowerStatus()))
				usagecounter.happLoans++;
			else
				usagecounter.elseLoans++;
		} else if (event.getType().equals("return") && event.getBorrowerStatus() != null) {
			if (userGroups.get("student").contains(event.getBorrowerStatus()))
				usagecounter.studentLoans--;
			else if (userGroups.get("extern").contains(event.getBorrowerStatus()))
				usagecounter.externLoans--;
			else if (userGroups.get("intern").contains(event.getBorrowerStatus()))
				usagecounter.internLoans--;
			else if (userGroups.get("happ").contains(event.getBorrowerStatus()))
				usagecounter.happLoans--;
			else
				usagecounter.elseLoans--;
		} else if (event.getType().equals("inventory") && event.getItem() != null) {
			usagecounter.stock++;
			if (event.getItem().getItemStatus() != null) {
				if (itemGroups.get("lendable").contains(event.getItem().getItemStatus()))
					usagecounter.stockLendable++;
			}
			if (itemGroups.get("lbs").contains(event.getItem().getCollection())) {
				usagecounter.stockLBS++;
			}
		} else if (event.getType().equals("deletion") && event.getItem() != null) {
			usagecounter.stock--;
			if (event.getItem().getItemStatus() != null) {
				if (itemGroups.get("lendable").contains(event.getItem().getItemStatus()))
					usagecounter.stockLendable--;
			}
			if (itemGroups.get("lbs").contains(event.getItem().getCollection())) {
				usagecounter.stockLBS--;
			}
			usagecounter.stockDeleted++;
		} else if (event.getType().equals("request")) {
			usagecounter.requests++;
		} else if (event.getType().equals("hold")) {
			usagecounter.requests--;
		}
	}

	private void prepareUserCategories() {
		ResponseEntity<UserGroup[]> response = new RestTemplate().getForEntity(
				settingsUrl + "/userCategory",
				UserGroup[].class
		);
		userGroups = new HashMap<>();
		relevantUserCategories = "";
		irrelevantUserCategories = "";
		for (UserGroup userGroup : response.getBody()) {
			userGroups.put(userGroup.getName(), userGroup.getUserCategoriesAsString());
			if (userGroup.isRelevantForAnalysis())
				relevantUserCategories += userGroup.getUserCategoriesAsString() + " ";
			else
				irrelevantUserCategories += userGroup.getUserCategoriesAsString() + " ";
		}
	}

	private void prepareItemCategories() {
		ResponseEntity<ItemGroup[]> response = new RestTemplate().getForEntity(
				settingsUrl + "/userCategory",
				ItemGroup[].class
		);
		itemGroups = new HashMap<>();
		relevantItemCategories = "";
		for (ItemGroup itemGroup : response.getBody()) {
			userGroups.put(itemGroup.getName(), itemGroup.getItemCategoriesAsString());
			if (itemGroup.isRelevantForAnalysis())
				relevantItemCategories += itemGroup.getItemCategoriesAsString() + " ";

		}
	}
}
