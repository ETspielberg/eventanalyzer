package unidue.ub.eventanalyzer;

/**
 * Plain old java object holding the individual counters for different user
 * groups and for different stock groups.
 * 
 * @author Eike Spielberg
 * @version 1
 */
public class UsageCounters implements Cloneable  {
	
	long studentLoans;

	long internLoans;

	long externLoans;

	long happLoans;

	long elseLoans;

	long stock;

	long stockLBS;

	long stockLendableNonLBS;

	long stockLendable;

	long stockNonLendable;

	long stockDeleted;

	long requests;
	
	long daysLoaned;
	
	long daysStockLendable;
	
	long daysRequested;

	/**
	 * Builds a new instance of a <code>Daylongimline</code>-object, setting the
	 * individual counters to 0.
	 * 
	 */

	public UsageCounters() {
		studentLoans = 0;
		internLoans = 0;
		externLoans = 0;
		happLoans = 0;
		elseLoans = 0;
		stock = 0;
		stockLBS = 0;
		stockLendableNonLBS = 0;
		stockLendable = 0;
		stockNonLendable = 0;
		stockDeleted = 0;
		requests = 0;
		daysLoaned = 0;
		daysRequested = 0;
		daysStockLendable = 0;
	}

	// studentLoas
	/**
	 * sets the value of items which are loaned by students at this day.
	 * 
	 * @param studentLoans
	 *            new value of items loaned by students
	 * 
	 * @return dit the updated <code>UsageCounters</code>
	 */
	public UsageCounters setStudentLoans(long studentLoans) {
		this.studentLoans = studentLoans;
		return this;
	}

	/**
	 * retrieves the value of items which are loaned by students at this day.
	 * 
	 * @return studentLoans the value of items which are loaned by students
	 * 
	 * @return studentLoans the value of items which are loaned by students
	 */
	public long getStudentLoans() {
		return studentLoans;
	}

	// externLoans
	/**
	 * sets the value of items which are loaned by external users at this day.
	 * 
	 * @param externLoans
	 *            new value of items loaned by external users
	 * @return dit the updated <code>UsageCounters</code>
	 */
	public UsageCounters setExternLoans(long externLoans) {
		this.externLoans = externLoans;
		return this;
	}

	/**
	 * retrieves the value of items which are loaned by external users at this
	 * day.
	 * @return externLoans the value of items which are loaned by external users
	 * 
	 */
	public long getExternLoans() {
		return externLoans;
	}

	// internLoans
	/**
	 * sets the value of items which are loaned by non-student members of the
	 * university at this day.
	 * 
	 * @param internLoans
	 *            new value of items loaned by non-student members of the
	 *            university
	 * @return dit the updated <code>UsageCounters</code>
	 */
	public UsageCounters setInternLoans(long internLoans) {
		this.internLoans = internLoans;
		return this;
	}

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

	// happLoans
	/**
	 * sets the value of items which are located in permanent loan in scientific
	 * departments.
	 * 
	 * @param happLoans
	 *            new value of items located in permanent loan in scientific
	 *            departments
	 * @return dit the updated <code>UsageCounters</code>
	 */
	public UsageCounters setHappLoans(long happLoans) {
		this.happLoans = happLoans;
		return this;
	}

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

	// elseLoans
	/**
	 * sets the value of items which are loaned by other users at this day.
	 * 
	 * @param elseLoans
	 *            new value of items loaned by other users
	 * @return dit the updated <code>UsageCounters</code>
	 */
	public UsageCounters setElseLoans(long elseLoans) {
		this.elseLoans = elseLoans;
		return this;
	}

	/**
	 * retrieves the value of items which are loaned by other users at this day.
	 * 
	 * @return elseLoans the value of items which are loaned by other users
	 */
	public long getElseLoans() {
		return elseLoans;
	}

	// stock
	/**
	 * sets the value of all items at this day.
	 * 
	 * @param stock
	 *            new value of all items
	 * @return dit the updated <code>UsageCounters</code>
	 */
	public UsageCounters setStock(long stock) {
		this.stock = stock;
		return this;
	}

	/**
	 * retrieves the value of items at this day.
	 * 
	 * @return stock the value of items
	 */
	public long getStock() {
		return stock;
	}

	// stockLendable
	/**
	 * sets the value of all circulation items at this day.
	 * 
	 * @param stockLendable
	 *            new value of all circulation items
	 * @return dit the updated <code>UsageCounters</code>
	 */
	public UsageCounters setStockLendable(long stockLendable) {
		this.stockLendable = stockLendable;
		return this;
	}

	/**
	 * retrieves the value of circulation items.
	 * 
	 * @return stockLendable the value of circulation items
	 */
	public long getStockLendable() {
		return stockLendable;
	}

	// stockLBS
	/**
	 * sets the value of items in the textbook collection at this day.
	 * 
	 * @param stockLBS
	 *            new value of items in the textbook collection
	 * @return dit the updated <code>UsageCounters</code>
	 */
	public UsageCounters setStockLBS(long stockLBS) {
		this.stockLBS = stockLBS;
		return this;
	}

