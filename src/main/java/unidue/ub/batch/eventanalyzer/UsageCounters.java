package unidue.ub.batch.eventanalyzer;

/**
 * Plain old java object holding the individual counters for different user
 * groups and for different stock groups.
 *
 * @author Eike Spielberg
 * @version 1
 */
public class UsageCounters implements Cloneable {

    long studentLoans;

    long internLoans;

    long externLoans;

    long happLoans;

    long elseLoans;

    long stock;

    long stockLendable;

    long stockDeleted;

    long requests;

    long daysLoaned;

    long daysStockLendable;

    long daysRequested;

    long calds;

    /**
     * Builds a new instance of a <code>Daylongimline</code>-object, setting the
     * individual counters to 0.
     */

    public UsageCounters() {
    }

    public void reset() {
        studentLoans = 0;
        internLoans = 0;
        externLoans = 0;
        happLoans = 0;
        elseLoans = 0;
        stock = 0;
        stockLendable = 0;
        stockDeleted = 0;
        requests = 0;
        calds = 0;
        daysLoaned = 0;
        daysRequested = 0;
        daysStockLendable = 0;
    }

    // studentLoas

    /**
     * retrieves the value of items which are loaned by students at this day.
     *
     * @return studentLoans the value of items which are loaned by students
     */
    public long getStudentLoans() {
        return studentLoans;
    }

    /**
     * sets the value of items which are loaned by students at this day.
     *
     * @param studentLoans new value of items loaned by students
     * @return dit the updated <code>UsageCounters</code>
     */
    public UsageCounters setStudentLoans(long studentLoans) {
        this.studentLoans = studentLoans;
        return this;
    }

    // externLoans

    /**
     * retrieves the value of items which are loaned by external users at this
     * day.
     *
     * @return externLoans the value of items which are loaned by external users
     */
    public long getExternLoans() {
        return externLoans;
    }

    /**
     * sets the value of items which are loaned by external users at this day.
     *
     * @param externLoans new value of items loaned by external users
     * @return dit the updated <code>UsageCounters</code>
     */
    public UsageCounters setExternLoans(long externLoans) {
        this.externLoans = externLoans;
        return this;
    }

    // internLoans

    /**
     * retrieves the value of items which are loaned by non-student members of
     * the university at this day.
     *
     * @return internLoans the value of items which are loaned by non-student members of
     * the university
     */
    public long getInternLoans() {
        return internLoans;
    }

    /**
     * sets the value of items which are loaned by non-student members of the
     * university at this day.
     *
     * @param internLoans new value of items loaned by non-student members of the
     *                    university
     * @return dit the updated <code>UsageCounters</code>
     */
    public UsageCounters setInternLoans(long internLoans) {
        this.internLoans = internLoans;
        return this;
    }

    // happLoans

    /**
     * retrieves the value of items which are located in permanent loan in
     * scientific departments at this day.
     *
     * @return happLoans the value of items which are located in permanent loan in
     * scientific departments
     */
    public long getHappLoans() {
        return happLoans;
    }

    /**
     * sets the value of items which are located in permanent loan in scientific
     * departments.
     *
     * @param happLoans new value of items located in permanent loan in scientific
     *                  departments
     * @return dit the updated <code>UsageCounters</code>
     */
    public UsageCounters setHappLoans(long happLoans) {
        this.happLoans = happLoans;
        return this;
    }

    // elseLoans

    /**
     * retrieves the value of items which are loaned by other users at this day.
     *
     * @return elseLoans the value of items which are loaned by other users
     */
    public long getElseLoans() {
        return elseLoans;
    }

    /**
     * sets the value of items which are loaned by other users at this day.
     *
     * @param elseLoans new value of items loaned by other users
     * @return dit the updated <code>UsageCounters</code>
     */
    public UsageCounters setElseLoans(long elseLoans) {
        this.elseLoans = elseLoans;
        return this;
    }

    // stock

    /**
     * retrieves the value of items at this day.
     *
     * @return stock the value of items
     */
    public long getStock() {
        return stock;
    }

    /**
     * sets the value of all items at this day.
     *
     * @param stock new value of all items
     * @return dit the updated <code>UsageCounters</code>
     */
    public UsageCounters setStock(long stock) {
        this.stock = stock;
        return this;
    }

    // stockLendable

