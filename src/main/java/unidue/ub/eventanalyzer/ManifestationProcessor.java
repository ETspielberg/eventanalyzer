package unidue.ub.eventanalyzer;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import unidue.ub.media.analysis.Eventanalysis;
import unidue.ub.media.monographs.Event;
import unidue.ub.media.monographs.Item;
import unidue.ub.media.monographs.Manifestation;
import unidue.ub.settings.fachref.Stockcontrol;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Scope(value = "step")
public class ManifestationProcessor implements ItemProcessor<Manifestation,Eventanalysis> {

    private static final Logger log = LoggerFactory.getLogger(ManifestationProcessor.class);

    @Value("${ub.statistics.settings.url}")
    private String settingsUrl;

    @Value("${ub.statistics.data.url}")
    private String dataURL;

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

    private Stockcontrol stockcontrol;

    @Override
    public Eventanalysis process(final Manifestation manifestation) throws Exception {
        log.info("analyzing manifestation " + manifestation.getTitleID() + " and shelfmark " + manifestation.getShelfmark());

        List<Event> events = new ArrayList<>();
        ItemFilter itemFilter = new ItemFilter(stockcontrol.getCollections(),stockcontrol.getMaterials());
        for (Item item : manifestation.getItems()) {
            if (itemFilter.matches(item))
                events.addAll(item.getEvents());
        }

        LocalDate TODAY = LocalDate.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Hashtable<Integer, Integer> allMaxLoansAbs = new Hashtable<>();
        Eventanalysis analysis = new Eventanalysis();
        analysis.setTitleId(manifestation.getTitleID());
        analysis.setDate(new Date());

        if (stockcontrol.getIdentifier() != null)
            analysis.setStockcontrolId(stockcontrol.getIdentifier());

        LocalDate scpStartDate = TODAY.minus((long) stockcontrol.getYearsToAverage(), ChronoUnit.YEARS);
        LocalDate scpStartYearRequests = TODAY.minus((long) stockcontrol.getYearsOfRequests(), ChronoUnit.YEARS);
        LocalDate scpMiniumumDate = TODAY.minus((long) stockcontrol.getMinimumYears(), ChronoUnit.YEARS);
        Collections.sort(events);

        // prepare the timeline to evaluate the relative lent
        TimelineGenerator tlg = new TimelineGenerator(events);
        List<ItemCounter> timeline = tlg.getTimeline();

        if (timeline.size() > 0) {
            // initialize the EventAnalyses and fill the first values
            Integer yearsBefore = TODAY.getYear() - LocalDate.parse(timeline.get(0).getDay(), dtf).getYear();
            for (int year = 0; year <= yearsBefore; year++) {
                allMaxLoansAbs.put(year, 0);
            }

            int oldLoans = 0;
            analysis.setLastStock(timeline.get(timeline.size() - 1).getStockLendable());
            for (ItemCounter dayInTimeline : timeline) {
                LocalDate eventDate = LocalDate.parse(dayInTimeline.getDay(), dtf);
                int timeIntervall = TODAY.getYear() - eventDate.getYear();

                // if there is a wrong date given, skip this entry
                if (eventDate.isAfter(TODAY)) {
                    continue;
                }

                int loans = dayInTimeline.getElseLoans() + dayInTimeline.getStudentLoans() + dayInTimeline.getExternLoans()
                        + dayInTimeline.getInternLoans();
                for (int i = timeIntervall; i <= yearsBefore; i++) {
                    int maxLoans = Math.max(allMaxLoansAbs.get(i), loans);
                    allMaxLoansAbs.replace(i, maxLoans);
                }
                if (eventDate.isAfter(scpStartDate)) {
                    loans = Math.max(loans, oldLoans);
                    analysis.setMaxLoansAbs(Math.max(loans, analysis.getMaxLoansAbs()));
                    int reducedStock = dayInTimeline.getStockLendable() - dayInTimeline.getHappLoans();
                    if (reducedStock > 0) {
                        double relativeLoan = (double) loans / (double) reducedStock;
                        analysis.setMaxRelativeLoan(Math.max(relativeLoan, analysis.getMaxRelativeLoan()));
                    }
                } else
                    oldLoans = loans;
                int requests = dayInTimeline.getElseRequests() + dayInTimeline.getStudentRequests()
                        + dayInTimeline.getInternRequests();
                int maxItemsNeeded = dayInTimeline.getStockLendable() + requests;
                if (eventDate.isAfter(scpStartYearRequests)) {
                    analysis.setMaxNumberRequest(Math.max(requests, analysis.getNumberRequests()));
                    analysis.setMaxItemsNeeded(Math.max(maxItemsNeeded, analysis.getMaxItemsNeeded()));
                }
            }
            SimpleRegression trend = new SimpleRegression();
            for (int year = 1; year <= yearsBefore; year++) {
                trend.addData(year, (double) allMaxLoansAbs.get(year));
            }
            double slope = 0;
            if (trend.getN() > 1)
                slope = trend.getSlope();
            analysis.setSlope(slope);
        }
        int daysLoaned = 0;
        int daysStockLendable = 0;
        int daysRequested = 0;

        // sum up the days loaned, days in stock, all lendable days in stock,
        // days in the lbs, both for the whole time range as well as for the
        // time frame given.
        for (Event event : events) {
            // set the start date
            LocalDate startDate = LocalDate.parse(event.getDate().substring(0, 10), dtf);

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
            else if (startDate.isBefore(scpStartDate))
                startDate = scpStartDate;
            int days = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;

            // analyze loan events
            if (event.getType().equals("loan")) {
                if (event.getBorrowerStatus() == null) {
                    continue;
                }
                String countableUserGroups = studentUser + externUser + internUser;

                if (countableUserGroups.contains(event.getBorrowerStatus()))
                    daysLoaned += days;
                else if (happUser.contains(event.getBorrowerStatus()))
                    daysStockLendable -= days;
                else
                    daysLoaned += days;

                // analyze stock events
            } else if (event.getType().equals("inventory")) {
                if (event.getItem() != null) {
                    if (event.getItem().getItemStatus() != null) {
                        if (lendable.contains(event.getItem().getItemStatus()))
                            daysStockLendable += days;
                    }
                }

                // analyze request events
            } else if (event.getType().equals("request")) {
                if (startDate.isAfter(scpStartYearRequests)) {
                    analysis.increaseNumberRequests();
                    daysRequested += days;
                }
            }
        }
        analysis.setDaysRequested(daysRequested);
        if (daysStockLendable != 0) {
            double meanRelativeLoan = (double) daysLoaned / (double) daysStockLendable;
            analysis.setMeanRelativeLoan(meanRelativeLoan);
        }

        if (analysis.getMaxRelativeLoan() != 0) {
            double ratio = analysis.getMeanRelativeLoan()/analysis.getMaxRelativeLoan();
            if (stockcontrol.getStaticBuffer() < 1 && stockcontrol.getVariableBuffer() < 1)
                analysis.setProposedDeletion((int) ((analysis.getLastStock() - analysis.getMaxLoansAbs()) * (1
                        - stockcontrol.getStaticBuffer()
                        - stockcontrol.getVariableBuffer() *ratio)));
            else if (stockcontrol.getStaticBuffer() >= 1 && stockcontrol.getVariableBuffer() < 1)
                analysis.setProposedDeletion(
                        (int) ((analysis.getLastStock() - analysis.getMaxLoansAbs() - stockcontrol.getStaticBuffer())
                                * (1 - stockcontrol.getVariableBuffer() * ratio)));
            else if (stockcontrol.getStaticBuffer() >= 1 && stockcontrol.getVariableBuffer() >= 1)
                analysis.setProposedDeletion(
                        (int) ((analysis.getLastStock() - analysis.getMaxLoansAbs() - stockcontrol.getStaticBuffer())
                                - stockcontrol.getVariableBuffer() * ratio));
            else if (stockcontrol.getStaticBuffer() < 1 && stockcontrol.getVariableBuffer() < 1)
                analysis.setProposedDeletion(
                        (int) ((analysis.getLastStock() - analysis.getMaxLoansAbs())*(1-stockcontrol.getStaticBuffer())
                                - stockcontrol.getVariableBuffer() * ratio));

            if (analysis.getProposedDeletion() < 0)
                analysis.setProposedDeletion(0);
            if (analysis.getProposedDeletion() == 0 && ratio > 0.5)
                analysis.setProposedPurchase((int) (-1 * analysis.getLastStock() * 0.001 * ratio));
        } else {
            if (stockcontrol.getStaticBuffer() < 1)
                analysis.setProposedDeletion(
                        (int) ((analysis.getLastStock() - analysis.getMaxLoansAbs()) * (1 - stockcontrol.getStaticBuffer())));
            else
                analysis.setProposedDeletion(
                        (int) (analysis.getLastStock() - analysis.getMaxLoansAbs() - stockcontrol.getStaticBuffer()));
        }
        if (events.size() > 0) {
            if (LocalDate.parse(events.get(0).getDate().substring(0, 10), dtf).isAfter(scpMiniumumDate))
                analysis.setProposedDeletion(0);
        }

        if (analysis.getLastStock() - analysis.getProposedDeletion() < 2 && analysis.getLastStock() >= 3)
            analysis.setComment("ggf. umstellen");

        if ((double) analysis.getDaysRequested() / (double) analysis.getNumberRequests() >= stockcontrol
                .getMinimumDaysOfRequest()) {
            analysis.setProposedPurchase(analysis.getMaxItemsNeeded() - analysis.getLastStock());
        }
        return analysis;
    }

    private ManifestationProcessor setStockcontrol(Stockcontrol stockcontrol) {
        this.stockcontrol = stockcontrol;
        return this;
    }
}
