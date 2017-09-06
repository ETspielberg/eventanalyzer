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

    private boolean noExpressionsFound;

    private Enumeration<Expression> expressionEnumeration;

    private Stockcontrol stockcontrol;

    ExpressionReader(Stockcontrol stockcontrol) { noExpressionsFound = true;
    this.stockcontrol = stockcontrol;}

    @Override
    public Expression read() throws Exception {
        if (noExpressionsFound) {
            collectManifestation();
        }
        if (expressionEnumeration.hasMoreElements())
            return expressionEnumeration.nextElement();
        else return null;
    }

    private void collectManifestation() {
        Hashtable<String,Expression> expressionData = new Hashtable<>();
        ManifestationReader manifestationReader = new ManifestationReader(stockcontrol);
        List<Manifestation> manifestations  = manifestationReader.getManifestations();
        if(manifestations.size() == 0)
            return;
        else {
            for (Manifestation manifestation : manifestations) {
                if (expressionData.contains(manifestation.getShelfmarkBase())) {
                    expressionData.get(manifestation.getShelfmarkBase()).addDocument(manifestation);
                } else {
                    Expression expression = new Expression(manifestation.getShelfmarkBase());
                    expressionData.put(expression.getShelfmarkBase(), expression);
                }
            }
            expressionEnumeration = expressionData.elements();
            noExpressionsFound = false;
        }
    }
}
