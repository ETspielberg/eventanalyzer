package unidue.ub.batch.eventanalyzer;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.log4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.client.Traverson;
import unidue.ub.media.analysis.Eventanalysis;
import unidue.ub.media.monographs.Event;
import unidue.ub.settings.fachref.ItemGroup;
import unidue.ub.settings.fachref.Stockcontrol;
import unidue.ub.settings.fachref.UserGroup;

import java.net.URI;
import java.net.URISyntaxException;
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
public class EventAnalyzer {

    private static final Logger log = Logger.getLogger(EventAnalyzer.class);
    private String irrelevantUserCategories;
    private String relevantItemCategories;
    private Map<String, String> userGroups;
    private Map<String, String> itemGroups;

    EventAnalyzer() {
    }

    /**
     * Calculates the loan and request parameters for a given List of Events
     * with the parameters in the Stockcontrol. The results are stored
     * in an DocumentAnalysis object.
     *
     * @param events a list of Event-objects.
     */

    Eventanalysis analyze(List<Event> events, Stockcontrol stockcontrol) throws URISyntaxException {
        Collections.sort(events);

        UsageCounters usagecounter = new UsageCounters();

        //build new analysis and set some fields
        Eventanalysis analysis = new Eventanalysis();
        analysis.setDate(new Date());
        analysis.setCollection(stockcontrol.getCollections());
        analysis.setMaterials(stockcontrol.getMaterials());
        if (stockcontrol.getIdentifier() != null)
            analysis.setStockcontrolId(stockcontrol.getIdentifier());

        //start analysis
        if (!events.isEmpty()) {
            prepareUserCategories();
            prepareItemCategories();

            LocalDate TODAY = LocalDate.now();

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            Hashtable<Integer, Long> allMaxLoansAbs = new Hashtable<>();
            LocalDate startDate = TODAY.minus(stockcontrol.getYearsToAverage(), ChronoUnit.YEARS);
            LocalDate startDateRequests = TODAY.minus(stockcontrol.getYearsOfRequests(), ChronoUnit.YEARS);
            LocalDate miniumumDate = TODAY.minus(stockcontrol.getMinimumYears(), ChronoUnit.YEARS);
            Collections.sort(events);

            Integer yearsBefore = TODAY.getYear() - Integer.parseInt(events.get(0).getDate().substring(0, 4));


            for (int year = 0; year <= yearsBefore; year++) {
                allMaxLoansAbs.put(year, 0L);
            }
            UsageCounters oldUsagecounter = usagecounter.clone();

            for (Event event : events) {
                LocalDate eventDate;
                try {
                    eventDate = LocalDate.parse(event.getDate().substring(0, 10), dtf);
                } catch (Exception e) {
                    continue;
                }
                if (eventDate.isAfter(TODAY))
                    continue;

                usagecounter = updateItemCounter(event, usagecounter);
                int timeIntervall = TODAY.getYear() - eventDate.getYear();
                long loans = usagecounter.getCorrectedLoans();
                for (int i = timeIntervall; i <= yearsBefore; i++) {
                    long maxLoans = Math.max(allMaxLoansAbs.get(i), loans);
                    allMaxLoansAbs.replace(i, maxLoans);
                }
                if (eventDate.isAfter(startDate)) {
                    loans = Math.max(loans, oldUsagecounter.getCorrectedLoans());
                    analysis.setMaxLoansAbs(Math.max(loans, analysis.getMaxLoansAbs()));
                    double relativeLoan = Math.max(usagecounter.getCorrectedRelativLoan(), oldUsagecounter.getCorrectedRelativLoan());
                    analysis.setMaxRelativeLoan(Math.max(relativeLoan, analysis.getMaxRelativeLoan()));
                } else
                    oldUsagecounter = usagecounter.clone();
                long maxItemsNeeded = usagecounter.getAllLoans() + usagecounter.requests;
                if (eventDate.isAfter(startDateRequests)) {
                    analysis.setMaxNumberRequest(Math.max(usagecounter.requests, analysis.getNumberRequests()));
                    analysis.setMaxItemsNeeded(Math.max(maxItemsNeeded, analysis.getMaxItemsNeeded()));
                }


                // try to get the end date. If no end date is given in the event,
                // set the end date to the actual date.
                Event endEvent = event.getEndEvent();
                LocalDate endDate;
                if (endEvent != null)
                    try {
                        endDate = LocalDate.parse(endEvent.getDate().substring(0, 10), dtf);
                    } catch (Exception e) {
                        endDate = eventDate;
                    }
                else
                    endDate = TODAY;
                if (endDate.isBefore(startDate))
                    continue;
                else if (eventDate.isBefore(startDate))
                    eventDate = startDate;
                int days = (int) ChronoUnit.DAYS.between(eventDate, endDate) + 1;

                switch (event.getType()) {
                    // analyze loan events
                    case "loan": {
                        if (event.getBorrowerStatus() == null) {
                            log.info("no Borrower given");
                            usagecounter.daysLoaned += days;
                        } else {
                            if (irrelevantUserCategories.contains(event.getBorrowerStatus()))
                                usagecounter.daysStockLendable -= days; //shouldn't it be daysStock reduced???
                            else
                                usagecounter.daysLoaned += days;
                        }
                        break;
                    }
                    case "inventory": {
                        // analyze stock events
                        if (event.getItem() != null) {
                            if (event.getItem().getItemStatus() != null) {
                                if (relevantItemCategories.contains(event.getItem().getItemStatus()))
                                    usagecounter.daysStockLendable += days;
                            } else
                                usagecounter.daysStockLendable += days;
                        } else
                            usagecounter.daysStockLendable += days;
                        break;
                    }
                    case "request": {
                        // analyze request events
                        if (eventDate.isAfter(startDateRequests)) {
                            analysis.increaseNumberRequests();
                            usagecounter.daysRequested += days;
                        }
                    }
                }
            }
            double slope = calculateSlope(allMaxLoansAbs, yearsBefore);
            analysis.setSlope(slope);
            analysis.setDaysRequested(usagecounter.daysRequested);
            analysis.setMeanRelativeLoan(usagecounter.getMeanRelativeLoan());
            analysis.setLastStock(usagecounter.stock);
            analysis.setLastStockLendable(usagecounter.getStockLendable());


            double staticBuffer = stockcontrol.getStaticBuffer();
            double variableBuffer = stockcontrol.getVariableBuffer();

            int proposedDeletion = 0;
            double ratio = 1;
            if (analysis.getMaxRelativeLoan() != 0)
                ratio = analysis.getMeanRelativeLoan() / analysis.getMaxRelativeLoan();

            if (staticBuffer < 1 && variableBuffer < 1)
                proposedDeletion = ((int) ((usagecounter.stock - analysis.getMaxLoansAbs()) * (1
                        - staticBuffer
                        - variableBuffer * ratio)));
            else if (staticBuffer >= 1 && variableBuffer < 1)
                proposedDeletion = (
                        (int) ((usagecounter.stock - analysis.getMaxLoansAbs() - staticBuffer)
                                * (1 - variableBuffer * ratio)));
            else if (staticBuffer >= 1 && variableBuffer >= 1)
                proposedDeletion = (
                        (int) ((usagecounter.stock - analysis.getMaxLoansAbs() - staticBuffer)
                                - variableBuffer * ratio));
            else if (staticBuffer < 1 && variableBuffer < 1)
                proposedDeletion = (
                        (int) ((usagecounter.stock - analysis.getMaxLoansAbs()) * (1 - staticBuffer)
                                - variableBuffer * ratio));

            if (proposedDeletion < 0)
                analysis.setProposedDeletion(0);
            else
                analysis.setProposedDeletion(proposedDeletion);
            if (analysis.getProposedDeletion() == 0 && ratio > 0.5)
                analysis.setProposedPurchase((int) (-1 * analysis.getLastStock() * 0.001 * ratio));
            if (events.size() > 0) {
                if (LocalDate.parse(events.get(0).getDate().substring(0, 10), dtf).isAfter(miniumumDate))
                    analysis.setProposedDeletion(0);
            }

            if (analysis.getLastStock() - analysis.getProposedDeletion() < 2 && analysis.getLastStock() >= 3)
                analysis.setComment("ggf. umstellen");

            if ((double) analysis.getDaysRequested() / (double) analysis.getNumberRequests() >= stockcontrol.getMinimumDaysOfRequest()) {
                analysis.setProposedPurchase(analysis.getMaxItemsNeeded() - analysis.getLastStock());
            }
            analysis.setStatus("proposed");
        } else {
            analysis.setStatus("noEvents");
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

    private UsageCounters updateItemCounter(Event event, UsageCounters usagecounter) {
        switch (event.getType()) {
            case "loan": {
                if (event.getBorrowerStatus() != null) {
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
                } else
                    usagecounter.elseLoans++;
                break;
            }
            case "return": {
                if (event.getBorrowerStatus() != null) {
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
                } else
                    usagecounter.elseLoans--;
                break;
            }
            case "inventory": {
                usagecounter.stock++;
                if (event.getItem() != null)
                    if (event.getItem().getItemStatus() != null)
                        if (itemGroups.get("lendable").contains(event.getItem().getItemStatus()))
                            usagecounter.stockLendable++;
                break;
            }
            case "deletion": {
                usagecounter.stock--;
                usagecounter.stockDeleted++;
                if (event.getItem() != null)
                    if (event.getItem().getItemStatus() != null)
                        if (itemGroups.get("lendable").contains(event.getItem().getItemStatus()))
                            usagecounter.stockLendable--;
                break;
            }
            case "request": {
                usagecounter.requests++;
                break;
            }
            case "hold": {
                usagecounter.requests--;
                break;
            }
            case "cald": {
                usagecounter.calds++;
            }
        }
        return usagecounter;
    }

    private void prepareUserCategories() throws URISyntaxException {
        irrelevantUserCategories = "";
        Traverson traverson = new Traverson(new URI("http://localhost:8082/api/settings/userGroup"), MediaTypes.HAL_JSON);
        Traverson.TraversalBuilder tb = traverson.follow("$._links.self.href");
        ParameterizedTypeReference<Resources<UserGroup>> typeRefDevices = new ParameterizedTypeReference<Resources<UserGroup>>() {
        };
        Resources<UserGroup> resUsers = tb.toObject(typeRefDevices);
        userGroups = new HashMap<>();
        for (UserGroup userGroup : resUsers.getContent()) {
            userGroups.put(userGroup.getName(), userGroup.getUserCategoriesAsString());
            if (!userGroup.isRelevantForAnalysis())
                irrelevantUserCategories += userGroup.getUserCategoriesAsString() + " ";
        }
    }

    private void prepareItemCategories() throws URISyntaxException {
        relevantItemCategories = "";
        itemGroups = new HashMap<>();
        Traverson traverson = new Traverson(new URI("http://localhost:8082/api/settings/itemGroup"), MediaTypes.HAL_JSON);
        Traverson.TraversalBuilder tb = traverson.follow("$._links.self.href");
        ParameterizedTypeReference<Resources<ItemGroup>> typeRefDevices = new ParameterizedTypeReference<Resources<ItemGroup>>() {
        };
        Resources<ItemGroup> resItems = tb.toObject(typeRefDevices);
        for (ItemGroup itemGroup : resItems.getContent()) {
            itemGroups.put(itemGroup.getName(), itemGroup.getItemCategoriesAsString());
            if (itemGroup.isRelevantForAnalysis())
                relevantItemCategories += itemGroup.getItemCategoriesAsString() + " ";
        }
    }
}
