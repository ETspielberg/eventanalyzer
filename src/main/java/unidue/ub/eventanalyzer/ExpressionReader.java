package unidue.ub.eventanalyzer;

import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import unidue.ub.media.monographs.Expression;
import unidue.ub.media.monographs.Manifestation;
import unidue.ub.settings.fachref.Notation;
import unidue.ub.settings.fachref.Stockcontrol;

import java.util.*;

public class ExpressionReader implements ItemReader<Expression> {

    private RestTemplate restTemplate;

    private RestTemplate notationTemplate;

    private Stockcontrol stockcontrol;

    private int nextExpressionIndex = 0;

    private Hashtable<String,Expression> expressionData;

    @Value("${ub.statistics.settings.url}")
    private String settingsUrl;

    @Value("${ub.statistics.getter.url}")
    private String getterURL;

    ExpressionReader(Stockcontrol stockcontrol, RestTemplate restTemplate) {
        this.stockcontrol = stockcontrol;
        this.restTemplate = restTemplate;
        nextExpressionIndex = 0;
    }

    ExpressionReader() {
        nextExpressionIndex = 0;
    }

    public ExpressionReader setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        return this;
    }

    public ExpressionReader setNotationTemplate(RestTemplate notationTemplate) {
        this.notationTemplate = notationTemplate;
        return this;
    }

    public ExpressionReader setStockcontrol(Stockcontrol stockcontrol) {
        this.stockcontrol = stockcontrol;
        return this;
    }

    @Override
    public Expression read() throws Exception {
        if (noExpressionsFound()) {
            collectManifestation();
        }
        Expression nextExpression = null;
        if (nextExpressionIndex < expressionData.size()) {

            nextExpression = expressionData.get(nextExpressionIndex);
            nextExpressionIndex++;
        }
        return nextExpression;
    }

    private void collectManifestation() {
        List<Notation> notations = new ArrayList<>();
        String[] notationGroupStrings;
        if (stockcontrol.getSystemCode().contains(",")) {
            notationGroupStrings = stockcontrol.getSystemCode().split(",");
        } else {
            notationGroupStrings = new String[] {stockcontrol.getSystemCode()};
        }
        for (String notationGroupString : notationGroupStrings) {
            if (notationGroupString.contains("-")) {
                String startNotation = notationGroupString.substring(0,notationGroupString.indexOf("-"));
                String endNotation = notationGroupString.substring(notationGroupString.indexOf("-") + 1, notationGroupString.length());
                ResponseEntity<Notation[]> response = notationTemplate.getForEntity(
                        settingsUrl + "/notation/search/getNotationList?startNotation=" + startNotation + "&endNotation=" + endNotation,
                        Notation[].class
                );
                notations.addAll(Arrays.asList(response.getBody()));
            } else {
                ResponseEntity<Notation[]> response = notationTemplate.getForEntity(
                        settingsUrl + "/notation/search/getNotationListForNotationgroup?notationGroupName=" + notationGroupString,
                        Notation[].class
                );
                notations.addAll(Arrays.asList(response.getBody()));
            }
        }
        expressionData = new Hashtable<>();
        for (Notation notation : notations) {
            ResponseEntity<Manifestation[]> manifestations = restTemplate.getForEntity(
                    getterURL + "/manifestations?identifier=" + notation.getNotation() + "&mode=notation",
                    Manifestation[].class
            );
            for (Manifestation manifestation : manifestations.getBody()){
                if (expressionData.contains(manifestation.getShelfmarkBase())) {
                    expressionData.get(manifestation.getShelfmarkBase()).addDocument(manifestation);
                } else {
                    Expression expression = new Expression(manifestation.getShelfmarkBase());
                    expressionData.put(expression.getShelfmarkBase(),expression);                }
            }
        }
    }

    private boolean noExpressionsFound() {
        return (this.expressionData == null);
    }
}