    /**
     * retrieves the value of circulation items.
     *
     * @return stockLendable the value of circulation items
     */
    public long getStockLendable() {
        return stockLendable;
    }

    /**
     * sets the value of all circulation items at this day.
     *
     * @param stockLendable new value of all circulation items
     * @return dit the updated <code>UsageCounters</code>
     */
    public UsageCounters setStockLendable(long stockLendable) {
        this.stockLendable = stockLendable;
        return this;
    }


    // stockDeleted

    /**
     * retrieves the value of deleted items.
     *
     * @return stockDeleted the value of deleted items
     */
    public long getStockDeleted() {
        return stockDeleted;
    }

    /**
     * sets the value of items being deleted up to that day.
     *
     * @param stockDeleted new value of items being deleted
     * @return dit the updated <code>UsageCounters</code>
     */
    public UsageCounters setStockDeleted(long stockDeleted) {
        this.stockDeleted = stockDeleted;
        return this;
    }

    public long getCalds() {
        return calds;
    }

    public UsageCounters setCalds(long calds) {
        this.calds = calds;
        return this;
    }

    /**
     * multiplies all counters with a given number of days.
     *
     * @param days days the individual counters are multiplied with
     * @return dit multiplied <code>UsageCounters</code>
     */
    public UsageCounters times(long days) {
        UsageCounters product = new UsageCounters();
        product.setStudentLoans(studentLoans * days).setInternLoans(internLoans * days)
                .setExternLoans(externLoans * days).setHappLoans(happLoans * days).setElseLoans(elseLoans * days)
                .setStock(stock * days).setStockLendable(stockLendable * days).setRequests(requests * days);
        return product;
    }

    public long getRequests() {
        return requests;
    }

    public void setRequests(long requests) {
        this.requests = requests;
    }

    public long getDaysLoaned() {
        return daysLoaned;
    }

    public void setDaysLoaned(long daysLoaned) {
        this.daysLoaned = daysLoaned;
    }

    public long getDaysStockLendable() {
        return daysStockLendable;
    }

    public void setDaysStockLendable(long daysStockLendable) {
        this.daysStockLendable = daysStockLendable;
    }

    public long getDaysRequested() {
        return daysRequested;
    }

    public void setDaysRequested(long daysRequested) {
        this.daysRequested = daysRequested;
    }

    public void addDaysRequested(long daysRequested) {
        this.daysRequested += daysRequested;
    }

    /**
     * retrieves the value of all items loaned by all user groups at this day.
     *
     * @return allLoans sum over all types of loan
     */
    public long getAllLoans() {
        long allLoans = studentLoans + internLoans + externLoans + happLoans + elseLoans;
        return allLoans;
    }

    public long getCorrectedLoans() {
        return studentLoans + internLoans + externLoans + elseLoans;
    }

    public double getRelativeLoan() {
        return (double) getAllLoans() / (double) stock;
    }

    public double getCorrectedRelativLoan() {
        if (getReducedStock() > 0)
            return (double) (studentLoans + internLoans + externLoans + elseLoans) / (double) getReducedStock();
        else return 0;
    }

    public long getReducedStock() {
        return stock - happLoans;
    }

    public long getMaxLoansAbs() {
        return studentLoans + internLoans + externLoans + elseLoans + happLoans;
    }

    public double getMeanRelativeLoan() {
        if (daysStockLendable != 0)
            return ((double) daysLoaned / (double) daysStockLendable);
        else return 0;
    }

    public UsageCounters clone() {
        UsageCounters clone = new UsageCounters();
        clone.studentLoans = studentLoans;
        clone.internLoans = internLoans;
        clone.externLoans = externLoans;
        clone.happLoans = happLoans;
        clone.elseLoans = elseLoans;
        clone.stock = stock;
        clone.stockLendable = stockLendable;
        clone.stockDeleted = stockDeleted;
        clone.requests = requests;
        clone.daysLoaned = daysLoaned;
        clone.daysStockLendable = daysStockLendable;
        clone.daysRequested = daysRequested;
        return clone;
    }

    @Override
    public String toString() {
        String data = studentLoans + "; " + internLoans + "; " + externLoans + "; " + happLoans + "; " + elseLoans + "; " +
                stock + "; " + stockLendable + "; " + stockDeleted + "; " +
                requests + "; " + daysLoaned + "; " + daysStockLendable + "; " + daysRequested;
        return data;
    }

}