	/**
	 * retrieves the value of items in the textbook collection.
	 * 
	 * @return stockLBS the value of items in the textbook collection
	 */
	public long getStockLBS() {
		return stockLBS;
	}

	// stockDeleted
	/**
	 * sets the value of items being deleted up to that day.
	 * 
	 * @param stockDeleted
	 *            new value of items being deleted
	 * @return dit the updated <code>UsageCounters</code>
	 */
	public UsageCounters setStockDeleted(long stockDeleted) {
		this.stockDeleted = stockDeleted;
		return this;
	}

	/**
	 * retrieves the value of deleted items.
	 * 
	 * @return stockDeleted the value of deleted items
	 */
	public long getStockDeleted() {
		return stockDeleted;
	}

	// stockLendableNonLBS
	/**
	 * sets the value of circulation items being not part of the textbook
	 * collection at this day.
	 * 
	 * @param stockLendableNonLBS
	 *            new value of circulating items being not part of the textbook
	 *            collection
	 * @return dit the updated <code>UsageCounters</code>
	 */
	public UsageCounters setStockLendableNonLBS(long stockLendableNonLBS) {
		this.stockLendableNonLBS = stockLendableNonLBS;
		return this;
	}

	/**
	 * retrieves the value of circulation items being not part of the textbook
	 * collection at this day.
	 * 
	 * @return stockLendableNonLBS the value of circulation items being not part of the textbook
	 * collection 
	 */
	public long getStockLendableNonLBS() {
		return stockLendableNonLBS;
	}

	// stockNonLendable
	/**
	 * sets the value of non-circulation items at this day.
	 * 
	 * @param stockNonLendable
	 *            new value of circulating items being not part of the textbook
	 *            collection
	 * @return dit the updated <code>UsageCounters</code>
	 */
	public UsageCounters setStockNonLendable(long stockNonLendable) {
		this.stockNonLendable = stockNonLendable;
		return this;
	}

	/**
	 * retrieves the value of non-circulation items.
	 * 
	 * @return stockNonLendable the value of non-circulation items
	 */
	public long getStockNonLendable() {
		return stockNonLendable;
	}

	
	
		/**
	 * adds another <code>UsageCounters</code> counter. all the individual
	 * counters are summed up, the day is kept from the original one.
	 * 
	 * @param dit
	 *            new value of items loaned by students
	 * @return dit added UsageCounters
	 */
	public UsageCounters plus(UsageCounters dit) {
		studentLoans += dit.studentLoans;
		internLoans += dit.internLoans;
		externLoans += dit.externLoans;
		happLoans += dit.happLoans;
		elseLoans += dit.elseLoans;
		stock += dit.stock;
		stockLBS += dit.stockLBS;
		stockLendableNonLBS += dit.stockLendableNonLBS;
		stockLendable += dit.stockLendable;
		stockNonLendable += dit.stockNonLendable;
		stockDeleted += dit.stockDeleted;
		requests += dit.requests;
		daysRequested += dit.daysRequested;
		daysStockLendable += dit.daysStockLendable;
		daysLoaned += dit.daysLoaned;
		return this;
	}

	/**
	 * multiplies all counters with a given number of days.
	 * 
	 * @param days
	 *            days the individual counters are multiplied with
	 * @return dit multiplied <code>UsageCounters</code>
	 */
	public UsageCounters times(long days) {
		UsageCounters product = new UsageCounters();
		product.setStudentLoans(studentLoans * days).setInternLoans(internLoans * days)
				.setExternLoans(externLoans * days).setHappLoans(happLoans * days).setElseLoans(elseLoans * days)
				.setStock(stock * days).setStockLBS(stockLBS * days).setStockLendableNonLBS(stockLendableNonLBS * days)
				.setStockLendable(stockLendable * days).setStockNonLendable(stockNonLendable * days)
				.setRequests(requests * days);
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
		this.daysRequested  += daysRequested;
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

	public double getRelativeLoan() {
		if (getReducedStock() > 0)
		return getAllLoans() / getReducedStock();
		else return 0;
	}

	public long getReducedStock() {
		return stock-happLoans;
	}

	public long getMaxLoansAbs() {
		return studentLoans + internLoans + externLoans + elseLoans + happLoans;
	}

	public double getMeanRelativeLoan() {
		if (daysStockLendable != 0)
			return daysLoaned / daysStockLendable;
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
		clone.stockLBS = stockLBS;
		clone.stockLendableNonLBS = stockLendableNonLBS;
		clone.stockLendable = stockLendable;
		clone.stockNonLendable = stockNonLendable;
		clone.stockDeleted = stockDeleted;
		clone.requests = requests;
		clone.daysLoaned = daysLoaned;
		clone.daysStockLendable = daysStockLendable;
		clone.daysRequested = daysRequested;
		return clone;

	}

}
